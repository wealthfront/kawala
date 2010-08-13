/**
 * Copyright 2010 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.converters;

import static com.kaching.platform.converters.CollectionOfElementsConverter.COLLECTION_KINDS;
import static com.kaching.platform.converters.NativeConverters.C_BOOLEAN;
import static com.kaching.platform.converters.NativeConverters.C_BYTE;
import static com.kaching.platform.converters.NativeConverters.C_CHAR;
import static com.kaching.platform.converters.NativeConverters.C_DOUBLE;
import static com.kaching.platform.converters.NativeConverters.C_FLOAT;
import static com.kaching.platform.converters.NativeConverters.C_INT;
import static com.kaching.platform.converters.NativeConverters.C_LONG;
import static com.kaching.platform.converters.NativeConverters.C_SHORT;
import static com.kaching.platform.converters.NativeConverters.C_STRING;
import static java.lang.String.format;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.BitSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.MoreTypes;
import com.kaching.platform.common.Option;
import com.kaching.platform.common.types.Unification;
import com.kaching.platform.converters.ConstructorAnalysis.FormalParameter;

class InstantiatorImplFactory<T> {

  private final static ConcurrentMap<Type, Converter<?>> BASE_CONVERTERS =
      ImmutableMap.<Type, Converter<?>> builder()
      .put(String.class, C_STRING)
      .put(Boolean.TYPE, C_BOOLEAN)
      .put(Byte.TYPE, C_BYTE)
      .put(Character.TYPE, C_CHAR)
      .put(Double.TYPE, C_DOUBLE)
      .put(Float.TYPE, C_FLOAT)
      .put(Integer.TYPE, C_INT)
      .put(Long.TYPE, C_LONG)
      .put(Short.TYPE, C_SHORT)
      .build();

  private final Errors errors = new Errors();
  private final ConverterBinderImpl binder = new ConverterBinderImpl(errors);
  private final Class<T> klass;

  private InstantiatorImplFactory(Class<T> klass) {
    this.klass = klass;
  }

  static <T> InstantiatorImplFactory<T> createFactory(Class<T> klass) {
    return new InstantiatorImplFactory<T>(klass);
  }

  ConverterBinder binder() {
    return binder;
  }

  InstantiatorImpl<T> build() {
    // 1. find constructor
    for (Constructor<T> constructor : getConstructor()) {
      constructor.setAccessible(true);
      // 2. for each parameter, find converter
      Type[] genericParameterTypes = constructor.getGenericParameterTypes();
      Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
      int parametersCount = genericParameterTypes.length;
      Converter<?>[] converters =
          parametersCount == 0 ? null : new Converter<?>[parametersCount];
      BitSet optionality = new BitSet();
      String[] defaultValues = null;
      Object[] defaultConstants = null;
      next_parameter: for (int i = 0; i < parametersCount; i++) {
        Annotation[] annotations = parameterAnnotations[i];
        Type genericParameterType = genericParameterTypes[i];
        for (final Converter<?> converter : createConverter(
            genericParameterType)) {
          converters[i] = converter;
          for (Optional optional : getOptionalAnnotation(annotations)) {
            String defaultValue = optional.value();
            String defaultConstant = optional.constant();

            if (!defaultValue.isEmpty() && !defaultConstant.isEmpty()) {
              errors.cannotSpecifyDefaultValueAndConstant(optional);
              continue next_parameter;
            } else if (!defaultValue.isEmpty()) {
              try {
                converter.fromString(defaultValue);
                if (defaultValues == null) {
                  defaultValues = new String[parametersCount];
                }
                defaultValues[i] = defaultValue;
              } catch (RuntimeException e) {
                errors.incorrectDefaultValue(defaultValue, e);
              }
            } else if (!defaultConstant.isEmpty()) {
              String[] parts = defaultConstant.split("#");
              Class<?> container;
              String constantName;
              switch (parts.length) {
                case 1:
                  container = klass;
                  constantName = parts[0];
                  break;

                case 2:
                  try {
                    container = Class.forName(parts[0]);
                  } catch (ClassNotFoundException e) {
                    errors.unableToResolveFullyQualifiedConstant(defaultConstant);
                    continue next_parameter;
                  }
                  constantName = parts[1];
                  break;

                default:
                  errors.unableToResolveFullyQualifiedConstant(defaultConstant);
                  continue next_parameter;
              }

              Field fieldOfConstant;
              try {
                fieldOfConstant = container.getDeclaredField(constantName);
              } catch (SecurityException e) {
                // TODO(pascal): better handling?
                throw new RuntimeException(e);
              } catch (NoSuchFieldException e) {
                errors.unableToResolveConstant(container, defaultConstant);
                continue next_parameter;
              }

              if ((fieldOfConstant.getModifiers() & Modifier.STATIC) == 0 ||
                  (fieldOfConstant.getModifiers() & Modifier.FINAL) == 0) {
                errors.constantIsNotStaticFinal(container, constantName);
                continue next_parameter;
              }

              // TODO(pascal): improve check. Should use MoreTypes.isAssignableFrom.
              if (!genericParameterType.equals(fieldOfConstant.getType())) {
                errors.constantHasIncompatibleType(container, constantName);
                continue next_parameter;
              }

              if (defaultConstants == null) {
                defaultConstants = new Object[parametersCount];
              }
              try {
                defaultConstants[i] = fieldOfConstant.get(null);
              } catch (IllegalArgumentException e) {
                // TODO(pascal): better handling?
                throw new RuntimeException(e);
              } catch (IllegalAccessException e) {
                // TODO(pascal): better handling?
                throw new RuntimeException(e);
              }
            } else {
              // optional literal types are not allowed to omit default values
              if (BASE_CONVERTERS.containsKey(genericParameterType) &&
                  !String.class.equals(genericParameterType)) {
                errors.optionalLiteralParameterMustHaveDefault(i);
              }
            }
            optionality.set(i);
          }
        }
      }
      // 3. reverse mapping (fields to parameters)
      Field[] fields = null;
      try {
        fields = retrieveFieldsFromAssignment(
            parametersCount, ConstructorAnalysis.analyse(klass, constructor));
      } catch (IOException e) {
        throw new IllegalStateException("should be able to access the class");
      } catch (ConstructorAnalysis.IllegalConstructorException e) {
        errors.illegalConstructor(klass, e.getMessage());
      }
      // 4. done
      errors.throwIfHasErrors();
      return new InstantiatorImpl<T>(
          constructor, converters, fields, optionality, defaultValues, defaultConstants);
    }

    // This program point is only reachable in erroneous cases and the next
    // statement will therefore end the control flow.
    errors.throwIfHasErrors();
    throw new IllegalStateException();
  }

  private Option<Optional> getOptionalAnnotation(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof Optional) {
        return Option.some((Optional) annotation);
      }
    }
    return Option.none();
  }

  @VisibleForTesting
  @SuppressWarnings("unchecked")
  Option<? extends Converter<?>> createConverter(Type targetType) {
    int sizeBefore = errors.size();
    // 1. explicit binding
    Map<TypeLiteral<?>, Converter<?>> instances = binder.getInstances();
    Map<TypeLiteral<?>, Class<? extends Converter<?>>> bindings = binder.getBindings();
    if (instances != null) {
      for (Entry<TypeLiteral<?>, Converter<?>> entry : instances.entrySet()) {
        if (MoreTypes.isInstance(entry.getKey().getType(), targetType)) {
          return Option.some(entry.getValue());
        }
      }
    }
    if (bindings != null) {
      for (Entry<TypeLiteral<?>, Class<? extends Converter<?>>> entry : bindings.entrySet()) {
        if (MoreTypes.isInstance(entry.getKey().getType(), targetType)) {
          for (Converter<?> converter : instantiateConverter(entry.getValue(), targetType)) {
            return Option.some(converter);
          }
        }
      }
    }

    if (targetType instanceof Class) {
      Class targetClass = (Class) targetType;
      // 2. @ConvertedBy
      for (Converter<?> converter : createConverterUsingConvertedBy(targetClass)) {
        return Option.some(converter);
      }
      // 3. base converters
      if (BASE_CONVERTERS.containsKey(targetClass)) {
        return Option.some(BASE_CONVERTERS.get(targetClass));
      }
      // 4. has <init>(Ljava/lang/String;)V;
      for (Converter<?> converter : createConverterUsingStringConstructor(targetClass)) {
        return Option.some(converter);
      }
    } else if (targetType instanceof ParameterizedType) {
      // 5. Set, List, Collection
      ParameterizedType parameterizedTargetType = (ParameterizedType) targetType;
      if (COLLECTION_KINDS.containsKey(parameterizedTargetType.getRawType()) &&
          parameterizedTargetType.getActualTypeArguments().length == 1) {
        Option<? extends Converter<?>> maybeElementConverter = createConverter(
            parameterizedTargetType.getActualTypeArguments()[0]);
        if (maybeElementConverter.isDefined()) {
          return (Option) Option.some(new CollectionOfElementsConverter(
              parameterizedTargetType.getRawType(),
              maybeElementConverter.getOrThrow()));
        }
      } else {
        // TODO(pascal) provide more detailed errors such as "you need to
        // parameterize your list"
      }
    }
    if (sizeBefore == errors.size()) {
      errors.noConverterForType(targetType);
    }
    return Option.none();
  }

  @VisibleForTesting
  Field[] retrieveFieldsFromAssignment(
      int parametersCount, Map<String, FormalParameter> assignments) {
    Field[] fields = new Field[parametersCount];
    outer: for (Entry<String, FormalParameter> entry : assignments.entrySet()) {
      int parameterIndex = entry.getValue().getIndex();
      if (parameterIndex < 0 || fields.length <= parameterIndex) {
        throw new IllegalStateException(
            format("formal parameter out of bounds (index %s)", parameterIndex));
      }

      Field field = null;
      String fieldName = entry.getKey();
      Class<?> classSearched = klass;
      while (true) {
        try {
          field = classSearched.getDeclaredField(fieldName);
          field.setAccessible(true);
          fields[parameterIndex] = field;
          continue outer;
        } catch (SecurityException e) {
          errors.unableToGetField(fieldName, e);
          continue outer;
        } catch (NoSuchFieldException e) {
          if (classSearched.equals(Object.class)) {
            errors.noSuchField(fieldName);
            continue outer;
          } else {
            classSearched = classSearched.getSuperclass();
          }
        }
      }
    }
    return fields;
  }

  private Option<? extends Converter<?>> createConverterUsingConvertedBy(
      final Class<?> targetClass) {
    Annotation[] typeAnnotations = targetClass.getAnnotations();
    for (Annotation typeAnnotation : typeAnnotations) {
      if (typeAnnotation instanceof ConvertedBy) {
        Class<? extends Converter<?>> converterClass = ((ConvertedBy) typeAnnotation).value();
        for (Converter<?> converter : instantiateConverter(converterClass, targetClass)) {
          return Option.some(converter);
        }
      }
    }
    return Option.none();
  }

  private Option<? extends Converter<?>> createConverterUsingStringConstructor(
      final Class<?> targetClass) {
    Constructor<?> stringConstructor;
    try {
      stringConstructor = targetClass.getDeclaredConstructor(String.class);
    } catch (SecurityException e) {
      // do proper exception handling, add this to errors
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      // Not having a String constructor is an acceptable outcome.
      return Option.none();
    }
    return Option.some(new StringConstructorConverter<Object>(stringConstructor));
  }

  // TODO(pascal) We should Guice this up.
  private Option<? extends Converter<?>> instantiateConverter(
      Class<? extends Converter<?>> converterClass, Type targetType) {
    try {
      Type producedType =
          Unification.getActualTypeArgument(converterClass, Converter.class, 0);
      if (targetType.equals(producedType)) {
        return Option.some(converterClass.newInstance());
      } else {
        errors.incorrectBoundForConverter(targetType, converterClass, producedType);
      }
    } catch (InstantiationException e) {
      errors.unableToInstantiate(converterClass, e);
    } catch (IllegalAccessException e) {
      errors.unableToInstantiate(converterClass, e);
    }
    return Option.none();
  }

  @VisibleForTesting
  Option<Constructor<T>> getConstructor() {
    @SuppressWarnings("unchecked")
    Constructor<T>[] constructors =
        (Constructor<T>[]) klass.getDeclaredConstructors();
    if (constructors.length > 1) {
      Constructor<T> convertableConstructor = null;
      for (Constructor<T> constructor : constructors) {
        if (constructor.getAnnotation(Instantiate.class) != null) {
          if (convertableConstructor == null) {
            convertableConstructor = constructor;
          } else {
            errors.moreThanOnceConstructorWithInstantiate(klass);
            return Option.none();
          }
        }
      }
      if (convertableConstructor != null) {
        return Option.some(convertableConstructor);
      } else {
        // should accumulate errors here
        throw new IllegalArgumentException(klass.toString() +
            " has more than one constructors");
      }
    } else if (constructors.length == 0) {
      // should accumulate errors here
      throw new IllegalArgumentException("No constructor found in " + klass);
    } else {
      return Option.some(constructors[0]);
    }
  }

  Errors getErrors() {
    return errors;
  }

}

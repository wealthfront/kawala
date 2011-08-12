/**
 * Copyright 2010 Wealthfront Inc. Licensed under the Apache License,
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
import static com.kaching.platform.converters.InstantiatorErrors.cannotAnnotateOptionWithOptional;
import static com.kaching.platform.converters.InstantiatorErrors.cannotSpecifyDefaultValueAndConstant;
import static com.kaching.platform.converters.InstantiatorErrors.constantHasIncompatibleType;
import static com.kaching.platform.converters.InstantiatorErrors.constantIsNotStaticFinal;
import static com.kaching.platform.converters.InstantiatorErrors.enumHasAmbiguousNames;
import static com.kaching.platform.converters.InstantiatorErrors.illegalConstructor;
import static com.kaching.platform.converters.InstantiatorErrors.incorrectBoundForConverter;
import static com.kaching.platform.converters.InstantiatorErrors.incorrectDefaultValue;
import static com.kaching.platform.converters.InstantiatorErrors.moreThanOneConstructor;
import static com.kaching.platform.converters.InstantiatorErrors.moreThanOneConstructorWithInstantiate;
import static com.kaching.platform.converters.InstantiatorErrors.moreThanOneMatchingFunction;
import static com.kaching.platform.converters.InstantiatorErrors.noConverterForType;
import static com.kaching.platform.converters.InstantiatorErrors.noSuchField;
import static com.kaching.platform.converters.InstantiatorErrors.optionalLiteralParameterMustHaveDefault;
import static com.kaching.platform.converters.InstantiatorErrors.unableToGetField;
import static com.kaching.platform.converters.InstantiatorErrors.unableToInstantiate;
import static com.kaching.platform.converters.InstantiatorErrors.unableToResolveConstant;
import static com.kaching.platform.converters.InstantiatorErrors.unableToResolveFullyQualifiedConstant;
import static com.kaching.platform.converters.NativeConverters.C_BOOLEAN;
import static com.kaching.platform.converters.NativeConverters.C_BYTE;
import static com.kaching.platform.converters.NativeConverters.C_CHAR;
import static com.kaching.platform.converters.NativeConverters.C_DOUBLE;
import static com.kaching.platform.converters.NativeConverters.C_FLOAT;
import static com.kaching.platform.converters.NativeConverters.C_INT;
import static com.kaching.platform.converters.NativeConverters.C_LONG;
import static com.kaching.platform.converters.NativeConverters.C_SHORT;
import static com.kaching.platform.converters.NativeConverters.C_STRING;
import static com.kaching.platform.converters.Optional.VALUE_DEFAULT;
import static java.lang.String.format;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.inject.TypeLiteral;
import com.kaching.platform.common.Errors;
import com.kaching.platform.common.Option;
import com.kaching.platform.common.types.Unification;
import com.kaching.platform.converters.ConstructorAnalysis.AnalysisResult;
import com.kaching.platform.converters.ConstructorAnalysis.FormalParameter;

class InstantiatorImplFactory<T> {

  private final static ImmutableMap<Type, Converter<?>> BASE_CONVERTERS =
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

  private final Errors errors;
  private final ConverterBinderImpl binder;
  private final Class<T> klass;

  private InstantiatorImplFactory(Errors errors, Class<T> klass) {
    this.errors = errors;
    this.klass = klass;
    this.binder = new ConverterBinderImpl(errors);
  }

  static <T> InstantiatorImplFactory<T> createFactory(Errors errors, Class<T> klass) {
    return new InstantiatorImplFactory<T>(errors, klass);
  }

  ConverterBinder binder() {
    return binder;
  }

  Option<InstantiatorImpl<T>> build() {
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
      BitSet wrapInOption = new BitSet();
      String[] defaultValues = null;
      Object[] defaultConstants = null;
      next_parameter: for (int i = 0; i < parametersCount; i++) {
        Annotation[] annotations = parameterAnnotations[i];
        Type genericParameterType = genericParameterTypes[i];
        Type genericParameterTypeForConverter;
        if (genericParameterType instanceof ParameterizedType &&
            ((ParameterizedType) genericParameterType).getRawType().equals(Option.class)) {
          wrapInOption.set(i);
          genericParameterTypeForConverter = ((ParameterizedType) genericParameterType).getActualTypeArguments()[0];
        } else {
          genericParameterTypeForConverter = genericParameterType;
        }
        for (final Converter<?> converter : createConverter(
            genericParameterTypeForConverter)) {
          converters[i] = converter;
          for (Optional optional : getOptionalAnnotation(annotations)) {
            if (wrapInOption.get(i)) {
              cannotAnnotateOptionWithOptional(errors, genericParameterType);
              continue next_parameter;
            }

            String defaultValue = optional.value();
            String defaultConstant = optional.constant();
            if (!defaultValue.equals(VALUE_DEFAULT) && !defaultConstant.isEmpty()) {
              cannotSpecifyDefaultValueAndConstant(errors, optional);
              continue next_parameter;
            } else if (!defaultValue.equals(VALUE_DEFAULT)) {
              try {
                converter.fromString(defaultValue);
                if (defaultValues == null) {
                  defaultValues = new String[parametersCount];
                }
                defaultValues[i] = defaultValue;
              } catch (RuntimeException e) {
                incorrectDefaultValue(errors, defaultValue, e);
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
                    unableToResolveFullyQualifiedConstant(errors, defaultConstant);
                    continue next_parameter;
                  }
                  constantName = parts[1];
                  break;

                default:
                  unableToResolveFullyQualifiedConstant(errors, defaultConstant);
                  continue next_parameter;
              }

              Field fieldOfConstant;
              try {
                fieldOfConstant = container.getDeclaredField(constantName);
              } catch (SecurityException e) {
                // TODO(pascal): better handling?
                throw new RuntimeException(e);
              } catch (NoSuchFieldException e) {
                unableToResolveConstant(errors, container, defaultConstant);
                continue next_parameter;
              }

              if ((fieldOfConstant.getModifiers() & Modifier.STATIC) == 0 ||
                  (fieldOfConstant.getModifiers() & Modifier.FINAL) == 0) {
                constantIsNotStaticFinal(errors, container, constantName);
                continue next_parameter;
              }

              // TODO(pascal): improve check. Should use MoreTypes.isAssignableFrom.
              if (!genericParameterType.equals(fieldOfConstant.getType())) {
                constantHasIncompatibleType(errors, container, constantName);
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
                optionalLiteralParameterMustHaveDefault(errors, i);
              }
            }
            optionality.set(i);
          }
        }
      }
      // 3. reverse mapping (fields to parameters)
      Field[] fields = null;
      AnalysisResult analysisResult = null;
      try {
        analysisResult = ConstructorAnalysis.analyse(klass, constructor);
        fields = retrieveFieldsFromAssignment(
            parametersCount, analysisResult.assignments);
      } catch (IOException e) {
        throw new IllegalStateException("should be able to access the class");
      } catch (ConstructorAnalysis.IllegalConstructorException e) {
        illegalConstructor(errors, klass, e.getMessage());
      }
      // 4. done
      if (!errors.hasErrors()) {
        return Option.some(new InstantiatorImpl<T>(
            constructor, converters, fields, optionality, wrapInOption, defaultValues,
            defaultConstants, analysisResult.paramaterNames));
      } else {
        return Option.none();
      }
    }
    return Option.none();
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
    List<Function<Type, Option<? extends Converter<?>>>> functions = binder.getFunctions();
    if (instances != null) {
      for (Entry<TypeLiteral<?>, Converter<?>> entry : instances.entrySet()) {
        if (isInstance(entry.getKey().getType(), targetType)) {
          return Option.some(entry.getValue());
        }
      }
    }
    if (bindings != null) {
      for (Entry<TypeLiteral<?>, Class<? extends Converter<?>>> entry : bindings.entrySet()) {
        if (isInstance(entry.getKey().getType(), targetType)) {
          for (Converter<?> converter : instantiateConverter(entry.getValue(), targetType)) {
            return Option.some(converter);
          }
        }
      }
    }

    // 2. function
    Converter<?> foundConverter = null;
    for (Function<Type, Option<? extends Converter<?>>> function : functions) {
      Option<? extends Converter<?>> option = function.apply(targetType);
      if (option.isDefined()) {
        if (foundConverter == null) {
          foundConverter = option.getOrThrow();
        } else {
          moreThanOneMatchingFunction(errors, targetType);
        }
      }
    }
    if (foundConverter != null) {
      return Option.some(foundConverter);
    }

    // 3. @ConvertedBy
    if (targetType instanceof Class ||
        targetType instanceof ParameterizedType) {
      Class targetClass = (Class) (targetType instanceof Class ?
          targetType :
          ((ParameterizedType) targetType).getRawType());
      for (Converter<?> converter : createConverterUsingConvertedBy(targetType, targetClass)) {
        return Option.some(converter);
      }
    }

    if (targetType instanceof Class) {
      Class targetClass = (Class) targetType;
      // 4. base converters
      if (BASE_CONVERTERS.containsKey(targetClass)) {
        return Option.some(BASE_CONVERTERS.get(targetClass));
      }
      // 5. has <init>(Ljava/lang/String;)V;
      for (Converter<?> converter : createConverterUsingStringConstructor(targetClass)) {
        return Option.some(converter);
      }
      // 6. is an Enum
      if (Enum.class.isAssignableFrom(targetClass)) {
        try {
          return Option.some((Converter<?>) new EnumConverter(targetClass));
        } catch (IllegalArgumentException e) {
          enumHasAmbiguousNames(errors, targetClass);
        }
      }
    } else if (targetType instanceof ParameterizedType) {
      // 7. has <init>(Ljava/lang/String;)V;
      for (Converter<?> converter : createConverterUsingStringConstructor(
            (Class)((ParameterizedType) targetType).getRawType())) {
        return Option.some(converter);
      }
      // 8. Set, List, Collection
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
      noConverterForType(errors, targetType);
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
          unableToGetField(errors, fieldName, e);
          continue outer;
        } catch (NoSuchFieldException e) {
          if (classSearched.equals(Object.class)) {
            noSuchField(errors, fieldName);
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
      final Type targetType, Class<?> targetClass) {
    Annotation[] typeAnnotations = targetClass.getAnnotations();
    for (Annotation typeAnnotation : typeAnnotations) {
      if (typeAnnotation instanceof ConvertedBy) {
        Class<? extends Converter<?>> converterClass = ((ConvertedBy) typeAnnotation).value();
        for (Converter<?> converter : instantiateConverter(converterClass, targetType)) {
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
      if (isInstance(producedType, targetType)) {
        Constructor<? extends Converter<?>> ctor = converterClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        return Option.some(ctor.newInstance());
      } else {
        incorrectBoundForConverter(errors, targetType, converterClass, producedType);
      }
    } catch (InstantiationException e) {
      unableToInstantiate(errors, converterClass, e);
    } catch (IllegalAccessException e) {
      unableToInstantiate(errors, converterClass, e);
    } catch (SecurityException e) {
      unableToInstantiate(errors, converterClass, e);
    } catch (NoSuchMethodException e) {
      unableToInstantiate(errors, converterClass, e);
    } catch (IllegalArgumentException e) {
      unableToInstantiate(errors, converterClass, e);
    } catch (InvocationTargetException e) {
      unableToInstantiate(errors, converterClass, e);
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
            moreThanOneConstructorWithInstantiate(errors, klass);
            return Option.none();
          }
        }
      }
      if (convertableConstructor != null) {
        return Option.some(convertableConstructor);
      } else {
        moreThanOneConstructor(errors, klass);
        return Option.none();
      }
    } else if (constructors.length == 0) {
      InstantiatorErrors.noConstructorFound(errors, klass);
      return Option.none();
    } else {
      return Option.some(constructors[0]);
    }
  }

  Errors getErrors() {
    return errors;
  }

  /**
   * Verifies that {@code b} is an instance of the type scheme {@code a}.
   * 
   * Formerly part of the Guice internal MoreTypes helper class; since the class
   * is internal and has since vanished the code is replaced from
   * <a href="https://github.com/pascallouisperez/guice-jit-providers/blob/fca95d48b9536f5429c991e1e81b07aaa51cceae/src/com/google/inject/internal/MoreTypes.java">the original source</a>
   */
  private static boolean isInstance(Type a, Type b) {
    if (a instanceof Class<?>) {
      return a.equals(b);
    } else if (a instanceof GenericArrayType) {
      if (b instanceof GenericArrayType) {
        return isInstance(
            ((GenericArrayType) a).getGenericComponentType(),
            ((GenericArrayType) b).getGenericComponentType());
      } else {
        return false;
      }
    } else if (a instanceof ParameterizedType) {
      if (!(b instanceof ParameterizedType)) {
        return false;
      }
      ParameterizedType parameterizedTypeA = (ParameterizedType) a;
      ParameterizedType parameterizedTypeB = (ParameterizedType) b;
      Type[] actualTypeArgumentsA = parameterizedTypeA.getActualTypeArguments();
      Type[] actualTypeArgumentsB = parameterizedTypeB.getActualTypeArguments();
      if (!parameterizedTypeA.getRawType().equals(parameterizedTypeB.getRawType()) ||
          actualTypeArgumentsA.length != actualTypeArgumentsB.length) {
        return false;
      }
      for (int i = 0; i < actualTypeArgumentsA.length; i++) {
        if (!isInstance(actualTypeArgumentsA[i], actualTypeArgumentsB[i])) {
          return false;
        }
      }
      return true;
    } else if (a instanceof TypeVariable<?>) {
      TypeVariable<?> typeVariable = (TypeVariable<?>) a;
      for (Type bound : typeVariable.getBounds()) {
        return isAssignableFrom(bound, b);
      }
      return true;
    } else if (a instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) a;
      for (Type lowerBound : wildcardType.getLowerBounds()) {
        if (!isAssignableFrom(b, lowerBound)) {
          return false;
        }
      }
      for (Type upperBound : wildcardType.getUpperBounds()) {
        if (!isAssignableFrom(upperBound, b)) {
          return false;
        }
      }
      return true;
    }
    throw new IllegalStateException();
  }
  
  /**
   * Equivalent of {@link Class#isAssignableFrom(Class)} at the {@link Type}
   * level.
   * 
   * Formerly part of the Guice internal MoreTypes helper class; since the class
   * is internal and has since vanished the code is replaced from
   * <a href="https://github.com/pascallouisperez/guice-jit-providers/blob/fca95d48b9536f5429c991e1e81b07aaa51cceae/src/com/google/inject/internal/MoreTypes.java">the original source</a>
   */
  private static boolean isAssignableFrom(Type a, Type b) {
    if (a instanceof Class<?>) {
      Class<?> classA = (Class<?>) a;
      if (b instanceof Class<?>) {
        return classA.isAssignableFrom((Class<?>) b);
      } else if (b instanceof GenericArrayType) {
        return classA.isArray() && isAssignableFrom(
            classA.getComponentType(), ((GenericArrayType) b).getGenericComponentType());
      } else if (b instanceof ParameterizedType) {
        return classA.isAssignableFrom((Class<?>) ((ParameterizedType) b).getRawType());
      } else if (b instanceof TypeVariable<?>) {
        TypeVariable<?> typeVariableB = (TypeVariable<?>) b;
        for (Type upperBound : typeVariableB.getBounds()) {
          if (!isAssignableFrom(a, upperBound)) {
            return false;
          }
        }
        return true;
      } else if (b instanceof WildcardType) {
        WildcardType wildcardTypeB = (WildcardType) b;
        for (Type upperBound : wildcardTypeB.getUpperBounds()) {
          if (!isAssignableFrom(a, upperBound)) {
            return false;
          }
        }
        return true;
      }
    } else if (a instanceof GenericArrayType) {
      if (b instanceof GenericArrayType) {
        return isAssignableFrom(
            ((GenericArrayType) a).getGenericComponentType(),
            ((GenericArrayType) b).getGenericComponentType());
      }
    } else if (a instanceof ParameterizedType) {
      ParameterizedType parameterizedTypeA = (ParameterizedType) a;
      if (b instanceof Class<?>) {
        return isAssignableFrom(parameterizedTypeA.getRawType(), b);
      } else if (b instanceof ParameterizedType) {
        ParameterizedType parameterizedTypeB = (ParameterizedType) b;
        Type[] actualTypeArgumentsA = parameterizedTypeA.getActualTypeArguments();
        Type[] actualTypeArgumentsB = parameterizedTypeB.getActualTypeArguments();
        if (actualTypeArgumentsA.length != actualTypeArgumentsB.length) {
          return false;
        }
        for (int i = 0; i < actualTypeArgumentsA.length; i++) {
          if (!isInstance(actualTypeArgumentsA[i], actualTypeArgumentsB[i])) {
            return false;
          }
        }
        return isAssignableFrom(
            parameterizedTypeA.getRawType(),
            parameterizedTypeB.getRawType());
      }
    } else if (a instanceof TypeVariable<?>) {
      for (Type bound : ((TypeVariable<?>) a).getBounds()) {
        if (!isAssignableFrom(bound, b)) {
          return false;
        }
      }
      return true;
    } else if (a instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) a;
      Type[] lowerBounds = wildcardType.getLowerBounds();
      for (Type lowerBound : lowerBounds) {
        if (!isAssignableFrom(lowerBound, b)) {
          return false;
        }
      }
      return lowerBounds.length != 0;
    }
    return false;
  }
}

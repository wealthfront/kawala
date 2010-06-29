package com.kaching.platform.common.types;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.min;
import static java.lang.String.format;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Unification for Java made easy. See
 * {@link http://blog.kaching.com/index.php/2009/01/16/unifying-type-parameters-in-java/}.
 */
public class Unification {

  public static Type getActualTypeArgument(
      Class<?> subClass, Class<?> superClass, int typeParameterIndex) {
    if (superClass.getTypeParameters().length <= typeParameterIndex) {
      throw new IllegalArgumentException(format(
          "%s does not have a type parameter of index %s",
          superClass, typeParameterIndex));
    }

    /* Assume the hierarchy is
     *
     *   A extends B<Integer>
     *   B<T1> extends C<List<T1>>
     *   C<T2>
     *
     * and we are resolving for T2.
     */

    // 1. Compute the linear hierarchy.
    // e.g. the inverted list [C, B, A] containing ClassWithType
    List<ClassWithType> linearHierarchy = getLinearHierarchy(subClass, superClass, null);
    if (linearHierarchy == null) {
      throw new IllegalArgumentException(format(
          "%s does not have %s as super class", subClass, superClass));
    }

    // 2. We now group by pairs actual type arguments and type variables.
    // e.g. [(Integer, T1), (List<T1>, T2)]
    LinkedList<GenericTypeInstantiation> chain = groupInGenericTypeInstantiations(
        superClass.getTypeParameters()[typeParameterIndex],
        linearHierarchy);

    // 3. Unify, i.e. solve for the rightmost type variable.
    return unify(chain);
  }

  private static List<ClassWithType> getLinearHierarchy(
      Class<?> subClass, Class<?> superClass, ParameterizedType type) {
    List<ClassWithType> linearHierarchy;

    // interfaces (array will be empty if class implements none)
    Class<?>[] interfaces = subClass.getInterfaces();
    Type[] genericInterfaces = subClass.getGenericInterfaces();
    for (int i = 0; i < min(interfaces.length, genericInterfaces.length); i++) {
      if (interfaces[i].equals(superClass)) {
        return newArrayList(
            new ClassWithType(
                interfaces[i], castToParameterizedType(subClass, genericInterfaces[i])),
            new ClassWithType(subClass, type));
      } else if (genericInterfaces[i] instanceof ParameterizedType) {
        linearHierarchy = getLinearHierarchy(
            interfaces[i], superClass, (ParameterizedType) genericInterfaces[i]);
        if (linearHierarchy != null) {
          linearHierarchy.add(new ClassWithType(subClass, type));
          return linearHierarchy;
        }
      }
    }

    // superclass
    Class<?> superclass = subClass.getSuperclass();
    if (superclass == null) {
      return null;
    }
    Type genericSuperclass = subClass.getGenericSuperclass();
    if (superclass.equals(Object.class)) {
      return null;
    } else if (superclass.equals(superClass)) {
      return newArrayList(
            new ClassWithType(superclass, castToParameterizedType(subClass, genericSuperclass)),
            new ClassWithType(subClass, type));
    } else {
      linearHierarchy = getLinearHierarchy(superclass, superClass,
            genericSuperclass instanceof ParameterizedType ?
                (ParameterizedType) genericSuperclass : null);
      if (linearHierarchy !=null) {
        linearHierarchy.add(new ClassWithType(subClass, type));
      }
      return linearHierarchy;
    }
  }

  private static ParameterizedType castToParameterizedType(Class<?> subClass, Type type) {
    if (!(type instanceof ParameterizedType)) {
      throw new IllegalArgumentException(format(
          "%s does extend parametrically %s", subClass, type));
    }
    return (ParameterizedType) type;
  }

  private static LinkedList<GenericTypeInstantiation> groupInGenericTypeInstantiations(
      TypeVariable<?> targetTypeVariable, List<ClassWithType> linearHierarchy) {
    LinkedList<GenericTypeInstantiation> chain = new LinkedList<GenericTypeInstantiation>();
    int max = linearHierarchy.size() - 1;
    resolve: for (int i = 0; i < max; /* done in loop */) {
      ClassWithType classWithType = linearHierarchy.get(i);
      TypeVariable<?>[] typeParameters = classWithType.clazz.getTypeParameters();
      for (int index = 0; index < typeParameters.length; index++) {
        if (targetTypeVariable.equals(typeParameters[index])) {
          chain.addFirst(new GenericTypeInstantiation(
              classWithType.type.getActualTypeArguments()[index],
              typeParameters[index]));

          // next
          i++;
          TypeVariable<?>[] potentialTargetTypeParameters = linearHierarchy.get(i).clazz.getTypeParameters();
          if (potentialTargetTypeParameters.length <= index) {
            return chain;
          }
          targetTypeVariable = potentialTargetTypeParameters[index];
          continue resolve;
        }
      }
      throw new IllegalStateException();
    }
    throw new IllegalStateException();
  }

  private static Type unify(LinkedList<GenericTypeInstantiation> chain) {
    Type unifiedType = chain.getLast().typeVariable;
    Map<TypeVariable<?>, Type> replacements = newHashMap();
    for (int i = chain.size() - 1; 0 <= i; i--) {
      GenericTypeInstantiation gti = chain.get(i);
      replacements.put(gti.typeVariable, gti.actualType);
      unifiedType = applyReplacements(replacements, unifiedType);
    }
    return unifiedType;
  }

  private static Type applyReplacements(
      Map<TypeVariable<?>, Type> replacements, Type unifiedType) {
    if (unifiedType instanceof TypeVariable<?>) {
      return replacements.get(unifiedType);
    } else if (unifiedType instanceof Class<?>) {
      return unifiedType;
    } else if (unifiedType instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) unifiedType;
      return new ParameterizedTypeImpl(
          applyReplacements(replacements, parameterizedType.getRawType()),
          applyReplacements(replacements, parameterizedType.getActualTypeArguments()));
    }
    throw new IllegalStateException("other kinds of types not handled yet");
  }

  private static Type[] applyReplacements(
      Map<TypeVariable<?>, Type> replacements, Type[] types) {
    Type[] replacedTypes = new Type[types.length];
    for (int i = 0; i < types.length; i++) {
      replacedTypes[i] = applyReplacements(replacements, types[i]);
    }
    return replacedTypes;
  }

  private static class GenericTypeInstantiation {
    private final Type actualType;
    private final TypeVariable<?> typeVariable;
    private GenericTypeInstantiation(
        Type actualType, TypeVariable<?> typeVariable) {
      this.actualType = actualType;
      this.typeVariable = typeVariable;
    }
    @Override
    public String toString() {
      return format("(%s,%s)", actualType, typeVariable);
    }
  }

  private static class ClassWithType {
    private Class<?> clazz;
    private ParameterizedType type;
    private ClassWithType(Class<?> clazz, ParameterizedType type) {
      this.clazz = clazz;
      this.type = type;
    }
    @Override
    public String toString() {
      return type == null ? "-" : type.toString();
    }
  }

}

package com.kaching.platform.common.types;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.min;
import static java.lang.String.format;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * Unification for Java made easy. See
 * {@link http://blog.kaching.com/index.php/2009/01/16/unifying-type-parameters-in-java/}.
 */
public class Unification {

  @SuppressWarnings("unchecked")
  public static Type getActualTypeArgument(
      Class<?> subClass, Class<?> superClass, int typeParameterIndex) {
    if (superClass.getTypeParameters().length <= typeParameterIndex) {
      throw new IllegalArgumentException(format(
          "%s does not have a type parameter of index %s",
          superClass, typeParameterIndex));
    }
    Type typeArgument = superClass.getTypeParameters()[typeParameterIndex];
    List<ClassWithType> linearHierarchy = getLinearHierarchy(subClass, superClass, null);
    if (linearHierarchy == null) {
      throw new IllegalArgumentException(format(
          "%s does not have %s as super class", subClass, superClass));
    }
    unify: for (ClassWithType tuple : linearHierarchy) {
      TypeVariable<?>[] typeParameters = tuple.clazz.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        if (typeArgument.equals(typeParameters[i])) {
          typeArgument = tuple.type.getActualTypeArguments()[i];
          if (!(typeArgument instanceof TypeVariable)) {
            return typeArgument;
          }
          continue unify;
        }
      }
    }

    throw new IllegalArgumentException(
        format("%s does not implement %s", subClass, superClass));
  }

  private static List<ClassWithType> getLinearHierarchy(
      Class<?> subClass, Class<?> superClass, ParameterizedType type) {
    List<ClassWithType> linearHierarchy;

    // interfaces (array will be empty if class implements none)
    Class<?>[] interfaces = subClass.getInterfaces();
    Type[] genericInterfaces = subClass.getGenericInterfaces();
    for (int i = 0; i < min(interfaces.length, genericInterfaces.length); i++) {
      if (interfaces[i].equals(superClass)) {
        linearHierarchy = newArrayList(
            new ClassWithType(
                interfaces[i], (ParameterizedType) genericInterfaces[i]));
      } else {
        linearHierarchy = getLinearHierarchy(
            interfaces[i], superClass, (ParameterizedType) genericInterfaces[i]);
      }
      if (linearHierarchy != null) {
        linearHierarchy.add(new ClassWithType(subClass, type));
        return linearHierarchy;
      }
    }

    // superclass
    Class<?> superclass = subClass.getSuperclass();
    Type genericSuperclass = subClass.getGenericSuperclass();
    if (superclass.equals(Object.class)) {
      return null;
    } else {
      linearHierarchy = getLinearHierarchy(superclass, superClass,
          genericSuperclass instanceof ParameterizedType ?
              (ParameterizedType) genericSuperclass : null);
      linearHierarchy.add(new ClassWithType(subClass, type));
      return linearHierarchy;
    }
  }

  private static class ClassWithType {
    private Class<?> clazz;
    private ParameterizedType type;
    private ClassWithType(Class<?> clazz, ParameterizedType type) {
      this.clazz = clazz;
      this.type = type;
    }
  }

}

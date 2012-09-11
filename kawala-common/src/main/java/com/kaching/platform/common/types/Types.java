package com.kaching.platform.common.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public class Types {

  /**
   * Verifies that {@code b} is an instance of the type scheme {@code a}.
   */
  public static boolean isInstance(Type a, Type b) {
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
   */
  public static boolean isAssignableFrom(Type a, Type b) {
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

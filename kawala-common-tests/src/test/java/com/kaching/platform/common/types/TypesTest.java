package com.kaching.platform.common.types;

import static com.google.inject.util.Types.listOf;
import static com.google.inject.util.Types.newParameterizedType;
import static com.google.inject.util.Types.setOf;
import static com.google.inject.util.Types.subtypeOf;
import static com.google.inject.util.Types.supertypeOf;
import static com.kaching.platform.common.types.Types.isAssignableFrom;
import static com.kaching.platform.common.types.Types.isInstance;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.google.inject.TypeLiteral;

public class TypesTest {


  @Test
  public void testIsInstance() {
    // class
    assertTrue(isInstance(String.class, String.class));
    assertTrue(isInstance(boolean[].class, boolean[].class));
    assertFalse(isInstance(List.class, String.class));

    // generic array type
    assertTrue(isInstance(
        new TypeLiteral<List<?>[]>() {}.getType(),
        new TypeLiteral<List<Integer>[]>() {}.getType()));

    // parameterized type
    assertTrue(isInstance(
        listOf(subtypeOf(Object.class)), listOf(String.class)));

    // type variable
    assertTrue(isInstance(new TypeVariableImpl(), String.class));
    assertTrue(isInstance(new TypeVariableImpl(Serializable.class), String.class));
    assertFalse(isInstance(new TypeVariableImpl(Collection.class), String.class));

    // wildcard type
    assertTrue(isInstance(
        subtypeOf(Object.class), listOf(Boolean.class)));
    assertTrue(isInstance(
        subtypeOf(Collection.class), listOf(Boolean.class)));

    assertFalse(isInstance(
        subtypeOf(Collection.class), Serializable.class));
    assertFalse(isInstance(
        supertypeOf(Set.class), TreeSet.class));
  }

  @Test
  public void testIsAssignableFrom() {
    // class
    assertTrue(isAssignableFrom(String.class, String.class));
    assertTrue(isAssignableFrom(Serializable.class, String.class));
    assertTrue(isAssignableFrom(Set.class, HashSet.class));
    assertTrue(isAssignableFrom(Object.class, listOf(String.class)));
    assertTrue(isAssignableFrom(
        List[].class, new TypeLiteral<List<?>[]>() {}.getType()));
    assertTrue(isAssignableFrom(
        ArrayList[].class, new TypeLiteral<ArrayList<?>[]>() {}.getType()));

    assertFalse(isAssignableFrom(Set.class, List.class));
    assertFalse(isAssignableFrom(
        ArrayList[].class, new TypeLiteral<List<?>[]>() {}.getType()));

    // generic array type
    assertTrue(isAssignableFrom(
        new TypeLiteral<List<?>[]>() {}.getType(),
        new TypeLiteral<List<Integer>[]>() {}.getType()));
    assertTrue(isAssignableFrom(
        new TypeLiteral<List<Integer>[]>() {}.getType(),
        new TypeLiteral<ArrayList<Integer>[]>() {}.getType()));
    assertTrue(isAssignableFrom(
        new TypeLiteral<List<? extends Set<?>>[]>() {}.getType(),
        new TypeLiteral<ArrayList<TreeSet<Double>>[]>() {}.getType()));

    assertFalse(isAssignableFrom(
        new TypeLiteral<List<?>[]>() {}.getType(), boolean[].class));
    assertFalse(isAssignableFrom(
        new TypeLiteral<List<?>[]>() {}.getType(), Collection[].class));
    assertFalse(isAssignableFrom(
        new TypeLiteral<List<?>[]>() {}.getType(), List[].class));

    // parameterized type
    assertTrue(isAssignableFrom(listOf(String.class),
        List.class));
    assertTrue(isAssignableFrom(listOf(String.class), listOf(String.class)));
    assertTrue(isAssignableFrom(listOf(String.class), newParameterizedType(ArrayList.class, String.class)));
    assertTrue(isAssignableFrom(listOf(subtypeOf(Serializable.class)), newParameterizedType(
        ArrayList.class, String.class)));

    assertFalse(isAssignableFrom(listOf(String.class), setOf(String.class)));
    assertFalse(isAssignableFrom(listOf(Serializable.class),
        listOf(String.class)));
    assertFalse(isAssignableFrom(listOf(String.class), listOf(Integer.class)));

    // type variable
    assertTrue(isAssignableFrom(new TypeVariableImpl(), String.class));
    assertTrue(isAssignableFrom(new TypeVariableImpl(String.class),
        String.class));
    assertTrue(isAssignableFrom(new TypeVariableImpl(String.class,
        Serializable.class, Comparable.class), String.class));

    assertFalse(isAssignableFrom(new TypeVariableImpl(List.class,
        Set.class), TreeSet.class));

    // wildcard type
    assertTrue(isAssignableFrom(
        supertypeOf(Collection.class), subtypeOf(List.class)));
    assertTrue(isAssignableFrom(
        supertypeOf(Collection.class), listOf(Integer.class)));
    assertTrue(isAssignableFrom(
        supertypeOf(new TypeLiteral<Collection<? extends Number>>() {}.getType()),
        listOf(Integer.class)));

    assertFalse(isAssignableFrom(
        subtypeOf(Object.class), subtypeOf(Object.class)));
    assertFalse(isAssignableFrom(
        supertypeOf(new TypeLiteral<Collection<? extends Number>>() {}.getType()),
        listOf(String.class)));
  }

  static class TypeVariableImpl implements TypeVariable<GenericDeclaration> {
    private final Type[] bounds;

    TypeVariableImpl() {
      this(new Type[0]);
    }

    TypeVariableImpl(Type... bounds) {
      this.bounds = bounds;
    }

    @Override
    public Type[] getBounds() {
      return bounds;
    }

    @Override
    public GenericDeclaration getGenericDeclaration() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
      throw new UnsupportedOperationException();
    }
  }
}

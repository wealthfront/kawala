/**
 * Copyright 2009 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.converters;

import static com.google.common.collect.Lists.newArrayList;
import static com.kaching.platform.converters.InstantiatorImplFactory.createFactory;
import static com.kaching.platform.converters.NativeConverters.C_BOOLEAN;
import static com.kaching.platform.converters.NativeConverters.C_BYTE;
import static com.kaching.platform.converters.NativeConverters.C_CHAR;
import static com.kaching.platform.converters.NativeConverters.C_DOUBLE;
import static com.kaching.platform.converters.NativeConverters.C_FLOAT;
import static com.kaching.platform.converters.NativeConverters.C_INT;
import static com.kaching.platform.converters.NativeConverters.C_LONG;
import static com.kaching.platform.converters.NativeConverters.C_SHORT;
import static com.kaching.platform.converters.NativeConverters.C_STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class InstantiatorImplTest {

  @Test
  public void newInstanceForObject() throws Exception {
    assertNotNull(
        new InstantiatorImpl<Object>(Object.class.getConstructor(), null, null, new BitSet(), null, null, null).newInstance());
  }

  @Test
  public void newInstanceForString() throws Exception {
    assertEquals(
        "hello",
        new InstantiatorImpl<String>(
            String.class.getConstructor(String.class),
            new Converter[] { C_STRING },
            null,
            new BitSet(),
            null,
            null,
            null).newInstance("hello"));
  }

  @Test
  public void wrongNumberOfArguments1() throws Exception {
    InstantiatorImpl<String> instantiator =
        new InstantiatorImpl<String>(String.class.getConstructor(String.class), null, null, new BitSet(), null, null, null);
    try {
      instantiator.newInstance();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("wrong number of arguments", e.getMessage());
    }
  }

  @Test
  public void wrongNumberOfArguments2() throws Exception {
    InstantiatorImpl<String> instantiator =
        new InstantiatorImpl<String>(
            String.class.getConstructor(String.class),
            new Converter[] { C_STRING },
            null,
            new BitSet(),
            null,
            null,
            null);
    try {
      instantiator.newInstance();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("wrong number of arguments", e.getMessage());
    }
  }

  @Test
  public void wrongNumberOfArguments3() throws Exception {
    InstantiatorImpl<String> instantiator =
        new InstantiatorImpl<String>(
            String.class.getConstructor(String.class),
            new Converter[] { C_STRING },
            null,
            new BitSet(),
            null,
            null,
            null);
    try {
      instantiator.newInstance("first", "second");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("wrong number of arguments", e.getMessage());
    }
  }

  @Test
  public void nullNotAllowedIsNotOptional() throws Exception {
    InstantiatorImpl<String> instantiator =
      new InstantiatorImpl<String>(
          String.class.getConstructor(String.class),
          new Converter[] { C_STRING },
          null,
          new BitSet(),
          null,
          null,
          null);
    try {
      instantiator.newInstance((String) null);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("parameter 1 is not optional but null was provided", e.getMessage());
    }
  }

  @Test
  public void converterCannotProduceNull() throws Exception {
    InstantiatorImpl<String> instantiator =
      new InstantiatorImpl<String>(
          String.class.getConstructor(String.class),
          new Converter[] { new ConverterOnlyProducesNull() },
          null,
          new BitSet(),
          null,
          null,
          null);
    try {
      instantiator.newInstance("hello");
      fail();
    } catch (IllegalStateException e) {
      assertEquals(
          "converter class com.kaching.platform.converters.InstantiatorImplTest" +
          "$ConverterOnlyProducesNull produced a null value",
          e.getMessage());
    }
  }

  static class ConverterOnlyProducesNull implements Converter<String> {
    @Override public String toString(String value) { return null; }
    @Override public String fromString(String representation) { return null; }
  }

  @Test
  public void optionalArgument() throws Exception {
    BitSet optionality = new BitSet();
    optionality.set(0);
    WrappedString instance = new InstantiatorImpl<WrappedString>(
        WrappedString.class.getConstructor(String.class),
        new Converter[] { C_STRING },
        null,
        optionality,
        null,
        null,
        null)
        .newInstance((String) null);
    assertNotNull(instance);
    assertNull(instance.string);
  }

  static class WrappedString {
    private final String string;
    public WrappedString(String string) {
      this.string = string;
    }
  }

  @Test
  public void optionalArgumentWithDefault() throws Exception {
    BitSet optionality = new BitSet();
    optionality.set(0);
    WrappedLong instance = new InstantiatorImpl<WrappedLong>(
        WrappedLong.class.getConstructor(Long.TYPE),
        new Converter[] { C_LONG },
        null,
        optionality,
        new String[] { "403" },
        null,
        null)
        .newInstance((String) null);
    assertNotNull(instance);
    assertEquals(403L, instance.value);
  }

  static class WrappedLong {
    private final long value;
    public WrappedLong(long value) {
      this.value = value;
    }
  }

  @Test
  public void natives() throws Exception {
    Natives instance = new InstantiatorImpl<Natives>(
        Natives.class.getConstructor(
            Integer.TYPE, Double.TYPE, Short.TYPE, Character.TYPE,
            Long.TYPE, Boolean.TYPE, Float.TYPE, Byte.TYPE),
        new Converter[] {
          C_INT, C_DOUBLE, C_SHORT, C_CHAR,
          C_LONG, C_BOOLEAN, C_FLOAT, C_BYTE },
        null,
        new BitSet(),
        null,
        null,
        null)
        .newInstance("1", "2.6", "3", "c", "4", "true", "5.5", "6");
    assertNotNull(instance);
    assertEquals(1, instance.i);
    assertEquals(2.6d, instance.d);
    assertEquals(3, instance.s);
    assertEquals('c', instance.c);
    assertEquals(4L, instance.l);
    assertEquals(true, instance.b);
    assertEquals(5.5f, instance.f);
    assertEquals(6, instance.y);
  }

  @Test
  public void fromInstanceSimple() {
    assertEquals(
        newArrayList("56"),
        createFactory(Simple.class).build()
            .fromInstance(new Simple(56)));
  }

  static class Simple {
    int value;
    Simple(int value) {
      this.value = value;
    }
  }

  @Test
  public void newInstanceHasEnum() {
    assertEquals(
        IsEnum.FOO,
        createFactory(HasEnum.class).build()
            .newInstance("FOO")
            .value);
  }

  @Test
  public void fromInstanceHasEnum() {
    assertEquals(
        newArrayList("FOO"),
        createFactory(HasEnum.class).build()
            .fromInstance(new HasEnum(IsEnum.FOO)));
  }

  static class HasEnum {
    IsEnum value;
    HasEnum(IsEnum value) {
      this.value = value;
    }
  }

  static enum IsEnum {
    FOO, BAR
  }

  @Test
  public void fromInstanceNatives() {
    List<String> parameters = createFactory(Natives.class).build()
        .fromInstance(new Natives(2, 3.4, (short) 5, '6', 7l, true, 8.0f, (byte) 9));
    assertEquals(
        newArrayList(
            "2", "3.4", "5", "6", "7", "true", "8.0", "9"),
        parameters);
  }

  @Test
  public void getConstructor() throws Exception {
    Constructor<Object> constructor = Object.class.getConstructor();
    InstantiatorImpl<Object> instantiator = new InstantiatorImpl<Object>(
        constructor, null, null, new BitSet(), null, null, null);
    assertTrue(constructor == instantiator.getConstructor());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void fromInstanceByNameThrowsIfNoParamaterNames() throws Exception {
    InstantiatorImpl<String> instantiator = new InstantiatorImpl<String>(
        null, null, null, null, null, null, null);
    instantiator.newInstance((Map<String, String>) null);
  }

  @Test
  public void toString1() throws Exception {
    InstantiatorImpl<Object> instantiator = new InstantiatorImpl<Object>(
        Object.class.getConstructor(), null, null, new BitSet(), null, null, null);
    assertEquals("instantiator java.lang.Object()", instantiator.toString());
  }

  @Test
  public void toString2() throws Exception {
    InstantiatorImpl<String> instantiator = new InstantiatorImpl<String>(
        String.class.getConstructor(byte[].class), null, null, new BitSet(), null, null, null);
    assertEquals("instantiator java.lang.String(byte[])", instantiator.toString());
  }

}

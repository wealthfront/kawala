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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.BitSet;

import org.junit.Test;

public class InstantiatorImplTest {

  @Test
  public void newInstanceForObject() throws Exception {
    assertNotNull(
        new InstantiatorImpl<Object>(Object.class.getConstructor(), null, new BitSet()).newInstance());
  }

  @Test
  public void newInstanceForString() throws Exception {
    assertEquals(
        "hello",
        new InstantiatorImpl<String>(
            String.class.getConstructor(String.class),
            new Converter[] { IdentityConverter.INSTANCE },
            new BitSet()).newInstance("hello"));
  }

  @Test
  public void wrongNumberOfArguments1() throws Exception {
    InstantiatorImpl<String> instantiator =
        new InstantiatorImpl<String>(String.class.getConstructor(String.class), null, new BitSet());
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
            new Converter[] { IdentityConverter.INSTANCE },
            new BitSet());
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
            new Converter[] { IdentityConverter.INSTANCE },
            new BitSet());
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
          new Converter[] { IdentityConverter.INSTANCE },
          new BitSet());
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
          new BitSet());
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
        new Converter[] { IdentityConverter.INSTANCE },
        optionality)
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

}

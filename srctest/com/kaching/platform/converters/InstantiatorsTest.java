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

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class InstantiatorsTest {

  @Test
  public void constructMe1() {
    assertNotNull(Instantiators
        .createInstantiator(ConstructMe1.class)
        .newInstance());

  }

  @Test
  public void constructMe2() {
    ConstructMe2 instance = Instantiators
        .createInstantiator(ConstructMe2.class)
        .newInstance("Jack Bauer");
    assertNotNull(instance);
    assertEquals("Jack Bauer", instance.name);
  }

  @Test
  public void constructMe3() {
    ConstructMe3 instance = Instantiators
        .createInstantiator(ConstructMe3.class)
        .newInstance("Jack Bauer", "First:Last");
    assertNotNull(instance);
    assertEquals("Jack Bauer", instance.name.content);
    assertEquals("First", instance.pair.first);
    assertEquals("Last", instance.pair.last);
  }

  static class ConstructMe1 {
    ConstructMe1() {}
  }

  static class ConstructMe2 {
    private final String name;
    ConstructMe2(String name) {
      this.name = name;
    }
  }

  static class ConstructMe3 {
    private final WrappedString name;
    private final ConvertedPair pair;
    ConstructMe3(WrappedString name, ConvertedPair pair) {
      this.name = name;
      this.pair = pair;
    }
  }

  static class WrappedString {
    private final String content;
    WrappedString(String content) {
      this.content = content;
    }
    @Override
    public String toString() {
      return content;
    }
  }

  @ConvertedBy(ConvertedPairConverter.class)
  static class ConvertedPair {
    private final String first;
    private final String last;
    ConvertedPair(String first, String last) {
      this.first = first;
      this.last = last;
    }
  }

  static class ConvertedPairConverter implements Converter<ConvertedPair> {

    @Override
    public String toString(ConvertedPair value) {
      return format("%s:%s", value.first, value.last);
    }

    @Override
    public ConvertedPair fromString(String representation) {
      String[] parts = representation.split(":");
      return new ConvertedPair(parts[0], parts[1]);
    }

  }

}

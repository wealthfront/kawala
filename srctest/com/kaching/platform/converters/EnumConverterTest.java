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

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class EnumConverterTest {

  @Test
  public void converter() throws Exception {
    TestConverter testConverter = new TestConverter();

    assertEquals("GooD", testConverter.toString(TheEnum.GooD));
    assertEquals("BaD", testConverter.toString(TheEnum.BaD));

    assertEquals(TheEnum.GooD, testConverter.fromString("GooD"));
    assertEquals(TheEnum.BaD, testConverter.fromString("BaD"));
  }

  static class TestConverter extends EnumConverter<TheEnum> {

    public TestConverter() {
      super(TheEnum.values());
    }

  }

  enum TheEnum {
    GooD, BaD;
  };

}

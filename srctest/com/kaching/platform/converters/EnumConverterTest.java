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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class EnumConverterTest {

  private Converter<TheEnum> converter;

  @Before
  public void before() throws Exception {
    converter = new EnumConverter<TheEnum>(TheEnum.class);
  }

  @Test
  public void fromAndTo() throws Exception {
    assertEquals(TheEnum.FOO, converter.fromString("FOO"));
    assertEquals(TheEnum.BAR, converter.fromString("BAR"));
    assertEquals("FOO", converter.toString(TheEnum.FOO));
    assertEquals("BAR", converter.toString(TheEnum.BAR));
  }

  @Test
  public void fromLowerCase() throws Exception {
    assertEquals(TheEnum.FOO, converter.fromString("foo"));
    assertEquals(TheEnum.BAR, converter.fromString("bar"));
  }

  enum TheEnum {
    FOO, BAR;
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotConvertAmbiguousEnum() throws Exception {
    new EnumConverter<AmbiguousEnum>(AmbiguousEnum.class);
  }

  enum AmbiguousEnum {
    FOO, Foo;
  }

}

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
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;


public class FiniteConverterTest {

  @Test
  public void fromAndTo() throws Exception {
    Converter<Integer> converter = 
        new FiniteConverter<Integer>(ImmutableMap.of("a", 1, "b", 2));
    assertEquals(1, converter.fromString("a"));
    assertEquals(2, converter.fromString("b"));
    assertEquals("a", converter.toString(1));
    assertEquals("b", converter.toString(2));
    assertNull(converter.fromString("c"));
    assertNull(converter.toString(3));
  }
  
  @Test
  public void getMap() throws Exception {
    Map<String, EnumType> map = FiniteConverter.getMap(EnumType.values());
    assertEquals(2, map.size());
    assertEquals(EnumType.A, map.get("a"));
    assertEquals(EnumType.B, map.get("b"));
  }
  
  private enum EnumType {
    A, B
  }
  
}

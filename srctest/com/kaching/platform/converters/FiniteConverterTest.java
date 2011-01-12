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

import com.google.common.collect.ImmutableMap;


public class FiniteConverterTest {

  private Converter<Integer> converter;

  @Before
  public void before() throws Exception {
    converter = new FiniteConverter<Integer>(ImmutableMap.of("a", 1, "b", 2));
  }

  @Test
  public void fromAndTo() throws Exception {
    assertEquals((Integer) 1, converter.fromString("a"));
    assertEquals((Integer) 2, converter.fromString("b"));
    assertEquals("a", converter.toString(1));
    assertEquals("b", converter.toString(2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void fromUnknownString() throws Exception {
    converter.fromString("c");
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownToString() throws Exception {
    converter.toString(3);
  }

}

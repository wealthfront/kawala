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

import static com.google.common.collect.Lists.newArrayList;
import static com.kaching.platform.converters.NativeConverters.C_BOOLEAN;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class CollectionOfElementsConverterTest {

  @Test
  public void emptyList() {
    check(Collections.emptyList(), "");
  }

  @Test
  public void oneElementList() {
    check(newArrayList((Object) true), "true");
  }

  @Test
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void createsRightKindOfCollection() {
    assertEquals(
        HashSet.class,
        new CollectionOfElementsConverter(Set.class, C_BOOLEAN).fromString("").getClass());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void check(List<Object> list, String representation) {
    CollectionOfElementsConverter converter = new CollectionOfElementsConverter(List.class, C_BOOLEAN);
    assertEquals(representation, converter.toString(list));
    assertEquals(list, converter.fromString(representation));
  }

}

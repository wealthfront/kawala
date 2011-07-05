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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class NativeConvertersTest {

  @Test
  public void booleanTrue() throws Exception {
    assertTrue(NativeConverters.C_BOOLEAN.fromString("True"));
    assertTrue(NativeConverters.C_BOOLEAN.fromString("TRUE"));;
    assertTrue(NativeConverters.C_BOOLEAN.fromString(" true"));
  }

  @Test
  public void booleanFalse() throws Exception {
    assertFalse(NativeConverters.C_BOOLEAN.fromString("False"));
    assertFalse(NativeConverters.C_BOOLEAN.fromString("false"));;
    assertFalse(NativeConverters.C_BOOLEAN.fromString(" false"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void booleanInvalid() throws Exception {
    NativeConverters.C_BOOLEAN.fromString("invalid");
  }
}

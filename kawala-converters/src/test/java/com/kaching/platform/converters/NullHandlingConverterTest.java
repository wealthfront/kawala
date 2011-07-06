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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class NullHandlingConverterTest {

  @Test
  public void fromNullString() throws Exception {
    Converter<Boolean> converter = new NullHandlingConverter<Boolean>() {
      @Override
      protected Boolean fromNonNullableString(String representation) {
        throw new UnsupportedOperationException();
      }
      @Override
      protected String nonNullableToString(Boolean value) {
        throw new UnsupportedOperationException();
      }
    };
    assertNull(converter.fromString(null));
  }
  
  @Test
  public void fromNotNullString() throws Exception {
    Converter<Boolean> converter = new NullHandlingConverter<Boolean>() {
      @Override
      protected Boolean fromNonNullableString(String representation) {
        return true;
      }
      @Override
      protected String nonNullableToString(Boolean value) {
        throw new UnsupportedOperationException();
      }
    };
    assertTrue(converter.fromString(""));
  }

  @Test
  public void nullToString() throws Exception {
    Converter<Boolean> converter = new NullHandlingConverter<Boolean>() {
      @Override
      protected Boolean fromNonNullableString(String representation) {
        throw new UnsupportedOperationException();
      }
      @Override
      protected String nonNullableToString(Boolean value) {
        throw new UnsupportedOperationException();
      }
    };
    assertNull(converter.toString(null));
  }

  @Test
  public void notNullToString() throws Exception {
    Converter<Boolean> converter = new NullHandlingConverter<Boolean>() {
      @Override
      protected Boolean fromNonNullableString(String representation) {
        throw new UnsupportedOperationException();
      }
      @Override
      protected String nonNullableToString(Boolean value) {
        return "";
      }
    };
    assertEquals("", converter.toString(true));
  }
  
}

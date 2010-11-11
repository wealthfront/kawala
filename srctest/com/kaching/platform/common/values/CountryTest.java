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
package com.kaching.platform.common.values;

import static com.kaching.platform.common.values.Country.AX;
import static com.kaching.platform.common.values.Country.US;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CountryTest {

  @Test
  public void getNumber() throws Exception {
    assertEquals(233, US.getNumber()); 
  }

  @Test
  public void getCountryName() throws Exception {
    assertEquals("United States", US.getCountryName()); 
  }
  
  @Test
  public void getAlpha2() throws Exception {
    assertEquals("US", US.getAlpha2()); 
  }
  
  @Test
  public void utf8CountryName() throws Exception {
    assertEquals("\u00c5land Islands", AX.getCountryName());
  }

}

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumberedEnumTest {

  @Test
  public void valueOfHit() {
    assertEquals(TestMe.A, get(1));
    assertEquals(TestMe.B, get(2));
    assertEquals(TestMe.C, get(3));
  }

  @Test(expected = NullPointerException.class)
  public void valueOfNull() {
    NumberedEnum.valueOf((Class<TestMe>) null, 89);
  }

  @Test(expected = IllegalArgumentException.class)
  public void valueOfUnknownNumber() {
    NumberedEnum.valueOf(TestMe.class, 89);
  }

  private TestMe get(int number) {
    return NumberedEnum.valueOf(TestMe.class, number);
  }
  
  enum TestMe implements NumberedValue {
    
    A(1),
    B(2),
    C(3);

    private final int number;

    private TestMe(int number) {
      this.number = number;
    }
    
    @Override
    public int getNumber() {
      return number;
    }
    
  }
  
}

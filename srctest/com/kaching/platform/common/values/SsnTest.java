/**
 * Copyright 2009 Wealthfront Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.common.values;

import static com.google.common.collect.Lists.newArrayList;
import static com.kaching.platform.testing.EquivalenceTester.check;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SsnTest {

  @Test
  public void parts() throws Exception {
    Ssn ssn = new Ssn("123456789");
    assertEquals("123", ssn.getAreaNumber());
    assertEquals("45", ssn.getGroupNumber());
    assertEquals("6789", ssn.getSerialNumber());
  }

  @Test(expected = IllegalArgumentException.class)
  public void tooLong() throws Exception {
    new Ssn("1234567890");
  }

  @Test(expected = IllegalArgumentException.class)
  public void tooShort() throws Exception {
    new Ssn("12345678");
  }

  @Test(expected = IllegalArgumentException.class)
  public void notNumeric() throws Exception {
    new Ssn("1234a6789");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidAreaNumber1() throws Exception {
    new Ssn("734456789");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidAreaNumber2() throws Exception {
    new Ssn("735456789");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidAreaNumber3() throws Exception {
    new Ssn("749456789");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidAreaNumber4() throws Exception {
    new Ssn("773456789");
  }

  @Test(expected = IllegalArgumentException.class)
  public void allZeros1() throws Exception {
    new Ssn("000456789");
  }

  @Test(expected = IllegalArgumentException.class)
  public void allZeros2() throws Exception {
    new Ssn("772006789");
  }

  @Test(expected = IllegalArgumentException.class)
  public void allZeros3() throws Exception {
    new Ssn("772450000");
  }

  @Test(expected = IllegalArgumentException.class)
  public void theBeast() throws Exception {
    new Ssn("666123456");
  }

  @Test(expected = IllegalArgumentException.class)
  public void reservedForAdvertisment1() throws Exception {
    new Ssn("98765430");
  }

  @Test(expected = IllegalArgumentException.class)
  public void reservedForAdvertisment2() throws Exception {
    new Ssn("98765431");
  }

  @Test(expected = IllegalArgumentException.class)
  public void reservedForAdvertisment3() throws Exception {
    new Ssn("98765439");
  }

  @Test
  public void equivalence() {
    check(
        newArrayList(
            new Ssn("123456789"),
            new Ssn("123456789")),
        newArrayList(
            new Ssn("123456788"),
            new Ssn("123456788")));
  }

  @Test
  public void toStringTest() {
    assertEquals(
        "***-**-6789",
        new Ssn("123456789").toString());
  }

}

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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import com.kaching.platform.common.AbstractIdentifier;

/**
 * A U.S. Social Security number, as issued to an individual by the Social
 * Security Administration.
 */
public class Ssn extends AbstractIdentifier<String> {

  private static final long serialVersionUID = -7832881252708349697L;

  public Ssn(String ssn) {
    super(validate(ssn.replaceAll("-", "")));
  }

  /**
   * Performs simple validation. Note that many fraudulent SSNs cannot easily
   * be detected using only publicly available information. The following
   * numbers are invalid:
   * <ol>
   *  <li>Numbers with an area number between 734 and 749, or above 772</li>
   *  <li>Numbers with all zeros in any digit group</li>
   *  <li>Numbers of the form 666-xx-####</li>
   *  <li>Numbers from 987-65-4320 to 987-65-4329</li>
   * </ol>
   * @throws IllegalArgumentException if the SSN is invalid
   * @return the untouched SSN, so it can be passed to the super class
   */
  private static String validate(String ssn) {

    // Careful, checks must not leak the SSN in stack traces!

    checkArgument(ssn.length() == 9);
    try {
      parseInt(ssn);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException();
    }

    String firstPart = firstPartOf(ssn);
    String secondPart = secondPartOf(ssn);
    String thirdPart = thirdPartOf(ssn);

    for (int i = 734; i <= 749; i++) {
      checkArgument(!firstPart.equals(String.valueOf(i)));
    }
    checkArgument(parseInt(firstPart) <= 772);

    checkArgument(!firstPart.equals("000"));
    checkArgument(!secondPart.equals("00"));
    checkArgument(!thirdPart.equals("0000"));

    checkArgument(!firstPart.equals("666"));

    for (int i = 0; i <= 9; i++) {
      checkArgument(!ssn.equals("98765432" + i));
    }

    return ssn;
  }

  /**
   * Returns the first three digits, assigned by the geographical region.
   */
  public String getAreaNumber() {
    return firstPartOf(getId());
  }

  /**
   * Returns the middle two digits. The group numbers range from 01 to 99.
   */
  public String getGroupNumber() {
    return secondPartOf(getId());
  }

  /**
   * Returns the last four digits. They represent a straight numerical
   * sequence of digits from 0001 to 9999 within the group.
   */
  public String getSerialNumber() {
    return thirdPartOf(getId());
  }

  @Override
  public String toString() {
    return format("***-**-%s", getSerialNumber());
  }

  private static String firstPartOf(String ssn) {
    return ssn.substring(0, 3);
  }

  private static String secondPartOf(String ssn) {
    return ssn.substring(3, 5);
  }

  private static String thirdPartOf(String ssn) {
    return ssn.substring(5, 9);
  }

}


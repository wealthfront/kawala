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

/**
 * Common interface to annotate values, often enumerations, whose numbering
 * scheme is used in external representation.
 * The numbering may be used for storage in a database, storage
 * in JSON representation or even within protobufs (avoiding duplicate enum
 * definitions).
 * 
 * For enums, using a separate numbering scheme than {@link Enum#ordinal()}
 * makes it less likely that refactoring will break critical
 * system wide invariant.
 */
public interface NumberedValue {

  /**
   * Return the value's numeric value.
   */
  int getNumber();

}

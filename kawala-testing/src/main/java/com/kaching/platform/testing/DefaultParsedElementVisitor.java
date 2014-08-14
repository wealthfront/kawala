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
package com.kaching.platform.testing;

import com.kaching.platform.testing.ParsedElements.ParsedClass;
import com.kaching.platform.testing.ParsedElements.ParsedConstructor;
import com.kaching.platform.testing.ParsedElements.ParsedField;
import com.kaching.platform.testing.ParsedElements.ParsedMethod;

public class DefaultParsedElementVisitor<T> implements ParsedElementVisitor<T> {

  private final T defaultValue;

  public DefaultParsedElementVisitor(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public T caseClass(ParsedClass parsedClass) {
    return defaultValue;
  }

  @Override
  public T caseConstructor(ParsedConstructor parsedConstructor) {
    return defaultValue;
  }

  @Override
  public T caseField(ParsedField parsedField) {
    return defaultValue;
  }

  @Override
  public T caseMethod(ParsedMethod parsedMethod) {
    return defaultValue;
  }

}

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

import static com.kaching.platform.converters.Instantiators.createInstantiator;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class IssuesTest {

  @Test
  public void issue17() {
    assertNotNull(createInstantiator(Issue17.class).newInstance(""));
  }

  static class Issue17 {
    Issue17(Id<String> id) {}
  }

  @ConvertedBy(IdConverter.class)
  static class Id<E> {}

  static class IdConverter implements Converter<Id<?>> {
    public String toString(Id<?> value) { return null; }
    public Id<?> fromString(String representation) { return new Id<String>(); }
  }

}

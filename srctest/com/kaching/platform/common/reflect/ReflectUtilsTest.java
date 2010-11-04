/**
 * Copyright 2010 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.common.reflect;

import static com.kaching.platform.common.reflect.ReflectUtils.getField;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReflectUtilsTest {

  @Test
  public void getField1() {
    assertEquals("child", getField(new Child(), "field1"));
  }

  @Test
  public void getField2() {
    assertEquals("parent", getField(new Child(), "field2"));
  }

  static class Child extends Parent {
    String field1 = "child";
  }

  static class Parent {
    String field2 = "parent";
  }

}

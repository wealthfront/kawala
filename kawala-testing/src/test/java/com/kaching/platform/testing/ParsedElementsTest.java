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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.OutputStream;
import java.util.Map;

import org.junit.Test;

import com.kaching.platform.testing.ParsedElements.ParsedClass;
import com.kaching.platform.testing.ParsedElements.ParsedField;
import com.kaching.platform.testing.ParsedElements.ParsedMethod;

public class ParsedElementsTest {

  @Test
  public void parseClass() {
    ParsedClass parsed = new ParsedElements.ParsedClass("java/io/OutputStream");
    assertEquals("java.io.OutputStream", parsed.toString());
    assertEquals("java.io.OutputStream", parsed.getOwner());
    assertEquals("java.io", parsed.getPackageName());
    assertEquals(OutputStream.class, parsed.load());
  }

  @Test
  public void parseNestedClass() {
    ParsedClass parsed = new ParsedElements.ParsedClass("java/util/Map$Entry");
    assertEquals("java.util.Map$Entry", parsed.toString());
    assertEquals("java.util.Map", parsed.getOwner());
    assertEquals("java.util", parsed.getPackageName());
    assertEquals(Map.Entry.class, parsed.load());
  }

  @Test
  public void parseAnonymousClass() {
    ParsedClass parsed = new ParsedElements.ParsedClass("com/google/protobuf/RpcUtil$1");
    assertEquals("com.google.protobuf.RpcUtil$1", parsed.toString());
    assertEquals("com.google.protobuf.RpcUtil", parsed.getOwner());
    assertEquals("com.google.protobuf", parsed.getPackageName());
  }

  @Test
  public void parseField() {
    ParsedElements.ParsedClass owner = new ParsedElements.ParsedClass("java/lang/System");
    ParsedField parsed = new ParsedElements.ParsedField(owner, "out");
    assertEquals("java.lang.System#out", parsed.toString());
    assertEquals(owner, parsed.getOwner());
    assertEquals("out", parsed.getName());
    assertNotNull(parsed.load());
  }

  @Test
  public void parseMethod() {
    ParsedElements.ParsedClass owner = new ParsedElements.ParsedClass("java.util.Map");
    ParsedMethod parsed = new ParsedElements.ParsedMethod(owner, "size", "()I");
    assertEquals("java.util.Map#size()I", parsed.toString());
    assertEquals(owner, parsed.getOwner());
    assertEquals("size", parsed.getName());
    assertEquals("()I", parsed.getSignature());
    assertNotNull(parsed.load());
  }

  @Test
  public void parseMethodWithComplexReturnType() {
    ParsedElements.ParsedClass owner = new ParsedElements.ParsedClass("java.lang.String");
    ParsedMethod parsed = new ParsedElements.ParsedMethod(owner, "format",
        "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
    assertEquals(
        "java.lang.String#format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
        parsed.toString());
    assertEquals(owner, parsed.getOwner());
    assertEquals("format", parsed.getName());
    assertEquals(
        "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", parsed.getSignature());
    assertNotNull(parsed.load());
  }

}

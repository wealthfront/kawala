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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static com.kaching.platform.converters.ConstructorAnalysis.analyse;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL32;
import static java.util.Collections.emptyMap;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.kaching.platform.converters.ConstructorAnalysis.FormalParameter;
import com.kaching.platform.converters.ConstructorAnalysis.IllegalConstructorException;

public class ConstructorAnalysisTest {

  static class NoOp {
  }

  @Test
  public void noOp() throws Exception {
    assertAssignement(NoOp.class, emptyMap());
  }

  static class OneAssignment {
    final int foo;
    OneAssignment(int foo) {
      this.foo = foo;
    }
  }

  @Test
  public void oneAssignment() throws Exception {
    assertAssignement(
        OneAssignment.class,
        ImmutableMap.of(
            "foo", "p0"));
  }

  static class TwoAssignments {
    final String foo;
    final int bar;
    TwoAssignments(int bar, String foo) {
      this.bar = bar;
      this.foo = foo;
    }
  }

  @Test
  public void twoAssignments() throws Exception {
    assertAssignement(
        TwoAssignments.class,
        ImmutableMap.of(
            "bar", "p0",
            "foo", "p1"));
  }

  @Test
  public void assignmentsToNatives() throws Exception {
    assertAssignement(
        Natives.class,
        ImmutableMap.<String, String> builder()
            .put("i", "p0")
            .put("d", "p1")
            .put("s", "p2")
            .put("c", "p3")
            .put("l", "p4")
            .put("b", "p5")
            .put("f", "p6")
            .put("y", "p7")
            .build());
  }

  static class DupAssignment {
    int foo;
    DupAssignment(int bar, int foo) {
      this.foo = bar;
      this.foo = foo;
    }
  }

  @Test
  public void dupAssignment() throws Exception {
    assertAnalysisFails(
        DupAssignment.class, "duplicate assignment to field foo");
  }

  static class KeepingSelfReference {
    KeepingSelfReference reference;
    KeepingSelfReference() {
      this.reference = this;
    }
  }

  @Test
  public void keepingSelfReference() throws Exception {
    assertAssignement(
        KeepingSelfReference.class,
        Collections.emptyMap());
  }

  static class AssigningSomethingElseThanParamater {
    int data;
    AssigningSomethingElseThanParamater(int data) {
      this.data = 4;
    }
  }

  @Test
  public void assigningSomethingElseThanParamater() throws Exception {
    assertAssignement(
        AssigningSomethingElseThanParamater.class,
        Collections.emptyMap());
  }

  static class ObjectInstantiation1 {
    ObjectInstantiation1() {
      new Object();
    }
  }

  @Test
  public void objectInstantiation1() throws Exception {
    assertAssignement(
        ObjectInstantiation1.class,
        Collections.emptyMap());
  }

  static class ObjectInstantiation2 {
    Object foo;
    ObjectInstantiation2() {
      this.foo = new Object();
    }
  }

  @Test
  public void objectInstantiation2() throws Exception {
    assertAssignement(
        ObjectInstantiation2.class,
        Collections.emptyMap());
  }

  static class ObjectInstantiation3 {
    Object foo;
    ObjectInstantiation3() {
      this.foo = new String("hello world!");
    }
  }

  @Test
  public void objectInstantiation3() throws Exception {
    assertAssignement(
        ObjectInstantiation3.class,
        Collections.emptyMap());
  }

  static class ObjectInstantiation4 {
    Object foo;
    ObjectInstantiation4() {
      this.foo = new BigDecimal(4.5, DECIMAL32);
    }
  }

  @Test
  public void objectInstantiation4() throws Exception {
    assertAssignement(
        ObjectInstantiation4.class,
        Collections.emptyMap());
  }

  static class ObjectInstantiation5 {
    Object foo;
    ObjectInstantiation5(String value) {
      this.foo = new String(value);
    }
  }

  @Test
  public void objectInstantiation5() throws Exception {
    assertAnalysisFails(
        ObjectInstantiation5.class,
        "can not assign non-idempotent expression new java.lang.String.<init>(p0) to field");
  }

  static class CheckArgument1 {
    final BigDecimal value;
    CheckArgument1(BigDecimal value) {
      checkArgument(ZERO.compareTo(value) <= 0);
      this.value = value;
    }
  }

  @Test
  @Ignore
  public void checkArgument1() throws Exception {
    assertAssignement(
        CheckArgument1.class,
        ImmutableMap.of(
            "value", "p0"));
  }

  static class DoingMathOperation1 {
    int foo;
    DoingMathOperation1(int foo) {
      this.foo = foo + 9;
    }
  }

  @Test
  public void doingMathOperation1() throws Exception {
    assertAnalysisFails(
        DoingMathOperation1.class,
        "can not assign non-idempotent expression p0 + 9 to field");
  }

  static class DoingMathOperation2 {
    long foo;
    DoingMathOperation2(long foo) {
      this.foo = foo * 5;
    }
  }

  @Test
  public void doingMathOperation2() throws Exception {
    assertAnalysisFails(
        DoingMathOperation2.class,
        "can not assign non-idempotent expression p0 * 5 to field");
  }

  static class ConstantHolder {
    static int CONSTANT = 4;
  }

  static class DoingMathOperation3 {
    int foo;
    DoingMathOperation3(int foo) {
      this.foo = foo % ConstantHolder.CONSTANT;
    }
  }

  @Test
  public void doingMathOperation3() throws Exception {
    assertAnalysisFails(
        DoingMathOperation3.class,
        "can not assign non-idempotent expression p0 % com.kaching.platform.converters.ConstructorAnalysisTest$ConstantHolder#CONSTANT to field");
  }

  static class CalledSuperclass {
    CalledSuperclass() {}
    CalledSuperclass(int foo) {}
  }
  static class CallingSuperConstructorNoArgument extends CalledSuperclass {
  }
  static class CallingSuperConstructorWithArguments extends CalledSuperclass {
    CallingSuperConstructorWithArguments(int foo) {
      super(foo);
    }
  }

  @Test
  public void callingSuperConstructorNoArgument() throws Exception {
    assertAssignement(
        CallingSuperConstructorNoArgument.class,
        Collections.emptyMap());
  }

  @Test
  public void callingSuperConstructorWithArguments() throws Exception {
    assertAnalysisFails(
        CallingSuperConstructorWithArguments.class,
        "can not call super constructor with argument(s)");
  }

  static class DelegatingToAnotherConstructor1 {
    DelegatingToAnotherConstructor1(int foo) {
      this();
    }
    DelegatingToAnotherConstructor1() {
    }
  }

  @Test
  public void delegatingToAnotherConstructor1() throws Exception {
    assertAnalysisFails(
        DelegatingToAnotherConstructor1.class,
        "can not delegate to another constructor");
  }

  static class DelegatingToAnotherConstructor2 {
    DelegatingToAnotherConstructor2() {
      this(4);
    }
    DelegatingToAnotherConstructor2(int foo) {
    }
  }

  @Test
  public void delegatingToAnotherConstructor2() throws Exception {
    assertAnalysisFails(
        DelegatingToAnotherConstructor2.class,
        "can not delegate to another constructor");
  }

  static class InvokeInterface {
    int size;
    InvokeInterface(Set<Integer> set) {
      this.size = set.size();
    }
  }

  @Test
  public void invokeInterface() throws Exception {
    assertAnalysisFails(
        InvokeInterface.class,
        "can not assign non-idempotent expression p0.size() to field");
  }

  abstract static class CalledByInvokeVirtual {
    abstract int callme();
  }
  static class InvokeVirtual {
    int size;
    InvokeVirtual(CalledByInvokeVirtual value) {
      this.size = value.callme();
    }
  }

  @Test
  public void invokeVirtual() throws Exception {
    assertAnalysisFails(
        InvokeVirtual.class,
        "can not assign non-idempotent expression p0.callme() to field");
  }

  static class InvokeStatic1 {
    final Map<String, String> map1 = newHashMap();
    final Map<String, String> map2;
    InvokeStatic1() {
      this.map2 = newHashMap();
    }
  }

  @Test
  public void invokeStatic1() throws Exception {
    assertAssignement(
        InvokeStatic1.class,
        Collections.emptyMap());
  }

  static class InvokeStatic2 {
    InvokeStatic2(int size) {
      newArrayListWithCapacity(size);
    }
  }

  @Test
  public void invokeStatic2() throws Exception {
    assertAssignement(
        InvokeStatic2.class,
        Collections.emptyMap());
  }

  static class InvokeStatic3 {
    List<Object> list;
    InvokeStatic3(int size) {
      this.list = newArrayListWithCapacity(size);
    }
  }

  @Test
  public void invokeStatic3() throws Exception {
    assertAnalysisFails(
        InvokeStatic3.class,
    "can not assign non-idempotent expression com.google.common.collect.Lists.newArrayListWithCapacity(p0) to field");
  }

  static class WriteToMyField { int myfield; }
  static class PutInDifferentField {
    PutInDifferentField(WriteToMyField value) {
      value.myfield = value.myfield + 5;
    }
  }

  @Test
  public void putInDifferentField() throws Exception {
    assertAssignement(
        PutInDifferentField.class,
        Collections.emptyMap());
  }

  static class ContainerToTrickLibraryUsingAliasing { List<String> ref; }
  static class AliasingResolutionIsNotSupported {
    List<String> non_idempotent;
    AliasingResolutionIsNotSupported(
        ContainerToTrickLibraryUsingAliasing trick,
        List<String> names) {
      trick.ref = names;
      trick.ref.add(Integer.toString(trick.ref.size()));
      this.non_idempotent = names;
    }
  }

  @Test
  public void aliasingResolutionIsNotSupported() throws Exception {
    /* Due to the lack of support for aliasing, one can successfully trick the
     * analysis. This is not an important case to protect for.
     */
    assertAssignement(
        AliasingResolutionIsNotSupported.class,
        ImmutableMap.of(
            "non_idempotent", "p1"));
  }

  @Test
  public void regression1() throws IOException {
    InputStream classInputStream = this.getClass().getResourceAsStream("example_scala_class01.bin");
    assertNotNull(classInputStream);
    Map<String, FormalParameter> assignements =
      analyse(classInputStream,
          "com/kaching/trading/rules/formula/FormulaValue",
          "java/lang/Object",
          int.class).assignments;
    assertMapEqualAsString(ImmutableMap.of("number", "p0"), assignements);
  }

  @Test
  public void regression2() throws IOException {
    InputStream classInputStream = this.getClass().getResourceAsStream("example_scala_class02.bin");
    assertNotNull(classInputStream);
    Map<String, FormalParameter> assignements =
      analyse(classInputStream,
          "com/kaching/user/GetAllModels",
          "com/kaching/platform/queryengine/AbstractQuery",
          Boolean.class).assignments;
    assertMapEqualAsString(ImmutableMap.of("com$kaching$user$GetAllModels$$withHistory", "p0"), assignements);
  }

  static class Regression3 {
    @SuppressWarnings("unused")
    private Object unimportant = ImmutableMap.builder().build();
  }

  @Test
  public void regression3() throws IOException {
    assertAssignement(
        Regression3.class,
        Collections.emptyMap());
  }

  static class CallingMethodOnStaticObject {
    static Object ref = new Object();
    CallingMethodOnStaticObject() {
      CallingMethodOnStaticObject.ref.hashCode();
    }
  }

  @Test
  public void callingMethodOnStaticObject() throws Exception {
    assertAssignement(
        CallingMethodOnStaticObject.class,
        Collections.emptyMap());
  }

  static class BoxingBoolean {
    boolean unboxed;
    Boolean boxed;
    BoxingBoolean(Boolean unboxed, boolean boxed) {
      this.unboxed = unboxed;
      this.boxed = boxed;
    }
  }

  @Test
  public void boxingBoolean() throws Exception {
    assertAssignement(
        BoxingBoolean.class,
        ImmutableMap.of(
            "unboxed", "p0",
            "boxed", "p1"));
  }

  private void assertAnalysisFails(Class<?> klass, String message) throws IOException {
    try {
      ConstructorAnalysis.analyse(klass, klass.getDeclaredConstructors()[0]);
      fail("analysis should have failed");
    } catch (IllegalConstructorException e) {
      assertEquals(message, e.getMessage());
    }
  }

  private void assertAssignement(
      Class<?> klass, Map<?, ?> expected) throws IOException {
    Map<String, FormalParameter> assignements =
        ConstructorAnalysis.analyse(klass, klass.getDeclaredConstructors()[0])
        .assignments;
    Map<String, String> actual = newHashMap();
    for (Entry<String, FormalParameter> entry : assignements.entrySet()) {
      actual.put(entry.getKey(), entry.getValue().toString());
    }
    assertEquals(expected, actual);
  }
  private void assertMapEqualAsString(Map<String, String> expected, Map<String, ?> actual) {
    Map<String, String> actualStrings = newHashMap();
    for (Entry<String, ?> entry : actual.entrySet()) {
      actualStrings.put(entry.getKey(), entry.getValue().toString());
    }
    assertEquals(expected, actualStrings);
  }

}

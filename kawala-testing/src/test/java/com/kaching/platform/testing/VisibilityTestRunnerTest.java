package com.kaching.platform.testing;

import static com.kaching.platform.testing.VisibilityTestRunner.Intent.PRIVATE;
import static org.junit.Assert.*;

import org.junit.Test;

public class VisibilityTestRunnerTest {

  @Test
  public void privateFieldMustBeVisibleByOwner() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertTrue(runner.isVisible(
        new ParsedElements.ParsedField(
            new ParsedElements.ParsedClass("java/util/HashMap"), "size"),
        new ParsedElements.ParsedClass("java/util/HashMap"), PRIVATE));
  }
  
  @Test
  public void privateFieldMustBeVisibleByInnerClass() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertTrue(runner.isVisible(
        new ParsedElements.ParsedField(
            new ParsedElements.ParsedClass("java/util/HashMap"), "size"),
        new ParsedElements.ParsedClass("java/util/HashMap$Entry"), PRIVATE));
  }
  
  @Test
  public void privateFieldMustBeVisibleByOuterClass() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertTrue(runner.isVisible(
        new ParsedElements.ParsedField(
            new ParsedElements.ParsedClass("java/util/HashMap$Entry"), "key"),
        new ParsedElements.ParsedClass("java/util/HashMap"), PRIVATE));
  }
  
  @Test
  public void privateFieldMustBeVisibleByAnonymousClass() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertTrue(runner.isVisible(
        new ParsedElements.ParsedField(
            new ParsedElements.ParsedClass("java/util/HashMap"), "size"),
        new ParsedElements.ParsedClass("java/util/HashMap$1"), PRIVATE));
  }
  
  @Test
  public void privateFieldMustNotBeVisibleByDifferentClass() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertFalse(runner.isVisible(
        new ParsedElements.ParsedField(
            new ParsedElements.ParsedClass("java/util/HashMap"), "size"),
        new ParsedElements.ParsedClass("java/util/ArrayList"), PRIVATE));
  }
  
  @Test
  public void privateFieldMustNotBeVisibleBySubclass() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertFalse(runner.isVisible(
        new ParsedElements.ParsedField(
            new ParsedElements.ParsedClass("java/util/HashMap"), "size"),
        new ParsedElements.ParsedClass("org/apache/commons/collections/MultiHashMap"), PRIVATE));
  }
  
  @Test
  public void privateInnerClassMustBeVisibleByOwner() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertTrue(runner.isVisible(
        new ParsedElements.ParsedClass("java/util/HashMap$Entry"),
        new ParsedElements.ParsedClass("java/util/HashMap"), PRIVATE));
  }
  
  @Test
  public void privateInnerClassMustBeVisibleByOtherInnerClass() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertTrue(runner.isVisible(
        new ParsedElements.ParsedClass("java/util/HashMap$Entry"),
        new ParsedElements.ParsedClass("java/util/HashMap$KeySet"), PRIVATE));
  }
  
  @Test
  public void privateInnerClassMustNotBeVisibleByDifferentClass() {
    VisibilityTestRunner runner = new VisibilityTestRunner(getClass());
    assertFalse(runner.isVisible(
        new ParsedElements.ParsedClass("java/util/HashMap$Entry"),
        new ParsedElements.ParsedClass("java/util/ArrayList"), PRIVATE));
  }

}

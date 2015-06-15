package com.kaching.platform.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.kaching.platform.testing.CyclicDependencyTestRunner.Packages;
import com.kaching.platform.testing.CyclicDependencyTestRunner.PackagesBuilder.Result;

public class CyclicDependencyTestRunnerTest {

  @Packages(
      binDirectories = "target/test-classes",
      forPackages = "com.kaching.platform.testing.testexamples.a")
  private static class SinglePackage {}

  @Test
  public void testSinglePackage_NoCycles() throws IOException {
    CyclicDependencyTestRunner runner = new CyclicDependencyTestRunner(SinglePackage.class);
    Result result = runner.getTestResults(SinglePackage.class.getAnnotation(Packages.class));
    assertTrue(result.numClasses > 0);
    assertTrue(result.getUniqueCycles().isEmpty());
  }

  @Packages(
      binDirectories = "target/test-classes",
      forPackages = {"com.kaching.platform.testing.testexamples.a",
                     "com.kaching.platform.testing.testexamples.b"})
  private static class TwoPackages {}

  @Test
  public void testTwoPackages_NoCycles() throws IOException {
    CyclicDependencyTestRunner runner = new CyclicDependencyTestRunner(TwoPackages.class);
    Result result = runner.getTestResults(TwoPackages.class.getAnnotation(Packages.class));
    assertTrue(result.numClasses > 0);
    assertTrue(result.getUniqueCycles().isEmpty());
  }

  @Packages(
      binDirectories = "target/test-classes",
      forPackages = {"com.kaching.platform.testing.testexamples.a",
                     "com.kaching.platform.testing.testexamples.b",
                     "com.kaching.platform.testing.testexamples.c"})
  private static class ThreePackages {}

  @Test
  public void testThreePackages_Cycles() throws IOException {
    CyclicDependencyTestRunner runner = new CyclicDependencyTestRunner(ThreePackages.class);
    Result result = runner.getTestResults(ThreePackages.class.getAnnotation(Packages.class));
    assertTrue(result.numClasses > 0);
    assertEquals(1, result.getUniqueCycles().size());
  }

  @Packages(
      binDirectories = "target/test-classes",
      minClasses = 1,
      forPackages = "com.kaching.platform.testing.testexamples")
  private static class BasePackage {}

  @Test
  public void testBasePackage_Cycles() throws IOException {
    CyclicDependencyTestRunner runner = new CyclicDependencyTestRunner(BasePackage.class);
    Result result = runner.getTestResults(BasePackage.class.getAnnotation(Packages.class));
    assertTrue(result.numClasses > 0);
    assertEquals(1, result.getUniqueCycles().size());
    assertEquals("Strongly connected components: {\n" +
        "[com.kaching.platform.testing.testexamples.a,\n"
        + " com.kaching.platform.testing.testexamples.b,\n"
        + " com.kaching.platform.testing.testexamples.c]\n"
        + "}", result.toString());
  }

}

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

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kaching.platform.testing.CyclicDependencyTestRunner.PackagesBuilder.Result;

/**
 * @see <a href="http://clarkware.com/software/JDepend.html#junit">JDepend and JUnit</a>
 */
public class CyclicDependencyTestRunner extends AbstractDeclarativeTestRunner<CyclicDependencyTestRunner.Packages> {

  @Target(TYPE)
  @Retention(RUNTIME)
  public @interface Packages {

    public int minClasses() default 10;

    public String[] forPackages();

    public String[] binDirectories() default "target/test-classes";

    public String binDirectoryProperty() default "kawala.bin_directories";

  }

  public CyclicDependencyTestRunner(Class<?> testClass) {
    super(testClass, Packages.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void runTest(final Packages dependencies) throws IOException {
    Result results = getTestResults(dependencies);
    /* This assertion is meant as a sanity check. It makes sure that when
     * run, JDepend can correctly find the project's classes. Otherwise, the
     * test could pass simply because the path is incorrect or the build
     * assembles classes in a different directory.
     */
    assertTrue(
        format(
            "project does not contain more than %s classes, only %s found",
            dependencies.minClasses(), results.numClasses),
        dependencies.minClasses() < results.numClasses);

    assertFalse(results.hasCycle());
  }

  Result getTestResults(final Packages dependencies) throws IOException {
    JDepend jDepend = new JDepend();
    String binDirectoryProperty = getProperty(dependencies.binDirectoryProperty());
    String[] binDirectories = binDirectoryProperty != null ?
        binDirectoryProperty.split(":") :
        dependencies.binDirectories();
    for (String binDirectory : binDirectories) {
      if (new File(binDirectory).isDirectory()) {
        jDepend.addDirectory(binDirectory);
      }
    }
    jDepend.analyzeInnerClasses(true);
    PackageFilter checkedPackagesFilter = new PackageFilter() {
      @Override
      public boolean accept(String scannedPackageName) {
        for (String expectedPackageName : dependencies.forPackages()) {
          if (scannedPackageName.startsWith(expectedPackageName)) {
            return true;
          }
        }
        return false;
      }
    };
    jDepend.setFilter(checkedPackagesFilter);
    jDepend.analyze();

    Result result = new Result(jDepend.countClasses());
    if (jDepend.containsCycles()) {
      for (Object o : jDepend.getPackages()) {
        JavaPackage pkg = (JavaPackage) o;
        List<JavaPackage> cycles = Lists.newArrayList();
        pkg.collectAllCycles(cycles);
        result.addCycles(cycles);
      }
    }
    return result;
  }

  static class PackagesBuilder {

    private Set<String> forPackages;

    public PackagesBuilder forPackages(String... packageExpressions) {
      if (forPackages != null) {
        throw new IllegalStateException();
      }
      forPackages = newHashSet();
      for (String packageExpression : packageExpressions) {
        forPackages.add(packageExpression);
      }
      return this;
    }

    PackagesBuilder check(String packageExpression) {
      if (forPackages == null) {
        throw new IllegalStateException();
      }
      return this;
    }

    static class Result {
      final int numClasses;
      final Map<JavaPackage, Set<JavaPackage>> sccs = Maps.newHashMap();
      Result(int numClasses) {
        this.numClasses = numClasses;
      }

      boolean hasCycle() {
        return !sccs.isEmpty();
      }

      void addCycles(List<JavaPackage> cycles) {
        Set<JavaPackage> scc = Sets.newHashSet(cycles);
        for (JavaPackage pkg : cycles) {
          sccs.put(pkg, scc);
        }
      }

      Set<Set<JavaPackage>> getUniqueCycles() {
        return Sets.newHashSet(sccs.values());
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder("Strongly connected components: {\n");
        for (Set<JavaPackage> scc : getUniqueCycles()) {
          boolean first = true;
          sb.append("[");
          for (JavaPackage jp : scc) {
            if (first) {
              first = false;
            } else {
              sb.append(",\n ");
            }
            sb.append(jp.getName());
          }
          sb.append("]\n");
        }
        return sb.append("}").toString();
      }
    }
  }

}

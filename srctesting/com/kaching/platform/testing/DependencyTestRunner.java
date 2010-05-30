package com.kaching.platform.testing;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdepend.framework.JDepend;
import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import junit.framework.AssertionFailedError;

import com.google.common.collect.Sets;

public class DependencyTestRunner extends AbstractDeclarativeTestRunner<DependencyTestRunner.Dependencies> {

  @Target(TYPE)
  @Retention(RUNTIME)
  public @interface Dependencies {

    public int minClasses();

    public String[] forPackages();

    public CheckPackage[] ensure();

    public String[] binDirectories() default "bin";

    public String binDirectoryProperty() default "kawala.bin_directories";

  }

  @Retention(RUNTIME)
  @Target({})
  public @interface CheckPackage {

    public String name();

    public String[] mayDependOn();

  }

  public DependencyTestRunner(Class<?> testClass) {
    super(testClass, Dependencies.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void runTest(Dependencies dependencies) throws IOException {
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
    jDepend.analyze();

    /* This assertion is meant as a sanity check. It makes sure that when
     * run, JDepend can correctly find the project's classes. Otherwise, the
     * test could pass simply because the path is incorrect or the build
     * assembles classes in a different directory.
     */
    assertTrue(
        format(
            "project does not contain more than %s classes, only %s found",
            dependencies.minClasses(), jDepend.countClasses()),
        dependencies.minClasses() < jDepend.countClasses());

    DependenciesBuilder builder = new DependenciesBuilder()
        .forPackages(dependencies.forPackages());
    for (CheckPackage checkPackage : dependencies.ensure()) {
      builder.check(checkPackage.name())
          .mayDependOn(checkPackage.mayDependOn());
    }
    builder.assertIsVerified(jDepend.getPackages());
  }

  static class DependenciesBuilder {

    private Set<String> forPackages;

    private final Map<String, Set<String>> mayDepend = newHashMap();

    private Set<String> currentDependencies;

    public DependenciesBuilder forPackages(String... packageExpressions) {
      if (forPackages != null) {
        throw new IllegalStateException();
      }
      forPackages = newHashSet();
      for (String packageExpression : packageExpressions) {
        forPackages.add(packageExpression);
      }
      return this;
    }

    DependenciesBuilder check(String packageExpression) {
      if (forPackages == null) {
        throw new IllegalStateException();
      }
      currentDependencies = newHashSet();
      mayDepend.put(packageExpression, currentDependencies);
      return this;
    }

    DependenciesBuilder mayDependOn(String... packageExpressions) {
      if (currentDependencies == null || forPackages == null) {
        throw new IllegalStateException();
      }
      for (String packageExpression : packageExpressions) {
        currentDependencies.add(packageExpression);
      }
      currentDependencies = null;
      return this;
    }

    @SuppressWarnings("unchecked")
    void assertIsVerified(Collection<JavaPackage> packages) {
      if (forPackages == null) {
        throw new IllegalStateException();
      }

      Set<String> flattenedForPackages = flattenForPackages(packages);
      Map<String, Set<String>> flattened = flattenMayDepend(packages);

      List<Violation> violations = newArrayList();

      for (JavaPackage package1 : packages) {
        if (!flattenedForPackages.contains(package1.getName())) {
          continue;
        }
        for (JavaPackage package2 : (Collection<JavaPackage>) package1.getEfferents()) {
          if (package1 != package2) {
            assertIsVerified(
                package1, package2, flattened, violations);
          }
        }
      }

      if (!violations.isEmpty()) {
        StringBuilder errors = new StringBuilder();
        errors.append(format("%s violation(s):", violations.size()));
        for (Violation violation : violations) {
          errors.append("\n");
          errors.append(violation.toString());
        }
        throw new AssertionFailedError(errors.toString());
      }
    }

    private Set<String> flattenForPackages(Collection<JavaPackage> packages) {
      Set<String> flattened = newHashSet();

      for (JavaPackage javaPackage : packages) {
        for (String expression : forPackages) {
          if (packageNameMatchesExpression(javaPackage.getName(), expression)) {
            flattened.add(javaPackage.getName());
          }
        }
      }

      return flattened;
    }

    private Map<String, Set<String>> flattenMayDepend(Collection<JavaPackage> packages) {
      Map<String, Set<String>> flattened = newHashMap();

      for (JavaPackage javaPackage : packages) {
        for (Map.Entry<String, Set<String>> entry : mayDepend.entrySet()) {
          String name = javaPackage.getName();
          if (packageNameMatchesExpression(name, entry.getKey())) {
            if (!flattened.containsKey(name)) {
              flattened.put(name, Sets.<String>newHashSet());
            }
            flattened.get(name).addAll(entry.getValue());
          }
        }
      }

      return flattened;
    }

    private void assertIsVerified(JavaPackage package1, JavaPackage package2,
        Map<String, Set<String>> flattened, List<Violation> violations) {
      boolean matched = false;
      Set<String> expressions = flattened.get(package1.getName());
      if (expressions != null) {
        for (String expression : expressions) {
          if (packageNameMatchesExpression(package2.getName(), expression)) {
            matched = true;
          }
        }
      }
      if (!matched) {
        violations.add(new Violation(package1, package2));
      }
    }

    /* visible for testing */
    static boolean packageNameMatchesExpression(String name, String expression) {
      return "*".equals(expression) ||
          name.equals(expression) ||
          (expression.endsWith(".*") &&
           name.startsWith(expression.substring(0, expression.length() - 2)));
    }
  }

  static class Violation {

    final JavaPackage javaPackage;
    final JavaPackage efferent;

    Violation(JavaPackage javaPackage, JavaPackage efferent) {
      this.javaPackage = javaPackage;
      this.efferent = efferent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
      String baseMessage = format("package %s cannot depend on package %s",
          javaPackage.getName(), efferent.getName());
      Collection<JavaClass> classes = javaPackage.getClasses();
      if (!classes.isEmpty()) {
        int javaPackageNameLength = javaPackage.getName().length() + 1;
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.append(" (classes ");
        boolean first = true;
        for (JavaClass javaClass : classes) {
          if (javaClass.getImportedPackages().contains(efferent)) {
            if (first) {
              first = false;
            } else {
              stringBuilder.append(", ");
            }
            stringBuilder.append(javaClass.getName().substring(javaPackageNameLength));
          }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
      } else {
        return baseMessage;
      }
    }

  }

}

package com.kaching.platform.testing;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.kaching.platform.util.collect.GenerativeMap;

public class BadCodeSnippetsRunner extends Runner {

  @Target(TYPE)
  @Retention(RUNTIME)
  public @interface CodeSnippets {

    public Check[] value();

  }

  @Retention(RUNTIME)
  public @interface Check {

    public String[] paths();

    public Snippet[] snippets();

  }

  @Retention(RUNTIME)
  public @interface Snippet {

    public String value();

    public String[] exceptions() default {};

  }

  private final Description description;
  private final Class<?> klass;
  private Description suiteDescription;

  /**
   * Internal use only.
   */
  public BadCodeSnippetsRunner(Class<?> klass) {
    this.klass = klass;
    suiteDescription = createSuiteDescription(klass);
    suiteDescription.addChild(description = createTestDescription(klass, "checking"));
  }

  @Override
  public Description getDescription() {
    return suiteDescription;
  }

  @Override
  public void run(RunNotifier notifier) {
    try {
      notifier.fireTestStarted(description);
      CodeSnippets codeSnippets = klass.getAnnotation(CodeSnippets.class);
      if (codeSnippets == null) {
        throw new AssertionError(format(
            "missing @%s annotation", CodeSnippets.class.getSimpleName()));
      }
      for (Check check : codeSnippets.value()) {
        checkBadCodeSnippet(check.paths(), check.snippets());
      }
    } catch (AssertionError e) {
      notifier.fireTestFailure(new Failure(description, e));
    } catch (IOException e) {
      notifier.fireTestFailure(new Failure(description, e));
    } finally {
      notifier.fireTestFinished(description);
    }
  }

  private void checkBadCodeSnippet(
      String[] paths, Snippet[] snippets) throws IOException {
    Map<Pattern, Set<File>> patternsToUses =
      new GenerativeMap<Pattern, Set<File>>() {
        @Override
        protected Set<File> compute(Pattern key) {
          return newHashSet();
        }};

    Map<Pattern, Set<File>> patternsToExceptions = patternsToExceptions(snippets);
    Set<Pattern> patterns = patternsToExceptions.keySet();
    for (String path : paths) {
      collectUses(new File(path), patterns, patternsToUses);
    }

    CombinedAssertionFailedError error =
        new CombinedAssertionFailedError("bad code uses");
    for (Pattern pattern : patterns) {
      Set<File> exceptions = patternsToExceptions.get(pattern);
      Set<File> uses = patternsToUses.get(pattern);
      List<File> spuriousExceptions = newArrayList(exceptions);
      spuriousExceptions.removeAll(uses);
      if (!spuriousExceptions.isEmpty()) {
        error.addError(format(
            "%s: marked as exception to snippet but didn't occur:\n    %s",
            pattern, Joiner.on("\n   ").join(spuriousExceptions)));
        continue;
      }
      uses.removeAll(exceptions);
      if (!uses.isEmpty()) {
        error.addError(format(
            "%s: found new uses in:\n    %s",
            pattern, Joiner.on("\n   ").join(uses)));
      }
    }

    if (error.hasErrors()) {
      throw error;
    }
  }

  private Map<Pattern, Set<File>> patternsToExceptions(Snippet[] snippets) {
    Map<Pattern, Set<File>> patternsToExceptions = newHashMap();
    for (Snippet snippet : snippets) {
      try {
        Set<File> files = newHashSet();
        for (int i = 0; i < snippet.exceptions().length; i++) {
          files.add(new File(snippet.exceptions()[i]));
        }
        patternsToExceptions.put(Pattern.compile(snippet.value()), files);
      } catch (PatternSyntaxException e) {
        throw new AssertionError(e.getMessage());
      }
    }
    return patternsToExceptions;
  }

  private void collectUses(
      File f, Iterable<Pattern> patterns, Map<Pattern, Set<File>> uses)
      throws IOException {
    if (f.isFile()) {
      if (f.getName().endsWith(".java")) {
        String code = Files.toString(f, UTF_8);
        for (Pattern p : patterns) {
          if (p.matcher(code).find()) {
            uses.get(p).add(f);
          }
        }
      }
    } else if (f.isDirectory()) {
      for (File c : f.listFiles()) {
        collectUses(c, patterns, uses);
      }
    }
  }

}

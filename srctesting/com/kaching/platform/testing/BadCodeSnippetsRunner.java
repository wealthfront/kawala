package com.kaching.platform.testing;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.kaching.platform.common.GenerativeMap;

public class BadCodeSnippetsRunner extends AbstractDeclarativeTestRunner<BadCodeSnippetsRunner.CodeSnippets> {

  /**
   * Top level annotation used to describe the bad code snippet test.
   */
  @Target(TYPE)
  @Retention(RUNTIME)
  public @interface CodeSnippets {

    /**
     * Lists all the checks that must be performed by this bad code snippet
     * test.
     */
    public Check[] value();
    
    /**
     * Specifies the file extension to check for bad code snippet. The default
     * is {@code java}.
     */
    public String fileExtension() default "java";

  }

  @Retention(RUNTIME)
  @Target({})
  public @interface Check {

    public String[] paths();

    public Snippet[] snippets();

  }

  @Retention(RUNTIME)
  @Target({})
  public @interface Snippet {

    public String value();

    public String[] exceptions() default {};

  }

  /**
   * Internal use only.
   */
  public BadCodeSnippetsRunner(Class<?> klass) {
    super(klass, CodeSnippets.class);
  }

  @Override
  protected void runTest(CodeSnippets codeSnippets) throws IOException {
    for (Check check : codeSnippets.value()) {
      checkBadCodeSnippet(check, codeSnippets.fileExtension());
    }
  }

  private void checkBadCodeSnippet(
      Check check, String fileExtension) throws IOException {
    Map<Pattern, Set<File>> patternsToUses =
      new GenerativeMap<Pattern, Set<File>>() {
        @Override
        protected Set<File> compute(Pattern key) {
          return newHashSet();
        }};

    Map<Pattern, Set<File>> patternsToExceptions = patternsToExceptions(check.snippets());
    Set<Pattern> patterns = patternsToExceptions.keySet();
    for (String path : check.paths()) {
      collectUses(fileExtension, new File(path), patterns, patternsToUses);
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
            "%s: found %s bad snippets in:\n    %s",
            pattern, uses.size(), Joiner.on("\n   ").join(uses)));
      }
    }

    error.throwIfHasErrors();
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
      String fileExtension, File f, Iterable<Pattern> patterns, Map<Pattern, Set<File>> uses)
      throws IOException {
    if (f.isFile()) {
      if (f.getName().endsWith("." + fileExtension)) {
        String code = Files.toString(f, UTF_8);
        for (Pattern p : patterns) {
          if (p.matcher(code).find()) {
            uses.get(p).add(f);
          }
        }
      }
    } else if (f.isDirectory()) {
      for (File c : f.listFiles()) {
        collectUses(fileExtension, c, patterns, uses);
      }
    }
  }

}

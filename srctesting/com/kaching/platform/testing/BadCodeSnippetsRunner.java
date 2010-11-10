/**
 * Copyright 2009 Wealthfront Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.testing;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.kaching.platform.testing.BadCodeSnippetsRunner.VerificationMode.BOTH;
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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.MapMaker;
import com.google.common.io.Files;

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

    public VerificationMode verificationMode() default BOTH;

    public String rationale() default "";

  }

  public enum VerificationMode {
    ONLY_MATCHES(false, true),
    ONLY_MISSING_MATCHES(true, false),
    BOTH(true, true);

    private final boolean reportMissing;
    private final boolean reportMatches;

    private VerificationMode(boolean missing, boolean matches) {
      this.reportMissing = missing;
      this.reportMatches = matches;
    }

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
    Map<Snippet, Set<File>> snippetsToUses = new MapMaker().makeComputingMap(
        new Function<Snippet, Set<File>>() {
          @Override
          public Set<File> apply(Snippet key) {
            return newHashSet();
          }});
    Map<Snippet, Pattern> compiledPatterns = new MapMaker().makeComputingMap(
        new Function<Snippet, Pattern>() {
          @Override
          public Pattern apply(Snippet key) {
            return Pattern.compile(key.value());
          }});

    Map<Snippet, Set<File>> snippetsToExceptions = snippetsToExceptions(check.snippets());
    Set<Snippet> snippets = snippetsToExceptions.keySet();
    for (String path : check.paths()) {
      collectUses(fileExtension, new File(path), snippets, snippetsToUses, compiledPatterns);
    }

    CombinedAssertionFailedError error =
        new CombinedAssertionFailedError("bad code uses");
    for (Snippet snippet : snippets) {
      Set<File> exceptions = snippetsToExceptions.get(snippet);
      Set<File> uses = snippetsToUses.get(snippet);
      List<File> spuriousExceptions = newArrayList(exceptions);
      spuriousExceptions.removeAll(uses);

      if (snippet.verificationMode().reportMissing && !spuriousExceptions.isEmpty()) {
        error.addError(format(
            "%s: marked as exception to snippet but didn't occur:\n    %s",
            snippet.value(), Joiner.on("\n   ").join(spuriousExceptions)));
        continue;
      }

      uses.removeAll(exceptions);
      if (snippet.verificationMode().reportMatches && !uses.isEmpty()) {
        String rationale = snippet.rationale().isEmpty() ? ""
            : format("\nrationale: %s", snippet.rationale());
        error.addError(format(
            "%s: found %s bad snippets in:\n    %s%s",
            snippet.value(), uses.size(), Joiner.on("\n   ").join(uses), rationale));
      }
    }

    error.throwIfHasErrors();
  }

  private Map<Snippet, Set<File>> snippetsToExceptions(Snippet[] snippets) {
    Map<Snippet, Set<File>> patternsToExceptions = newHashMap();
    for (Snippet snippet : snippets) {
      try {
        Set<File> files = newHashSet();
        for (int i = 0; i < snippet.exceptions().length; i++) {
          files.add(new File(snippet.exceptions()[i]));
        }
        patternsToExceptions.put(snippet, files);
      } catch (PatternSyntaxException e) {
        throw new AssertionError(e.getMessage());
      }
    }
    return patternsToExceptions;
  }

  private void collectUses(
      String fileExtension, File f, Iterable<Snippet> patterns,
      Map<Snippet, Set<File>> uses, Map<Snippet, Pattern> compiledPatterns)
      throws IOException {
    if (f.isFile()) {
      if (f.getName().endsWith("." + fileExtension)) {
        String code = Files.toString(f, UTF_8);
        for (Snippet p : patterns) {
          if (compiledPatterns.get(p).matcher(code).find()) {
            uses.get(p).add(f);
          }
        }
      }
    } else if (f.isDirectory()) {
      for (File c : f.listFiles()) {
        collectUses(fileExtension, c, patterns, uses, compiledPatterns);
      }
    }
  }

}

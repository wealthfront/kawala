/**
 * Copyright 2012 Wealthfront Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.testing;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.Type.getArgumentTypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class ForbiddenCallsTestRunner
    extends AbstractDeclarativeTestRunner<ForbiddenCallsTestRunner.ForbiddenCalls> {

  @Target(TYPE)
  @Retention(RUNTIME)
  public @interface ForbiddenCalls {

    public Check[] value();

  }

  @Retention(RUNTIME)
  @Target({})
  public @interface Check {

    public String[] paths();

    public String[] forbiddenMethods();

  }

  public ForbiddenCallsTestRunner(Class<?> testClass) {
    super(testClass, ForbiddenCalls.class);
  }

  @Override
  protected void runTest(ForbiddenCalls annotation) throws IOException {
    for (Check check : annotation.value()) {
      checkForbiddenCalls(
          getClassFiles(ImmutableList.copyOf(check.paths())),
                        ImmutableSet.copyOf(check.forbiddenMethods()));
    }
  }

  private static Iterable<File> getClassFiles(List<String> paths) {
    return FluentIterable.from(paths)
        .transform(new Function<String, ClassTree>() {
          @Override
          public ClassTree apply(String path) {
            return new ClassTree(new File(path));
          }
        })
        .transformAndConcat(new Function<ClassTree, List<File>>() {
          @Override
          public List<File> apply(ClassTree from) {
            return from.getClassFiles();
          }
        });
  }

  private void checkForbiddenCalls(
      Iterable<File> files,
      Set<String> forbiddenMethods) throws IOException {

    List<String> errors = newArrayList();
    for (File file : files) {
      visitFile(file, new FindForbiddenCalls(errors, forbiddenMethods));
    }
    if (!errors.isEmpty()) {
      errors = newArrayList(transform(errors, new Function<String, String>() {
        @Override
        public String apply(String from) {
          return "  " + from;
        }
      }));
      errors.add(0, "");
      fail(Joiner.on("\n").join(errors));
    }
  }

  private static void visitFile(File file, EmptyVisitor visitor) throws IOException {
    FileInputStream in = new FileInputStream(file);
    new ClassReader(in).accept(visitor, SKIP_FRAMES | SKIP_DEBUG);
    in.close();
  }

  private static class FindForbiddenCalls extends EmptyVisitor {

    private final List<String> errors;
    private final Set<String> forbiddenMethods;

    private String currentClassName;
    private String currentMethodName;
    private int currentLineNumber;

    FindForbiddenCalls(List<String> errors, Set<String> forbiddenMethods) {
      this.errors = errors;
      this.forbiddenMethods = forbiddenMethods;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
        String superName, String[] interfaces) {
      currentClassName = name.replace('/', '.');
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
        String signature, String[] exceptions) {
      currentMethodName = name;
      return this;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
      currentLineNumber = line;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      String method = encodeMethod(owner, name, desc);
      if (forbiddenMethods.contains(method)) {
        errors.add(
            format(
                "%s#%s:%s calls %s",
                currentClassName,
                currentMethodName,
                currentLineNumber,
                method));
      }
    }

    private static String encodeMethod(String owner, String name, String desc) {
      if (name.equals("<init>")) {
        name = getLast(asList(owner.split("/")));
      }
      return format(
          "%s#%s(%s)",
          owner.replace('/', '.'),
          name,
          Joiner.on(",").join(transform(
              newArrayList(getArgumentTypes(desc)),
              new Function<Type, String>() {
                @Override
                public String apply(Type from) {
                  return from.getClassName();
                }
              })));
    }

  }

}

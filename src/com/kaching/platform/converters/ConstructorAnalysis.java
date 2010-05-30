package com.kaching.platform.converters;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static org.apache.commons.logging.LogFactory.getLog;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import com.google.common.base.Joiner;

public class ConstructorAnalysis {

  private static final Log log = getLog(ConstructorAnalysis.class);

  /**
   * Produces an assignment or field names to values or fails.
   * @throws IllegalConstructorException
   */
  static Map<String, FormalParameter> analyse(
      Class<?> klass, Constructor<?> constructor) throws IOException {
    final String constructorDescriptor =
        Type.getConstructorDescriptor(constructor);
    final ConstructorExecutionState state =
        new ConstructorExecutionState(
            klass,
            constructor.getParameterTypes().length);
    final boolean[] hasVisitedConstructor = new boolean[] { false };
    analyse(klass, new EmptyVisitor() {
      @Override
      public MethodVisitor visitMethod(int access, String name, String desc,
          String signature, String[] exceptions) {
        if (name.equals("<init>") &&
            desc.equals(constructorDescriptor)) {
          if (!hasVisitedConstructor[0]) {
            hasVisitedConstructor[0] = true;
            return new ConstructorAnalyzer(state);
          } else {
            throw new IllegalStateException(
                "impossible to encounter twice a method with the same signature");
          }
        } else {
          return this;
        }
      }
    });
    return validateAndCast(state.assignements);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, FormalParameter> validateAndCast(
      final Map<String, JavaValue> assignements) {
    for (JavaValue value : assignements.values()) {
      if (!value.getClass().equals(FormalParameter.class)) {
        throw new IllegalConstructorException(
            "cannot assign values other than formal parameters to fields");
      }
    }
    return (Map) assignements;
  }

  /**
   * This constructor analyzer does abstract interpretation of the code
   * of a constructor to:
   * 1. Determine whether this is a legal kawala constructor
   *    (per the instantiator spec)
   * 2. Infer the parameter index of each field
   * @see http://homepages.inf.ed.ac.uk/kwxm/JVM/codeByNo.html for quick
   *    reference on opcodes
   */
  static class ConstructorAnalyzer implements MethodVisitor {

    private final ConstructorExecutionState state;

    private ConstructorAnalyzer(ConstructorExecutionState state) {
      this.state = state;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return new EmptyVisitor();
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
      return new EmptyVisitor();
    }

    @Override
    public void visitAttribute(Attribute attr) {
      throw illegalConstructor();
    }

    @Override
    public void visitCode() {
    }

    @Override
    public void visitEnd() {
      log.trace(format("end state:\n%s", state));
      checkState(state.stack.isEmpty(), "stack not empty on exit");
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name,
        String desc) {
      switch (opcode) {
        case 0xB5: // putfield
          log.trace(format("putfield %s %s %s", owner, name, desc));
          JavaValue value = state.stack.pop();
          state.stack.pop();
          state.assign(name, value);
          return;

        default: unknown(opcode);
      }
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
      throw illegalConstructor();
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      throw illegalConstructor();
    }

    @Override
    public void visitInsn(int opcode) {
      switch (opcode) {
        case 0xB1: // return
          log.trace("return");
          return;

        default: unknown(opcode);
      }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
      throw illegalConstructor();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      throw illegalConstructor();
    }

    @Override
    public void visitLabel(Label label) {
      throw illegalConstructor();
    }

    @Override
    public void visitLdcInsn(Object cst) {
      throw illegalConstructor();
    }

    @Override
    public void visitLineNumber(int line, Label start) {
      throw illegalConstructor();
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature,
        Label start, Label end, int index) {
      throw illegalConstructor();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      throw illegalConstructor();
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
    }

    @Override
    public void visitMethodInsn(
        int opcode, String owner, String name, String desc) {
      switch (opcode) {
        case 0xB7: // invokespecial
          log.trace(format("invokespecial %s %s %s", owner, name, desc));
          if (!owner.equals(state.owner) &&
              name.equals("<init>") &&
              desc.equals("()V")) {
            /* super()
             * consumes object reference
             */
            state.stack.pop();
            break;
          }
          if (owner.equals(state.owner) &&
              name.equals("<init>") &&
              desc.equals("()V")) {
            throw new IllegalConstructorException(
                "cannot call delegate constructor");
          }
          throw new IllegalStateException();

        default: unknown(opcode);
      }
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
      throw illegalConstructor();
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter,
        String desc, boolean visible) {
      // TODO we need to capture @Optional parameters
      return new EmptyVisitor();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt,
        Label[] labels) {
      throw illegalConstructor();
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler,
        String type) {
      throw illegalConstructor();
    }

    @Override
    public void visitTypeInsn(int opcode, String desc) {
      throw illegalConstructor();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      switch (opcode) {
        case 0x15: // iload
        case 0x19: // aload
          log.trace(format("_load %s", var));
          state.stack.push(state.locals.get(var));
          return;

        default: unknown(opcode);
      }
    }

    private void unknown(int opcode) {
      throw illegalConstructor();
    }

    private IllegalConstructorException illegalConstructor() {
      return new IllegalConstructorException("illegal constructor");
    }

  }

  /**
   * This constructor execution state captures the abstract interpretation of
   * a constructor's instructions.
   */
  static class ConstructorExecutionState {

    private final String owner;
    private final List<JavaValue> locals = newArrayList();
    private final Stack<JavaValue> stack = new Stack<JavaValue>();
    private final Map<String, JavaValue> assignements = newHashMap();

    ConstructorExecutionState(Class<?> klass, int argumentsNum) {
      owner = klass.getName().replace('.', '/');
      locals.add(new ThisPointer());
      for (int i = 0; i < argumentsNum; i++) {
        locals.add(new FormalParameter(i));
      }
    }

    void assign(String field, JavaValue value) {
      if (assignements.containsKey(field)) {
        throw new IllegalConstructorException("duplicate assignment to field %s", field);
      } else {
        assignements.put(field, value);
      }
    }

    @Override
    public String toString() {
      return Joiner.on("\n").join(
          format("locals: %s", locals), format("stack : %s", stack),
          format("assign: %s", assignements));
    }

  }

  static class IllegalConstructorException extends RuntimeException {

    private static final long serialVersionUID = -7666362130696013958L;

    IllegalConstructorException(String format, Object... args) {
      super(format(format, args));
    }

  }

  interface JavaValue {
  }

  static class ThisPointer implements JavaValue {
  }

  static class FormalParameter implements JavaValue {
    private final int index;
    FormalParameter(int index) {
      this.index = index;
    }
    @Override
    public String toString() {
      return format("p%s", index);
    }
    int getIndex() {
      return index;
    }
  }

  private static void analyse(Class<?> klass, ClassVisitor visitor) throws IOException {
    InputStream in = null;
    try {
      in = klass.getResourceAsStream("/" + klass.getName().replace('.', '/') + ".class");
      if (in == null) {
        throw new IllegalArgumentException(format("cannot find bytecode for %s", klass));
      }
      ClassReader reader = new ClassReader(in);
      reader.accept(visitor, SKIP_FRAMES | SKIP_DEBUG);
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

}

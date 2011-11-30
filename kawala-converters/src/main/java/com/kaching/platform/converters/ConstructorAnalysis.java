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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.ADD;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.AND;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.DIV;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.MUL;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.OR;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.REM;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.SHL;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.SHR;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.SUB;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.USHR;
import static com.kaching.platform.converters.ConstructorAnalysis.Operation.XOR;
import static java.lang.String.format;
import static org.apache.commons.logging.LogFactory.getLog;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

public class ConstructorAnalysis {

  private static final Log log = getLog(ConstructorAnalysis.class);

  /**
   * Produces an assignment or field names to values or fails.
   * @throws IllegalConstructorException
   */
  static AnalysisResult analyse(
      Class<?> klass, Constructor<?> constructor) throws IOException {
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    InputStream in = klass.getResourceAsStream("/" + klass.getName().replace('.', '/') + ".class");
    if (in == null) {
      throw new IllegalArgumentException(format("can not find bytecode for %s", klass));
    }
    return analyse(in, klass.getName().replace('.', '/'),
        klass.getSuperclass().getName().replace('.', '/'),
        parameterTypes);
  }

  @VisibleForTesting
  static AnalysisResult analyse(InputStream classInputStream,
      String owner, String superclass,
      Class<?>... parameterTypes) throws IOException {
    Type[] types = new Type[parameterTypes.length];
    for (int i = 0; i < types.length; i++) {
      types[i] = Type.getType(parameterTypes[i]);
    }
    final String constructorDescriptor =
      Type.getMethodDescriptor(Type.VOID_TYPE, types);
    final ConstructorExecutionState state =
        new ConstructorExecutionState(
            owner,
            superclass,
            parameterTypes);
    final boolean[] hasVisitedConstructor = new boolean[] { false };
    analyse(classInputStream, new EmptyVisitor() {
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
    return new AnalysisResult() {{
      this.assignments = validateAndCast(state.assignements);
      this.paramaterNames = state.parameterNames;
    }};
  }
  private static Map<String, FormalParameter> validateAndCast(
      final Map<String, JavaValue> assignements) {
    Map<String, FormalParameter> parameterAssignements = newHashMap();
    for (Entry<String, JavaValue> entry : assignements.entrySet()) {
      JavaValue value = entry.getValue();
      FormalParameter formalParameter = extractFormalParameterIfIsIdempotent(value);
      if (formalParameter == null) {
        if (value.containsFormalParameter()) {
          throw new IllegalConstructorException(format(
              "can not assign non-idempotent expression %s to field", value.toString().replaceAll("%", "%%")));
        }
      } else {
        parameterAssignements.put(entry.getKey(), formalParameter);
      }
    }
    return parameterAssignements;
  }

  private static final Map<Class<?>, String> NATIVES = ImmutableMap.<Class<?>, String> builder()
      .put(Byte.class, "byteValue")
      .put(Character.class, "charValue")
      .put(Boolean.class, "booleanValue")
      .put(Short.class, "shortValue")
      .put(Integer.class, "intValue")
      .put(Long.class, "longValue")
      .put(Float.class, "floatValue")
      .put(Double.class, "doubleValue")
      .build();

  private static FormalParameter extractFormalParameterIfIsIdempotent(JavaValue value) {
    // note to self: pattern matching would be handy here...
    if (value instanceof FormalParameter) {
      return (FormalParameter) value;
    } else if (value instanceof ObjectReference) {
      JavaValue reference = ((ObjectReference) value).getReference();
      if (reference instanceof FormalParameter) {
        return (FormalParameter) reference;
      }
      for (Entry<Class<?>, String> entry : NATIVES.entrySet()) {
        // java.lang.Boolean.valueOf(pN)
        // ObjectRef(StaticCall("java/lang/Boolean", "valueOf", FormalParameter(N)))
        if (reference instanceof StaticCall) {
          StaticCall call = (StaticCall) reference;
          if (call.owner.equals(entry.getKey().getName().replace('.', '/')) &&
              call.name.equals("valueOf") &&
              call.arguments.size() == 1 &&
              call.arguments.get(0) instanceof FormalParameter) {
            return (FormalParameter) call.arguments.get(0);
          }
        }
        // pN.booleanValue()
        // ObjectReference(MethodCall([], "booleanValue", FormalParameter(N)))
        if (reference instanceof MethodCall) {
          MethodCall call = (MethodCall) reference;
          if (call.arguments.isEmpty() &&
              call.name.equals(entry.getValue()) &&
              call.object instanceof FormalParameter &&
              ((FormalParameter) call.object).kind.equals(entry.getKey())) {
            return (FormalParameter) call.object;
          }
        }
      }
    }
    return null;
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
      ObjectReference reference;
      switch (opcode) {
        case 0xB2: // getstatic
          state.stackPush(desc, new StaticValue(owner, name, desc));
          return;

        case 0xB4: // getfield
          reference = (ObjectReference) state.stackPop();
          state.stackPush(desc, new GetField(reference.value, name));
          return;

        case 0xB5: // putfield
          log.trace(format("putfield %s %s %s", owner, name, desc));
          JavaValue value = state.stackPop();
          reference = (ObjectReference) state.stackPop();
          if (isThis(reference)) {
            state.assign(name, value);
          } else {
            /* We do not care about tracking other object's modification. We
            /* assume that API users are not trying to trick us with aliasing:
             *
             *   static class MyHolder { Object ref; }
             *
             *   MyConstructor(MyHolder trick, List<String> names) {
             *     trick.ref = names;
             *     trick.ref.add(Integer.toString(trick.size()));
             *     this.notidempotent = names;
             *   }
             *
             * If users are that crazy, let 'em!
             */
          }
          return;

        default: unknown(opcode);
      }
    }

    private boolean isThis(ObjectReference reference) {
      return (reference.value instanceof MethodCall &&
          ((MethodCall) reference.value).object instanceof ThisPointer) ||
          reference.value instanceof ThisPointer;
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

        case 0x01: // aconst_null
          state.stackPushLiteral(new ObjectReference(null));
          break;

        case 0x03: // iconst_0
        case 0x04: // iconst_1
        case 0x05: // iconst_2
        case 0x06: // iconst_3
        case 0x07: // iconst_4
        case 0x08: // iconst_5
          state.stackPushLiteral(new IntLiteral(opcode - 0x03));
          return;

        case 0x58: // pop2
          state.stackPop();
          /* fall-through to pop twice */
        case 0x57: // pop
          state.stackPop();
          return;

        case 0x59: // dup
          state.stackPushLiteral(state.stackPeek());
          return;

        case 0x60: // iadd
        case 0x61: // ladd
        case 0x62: // fadd
        case 0x63: // dadd
          operation(ADD);
          return;

        case 0x64: // isub
        case 0x65: // lsub
        case 0x66: // fsub
        case 0x67: // dsub
          operation(SUB);
          return;

        case 0x68: // imul
        case 0x69: // lmul
        case 0x6A: // fmul
        case 0x6B: // dmul
          operation(MUL);
          return;

        case 0x6C: // idiv
        case 0x6D: // ldiv
        case 0x6E: // fdiv
        case 0x6F: // ddiv
          operation(DIV);
          return;

        case 0x70: // irem
        case 0x71: // lrem
        case 0x72: // frem
        case 0x73: // drem
          operation(REM);
          return;

        case 0x78: // ishl
        case 0x79: // lshl
          operation(SHL);
          return;

        case 0x7A: // ishr
        case 0x7B: // lshr
          operation(SHR);
          return;

        case 0x7C: // iushr
        case 0x7D: // lushr
          operation(USHR);
          return;

        case 0x7E: // iand
        case 0x7F: // land
          operation(AND);
          return;

        case 0x80: // ior
        case 0x81: // lor
          operation(OR);
          return;

        case 0x82: // ixor
        case 0x83: // lxor
          operation(XOR);
          return;

        default: unknown(opcode);
      }
    }

    private void operation(Operation operation) {
      JavaValue value2 = state.stackPop();
      JavaValue value1 = state.stackPop();
      state.stackPushLiteral(new MathOperation(operation, value1, value2));
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
      switch (opcode) {
        case 0x10: state.stackPushLiteral(new IntLiteral(operand)); return; // bipush
        default: unknown(opcode);
      }
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      throw illegalConstructor();
    }

    @Override
    public void visitLabel(Label label) {
      // debug information
    }

    @Override
    public void visitLdcInsn(Object cst) {
      state.stackPushLiteral(new ConstantValue(cst));
    }

    @Override
    public void visitLineNumber(int line, Label start) {
      // debug information
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature,
        Label start, Label end, int index) {
      state.recordParameterName(index, name);
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
        case 0xB6: // invokevirtual
        case 0xB7: // invokespecial
        case 0xB9: // invokeinterface
        case 0xB8: // invokestatic
          log.trace(format("invoke___ %s %s %s", owner, name, desc));
          if (owner.equals(state.superclass) && name.equals("<init>")) { // super(...);
            if (!desc.equals("()V")) {
              throw new IllegalConstructorException(
                  "can not call super constructor with argument(s)");
            }
          } else if (owner.equals(state.owner) && name.equals("<init>")) { // this(...);
            throw new IllegalConstructorException(
                "can not delegate to another constructor");
          }
          // consuming arguments
          List<JavaValue> arguments = newArrayList();
          int index = 1;
          while (desc.charAt(index) != ')') {
            arguments.add(state.stackPop());
            // TODO(pascal): what about arrays which start with [
            index = desc.charAt(index) == 'L' ?
                desc.indexOf(';', index) + 1 :
                index + 1;
          }
          JavaValue returnValue;
          if (opcode == 0xB8) {
            returnValue = new StaticCall(owner, name, arguments);
          } else {
            ObjectReference reference = (ObjectReference) state.stackPop();
            reference.updateReference(
                new MethodCall(reference.value, name, arguments));
            returnValue = reference;
          }
          if (desc.charAt(index + 1) != 'V') {
            int rparen = desc.indexOf(')');
            state.stackPush(desc.substring(rparen + 1), returnValue);
          }
          return;

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
      switch (opcode) {
        case 0xBB: // new
          state.stackPushLiteral(new ObjectReference(new NewObject(desc)));
          return;

        case 0xBD: // anewarray
        case 0xC0: // checkcast
        case 0xC1: // instanceof
        default: unknown(opcode);
      }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      switch (opcode) {
        case 0x15: // iload
        case 0x16: // lload
        case 0x17: // fload
        case 0x18: // dload
        case 0x19: // aload
          log.trace(format("_load %s", var));
          state.stackPushLiteral(state.locals.get(var));
          return;

        default: unknown(opcode);
      }
    }

    private void unknown(int opcode) {
      throw illegalConstructor();
    }

    private IllegalConstructorException illegalConstructor() {
      return new IllegalConstructorException();
    }

  }

  static class AnalysisResult {
    Map<String, FormalParameter> assignments;
    String[] paramaterNames;
  }

  /**
   * This constructor execution state captures the abstract interpretation of
   * a constructor's instructions.
   */
  static class ConstructorExecutionState {

    private final String owner;
    private final String superclass;
    private final int parameterNums;
    private final List<JavaValue> locals = newArrayList();
    private final LinkedList<JavaValue> stack = new LinkedList<JavaValue>();
    private final Map<String, JavaValue> assignements = newHashMap();
    private final List<Integer> parameterNameRewrite;
    private String[] parameterNames;

    ConstructorExecutionState(String owner, String superclass, Class<?>[] parameterTypes) {
      this.owner = owner;
      this.superclass = superclass;
      locals.add(new ObjectReference(new ThisPointer()));
      this.parameterNums = parameterTypes.length;
      this.parameterNameRewrite = newArrayList();
      for (int i = 0; i < parameterNums; i++) {
        Class<?> parameterType = parameterTypes[i];
        JavaValue formalParameter = new FormalParameter(i, parameterType);
        if (!parameterType.isPrimitive()) {
          formalParameter = new ObjectReference(formalParameter);
        }
        locals.add(formalParameter);
        parameterNameRewrite.add(i);
        if (parameterType.equals(Long.TYPE) ||
            parameterType.equals(Double.TYPE)) {
          locals.add(formalParameter);
          parameterNameRewrite.add(i);
        }
      }
    }

    void recordParameterName(int index, String name) {
      if (index > 0) {
        if (parameterNames == null) {
          parameterNames = new String[parameterNums];
        }
        parameterNames[parameterNameRewrite.get(index - 1)] = name;
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

    boolean isStackEmpty() {
      return stack.isEmpty();
    }

    void stackPushLiteral(JavaValue value) {
      stack.push(value);
    }

    void stackPush(String desc, JavaValue value) {
      if (desc.charAt(0) == 'L' && !(value instanceof ObjectReference)) {
        stack.push(new ObjectReference(value));
      } else {
        stack.push(value);
      }
    }

    JavaValue stackPop() {
      return stack.poll();
    }

    JavaValue stackPeek() {
      return stack.peek();
    }

  }

  static class IllegalConstructorException extends RuntimeException {

    private static final long serialVersionUID = -7666362130696013958L;

    IllegalConstructorException() {
    }

    IllegalConstructorException(String format, Object... args) {
      super(format(format, args));
    }

  }

  /**
   * A Java value used for abstract interpretation.
   */
  interface JavaValue {

    /**
     * Returns {@code true} if this Java value is computed from a formal
     * parameter, {@code false} otherwise. This method answers the question
     * free(value) intersect formal parameters set != empty set.
     */
    boolean containsFormalParameter();

  }

  static abstract class AbstractConstantValue implements JavaValue {
    @Override
    public boolean containsFormalParameter() {
      return false;
    }
  }

  static class ThisPointer extends AbstractConstantValue {
    @Override
    public String toString() {
      return "this";
    }
  }

  static class FormalParameter implements JavaValue {
    private final int index;
    private final Class<?> kind;
    FormalParameter(int index, Class<?> kind) {
      this.index = index;
      this.kind = kind;
    }
    @Override
    public String toString() {
      return format("p%s", index);
    }
    int getIndex() {
      return index;
    }
    Class<?> getKind() {
      return kind;
    }
    @Override
    public boolean containsFormalParameter() {
      return true;
    }
  }

  static class ConstantValue extends AbstractConstantValue {
    private final Object cst;
    ConstantValue(Object cst) {
      this.cst = cst;
    }
    @Override
    public String toString() {
      return cst.toString();
    }
  }

  static class IntLiteral extends AbstractConstantValue {
    private final int value;
    IntLiteral(int value) {
      this.value = value;
    }
    @Override
    public String toString() {
      return Integer.toString(value);
    }
  }

  static class ObjectReference implements JavaValue {
    private JavaValue value;
    ObjectReference(JavaValue value) {
      this.value = value;
    }
    @Override
    public boolean containsFormalParameter() {
      return value != null && value.containsFormalParameter();
    }
    @Override
    public String toString() {
      return String.valueOf(value);
    }
    void updateReference(JavaValue value) {
      this.value = value;
    }
    JavaValue getReference() {
      return value;
    }
  }

  static class NewObject extends AbstractConstantValue {
    private final String desc;
    NewObject(String desc) {
      this.desc = desc;
    }
    @Override
    public String toString() {
      return format("new %s", desc.replaceAll("/", "."));
    }
  }

  static class MethodCall implements JavaValue {
    private final JavaValue object;
    private final List<JavaValue> arguments;
    private final String name;
    MethodCall(JavaValue object, String name, List<JavaValue> arguments) {
      this.object = object;
      this.name = name;
      this.arguments = arguments;
    }
    @Override
    public boolean containsFormalParameter() {
      if (object.containsFormalParameter()) {
        return true;
      }
      for (JavaValue parameter : arguments) {
        if (parameter.containsFormalParameter()) {
          return true;
        }
      }
      return false;
    }
    @Override
    public String toString() {
      return format("%s.%s(%s)", object, name.replaceAll("/", "."), Joiner.on(",").join(arguments));
    }
  }

  static class StaticCall implements JavaValue {
    private final String owner;
    private final String name;
    private final List<JavaValue> arguments;
    StaticCall(String owner, String name, List<JavaValue> arguments) {
      this.owner = owner;
      this.name = name;
      this.arguments = arguments;
    }
    @Override
    public boolean containsFormalParameter() {
      for (JavaValue parameter : arguments) {
        if (parameter.containsFormalParameter()) {
          return true;
        }
      }
      return false;
    }
    @Override
    public String toString() {
      return format("%s.%s(%s)", owner.replaceAll("/", "."), name.replaceAll("/", "."), Joiner.on(",").join(arguments));
    }
  }

  static class GetField implements JavaValue {
    private final JavaValue object;
    private final String name;
    GetField(JavaValue object, String name) {
      this.object = object;
      this.name = name;
    }
    @Override
    public boolean containsFormalParameter() {
      return object.containsFormalParameter();
    }
    @Override
    public String toString() {
      return format("%s.%s", object, name);
    }
  }

  static class StaticValue extends AbstractConstantValue {
    private final String owner;
    private final String name;
    StaticValue(String owner, String name, String desc) {
      this.owner = owner;
      this.name = name;
      // TODO(pascal): do we need desc?
    }
    @Override
    public String toString() {
      return format("%s#%s", owner.replaceAll("/", "."), name);
    }
  }

  static class MathOperation implements JavaValue {
    private final Operation operation;
    private final JavaValue value1;
    private final JavaValue value2;
    MathOperation(Operation operation, JavaValue value1, JavaValue value2) {
      this.operation = operation;
      this.value1 = value1;
      this.value2 = value2;
    }
    @Override
    public boolean containsFormalParameter() {
      return value1.containsFormalParameter() || value2.containsFormalParameter();
    }
    @Override
    public String toString() {
      return String.format("%s %s %s", value1, operation.representation, value2);
    }
  }

  enum Operation {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    REM("%"),
    SHL("<<"),
    SHR(">>"),
    USHR(">>>"),
    AND("&"),
    OR("|"),
    XOR("^");
    private final String representation;
    private Operation(String representation) {
      this.representation = representation;
    }
  }

  private static void analyse(InputStream classFileIn,
      ClassVisitor visitor) throws IOException {
    try {
      ClassReader reader = new ClassReader(classFileIn);
      reader.accept(visitor, SKIP_FRAMES);
    } finally {
      classFileIn.close();
    }
  }

}

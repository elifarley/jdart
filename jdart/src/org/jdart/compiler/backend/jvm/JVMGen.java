package org.jdart.compiler.backend.jvm;

import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.BOOLEAN;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.DOUBLE;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.DYNAMIC;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.FUNCTION;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.INT;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.NONE;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.VOID;

import static org.objectweb.asm.Opcodes.*;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.type.PrimitiveType;

import org.jdart.compiler.backend.jvm.TypeChecker.Env;
import org.jdart.runtime.RT;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;

import com.google.dart.compiler.ast.DartArrayAccess;
import com.google.dart.compiler.ast.DartArrayLiteral;
import com.google.dart.compiler.ast.DartAssertion;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBooleanLiteral;
import com.google.dart.compiler.ast.DartBreakStatement;
import com.google.dart.compiler.ast.DartCase;
import com.google.dart.compiler.ast.DartCatchBlock;
import com.google.dart.compiler.ast.DartClass;
import com.google.dart.compiler.ast.DartConditional;
import com.google.dart.compiler.ast.DartContinueStatement;
import com.google.dart.compiler.ast.DartDefault;
import com.google.dart.compiler.ast.DartDoWhileStatement;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartEmptyStatement;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartField;
import com.google.dart.compiler.ast.DartFieldDefinition;
import com.google.dart.compiler.ast.DartForInStatement;
import com.google.dart.compiler.ast.DartForStatement;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartFunctionExpression;
import com.google.dart.compiler.ast.DartFunctionObjectInvocation;
import com.google.dart.compiler.ast.DartFunctionTypeAlias;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIfStatement;
import com.google.dart.compiler.ast.DartImportDirective;
import com.google.dart.compiler.ast.DartInitializer;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartInvocation;
import com.google.dart.compiler.ast.DartLabel;
import com.google.dart.compiler.ast.DartLibraryDirective;
import com.google.dart.compiler.ast.DartMapLiteral;
import com.google.dart.compiler.ast.DartMapLiteralEntry;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartMethodInvocation;
import com.google.dart.compiler.ast.DartNamedExpression;
import com.google.dart.compiler.ast.DartNativeBlock;
import com.google.dart.compiler.ast.DartNativeDirective;
import com.google.dart.compiler.ast.DartNewExpression;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartNullLiteral;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartParameterizedNode;
import com.google.dart.compiler.ast.DartParenthesizedExpression;
import com.google.dart.compiler.ast.DartPlainVisitor;
import com.google.dart.compiler.ast.DartPropertyAccess;
import com.google.dart.compiler.ast.DartRedirectConstructorInvocation;
import com.google.dart.compiler.ast.DartResourceDirective;
import com.google.dart.compiler.ast.DartReturnStatement;
import com.google.dart.compiler.ast.DartSourceDirective;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartStringInterpolation;
import com.google.dart.compiler.ast.DartStringLiteral;
import com.google.dart.compiler.ast.DartSuperConstructorInvocation;
import com.google.dart.compiler.ast.DartSuperExpression;
import com.google.dart.compiler.ast.DartSwitchStatement;
import com.google.dart.compiler.ast.DartSyntheticErrorExpression;
import com.google.dart.compiler.ast.DartSyntheticErrorStatement;
import com.google.dart.compiler.ast.DartThisExpression;
import com.google.dart.compiler.ast.DartThrowStatement;
import com.google.dart.compiler.ast.DartTryStatement;
import com.google.dart.compiler.ast.DartTypeExpression;
import com.google.dart.compiler.ast.DartTypeNode;
import com.google.dart.compiler.ast.DartTypeParameter;
import com.google.dart.compiler.ast.DartUnaryExpression;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.ast.DartWhileStatement;
import com.google.dart.compiler.ast.Modifiers;
import com.google.dart.compiler.common.Symbol;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.ConstructorElement;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.EnclosingElement;
import com.google.dart.compiler.resolver.FieldElement;
import com.google.dart.compiler.resolver.LibraryElement;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.resolver.VariableElement;
import com.google.dart.compiler.type.FunctionType;
import com.google.dart.compiler.type.InterfaceType;

public class JVMGen implements Visitor<Void, JVMGen.Env> {
  private static final Handle BSM_FUNCALL = new Handle(H_INVOKESTATIC,
      Type.getInternalName(RT.class), "funCall", RT.BSM_PLUS_CLASS_DESC);
  private static final Handle BSM_METHCALL = new Handle(H_INVOKESTATIC,
      Type.getInternalName(RT.class), "methCall", RT.BSM_DESC);
  private static final Handle BSM_SUPERCALL = new Handle(H_INVOKESTATIC,
      Type.getInternalName(RT.class), "superCall", RT.BSM_DESC);
  private static final Handle BSM_NEWCALL = new Handle(H_INVOKESTATIC,
      Type.getInternalName(RT.class), "newCall", RT.BSM_DESC);
  
  private final DartVisitorBridge<Void, Env> bridge =
      new DartVisitorBridge<>(this);
  
  public DartPlainVisitor<Void> getBridge() {
    return bridge;
  }
      
  private final Map<DartNode, JVMType> typeMap;
  private final Map<String, ClassWriter> writerMap;
  
  public JVMGen(Map<DartNode, JVMType> typeMap, Map<String, ClassWriter> writerMap) {
    this.typeMap = typeMap;
    this.writerMap = writerMap;
  }
  
  static class Env {
    private final ClassVisitor classVisitor;
    private final MethodVisitor methodVisitor;
    private final RegisterScope scope;
    
    private Env(ClassVisitor classVisitor, MethodVisitor methodVisitor, RegisterScope scope) {
      this.classVisitor = classVisitor;
      this.methodVisitor = methodVisitor;
      this.scope = scope;
    }

    public Env(ClassVisitor cv) {
      this(cv, null, null);
    }
    
    public ClassVisitor getClassVisitor() {
      return classVisitor;
    }
    public MethodVisitor getMethodVisitor() {
      return methodVisitor;
    }
    public RegisterScope getScope() {
      return scope;
    }
    
    public Env methodEnv(MethodVisitor mv) {
      return new Env(classVisitor, mv, new RegisterScope(null));
    }

    public void load(Element element, Type type) {
      int register = scope.load(element);
      methodVisitor.visitVarInsn(type.getOpcode(ILOAD), register);
    }
    
    public void store(Element element, Type type) {
      int register = scope.store(element, type);
      methodVisitor.visitVarInsn(type.getOpcode(ISTORE), register);
    }
  }
  
  private void gen(DartNode node, Env env) {
    bridge.accept(node, env);
  }
  
  // --- helper methods
  
  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final Type FUNCTION_TYPE = Type.getObjectType("Function");
  
  private JVMType getJVMType(DartNode node) {
    Objects.requireNonNull(node);
    JVMType type = typeMap.get(node);
    if (type == null) {
      throw new NullPointerException("no type registered for node "+node);
    }
    return type;
  }
  
  private JVMType asJVMType(ClassElement element) {
    return new JVMInterfaceType(element.getName());
  }
  
  private Type asType(DartNode node) {
    return asType(getJVMType(node));
  }
  
  private Type asType(JVMType type) {
    if (type instanceof JVMPrimitiveType) {
      switch((JVMPrimitiveType)type) {
      case BOOLEAN:
        return Type.BOOLEAN_TYPE;
      case DYNAMIC:
        return OBJECT_TYPE;
      case DOUBLE:
        return Type.DOUBLE_TYPE;
      case FUNCTION:
        return FUNCTION_TYPE;
      case INT:
        return Type.INT_TYPE;
      case VOID:
        return Type.VOID_TYPE;
      default:
      }
      throw new AssertionError("invalid primitive type "+type);
    }
    if (type instanceof JVMInterfaceType) {
      String name = ((JVMInterfaceType)type).getName();
      if (name.equals("String")) {  // FIXME, should be an interface String
        name = "java/lang/String";
      }
      return Type.getObjectType(name);
    }
    if (type instanceof JVMFunctionType) {
      JVMFunctionType functionType = (JVMFunctionType)type;
      Type returnType = asType(functionType.getReturnType());
      Type[] parameterTypes = new Type[functionType.getParameterTypes().size()];
      int i = 0;
      for(JVMType parameterType: functionType.getParameterTypes()) {
        parameterTypes[i++] = asType(parameterType);
      }
      return Type.getMethodType(returnType, parameterTypes);
    }
    throw new AssertionError("unknown type "+type);
  }
  
  private static void fixStack(MethodVisitor mv, Type type) {
    switch(type.getSize()) {
    case 0:
      // void do nothing
      break;
    case 1:
      mv.visitInsn(POP);
      break;
    case 2:
      mv.visitInsn(POP2);
      break;
    }
  }
  
  private static void genDefaultValue(MethodVisitor mv, Type returnType) {
    switch(returnType.getSort()) {
    case Type.VOID:
      return;
    case Type.BOOLEAN:
    case Type.INT:
      mv.visitInsn(ICONST_0);
      return;
    case Type.DOUBLE:
      mv.visitInsn(DCONST_0);
      return;
    default:
      mv.visitInsn(ACONST_NULL);
      return;
    }
  }
  
  private void mayInsertCast(MethodVisitor mv, Type leftType, Type rightType) {
    // FIXME, do something here
  }
  
  private static void generateEntryPoint(ClassVisitor cv, LibraryElement element) {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC,
        "main",
        "([Ljava/lang/String;)V",
        null, null);
    mv.visitCode();
    mv.visitInvokeDynamicInsn("__main__", "()V",
        BSM_FUNCALL, Type.getObjectType(getLibraryClassName(element)));
    mv.visitInsn(RETURN);
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
  }
  
  private static String getLibraryClassName(LibraryElement element) {
    String className = element.getName();
    className = className.substring(0, className.lastIndexOf('.'));  // remove ".dart"
    return className;
  }
  
  private ClassVisitor newClassWriter(String className) {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
    writerMap.put(className, writer);
    return writer;
  }
  
  // --- unit
  
  @Override
  public Void visitUnit(DartUnit node, Env env) {
    String className = getLibraryClassName(node.getLibrary().getElement());
    ClassVisitor cv = newClassWriter(className);
    
    cv.visit(V1_7, ACC_PUBLIC|ACC_SUPER, className, null, "java/lang/Object", null);
    cv.visitSource(node.getSourceName(), null);
    
    env = new Env(cv);
    for(DartNode topLevelNode: node.getTopLevelNodes()) {
      gen(topLevelNode, env);
    }
    
    cv.visitEnd();
    return null;
  }
  
  
  // --- class
  
  @Override
  public Void visitClass(DartClass node, Env env) {
    Symbol supersymbol = node.getSuperSymbol();
    Type superType = (supersymbol != null)? asType(supersymbol.getNode()): OBJECT_TYPE;
    
    ClassVisitor cv = newClassWriter(node.getClassName());
    cv.visit(V1_7, ACC_PUBLIC|ACC_SUPER, node.getClassName(), null, superType.getInternalName(), null);
    cv.visitSource(((DartUnit)node.getParent()).getSourceName(), null);
    
    // create a default constructor to bypass the JVM constructor restriction
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, superType.getInternalName(), "<init>", "()V");
    mv.visitInsn(RETURN);
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
    
    env = new Env(cv);
    for(DartNode member: node.getMembers()) {
      gen(member, env);
    }
  
    cv.visitEnd();
    return null;
  }
  
  @Override
  public Void visitFieldDefinition(DartFieldDefinition node, Env env) {
    for(DartField field: node.getFields()) {
      gen(field, env);
    }
    return null;
  }
  @Override
  public Void visitField(DartField node, Env env) {
    env.getClassVisitor().visitField(ACC_PUBLIC,
        node.getName().getTargetName(),
        asType(node).getDescriptor(),
        null, null);  //FIXME, static field may have a static value
    return null;
  }
  
  @Override
  public Void visitInitializer(DartInitializer node, Env env) {
    MethodVisitor mv = env.getMethodVisitor();
    mv.visitVarInsn(ALOAD, 0);
    DartExpression expr = node.getValue();
    gen(expr, env);
    
    if (!node.isInvocation()) {
      FieldElement fieldElement = (FieldElement)node.getName().getTargetSymbol();
      ClassElement classElement = (ClassElement)fieldElement.getEnclosingElement();
      JVMType ownerType = getJVMType(classElement.getNode());
      Type fieldType = asType(fieldElement.getNode());
      mayInsertCast(mv, fieldType, asType(expr));
      mv.visitFieldInsn(PUTFIELD, asType(ownerType).getInternalName(),
          node.getName().getTargetName(),
          fieldType.getDescriptor());
    }
    // if it's an invocation, no need to fix the stack because all constructors return void
    
    return null;
  }
  
  
  private void genConstructorCall(DartInvocation node, Env env) {
    ConstructorElement element = (ConstructorElement)node.getReferencedElement();
    ClassElement classElement = (ClassElement)element.getEnclosingElement();
    JVMType receiverType = getJVMType(classElement.getNode());
    if (asType(receiverType).equals(OBJECT_TYPE)) {  // call to super object already done
      env.getMethodVisitor().visitInsn(POP);         // FIXME Hack
      return;
    }
    
    int i = 0;
    List<DartExpression> args = node.getArgs();
    JVMType[] types = new JVMType[args.size()];
    for(DartExpression arg: args) {
      types[i++] = getJVMType(arg);
      // no checkCast, it's done at runtime using invokedynamic
      gen(arg, env);
    }
    
    JVMFunctionType functionType = new JVMFunctionType(types, VOID);
    functionType = functionType.insertReceiverType(receiverType);
    
    MethodVisitor mv = env.getMethodVisitor();
    mv.visitInvokeDynamicInsn(asType(classElement.getNode()).getInternalName(),
        asType(functionType).getDescriptor(),
        BSM_SUPERCALL);
  }
  @Override
  public Void visitRedirectConstructorInvocation(DartRedirectConstructorInvocation node, Env env) {
    genConstructorCall(node, env);
    return null;
  }
  @Override
  public Void visitSuperConstructorInvocation(DartSuperConstructorInvocation node, Env env) {
    genConstructorCall(node, env);
    return null;
  }
  
  
  @Override
  public Void visitMethodDefinition(DartMethodDefinition node, Env env) {
    ClassVisitor cv = env.getClassVisitor();
    MethodElement methodElement = node.getSymbol();
    Modifiers modifiers = node.getModifiers();
    
    int flags;
    String name;
    EnclosingElement enclosingElement = methodElement.getEnclosingElement();
    if (methodElement.isConstructor()) {
      flags = ACC_PUBLIC;
      name = enclosingElement.getName();
    } else {
      name = methodElement.getName();
      if (name.equals("main")) {  //FIXME, is there another way to find the entry point ?
        generateEntryPoint(cv, (LibraryElement)enclosingElement);
        name = "__main__";
      }

      flags = ACC_PUBLIC |
          ((enclosingElement instanceof ClassElement)? 0: ACC_STATIC) |
          ((modifiers.isStatic())? ACC_STATIC: 0);

      if (modifiers.isNative()) {
        name = '@' + name; // don't use an annotation because the JDK implementation
        // slow down the init of the runtime too much
      }
    }
    
    MethodVisitor mv = cv.visitMethod(flags,
        name,
        asType(node).getDescriptor(),
        null, null);
    env = env.methodEnv(mv);
    
    if (methodElement.isConstructor()) {
      // first init initializer
      List<DartInitializer> initializers = node.getInitializers();
      for(DartInitializer initializer: initializers) {
        gen(initializer, env);
      }
      
      // then init fields
      ClassElement classElement = (ClassElement)methodElement.getEnclosingElement();
      Type ownerType = asType(asJVMType(classElement));
      for(Element member: classElement.getMembers()) {
        DartNode memberNode = member.getNode();
        if (memberNode instanceof DartFieldDefinition) {
          DartFieldDefinition fieldDefinition = (DartFieldDefinition)memberNode;
          for(DartField field: fieldDefinition.getFields()) {
            FieldElement fieldElement = field.getSymbol(); 
            DartExpression expr = field.getValue();
            if (expr != null) {
              mv.visitVarInsn(ALOAD, 0);
              gen(expr, env);
              mv.visitFieldInsn(PUTFIELD, ownerType.getInternalName(),
                  field.getName().getTargetName(),
                  asType(field).getDescriptor());
            }
          }
        }
      }
    }
    
    // natives are resolved at runtime, just add a return statement
    if (modifiers.isNative()) {
      mv.visitCode();
      Type returnType = asType(node.getFunction()).getReturnType();
      genDefaultValue(mv, returnType);
      mv.visitInsn(returnType.getOpcode(IRETURN));
      
      mv.visitMaxs(-1, -1);
      mv.visitEnd();
    } else {
      gen(node.getFunction(), env);  
    }
    
    return null;
  }
  
  
  @Override
  public Void visitFunction(DartFunction node, Env env) {
    MethodVisitor mv = env.getMethodVisitor();
    mv.visitCode();
    gen(node.getBody(), env);
    
    if (getJVMType(node.getBody()) == Liveness.ALIVE) {
      // add a return statement
      Type returnType = asType(node).getReturnType();
      genDefaultValue(mv, returnType);
      mv.visitInsn(returnType.getOpcode(IRETURN));
    }
    
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
    return null;
  }
  
  
  // --- statements

  @Override
  public Void visitBlock(DartBlock node, Env env) {
    for(DartStatement statement: node.getStatements()) {
      // don't generate unreachable statement
      if (getJVMType(statement) != Liveness.ALIVE) {
        break;
      }
      gen(statement, env);
    }
    return null;
  }
  
  @Override
  public Void visitVariableStatement(DartVariableStatement node, Env env) {
    for(DartVariable variableNode: node.getVariables()) {
      DartExpression expression = variableNode.getValue();
      gen(expression, env);
      Element element = variableNode.getSymbol();
      Type type = asType(variableNode);
      mayInsertCast(env.getMethodVisitor(), type, asType(expression));
      env.store(element, type);
    }
    return null;
  }


  @Override
  public Void visitExprStmt(DartExprStmt node, Env env) {
    gen(node.getExpression(), env);
    MethodVisitor mv = env.getMethodVisitor();
    fixStack(mv, asType(node.getExpression()));
    return null;
  }
  
  
  
  @Override
  public Void visitReturnStatement(DartReturnStatement node, Env env) {
    DartExpression expression = node.getValue();
    gen(expression, env);
    mayInsertCast(env.getMethodVisitor(), asType(node).getReturnType(), asType(expression));
    
    return null;
  }
  
  // --- expressions
  
  @Override
  public Void visitNewExpression(DartNewExpression node, Env env) {
    int i = 0;
    List<DartExpression> args = node.getArgs();
    JVMType[] types = new JVMType[args.size()];
    for(DartExpression arg: args) {
      types[i++] = getJVMType(arg);
      // no checkCast, it's done at runtime using invokedynamic
      gen(arg, env);
    }
    ConstructorElement element = node.getSymbol();
    ClassElement classElement = element.getConstructorType();
    JVMFunctionType functionType = new JVMFunctionType(types, asJVMType(classElement));
    
    env.getMethodVisitor().visitInvokeDynamicInsn(classElement.getName(),
        asType(functionType).getDescriptor(),
        BSM_NEWCALL);
    return null;
  }
  
  @Override
  public Void visitUnqualifiedInvocation(DartUnqualifiedInvocation node, Env env) {
    int i = 0;
    List<DartExpression> args = node.getArgs();
    JVMType[] types = new JVMType[args.size()];
    for(DartExpression arg: args) {
      types[i++] = getJVMType(arg);
      // no checkCast, it's done at runtime using invokedynamic
      gen(arg, env);
    }
    Element element = node.getReferencedElement();
    //System.out.println("referenced element" + element+" "+System.identityHashCode(element));
    
    LibraryElement enclosingElement = (LibraryElement)element.getEnclosingElement();
    JVMFunctionType functionType = new JVMFunctionType(types, getJVMType(node));
    
    env.getMethodVisitor().visitInvokeDynamicInsn(element.getName(),
        asType(functionType).getDescriptor(),
        BSM_FUNCALL, 
        Type.getObjectType(getLibraryClassName(enclosingElement)));
    return null;
  }
  
  @Override
  public Void visitMethodInvocation(DartMethodInvocation node, Env env) {
    List<DartExpression> args = node.getArgs();
    JVMType[] types = new JVMType[1 + args.size()];
    types[0] = getJVMType(node.getTarget());
    gen(node.getTarget(), env);
    int i = 1;
    for(DartExpression arg: args) {
      types[i++] = getJVMType(arg);
      // no checkCast, it's done at runtime using invokedynamic
      gen(arg, env);
    }
    
    JVMFunctionType functionType = new JVMFunctionType(types, getJVMType(node));
    
    //System.out.println("gen meth invoke "+node.getFunctionNameString());
    
    env.getMethodVisitor().visitInvokeDynamicInsn(node.getFunctionNameString(),
        asType(functionType).getDescriptor(),
        BSM_METHCALL);
    return null;
  }
  
  @Override
  public Void visitIdentifier(DartIdentifier node, Env env) {
    env.load(node.getSymbol(), asType(node));
    return null;
  }

  //--- literals

  @Override
  public Void visitBooleanLiteral(DartBooleanLiteral node, Env env) {
    env.getMethodVisitor().visitInsn((node.getValue())? ICONST_1: ICONST_0);
    return null;
  }
  @Override
  public Void visitIntegerLiteral(DartIntegerLiteral node, Env env) {
    env.getMethodVisitor().visitLdcInsn(node.getValue().intValue());
    return null;
  }
  @Override
  public Void visitDoubleLiteral(DartDoubleLiteral node, Env env) {
    env.getMethodVisitor().visitLdcInsn(node.getValue());
    return null;
  }
  @Override
  public Void visitStringLiteral(DartStringLiteral node, Env env) {
    env.getMethodVisitor().visitLdcInsn(node.getValue());
    return null;
  }

  
  // -- NYI
  
  @Override
  public Void visitArrayAccess(DartArrayAccess node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitArrayLiteral(DartArrayLiteral node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitAssertion(DartAssertion node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitBinaryExpression(DartBinaryExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitBreakStatement(DartBreakStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitFunctionObjectInvocation(DartFunctionObjectInvocation node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitCase(DartCase node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitConditional(DartConditional node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitContinueStatement(DartContinueStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitDefault(DartDefault node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitDoWhileStatement(DartDoWhileStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitEmptyStatement(DartEmptyStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitForInStatement(DartForInStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitForStatement(DartForStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitFunctionExpression(DartFunctionExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitFunctionTypeAlias(DartFunctionTypeAlias node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitIfStatement(DartIfStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitImportDirective(DartImportDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitLabel(DartLabel node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitLibraryDirective(DartLibraryDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitMapLiteral(DartMapLiteral node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitMapLiteralEntry(DartMapLiteralEntry node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitNativeDirective(DartNativeDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitNullLiteral(DartNullLiteral node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitParameter(DartParameter node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitParameterizedNode(DartParameterizedNode node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitParenthesizedExpression(DartParenthesizedExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitPropertyAccess(DartPropertyAccess node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitTypeNode(DartTypeNode node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitResourceDirective(DartResourceDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitSourceDirective(DartSourceDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitStringInterpolation(DartStringInterpolation node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitSuperExpression(DartSuperExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitSwitchStatement(DartSwitchStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitSyntheticErrorExpression(DartSyntheticErrorExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitSyntheticErrorStatement(DartSyntheticErrorStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitThisExpression(DartThisExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitThrowStatement(DartThrowStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitCatchBlock(DartCatchBlock node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitTryStatement(DartTryStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitUnaryExpression(DartUnaryExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitVariable(DartVariable node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitWhileStatement(DartWhileStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitNamedExpression(DartNamedExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitTypeExpression(DartTypeExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitTypeParameter(DartTypeParameter node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public Void visitNativeBlock(DartNativeBlock node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }
}

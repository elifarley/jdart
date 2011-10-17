package org.jdart.compiler.backend.jvm;

import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.BOOLEAN;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.DOUBLE;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.DYNAMIC;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.FUNCTION;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.INT;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.NONE;
import static org.jdart.compiler.backend.jvm.JVMPrimitiveType.VOID;

import static org.jdart.compiler.backend.jvm.Liveness.ALIVE;
import static org.jdart.compiler.backend.jvm.Liveness.DEAD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.ConstructorElement;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.ElementKind;
import com.google.dart.compiler.resolver.EnclosingElement;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.type.InterfaceType;
import com.google.dart.compiler.type.Type;

public class TypeChecker implements Visitor<JVMType, TypeChecker.Env> {
  private final DartVisitorBridge<JVMType, Env> bridge =
      new DartVisitorBridge<>(this);
  
  public DartPlainVisitor<JVMType> getBridge() {
    return bridge;
  }
  
  private final Map<DartNode, JVMType> typeMap;
  private final HashMap<Element, JVMType> elementMap =
      new HashMap<>();
      
  public TypeChecker(Map<DartNode, JVMType> typeMap) {
    this.typeMap = typeMap;
  }    
      
  static class Env {
    private final JVMType expectedType;
    private final JVMType returnType;

    Env(JVMType expectedType, JVMType returnType) {
      this.expectedType = expectedType;
      this.returnType = returnType;
    }
    
    public JVMType getExpectedType() {
      return expectedType;
    }
    public JVMType getReturnType() {
      return returnType;
    }
    
    public Env expectedType(JVMType expectedType) {
      if (this.expectedType == expectedType) {
        return this;
      }
      return new Env(expectedType, returnType);
    }

    public Env returnType(JVMType returnType) {
      if (this.returnType == returnType) {
        return this;
      }
      return new Env(expectedType, returnType);
    }
  }
  
  private JVMType typeCheck(DartNode node, Env env) {
    if (node == null) {
      return DYNAMIC;
    }
    JVMType type = bridge.accept(node, env);
    if (type != null) {
      typeMap.put(node, type);
    }
    return type;
  }
  
  // --- helper methods
  
  private JVMType asType(DartTypeNode node) {
    if (node == null) {
      return DYNAMIC;
    }
    return asType(node.getType());
  }
  
  private static JVMType asType(Type type) {
    switch(type.getKind()) {
    case DYNAMIC:
      return DYNAMIC;
    case VOID:
      return VOID;
    case FUNCTION:
      return FUNCTION;
    case NONE:
      return NONE;
    case INTERFACE:
      String name = ((InterfaceType)type).getElement().getName();
      //FIXME, revisit
      switch(name) {
      case "int":
        return INT;
      case "double":
        return DOUBLE;
      case "bool":
        return BOOLEAN;
      }
      
      return new JVMInterfaceType(name);
    case VARIABLE:
    case FUNCTION_ALIAS:
    default:
      throw new UnsupportedOperationException();
    }
  }
  
  
  // --- unit
  
  @Override
  public JVMType visitUnit(DartUnit node, Env env) {
    env = new Env(null, null);
    for(DartNode topLevelNode: node.getTopLevelNodes()) {
      typeCheck(topLevelNode, env);
    }
    return null;
  }
  
  
  // --- type
  
  @Override
  public JVMType visitTypeNode(DartTypeNode node, Env env) {
    throw new AssertionError("should use asType instead");
  }
  
  // --- class
  
  @Override
  public JVMType visitClass(DartClass node, Env env) {
    String className = node.getClassName();
    
    // map Dart default object to Java one 
    switch(className) {
    case "Object":
      className = "java/lang/Object";
      break;
    case "String":
      className = "java/lang/String";
      break;
    default:
    }
    
    JVMInterfaceType type = new JVMInterfaceType(className);
    elementMap.put(node.getSymbol(), type);
    for(DartNode member: node.getMembers()) {
      typeCheck(member, env);
    }
    return type;
  }
  
  @Override
  public JVMType visitFieldDefinition(DartFieldDefinition node, Env env) {
    JVMType type = asType(node.getTypeNode());
    env = env.expectedType(type);
    for(DartField field: node.getFields()) {
      typeCheck(field, env);
    }
    return null;
  }
  @Override
  public JVMType visitField(DartField node, Env env) {
    Modifiers modifiers = node.getModifiers();
    DartExpression expr = node.getValue();
    JVMType type = (expr != null)? typeCheck(expr, env): env.getExpectedType();
    type = (modifiers.isFinal())? type: env.getExpectedType();
    elementMap.put(node.getSymbol(), type);
    return type;
  }
  
  @Override
  public JVMType visitInitializer(DartInitializer node, Env env) {
    JVMType expectedType;
    if (!node.isInvocation()) { // not a call to this() or super()
      Element element = (Element)node.getSymbol();
      expectedType = elementMap.get(element);
    } else {
      expectedType = VOID;
    }
    typeCheck(node.getValue(), env.expectedType(expectedType));
    return VOID;
  }
  
  @Override
  public JVMType visitRedirectConstructorInvocation(DartRedirectConstructorInvocation node, Env env) {
    JVMFunctionType functionType = (JVMFunctionType)elementMap.get(node.getReferencedElement());
    List<JVMType> parameterTypes = functionType.getParameterTypes();
    int i = 0;
    for(DartExpression arg: node.getArgs()) {
      typeCheck(arg, env.expectedType(parameterTypes.get(i++)));
    }
    return VOID;
  }
  @Override
  public JVMType visitSuperConstructorInvocation(DartSuperConstructorInvocation node, Env env) {
    JVMFunctionType functionType = (JVMFunctionType)elementMap.get(node.getReferencedElement());
    List<JVMType> parameterTypes = functionType.getParameterTypes();
    int i = 0;
    for(DartExpression arg: node.getArgs()) {
      typeCheck(arg, env.expectedType(parameterTypes.get(i++)));
    }
    return VOID;
  }
  
  
  @Override
  public JVMType visitMethodDefinition(DartMethodDefinition node, Env env) {
    MethodElement method = node.getSymbol();
    for(DartInitializer initializer: node.getInitializers()) {
      typeCheck(initializer, env);
    }
    env = env.expectedType((method.isConstructor())? VOID: DYNAMIC);
    JVMFunctionType type = (JVMFunctionType)typeCheck(node.getFunction(), env);
    
    //System.out.println(System.identityHashCode(method)+" "+method.getName()+methodType);
    elementMap.put(method, type);
    return type;
  }
  
  @Override
  public JVMType visitFunction(DartFunction node, Env env) {
    List<DartParameter> parameters = node.getParams();
    JVMType[] parameterTypes = new JVMType[parameters.size()];
    int i = 0;
    for(DartParameter parameter: parameters) {
      JVMType type = typeCheck(parameter, env);
      parameterTypes[i++] = type;
      elementMap.put(parameter.getSymbol(), type);
    }
    
    JVMType returnType = asType(node.getReturnTypeNode());
    returnType = (returnType == DYNAMIC)? env.getExpectedType(): returnType;
    typeCheck(node.getBody(), env.returnType(returnType));
    return new JVMFunctionType(parameterTypes, returnType);
  }
  
  @Override
  public JVMType visitParameter(DartParameter node, Env env) {
    boolean isFinal = node.getModifiers().isFinal();
    return (isFinal)? DYNAMIC: asType(node.getTypeNode());
  }
  
  
  
  // --- statements
  
  @Override
  public JVMType visitBlock(DartBlock node, Env env) {
    boolean liveness = true;
    for(DartStatement statement: node.getStatements()) {
      // not ALIVE implies DEAD, the reverse is false
      boolean isDead = typeCheck(statement, env) != ALIVE;
      if (isDead) {
        liveness = false;
      }
    }
    return (liveness)? ALIVE: DEAD;
  }
  
  @Override
  public JVMType visitNativeBlock(DartNativeBlock node, Env env) {
    // do nothing, the linkage will be done at runtime
    return DEAD; 
  }
  
  @Override
  public JVMType visitVariableStatement(DartVariableStatement node, Env env) {
    boolean isFinal = node.getModifiers().isFinal();
    JVMType specifiedType = (isFinal)? DYNAMIC: asType(node.getTypeNode());
    
    for(DartVariable variableNode: node.getVariables()) {
      DartExpression expr = variableNode.getValue();
      JVMType type = typeCheck(expr, env.expectedType(specifiedType));
      type = (isFinal)? type: specifiedType;
      Element symbol = variableNode.getSymbol();
      elementMap.put(symbol, type);
      
      typeMap.put(variableNode, type);
    }
    return ALIVE;
  }
  
  @Override
  public JVMType visitExprStmt(DartExprStmt node, Env env) {
    typeCheck(node.getExpression(), env.expectedType(VOID));
    return ALIVE;
  }
  
  @Override
  public JVMType visitReturnStatement(DartReturnStatement node, Env env) {
    // liveness trick, not ALIVE implies DEAD
    // so return is always DEAD
    return typeCheck(node.getValue(), env.expectedType(env.getReturnType()));
  }
  
  
  // --- expressions
  
  @Override
  public JVMType visitNewExpression(DartNewExpression node, Env env) {
    ConstructorElement element = node.getSymbol();
    JVMFunctionType functionType = (JVMFunctionType)elementMap.get(element);
    List<JVMType> parameterTypes = functionType.getParameterTypes();
    int i = 0;
    for(DartExpression arg: node.getArgs()) {
      typeCheck(arg, env.expectedType(parameterTypes.get(i++)));
    }
    
    return asType(element.getConstructorType().getType());
  }
  
  @Override
  public JVMType visitUnqualifiedInvocation(DartUnqualifiedInvocation node, Env env) {
    JVMFunctionType functionType = (JVMFunctionType)elementMap.get(node.getReferencedElement());
    List<JVMType> parameterTypes = functionType.getParameterTypes();
    int i = 0;
    for(DartExpression arg: node.getArgs()) {
      typeCheck(arg, env.expectedType(parameterTypes.get(i++)));
    }
    JVMType returnType = env.getExpectedType();
    return (returnType != DYNAMIC)? returnType: functionType.getReturnType();
  }
  
  @Override
  public JVMType visitMethodInvocation(DartMethodInvocation node, Env env) {
    // element may be null if called on a dynamic variable
    Element element = node.getReferencedElement();
    JVMFunctionType functionType = null;
    List<JVMType> parameterTypes = null;
    if (element != null) {
      functionType = (JVMFunctionType)elementMap.get(element);
      functionType = functionType.insertReceiverType(elementMap.get(element.getEnclosingElement()));
      parameterTypes = functionType.getParameterTypes();
    }
    
    int i = 0;
    JVMType expectedType = (parameterTypes == null)? DYNAMIC: parameterTypes.get(i++);
    typeCheck(node.getTarget(), env.expectedType(expectedType));
    for(DartExpression arg: node.getArgs()) {
      expectedType = (parameterTypes == null)? DYNAMIC: parameterTypes.get(i++);
      typeCheck(arg, env.expectedType(expectedType));
    }
    JVMType returnType =  env.getExpectedType();
    return (returnType != DYNAMIC)? returnType: (functionType == null)? DYNAMIC: functionType.getReturnType();
  }
  
  @Override
  public JVMType visitIdentifier(DartIdentifier node, Env env) {
    Element symbol = node.getSymbol();
    return elementMap.get(symbol);
  }

  //--- literals

  @Override
  public JVMType visitBooleanLiteral(DartBooleanLiteral node, Env env) {
    return BOOLEAN;
  }
  @Override
  public JVMType visitIntegerLiteral(DartIntegerLiteral node, Env env) {
    return INT;
  }
  @Override
  public JVMType visitDoubleLiteral(DartDoubleLiteral node, Env env) {
    return DOUBLE;
  }
  @Override
  public JVMType visitStringLiteral(DartStringLiteral node, Env env) {
    return new JVMInterfaceType("String");
  }

  
  // --- NYI
  
  @Override
  public JVMType visitArrayAccess(DartArrayAccess node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitArrayLiteral(DartArrayLiteral node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitAssertion(DartAssertion node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitBinaryExpression(DartBinaryExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitBreakStatement(DartBreakStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitFunctionObjectInvocation(
      DartFunctionObjectInvocation node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitCase(DartCase node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitConditional(DartConditional node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitContinueStatement(DartContinueStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitDefault(DartDefault node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitDoWhileStatement(DartDoWhileStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitEmptyStatement(DartEmptyStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitForInStatement(DartForInStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitForStatement(DartForStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitFunctionExpression(DartFunctionExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitFunctionTypeAlias(DartFunctionTypeAlias node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitIfStatement(DartIfStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitImportDirective(DartImportDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitLabel(DartLabel node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitLibraryDirective(DartLibraryDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitMapLiteral(DartMapLiteral node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitMapLiteralEntry(DartMapLiteralEntry node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitNativeDirective(DartNativeDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitNullLiteral(DartNullLiteral node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitParameterizedNode(DartParameterizedNode node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitParenthesizedExpression(DartParenthesizedExpression node,
      Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitPropertyAccess(DartPropertyAccess node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitResourceDirective(DartResourceDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitSourceDirective(DartSourceDirective node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitStringInterpolation(DartStringInterpolation node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitSuperExpression(DartSuperExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitSwitchStatement(DartSwitchStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitSyntheticErrorExpression(
      DartSyntheticErrorExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitSyntheticErrorStatement(DartSyntheticErrorStatement node,
      Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitThisExpression(DartThisExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitThrowStatement(DartThrowStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitCatchBlock(DartCatchBlock node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitTryStatement(DartTryStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitUnaryExpression(DartUnaryExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitVariable(DartVariable node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitWhileStatement(DartWhileStatement node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitNamedExpression(DartNamedExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitTypeExpression(DartTypeExpression node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }

  @Override
  public JVMType visitTypeParameter(DartTypeParameter node, Env env) {
    throw new UnsupportedOperationException("Not Yet Implemented");
  }
}

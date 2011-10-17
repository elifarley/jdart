package org.jdart.compiler.backend.jvm;

import java.util.List;

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

class DartVisitorBridge<R, E> implements DartPlainVisitor<R> {
  private final Visitor<? extends R, ? super E> visitor;
  private E env;
  
  public DartVisitorBridge(Visitor<? extends R, ? super E> visitor) {
    this.visitor = visitor;
  }

  public R accept(DartNode node, E env) {
    this.env = env;
    try {
      return node.accept(this);
    } finally {
      env = null;
    }
  }
  
  @Override
  public void visit(List<? extends DartNode> nodes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visitArrayAccess(DartArrayAccess node) {
    return visitor.visitArrayAccess(node, env);
  }

  @Override
  public R visitArrayLiteral(DartArrayLiteral node) {
    return visitor.visitArrayLiteral(node, env);
  }

  @Override
  public R visitAssertion(DartAssertion node) {
    return visitor.visitAssertion(node, env);
  }

  @Override
  public R visitBinaryExpression(DartBinaryExpression node) {
    return visitor.visitBinaryExpression(node, env);
  }

  @Override
  public R visitBlock(DartBlock node) {
    return visitor.visitBlock(node, env);
  }

  @Override
  public R visitBooleanLiteral(DartBooleanLiteral node) {
    return visitor.visitBooleanLiteral(node, env);
  }

  @Override
  public R visitBreakStatement(DartBreakStatement node) {
    return visitor.visitBreakStatement(node, env);
  }

  @Override
  public R visitFunctionObjectInvocation(DartFunctionObjectInvocation node) {
    return visitor.visitFunctionObjectInvocation(node, env);
  }

  @Override
  public R visitMethodInvocation(DartMethodInvocation node) {
    return visitor.visitMethodInvocation(node, env);
  }

  @Override
  public R visitSuperConstructorInvocation(DartSuperConstructorInvocation node) {
    return visitor.visitSuperConstructorInvocation(node, env);
  }

  @Override
  public R visitCase(DartCase node) {
    return visitor.visitCase(node, env);
  }

  @Override
  public R visitClass(DartClass node) {
    return visitor.visitClass(node, env);
  }

  @Override
  public R visitConditional(DartConditional node) {
    return visitor.visitConditional(node, env);
  }

  @Override
  public R visitContinueStatement(DartContinueStatement node) {
    return visitor.visitContinueStatement(node, env);
  }

  @Override
  public R visitDefault(DartDefault node) {
    return visitor.visitDefault(node, env);
  }

  @Override
  public R visitDoubleLiteral(DartDoubleLiteral node) {
    return visitor.visitDoubleLiteral(node, env);
  }

  @Override
  public R visitDoWhileStatement(DartDoWhileStatement node) {
    return visitor.visitDoWhileStatement(node, env);
  }

  @Override
  public R visitEmptyStatement(DartEmptyStatement node) {
    return visitor.visitEmptyStatement(node, env);
  }

  @Override
  public R visitExprStmt(DartExprStmt node) {
    return visitor.visitExprStmt(node, env);
  }

  @Override
  public R visitField(DartField node) {
    return visitor.visitField(node, env);
  }

  @Override
  public R visitFieldDefinition(DartFieldDefinition node) {
    return visitor.visitFieldDefinition(node, env);
  }

  @Override
  public R visitForInStatement(DartForInStatement node) {
    return visitor.visitForInStatement(node, env);
  }

  @Override
  public R visitForStatement(DartForStatement node) {
    return visitor.visitForStatement(node, env);
  }

  @Override
  public R visitFunction(DartFunction node) {
    return visitor.visitFunction(node, env);
  }

  @Override
  public R visitFunctionExpression(DartFunctionExpression node) {
    return visitor.visitFunctionExpression(node, env);
  }

  @Override
  public R visitFunctionTypeAlias(DartFunctionTypeAlias node) {
    return visitor.visitFunctionTypeAlias(node, env);
  }

  @Override
  public R visitIdentifier(DartIdentifier node) {
    return visitor.visitIdentifier(node, env);
  }

  @Override
  public R visitIfStatement(DartIfStatement node) {
    return visitor.visitIfStatement(node, env);
  }

  @Override
  public R visitImportDirective(DartImportDirective node) {
    return visitor.visitImportDirective(node, env);
  }

  @Override
  public R visitInitializer(DartInitializer node) {
    return visitor.visitInitializer(node, env);
  }

  @Override
  public R visitIntegerLiteral(DartIntegerLiteral node) {
    return visitor.visitIntegerLiteral(node, env);
  }

  @Override
  public R visitLabel(DartLabel node) {
    return visitor.visitLabel(node, env);
  }

  @Override
  public R visitLibraryDirective(DartLibraryDirective node) {
    return visitor.visitLibraryDirective(node, env);
  }

  @Override
  public R visitMapLiteral(DartMapLiteral node) {
    return visitor.visitMapLiteral(node, env);
  }

  @Override
  public R visitMapLiteralEntry(DartMapLiteralEntry node) {
    return visitor.visitMapLiteralEntry(node, env);
  }

  @Override
  public R visitMethodDefinition(DartMethodDefinition node) {
    return visitor.visitMethodDefinition(node, env);
  }

  @Override
  public R visitNativeDirective(DartNativeDirective node) {
    return visitor.visitNativeDirective(node, env);
  }

  @Override
  public R visitNewExpression(DartNewExpression node) {
    return visitor.visitNewExpression(node, env);
  }

  @Override
  public R visitNullLiteral(DartNullLiteral node) {
    return visitor.visitNullLiteral(node, env);
  }

  @Override
  public R visitParameter(DartParameter node) {
    return visitor.visitParameter(node, env);
  }

  @Override
  public R visitParameterizedNode(DartParameterizedNode node) {
    return visitor.visitParameterizedNode(node, env);
  }

  @Override
  public R visitParenthesizedExpression(DartParenthesizedExpression node) {
    return visitor.visitParenthesizedExpression(node, env);
  }

  @Override
  public R visitPropertyAccess(DartPropertyAccess node) {
    return visitor.visitPropertyAccess(node, env);
  }

  @Override
  public R visitTypeNode(DartTypeNode node) {
    return visitor.visitTypeNode(node, env);
  }

  @Override
  public R visitResourceDirective(DartResourceDirective node) {
    return visitor.visitResourceDirective(node, env);
  }

  @Override
  public R visitReturnStatement(DartReturnStatement node) {
    return visitor.visitReturnStatement(node, env);
  }

  @Override
  public R visitSourceDirective(DartSourceDirective node) {
    return visitor.visitSourceDirective(node, env);
  }

  @Override
  public R visitStringLiteral(DartStringLiteral node) {
    return visitor.visitStringLiteral(node, env);
  }

  @Override
  public R visitStringInterpolation(DartStringInterpolation node) {
    return visitor.visitStringInterpolation(node, env);
  }

  @Override
  public R visitSuperExpression(DartSuperExpression node) {
    return visitor.visitSuperExpression(node, env);
  }

  @Override
  public R visitSwitchStatement(DartSwitchStatement node) {
    return visitor.visitSwitchStatement(node, env);
  }

  @Override
  public R visitSyntheticErrorExpression(DartSyntheticErrorExpression node) {
    return visitor.visitSyntheticErrorExpression(node, env);
  }

  @Override
  public R visitSyntheticErrorStatement(DartSyntheticErrorStatement node) {
    return visitor.visitSyntheticErrorStatement(node, env);
  }

  @Override
  public R visitThisExpression(DartThisExpression node) {
    return visitor.visitThisExpression(node, env);
  }

  @Override
  public R visitThrowStatement(DartThrowStatement node) {
    return visitor.visitThrowStatement(node, env);
  }

  @Override
  public R visitCatchBlock(DartCatchBlock node) {
    return visitor.visitCatchBlock(node, env);
  }

  @Override
  public R visitTryStatement(DartTryStatement node) {
    return visitor.visitTryStatement(node, env);
  }

  @Override
  public R visitUnaryExpression(DartUnaryExpression node) {
    return visitor.visitUnaryExpression(node, env);
  }

  @Override
  public R visitUnit(DartUnit node) {
    return visitor.visitUnit(node, env);
  }

  @Override
  public R visitUnqualifiedInvocation(DartUnqualifiedInvocation node) {
    return visitor.visitUnqualifiedInvocation(node, env);
  }

  @Override
  public R visitVariable(DartVariable node) {
    return visitor.visitVariable(node, env);
  }

  @Override
  public R visitVariableStatement(DartVariableStatement node) {
    return visitor.visitVariableStatement(node, env);
  }

  @Override
  public R visitWhileStatement(DartWhileStatement node) {
    return visitor.visitWhileStatement(node, env);
  }

  @Override
  public R visitNamedExpression(DartNamedExpression node) {
    return visitor.visitNamedExpression(node, env);
  }

  @Override
  public R visitTypeExpression(DartTypeExpression node) {
    return visitor.visitTypeExpression(node, env);
  }

  @Override
  public R visitTypeParameter(DartTypeParameter node) {
    return visitor.visitTypeParameter(node, env);
  }

  @Override
  public R visitNativeBlock(DartNativeBlock node) {
    return visitor.visitNativeBlock(node, env);
  }

  @Override
  public R visitRedirectConstructorInvocation(DartRedirectConstructorInvocation node) {
    return visitor.visitRedirectConstructorInvocation(node, env);
  }
}
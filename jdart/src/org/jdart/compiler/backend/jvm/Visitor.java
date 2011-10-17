package org.jdart.compiler.backend.jvm;

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
import com.google.dart.compiler.ast.DartNullLiteral;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartParameterizedNode;
import com.google.dart.compiler.ast.DartParenthesizedExpression;
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

public interface Visitor<R, E> {
  R visitArrayAccess(DartArrayAccess node, E env);

  R visitArrayLiteral(DartArrayLiteral node, E env);

  R visitAssertion(DartAssertion node, E env);

  R visitBinaryExpression(DartBinaryExpression node, E env);

  R visitBlock(DartBlock node, E env);

  R visitBooleanLiteral(DartBooleanLiteral node, E env);

  R visitBreakStatement(DartBreakStatement node, E env);

  R visitFunctionObjectInvocation(DartFunctionObjectInvocation node, E env);

  R visitMethodInvocation(DartMethodInvocation node, E env);

  R visitSuperConstructorInvocation(DartSuperConstructorInvocation node, E env);

  R visitCase(DartCase node, E env);

  R visitClass(DartClass node, E env);

  R visitConditional(DartConditional node, E env);

  R visitContinueStatement(DartContinueStatement node, E env);

  R visitDefault(DartDefault node, E env);

  R visitDoubleLiteral(DartDoubleLiteral node, E env);

  R visitDoWhileStatement(DartDoWhileStatement node, E env);

  R visitEmptyStatement(DartEmptyStatement node, E env);

  R visitExprStmt(DartExprStmt node, E env);

  R visitField(DartField node, E env);

  R visitFieldDefinition(DartFieldDefinition node, E env);

  R visitForInStatement(DartForInStatement node, E env);

  R visitForStatement(DartForStatement node, E env);

  R visitFunction(DartFunction node, E env);

  R visitFunctionExpression(DartFunctionExpression node, E env);

  R visitFunctionTypeAlias(DartFunctionTypeAlias node, E env);

  R visitIdentifier(DartIdentifier node, E env);

  R visitIfStatement(DartIfStatement node, E env);

  R visitImportDirective(DartImportDirective node, E env);

  R visitInitializer(DartInitializer node, E env);

  R visitIntegerLiteral(DartIntegerLiteral node, E env);

  R visitLabel(DartLabel node, E env);

  R visitLibraryDirective(DartLibraryDirective node, E env);

  R visitMapLiteral(DartMapLiteral node, E env);

  R visitMapLiteralEntry(DartMapLiteralEntry node, E env);

  R visitMethodDefinition(DartMethodDefinition node, E env);

  R visitNativeDirective(DartNativeDirective node, E env);

  R visitNewExpression(DartNewExpression node, E env);

  R visitNullLiteral(DartNullLiteral node, E env);

  R visitParameter(DartParameter node, E env);

  R visitParameterizedNode(DartParameterizedNode node, E env);

  R visitParenthesizedExpression(DartParenthesizedExpression node, E env);

  R visitPropertyAccess(DartPropertyAccess node, E env);

  R visitTypeNode(DartTypeNode node, E env);

  R visitResourceDirective(DartResourceDirective node, E env);

  R visitReturnStatement(DartReturnStatement node, E env);

  R visitSourceDirective(DartSourceDirective node, E env);

  R visitStringLiteral(DartStringLiteral node, E env);

  R visitStringInterpolation(DartStringInterpolation node, E env);

  R visitSuperExpression(DartSuperExpression node, E env);

  R visitSwitchStatement(DartSwitchStatement node, E env);

  R visitSyntheticErrorExpression(DartSyntheticErrorExpression node, E env);

  R visitSyntheticErrorStatement(DartSyntheticErrorStatement node, E env);

  R visitThisExpression(DartThisExpression node, E env);

  R visitThrowStatement(DartThrowStatement node, E env);

  R visitCatchBlock(DartCatchBlock node, E env);

  R visitTryStatement(DartTryStatement node, E env);

  R visitUnaryExpression(DartUnaryExpression node, E env);

  R visitUnit(DartUnit node, E env);

  R visitUnqualifiedInvocation(DartUnqualifiedInvocation node, E env);

  R visitVariable(DartVariable node, E env);

  R visitVariableStatement(DartVariableStatement node, E env);

  R visitWhileStatement(DartWhileStatement node, E env);

  R visitNamedExpression(DartNamedExpression node, E env);

  R visitTypeExpression(DartTypeExpression node, E env);

  R visitTypeParameter(DartTypeParameter node, E env);

  R visitNativeBlock(DartNativeBlock node, E env);

  R visitRedirectConstructorInvocation(DartRedirectConstructorInvocation node, E env);
}
/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.codemanipulation;

import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.che.jdt.util.JdtFlags;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.NecessaryParenthesesChecker;
import org.eclipse.jdt.ui.CodeGeneration;

public class GetterSetterUtil {

  private static final String[] EMPTY = new String[0];

  // no instances
  private GetterSetterUtil() {}

  public static String getGetterName(IField field, String[] excludedNames)
      throws JavaModelException {
    boolean useIs = StubUtility.useIsForBooleanGetters(field.getJavaProject());
    return getGetterName(field, excludedNames, useIs);
  }

  private static String getGetterName(
      IField field, String[] excludedNames, boolean useIsForBoolGetters) throws JavaModelException {
    if (excludedNames == null) {
      excludedNames = EMPTY;
    }
    return getGetterName(
        field.getJavaProject(),
        field.getElementName(),
        field.getFlags(),
        useIsForBoolGetters && JavaModelUtil.isBoolean(field),
        excludedNames);
  }

  public static String getGetterName(
      IVariableBinding variableType,
      IJavaProject project,
      String[] excludedNames,
      boolean isBoolean) {
    boolean useIs = StubUtility.useIsForBooleanGetters(project) && isBoolean;
    return getGetterName(
        project, variableType.getName(), variableType.getModifiers(), useIs, excludedNames);
  }

  public static String getGetterName(
      IJavaProject project,
      String fieldName,
      int flags,
      boolean isBoolean,
      String[] excludedNames) {
    return NamingConventions.suggestGetterName(project, fieldName, flags, isBoolean, excludedNames);
  }

  public static String getSetterName(
      IVariableBinding variableType,
      IJavaProject project,
      String[] excludedNames,
      boolean isBoolean) {
    return getSetterName(
        project, variableType.getName(), variableType.getModifiers(), isBoolean, excludedNames);
  }

  public static String getSetterName(
      IJavaProject project,
      String fieldName,
      int flags,
      boolean isBoolean,
      String[] excludedNames) {
    boolean useIs = StubUtility.useIsForBooleanGetters(project);
    return NamingConventions.suggestSetterName(
        project, fieldName, flags, useIs && isBoolean, excludedNames);
  }

  public static String getSetterName(IField field, String[] excludedNames)
      throws JavaModelException {
    if (excludedNames == null) {
      excludedNames = EMPTY;
    }
    return getSetterName(
        field.getJavaProject(),
        field.getElementName(),
        field.getFlags(),
        JavaModelUtil.isBoolean(field),
        excludedNames);
  }

  public static IMethod getGetter(IField field) throws JavaModelException {
    String getterName = getGetterName(field, EMPTY, true);
    IMethod primaryCandidate =
        JavaModelUtil.findMethod(getterName, new String[0], false, field.getDeclaringType());
    if (!JavaModelUtil.isBoolean(field) || (primaryCandidate != null && primaryCandidate.exists()))
      return primaryCandidate;
    // bug 30906 describes why we need to look for other alternatives here (try with get... for
    // booleans)
    String secondCandidateName = getGetterName(field, EMPTY, false);
    return JavaModelUtil.findMethod(
        secondCandidateName, new String[0], false, field.getDeclaringType());
  }

  public static IMethod getSetter(IField field) throws JavaModelException {
    String[] args = new String[] {field.getTypeSignature()};
    return JavaModelUtil.findMethod(
        getSetterName(field, EMPTY), args, false, field.getDeclaringType());
  }

  /**
   * Create a stub for a getter of the given field using getter/setter templates. The resulting code
   * has to be formatted and indented.
   *
   * @param field The field to create a getter for
   * @param setterName The chosen name for the setter
   * @param addComments If <code>true</code>, comments will be added.
   * @param flags The flags signaling visibility, if static, synchronized or final
   * @return Returns the generated stub.
   * @throws CoreException when stub creation failed
   */
  public static String getSetterStub(
      IField field, String setterName, boolean addComments, int flags) throws CoreException {

    String fieldName = field.getElementName();
    IType parentType = field.getDeclaringType();

    String returnSig = field.getTypeSignature();
    String typeName = Signature.toString(returnSig);

    IJavaProject project = field.getJavaProject();

    String accessorName = StubUtility.getBaseName(field);
    String argname = StubUtility.suggestArgumentName(project, accessorName, EMPTY);

    boolean isStatic = Flags.isStatic(flags);
    boolean isSync = Flags.isSynchronized(flags);
    boolean isFinal = Flags.isFinal(flags);

    String lineDelim =
        "\n"; // Use default line delimiter, as generated stub has to be formatted anyway
    // //$NON-NLS-1$
    StringBuffer buf = new StringBuffer();
    if (addComments) {
      String comment =
          CodeGeneration.getSetterComment(
              field.getCompilationUnit(),
              parentType.getTypeQualifiedName('.'),
              setterName,
              field.getElementName(),
              typeName,
              argname,
              accessorName,
              lineDelim);
      if (comment != null) {
        buf.append(comment);
        buf.append(lineDelim);
      }
    }
    buf.append(JdtFlags.getVisibilityString(flags));
    buf.append(' ');
    if (isStatic) buf.append("static "); // $NON-NLS-1$
    if (isSync) buf.append("synchronized "); // $NON-NLS-1$
    if (isFinal) buf.append("final "); // $NON-NLS-1$

    buf.append("void "); // $NON-NLS-1$
    buf.append(setterName);
    buf.append('(');
    buf.append(typeName);
    buf.append(' ');
    buf.append(argname);
    buf.append(") {"); // $NON-NLS-1$
    buf.append(lineDelim);

    boolean useThis = StubUtility.useThisForFieldAccess(project);
    if (argname.equals(fieldName) || (useThis && !isStatic)) {
      if (isStatic) fieldName = parentType.getElementName() + '.' + fieldName;
      else fieldName = "this." + fieldName; // $NON-NLS-1$
    }
    String body =
        CodeGeneration.getSetterMethodBodyContent(
            field.getCompilationUnit(),
            parentType.getTypeQualifiedName('.'),
            setterName,
            fieldName,
            argname,
            lineDelim);
    if (body != null) {
      buf.append(body);
    }
    buf.append("}"); // $NON-NLS-1$
    buf.append(lineDelim);
    return buf.toString();
  }

  /**
   * Create a stub for a getter of the given field using getter/setter templates. The resulting code
   * has to be formatted and indented.
   *
   * @param field The field to create a getter for
   * @param getterName The chosen name for the getter
   * @param addComments If <code>true</code>, comments will be added.
   * @param flags The flags signaling visibility, if static, synchronized or final
   * @return Returns the generated stub.
   * @throws CoreException when stub creation failed
   */
  public static String getGetterStub(
      IField field, String getterName, boolean addComments, int flags) throws CoreException {
    String fieldName = field.getElementName();
    IType parentType = field.getDeclaringType();

    boolean isStatic = Flags.isStatic(flags);
    boolean isSync = Flags.isSynchronized(flags);
    boolean isFinal = Flags.isFinal(flags);

    String typeName = Signature.toString(field.getTypeSignature());
    String accessorName = StubUtility.getBaseName(field);

    String lineDelim =
        "\n"; // Use default line delimiter, as generated stub has to be formatted anyway
    // //$NON-NLS-1$
    StringBuffer buf = new StringBuffer();
    if (addComments) {
      String comment =
          CodeGeneration.getGetterComment(
              field.getCompilationUnit(),
              parentType.getTypeQualifiedName('.'),
              getterName,
              field.getElementName(),
              typeName,
              accessorName,
              lineDelim);
      if (comment != null) {
        buf.append(comment);
        buf.append(lineDelim);
      }
    }

    buf.append(JdtFlags.getVisibilityString(flags));
    buf.append(' ');
    if (isStatic) buf.append("static "); // $NON-NLS-1$
    if (isSync) buf.append("synchronized "); // $NON-NLS-1$
    if (isFinal) buf.append("final "); // $NON-NLS-1$

    buf.append(typeName);
    buf.append(' ');
    buf.append(getterName);
    buf.append("() {"); // $NON-NLS-1$
    buf.append(lineDelim);

    boolean useThis = StubUtility.useThisForFieldAccess(field.getJavaProject());
    if (useThis && !isStatic) {
      fieldName = "this." + fieldName; // $NON-NLS-1$
    }

    String body =
        CodeGeneration.getGetterMethodBodyContent(
            field.getCompilationUnit(),
            parentType.getTypeQualifiedName('.'),
            getterName,
            fieldName,
            lineDelim);
    if (body != null) {
      buf.append(body);
    }
    buf.append("}"); // $NON-NLS-1$
    buf.append(lineDelim);
    return buf.toString();
  }

  /**
   * Converts an assignment, postfix expression or prefix expression into an assignable equivalent
   * expression using the getter.
   *
   * @param node the assignment/prefix/postfix node
   * @param astRewrite the astRewrite to use
   * @param getterExpression the expression to insert for read accesses or <code>null</code> if such
   *     an expression does not exist
   * @param variableType the type of the variable that the result will be assigned to
   * @param is50OrHigher <code>true</code> if a 5.0 or higher environment can be used
   * @return an expression that can be assigned to the type variableType with node being replaced by
   *     a equivalent expression using the getter
   */
  public static Expression getAssignedValue(
      ASTNode node,
      ASTRewrite astRewrite,
      Expression getterExpression,
      ITypeBinding variableType,
      boolean is50OrHigher) {
    InfixExpression.Operator op = null;
    AST ast = astRewrite.getAST();
    if (isNotInBlock(node)) return null;
    if (node.getNodeType() == ASTNode.ASSIGNMENT) {
      Assignment assignment = ((Assignment) node);
      Expression rightHandSide = assignment.getRightHandSide();
      Expression copiedRightOp = (Expression) astRewrite.createCopyTarget(rightHandSide);
      if (assignment.getOperator() == Operator.ASSIGN) {
        ITypeBinding rightHandSideType = rightHandSide.resolveTypeBinding();
        copiedRightOp =
            createNarrowCastIfNessecary(
                copiedRightOp, rightHandSideType, ast, variableType, is50OrHigher);
        return copiedRightOp;
      }
      if (getterExpression != null) {
        InfixExpression infix = ast.newInfixExpression();
        infix.setLeftOperand(getterExpression);
        infix.setOperator(ASTNodes.convertToInfixOperator(assignment.getOperator()));
        ITypeBinding infixType = infix.resolveTypeBinding();
        if (NecessaryParenthesesChecker.needsParenthesesForRightOperand(
            rightHandSide, infix, variableType)) {
          ParenthesizedExpression p = ast.newParenthesizedExpression();
          p.setExpression(copiedRightOp);
          copiedRightOp = p;
        }
        infix.setRightOperand(copiedRightOp);
        return createNarrowCastIfNessecary(infix, infixType, ast, variableType, is50OrHigher);
      }
    } else if (node.getNodeType() == ASTNode.POSTFIX_EXPRESSION) {
      PostfixExpression po = (PostfixExpression) node;
      if (po.getOperator() == PostfixExpression.Operator.INCREMENT)
        op = InfixExpression.Operator.PLUS;
      if (po.getOperator() == PostfixExpression.Operator.DECREMENT)
        op = InfixExpression.Operator.MINUS;
    } else if (node.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
      PrefixExpression pe = (PrefixExpression) node;
      if (pe.getOperator() == PrefixExpression.Operator.INCREMENT)
        op = InfixExpression.Operator.PLUS;
      if (pe.getOperator() == PrefixExpression.Operator.DECREMENT)
        op = InfixExpression.Operator.MINUS;
    }
    if (op != null && getterExpression != null) {
      return createInfixInvocationFromPostPrefixExpression(
          op, getterExpression, ast, variableType, is50OrHigher);
    }
    return null;
  }

  /*
   * Check if the node is in a block. We don't want to update declarations
   */
  private static boolean isNotInBlock(ASTNode parent) {
    ASTNode statement = parent.getParent();
    boolean isStatement = statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT;
    ASTNode block = statement.getParent();
    boolean isBlock =
        block.getNodeType() == ASTNode.BLOCK || block.getNodeType() == ASTNode.SWITCH_STATEMENT;
    boolean isControlStatemenBody =
        ASTNodes.isControlStatementBody(statement.getLocationInParent());
    return isStatement || !(isBlock || isControlStatemenBody);
  }

  private static Expression createInfixInvocationFromPostPrefixExpression(
      InfixExpression.Operator operator,
      Expression getterExpression,
      AST ast,
      ITypeBinding variableType,
      boolean is50OrHigher) {
    InfixExpression infix = ast.newInfixExpression();
    infix.setLeftOperand(getterExpression);
    infix.setOperator(operator);
    NumberLiteral number = ast.newNumberLiteral();
    number.setToken("1"); // $NON-NLS-1$
    infix.setRightOperand(number);
    ITypeBinding infixType = infix.resolveTypeBinding();
    return createNarrowCastIfNessecary(infix, infixType, ast, variableType, is50OrHigher);
  }

  /**
   * Checks if the assignment needs a downcast and inserts it if necessary
   *
   * @param expression the right hand-side
   * @param expressionType the type of the right hand-side. Can be null
   * @param ast the AST
   * @param variableType the Type of the variable the expression will be assigned to
   * @param is50OrHigher if <code>true</code> java 5.0 code will be assumed
   * @return the casted expression if necessary
   */
  private static Expression createNarrowCastIfNessecary(
      Expression expression,
      ITypeBinding expressionType,
      AST ast,
      ITypeBinding variableType,
      boolean is50OrHigher) {
    PrimitiveType castTo = null;
    if (variableType.isEqualTo(expressionType)) return expression; // no cast for same type
    if (is50OrHigher) {
      if (ast.resolveWellKnownType("java.lang.Character").isEqualTo(variableType)) // $NON-NLS-1$
      castTo = ast.newPrimitiveType(PrimitiveType.CHAR);
      if (ast.resolveWellKnownType("java.lang.Byte").isEqualTo(variableType)) // $NON-NLS-1$
      castTo = ast.newPrimitiveType(PrimitiveType.BYTE);
      if (ast.resolveWellKnownType("java.lang.Short").isEqualTo(variableType)) // $NON-NLS-1$
      castTo = ast.newPrimitiveType(PrimitiveType.SHORT);
    }
    if (ast.resolveWellKnownType("char").isEqualTo(variableType)) // $NON-NLS-1$
    castTo = ast.newPrimitiveType(PrimitiveType.CHAR);
    if (ast.resolveWellKnownType("byte").isEqualTo(variableType)) // $NON-NLS-1$
    castTo = ast.newPrimitiveType(PrimitiveType.BYTE);
    if (ast.resolveWellKnownType("short").isEqualTo(variableType)) // $NON-NLS-1$
    castTo = ast.newPrimitiveType(PrimitiveType.SHORT);
    if (castTo != null) {
      CastExpression cast = ast.newCastExpression();
      if (NecessaryParenthesesChecker.needsParentheses(
          expression, cast, CastExpression.EXPRESSION_PROPERTY)) {
        ParenthesizedExpression parenthesized = ast.newParenthesizedExpression();
        parenthesized.setExpression(expression);
        cast.setExpression(parenthesized);
      } else cast.setExpression(expression);
      cast.setType(castTo);
      return cast;
    }
    return expression;
  }
}

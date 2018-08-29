/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Dmitry Stalnov
 * (dstalnov@fusionone.com) - contributed fix for bug "inline method - doesn't handle implicit cast"
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=24941). Dmitry Stalnov
 * (dstalnov@fusionone.com) - contributed fix for bug Encapsulate field can fail when two variables
 * in one variable declaration (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=51540).
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.preferences.MembersOrderPreferenceCache;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

/**
 * JDT-UI-internal helper methods that deal with {@link org.eclipse.jdt.core.dom.ASTNode}s:
 *
 * <ul>
 *   <li>additional operations on {@link org.eclipse.jdt.core.dom.ASTNode}s and subtypes
 *   <li>finding related nodes in an AST
 *   <li>some methods that deal with bindings (new such methods should go into {@link Bindings})
 * </ul>
 */
public class ASTNodes {

  public static final int NODE_ONLY = 0;
  public static final int INCLUDE_FIRST_PARENT = 1;
  public static final int INCLUDE_ALL_PARENTS = 2;

  public static final int WARNING = 1 << 0;
  public static final int ERROR = 1 << 1;
  public static final int PROBLEMS = WARNING | ERROR;

  private static final Message[] EMPTY_MESSAGES = new Message[0];
  private static final IProblem[] EMPTY_PROBLEMS = new IProblem[0];

  private static final int CLEAR_VISIBILITY =
      ~(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE);

  private ASTNodes() {
    // no instance;
  }

  public static String asString(ASTNode node) {
    ASTFlattener flattener = new ASTFlattener();
    node.accept(flattener);
    return flattener.getResult();
  }

  public static String asFormattedString(
      ASTNode node, int indent, String lineDelim, Map<String, String> options) {
    String unformatted = asString(node);
    TextEdit edit = CodeFormatterUtil.format2(node, unformatted, indent, lineDelim, options);
    if (edit != null) {
      Document document = new Document(unformatted);
      try {
        edit.apply(document, TextEdit.NONE);
      } catch (BadLocationException e) {
        JavaPlugin.log(e);
      }
      return document.get();
    }
    return unformatted; // unknown node
  }

  /**
   * Returns the source of the given node from the location where it was parsed.
   *
   * @param node the node to get the source from
   * @param extendedRange if set, the extended ranges of the nodes should ne used
   * @param removeIndent if set, the indentation is removed.
   * @return return the source for the given node or null if accessing the source failed.
   */
  public static String getNodeSource(ASTNode node, boolean extendedRange, boolean removeIndent) {
    ASTNode root = node.getRoot();
    if (root instanceof CompilationUnit) {
      CompilationUnit astRoot = (CompilationUnit) root;
      ITypeRoot typeRoot = astRoot.getTypeRoot();
      try {
        if (typeRoot != null && typeRoot.getBuffer() != null) {
          IBuffer buffer = typeRoot.getBuffer();
          int offset =
              extendedRange ? astRoot.getExtendedStartPosition(node) : node.getStartPosition();
          int length = extendedRange ? astRoot.getExtendedLength(node) : node.getLength();
          String str = buffer.getText(offset, length);
          if (removeIndent) {
            IJavaProject project = typeRoot.getJavaProject();
            int indent = StubUtility.getIndentUsed(buffer, node.getStartPosition(), project);
            str =
                Strings.changeIndent(
                    str, indent, project, new String(), typeRoot.findRecommendedLineSeparator());
          }
          return str;
        }
      } catch (JavaModelException e) {
        // ignore
      }
    }
    return null;
  }

  /**
   * Returns the list that contains the given ASTNode. If the node isn't part of any list, <code>
   * null</code> is returned.
   *
   * @param node the node in question
   * @return the list that contains the node or <code>null</code>
   */
  public static List<? extends ASTNode> getContainingList(ASTNode node) {
    StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
    if (locationInParent != null && locationInParent.isChildListProperty()) {
      return getChildListProperty(node.getParent(), (ChildListPropertyDescriptor) locationInParent);
    }
    return null;
  }

  /**
   * Variant of {@link
   * org.eclipse.jdt.core.dom.ASTNode#getStructuralProperty(org.eclipse.jdt.core.dom.StructuralPropertyDescriptor)}
   * that avoids unchecked casts in the caller.
   *
   * <p>To improve type-safety, callers can add the expected element type as explicit type argument,
   * e.g.:
   *
   * <p>{@code ASTNodes.<BodyDeclaration>getChildListProperty(typeDecl, bodyDeclarationsProperty)}
   *
   * @param node the node
   * @param propertyDescriptor the child list property to get
   * @return the child list
   * @exception RuntimeException if this node does not have the given property
   */
  @SuppressWarnings("unchecked")
  public static <T extends ASTNode> List<T> getChildListProperty(
      ASTNode node, ChildListPropertyDescriptor propertyDescriptor) {
    return (List<T>) node.getStructuralProperty(propertyDescriptor);
  }

  /**
   * Returns a list of the direct children of a node. The siblings are ordered by start offset.
   *
   * @param node the node to get the children for
   * @return the children
   */
  public static List<ASTNode> getChildren(ASTNode node) {
    ChildrenCollector visitor = new ChildrenCollector();
    node.accept(visitor);
    return visitor.result;
  }

  private static class ChildrenCollector extends GenericVisitor {
    public List<ASTNode> result;

    public ChildrenCollector() {
      super(true);
      result = null;
    }

    @Override
    protected boolean visitNode(ASTNode node) {
      if (result == null) { // first visitNode: on the node's parent: do nothing, return true
        result = new ArrayList<ASTNode>();
        return true;
      }
      result.add(node);
      return false;
    }
  }

  /**
   * Returns true if this is an existing node, i.e. it was created as part of a parsing process of a
   * source code file. Returns false if this is a newly created node which has not yet been given a
   * source position.
   *
   * @param node the node to be tested.
   * @return true if this is an existing node, false if not.
   */
  public static boolean isExistingNode(ASTNode node) {
    return node.getStartPosition() != -1;
  }

  /**
   * Returns the element type. This is a convenience method that returns its argument if it is a
   * simple type and the element type if the parameter is an array type.
   *
   * @param type The type to get the element type from.
   * @return The element type of the type or the type itself.
   */
  public static Type getElementType(Type type) {
    if (!type.isArrayType()) return type;
    return ((ArrayType) type).getElementType();
  }

  public static ASTNode findDeclaration(IBinding binding, ASTNode root) {
    root = root.getRoot();
    if (root instanceof CompilationUnit) {
      return ((CompilationUnit) root).findDeclaringNode(binding);
    }
    return null;
  }

  public static VariableDeclaration findVariableDeclaration(
      IVariableBinding binding, ASTNode root) {
    if (binding.isField()) return null;
    ASTNode result = findDeclaration(binding, root);
    if (result instanceof VariableDeclaration) return (VariableDeclaration) result;

    return null;
  }

  /**
   * Returns the type node for the given declaration.
   *
   * @param declaration the declaration
   * @return the type node or <code>null</code> if the given declaration represents a type inferred
   *     parameter in lambda expression
   */
  public static Type getType(VariableDeclaration declaration) {
    if (declaration instanceof SingleVariableDeclaration) {
      return ((SingleVariableDeclaration) declaration).getType();
    } else if (declaration instanceof VariableDeclarationFragment) {
      ASTNode parent = ((VariableDeclarationFragment) declaration).getParent();
      if (parent instanceof VariableDeclarationExpression)
        return ((VariableDeclarationExpression) parent).getType();
      else if (parent instanceof VariableDeclarationStatement)
        return ((VariableDeclarationStatement) parent).getType();
      else if (parent instanceof FieldDeclaration) return ((FieldDeclaration) parent).getType();
      else if (parent instanceof LambdaExpression) return null;
    }
    Assert.isTrue(false, "Unknown VariableDeclaration"); // $NON-NLS-1$
    return null;
  }

  public static int getDimensions(VariableDeclaration declaration) {
    int dim = declaration.getExtraDimensions();
    if (declaration instanceof VariableDeclarationFragment
        && declaration.getParent() instanceof LambdaExpression) {
      LambdaExpression lambda = (LambdaExpression) declaration.getParent();
      IMethodBinding methodBinding = lambda.resolveMethodBinding();
      if (methodBinding != null) {
        ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
        int index = lambda.parameters().indexOf(declaration);
        ITypeBinding typeBinding = parameterTypes[index];
        return typeBinding.getDimensions();
      }
    } else {
      Type type = getType(declaration);
      if (type instanceof ArrayType) {
        dim += ((ArrayType) type).getDimensions();
      }
    }
    return dim;
  }

  public static List<IExtendedModifier> getModifiers(VariableDeclaration declaration) {
    Assert.isNotNull(declaration);
    if (declaration instanceof SingleVariableDeclaration) {
      return ((SingleVariableDeclaration) declaration).modifiers();
    } else if (declaration instanceof VariableDeclarationFragment) {
      ASTNode parent = declaration.getParent();
      if (parent instanceof VariableDeclarationExpression)
        return ((VariableDeclarationExpression) parent).modifiers();
      else if (parent instanceof VariableDeclarationStatement)
        return ((VariableDeclarationStatement) parent).modifiers();
    }
    return new ArrayList<IExtendedModifier>(0);
  }

  public static boolean isSingleDeclaration(VariableDeclaration declaration) {
    Assert.isNotNull(declaration);
    if (declaration instanceof SingleVariableDeclaration) {
      return true;
    } else if (declaration instanceof VariableDeclarationFragment) {
      ASTNode parent = declaration.getParent();
      if (parent instanceof VariableDeclarationExpression)
        return ((VariableDeclarationExpression) parent).fragments().size() == 1;
      else if (parent instanceof VariableDeclarationStatement)
        return ((VariableDeclarationStatement) parent).fragments().size() == 1;
    }
    return false;
  }

  public static boolean isLiteral(Expression expression) {
    int type = expression.getNodeType();
    return type == ASTNode.BOOLEAN_LITERAL
        || type == ASTNode.CHARACTER_LITERAL
        || type == ASTNode.NULL_LITERAL
        || type == ASTNode.NUMBER_LITERAL
        || type == ASTNode.STRING_LITERAL
        || type == ASTNode.TYPE_LITERAL;
  }

  public static boolean isLabel(SimpleName name) {
    int parentType = name.getParent().getNodeType();
    return parentType == ASTNode.LABELED_STATEMENT
        || parentType == ASTNode.BREAK_STATEMENT
        || parentType != ASTNode.CONTINUE_STATEMENT;
  }

  public static boolean isStatic(BodyDeclaration declaration) {
    return Modifier.isStatic(declaration.getModifiers());
  }

  public static List<BodyDeclaration> getBodyDeclarations(ASTNode node) {
    if (node instanceof AbstractTypeDeclaration) {
      return ((AbstractTypeDeclaration) node).bodyDeclarations();
    } else if (node instanceof AnonymousClassDeclaration) {
      return ((AnonymousClassDeclaration) node).bodyDeclarations();
    }
    // should not happen.
    Assert.isTrue(false);
    return null;
  }

  /**
   * Returns the structural property descriptor for the "bodyDeclarations" property of this node
   * (element type: {@link org.eclipse.jdt.core.dom.BodyDeclaration}).
   *
   * @param node the node, either an {@link org.eclipse.jdt.core.dom.AbstractTypeDeclaration} or an
   *     {@link org.eclipse.jdt.core.dom .AnonymousClassDeclaration}
   * @return the property descriptor
   */
  public static ChildListPropertyDescriptor getBodyDeclarationsProperty(ASTNode node) {
    if (node instanceof AbstractTypeDeclaration) {
      return ((AbstractTypeDeclaration) node).getBodyDeclarationsProperty();
    } else if (node instanceof AnonymousClassDeclaration) {
      return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
    }
    // should not happen.
    Assert.isTrue(false);
    return null;
  }

  /**
   * Returns the simple name of the type, followed by array dimensions. Skips qualifiers, type
   * arguments, and type annotations.
   *
   * <p>Does <b>not</b> work for WildcardTypes, etc.!
   *
   * @param type a type that has a simple name
   * @return the simple name, followed by array dimensions
   * @see #getSimpleNameIdentifier(org.eclipse.jdt.core.dom.Name)
   * @since 3.10
   */
  public static String getTypeName(Type type) {
    final StringBuffer buffer = new StringBuffer();
    ASTVisitor visitor =
        new ASTVisitor() {
          @Override
          public boolean visit(PrimitiveType node) {
            buffer.append(node.getPrimitiveTypeCode().toString());
            return false;
          }

          @Override
          public boolean visit(SimpleType node) {
            buffer.append(getSimpleNameIdentifier(node.getName()));
            return false;
          }

          @Override
          public boolean visit(QualifiedType node) {
            buffer.append(node.getName().getIdentifier());
            return false;
          }

          @Override
          public boolean visit(NameQualifiedType node) {
            buffer.append(node.getName().getIdentifier());
            return false;
          }

          @Override
          public boolean visit(ParameterizedType node) {
            node.getType().accept(this);
            return false;
          }

          @Override
          public void endVisit(ArrayType node) {
            for (int i = 0; i < node.dimensions().size(); i++) {
              buffer.append("[]"); // $NON-NLS-1$
            }
          }
        };
    type.accept(visitor);
    return buffer.toString();
  }

  /**
   * Returns the (potentially qualified) name of a type, followed by array dimensions. Skips type
   * arguments and type annotations.
   *
   * @param type a type that has a name
   * @return the name, followed by array dimensions
   * @since 3.10
   */
  public static String getQualifiedTypeName(Type type) {
    final StringBuffer buffer = new StringBuffer();
    ASTVisitor visitor =
        new ASTVisitor() {
          @Override
          public boolean visit(SimpleType node) {
            buffer.append(node.getName().getFullyQualifiedName());
            return false;
          }

          @Override
          public boolean visit(QualifiedType node) {
            node.getQualifier().accept(this);
            buffer.append('.');
            buffer.append(node.getName().getIdentifier());
            return false;
          }

          @Override
          public boolean visit(NameQualifiedType node) {
            buffer.append(node.getQualifier().getFullyQualifiedName());
            buffer.append('.');
            buffer.append(node.getName().getIdentifier());
            return false;
          }

          @Override
          public boolean visit(ParameterizedType node) {
            node.getType().accept(this);
            return false;
          }

          @Override
          public void endVisit(ArrayType node) {
            for (int i = 0; i < node.dimensions().size(); i++) {
              buffer.append("[]"); // $NON-NLS-1$
            }
          }
        };
    type.accept(visitor);
    return buffer.toString();
  }
  //
  public static InfixExpression.Operator convertToInfixOperator(Assignment.Operator operator) {
    if (operator.equals(Assignment.Operator.PLUS_ASSIGN)) return InfixExpression.Operator.PLUS;

    if (operator.equals(Assignment.Operator.MINUS_ASSIGN)) return InfixExpression.Operator.MINUS;

    if (operator.equals(Assignment.Operator.TIMES_ASSIGN)) return InfixExpression.Operator.TIMES;

    if (operator.equals(Assignment.Operator.DIVIDE_ASSIGN)) return InfixExpression.Operator.DIVIDE;

    if (operator.equals(Assignment.Operator.BIT_AND_ASSIGN)) return InfixExpression.Operator.AND;

    if (operator.equals(Assignment.Operator.BIT_OR_ASSIGN)) return InfixExpression.Operator.OR;

    if (operator.equals(Assignment.Operator.BIT_XOR_ASSIGN)) return InfixExpression.Operator.XOR;

    if (operator.equals(Assignment.Operator.REMAINDER_ASSIGN))
      return InfixExpression.Operator.REMAINDER;

    if (operator.equals(Assignment.Operator.LEFT_SHIFT_ASSIGN))
      return InfixExpression.Operator.LEFT_SHIFT;

    if (operator.equals(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN))
      return InfixExpression.Operator.RIGHT_SHIFT_SIGNED;

    if (operator.equals(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN))
      return InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED;

    Assert.isTrue(false, "Cannot convert assignment operator"); // $NON-NLS-1$
    return null;
  }

  /**
   * Returns true if a node at a given location is a body of a control statement. Such body nodes
   * are interesting as when replacing them, it has to be evaluates if a Block is needed instead.
   * E.g. <code> if (x) do(); -> if (x) { do1(); do2() } </code>
   *
   * @param locationInParent Location of the body node
   * @return Returns true if the location is a body node location of a control statement.
   */
  public static boolean isControlStatementBody(StructuralPropertyDescriptor locationInParent) {
    return locationInParent == IfStatement.THEN_STATEMENT_PROPERTY
        || locationInParent == IfStatement.ELSE_STATEMENT_PROPERTY
        || locationInParent == ForStatement.BODY_PROPERTY
        || locationInParent == EnhancedForStatement.BODY_PROPERTY
        || locationInParent == WhileStatement.BODY_PROPERTY
        || locationInParent == DoStatement.BODY_PROPERTY;
  }

  /**
   * Returns the type to which an inlined variable initializer should be cast, or <code>null</code>
   * if no cast is necessary.
   *
   * @param initializer the initializer expression of the variable to inline
   * @param reference the reference to the variable (which is to be inlined)
   * @return a type binding to which the initializer should be cast, or <code>null</code> iff no
   *     cast is necessary
   * @since 3.6
   */
  public static ITypeBinding getExplicitCast(Expression initializer, Expression reference) {
    ITypeBinding initializerType = initializer.resolveTypeBinding();
    ITypeBinding referenceType = reference.resolveTypeBinding();
    if (initializerType == null || referenceType == null) return null;

    if (initializerType.isPrimitive()
        && referenceType.isPrimitive()
        && !referenceType.isEqualTo(initializerType)) {
      return referenceType;

    } else if (initializerType.isPrimitive()
        && !referenceType.isPrimitive()) { // initializer is autoboxed
      ITypeBinding unboxedReferenceType =
          Bindings.getUnboxedTypeBinding(referenceType, reference.getAST());
      if (!unboxedReferenceType.isEqualTo(initializerType)) return unboxedReferenceType;
      else if (needsExplicitBoxing(reference)) return referenceType;

    } else if (!initializerType.isPrimitive()
        && referenceType.isPrimitive()) { // initializer is autounboxed
      ITypeBinding unboxedInitializerType =
          Bindings.getUnboxedTypeBinding(initializerType, reference.getAST());
      if (!unboxedInitializerType.isEqualTo(referenceType)) return referenceType;

    } else if (initializerType.isRawType() && referenceType.isParameterizedType()) {
      return referenceType; // don't lose the unchecked conversion

    } else if (initializer instanceof LambdaExpression || initializer instanceof MethodReference) {
      if (isTargetAmbiguous(reference, isExplicitlyTypedLambda(initializer))) {
        return referenceType;
      } else {
        ITypeBinding targetType = getTargetType(reference);
        if (targetType == null || targetType != referenceType) {
          return referenceType;
        }
      }

    } else if (!TypeRules.canAssign(initializerType, referenceType)) {
      if (!Bindings.containsTypeVariables(referenceType)) return referenceType;
    }

    return null;
  }

  /**
   * Checks whether overloaded methods can result in an ambiguous method call or a semantic change
   * when the <code>expression</code> argument is replaced with a poly expression form of the
   * functional interface instance.
   *
   * @param expression the method argument, which is a functional interface instance
   * @param expressionIsExplicitlyTyped <code>true</code> iff the intended replacement for <code>
   *     expression</code> is an explicitly typed lambda expression (JLS8 15.27.1)
   * @return <code>true</code> if overloaded methods can result in an ambiguous method call or a
   *     semantic change, <code>false</code> otherwise
   * @since 3.10
   */
  public static boolean isTargetAmbiguous(
      Expression expression, boolean expressionIsExplicitlyTyped) {
    StructuralPropertyDescriptor locationInParent = expression.getLocationInParent();

    while (locationInParent == ParenthesizedExpression.EXPRESSION_PROPERTY
        || locationInParent == ConditionalExpression.THEN_EXPRESSION_PROPERTY
        || locationInParent == ConditionalExpression.ELSE_EXPRESSION_PROPERTY) {
      expression = (Expression) expression.getParent();
      locationInParent = expression.getLocationInParent();
    }

    ASTNode parent = expression.getParent();
    IMethodBinding methodBinding;
    int argumentIndex;
    int argumentCount;
    Expression invocationQualifier = null;
    if (locationInParent == MethodInvocation.ARGUMENTS_PROPERTY) {
      MethodInvocation methodInvocation = (MethodInvocation) parent;
      methodBinding = methodInvocation.resolveMethodBinding();
      argumentIndex = methodInvocation.arguments().indexOf(expression);
      argumentCount = methodInvocation.arguments().size();
      invocationQualifier = methodInvocation.getExpression();
    } else if (locationInParent == SuperMethodInvocation.ARGUMENTS_PROPERTY) {
      SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) parent;
      methodBinding = superMethodInvocation.resolveMethodBinding();
      argumentIndex = superMethodInvocation.arguments().indexOf(expression);
      argumentCount = superMethodInvocation.arguments().size();
      invocationQualifier = superMethodInvocation.getQualifier();
    } else if (locationInParent == ConstructorInvocation.ARGUMENTS_PROPERTY) {
      ConstructorInvocation constructorInvocation = (ConstructorInvocation) parent;
      methodBinding = constructorInvocation.resolveConstructorBinding();
      argumentIndex = constructorInvocation.arguments().indexOf(expression);
      argumentCount = constructorInvocation.arguments().size();
    } else if (locationInParent == SuperConstructorInvocation.ARGUMENTS_PROPERTY) {
      SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) parent;
      methodBinding = superConstructorInvocation.resolveConstructorBinding();
      argumentIndex = superConstructorInvocation.arguments().indexOf(expression);
      argumentCount = superConstructorInvocation.arguments().size();
    } else if (locationInParent == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
      ClassInstanceCreation creation = (ClassInstanceCreation) parent;
      methodBinding = creation.resolveConstructorBinding();
      argumentIndex = creation.arguments().indexOf(expression);
      argumentCount = creation.arguments().size();
    } else if (locationInParent == EnumConstantDeclaration.ARGUMENTS_PROPERTY) {
      EnumConstantDeclaration enumConstantDecl = (EnumConstantDeclaration) parent;
      methodBinding = enumConstantDecl.resolveConstructorBinding();
      argumentIndex = enumConstantDecl.arguments().indexOf(expression);
      argumentCount = enumConstantDecl.arguments().size();
    } else {
      return false;
    }

    if (methodBinding != null) {
      ITypeBinding invocationTargetType;
      if (parent instanceof MethodInvocation || parent instanceof SuperMethodInvocation) {
        if (invocationQualifier != null) {
          invocationTargetType = invocationQualifier.resolveTypeBinding();
          if (invocationTargetType != null && parent instanceof SuperMethodInvocation) {
            invocationTargetType = invocationTargetType.getSuperclass();
          }
        } else {
          ITypeBinding enclosingType = getEnclosingType(parent);
          if (enclosingType != null && parent instanceof SuperMethodInvocation) {
            enclosingType = enclosingType.getSuperclass();
          }
          if (enclosingType != null) {
            IMethodBinding methodInHierarchy =
                Bindings.findMethodInHierarchy(
                    enclosingType, methodBinding.getName(), methodBinding.getParameterTypes());
            if (methodInHierarchy != null) {
              invocationTargetType = enclosingType;
            } else {
              invocationTargetType = methodBinding.getDeclaringClass();
            }
          } else {
            // not expected
            invocationTargetType = methodBinding.getDeclaringClass();
          }
        }
      } else {
        invocationTargetType = methodBinding.getDeclaringClass();
      }
      if (invocationTargetType != null) {
        TypeBindingVisitor visitor =
            new AmbiguousTargetMethodAnalyzer(
                invocationTargetType,
                methodBinding,
                argumentIndex,
                argumentCount,
                expressionIsExplicitlyTyped);
        return !(visitor.visit(invocationTargetType)
            && Bindings.visitHierarchy(invocationTargetType, visitor));
      }
    }

    return true;
  }

  private static class AmbiguousTargetMethodAnalyzer implements TypeBindingVisitor {
    private ITypeBinding fDeclaringType;
    private IMethodBinding fOriginalMethod;
    private int fArgIndex;
    private int fArgumentCount;
    private boolean fExpressionIsExplicitlyTyped;

    /**
     * @param declaringType the type binding declaring the <code>originalMethod</code>
     * @param originalMethod the method declaration binding corresponding to the method call
     * @param argumentIndex the index of the functional interface instance argument in the method
     *     call
     * @param argumentCount the number of arguments in the method call
     * @param expressionIsExplicitlyTyped <code>true</code> iff the intended replacement for <code>
     *     expression</code> is an explicitly typed lambda expression (JLS8 15.27.1)
     */
    public AmbiguousTargetMethodAnalyzer(
        ITypeBinding declaringType,
        IMethodBinding originalMethod,
        int argumentIndex,
        int argumentCount,
        boolean expressionIsExplicitlyTyped) {
      fDeclaringType = declaringType;
      fOriginalMethod = originalMethod;
      fArgIndex = argumentIndex;
      fArgumentCount = argumentCount;
      fExpressionIsExplicitlyTyped = expressionIsExplicitlyTyped;
    }

    public boolean visit(ITypeBinding type) {
      IMethodBinding[] methods = type.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++) {
        IMethodBinding candidate = methods[i];
        if (candidate.getMethodDeclaration() == fOriginalMethod.getMethodDeclaration()) {
          continue;
        }
        ITypeBinding candidateDeclaringType = candidate.getDeclaringClass();
        if (fDeclaringType != candidateDeclaringType) {
          int modifiers = candidate.getModifiers();
          if (candidateDeclaringType.isInterface() && Modifier.isStatic(modifiers)) {
            continue;
          }
          if (Modifier.isPrivate(modifiers)) {
            continue;
          }
        }
        if (fOriginalMethod.getName().equals(candidate.getName())
            && !fOriginalMethod.overrides(candidate)) {
          ITypeBinding[] originalParameterTypes = fOriginalMethod.getParameterTypes();
          ITypeBinding[] candidateParameterTypes = candidate.getParameterTypes();

          boolean couldBeAmbiguous;
          if (originalParameterTypes.length == candidateParameterTypes.length) {
            couldBeAmbiguous = true;
          } else if (fOriginalMethod.isVarargs() || candidate.isVarargs()) {
            int candidateMinArgumentCount = candidateParameterTypes.length;
            if (candidate.isVarargs()) candidateMinArgumentCount--;
            couldBeAmbiguous = fArgumentCount >= candidateMinArgumentCount;
          } else {
            couldBeAmbiguous = false;
          }
          if (couldBeAmbiguous) {
            ITypeBinding parameterType = ASTResolving.getParameterTypeBinding(candidate, fArgIndex);
            if (parameterType != null && parameterType.getFunctionalInterfaceMethod() != null) {
              if (!fExpressionIsExplicitlyTyped) {
                /* According to JLS8 15.12.2.2, implicitly typed lambda expressions are not "pertinent to applicability"
                 * and hence potentially applicable methods are always "applicable by strict invocation",
                 * regardless of whether argument expressions are compatible with the method's parameter types or not.
                 * If there are multiple such methods, 15.12.2.5 results in an ambiguous method invocation.
                 */
                return false;
              }
              /* Explicitly typed lambda expressions are pertinent to applicability, and hence
               * compatibility with the corresponding method parameter type is checked. And since this check
               * separates functional interface methods by their void-compatibility state, functional interfaces
               * with a different void compatibility are not applicable any more and hence can't cause
               * an ambiguous method invocation.
               */
              ITypeBinding origParamType =
                  ASTResolving.getParameterTypeBinding(fOriginalMethod, fArgIndex);
              boolean originalIsVoidCompatible =
                  Bindings.isVoidType(origParamType.getFunctionalInterfaceMethod().getReturnType());
              boolean candidateIsVoidCompatible =
                  Bindings.isVoidType(parameterType.getFunctionalInterfaceMethod().getReturnType());
              if (originalIsVoidCompatible == candidateIsVoidCompatible) {
                return false;
              }
            }
          }
        }
      }
      return true;
    }
  }

  /**
   * Derives the target type defined at the location of the given expression if the target context
   * supports poly expressions.
   *
   * @param expression the expression at whose location the target type is required
   * @return the type binding of the target type defined at the location of the given expression if
   *     the target context supports poly expressions, or <code>null</code> if the target type could
   *     not be derived
   * @since 3.10
   */
  public static ITypeBinding getTargetType(Expression expression) {
    ASTNode parent = expression.getParent();
    StructuralPropertyDescriptor locationInParent = expression.getLocationInParent();

    if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY
        || locationInParent == SingleVariableDeclaration.INITIALIZER_PROPERTY) {
      return ((VariableDeclaration) parent).getName().resolveTypeBinding();

    } else if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
      return ((Assignment) parent).getLeftHandSide().resolveTypeBinding();

    } else if (locationInParent == ReturnStatement.EXPRESSION_PROPERTY) {
      return getTargetTypeForReturnStmt((ReturnStatement) parent);

    } else if (locationInParent == ArrayInitializer.EXPRESSIONS_PROPERTY) {
      return getTargetTypeForArrayInitializer((ArrayInitializer) parent);

    } else if (locationInParent == MethodInvocation.ARGUMENTS_PROPERTY) {
      MethodInvocation methodInvocation = (MethodInvocation) parent;
      IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
      if (methodBinding != null) {
        return getParameterTypeBinding(expression, methodInvocation.arguments(), methodBinding);
      }

    } else if (locationInParent == SuperMethodInvocation.ARGUMENTS_PROPERTY) {
      SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) parent;
      IMethodBinding superMethodBinding = superMethodInvocation.resolveMethodBinding();
      if (superMethodBinding != null) {
        return getParameterTypeBinding(
            expression, superMethodInvocation.arguments(), superMethodBinding);
      }

    } else if (locationInParent == ConstructorInvocation.ARGUMENTS_PROPERTY) {
      ConstructorInvocation constructorInvocation = (ConstructorInvocation) parent;
      IMethodBinding constructorBinding = constructorInvocation.resolveConstructorBinding();
      if (constructorBinding != null) {
        return getParameterTypeBinding(
            expression, constructorInvocation.arguments(), constructorBinding);
      }

    } else if (locationInParent == SuperConstructorInvocation.ARGUMENTS_PROPERTY) {
      SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) parent;
      IMethodBinding superConstructorBinding =
          superConstructorInvocation.resolveConstructorBinding();
      if (superConstructorBinding != null) {
        return getParameterTypeBinding(
            expression, superConstructorInvocation.arguments(), superConstructorBinding);
      }

    } else if (locationInParent == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
      ClassInstanceCreation creation = (ClassInstanceCreation) parent;
      IMethodBinding creationBinding = creation.resolveConstructorBinding();
      if (creationBinding != null) {
        return getParameterTypeBinding(expression, creation.arguments(), creationBinding);
      }

    } else if (locationInParent == EnumConstantDeclaration.ARGUMENTS_PROPERTY) {
      EnumConstantDeclaration enumConstantDecl = (EnumConstantDeclaration) parent;
      IMethodBinding enumConstructorBinding = enumConstantDecl.resolveConstructorBinding();
      if (enumConstructorBinding != null) {
        return getParameterTypeBinding(
            expression, enumConstantDecl.arguments(), enumConstructorBinding);
      }

    } else if (locationInParent == LambdaExpression.BODY_PROPERTY) {
      IMethodBinding methodBinding = ((LambdaExpression) parent).resolveMethodBinding();
      if (methodBinding != null) {
        return methodBinding.getReturnType();
      }

    } else if (locationInParent == ConditionalExpression.THEN_EXPRESSION_PROPERTY
        || locationInParent == ConditionalExpression.ELSE_EXPRESSION_PROPERTY) {
      return getTargetType((ConditionalExpression) parent);

    } else if (locationInParent == CastExpression.EXPRESSION_PROPERTY) {
      return ((CastExpression) parent).getType().resolveBinding();

    } else if (locationInParent == ParenthesizedExpression.EXPRESSION_PROPERTY) {
      return getTargetType((ParenthesizedExpression) parent);
    }
    return null;
  }

  private static ITypeBinding getParameterTypeBinding(
      Expression expression, List<Expression> arguments, IMethodBinding methodBinding) {
    int index = arguments.indexOf(expression);
    return ASTResolving.getParameterTypeBinding(methodBinding, index);
  }

  private static ITypeBinding getTargetTypeForArrayInitializer(ArrayInitializer arrayInitializer) {
    ASTNode initializerParent = arrayInitializer.getParent();
    while (initializerParent instanceof ArrayInitializer) {
      initializerParent = initializerParent.getParent();
    }
    if (initializerParent instanceof ArrayCreation) {
      return ((ArrayCreation) initializerParent).getType().getElementType().resolveBinding();
    } else if (initializerParent instanceof VariableDeclaration) {
      ITypeBinding typeBinding =
          ((VariableDeclaration) initializerParent).getName().resolveTypeBinding();
      if (typeBinding != null) {
        return typeBinding.getElementType();
      }
    }
    return null;
  }

  private static ITypeBinding getTargetTypeForReturnStmt(ReturnStatement returnStmt) {
    LambdaExpression enclosingLambdaExpr = ASTResolving.findEnclosingLambdaExpression(returnStmt);
    if (enclosingLambdaExpr != null) {
      IMethodBinding methodBinding = enclosingLambdaExpr.resolveMethodBinding();
      return methodBinding == null ? null : methodBinding.getReturnType();
    }
    MethodDeclaration enclosingMethodDecl = ASTResolving.findParentMethodDeclaration(returnStmt);
    if (enclosingMethodDecl != null) {
      IMethodBinding methodBinding = enclosingMethodDecl.resolveBinding();
      return methodBinding == null ? null : methodBinding.getReturnType();
    }
    return null;
  }

  /**
   * Returns whether an expression at the given location needs explicit boxing.
   *
   * @param expression the expression
   * @return <code>true</code> iff an expression at the given location needs explicit boxing
   * @since 3.6
   */
  private static boolean needsExplicitBoxing(Expression expression) {
    StructuralPropertyDescriptor locationInParent = expression.getLocationInParent();
    if (locationInParent == ParenthesizedExpression.EXPRESSION_PROPERTY)
      return needsExplicitBoxing((ParenthesizedExpression) expression.getParent());

    if (locationInParent == ClassInstanceCreation.EXPRESSION_PROPERTY
        || locationInParent == FieldAccess.EXPRESSION_PROPERTY
        || locationInParent == MethodInvocation.EXPRESSION_PROPERTY) return true;

    return false;
  }

  private static boolean isExplicitlyTypedLambda(Expression expression) {
    if (!(expression instanceof LambdaExpression)) return false;
    LambdaExpression lambda = (LambdaExpression) expression;
    List<VariableDeclaration> parameters = lambda.parameters();
    if (parameters.isEmpty()) return true;
    return parameters.get(0) instanceof SingleVariableDeclaration;
  }

  /**
   * Returns the closest ancestor of <code>node</code> that is an instance of <code>parentClass
   * </code>, or <code>null</code> if none.
   *
   * <p><b>Warning:</b> This method does not stop at any boundaries like parentheses, statements,
   * body declarations, etc. The resulting node may be in a totally different scope than the given
   * node. Consider using one of the {@link ASTResolving}<code>.find(..)</code> methods instead.
   *
   * @param node the node
   * @param parentClass the class of the sought ancestor node
   * @return the closest ancestor of <code>node</code> that is an instance of <code>parentClass
   *     </code>, or <code>null</code> if none
   */
  public static ASTNode getParent(ASTNode node, Class<? extends ASTNode> parentClass) {
    do {
      node = node.getParent();
    } while (node != null && !parentClass.isInstance(node));
    return node;
  }

  /**
   * Returns the closest ancestor of <code>node</code> whose type is <code>nodeType</code>, or
   * <code>null</code> if none.
   *
   * <p><b>Warning:</b> This method does not stop at any boundaries like parentheses, statements,
   * body declarations, etc. The resulting node may be in a totally different scope than the given
   * node. Consider using one of the {@link ASTResolving}<code>.find(..)</code> methods instead.
   *
   * @param node the node
   * @param nodeType the node type constant from {@link org.eclipse.jdt.core.dom.ASTNode}
   * @return the closest ancestor of <code>node</code> whose type is <code>nodeType</code>, or
   *     <code>null</code> if none
   */
  public static ASTNode getParent(ASTNode node, int nodeType) {
    do {
      node = node.getParent();
    } while (node != null && node.getNodeType() != nodeType);
    return node;
  }
  //
  //    public static ASTNode findParent(ASTNode node, StructuralPropertyDescriptor[][] pathes) {
  //        for (int p = 0; p < pathes.length; p++) {
  //            StructuralPropertyDescriptor[] path = pathes[p];
  //            ASTNode current = node;
  //            int d = path.length - 1;
  //            for (; d >= 0 && current != null; d--) {
  //                StructuralPropertyDescriptor descriptor = path[d];
  //                if (!descriptor.equals(current.getLocationInParent()))
  //                    break;
  //                current = current.getParent();
  //            }
  //            if (d < 0)
  //                return current;
  //        }
  //        return null;
  //    }

  /**
   * For {@link org.eclipse.jdt.core.dom.Name} or {@link org.eclipse.jdt.core.dom.Type} nodes,
   * returns the topmost {@link org.eclipse .jdt.core.dom.Type} node that shares the same type
   * binding as the given node.
   *
   * @param node an ASTNode
   * @return the normalized {@link org.eclipse.jdt.core.dom.Type} node or the original node
   */
  public static ASTNode getNormalizedNode(ASTNode node) {
    ASTNode current = node;
    // normalize name
    if (QualifiedName.NAME_PROPERTY.equals(current.getLocationInParent())) {
      current = current.getParent();
    }
    // normalize type
    if (QualifiedType.NAME_PROPERTY.equals(current.getLocationInParent())
        || SimpleType.NAME_PROPERTY.equals(current.getLocationInParent())
        || NameQualifiedType.NAME_PROPERTY.equals(current.getLocationInParent())) {
      current = current.getParent();
    }
    // normalize parameterized types
    if (ParameterizedType.TYPE_PROPERTY.equals(current.getLocationInParent())) {
      current = current.getParent();
    }
    return current;
  }

  /**
   * Returns <code>true</code> iff <code>parent</code> is a true ancestor of <code>node</code> (i.e.
   * returns <code>false</code> if <code>parent == node</code>).
   *
   * @param node node to test
   * @param parent assumed parent
   * @return <code>true</code> iff <code>parent</code> is a true ancestor of <code>node</code>
   */
  public static boolean isParent(ASTNode node, ASTNode parent) {
    Assert.isNotNull(parent);
    do {
      node = node.getParent();
      if (node == parent) return true;
    } while (node != null);
    return false;
  }

  public static int getExclusiveEnd(ASTNode node) {
    return node.getStartPosition() + node.getLength();
  }

  public static int getInclusiveEnd(ASTNode node) {
    return node.getStartPosition() + node.getLength() - 1;
  }

  public static IMethodBinding getMethodBinding(Name node) {
    IBinding binding = node.resolveBinding();
    if (binding instanceof IMethodBinding) return (IMethodBinding) binding;
    return null;
  }

  public static IVariableBinding getVariableBinding(Name node) {
    IBinding binding = node.resolveBinding();
    if (binding instanceof IVariableBinding) return (IVariableBinding) binding;
    return null;
  }

  public static IVariableBinding getLocalVariableBinding(Name node) {
    IVariableBinding result = getVariableBinding(node);
    if (result == null || result.isField()) return null;

    return result;
  }

  public static IVariableBinding getFieldBinding(Name node) {
    IVariableBinding result = getVariableBinding(node);
    if (result == null || !result.isField()) return null;

    return result;
  }

  public static ITypeBinding getTypeBinding(Name node) {
    IBinding binding = node.resolveBinding();
    if (binding instanceof ITypeBinding) return (ITypeBinding) binding;
    return null;
  }

  /**
   * Returns the receiver's type binding of the given method invocation.
   *
   * @param invocation method invocation to resolve type of
   * @return the type binding of the receiver
   */
  public static ITypeBinding getReceiverTypeBinding(MethodInvocation invocation) {
    ITypeBinding result = null;
    Expression exp = invocation.getExpression();
    if (exp != null) {
      return exp.resolveTypeBinding();
    } else {
      AbstractTypeDeclaration type =
          (AbstractTypeDeclaration) getParent(invocation, AbstractTypeDeclaration.class);
      if (type != null) return type.resolveBinding();
    }
    return result;
  }

  public static ITypeBinding getEnclosingType(ASTNode node) {
    while (node != null) {
      if (node instanceof AbstractTypeDeclaration) {
        return ((AbstractTypeDeclaration) node).resolveBinding();
      } else if (node instanceof AnonymousClassDeclaration) {
        return ((AnonymousClassDeclaration) node).resolveBinding();
      }
      node = node.getParent();
    }
    return null;
  }

  public static IProblem[] getProblems(ASTNode node, int scope, int severity) {
    ASTNode root = node.getRoot();
    if (!(root instanceof CompilationUnit)) return EMPTY_PROBLEMS;
    IProblem[] problems = ((CompilationUnit) root).getProblems();
    if (root == node) return problems;
    final int iterations = computeIterations(scope);
    List<IProblem> result = new ArrayList<IProblem>(5);
    for (int i = 0; i < problems.length; i++) {
      IProblem problem = problems[i];
      boolean consider = false;
      if ((severity & PROBLEMS) == PROBLEMS) consider = true;
      else if ((severity & WARNING) != 0) consider = problem.isWarning();
      else if ((severity & ERROR) != 0) consider = problem.isError();
      if (consider) {
        ASTNode temp = node;
        int count = iterations;
        do {
          int nodeOffset = temp.getStartPosition();
          int problemOffset = problem.getSourceStart();
          if (nodeOffset <= problemOffset && problemOffset < nodeOffset + temp.getLength()) {
            result.add(problem);
            count = 0;
          } else {
            count--;
          }
        } while ((temp = temp.getParent()) != null && count > 0);
      }
    }
    return result.toArray(new IProblem[result.size()]);
  }

  public static Message[] getMessages(ASTNode node, int flags) {
    ASTNode root = node.getRoot();
    if (!(root instanceof CompilationUnit)) return EMPTY_MESSAGES;
    Message[] messages = ((CompilationUnit) root).getMessages();
    if (root == node) return messages;
    final int iterations = computeIterations(flags);
    List<Message> result = new ArrayList<Message>(5);
    for (int i = 0; i < messages.length; i++) {
      Message message = messages[i];
      ASTNode temp = node;
      int count = iterations;
      do {
        int nodeOffset = temp.getStartPosition();
        int messageOffset = message.getStartPosition();
        if (nodeOffset <= messageOffset && messageOffset < nodeOffset + temp.getLength()) {
          result.add(message);
          count = 0;
        } else {
          count--;
        }
      } while ((temp = temp.getParent()) != null && count > 0);
    }
    return result.toArray(new Message[result.size()]);
  }

  private static int computeIterations(int flags) {
    switch (flags) {
      case NODE_ONLY:
        return 1;
      case INCLUDE_ALL_PARENTS:
        return Integer.MAX_VALUE;
      case INCLUDE_FIRST_PARENT:
        return 2;
      default:
        return 1;
    }
  }

  private static int getOrderPreference(BodyDeclaration member, MembersOrderPreferenceCache store) {
    int memberType = member.getNodeType();
    int modifiers = member.getModifiers();

    switch (memberType) {
      case ASTNode.TYPE_DECLARATION:
      case ASTNode.ENUM_DECLARATION:
      case ASTNode.ANNOTATION_TYPE_DECLARATION:
        return store.getCategoryIndex(MembersOrderPreferenceCache.TYPE_INDEX) * 2;
      case ASTNode.FIELD_DECLARATION:
        if (Modifier.isStatic(modifiers)) {
          int index = store.getCategoryIndex(MembersOrderPreferenceCache.STATIC_FIELDS_INDEX) * 2;
          if (Modifier.isFinal(modifiers)) {
            return index; // first final static, then static
          }
          return index + 1;
        }
        return store.getCategoryIndex(MembersOrderPreferenceCache.FIELDS_INDEX) * 2;
      case ASTNode.INITIALIZER:
        if (Modifier.isStatic(modifiers)) {
          return store.getCategoryIndex(MembersOrderPreferenceCache.STATIC_INIT_INDEX) * 2;
        }
        return store.getCategoryIndex(MembersOrderPreferenceCache.INIT_INDEX) * 2;
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
        return store.getCategoryIndex(MembersOrderPreferenceCache.METHOD_INDEX) * 2;
      case ASTNode.METHOD_DECLARATION:
        if (Modifier.isStatic(modifiers)) {
          return store.getCategoryIndex(MembersOrderPreferenceCache.STATIC_METHODS_INDEX) * 2;
        }
        if (((MethodDeclaration) member).isConstructor()) {
          return store.getCategoryIndex(MembersOrderPreferenceCache.CONSTRUCTORS_INDEX) * 2;
        }
        return store.getCategoryIndex(MembersOrderPreferenceCache.METHOD_INDEX) * 2;
      default:
        return 100;
    }
  }

  /**
   * Computes the insertion index to be used to add the given member to the the list <code>container
   * </code>.
   *
   * @param member the member to add
   * @param container a list containing objects of type <code>BodyDeclaration</code>
   * @return the insertion index to be used
   */
  public static int getInsertionIndex(
      BodyDeclaration member, List<? extends BodyDeclaration> container) {
    int containerSize = container.size();

    MembersOrderPreferenceCache orderStore =
        JavaPlugin.getDefault().getMemberOrderPreferenceCache();

    int orderIndex = getOrderPreference(member, orderStore);

    int insertPos = containerSize;
    int insertPosOrderIndex = -1;

    for (int i = containerSize - 1; i >= 0; i--) {
      int currOrderIndex = getOrderPreference(container.get(i), orderStore);
      if (orderIndex == currOrderIndex) {
        if (insertPosOrderIndex != orderIndex) { // no perfect match yet
          insertPos = i + 1; // after a same kind
          insertPosOrderIndex = orderIndex; // perfect match
        }
      } else if (insertPosOrderIndex != orderIndex) { // not yet a perfect match
        if (currOrderIndex < orderIndex) { // we are bigger
          if (insertPosOrderIndex == -1) {
            insertPos = i + 1; // after
            insertPosOrderIndex = currOrderIndex;
          }
        } else {
          insertPos = i; // before
          insertPosOrderIndex = currOrderIndex;
        }
      }
    }
    return insertPos;
  }

  public static SimpleName getLeftMostSimpleName(Name name) {
    if (name instanceof SimpleName) {
      return (SimpleName) name;
    } else {
      final SimpleName[] result = new SimpleName[1];
      ASTVisitor visitor =
          new ASTVisitor() {
            @Override
            public boolean visit(QualifiedName qualifiedName) {
              Name left = qualifiedName.getQualifier();
              if (left instanceof SimpleName) result[0] = (SimpleName) left;
              else left.accept(this);
              return false;
            }
          };
      name.accept(visitor);
      return result[0];
    }
  }

  /**
   * Returns the topmost ancestor of <code>name</code> that is still a {@link
   * org.eclipse.jdt.core.dom.Name}.
   *
   * <p><b>Note:</b> The returned node may resolve to a different binding than the given <code>name
   * </code>!
   *
   * @param name a name node
   * @return the topmost name
   * @see #getNormalizedNode(org.eclipse.jdt.core.dom.ASTNode)
   */
  public static Name getTopMostName(Name name) {
    Name result = name;
    while (result.getParent() instanceof Name) {
      result = (Name) result.getParent();
    }
    return result;
  }

  //    /**
  //     * Returns the topmost ancestor of <code>node</code> that is a {@link
  // org.eclipse.jdt.core.dom.Type} (but not a {@link org.eclipse
  // .jdt.core.dom.UnionType}).
  //     * <p>
  //     * <b>Note:</b> The returned node often resolves to a different binding than the given
  // <code>node</code>!
  //     *
  //     * @param node the starting node, can be <code>null</code>
  //     * @return the topmost type or <code>null</code> if the node is not a descendant of a type
  // node
  //     * @see #getNormalizedNode(org.eclipse.jdt.core.dom.ASTNode)
  //     */
  //    public static Type getTopMostType(ASTNode node) {
  //        ASTNode result = null;
  //        while (node instanceof Type && !(node instanceof UnionType)
  //               || node instanceof Name
  //               || node instanceof Annotation || node instanceof MemberValuePair
  //               ||
  //               node instanceof Expression) { // Expression could maybe be reduced to expression
  // node types that can appear in an
  // annotation
  //            result = node;
  //            node = node.getParent();
  //        }
  //
  //        if (result instanceof Type)
  //            return (Type)result;
  //
  //        return null;
  //    }
  //
  //    public static int changeVisibility(int modifiers, int visibility) {
  //        return (modifiers & CLEAR_VISIBILITY) | visibility;
  //    }
  //
  //    /**
  //     * Adds flags to the given node and all its descendants.
  //     * @param root The root node
  //     * @param flags The flags to set
  //     */
  //    public static void setFlagsToAST(ASTNode root, final int flags) {
  //        root.accept(new GenericVisitor(true) {
  //            @Override
  //            protected boolean visitNode(ASTNode node) {
  //                node.setFlags(node.getFlags() | flags);
  //                return true;
  //            }
  //        });
  //    }

  public static String getQualifier(Name name) {
    if (name.isQualifiedName()) {
      return ((QualifiedName) name).getQualifier().getFullyQualifiedName();
    }
    return ""; // $NON-NLS-1$
  }

  public static String getSimpleNameIdentifier(Name name) {
    if (name.isQualifiedName()) {
      return ((QualifiedName) name).getName().getIdentifier();
    } else {
      return ((SimpleName) name).getIdentifier();
    }
  }

  public static boolean isDeclaration(Name name) {
    if (name.isQualifiedName()) {
      return ((QualifiedName) name).getName().isDeclaration();
    } else {
      return ((SimpleName) name).isDeclaration();
    }
  }

  public static Modifier findModifierNode(int flag, List<IExtendedModifier> modifiers) {
    for (int i = 0; i < modifiers.size(); i++) {
      Object curr = modifiers.get(i);
      if (curr instanceof Modifier && ((Modifier) curr).getKeyword().toFlagValue() == flag) {
        return (Modifier) curr;
      }
    }
    return null;
  }

  public static ITypeBinding getTypeBinding(CompilationUnit root, IType type)
      throws JavaModelException {
    if (type.isAnonymous()) {
      final IJavaElement parent = type.getParent();
      if (parent instanceof IField && Flags.isEnum(((IMember) parent).getFlags())) {
        final EnumConstantDeclaration constant =
            (EnumConstantDeclaration)
                NodeFinder.perform(root, ((ISourceReference) parent).getSourceRange());
        if (constant != null) {
          final AnonymousClassDeclaration declaration = constant.getAnonymousClassDeclaration();
          if (declaration != null) return declaration.resolveBinding();
        }
      } else {
        final ClassInstanceCreation creation =
            (ClassInstanceCreation)
                getParent(
                    NodeFinder.perform(root, type.getNameRange()), ClassInstanceCreation.class);
        if (creation != null) return creation.resolveTypeBinding();
      }
    } else {
      final AbstractTypeDeclaration declaration =
          (AbstractTypeDeclaration)
              getParent(
                  NodeFinder.perform(root, type.getNameRange()), AbstractTypeDeclaration.class);
      if (declaration != null) return declaration.resolveBinding();
    }
    return null;
  }

  /**
   * Escapes a string value to a literal that can be used in Java source.
   *
   * @param stringValue the string value
   * @return the escaped string
   * @see org.eclipse.jdt.core.dom.StringLiteral#getEscapedValue()
   */
  public static String getEscapedStringLiteral(String stringValue) {
    StringLiteral stringLiteral = AST.newAST(ASTProvider.SHARED_AST_LEVEL).newStringLiteral();
    stringLiteral.setLiteralValue(stringValue);
    return stringLiteral.getEscapedValue();
  }

  /**
   * Escapes a character value to a literal that can be used in Java source.
   *
   * @param ch the character value
   * @return the escaped string
   * @see org.eclipse.jdt.core.dom.CharacterLiteral#getEscapedValue()
   */
  public static String getEscapedCharacterLiteral(char ch) {
    CharacterLiteral characterLiteral =
        AST.newAST(ASTProvider.SHARED_AST_LEVEL).newCharacterLiteral();
    characterLiteral.setCharValue(ch);
    return characterLiteral.getEscapedValue();
  }

  /**
   * Type-safe variant of {@link
   * org.eclipse.jdt.core.dom.rewrite.ASTRewrite#createMoveTarget(org.eclipse.jdt.core.dom.ASTNode)}.
   *
   * @param rewrite ASTRewrite for the given node
   * @param node the node to create a move placeholder for
   * @return the new placeholder node
   * @throws IllegalArgumentException if the node is null, or if the node is not part of the
   *     rewrite's AST
   */
  @SuppressWarnings("unchecked")
  public static <T extends ASTNode> T createMoveTarget(ASTRewrite rewrite, T node) {
    return (T) rewrite.createMoveTarget(node);
  }

  /**
   * Type-safe variant of {@link
   * org.eclipse.jdt.core.dom.ASTNode#copySubtree(org.eclipse.jdt.core.dom.AST,
   * org.eclipse.jdt.core.dom .ASTNode)}.
   *
   * @param target the AST that is to own the nodes in the result
   * @param node the node to copy, or <code>null</code> if none
   * @return the copied node, or <code>null</code> if <code>node</code> is <code>null</code>
   */
  @SuppressWarnings("unchecked")
  public static <T extends ASTNode> T copySubtree(AST target, T node) {
    return (T) ASTNode.copySubtree(target, node);
  }

  /**
   * Returns a list of local variable names which are visible at the given node.
   *
   * @param node the AST node
   * @return a list of local variable names visible at the given node
   * @see ScopeAnalyzer#getDeclarationsInScope(int, int)
   * @since 3.10
   */
  public static List<String> getVisibleLocalVariablesInScope(ASTNode node) {
    List<String> variableNames = new ArrayList<String>();
    CompilationUnit root = (CompilationUnit) node.getRoot();
    IBinding[] bindings =
        new ScopeAnalyzer(root)
            .getDeclarationsInScope(
                node.getStartPosition(), ScopeAnalyzer.VARIABLES | ScopeAnalyzer.CHECK_VISIBILITY);
    for (IBinding binding : bindings) {
      if (binding instanceof IVariableBinding && !((IVariableBinding) binding).isField()) {
        variableNames.add(binding.getName());
      }
    }
    return variableNames;
  }
}

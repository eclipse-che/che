/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.corext.dom.TypeBindingVisitor;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;

/** JDT-UI-internal helper methods to find AST nodes or bindings. */
public class ASTResolving {

  public static ITypeBinding guessBindingForReference(ASTNode node) {
    return Bindings.normalizeTypeBinding(getPossibleReferenceBinding(node));
  }

  private static ITypeBinding getPossibleReferenceBinding(ASTNode node) {
    ASTNode parent = node.getParent();
    switch (parent.getNodeType()) {
      case ASTNode.ASSIGNMENT:
        Assignment assignment = (Assignment) parent;
        if (node.equals(assignment.getLeftHandSide())) {
          // field write access: xx= expression
          return assignment.getRightHandSide().resolveTypeBinding();
        }
        // read access
        return assignment.getLeftHandSide().resolveTypeBinding();
      case ASTNode.INFIX_EXPRESSION:
        InfixExpression infix = (InfixExpression) parent;
        InfixExpression.Operator op = infix.getOperator();
        if (op == InfixExpression.Operator.CONDITIONAL_AND
            || op == InfixExpression.Operator.CONDITIONAL_OR) {
          // boolean operation
          return infix.getAST().resolveWellKnownType("boolean"); // $NON-NLS-1$
        } else if (op == InfixExpression.Operator.LEFT_SHIFT
            || op == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED
            || op == InfixExpression.Operator.RIGHT_SHIFT_SIGNED) {
          // asymmetric operation
          return infix.getAST().resolveWellKnownType("int"); // $NON-NLS-1$
        }
        if (node.equals(infix.getLeftOperand())) {
          //	xx operation expression
          ITypeBinding rigthHandBinding = infix.getRightOperand().resolveTypeBinding();
          if (rigthHandBinding != null) {
            return rigthHandBinding;
          }
        } else {
          // expression operation xx
          ITypeBinding leftHandBinding = infix.getLeftOperand().resolveTypeBinding();
          if (leftHandBinding != null) {
            return leftHandBinding;
          }
        }
        if (op != InfixExpression.Operator.EQUALS && op != InfixExpression.Operator.NOT_EQUALS) {
          return infix.getAST().resolveWellKnownType("int"); // $NON-NLS-1$
        }
        break;
      case ASTNode.INSTANCEOF_EXPRESSION:
        InstanceofExpression instanceofExpression = (InstanceofExpression) parent;
        return instanceofExpression.getRightOperand().resolveBinding();
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
        VariableDeclarationFragment frag = (VariableDeclarationFragment) parent;
        if (frag.getInitializer().equals(node)) {
          return frag.getName().resolveTypeBinding();
        }
        break;
      case ASTNode.SUPER_METHOD_INVOCATION:
        SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) parent;
        IMethodBinding superMethodBinding =
            ASTNodes.getMethodBinding(superMethodInvocation.getName());
        if (superMethodBinding != null) {
          return getParameterTypeBinding(
              node, superMethodInvocation.arguments(), superMethodBinding);
        }
        break;
      case ASTNode.METHOD_INVOCATION:
        MethodInvocation methodInvocation = (MethodInvocation) parent;
        IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
        if (methodBinding != null) {
          return getParameterTypeBinding(node, methodInvocation.arguments(), methodBinding);
        }
        break;
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
        {
          SuperConstructorInvocation superInvocation = (SuperConstructorInvocation) parent;
          IMethodBinding superBinding = superInvocation.resolveConstructorBinding();
          if (superBinding != null) {
            return getParameterTypeBinding(node, superInvocation.arguments(), superBinding);
          }
          break;
        }
      case ASTNode.CONSTRUCTOR_INVOCATION:
        {
          ConstructorInvocation constrInvocation = (ConstructorInvocation) parent;
          IMethodBinding constrBinding = constrInvocation.resolveConstructorBinding();
          if (constrBinding != null) {
            return getParameterTypeBinding(node, constrInvocation.arguments(), constrBinding);
          }
          break;
        }
      case ASTNode.CLASS_INSTANCE_CREATION:
        {
          ClassInstanceCreation creation = (ClassInstanceCreation) parent;
          IMethodBinding creationBinding = creation.resolveConstructorBinding();
          if (creationBinding != null) {
            return getParameterTypeBinding(node, creation.arguments(), creationBinding);
          }
          break;
        }
      case ASTNode.PARENTHESIZED_EXPRESSION:
        return guessBindingForReference(parent);
      case ASTNode.ARRAY_ACCESS:
        if (((ArrayAccess) parent).getIndex().equals(node)) {
          return parent.getAST().resolveWellKnownType("int"); // $NON-NLS-1$
        } else {
          ITypeBinding parentBinding = getPossibleReferenceBinding(parent);
          if (parentBinding == null) {
            parentBinding = parent.getAST().resolveWellKnownType("java.lang.Object"); // $NON-NLS-1$
          }
          return parentBinding.createArrayType(1);
        }
      case ASTNode.ARRAY_CREATION:
        if (((ArrayCreation) parent).dimensions().contains(node)) {
          return parent.getAST().resolveWellKnownType("int"); // $NON-NLS-1$
        }
        break;
      case ASTNode.ARRAY_INITIALIZER:
        ASTNode initializerParent = parent.getParent();
        int dim = 1;
        while (initializerParent instanceof ArrayInitializer) {
          initializerParent = initializerParent.getParent();
          dim++;
        }
        Type creationType = null;
        if (initializerParent instanceof ArrayCreation) {
          creationType = ((ArrayCreation) initializerParent).getType();
        } else if (initializerParent instanceof VariableDeclaration) {
          VariableDeclaration varDecl = (VariableDeclaration) initializerParent;
          creationType = ASTNodes.getType(varDecl);
          dim -= varDecl.getExtraDimensions();
        } else if (initializerParent instanceof MemberValuePair) {
          String name = ((MemberValuePair) initializerParent).getName().getIdentifier();
          IMethodBinding annotMember =
              findAnnotationMember((Annotation) initializerParent.getParent(), name);
          if (annotMember != null) {
            return getReducedDimensionBinding(annotMember.getReturnType(), dim);
          }
        }
        if (creationType instanceof ArrayType) {
          ITypeBinding creationTypeBinding = ((ArrayType) creationType).resolveBinding();
          if (creationTypeBinding != null) {
            return Bindings.getComponentType(creationTypeBinding, dim);
          }
        }
        break;
      case ASTNode.CONDITIONAL_EXPRESSION:
        ConditionalExpression expression = (ConditionalExpression) parent;
        if (node.equals(expression.getExpression())) {
          return parent.getAST().resolveWellKnownType("boolean"); // $NON-NLS-1$
        }
        if (node.equals(expression.getElseExpression())) {
          return expression.getThenExpression().resolveTypeBinding();
        }
        return expression.getElseExpression().resolveTypeBinding();
      case ASTNode.POSTFIX_EXPRESSION:
        return parent.getAST().resolveWellKnownType("int"); // $NON-NLS-1$
      case ASTNode.PREFIX_EXPRESSION:
        if (((PrefixExpression) parent).getOperator() == PrefixExpression.Operator.NOT) {
          return parent.getAST().resolveWellKnownType("boolean"); // $NON-NLS-1$
        }
        return parent.getAST().resolveWellKnownType("int"); // $NON-NLS-1$
      case ASTNode.IF_STATEMENT:
      case ASTNode.WHILE_STATEMENT:
      case ASTNode.DO_STATEMENT:
        if (node instanceof Expression) {
          return parent.getAST().resolveWellKnownType("boolean"); // $NON-NLS-1$
        }
        break;
      case ASTNode.SWITCH_STATEMENT:
        if (((SwitchStatement) parent).getExpression().equals(node)) {
          return parent.getAST().resolveWellKnownType("int"); // $NON-NLS-1$
        }
        break;
      case ASTNode.RETURN_STATEMENT:
        MethodDeclaration decl = ASTResolving.findParentMethodDeclaration(parent);
        if (decl != null && !decl.isConstructor()) {
          return decl.getReturnType2().resolveBinding();
        }
        LambdaExpression lambdaExpr = ASTResolving.findEnclosingLambdaExpression(parent);
        if (lambdaExpr != null) {
          IMethodBinding lambdaMethodBinding = lambdaExpr.resolveMethodBinding();
          if (lambdaMethodBinding != null && lambdaMethodBinding.getReturnType() != null) {
            return lambdaMethodBinding.getReturnType();
          }
        }
        break;
      case ASTNode.CAST_EXPRESSION:
        return ((CastExpression) parent).getType().resolveBinding();
      case ASTNode.THROW_STATEMENT:
      case ASTNode.CATCH_CLAUSE:
        return parent.getAST().resolveWellKnownType("java.lang.Exception"); // $NON-NLS-1$
      case ASTNode.FIELD_ACCESS:
        if (node.equals(((FieldAccess) parent).getName())) {
          return getPossibleReferenceBinding(parent);
        }
        break;
      case ASTNode.SUPER_FIELD_ACCESS:
        return getPossibleReferenceBinding(parent);
      case ASTNode.QUALIFIED_NAME:
        if (node.equals(((QualifiedName) parent).getName())) {
          return getPossibleReferenceBinding(parent);
        }
        break;
      case ASTNode.SWITCH_CASE:
        if (node.equals(((SwitchCase) parent).getExpression())
            && parent.getParent() instanceof SwitchStatement) {
          return ((SwitchStatement) parent.getParent()).getExpression().resolveTypeBinding();
        }
        break;
      case ASTNode.ASSERT_STATEMENT:
        if (node.getLocationInParent() == AssertStatement.EXPRESSION_PROPERTY) {
          return parent.getAST().resolveWellKnownType("boolean"); // $NON-NLS-1$
        }
        return parent.getAST().resolveWellKnownType("java.lang.String"); // $NON-NLS-1$
      case ASTNode.SINGLE_MEMBER_ANNOTATION:
        {
          IMethodBinding annotMember =
              findAnnotationMember((Annotation) parent, "value"); // $NON-NLS-1$
          if (annotMember != null) {
            return annotMember.getReturnType();
          }
          break;
        }
      case ASTNode.MEMBER_VALUE_PAIR:
        {
          String name = ((MemberValuePair) parent).getName().getIdentifier();
          IMethodBinding annotMember = findAnnotationMember((Annotation) parent.getParent(), name);
          if (annotMember != null) {
            return annotMember.getReturnType();
          }
          break;
        }
      default:
        // do nothing
    }

    return null;
  }

  private static IMethodBinding findAnnotationMember(Annotation annotation, String name) {
    ITypeBinding annotBinding = annotation.resolveTypeBinding();
    if (annotBinding != null) {
      return Bindings.findMethodInType(annotBinding, name, (String[]) null);
    }
    return null;
  }

  public static Type guessTypeForReference(AST ast, ASTNode node) {
    ASTNode parent = node.getParent();
    while (parent != null) {
      switch (parent.getNodeType()) {
        case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
          if (((VariableDeclarationFragment) parent).getInitializer() == node) {
            return ASTNodeFactory.newType(ast, (VariableDeclaration) parent);
          }
          return null;
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
          if (((VariableDeclarationFragment) parent).getInitializer() == node) {
            return ASTNodeFactory.newType(ast, (VariableDeclaration) parent);
          }
          return null;
        case ASTNode.ARRAY_ACCESS:
          if (!((ArrayAccess) parent).getIndex().equals(node)) {
            Type type = guessTypeForReference(ast, parent);
            if (type != null) {
              return ASTNodeFactory.newArrayType(type);
            }
          }
          return null;
        case ASTNode.FIELD_ACCESS:
          if (node.equals(((FieldAccess) parent).getName())) {
            node = parent;
            parent = parent.getParent();
          } else {
            return null;
          }
          break;
        case ASTNode.SUPER_FIELD_ACCESS:
        case ASTNode.PARENTHESIZED_EXPRESSION:
          node = parent;
          parent = parent.getParent();
          break;
        case ASTNode.QUALIFIED_NAME:
          if (node.equals(((QualifiedName) parent).getName())) {
            node = parent;
            parent = parent.getParent();
          } else {
            return null;
          }
          break;
        default:
          return null;
      }
    }
    return null;
  }

  private static ITypeBinding getReducedDimensionBinding(
      ITypeBinding arrayBinding, int dimsToReduce) {
    while (dimsToReduce > 0) {
      arrayBinding = arrayBinding.getComponentType();
      dimsToReduce--;
    }
    return arrayBinding;
  }

  private static ITypeBinding getParameterTypeBinding(
      ASTNode node, List<Expression> args, IMethodBinding binding) {
    int index = args.indexOf(node);
    return getParameterTypeBinding(binding, index);
  }

  public static ITypeBinding getParameterTypeBinding(
      IMethodBinding methodBinding, int argumentIndex) {
    ITypeBinding[] paramTypes = methodBinding.getParameterTypes();
    if (methodBinding.isVarargs() && argumentIndex >= paramTypes.length - 1) {
      return paramTypes[paramTypes.length - 1].getComponentType();
    }
    if (argumentIndex >= 0 && argumentIndex < paramTypes.length) {
      return paramTypes[argumentIndex];
    }
    return null;
  }

  public static ITypeBinding guessBindingForTypeReference(ASTNode node) {
    StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
    if (locationInParent == QualifiedName.QUALIFIER_PROPERTY) {
      return null; // can't guess type for X.A
    }
    if (locationInParent == SimpleType.NAME_PROPERTY
        || locationInParent == NameQualifiedType.NAME_PROPERTY) {
      node = node.getParent();
    }
    ITypeBinding binding = Bindings.normalizeTypeBinding(getPossibleTypeBinding(node));
    if (binding != null) {
      if (binding.isWildcardType()) {
        return normalizeWildcardType(binding, true, node.getAST());
      }
    }
    return binding;
  }

  private static ITypeBinding getPossibleTypeBinding(ASTNode node) {
    ASTNode parent = node.getParent();
    switch (parent.getNodeType()) {
      case ASTNode.ARRAY_TYPE:
        {
          int dim = ((ArrayType) parent).getDimensions();
          ITypeBinding parentBinding = getPossibleTypeBinding(parent);
          if (parentBinding != null && parentBinding.getDimensions() == dim) {
            return parentBinding.getElementType();
          }
          return null;
        }
      case ASTNode.PARAMETERIZED_TYPE:
        {
          ITypeBinding parentBinding = getPossibleTypeBinding(parent);
          if (parentBinding == null || !parentBinding.isParameterizedType()) {
            return null;
          }
          if (node.getLocationInParent() == ParameterizedType.TYPE_PROPERTY) {
            return parentBinding;
          }

          ITypeBinding[] typeArguments = parentBinding.getTypeArguments();
          List<Type> argumentNodes = ((ParameterizedType) parent).typeArguments();
          int index = argumentNodes.indexOf(node);
          if (index != -1 && typeArguments.length == argumentNodes.size()) {
            return typeArguments[index];
          }
          return null;
        }
      case ASTNode.WILDCARD_TYPE:
        {
          ITypeBinding parentBinding = getPossibleTypeBinding(parent);
          if (parentBinding == null || !parentBinding.isWildcardType()) {
            return null;
          }
          WildcardType wildcardType = (WildcardType) parent;
          if (parentBinding.isUpperbound() == wildcardType.isUpperBound()) {
            return parentBinding.getBound();
          }
          return null;
        }
      case ASTNode.QUALIFIED_TYPE:
        {
          ITypeBinding parentBinding = getPossibleTypeBinding(parent);
          if (parentBinding == null || !parentBinding.isMember()) {
            return null;
          }
          if (node.getLocationInParent() == QualifiedType.QUALIFIER_PROPERTY) {
            return parentBinding.getDeclaringClass();
          }
          return parentBinding;
        }
      case ASTNode.NAME_QUALIFIED_TYPE:
        {
          ITypeBinding parentBinding = getPossibleTypeBinding(parent);
          if (parentBinding == null || !parentBinding.isMember()) {
            return null;
          }
          if (node.getLocationInParent() == NameQualifiedType.QUALIFIER_PROPERTY) {
            return parentBinding.getDeclaringClass();
          }
          return parentBinding;
        }
      case ASTNode.VARIABLE_DECLARATION_STATEMENT:
        return guessVariableType(((VariableDeclarationStatement) parent).fragments());
      case ASTNode.FIELD_DECLARATION:
        return guessVariableType(((FieldDeclaration) parent).fragments());
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
        return guessVariableType(((VariableDeclarationExpression) parent).fragments());
      case ASTNode.SINGLE_VARIABLE_DECLARATION:
        SingleVariableDeclaration varDecl = (SingleVariableDeclaration) parent;
        if (varDecl.getInitializer() != null) {
          return Bindings.normalizeTypeBinding(varDecl.getInitializer().resolveTypeBinding());
        }
        break;
      case ASTNode.ARRAY_CREATION:
        ArrayCreation creation = (ArrayCreation) parent;
        if (creation.getInitializer() != null) {
          return creation.getInitializer().resolveTypeBinding();
        }
        return getPossibleReferenceBinding(parent);
      case ASTNode.TYPE_LITERAL:
        return ((TypeLiteral) parent).getType().resolveBinding();
      case ASTNode.CLASS_INSTANCE_CREATION:
        return getPossibleReferenceBinding(parent);
      case ASTNode.CAST_EXPRESSION:
        return getPossibleReferenceBinding(parent);
      case ASTNode.TAG_ELEMENT:
        TagElement tagElement = (TagElement) parent;
        if (TagElement.TAG_THROWS.equals(tagElement.getTagName())
            || TagElement.TAG_EXCEPTION.equals(tagElement.getTagName())) {
          ASTNode methNode = tagElement.getParent().getParent();
          if (methNode instanceof MethodDeclaration) {
            List<Type> thrownExceptions = ((MethodDeclaration) methNode).thrownExceptionTypes();
            if (thrownExceptions.size() == 1) {
              return thrownExceptions.get(0).resolveBinding();
            }
          }
        }
        break;
    }
    return null;
  }

  private static ITypeBinding guessVariableType(List<VariableDeclarationFragment> fragments) {
    for (Iterator<VariableDeclarationFragment> iter = fragments.iterator(); iter.hasNext(); ) {
      VariableDeclarationFragment frag = iter.next();
      if (frag.getInitializer() != null) {
        return Bindings.normalizeTypeBinding(frag.getInitializer().resolveTypeBinding());
      }
    }
    return null;
  }

  /**
   * Finds all type bindings that contain a method of a given signature
   *
   * @param searchRoot the ast node to start the search from
   * @param selector the method name
   * @param arguments the method arguments
   * @param context the context in which the method would be called
   * @return returns all types known in the AST that have a method with a given name
   */
  public static ITypeBinding[] getQualifierGuess(
      ASTNode searchRoot,
      final String selector,
      List<Expression> arguments,
      final IBinding context) {
    final int nArgs = arguments.size();
    final ArrayList<ITypeBinding> result = new ArrayList<ITypeBinding>();

    // test if selector is a object method
    ITypeBinding binding =
        searchRoot.getAST().resolveWellKnownType("java.lang.Object"); // $NON-NLS-1$
    IMethodBinding[] objectMethods = binding.getDeclaredMethods();
    for (int i = 0; i < objectMethods.length; i++) {
      IMethodBinding meth = objectMethods[i];
      if (meth.getName().equals(selector) && meth.getParameterTypes().length == nArgs) {
        return new ITypeBinding[] {binding};
      }
    }

    visitAllBindings(
        searchRoot,
        new TypeBindingVisitor() {
          private HashSet<String> fVisitedBindings = new HashSet<String>(100);

          public boolean visit(ITypeBinding node) {
            node = Bindings.normalizeTypeBinding(node);
            if (node == null) {
              return true;
            }

            if (!fVisitedBindings.add(node.getKey())) {
              return true;
            }
            if (node.isGenericType()) {
              return true; // only look at  parameterized types
            }
            if (context != null && !isUseableTypeInContext(node, context, false)) {
              return true;
            }

            IMethodBinding[] methods = node.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
              IMethodBinding meth = methods[i];
              if (meth.getName().equals(selector) && meth.getParameterTypes().length == nArgs) {
                result.add(node);
              }
            }
            return true;
          }
        });
    return result.toArray(new ITypeBinding[result.size()]);
  }

  public static void visitAllBindings(ASTNode astRoot, TypeBindingVisitor visitor) {
    try {
      astRoot.accept(new AllBindingsVisitor(visitor));
    } catch (AllBindingsVisitor.VisitCancelledException e) {
      // visit cancelled
    }
  }

  private static class AllBindingsVisitor extends GenericVisitor {
    private final TypeBindingVisitor fVisitor;

    private static class VisitCancelledException extends RuntimeException {
      private static final long serialVersionUID = 1L;
    }

    public AllBindingsVisitor(TypeBindingVisitor visitor) {
      super(true);
      fVisitor = visitor;
    }

    @Override
    public boolean visit(SimpleName node) {
      ITypeBinding binding = node.resolveTypeBinding();
      if (binding != null) {
        boolean res = fVisitor.visit(binding);
        if (res) {
          res = Bindings.visitHierarchy(binding, fVisitor);
        }
        if (!res) {
          throw new VisitCancelledException();
        }
      }
      return false;
    }
  }

  public static IBinding getParentMethodOrTypeBinding(ASTNode node) {
    do {
      if (node instanceof MethodDeclaration) {
        return ((MethodDeclaration) node).resolveBinding();
      } else if (node instanceof AbstractTypeDeclaration) {
        return ((AbstractTypeDeclaration) node).resolveBinding();
      } else if (node instanceof AnonymousClassDeclaration) {
        return ((AnonymousClassDeclaration) node).resolveBinding();
      }
      node = node.getParent();
    } while (node != null);

    return null;
  }

  public static BodyDeclaration findParentBodyDeclaration(ASTNode node) {
    while ((node != null) && (!(node instanceof BodyDeclaration))) {
      node = node.getParent();
    }
    return (BodyDeclaration) node;
  }

  public static BodyDeclaration findParentBodyDeclaration(
      ASTNode node, boolean treatModifiersOutside) {
    StructuralPropertyDescriptor lastLocation = null;

    while (node != null) {
      if (node instanceof BodyDeclaration) {
        BodyDeclaration decl = (BodyDeclaration) node;
        if (!treatModifiersOutside || lastLocation != decl.getModifiersProperty()) {
          return decl;
        }
        treatModifiersOutside = false;
      }
      lastLocation = node.getLocationInParent();
      node = node.getParent();
    }
    return (BodyDeclaration) node;
  }

  public static CompilationUnit findParentCompilationUnit(ASTNode node) {
    return (CompilationUnit) findAncestor(node, ASTNode.COMPILATION_UNIT);
  }

  /**
   * Finds the ancestor type of <code>node</code> (includes <code>node</code> in the search).
   *
   * @param node the node to start the search from, can be <code>null</code>
   * @param treatModifiersOutside if set, modifiers are not part of their type, but of the type's
   *     parent
   * @return returns the ancestor type of <code>node</code> (AbstractTypeDeclaration or
   *     AnonymousTypeDeclaration) if any (including <code>node</code>), <code>null</code> otherwise
   */
  public static ASTNode findParentType(ASTNode node, boolean treatModifiersOutside) {
    StructuralPropertyDescriptor lastLocation = null;

    while (node != null) {
      if (node instanceof AbstractTypeDeclaration) {
        AbstractTypeDeclaration decl = (AbstractTypeDeclaration) node;
        if (!treatModifiersOutside || lastLocation != decl.getModifiersProperty()) {
          return decl;
        }
      } else if (node instanceof AnonymousClassDeclaration) {
        return node;
      }
      lastLocation = node.getLocationInParent();
      node = node.getParent();
    }
    return null;
  }

  /**
   * Finds the ancestor type of <code>node</code> (includes <code>node</code> in the search).
   *
   * @param node the node to start the search from, can be <code>null</code>
   * @return returns the ancestor type of <code>node</code> (AbstractTypeDeclaration or
   *     AnonymousTypeDeclaration) if any (including <code>node</code>), <code>null</code> otherwise
   */
  public static ASTNode findParentType(ASTNode node) {
    return findParentType(node, false);
  }

  /**
   * The node's enclosing method declaration or <code>null</code> if the node is not inside a method
   * and is not a method declaration itself.
   *
   * @param node a node
   * @return the enclosing method declaration or <code>null</code>
   */
  public static MethodDeclaration findParentMethodDeclaration(ASTNode node) {
    while (node != null) {
      if (node instanceof MethodDeclaration) {
        return (MethodDeclaration) node;
      } else if (node instanceof BodyDeclaration
          || node instanceof AnonymousClassDeclaration
          || node instanceof LambdaExpression) {
        return null;
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the lambda expression node which encloses the given <code>node</code>, or <code>null
   * </code> if none.
   *
   * @param node the node
   * @return the enclosing lambda expression node for the given <code>node</code>, or <code>null
   *     </code> if none
   * @since 3.10
   */
  public static LambdaExpression findEnclosingLambdaExpression(ASTNode node) {
    node = node.getParent();
    while (node != null) {
      if (node instanceof LambdaExpression) {
        return (LambdaExpression) node;
      }
      if (node instanceof BodyDeclaration || node instanceof AnonymousClassDeclaration) {
        return null;
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the closest ancestor of <code>node</code> (including <code>node</code> itself) whose
   * type is <code>nodeType</code>, or <code>null</code> if none.
   *
   * <p><b>Warning:</b> This method does not stop at any boundaries like parentheses, statements,
   * body declarations, etc. The resulting node may be in a totally different scope than the given
   * node. Consider using one of the other {@link ASTResolving}<code>.find(..)</code> methods
   * instead.
   *
   * @param node the node
   * @param nodeType the node type constant from {@link ASTNode}
   * @return the closest ancestor of <code>node</code> (including <code>node</code> itself) whose
   *     type is <code>nodeType</code>, or <code>null</code> if none
   */
  public static ASTNode findAncestor(ASTNode node, int nodeType) {
    while ((node != null) && (node.getNodeType() != nodeType)) {
      node = node.getParent();
    }
    return node;
  }

  public static Statement findParentStatement(ASTNode node) {
    while ((node != null) && (!(node instanceof Statement))) {
      node = node.getParent();
      if (node instanceof BodyDeclaration) {
        return null;
      }
    }
    return (Statement) node;
  }

  public static TryStatement findParentTryStatement(ASTNode node) {
    while ((node != null) && (!(node instanceof TryStatement))) {
      node = node.getParent();
      if (node instanceof BodyDeclaration) {
        return null;
      }
    }
    return (TryStatement) node;
  }

  public static boolean isInsideConstructorInvocation(
      MethodDeclaration methodDeclaration, ASTNode node) {
    if (methodDeclaration.isConstructor()) {
      Statement statement = ASTResolving.findParentStatement(node);
      if (statement instanceof ConstructorInvocation
          || statement instanceof SuperConstructorInvocation) {
        return true; // argument in a this or super call
      }
    }
    return false;
  }

  public static boolean isInsideModifiers(ASTNode node) {
    while (node != null && !(node instanceof BodyDeclaration)) {
      if (node instanceof Annotation) {
        return true;
      }
      node = node.getParent();
    }
    return false;
  }

  public static boolean isInStaticContext(ASTNode selectedNode) {
    BodyDeclaration decl = ASTResolving.findParentBodyDeclaration(selectedNode);
    if (decl instanceof MethodDeclaration) {
      if (isInsideConstructorInvocation((MethodDeclaration) decl, selectedNode)) {
        return true;
      }
      return Modifier.isStatic(decl.getModifiers());
    } else if (decl instanceof Initializer) {
      return Modifier.isStatic(((Initializer) decl).getModifiers());
    } else if (decl instanceof FieldDeclaration) {
      return Modifier.isStatic(((FieldDeclaration) decl).getModifiers());
    }
    return false;
  }

  public static boolean isWriteAccess(Name selectedNode) {
    ASTNode curr = selectedNode;
    ASTNode parent = curr.getParent();
    while (parent != null) {
      switch (parent.getNodeType()) {
        case ASTNode.QUALIFIED_NAME:
          if (((QualifiedName) parent).getQualifier() == curr) {
            return false;
          }
          break;
        case ASTNode.FIELD_ACCESS:
          if (((FieldAccess) parent).getExpression() == curr) {
            return false;
          }
          break;
        case ASTNode.SUPER_FIELD_ACCESS:
          break;
        case ASTNode.ASSIGNMENT:
          return ((Assignment) parent).getLeftHandSide() == curr;
        case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
          return ((VariableDeclaration) parent).getName() == curr;
        case ASTNode.POSTFIX_EXPRESSION:
          return true;
        case ASTNode.PREFIX_EXPRESSION:
          PrefixExpression.Operator op = ((PrefixExpression) parent).getOperator();
          return op == PrefixExpression.Operator.DECREMENT
              || op == PrefixExpression.Operator.INCREMENT;
        default:
          return false;
      }

      curr = parent;
      parent = curr.getParent();
    }
    return false;
  }

  public static int getPossibleTypeKinds(ASTNode node, boolean is50OrHigher) {
    int kinds = internalGetPossibleTypeKinds(node);
    if (!is50OrHigher) {
      kinds &= (SimilarElementsRequestor.INTERFACES | SimilarElementsRequestor.CLASSES);
    }
    return kinds;
  }

  private static int internalGetPossibleTypeKinds(ASTNode node) {
    int kind = SimilarElementsRequestor.ALL_TYPES;

    int mask = SimilarElementsRequestor.ALL_TYPES | SimilarElementsRequestor.VOIDTYPE;

    ASTNode parent = node.getParent();
    while (parent instanceof QualifiedName) {
      if (node.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY) {
        return SimilarElementsRequestor.REF_TYPES;
      }
      node = parent;
      parent = parent.getParent();
      mask = SimilarElementsRequestor.REF_TYPES;
    }
    while (parent instanceof Type) {
      if (parent instanceof QualifiedType) {
        if (node.getLocationInParent() == QualifiedType.QUALIFIER_PROPERTY) {
          return mask & (SimilarElementsRequestor.REF_TYPES);
        }
        mask &= SimilarElementsRequestor.REF_TYPES;
      } else if (parent instanceof NameQualifiedType) {
        if (node.getLocationInParent() == NameQualifiedType.QUALIFIER_PROPERTY) {
          return mask & (SimilarElementsRequestor.REF_TYPES);
        }
        mask &= SimilarElementsRequestor.REF_TYPES;
      } else if (parent instanceof ParameterizedType) {
        if (node.getLocationInParent() == ParameterizedType.TYPE_ARGUMENTS_PROPERTY) {
          return mask & SimilarElementsRequestor.REF_TYPES_AND_VAR;
        }
        mask &= SimilarElementsRequestor.CLASSES | SimilarElementsRequestor.INTERFACES;
      } else if (parent instanceof WildcardType) {
        if (node.getLocationInParent() == WildcardType.BOUND_PROPERTY) {
          return mask & SimilarElementsRequestor.REF_TYPES_AND_VAR;
        }
      }
      node = parent;
      parent = parent.getParent();
    }

    switch (parent.getNodeType()) {
      case ASTNode.TYPE_DECLARATION:
        if (node.getLocationInParent() == TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY) {
          kind = SimilarElementsRequestor.INTERFACES;
        } else if (node.getLocationInParent() == TypeDeclaration.SUPERCLASS_TYPE_PROPERTY) {
          kind = SimilarElementsRequestor.CLASSES;
        }
        break;
      case ASTNode.ENUM_DECLARATION:
        kind = SimilarElementsRequestor.INTERFACES;
        break;
      case ASTNode.METHOD_DECLARATION:
        if (node.getLocationInParent() == MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY) {
          kind = SimilarElementsRequestor.CLASSES;
        } else if (node.getLocationInParent() == MethodDeclaration.RETURN_TYPE2_PROPERTY) {
          kind = SimilarElementsRequestor.ALL_TYPES | SimilarElementsRequestor.VOIDTYPE;
        }
        break;
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
        kind =
            SimilarElementsRequestor.PRIMITIVETYPES
                | SimilarElementsRequestor.ANNOTATIONS
                | SimilarElementsRequestor.ENUMS;
        break;
      case ASTNode.INSTANCEOF_EXPRESSION:
        kind = SimilarElementsRequestor.REF_TYPES;
        break;
      case ASTNode.THROW_STATEMENT:
        kind = SimilarElementsRequestor.CLASSES;
        break;
      case ASTNode.CLASS_INSTANCE_CREATION:
        if (((ClassInstanceCreation) parent).getAnonymousClassDeclaration() == null) {
          kind = SimilarElementsRequestor.CLASSES;
        } else {
          kind = SimilarElementsRequestor.CLASSES | SimilarElementsRequestor.INTERFACES;
        }
        break;
      case ASTNode.SINGLE_VARIABLE_DECLARATION:
        int superParent = parent.getParent().getNodeType();
        if (superParent == ASTNode.CATCH_CLAUSE) {
          kind = SimilarElementsRequestor.CLASSES;
        } else if (superParent == ASTNode.ENHANCED_FOR_STATEMENT) {
          kind = SimilarElementsRequestor.REF_TYPES;
        }
        break;
      case ASTNode.TAG_ELEMENT:
        kind = SimilarElementsRequestor.REF_TYPES;
        break;
      case ASTNode.MARKER_ANNOTATION:
      case ASTNode.SINGLE_MEMBER_ANNOTATION:
      case ASTNode.NORMAL_ANNOTATION:
        kind = SimilarElementsRequestor.ANNOTATIONS;
        break;
      case ASTNode.TYPE_PARAMETER:
        if (((TypeParameter) parent).typeBounds().indexOf(node) > 0) {
          kind = SimilarElementsRequestor.INTERFACES;
        } else {
          kind = SimilarElementsRequestor.REF_TYPES_AND_VAR;
        }
        break;
      case ASTNode.TYPE_LITERAL:
        kind = SimilarElementsRequestor.REF_TYPES;
        break;
      default:
    }
    return kind & mask;
  }

  public static String getFullName(Name name) {
    return name.getFullyQualifiedName();
  }

  public static ICompilationUnit findCompilationUnitForBinding(
      ICompilationUnit cu, CompilationUnit astRoot, ITypeBinding binding)
      throws JavaModelException {
    if (binding == null
        || !binding.isFromSource()
        || binding.isTypeVariable()
        || binding.isWildcardType()) {
      return null;
    }
    ASTNode node = astRoot.findDeclaringNode(binding.getTypeDeclaration());
    if (node == null) {
      ICompilationUnit targetCU = Bindings.findCompilationUnit(binding, cu.getJavaProject());
      if (targetCU != null) {
        return targetCU;
      }
      return null;
    } else if (node instanceof AbstractTypeDeclaration
        || node instanceof AnonymousClassDeclaration) {
      return cu;
    }
    return null;
  }

  private static final Code[] CODE_ORDER = {
    PrimitiveType.CHAR,
    PrimitiveType.SHORT,
    PrimitiveType.INT,
    PrimitiveType.LONG,
    PrimitiveType.FLOAT,
    PrimitiveType.DOUBLE
  };

  public static ITypeBinding[] getNarrowingTypes(AST ast, ITypeBinding type) {
    ArrayList<ITypeBinding> res = new ArrayList<ITypeBinding>();
    res.add(type);
    if (type.isPrimitive()) {
      Code code = PrimitiveType.toCode(type.getName());
      for (int i = 0; i < CODE_ORDER.length && code != CODE_ORDER[i]; i++) {
        String typeName = CODE_ORDER[i].toString();
        res.add(ast.resolveWellKnownType(typeName));
      }
    }
    return res.toArray(new ITypeBinding[res.size()]);
  }

  public static ITypeBinding[] getRelaxingTypes(AST ast, ITypeBinding type) {
    ArrayList<ITypeBinding> res = new ArrayList<ITypeBinding>();
    res.add(type);
    if (type.isArray()) {
      res.add(ast.resolveWellKnownType("java.lang.Object")); // $NON-NLS-1$
      // The following two types are not available in some j2me implementations, see
      // https://bugs.eclipse.org/bugs/show_bug
      // .cgi?id=288060 :
      ITypeBinding serializable = ast.resolveWellKnownType("java.io.Serializable"); // $NON-NLS-1$
      if (serializable != null) res.add(serializable);
      ITypeBinding cloneable = ast.resolveWellKnownType("java.lang.Cloneable"); // $NON-NLS-1$
      if (cloneable != null) res.add(cloneable);
    } else if (type.isPrimitive()) {
      Code code = PrimitiveType.toCode(type.getName());
      boolean found = false;
      for (int i = 0; i < CODE_ORDER.length; i++) {
        if (found) {
          String typeName = CODE_ORDER[i].toString();
          res.add(ast.resolveWellKnownType(typeName));
        }
        if (code == CODE_ORDER[i]) {
          found = true;
        }
      }
    } else {
      collectRelaxingTypes(res, type);
    }
    return res.toArray(new ITypeBinding[res.size()]);
  }

  private static void collectRelaxingTypes(Collection<ITypeBinding> res, ITypeBinding type) {
    ITypeBinding[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      ITypeBinding curr = interfaces[i];
      if (!res.contains(curr)) {
        res.add(curr);
      }
      collectRelaxingTypes(res, curr);
    }
    ITypeBinding binding = type.getSuperclass();
    if (binding != null) {
      if (!res.contains(binding)) {
        res.add(binding);
      }
      collectRelaxingTypes(res, binding);
    }
  }

  public static String[] getUsedVariableNames(ASTNode node) {
    CompilationUnit root = (CompilationUnit) node.getRoot();
    Collection<String> res =
        (new ScopeAnalyzer(root)).getUsedVariableNames(node.getStartPosition(), node.getLength());
    return res.toArray(new String[res.size()]);
  }

  private static boolean isVariableDefinedInContext(IBinding binding, ITypeBinding typeVariable) {
    if (binding.getKind() == IBinding.VARIABLE) {
      IVariableBinding var = (IVariableBinding) binding;
      binding = var.getDeclaringMethod();
      if (binding == null) {
        binding = var.getDeclaringClass();
      }
    }
    if (binding instanceof IMethodBinding) {
      if (binding == typeVariable.getDeclaringMethod()) {
        return true;
      }
      binding = ((IMethodBinding) binding).getDeclaringClass();
    }

    while (binding instanceof ITypeBinding) {
      if (binding == typeVariable.getDeclaringClass()) {
        return true;
      }
      if (Modifier.isStatic(binding.getModifiers())) {
        break;
      }
      binding = ((ITypeBinding) binding).getDeclaringClass();
    }
    return false;
  }

  public static boolean isUseableTypeInContext(
      ITypeBinding[] binding, IBinding context, boolean noWildcards) {
    for (int i = 0; i < binding.length; i++) {
      if (!isUseableTypeInContext(binding[i], context, noWildcards)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isUseableTypeInContext(
      ITypeBinding type, IBinding context, boolean noWildcards) {
    if (type.isArray()) {
      type = type.getElementType();
    }
    if (type.isAnonymous()) {
      return false;
    }
    if (type.isRawType() || type.isPrimitive()) {
      return true;
    }
    if (type.isTypeVariable()) {
      return isVariableDefinedInContext(context, type);
    }
    if (type.isGenericType()) {
      ITypeBinding[] typeParameters = type.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        if (!isUseableTypeInContext(typeParameters[i], context, noWildcards)) {
          return false;
        }
      }
      return true;
    }
    if (type.isParameterizedType()) {
      ITypeBinding[] typeArguments = type.getTypeArguments();
      for (int i = 0; i < typeArguments.length; i++) {
        if (!isUseableTypeInContext(typeArguments[i], context, noWildcards)) {
          return false;
        }
      }
      return true;
    }
    if (type.isCapture()) {
      type = type.getWildcard();
    }

    if (type.isWildcardType()) {
      if (noWildcards) {
        return false;
      }
      if (type.getBound() != null) {
        return isUseableTypeInContext(type.getBound(), context, noWildcards);
      }
    }
    return true;
  }

  /**
   * Use this method before creating a type for a wildcard. Either to assign a wildcard to a new
   * type or for a type to be assigned.
   *
   * @param wildcardType the wildcard type to normalize
   * @param isBindingToAssign if true, then the type X for new variable x is returned (X x= s); if
   *     false, the type of an expression x (R r= x)
   * @param ast the current AST
   * @return the normalized binding or null when only the 'null' binding
   * @see Bindings#normalizeForDeclarationUse(ITypeBinding, AST)
   */
  public static ITypeBinding normalizeWildcardType(
      ITypeBinding wildcardType, boolean isBindingToAssign, AST ast) {
    ITypeBinding bound = wildcardType.getBound();
    if (isBindingToAssign) {
      if (bound == null || !wildcardType.isUpperbound()) {
        ITypeBinding[] typeBounds = wildcardType.getTypeBounds();
        if (typeBounds.length > 0) {
          return typeBounds[0];
        } else {
          return wildcardType.getErasure();
        }
      }
    } else {
      if (bound == null || wildcardType.isUpperbound()) {
        return null;
      }
    }
    return bound;
  }

  // pretty signatures

  public static String getTypeSignature(ITypeBinding type) {
    return BindingLabelProvider.getBindingLabel(type, BindingLabelProvider.DEFAULT_TEXTFLAGS);
  }

  public static String getMethodSignature(IMethodBinding binding) {
    return BindingLabelProvider.getBindingLabel(binding, BindingLabelProvider.DEFAULT_TEXTFLAGS);
  }

  public static String getMethodSignature(String name, ITypeBinding[] params, boolean isVarArgs) {
    StringBuffer buf = new StringBuffer();
    buf.append(name).append('(');
    for (int i = 0; i < params.length; i++) {
      if (i > 0) {
        buf.append(JavaElementLabels.COMMA_STRING);
      }
      if (isVarArgs && i == params.length - 1) {
        buf.append(getTypeSignature(params[i].getElementType()));
        buf.append("..."); // $NON-NLS-1$
      } else {
        buf.append(getTypeSignature(params[i]));
      }
    }
    buf.append(')');
    return BasicElementLabels.getJavaElementName(buf.toString());
  }

  public static CompilationUnit createQuickFixAST(
      ICompilationUnit compilationUnit, IProgressMonitor monitor) {
    CheASTParser astParser = CheASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
    astParser.setSource(compilationUnit);
    astParser.setResolveBindings(true);
    astParser.setStatementsRecovery(ASTProvider.SHARED_AST_STATEMENT_RECOVERY);
    astParser.setBindingsRecovery(ASTProvider.SHARED_BINDING_RECOVERY);
    return (CompilationUnit) astParser.createAST(monitor);
  }
}

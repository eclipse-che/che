/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.generics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.HierarchicalASTVisitor;
import org.eclipse.jdt.internal.corext.refactoring.rename.MethodChecks;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.GenericType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ParameterizedType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TypeVariable;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.WildcardType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ArrayElementVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.CollectionElementVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ConstraintVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ImmutableTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.IndependentTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ParameterTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ParameterizedTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ReturnTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TTypes;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.VariableVariable2;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

public class InferTypeArgumentsConstraintCreator extends HierarchicalASTVisitor {

  /** Property in <code>ASTNode</code>s that holds the node's <code>ConstraintVariable</code>. */
  private static final String CV_PROP =
      "org.eclipse.jdt.internal.corext.refactoring.typeconstraints.CONSTRAINT_VARIABLE"; // $NON-NLS-1$

  private InferTypeArgumentsTCModel fTCModel;
  private ICompilationUnit fCU;

  private final boolean fAssumeCloneReturnsSameType;

  public InferTypeArgumentsConstraintCreator(
      InferTypeArgumentsTCModel model, boolean assumeCloneReturnsSameType) {
    fTCModel = model;
    fAssumeCloneReturnsSameType = assumeCloneReturnsSameType;
  }

  @Override
  public boolean visit(CompilationUnit node) {
    fTCModel.newCu(); // TODO: make sure that accumulators are reset after last CU!
    fCU = (ICompilationUnit) node.getJavaElement();
    fTCModel.getTypeEnvironment().initializeJavaLangObject(fCU.getJavaProject());
    return super.visit(node);
  }

  @Override
  public boolean visit(Javadoc node) {
    return false;
  }

  @Override
  public boolean visit(Type node) {
    return false; // TODO
  }

  /*
   * @see org.eclipse.jdt.internal.corext.dom.HierarchicalASTVisitor#endVisit(org.eclipse.jdt.core.dom.Type)
   */
  @Override
  public void endVisit(Type node) {
    if (node.isParameterizedType()) {
      // retain already parameterized types
      ImmutableTypeVariable2 typeVariable =
          fTCModel.makeImmutableTypeVariable(node.resolveBinding(), /*no boxing*/ null);
      setConstraintVariable(node, typeVariable);
    } else {
      TypeVariable2 typeVariable = fTCModel.makeTypeVariable(node);
      setConstraintVariable(node, typeVariable);
    }
  }

  @Override
  public void endVisit(SimpleName node) {
    if (node.resolveBoxing()) {
      ImmutableTypeVariable2 boxed =
          fTCModel.makeImmutableTypeVariable(node.resolveTypeBinding(), node);
      setConstraintVariable(node, boxed);
      return;
    }
    IBinding binding = node.resolveBinding();
    if (binding instanceof IVariableBinding) {
      // TODO: code is similar to handling of method return value
      IVariableBinding variableBinding = (IVariableBinding) binding;
      ITypeBinding declaredVariableType = variableBinding.getVariableDeclaration().getType();
      if (declaredVariableType.isTypeVariable()) {
        Expression receiver = getSimpleNameReceiver(node);
        if (receiver != null) {
          ConstraintVariable2 receiverCv = getConstraintVariable(receiver);
          Assert.isNotNull(receiverCv); // the type variable must come from the receiver!

          ConstraintVariable2 elementCv =
              fTCModel.getElementVariable(receiverCv, declaredVariableType);
          // [retVal] =^= Elem[receiver]:
          setConstraintVariable(node, elementCv);
          return;
        }

      } else if (declaredVariableType.isParameterizedType()) {
        Expression receiver = getSimpleNameReceiver(node);
        if (receiver != null) {
          ConstraintVariable2 receiverCv = getConstraintVariable(receiver);
          if (receiverCv != null) {
            //						ITypeBinding genericVariableType= declaredVariableType.getTypeDeclaration();
            ConstraintVariable2 returnTypeCv =
                fTCModel.makeParameterizedTypeVariable(declaredVariableType);
            setConstraintVariable(node, returnTypeCv);
            // Elem[retVal] =^= Elem[receiver]
            TType declaredVariableTType = fTCModel.createTType(declaredVariableType);
            fTCModel.createTypeVariablesEqualityConstraints(
                receiverCv,
                Collections.<String, IndependentTypeVariable2>emptyMap(),
                returnTypeCv,
                declaredVariableTType);
            return;
          }
        }

      } else {
        // TODO: array...
        // logUnexpectedNode(node, null);
      }

      // default:
      VariableVariable2 cv = fTCModel.makeVariableVariable(variableBinding);
      setConstraintVariable(node, cv);
    }
    // TODO else?
  }

  private Expression getSimpleNameReceiver(SimpleName node) {
    Expression receiver;
    if (node.getParent() instanceof QualifiedName
        && node.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
      receiver = ((QualifiedName) node.getParent()).getQualifier();
    } else if (node.getParent() instanceof FieldAccess
        && node.getLocationInParent() == FieldAccess.NAME_PROPERTY) {
      receiver = ((FieldAccess) node.getParent()).getExpression();
    } else {
      // TODO other cases? (ThisExpression, SuperAccessExpression, ...)
      receiver = null;
    }
    if (receiver instanceof ThisExpression) return null;
    else return receiver;
  }

  @Override
  public void endVisit(FieldAccess node) {
    if (node.resolveBoxing()) {
      ImmutableTypeVariable2 boxed =
          fTCModel.makeImmutableTypeVariable(node.resolveTypeBinding(), node);
      setConstraintVariable(node, boxed);
      return;
    }
    ConstraintVariable2 nameCv = getConstraintVariable(node.getName());
    setConstraintVariable(node, nameCv);
  }

  @Override
  public void endVisit(QualifiedName node) {
    if (node.resolveBoxing()) {
      ImmutableTypeVariable2 boxed =
          fTCModel.makeImmutableTypeVariable(node.resolveTypeBinding(), node);
      setConstraintVariable(node, boxed);
      return;
    }
    ConstraintVariable2 cv = getConstraintVariable(node.getName());
    setConstraintVariable(node, cv);
  }

  @Override
  public void endVisit(ArrayAccess node) {
    if (node.resolveBoxing()) {
      ImmutableTypeVariable2 boxed =
          fTCModel.makeImmutableTypeVariable(node.resolveTypeBinding(), node);
      setConstraintVariable(node, boxed);
      return;
    }

    ConstraintVariable2 arrayCv = getConstraintVariable(node.getArray());
    if (arrayCv == null) return;

    ArrayElementVariable2 arrayElementCv = fTCModel.getArrayElementVariable(arrayCv);
    setConstraintVariable(node, arrayElementCv);
  }

  @Override
  public void endVisit(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    Expression rhs = node.getRightHandSide();

    ConstraintVariable2 left = getConstraintVariable(lhs);
    ConstraintVariable2 right = getConstraintVariable(rhs);
    if (node.resolveBoxing()) {
      ImmutableTypeVariable2 boxed =
          fTCModel.makeImmutableTypeVariable(node.resolveTypeBinding(), node);
      setConstraintVariable(node, boxed);
    } else {
      setConstraintVariable(node, left); // type of assignement is type of 'left'
    }
    if (left == null || right == null) return;

    Assignment.Operator op = node.getOperator();
    if (op == Assignment.Operator.PLUS_ASSIGN
        && (lhs.resolveTypeBinding()
            == node.getAST().resolveWellKnownType("java.lang.String"))) { // $NON-NLS-1$
      // Special handling for automatic String conversion: do nothing; the RHS can be anything.
    } else {
      fTCModel.createElementEqualsConstraints(left, right);
      fTCModel.createSubtypeConstraint(right, left); // left= right;  -->  [right] <= [left]
    }
    // TODO: other implicit conversions: numeric promotion, autoboxing?
  }

  @Override
  public void endVisit(CastExpression node) {
    //		if (! (expressionCv instanceof CollectionElementVariable2))
    //			return; //TODO: returns too early when dealing with nested collections.

    Type type = node.getType();
    ITypeBinding typeBinding = type.resolveBinding();
    if (typeBinding.isPrimitive()) {
      ImmutableTypeVariable2 boxed = fTCModel.makeImmutableTypeVariable(typeBinding, node);
      setConstraintVariable(node, boxed);
      return; // avoid removing numeric conversions
    }

    ConstraintVariable2 typeCv = getConstraintVariable(type);
    if (typeCv == null) return;

    // TODO: can this be loosened when we remove casts?
    setConstraintVariable(node, typeCv);

    Expression expression = node.getExpression();
    ConstraintVariable2 expressionCv = getConstraintVariable(expression);

    // Avoid removing casts that have not been made obsolete by this refactoring:
    if (expressionCv == null) return;
    if (expressionCv instanceof ImmutableTypeVariable2) return;
    if (!(expressionCv instanceof TypeVariable2
            || expressionCv instanceof IndependentTypeVariable2
            || expressionCv instanceof CollectionElementVariable2)
        && fTCModel.getElementVariables(expressionCv).size() == 0
        && fTCModel.getArrayElementVariable(expressionCv) == null) return;

    fTCModel.createAssignmentElementConstraints(typeCv, expressionCv);

    if (expression instanceof MethodInvocation) {
      MethodInvocation invoc = (MethodInvocation) expression;
      if (!isSpecialCloneInvocation(invoc.resolveMethodBinding(), invoc.getExpression())) {
        fTCModel.makeCastVariable(node, expressionCv);
      }
    } else {
      fTCModel.makeCastVariable(node, expressionCv);
    }

    boolean eitherIsIntf =
        typeBinding.isInterface() || expression.resolveTypeBinding().isInterface();
    if (eitherIsIntf) return;

    // TODO: preserve up- and down-castedness!

  }

  @Override
  public void endVisit(ParenthesizedExpression node) {
    if (node.resolveBoxing()) {
      ImmutableTypeVariable2 boxed =
          fTCModel.makeImmutableTypeVariable(node.resolveTypeBinding(), node);
      setConstraintVariable(node, boxed);
      return;
    }
    ConstraintVariable2 expressionCv = getConstraintVariable(node.getExpression());
    setConstraintVariable(node, expressionCv);
  }

  @Override
  public void endVisit(ConditionalExpression node) {
    // for now, no support for passing generic types through conditional expressions
    ImmutableTypeVariable2 boxed =
        fTCModel.makeImmutableTypeVariable(node.resolveTypeBinding(), node);
    setConstraintVariable(node, boxed);
  }

  @Override
  public boolean visit(CatchClause node) {
    SingleVariableDeclaration exception = node.getException();
    IVariableBinding variableBinding = exception.resolveBinding();
    VariableVariable2 cv = fTCModel.makeDeclaredVariableVariable(variableBinding, fCU);
    setConstraintVariable(exception, cv);
    return true;
  }

  @Override
  public void endVisit(StringLiteral node) {
    ITypeBinding typeBinding = node.resolveTypeBinding();
    ImmutableTypeVariable2 cv = fTCModel.makeImmutableTypeVariable(typeBinding, /*no boxing*/ null);
    setConstraintVariable(node, cv);
  }

  @Override
  public void endVisit(NumberLiteral node) {
    ITypeBinding typeBinding = node.resolveTypeBinding();
    ImmutableTypeVariable2 cv = fTCModel.makeImmutableTypeVariable(typeBinding, node);
    setConstraintVariable(node, cv);
  }

  @Override
  public void endVisit(BooleanLiteral node) {
    ITypeBinding typeBinding = node.resolveTypeBinding();
    ImmutableTypeVariable2 cv = fTCModel.makeImmutableTypeVariable(typeBinding, node);
    setConstraintVariable(node, cv);
  }

  @Override
  public void endVisit(CharacterLiteral node) {
    ITypeBinding typeBinding = node.resolveTypeBinding();
    ImmutableTypeVariable2 cv = fTCModel.makeImmutableTypeVariable(typeBinding, node);
    setConstraintVariable(node, cv);
  }

  @Override
  public void endVisit(ThisExpression node) {
    ITypeBinding typeBinding = node.resolveTypeBinding();
    ImmutableTypeVariable2 cv = fTCModel.makeImmutableTypeVariable(typeBinding, /*no boxing*/ null);
    setConstraintVariable(node, cv);
  }

  @Override
  public void endVisit(TypeLiteral node) {
    ITypeBinding typeBinding = node.resolveTypeBinding();
    ImmutableTypeVariable2 cv = fTCModel.makeImmutableTypeVariable(typeBinding, /*no boxing*/ null);
    setConstraintVariable(node, cv);
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    IMethodBinding methodBinding = node.resolveBinding();

    if (methodBinding == null) return; // TODO: emit error?

    int parameterCount = node.parameters().size();
    ConstraintVariable2[] parameterTypeCvs = new ConstraintVariable2[parameterCount];
    for (int i = 0; i < parameterCount; i++) {
      SingleVariableDeclaration paramDecl = (SingleVariableDeclaration) node.parameters().get(i);
      // parameterTypeVariable currently not used, but need to register in order to store source
      // range
      ConstraintVariable2 parameterTypeCv =
          fTCModel.makeDeclaredParameterTypeVariable(methodBinding, i, fCU);
      parameterTypeCvs[i] = parameterTypeCv;
      if (parameterTypeCv == null) continue;

      // creating equals constraint between parameterTypeVariable's elements and the Type's elements
      ConstraintVariable2 typeCv = getConstraintVariable(paramDecl.getType());
      fTCModel.createElementEqualsConstraints(parameterTypeCv, typeCv);

      // TODO: should avoid having a VariableVariable as well as a ParameterVariable for a parameter
      ConstraintVariable2 nameCv = getConstraintVariable(paramDecl.getName());
      fTCModel.createElementEqualsConstraints(parameterTypeCv, nameCv);
    }

    ConstraintVariable2 returnTypeCv = null;
    if (!methodBinding.isConstructor()) {
      // TODO: should only create return type variable if type is generic?
      ConstraintVariable2 returnTypeBindingCv =
          fTCModel.makeDeclaredReturnTypeVariable(methodBinding, fCU);
      if (returnTypeBindingCv != null) {
        returnTypeCv = getConstraintVariable(node.getReturnType2());
        fTCModel.createElementEqualsConstraints(returnTypeBindingCv, returnTypeCv);
      }
    }
    if (MethodChecks.isVirtual(methodBinding)) {
      // TODO: RippleMethod constraints for corner cases: see testCuRippleMethods3, bug 41989
      addConstraintsForOverriding(methodBinding, returnTypeCv, parameterTypeCvs);
    }
  }

  private void addConstraintsForOverriding(
      IMethodBinding methodBinding,
      ConstraintVariable2 returnTypeCv,
      ConstraintVariable2[] parameterTypeCvs) {
    boolean hasParameterElementCvs = false;
    for (int i = 0; i < parameterTypeCvs.length; i++)
      if (parameterTypeCvs[i] != null) hasParameterElementCvs = true;

    if (returnTypeCv == null && !hasParameterElementCvs) return;

    ITypeBinding[] allSuperTypes = Bindings.getAllSuperTypes(methodBinding.getDeclaringClass());
    for (int i = 0; i < allSuperTypes.length; i++) {
      ITypeBinding superType = allSuperTypes[i];
      IMethodBinding superMethod = Bindings.findOverriddenMethodInType(superType, methodBinding);
      if (superMethod == null) continue;

      for (int p = 0; p < parameterTypeCvs.length; p++) {
        if (parameterTypeCvs[p] == null) continue;
        ParameterTypeVariable2 parameterTypeCv = fTCModel.makeParameterTypeVariable(superMethod, p);
        fTCModel.createElementEqualsConstraints(parameterTypeCv, parameterTypeCvs[p]);
      }

      if (returnTypeCv != null) {
        ReturnTypeVariable2 superMethodReturnTypeCv = fTCModel.makeReturnTypeVariable(superMethod);
        fTCModel.createElementEqualsConstraints(superMethodReturnTypeCv, returnTypeCv);
      }
    }
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding methodBinding = node.resolveMethodBinding();
    if (methodBinding == null) return;

    Expression receiver;
    if (JdtFlags.isStatic(methodBinding)) receiver = null;
    else receiver = node.getExpression();

    // TODO: Expression can be null when visiting a non-special method in a subclass of a container
    // type.

    if (isSpecialCloneInvocation(methodBinding, receiver)) {
      ConstraintVariable2 expressionCv = getConstraintVariable(receiver);
      // [retVal] =^= [receiver]:
      setConstraintVariable(node, expressionCv);

    } else if ("getClass".equals(methodBinding.getName())
        && methodBinding.getParameterTypes().length == 0) { // $NON-NLS-1$
      // special case: see JLS3 4.3.2
      ITypeBinding returnType = node.resolveTypeBinding();
      ITypeBinding returnTypeDeclaration = returnType.getTypeDeclaration();
      ParameterizedTypeVariable2 expressionCv =
          fTCModel.makeParameterizedTypeVariable(returnTypeDeclaration);
      setConstraintVariable(node, expressionCv);
      ConstraintVariable2 classTypeVariable =
          fTCModel.getElementVariable(expressionCv, returnTypeDeclaration.getTypeParameters()[0]);

      // type of expression 'e.getClass()' is 'Class<? extends X>' where X is the static type of e
      ITypeBinding capture = returnType.getTypeArguments()[0];
      ITypeBinding wildcard = capture.getWildcard();
      if (wildcard.getBound() == null)
        return; // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=234619

      ImmutableTypeVariable2 wildcardType =
          fTCModel.makeImmutableTypeVariable(wildcard, /*no boxing*/ null);
      fTCModel.createSubtypeConstraint(classTypeVariable, wildcardType);

      //			ITypeBinding bound= wildcard.getBound();
      //			ImmutableTypeVariable2 boundType= fTCModel.makeImmutableTypeVariable(bound,
      // node.getAST());
      //			fTCModel.createSubtypeConstraint(classTypeVariable, boundType);

    } else {
      Map<String, IndependentTypeVariable2> methodTypeVariables =
          createMethodTypeArguments(methodBinding);

      doVisitMethodInvocationReturnType(node, methodBinding, receiver, methodTypeVariables);
      doVisitMethodInvocationArguments(
          methodBinding, node.arguments(), receiver, methodTypeVariables, /*no created type*/ null);
    }
  }

  /** @return a map from type variable key to type variable constraint variable */
  private Map<String, IndependentTypeVariable2> createMethodTypeArguments(
      IMethodBinding methodBinding) {
    ITypeBinding[] methodTypeParameters = methodBinding.getMethodDeclaration().getTypeParameters();
    Map<String, IndependentTypeVariable2> methodTypeVariables;
    if (methodTypeParameters.length == 0) {
      methodTypeVariables = Collections.emptyMap();
    } else {
      methodTypeVariables = new HashMap<String, IndependentTypeVariable2>();
      for (int i = 0; i < methodTypeParameters.length; i++) {
        ITypeBinding methodTypeParameter = methodTypeParameters[i];
        // TODO: typeVariable does not need a type binding - only used in equality constraints
        TypeVariable typeVariable = (TypeVariable) fTCModel.createTType(methodTypeParameter);
        IndependentTypeVariable2 typeVariableCv =
            fTCModel.makeIndependentTypeVariable(typeVariable);
        methodTypeVariables.put(methodTypeParameter.getKey(), typeVariableCv);
      }
    }
    return methodTypeVariables;
  }

  private void doVisitMethodInvocationReturnType(
      MethodInvocation node,
      IMethodBinding methodBinding,
      Expression receiver,
      Map<String, IndependentTypeVariable2> methodTypeVariables) {
    ITypeBinding declaredReturnType = methodBinding.getMethodDeclaration().getReturnType();

    if (declaredReturnType.isPrimitive()) {
      ImmutableTypeVariable2 boxed = fTCModel.makeImmutableTypeVariable(declaredReturnType, node);
      setConstraintVariable(node, boxed);

    } else if (declaredReturnType.isTypeVariable()) {
      ConstraintVariable2 methodTypeVariableCv =
          methodTypeVariables.get(declaredReturnType.getKey());
      if (methodTypeVariableCv != null) {
        // e.g. in Collections: <T ..> T min(Collection<? extends T> coll):
        setConstraintVariable(
            node, methodTypeVariableCv); // TODO: should be [retVal] <= Elem[arg] in this case?

        //			TODO:
        //			} else if (methodBinding.getErasure().getTypeParameters().length == 1 &&
        //					(genericReturnType.isTypeVariable() || genericReturnType.isWildcardType()) &&
        //					methodBinding.getParameterTypes().length == 1 &&
        //					methodBinding.getParameterTypes()[0].getErasure().isGenericType()) {
        //				// e.g. in Collections: <T ..> T min(Collection<? extends T> coll):
        //				TypeConstraintVariable2 argCv= (TypeConstraintVariable2)
        // getConstraintVariable((Expression) node.arguments().get(0));
        //				ConstraintVariable2 elementCv= fTCModel.makeElementVariable(argCv);
        //				// [retVal] =^= Elem[arg]:
        //				setConstraintVariable(node, elementCv); //TODO: should be [retVal] <= Elem[arg]

      } else {
        // TODO: nested generic classes and methods?

        if (receiver == null) // TODO: deal with methods inside generic types
        return;
        // e.g. in List<E>: E get(int index):
        ConstraintVariable2 expressionCv = getConstraintVariable(receiver);
        ConstraintVariable2 elementCv =
            fTCModel.getElementVariable(expressionCv, declaredReturnType);
        // [retVal] =^= Elem[receiver]:
        setConstraintVariable(node, elementCv);
      }

    } else if (declaredReturnType.isParameterizedType()) {
      ConstraintVariable2 returnTypeCv =
          fTCModel.makeParameterizedTypeVariable(declaredReturnType.getTypeDeclaration());
      setConstraintVariable(node, returnTypeCv);
      // e.g. List<E>: Iterator<E> iterator()
      ConstraintVariable2 receiverCv = null;
      if (receiver != null) // TODO: deal with methods inside generic types
      receiverCv = getConstraintVariable(receiver);
      // Elem[retVal] =^= Elem[receiver]
      TType declaredReturnTType = fTCModel.createTType(declaredReturnType);
      fTCModel.createTypeVariablesEqualityConstraints(
          receiverCv, methodTypeVariables, returnTypeCv, declaredReturnTType);

    } else if (declaredReturnType.isArray()) {
      ConstraintVariable2 returnTypeCv = fTCModel.makeArrayTypeVariable(declaredReturnType);
      setConstraintVariable(node, returnTypeCv);
      // e.g. List<E>: <T> T[] toArray(T[] a)
      ConstraintVariable2 receiverCv = null;
      if (receiver != null) { // TODO: deal with methods inside generic types
        receiverCv = getConstraintVariable(receiver);

        // TODO: is this necessary elsewhere?
        fTCModel.setMethodReceiverCV(returnTypeCv, receiverCv);
      }
      // Elem[retVal] =^= Elem[receiver]
      TType declaredReturnTType = fTCModel.createTType(declaredReturnType);
      fTCModel.createTypeVariablesEqualityConstraints(
          receiverCv, methodTypeVariables, returnTypeCv, declaredReturnTType);

    } else {
      ReturnTypeVariable2 returnTypeCv = fTCModel.makeReturnTypeVariable(methodBinding);
      setConstraintVariable(node, returnTypeCv);
    }
  }

  private boolean isSpecialCloneInvocation(IMethodBinding methodBinding, Expression receiver) {
    return fAssumeCloneReturnsSameType
        && "clone".equals(methodBinding.getName()) // $NON-NLS-1$
        && methodBinding.getParameterTypes().length == 0
        && receiver != null
        && receiver.resolveTypeBinding() != methodBinding.getMethodDeclaration().getReturnType();
  }

  private void doVisitMethodInvocationArguments(
      IMethodBinding methodBinding,
      List<Expression> arguments,
      Expression receiver,
      Map<String, IndependentTypeVariable2> methodTypeVariables,
      Type createdType) {
    // TODO: connect generic method type parameters, e.g. <T> void take(T t, List<T> ts)
    ITypeBinding[] declaredParameterTypes =
        methodBinding.getMethodDeclaration().getParameterTypes();
    int lastParamIdx = declaredParameterTypes.length - 1;
    for (int i = 0; i < arguments.size(); i++) {
      Expression arg = arguments.get(i);
      ConstraintVariable2 argCv = getConstraintVariable(arg);
      if (argCv == null) continue;

      TType declaredParameterType;
      int iParam;
      if (!methodBinding.isVarargs() || i < lastParamIdx) {
        iParam = i;
        declaredParameterType = fTCModel.createTType(declaredParameterTypes[iParam]);
      } else { // isVararg() && i >= lastParamIdx
        iParam = lastParamIdx;
        declaredParameterType = fTCModel.createTType(declaredParameterTypes[iParam]);
        if (i == lastParamIdx
            && canAssignToVararg(
                fTCModel.createTType(arg.resolveTypeBinding()),
                (org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType)
                    declaredParameterType)) {
          // OK: argument will not be packed into an array
        } else {
          declaredParameterType =
              ((org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType)
                      declaredParameterType)
                  .getComponentType();
        }
      }

      if (declaredParameterType.isTypeVariable()) {

        ConstraintVariable2 methodTypeVariableCv =
            methodTypeVariables.get(declaredParameterType.getBindingKey());
        if (methodTypeVariableCv != null) {
          // e.g. t in "<T> void take(T t, List<T> ts)"
          fTCModel.createSubtypeConstraint(argCv, methodTypeVariableCv);

        } else {
          if (createdType != null) {
            // e.g. Tuple<T1, T2>: constructor Tuple(T1 t1, T2 t2)
            ConstraintVariable2 createdTypeCv = getConstraintVariable(createdType);
            ConstraintVariable2 elementCv =
                fTCModel.getElementVariable(createdTypeCv, (TypeVariable) declaredParameterType);
            // [arg] <= Elem[createdType]:
            fTCModel.createSubtypeConstraint(argCv, elementCv);
          }
          if (receiver != null) {
            // e.g. "Collection<E>: boolean add(E o)"
            ConstraintVariable2 expressionCv = getConstraintVariable(receiver);
            ConstraintVariable2 elementCv =
                fTCModel.getElementVariable(expressionCv, (TypeVariable) declaredParameterType);

            //	//TypeVariableConstraintVariable2 typeVariableCv=
            // fTCModel.makeTypeVariableVariable(declaredParameterType);
            //				ConstraintVariable2 elementCv= fTCModel.makeElementVariable(expressionCv,
            // typeVariableCv);
            // TODO: Somebody must connect typeVariableCv to corresponding typeVariableCVs of
            // supertypes.
            // - Do only once for binaries.
            // - Do when passing for sources.
            // - Keep a flag in CV whether done?
            // - Do in one pass over all TypeVarCvs at the end?

            // [arg] <= Elem[receiver]:
            fTCModel.createSubtypeConstraint(argCv, elementCv);
          } else {
            // TODO: ???
          }
        }
      } else if (declaredParameterType.isParameterizedType()) {
        TType[] typeArguments = ((ParameterizedType) declaredParameterType).getTypeArguments();
        TypeVariable[] typeParameters =
            ((GenericType) declaredParameterType.getTypeDeclaration()).getTypeParameters();
        for (int ta = 0; ta < typeArguments.length; ta++) {
          TType typeArgument = typeArguments[ta];
          CollectionElementVariable2 argElementCv =
              fTCModel.getElementVariable(argCv, typeParameters[ta]);
          if (typeArgument.isWildcardType()) {
            // Elem[arg] <= Elem[receiver]
            WildcardType wildcardTypeArgument = (WildcardType) typeArgument;
            TType bound = wildcardTypeArgument.getBound();
            if (bound != null && bound.isTypeVariable()) {
              ConstraintVariable2 methodTypeVariableCv =
                  methodTypeVariables.get(bound.getBindingKey());
              if (methodTypeVariableCv != null) {
                // e.g. in Collections: <T ..> T min(Collection<? extends T> coll):
                createWildcardConstraint(wildcardTypeArgument, argElementCv, methodTypeVariableCv);
              } else {
                if (createdType != null) {
                  ConstraintVariable2 createdTypeCv = getConstraintVariable(createdType);
                  CollectionElementVariable2 elementCv =
                      fTCModel.getElementVariable(createdTypeCv, typeParameters[ta]);
                  createWildcardConstraint(wildcardTypeArgument, argElementCv, elementCv);
                }
                if (receiver != null) {
                  // e.g. Collection<E>: boolean addAll(Collection<? extends E> c)
                  ConstraintVariable2 expressionCv = getConstraintVariable(receiver);
                  CollectionElementVariable2 elementCv =
                      fTCModel.getElementVariable(expressionCv, typeParameters[ta]);
                  createWildcardConstraint(wildcardTypeArgument, argElementCv, elementCv);
                } else {
                  // TODO: ???
                }
              }

            } else {
              // TODO
            }

          } else if (typeArgument.isTypeVariable()) {
            ConstraintVariable2 methodTypeVariableCv =
                methodTypeVariables.get(typeArgument.getBindingKey());
            if (methodTypeVariableCv != null) {
              // e.g. in Collections: <T> List<T> synchronizedList(List<T> list)
              fTCModel.createEqualsConstraint(argElementCv, methodTypeVariableCv);
            } else {
              if (createdType != null) {
                ConstraintVariable2 createdTypeCv = getConstraintVariable(createdType);
                ConstraintVariable2 elementCv =
                    fTCModel.getElementVariable(createdTypeCv, (TypeVariable) typeArgument);
                fTCModel.createEqualsConstraint(argElementCv, elementCv);
              }
              if (receiver != null) {
                ConstraintVariable2 expressionCv = getConstraintVariable(receiver);
                ConstraintVariable2 elementCv =
                    fTCModel.getElementVariable(expressionCv, (TypeVariable) typeArgument);
                fTCModel.createEqualsConstraint(argElementCv, elementCv);
              } else {
                // TODO: ???
              }
            }

          } else {
            ImmutableTypeVariable2 typeArgumentCv =
                fTCModel.makeImmutableTypeVariable(typeArgument);
            fTCModel.createEqualsConstraint(argElementCv, typeArgumentCv);
          }
        }

      } else if (declaredParameterType.isArrayType()) {
        // TODO: check methodBinding.isVarargs() !
        TType declaredElementType =
            ((org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType)
                    declaredParameterType)
                .getElementType();
        if (declaredElementType.isTypeVariable()) {
          ConstraintVariable2 methodTypeVariableCv =
              methodTypeVariables.get(declaredElementType.getBindingKey());
          if (methodTypeVariableCv != null) {
            ArrayElementVariable2 argElementCv = fTCModel.getArrayElementVariable(argCv);
            // e.g. in Arrays: <T> List<T> asList(T... a): //<T> List<T> asList(T[] a)
            fTCModel.createEqualsConstraint(argElementCv, methodTypeVariableCv);
          } else {
            // TODO: receiver, createdType
          }
        } else {
          // TODO
        }

      } else { // TODO: not else, but always? Other kinds of type references?
        if (!InferTypeArgumentsTCModel.isAGenericType(declaredParameterType)) continue;
        ParameterTypeVariable2 parameterTypeCv =
            fTCModel.makeParameterTypeVariable(methodBinding, iParam);
        // Elem[param] =^= Elem[arg]
        fTCModel.createElementEqualsConstraints(parameterTypeCv, argCv);
      }
    }
  }

  private boolean canAssignToVararg(
      TType rhs, org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType lhs) {
    return TTypes.canAssignTo(rhs.getErasure(), lhs.getErasure());
  }

  private void createWildcardConstraint(
      WildcardType typeArgument,
      CollectionElementVariable2 argElementCv,
      ConstraintVariable2 paramElementCv) {
    if (typeArgument.isExtendsWildcardType())
      fTCModel.createSubtypeConstraint(argElementCv, paramElementCv);
    else fTCModel.createSubtypeConstraint(paramElementCv, argElementCv);
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    Expression receiver = node.getExpression();
    Type createdType = node.getType();

    ConstraintVariable2 typeCv;
    if (node.getAnonymousClassDeclaration() == null) {
      typeCv = getConstraintVariable(createdType);
    } else {
      typeCv = fTCModel.makeImmutableTypeVariable(createdType.resolveBinding(), null);
      setConstraintVariable(createdType, typeCv);
    }
    setConstraintVariable(node, typeCv);

    IMethodBinding methodBinding = node.resolveConstructorBinding();
    Map<String, IndependentTypeVariable2> methodTypeVariables =
        createMethodTypeArguments(methodBinding);
    List<Expression> arguments = node.arguments();
    doVisitMethodInvocationArguments(
        methodBinding, arguments, receiver, methodTypeVariables, createdType);
  }

  @Override
  public void endVisit(ArrayCreation node) {
    ArrayType arrayType = node.getType();
    TypeVariable2 arrayTypeCv = (TypeVariable2) getConstraintVariable(arrayType);
    if (arrayTypeCv == null) return;
    setConstraintVariable(node, arrayTypeCv);
    // TODO: constraints for array initializer?
  }

  @Override
  public void endVisit(ReturnStatement node) {
    Expression expression = node.getExpression();
    if (expression == null) return;
    ConstraintVariable2 expressionCv = getConstraintVariable(expression);
    if (expressionCv == null) return;

    MethodDeclaration methodDeclaration =
        (MethodDeclaration) ASTNodes.getParent(node, ASTNode.METHOD_DECLARATION);
    if (methodDeclaration == null) return;
    IMethodBinding methodBinding = methodDeclaration.resolveBinding();
    if (methodBinding == null) return;
    ReturnTypeVariable2 returnTypeCv = fTCModel.makeReturnTypeVariable(methodBinding);
    if (returnTypeCv == null) return;

    fTCModel.createElementEqualsConstraints(returnTypeCv, expressionCv);
  }

  @Override
  public void endVisit(VariableDeclarationExpression node) {
    // Constrain the types of the child VariableDeclarationFragments to be equal to one
    // another, since the initializers in a 'for' statement can only have one type.
    // Pairwise constraints between adjacent variables is enough.
    Type type = node.getType();
    ConstraintVariable2 typeCv = getConstraintVariable(type);
    if (typeCv == null) return;

    setConstraintVariable(node, typeCv);

    List<VariableDeclarationFragment> fragments = node.fragments();
    for (Iterator<VariableDeclarationFragment> iter = fragments.iterator(); iter.hasNext(); ) {
      VariableDeclarationFragment fragment = iter.next();
      ConstraintVariable2 fragmentCv = getConstraintVariable(fragment);
      fTCModel.createElementEqualsConstraints(typeCv, fragmentCv);
    }
  }

  @Override
  public void endVisit(VariableDeclarationStatement node) {
    // TODO: in principle, no need to tie the VariableDeclarationFragments together.
    // The VariableDeclarationExpression can be split up when fragments get different types.
    // Warning: still need to connect fragments with type!
    endVisitFieldVariableDeclaration(node.getType(), node.fragments());
  }

  @Override
  public void endVisit(FieldDeclaration node) {
    // TODO: in principle, no need to tie the VariableDeclarationFragments together.
    // The FieldDeclaration can be split up when fragments get different types.
    // Warning: still need to connect fragments with type!
    endVisitFieldVariableDeclaration(node.getType(), node.fragments());
  }

  private void endVisitFieldVariableDeclaration(Type type, List<VariableDeclarationFragment> list) {
    ConstraintVariable2 typeCv = getConstraintVariable(type);
    if (typeCv == null) return;

    for (Iterator<VariableDeclarationFragment> iter = list.iterator(); iter.hasNext(); ) {
      VariableDeclarationFragment fragment = iter.next();
      ConstraintVariable2 fragmentCv = getConstraintVariable(fragment);
      fTCModel.createElementEqualsConstraints(typeCv, fragmentCv);
    }
  }

  @Override
  public void endVisit(SingleVariableDeclaration node) {
    // used for formal method parameters and catch clauses
    // TODO: extra dimensions?

    //		ConstraintVariable2 typeCv= getConstraintVariable(node.getType()); //TODO: who needs this?

    //		ConstraintVariable2 nameCv;
    //		switch (node.getParent().getNodeType()) {
    //			case ASTNode.METHOD_DECLARATION :
    //				MethodDeclaration parent= (MethodDeclaration) node.getParent();
    //				int index= parent.parameters().indexOf(node);
    //				nameCv= fTCFactory.makeParameterTypeVariable(parent.resolveBinding(), index,
    // node.getType());
    //				//store source range even if variable not used in constraint here. TODO: move to
    // visit(MethodDeclaration)?
    //				break;
    //			case ASTNode.CATCH_CLAUSE :
    //				nameCv= fTCFactory.makeVariableVariable(node.resolveBinding());
    //
    //				break;
    //			default:
    //				unexpectedNode(node.getParent());
    //		}
    //		setConstraintVariable(node, nameCv);

    // TODO: Move this into visit(SimpleName) or leave it here?
    //		ExpressionVariable2 name= fTCFactory.makeExpressionVariable(node.getName());
    //		TypeVariable2 type= fTCFactory.makeTypeVariable(node.getType());
    //		ITypeConstraint2[] nameEqualsType= fTCFactory.createEqualsConstraint(name, type);
    //		addConstraints(nameEqualsType);

    // TODO: When can a SingleVariableDeclaration have an initializer? Never up to Java 1.5?
    Expression initializer = node.getInitializer();
    if (initializer == null) return;

    //		ConstraintVariable2 initializerCv= getConstraintVariable(initializer);
    //		ConstraintVariable2 nameCv= getConstraintVariable(node);
    // TODO: check: property has been set in visit(CatchClause), visit(MethodDeclaration),
    // visit(EnhancedForStatament)
    // fTCFactory.createSubtypeConstraint(initializerCv, nameCv); //TODO: not for augment raw
    // container clients
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    VariableVariable2 cv = fTCModel.makeDeclaredVariableVariable(node.resolveBinding(), fCU);
    if (cv == null) return;

    setConstraintVariable(node, cv);

    // TODO: prune unused CV for local variables (but not fields)

    Expression initializer = node.getInitializer();
    if (initializer == null) return;

    ConstraintVariable2 initializerCv = getConstraintVariable(initializer);
    if (initializerCv == null) return;

    fTCModel.createElementEqualsConstraints(cv, initializerCv);

    // name= initializer  -->  [initializer] <= [name]
    //		if (initializerCv instanceof CollectionElementVariable2)
    //			fTCModel.createSubtypeConstraint(initializerCv, cv);
  }

  // --------- private helpers ----------------//

  public InferTypeArgumentsTCModel getTCModel() {
    return fTCModel;
  }

  /**
   * @param node the ASTNode
   * @return the {@link ConstraintVariable2} associated with the node, or <code>null</code>
   */
  protected static ConstraintVariable2 getConstraintVariable(ASTNode node) {
    return (ConstraintVariable2) node.getProperty(CV_PROP);
  }

  /**
   * @param node the ASTNode
   * @param constraintVariable the {@link ConstraintVariable2} to be associated with node
   */
  protected static void setConstraintVariable(
      ASTNode node, ConstraintVariable2 constraintVariable) {
    node.setProperty(CV_PROP, constraintVariable);
  }

  //	private void logUnexpectedNode(ASTNode node, String msg) {
  //		String message= msg == null ? "" : msg + ":\n";  //$NON-NLS-1$//$NON-NLS-2$
  //		if (node == null) {
  //			message+= "ASTNode was not expected to be null"; //$NON-NLS-1$
  //		} else {
  //			message+= "Found unexpected node (type: " + node.getNodeType() + "):\n" + node.toString();
  // //$NON-NLS-1$ //$NON-NLS-2$
  //		}
  //		JavaPlugin.log(new Exception(message).fillInStackTrace());
  //	}

}

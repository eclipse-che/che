/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Benjamin Muskalla
 * <bmuskalla@eclipsesource.com> - [quick fix] proposes wrong cast from Object to primitive int -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100593 Benjamin Muskalla
 * <bmuskalla@eclipsesource.com> - [quick fix] "Add exceptions to..." quickfix does nothing -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=107924
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CastCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeMethodSignatureProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeMethodSignatureProposal.ChangeDescription;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeMethodSignatureProposal.InsertDescription;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeMethodSignatureProposal.RemoveDescription;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ImplementInterfaceProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewVariableCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.TypeChangeCorrectionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.swt.graphics.Image;

public class TypeMismatchSubProcessor {

  private TypeMismatchSubProcessor() {}

  public static void addTypeMismatchProposals(
      IInvocationContext context, IProblemLocation problem, Collection<ICommandAccess> proposals)
      throws CoreException {
    String[] args = problem.getProblemArguments();
    if (args.length != 2) {
      return;
    }

    ICompilationUnit cu = context.getCompilationUnit();

    CompilationUnit astRoot = context.getASTRoot();
    AST ast = astRoot.getAST();

    ASTNode selectedNode = problem.getCoveredNode(astRoot);
    if (!(selectedNode instanceof Expression)) {
      return;
    }
    Expression nodeToCast = (Expression) selectedNode;
    Name receiverNode = null;
    ITypeBinding castTypeBinding = null;

    int parentNodeType = selectedNode.getParent().getNodeType();
    if (parentNodeType == ASTNode.ASSIGNMENT) {
      Assignment assign = (Assignment) selectedNode.getParent();
      Expression leftHandSide = assign.getLeftHandSide();
      if (selectedNode.equals(leftHandSide)) {
        nodeToCast = assign.getRightHandSide();
      }
      castTypeBinding = assign.getLeftHandSide().resolveTypeBinding();
      if (leftHandSide instanceof Name) {
        receiverNode = (Name) leftHandSide;
      } else if (leftHandSide instanceof FieldAccess) {
        receiverNode = ((FieldAccess) leftHandSide).getName();
      }
    } else if (parentNodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
      VariableDeclarationFragment frag = (VariableDeclarationFragment) selectedNode.getParent();
      if (selectedNode.equals(frag.getName()) || selectedNode.equals(frag.getInitializer())) {
        nodeToCast = frag.getInitializer();
        castTypeBinding = ASTNodes.getType(frag).resolveBinding();
        receiverNode = frag.getName();
      }
    } else if (parentNodeType == ASTNode.MEMBER_VALUE_PAIR) {
      receiverNode = ((MemberValuePair) selectedNode.getParent()).getName();
      castTypeBinding = ASTResolving.guessBindingForReference(nodeToCast);
    } else if (parentNodeType == ASTNode.SINGLE_MEMBER_ANNOTATION) {
      receiverNode =
          ((SingleMemberAnnotation) selectedNode.getParent()).getTypeName(); // use the type name
      castTypeBinding = ASTResolving.guessBindingForReference(nodeToCast);
    } else {
      // try to find the binding corresponding to 'castTypeName'
      castTypeBinding = ASTResolving.guessBindingForReference(nodeToCast);
    }
    if (castTypeBinding == null) {
      return;
    }

    ITypeBinding currBinding = nodeToCast.resolveTypeBinding();

    if (!(nodeToCast instanceof ArrayInitializer)) {
      ITypeBinding castFixType = null;
      if (currBinding == null
          || castTypeBinding.isCastCompatible(currBinding)
          || nodeToCast instanceof CastExpression) {
        castFixType = castTypeBinding;
      } else if (JavaModelUtil.is50OrHigher(cu.getJavaProject())) {
        ITypeBinding boxUnboxedTypeBinding = boxUnboxPrimitives(castTypeBinding, currBinding, ast);
        if (boxUnboxedTypeBinding != castTypeBinding
            && boxUnboxedTypeBinding.isCastCompatible(currBinding)) {
          castFixType = boxUnboxedTypeBinding;
        }
      }
      if (castFixType != null) {
        proposals.add(
            createCastProposal(context, castFixType, nodeToCast, IProposalRelevance.CREATE_CAST));
      }
    }

    boolean nullOrVoid = currBinding == null || "void".equals(currBinding.getName()); // $NON-NLS-1$

    // change method return statement to actual type
    if (!nullOrVoid && parentNodeType == ASTNode.RETURN_STATEMENT) {
      BodyDeclaration decl = ASTResolving.findParentBodyDeclaration(selectedNode);
      if (decl instanceof MethodDeclaration) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) decl;

        currBinding = Bindings.normalizeTypeBinding(currBinding);
        if (currBinding == null) {
          currBinding = ast.resolveWellKnownType("java.lang.Object"); // $NON-NLS-1$
        }
        if (currBinding.isWildcardType()) {
          currBinding = ASTResolving.normalizeWildcardType(currBinding, true, ast);
        }

        ASTRewrite rewrite = ASTRewrite.create(ast);

        String label =
            Messages.format(
                CorrectionMessages.TypeMismatchSubProcessor_changereturntype_description,
                BasicElementLabels.getJavaElementName(currBinding.getName()));
        Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
        LinkedCorrectionProposal proposal =
            new LinkedCorrectionProposal(
                label, cu, rewrite, IProposalRelevance.CHANGE_METHOD_RETURN_TYPE, image);

        ImportRewrite imports = proposal.createImportRewrite(astRoot);
        ImportRewriteContext importRewriteContext =
            new ContextSensitiveImportRewriteContext(decl, imports);

        Type newReturnType = imports.addImport(currBinding, ast, importRewriteContext);
        rewrite.replace(methodDeclaration.getReturnType2(), newReturnType, null);

        String returnKey = "return"; // $NON-NLS-1$
        proposal.addLinkedPosition(rewrite.track(newReturnType), true, returnKey);
        ITypeBinding[] typeSuggestions = ASTResolving.getRelaxingTypes(ast, currBinding);
        for (int i = 0; i < typeSuggestions.length; i++) {
          proposal.addLinkedPositionProposal(returnKey, typeSuggestions[i]);
        }
        proposals.add(proposal);
      }
    }

    if (!nullOrVoid && receiverNode != null) {
      currBinding = Bindings.normalizeTypeBinding(currBinding);
      if (currBinding == null) {
        currBinding = ast.resolveWellKnownType("java.lang.Object"); // $NON-NLS-1$
      }
      if (currBinding.isWildcardType()) {
        currBinding = ASTResolving.normalizeWildcardType(currBinding, true, ast);
      }
      addChangeSenderTypeProposals(
          context,
          receiverNode,
          currBinding,
          true,
          IProposalRelevance.CHANGE_TYPE_OF_RECEIVER_NODE,
          proposals);
    }

    addChangeSenderTypeProposals(
        context,
        nodeToCast,
        castTypeBinding,
        false,
        IProposalRelevance.CHANGE_TYPE_OF_NODE_TO_CAST,
        proposals);

    if (castTypeBinding == ast.resolveWellKnownType("boolean")
        && currBinding != null
        && !currBinding.isPrimitive()
        && !Bindings.isVoidType(currBinding)) { // $NON-NLS-1$
      String label = CorrectionMessages.TypeMismatchSubProcessor_insertnullcheck_description;
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
      ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());

      InfixExpression expression = ast.newInfixExpression();
      expression.setLeftOperand((Expression) rewrite.createMoveTarget(nodeToCast));
      expression.setRightOperand(ast.newNullLiteral());
      expression.setOperator(InfixExpression.Operator.NOT_EQUALS);
      rewrite.replace(nodeToCast, expression, null);

      proposals.add(
          new ASTRewriteCorrectionProposal(
              label,
              context.getCompilationUnit(),
              rewrite,
              IProposalRelevance.INSERT_NULL_CHECK,
              image));
    }
  }

  public static ITypeBinding boxUnboxPrimitives(
      ITypeBinding castType, ITypeBinding toCast, AST ast) {
    /*
     * e.g:
     * 	void m(toCast var) {
     * 		castType i= var;
     * 	}
     */
    if (castType.isPrimitive() && !toCast.isPrimitive()) {
      return Bindings.getBoxedTypeBinding(castType, ast);
    } else if (!castType.isPrimitive() && toCast.isPrimitive()) {
      return Bindings.getUnboxedTypeBinding(castType, ast);
    } else {
      return castType;
    }
  }

  public static void addChangeSenderTypeProposals(
      IInvocationContext context,
      Expression nodeToCast,
      ITypeBinding castTypeBinding,
      boolean isAssignedNode,
      int relevance,
      Collection<ICommandAccess> proposals)
      throws JavaModelException {
    IBinding callerBinding = Bindings.resolveExpressionBinding(nodeToCast, false);

    ICompilationUnit cu = context.getCompilationUnit();
    CompilationUnit astRoot = context.getASTRoot();

    ICompilationUnit targetCu = null;
    ITypeBinding declaringType = null;
    IBinding callerBindingDecl = callerBinding;
    if (callerBinding instanceof IVariableBinding) {
      IVariableBinding variableBinding = (IVariableBinding) callerBinding;

      if (variableBinding.isEnumConstant()) {
        return;
      }
      if (!variableBinding.isField()) {
        targetCu = cu;
      } else {
        callerBindingDecl = variableBinding.getVariableDeclaration();
        ITypeBinding declaringClass = variableBinding.getDeclaringClass();
        if (declaringClass == null) {
          return; // array length
        }
        declaringType = declaringClass.getTypeDeclaration();
      }
    } else if (callerBinding instanceof IMethodBinding) {
      IMethodBinding methodBinding = (IMethodBinding) callerBinding;
      if (!methodBinding.isConstructor()) {
        declaringType = methodBinding.getDeclaringClass().getTypeDeclaration();
        callerBindingDecl = methodBinding.getMethodDeclaration();
      }
    } else if (callerBinding instanceof ITypeBinding
        && nodeToCast.getLocationInParent() == SingleMemberAnnotation.TYPE_NAME_PROPERTY) {
      declaringType = (ITypeBinding) callerBinding;
      callerBindingDecl =
          Bindings.findMethodInType(declaringType, "value", (String[]) null); // $NON-NLS-1$
      if (callerBindingDecl == null) {
        return;
      }
    }

    if (declaringType != null && declaringType.isFromSource()) {
      targetCu = ASTResolving.findCompilationUnitForBinding(cu, astRoot, declaringType);
    }
    if (targetCu != null
        && ASTResolving.isUseableTypeInContext(castTypeBinding, callerBindingDecl, false)) {
      proposals.add(
          new TypeChangeCorrectionProposal(
              targetCu, callerBindingDecl, astRoot, castTypeBinding, isAssignedNode, relevance));
    }

    // add interface to resulting type
    if (!isAssignedNode) {
      ITypeBinding nodeType = nodeToCast.resolveTypeBinding();
      if (castTypeBinding.isInterface()
          && nodeType != null
          && nodeType.isClass()
          && !nodeType.isAnonymous()
          && nodeType.isFromSource()) {
        ITypeBinding typeDecl = nodeType.getTypeDeclaration();
        ICompilationUnit nodeCu = ASTResolving.findCompilationUnitForBinding(cu, astRoot, typeDecl);
        if (nodeCu != null
            && ASTResolving.isUseableTypeInContext(castTypeBinding, typeDecl, true)) {
          proposals.add(
              new ImplementInterfaceProposal(
                  nodeCu, typeDecl, astRoot, castTypeBinding, relevance - 1));
        }
      }
    }
  }

  public static ASTRewriteCorrectionProposal createCastProposal(
      IInvocationContext context,
      ITypeBinding castTypeBinding,
      Expression nodeToCast,
      int relevance) {
    ICompilationUnit cu = context.getCompilationUnit();

    String label;
    String castType =
        BindingLabelProvider.getBindingLabel(castTypeBinding, JavaElementLabels.ALL_DEFAULT);
    if (nodeToCast.getNodeType() == ASTNode.CAST_EXPRESSION) {
      label =
          Messages.format(
              CorrectionMessages.TypeMismatchSubProcessor_changecast_description, castType);
    } else {
      label =
          Messages.format(
              CorrectionMessages.TypeMismatchSubProcessor_addcast_description, castType);
    }
    return new CastCorrectionProposal(label, cu, nodeToCast, castTypeBinding, relevance);
  }

  public static void addIncompatibleReturnTypeProposals(
      IInvocationContext context, IProblemLocation problem, Collection<ICommandAccess> proposals)
      throws JavaModelException {
    CompilationUnit astRoot = context.getASTRoot();
    ASTNode selectedNode = problem.getCoveringNode(astRoot);
    if (selectedNode == null) {
      return;
    }
    MethodDeclaration decl = ASTResolving.findParentMethodDeclaration(selectedNode);
    if (decl == null) {
      return;
    }
    IMethodBinding methodDeclBinding = decl.resolveBinding();
    if (methodDeclBinding == null) {
      return;
    }

    ITypeBinding returnType = methodDeclBinding.getReturnType();
    IMethodBinding overridden = Bindings.findOverriddenMethod(methodDeclBinding, false);
    if (overridden == null || overridden.getReturnType() == returnType) {
      return;
    }

    ICompilationUnit cu = context.getCompilationUnit();
    IMethodBinding methodDecl = methodDeclBinding.getMethodDeclaration();
    ITypeBinding overriddenReturnType = overridden.getReturnType();
    if (!JavaModelUtil.is50OrHigher(context.getCompilationUnit().getJavaProject())) {
      overriddenReturnType = overriddenReturnType.getErasure();
    }
    proposals.add(
        new TypeChangeCorrectionProposal(
            cu,
            methodDecl,
            astRoot,
            overriddenReturnType,
            false,
            IProposalRelevance.CHANGE_RETURN_TYPE));

    ICompilationUnit targetCu = cu;

    IMethodBinding overriddenDecl = overridden.getMethodDeclaration();
    ITypeBinding overridenDeclType = overriddenDecl.getDeclaringClass();

    if (overridenDeclType.isFromSource()) {
      targetCu = ASTResolving.findCompilationUnitForBinding(cu, astRoot, overridenDeclType);
      if (targetCu != null
          && ASTResolving.isUseableTypeInContext(returnType, overriddenDecl, false)) {
        TypeChangeCorrectionProposal proposal =
            new TypeChangeCorrectionProposal(
                targetCu,
                overriddenDecl,
                astRoot,
                returnType,
                false,
                IProposalRelevance.CHANGE_RETURN_TYPE_OF_OVERRIDDEN);
        if (overridenDeclType.isInterface()) {
          proposal.setDisplayName(
              Messages.format(
                  CorrectionMessages.TypeMismatchSubProcessor_changereturnofimplemented_description,
                  BasicElementLabels.getJavaElementName(overriddenDecl.getName())));
        } else {
          proposal.setDisplayName(
              Messages.format(
                  CorrectionMessages.TypeMismatchSubProcessor_changereturnofoverridden_description,
                  BasicElementLabels.getJavaElementName(overriddenDecl.getName())));
        }
        proposals.add(proposal);
      }
    }
  }

  public static void addIncompatibleThrowsProposals(
      IInvocationContext context, IProblemLocation problem, Collection<ICommandAccess> proposals)
      throws JavaModelException {
    CompilationUnit astRoot = context.getASTRoot();
    ASTNode selectedNode = problem.getCoveringNode(astRoot);
    if (!(selectedNode instanceof MethodDeclaration)) {
      return;
    }
    MethodDeclaration decl = (MethodDeclaration) selectedNode;
    IMethodBinding methodDeclBinding = decl.resolveBinding();
    if (methodDeclBinding == null) {
      return;
    }

    IMethodBinding overridden = Bindings.findOverriddenMethod(methodDeclBinding, false);
    if (overridden == null) {
      return;
    }

    ICompilationUnit cu = context.getCompilationUnit();

    ITypeBinding[] methodExceptions = methodDeclBinding.getExceptionTypes();
    ITypeBinding[] definedExceptions = overridden.getExceptionTypes();

    ArrayList<ITypeBinding> undeclaredExceptions = new ArrayList<ITypeBinding>();
    {
      ChangeDescription[] changes = new ChangeDescription[methodExceptions.length];

      for (int i = 0; i < methodExceptions.length; i++) {
        if (!isDeclaredException(methodExceptions[i], definedExceptions)) {
          changes[i] = new RemoveDescription();
          undeclaredExceptions.add(methodExceptions[i]);
        }
      }
      String label =
          Messages.format(
              CorrectionMessages.TypeMismatchSubProcessor_removeexceptions_description,
              BasicElementLabels.getJavaElementName(methodDeclBinding.getName()));
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);
      proposals.add(
          new ChangeMethodSignatureProposal(
              label,
              cu,
              astRoot,
              methodDeclBinding,
              null,
              changes,
              IProposalRelevance.REMOVE_EXCEPTIONS,
              image));
    }

    ITypeBinding declaringType = overridden.getDeclaringClass();
    ICompilationUnit targetCu = null;
    if (declaringType.isFromSource()) {
      targetCu = ASTResolving.findCompilationUnitForBinding(cu, astRoot, declaringType);
    }
    if (targetCu != null) {
      ChangeDescription[] changes =
          new ChangeDescription[definedExceptions.length + undeclaredExceptions.size()];

      for (int i = 0; i < undeclaredExceptions.size(); i++) {
        changes[i + definedExceptions.length] =
            new InsertDescription(undeclaredExceptions.get(i), ""); // $NON-NLS-1$
      }
      IMethodBinding overriddenDecl = overridden.getMethodDeclaration();
      String[] args = {
        BasicElementLabels.getJavaElementName(declaringType.getName()),
        BasicElementLabels.getJavaElementName(overridden.getName())
      };
      String label =
          Messages.format(
              CorrectionMessages.TypeMismatchSubProcessor_addexceptions_description, args);
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_ADD);
      proposals.add(
          new ChangeMethodSignatureProposal(
              label,
              targetCu,
              astRoot,
              overriddenDecl,
              null,
              changes,
              IProposalRelevance.ADD_EXCEPTIONS,
              image));
    }
  }

  private static boolean isDeclaredException(ITypeBinding curr, ITypeBinding[] declared) {
    for (int i = 0; i < declared.length; i++) {
      if (Bindings.isSuperType(declared[i], curr)) {
        return true;
      }
    }
    return false;
  }

  public static void addTypeMismatchInForEachProposals(
      IInvocationContext context, IProblemLocation problem, Collection<ICommandAccess> proposals) {
    CompilationUnit astRoot = context.getASTRoot();
    ASTNode selectedNode = problem.getCoveringNode(astRoot);
    if (selectedNode == null
        || selectedNode.getLocationInParent() != EnhancedForStatement.EXPRESSION_PROPERTY) {
      return;
    }
    EnhancedForStatement forStatement = (EnhancedForStatement) selectedNode.getParent();

    ITypeBinding expressionBinding = forStatement.getExpression().resolveTypeBinding();
    if (expressionBinding == null) {
      return;
    }

    ITypeBinding expectedBinding;
    if (expressionBinding.isArray()) {
      expectedBinding = expressionBinding.getComponentType();
    } else {
      IMethodBinding iteratorMethod =
          Bindings.findMethodInHierarchy(
              expressionBinding, "iterator", new String[0]); // $NON-NLS-1$
      if (iteratorMethod == null) {
        return;
      }
      ITypeBinding[] typeArguments = iteratorMethod.getReturnType().getTypeArguments();
      if (typeArguments.length != 1) {
        return;
      }
      expectedBinding = typeArguments[0];
    }
    AST ast = astRoot.getAST();
    expectedBinding = Bindings.normalizeForDeclarationUse(expectedBinding, ast);

    SingleVariableDeclaration parameter = forStatement.getParameter();

    ICompilationUnit cu = context.getCompilationUnit();
    if (parameter.getName().getLength() == 0) {
      SimpleName simpleName = null;
      if (parameter.getType() instanceof SimpleType) {
        SimpleType type = (SimpleType) parameter.getType();
        if (type.getName() instanceof SimpleName) {
          simpleName = (SimpleName) type.getName();
        }
      } else if (parameter.getType() instanceof NameQualifiedType) {
        simpleName = ((NameQualifiedType) parameter.getType()).getName();
      }
      if (simpleName != null) {
        String name = simpleName.getIdentifier();
        int relevance = StubUtility.hasLocalVariableName(cu.getJavaProject(), name) ? 10 : 7;
        String label =
            Messages.format(
                CorrectionMessages.TypeMismatchSubProcessor_create_loop_variable_description,
                BasicElementLabels.getJavaElementName(name));
        Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);

        proposals.add(
            new NewVariableCorrectionProposal(
                label,
                cu,
                NewVariableCorrectionProposal.LOCAL,
                simpleName,
                null,
                relevance,
                image));
        return;
      }
    }

    String label =
        Messages.format(
            CorrectionMessages.TypeMismatchSubProcessor_incompatible_for_each_type_description,
            new String[] {
              BasicElementLabels.getJavaElementName(parameter.getName().getIdentifier()),
              BindingLabelProvider.getBindingLabel(
                  expectedBinding, BindingLabelProvider.DEFAULT_TEXTFLAGS)
            });
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewrite rewrite = ASTRewrite.create(ast);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label, cu, rewrite, IProposalRelevance.INCOMPATIBLE_FOREACH_TYPE, image);

    ImportRewrite importRewrite = proposal.createImportRewrite(astRoot);
    ImportRewriteContext importRewriteContext =
        new ContextSensitiveImportRewriteContext(
            ASTResolving.findParentBodyDeclaration(selectedNode), importRewrite);
    Type newType = importRewrite.addImport(expectedBinding, ast, importRewriteContext);
    rewrite.replace(parameter.getType(), newType, null);

    proposals.add(proposal);
  }
}

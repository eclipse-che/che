/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.refactoring.sef.SelfEncapsulateFieldRefactoring;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.graphics.Image;

public class GetterSetterCorrectionSubProcessor {

  public static final String SELF_ENCAPSULATE_FIELD_ID =
      "org.eclipse.jdt.ui.correction.encapsulateField.assist"; // $NON-NLS-1$

  private static class ProposalParameter {
    public final boolean useSuper;
    public final ICompilationUnit compilationUnit;
    public final ASTRewrite astRewrite;
    public final Expression accessNode;
    public final Expression qualifier;
    public final IVariableBinding variableBinding;

    public ProposalParameter(
        boolean useSuper,
        ICompilationUnit compilationUnit,
        ASTRewrite rewrite,
        Expression accessNode,
        Expression qualifier,
        IVariableBinding variableBinding) {
      this.useSuper = useSuper;
      this.compilationUnit = compilationUnit;
      this.astRewrite = rewrite;
      this.accessNode = accessNode;
      this.qualifier = qualifier;
      this.variableBinding = variableBinding;
    }
  }

  public static class SelfEncapsulateFieldProposal
      extends ChangeCorrectionProposal { // public for tests

    private IField fField;
    private boolean fNoDialog;

    public SelfEncapsulateFieldProposal(int relevance, IField field) {
      super(
          getDescription(field),
          null,
          relevance,
          JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
      fField = field;
      fNoDialog = false;
      setCommandId(SELF_ENCAPSULATE_FIELD_ID);
    }

    public IField getField() {
      return fField;
    }

    public void setNoDialog(boolean noDialog) {
      fNoDialog = noDialog;
    }

    public TextFileChange getChange(IFile file) throws CoreException {
      final SelfEncapsulateFieldRefactoring refactoring =
          new SelfEncapsulateFieldRefactoring(fField);
      refactoring.setVisibility(Flags.AccPublic);
      refactoring.setConsiderVisibility(
          false); // private field references are just searched in local file
      refactoring.checkInitialConditions(new NullProgressMonitor());
      refactoring.checkFinalConditions(new NullProgressMonitor());
      Change createdChange = refactoring.createChange(new NullProgressMonitor());
      if (createdChange instanceof CompositeChange) {
        Change[] children = ((CompositeChange) createdChange).getChildren();
        for (int i = 0; i < children.length; i++) {
          Change curr = children[i];
          if (curr instanceof TextFileChange && ((TextFileChange) curr).getFile().equals(file)) {
            return (TextFileChange) curr;
          }
        }
      }
      return null;
    }

    private static String getDescription(IField field) {
      return Messages.format(
          CorrectionMessages
              .GetterSetterCorrectionSubProcessor_creategetterunsingencapsulatefield_description,
          BasicElementLabels.getJavaElementName(field.getElementName()));
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension5#getAdditionalProposalInfo(org.eclipse.core.runtime
     * .IProgressMonitor)
     * @since 3.5
     */
    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
      return CorrectionMessages.GetterSetterCorrectionSubProcessor_additional_info;
    }

    @Override
    public void apply(IDocument document) {
      try {
        final SelfEncapsulateFieldRefactoring refactoring =
            new SelfEncapsulateFieldRefactoring(fField);
        refactoring.setVisibility(Flags.AccPublic);
        refactoring.setConsiderVisibility(
            false); // private field references are just searched in local file
        //				if (fNoDialog) {
        //					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        //					final RefactoringExecutionHelper helper =
        //							new RefactoringExecutionHelper(refactoring, RefactoringStatus.ERROR,
        // RefactoringSaveHelper.SAVE_REFACTORING,
        //														   JavaPlugin.getActiveWorkbenchShell(), window);
        //					if (Display.getCurrent() != null) {
        //						try {
        //							helper.perform(false, false);
        //						} catch (InterruptedException e) {
        //							JavaPlugin.log(e);
        //						} catch (InvocationTargetException e) {
        //							JavaPlugin.log(e);
        //						}
        //					} else {
        //						Display.getDefault().syncExec(new Runnable() {
        //							public void run() {
        //								try {
        //									helper.perform(false, false);
        //								} catch (InterruptedException e) {
        //									JavaPlugin.log(e);
        //								} catch (InvocationTargetException e) {
        //									JavaPlugin.log(e);
        //								}
        //							}
        //						});
        //					}
        //				} else {
        //					new RefactoringStarter().activate(new SelfEncapsulateFieldWizard(refactoring),
        // JavaPlugin.getActiveWorkbenchShell(),
        //													  "",
        //													  RefactoringSaveHelper.SAVE_REFACTORING); //$NON-NLS-1$
        //				}
      } catch (JavaModelException e) {
        //				ExceptionHandler.handle(e,
        // CorrectionMessages.GetterSetterCorrectionSubProcessor_encapsulate_field_error_title,
        //
        //	CorrectionMessages.GetterSetterCorrectionSubProcessor_encapsulate_field_error_message);
      }
    }
  }

  /**
   * Used by quick assist
   *
   * @param context the invocation context
   * @param coveringNode the covering node
   * @param locations the problems at the corrent location
   * @param resultingCollections the resulting proposals
   * @return <code>true</code> if the quick assist is applicable at this offset
   */
  public static boolean addGetterSetterProposal(
      IInvocationContext context,
      ASTNode coveringNode,
      IProblemLocation[] locations,
      ArrayList<ICommandAccess> resultingCollections) {
    if (locations != null) {
      for (int i = 0; i < locations.length; i++) {
        int problemId = locations[i].getProblemId();
        if (problemId == IProblem.UnusedPrivateField) return false;
        if (problemId == IProblem.UnqualifiedFieldAccess) return false;
      }
    }
    return addGetterSetterProposal(
        context, coveringNode, resultingCollections, IProposalRelevance.GETTER_SETTER_QUICK_ASSIST);
  }

  public static void addGetterSetterProposal(
      IInvocationContext context,
      IProblemLocation location,
      Collection<ICommandAccess> proposals,
      int relevance) {
    addGetterSetterProposal(
        context, location.getCoveringNode(context.getASTRoot()), proposals, relevance);
  }

  private static boolean addGetterSetterProposal(
      IInvocationContext context,
      ASTNode coveringNode,
      Collection<ICommandAccess> proposals,
      int relevance) {
    if (!(coveringNode instanceof SimpleName)) {
      return false;
    }
    SimpleName sn = (SimpleName) coveringNode;

    IBinding binding = sn.resolveBinding();
    if (!(binding instanceof IVariableBinding)) return false;
    IVariableBinding variableBinding = (IVariableBinding) binding;
    if (!variableBinding.isField()) return false;

    if (proposals == null) return true;

    ChangeCorrectionProposal proposal =
        getProposal(context.getCompilationUnit(), sn, variableBinding, relevance);
    if (proposal != null) proposals.add(proposal);
    return true;
  }

  private static ChangeCorrectionProposal getProposal(
      ICompilationUnit cu, SimpleName sn, IVariableBinding variableBinding, int relevance) {
    Expression accessNode = sn;
    Expression qualifier = null;
    boolean useSuper = false;

    ASTNode parent = sn.getParent();
    switch (parent.getNodeType()) {
      case ASTNode.QUALIFIED_NAME:
        accessNode = (Expression) parent;
        qualifier = ((QualifiedName) parent).getQualifier();
        break;
      case ASTNode.SUPER_FIELD_ACCESS:
        accessNode = (Expression) parent;
        qualifier = ((SuperFieldAccess) parent).getQualifier();
        useSuper = true;
        break;
    }
    ASTRewrite rewrite = ASTRewrite.create(sn.getAST());
    ProposalParameter gspc =
        new ProposalParameter(useSuper, cu, rewrite, accessNode, qualifier, variableBinding);
    if (ASTResolving.isWriteAccess(sn)) return addSetterProposal(gspc, relevance);
    else return addGetterProposal(gspc, relevance);
  }

  /**
   * Proposes a getter for this field.
   *
   * @param context the proposal parameter
   * @param relevance relevance of this proposal
   * @return the proposal if available or null
   */
  private static ChangeCorrectionProposal addGetterProposal(
      ProposalParameter context, int relevance) {
    IMethodBinding method = findGetter(context);
    if (method != null) {
      Expression mi = createMethodInvocation(context, method, null);
      context.astRewrite.replace(context.accessNode, mi, null);

      String label =
          Messages.format(
              CorrectionMessages.GetterSetterCorrectionSubProcessor_replacewithgetter_description,
              BasicElementLabels.getJavaCodeString(ASTNodes.asString(context.accessNode)));
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
      ASTRewriteCorrectionProposal proposal =
          new ASTRewriteCorrectionProposal(
              label, context.compilationUnit, context.astRewrite, relevance, image);
      return proposal;
    } else {
      //			IJavaElement element= context.variableBinding.getJavaElement();
      //			if (element instanceof IField) {
      //				IField field= (IField) element;
      //				try {
      //					if (RefactoringAvailabilityTester.isSelfEncapsulateAvailable(field))
      //						return new SelfEncapsulateFieldProposal(relevance, field);
      //				} catch (JavaModelException e) {
      //					JavaPlugin.log(e);
      //				}
      //			}
    }
    return null;
  }

  private static IMethodBinding findGetter(ProposalParameter context) {
    ITypeBinding returnType = context.variableBinding.getType();
    String getterName =
        GetterSetterUtil.getGetterName(
            context.variableBinding,
            context.compilationUnit.getJavaProject(),
            null,
            isBoolean(context));
    ITypeBinding declaringType = context.variableBinding.getDeclaringClass();
    if (declaringType == null) return null;
    IMethodBinding getter =
        Bindings.findMethodInHierarchy(declaringType, getterName, new ITypeBinding[0]);
    if (getter != null
        && getter.getReturnType().isAssignmentCompatible(returnType)
        && Modifier.isStatic(getter.getModifiers())
            == Modifier.isStatic(context.variableBinding.getModifiers())) return getter;
    return null;
  }

  private static Expression createMethodInvocation(
      ProposalParameter context, IMethodBinding method, Expression argument) {
    AST ast = context.astRewrite.getAST();
    Expression qualifier = context.qualifier;
    if (context.useSuper) {
      SuperMethodInvocation invocation = ast.newSuperMethodInvocation();
      invocation.setName(ast.newSimpleName(method.getName()));
      if (qualifier != null)
        invocation.setQualifier((Name) context.astRewrite.createCopyTarget(qualifier));
      if (argument != null) invocation.arguments().add(argument);
      return invocation;
    } else {
      MethodInvocation invocation = ast.newMethodInvocation();
      invocation.setName(ast.newSimpleName(method.getName()));
      if (qualifier != null)
        invocation.setExpression((Expression) context.astRewrite.createCopyTarget(qualifier));
      if (argument != null) invocation.arguments().add(argument);
      return invocation;
    }
  }

  /**
   * Proposes a setter for this field.
   *
   * @param context the proposal parameter
   * @param relevance relevance of this proposal
   * @return the proposal if available or null
   */
  private static ChangeCorrectionProposal addSetterProposal(
      ProposalParameter context, int relevance) {
    boolean isBoolean = isBoolean(context);
    String setterName =
        GetterSetterUtil.getSetterName(
            context.variableBinding, context.compilationUnit.getJavaProject(), null, isBoolean);
    ITypeBinding declaringType = context.variableBinding.getDeclaringClass();
    if (declaringType == null) return null;

    IMethodBinding method =
        Bindings.findMethodInHierarchy(
            declaringType, setterName, new ITypeBinding[] {context.variableBinding.getType()});
    if (method != null
        && Bindings.isVoidType(method.getReturnType())
        && (Modifier.isStatic(method.getModifiers())
            == Modifier.isStatic(context.variableBinding.getModifiers()))) {
      Expression assignedValue = getAssignedValue(context);
      if (assignedValue == null) return null; // we don't know how to handle those cases.
      Expression mi = createMethodInvocation(context, method, assignedValue);
      context.astRewrite.replace(context.accessNode.getParent(), mi, null);

      String label =
          Messages.format(
              CorrectionMessages.GetterSetterCorrectionSubProcessor_replacewithsetter_description,
              BasicElementLabels.getJavaCodeString(ASTNodes.asString(context.accessNode)));
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
      ASTRewriteCorrectionProposal proposal =
          new ASTRewriteCorrectionProposal(
              label, context.compilationUnit, context.astRewrite, relevance, image);
      return proposal;
    } else {
      // TODO
      //			IJavaElement element= context.variableBinding.getJavaElement();
      //			if (element instanceof IField) {
      //				IField field= (IField) element;
      //				try {
      //					if (RefactoringAvailabilityTester.isSelfEncapsulateAvailable(field))
      //						return new SelfEncapsulateFieldProposal(relevance, field);
      //				} catch (JavaModelException e) {
      //					JavaPlugin.log(e);
      //				}
      //			}
    }
    return null;
  }

  private static boolean isBoolean(ProposalParameter context) {
    AST ast = context.astRewrite.getAST();
    boolean isBoolean =
        ast.resolveWellKnownType("boolean") == context.variableBinding.getType(); // $NON-NLS-1$
    if (!isBoolean)
      isBoolean =
          ast.resolveWellKnownType("java.lang.Boolean")
              == context.variableBinding.getType(); // $NON-NLS-1$
    return isBoolean;
  }

  private static Expression getAssignedValue(ProposalParameter context) {
    ASTNode parent = context.accessNode.getParent();
    ASTRewrite astRewrite = context.astRewrite;
    IJavaProject javaProject = context.compilationUnit.getJavaProject();
    IMethodBinding getter = findGetter(context);
    Expression getterExpression = null;
    if (getter != null) {
      getterExpression = astRewrite.getAST().newSimpleName("placeholder"); // $NON-NLS-1$
    }
    ITypeBinding type = context.variableBinding.getType();
    boolean is50OrHigher = JavaModelUtil.is50OrHigher(javaProject);
    Expression result =
        GetterSetterUtil.getAssignedValue(parent, astRewrite, getterExpression, type, is50OrHigher);
    if (result != null && getterExpression != null && getterExpression.getParent() != null) {
      getterExpression
          .getParent()
          .setStructuralProperty(
              getterExpression.getLocationInParent(),
              createMethodInvocation(context, getter, null));
    }
    return result;
  }
}

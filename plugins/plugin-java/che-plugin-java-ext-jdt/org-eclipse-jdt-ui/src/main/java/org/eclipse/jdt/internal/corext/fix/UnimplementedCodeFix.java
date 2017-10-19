/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class UnimplementedCodeFix extends CompilationUnitRewriteOperationsFix {

  public static final class MakeTypeAbstractOperation extends CompilationUnitRewriteOperation {

    private final TypeDeclaration fTypeDeclaration;

    public MakeTypeAbstractOperation(TypeDeclaration typeDeclaration) {
      fTypeDeclaration = typeDeclaration;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(
        CompilationUnitRewrite cuRewrite, LinkedProposalModel linkedProposalPositions)
        throws CoreException {
      AST ast = cuRewrite.getAST();
      ASTRewrite rewrite = cuRewrite.getASTRewrite();
      Modifier newModifier = ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
      TextEditGroup textEditGroup =
          createTextEditGroup(
              CorrectionMessages.UnimplementedCodeFix_TextEditGroup_label, cuRewrite);
      rewrite
          .getListRewrite(fTypeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY)
          .insertLast(newModifier, textEditGroup);

      LinkedProposalPositionGroup group =
          new LinkedProposalPositionGroup("modifier"); // $NON-NLS-1$
      group.addPosition(rewrite.track(newModifier), !linkedProposalPositions.hasLinkedPositions());
      linkedProposalPositions.addPositionGroup(group);
    }
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit root,
      boolean addMissingMethod,
      boolean makeTypeAbstract,
      IProblemLocation[] problems) {
    Assert.isLegal(!addMissingMethod || !makeTypeAbstract);
    if (!addMissingMethod && !makeTypeAbstract) return null;

    if (problems.length == 0) return null;

    ArrayList<CompilationUnitRewriteOperation> operations =
        new ArrayList<CompilationUnitRewriteOperation>();

    for (int i = 0; i < problems.length; i++) {
      IProblemLocation problem = problems[i];
      if (addMissingMethod) {
        ASTNode typeNode = getSelectedTypeNode(root, problem);
        if (typeNode != null && !isTypeBindingNull(typeNode)) {
          operations.add(new AddUnimplementedMethodsOperation(typeNode));
        }
      } else {
        ASTNode typeNode = getSelectedTypeNode(root, problem);
        if (typeNode instanceof TypeDeclaration) {
          operations.add(new MakeTypeAbstractOperation((TypeDeclaration) typeNode));
        }
      }
    }

    if (operations.size() == 0) return null;

    String label;
    if (addMissingMethod) {
      label = CorrectionMessages.UnimplementedMethodsCorrectionProposal_description;
    } else {
      label = CorrectionMessages.UnimplementedCodeFix_MakeAbstractFix_label;
    }
    return new UnimplementedCodeFix(
        label, root, operations.toArray(new CompilationUnitRewriteOperation[operations.size()]));
  }

  public static IProposableFix createAddUnimplementedMethodsFix(
      final CompilationUnit root, IProblemLocation problem) {
    ASTNode typeNode = getSelectedTypeNode(root, problem);
    if (typeNode == null) return null;

    if (isTypeBindingNull(typeNode)) return null;

    AddUnimplementedMethodsOperation operation = new AddUnimplementedMethodsOperation(typeNode);
    if (operation.getMethodsToImplement().length > 0) {
      return new UnimplementedCodeFix(
          CorrectionMessages.UnimplementedMethodsCorrectionProposal_description,
          root,
          new CompilationUnitRewriteOperation[] {operation});
    } else {
      return new IProposableFix() {
        public CompilationUnitChange createChange(IProgressMonitor progressMonitor)
            throws CoreException {
          CompilationUnitChange change =
              new CompilationUnitChange(
                  CorrectionMessages.UnimplementedMethodsCorrectionProposal_description,
                  (ICompilationUnit) root.getJavaElement()) {
                @Override
                public Change perform(IProgressMonitor pm) throws CoreException {
                  //							Shell shell=
                  // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                  //							String dialogTitle=
                  // CorrectionMessages.UnimplementedMethodsCorrectionProposal_description;
                  //							IStatus status= getStatus();
                  //							ErrorDialog.openError(shell, dialogTitle,
                  // CorrectionMessages.UnimplementedCodeFix_DependenciesErrorMessage, status);
                  // TODO
                  return new NullChange();
                }
              };
          change.setEdit(new MultiTextEdit());
          return change;
        }

        public String getAdditionalProposalInfo() {
          return new String();
        }

        public String getDisplayString() {
          return CorrectionMessages.UnimplementedMethodsCorrectionProposal_description;
        }

        public IStatus getStatus() {
          return new Status(
              IStatus.ERROR,
              JavaPlugin.ID_PLUGIN,
              CorrectionMessages.UnimplementedCodeFix_DependenciesStatusMessage);
        }
      };
    }
  }

  public static UnimplementedCodeFix createMakeTypeAbstractFix(
      CompilationUnit root, IProblemLocation problem) {
    ASTNode typeNode = getSelectedTypeNode(root, problem);
    if (!(typeNode instanceof TypeDeclaration)) return null;

    TypeDeclaration typeDeclaration = (TypeDeclaration) typeNode;
    MakeTypeAbstractOperation operation = new MakeTypeAbstractOperation(typeDeclaration);

    String label =
        Messages.format(
            CorrectionMessages.ModifierCorrectionSubProcessor_addabstract_description,
            BasicElementLabels.getJavaElementName(typeDeclaration.getName().getIdentifier()));
    return new UnimplementedCodeFix(label, root, new CompilationUnitRewriteOperation[] {operation});
  }

  public static ASTNode getSelectedTypeNode(CompilationUnit root, IProblemLocation problem) {
    ASTNode selectedNode = problem.getCoveringNode(root);
    if (selectedNode == null) return null;

    if (selectedNode.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) { // bug 200016
      selectedNode = selectedNode.getParent();
    }

    if (selectedNode.getLocationInParent() == EnumConstantDeclaration.NAME_PROPERTY) {
      selectedNode = selectedNode.getParent();
    }
    if (selectedNode.getNodeType() == ASTNode.SIMPLE_NAME
        && selectedNode.getParent() instanceof AbstractTypeDeclaration) {
      return selectedNode.getParent();
    } else if (selectedNode.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
      return ((ClassInstanceCreation) selectedNode).getAnonymousClassDeclaration();
    } else if (selectedNode.getNodeType() == ASTNode.ENUM_CONSTANT_DECLARATION) {
      EnumConstantDeclaration enumConst = (EnumConstantDeclaration) selectedNode;
      if (enumConst.getAnonymousClassDeclaration() != null)
        return enumConst.getAnonymousClassDeclaration();
      return enumConst;
    } else {
      return null;
    }
  }

  private static boolean isTypeBindingNull(ASTNode typeNode) {
    if (typeNode instanceof AbstractTypeDeclaration) {
      AbstractTypeDeclaration abstractTypeDeclaration = (AbstractTypeDeclaration) typeNode;
      if (abstractTypeDeclaration.resolveBinding() == null) return true;

      return false;
    } else if (typeNode instanceof AnonymousClassDeclaration) {
      AnonymousClassDeclaration anonymousClassDeclaration = (AnonymousClassDeclaration) typeNode;
      if (anonymousClassDeclaration.resolveBinding() == null) return true;

      return false;
    } else if (typeNode instanceof EnumConstantDeclaration) {
      return false;
    } else {
      return true;
    }
  }

  public UnimplementedCodeFix(
      String name,
      CompilationUnit compilationUnit,
      CompilationUnitRewriteOperation[] fixRewriteOperations) {
    super(name, compilationUnit, fixRewriteOperations);
  }
}

/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.JavadocTagsSubProcessor;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;

public class RemoveDeclarationCorrectionProposal extends ASTRewriteCorrectionProposal {

  private static class SideEffectFinder extends ASTVisitor {

    private ArrayList<Expression> fSideEffectNodes;

    public SideEffectFinder(ArrayList<Expression> res) {
      fSideEffectNodes = res;
    }

    @Override
    public boolean visit(Assignment node) {
      fSideEffectNodes.add(node);
      return false;
    }

    @Override
    public boolean visit(PostfixExpression node) {
      fSideEffectNodes.add(node);
      return false;
    }

    @Override
    public boolean visit(PrefixExpression node) {
      Object operator = node.getOperator();
      if (operator == PrefixExpression.Operator.INCREMENT
          || operator == PrefixExpression.Operator.DECREMENT) {
        fSideEffectNodes.add(node);
      }
      return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
      fSideEffectNodes.add(node);
      return false;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
      fSideEffectNodes.add(node);
      return false;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
      fSideEffectNodes.add(node);
      return false;
    }
  }

  private SimpleName fName;

  public RemoveDeclarationCorrectionProposal(ICompilationUnit cu, SimpleName name, int relevance) {
    super(
        "", cu, null, relevance, JavaPluginImages.get(JavaPluginImages.IMG_TOOL_DELETE)
        /*JavaPlugin.getDefault().getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE)*/ ); // $NON-NLS-1$
    fName = name;
  }

  @Override
  public String getName() {
    IBinding binding = fName.resolveBinding();
    String name = BasicElementLabels.getJavaElementName(fName.getIdentifier());
    switch (binding.getKind()) {
      case IBinding.TYPE:
        return Messages.format(
            CorrectionMessages.RemoveDeclarationCorrectionProposal_removeunusedtype_description,
            name);
      case IBinding.METHOD:
        if (((IMethodBinding) binding).isConstructor()) {
          return Messages.format(
              CorrectionMessages
                  .RemoveDeclarationCorrectionProposal_removeunusedconstructor_description,
              name);
        } else {
          return Messages.format(
              CorrectionMessages.RemoveDeclarationCorrectionProposal_removeunusedmethod_description,
              name);
        }
      case IBinding.VARIABLE:
        if (((IVariableBinding) binding).isField()) {
          return Messages.format(
              CorrectionMessages.RemoveDeclarationCorrectionProposal_removeunusedfield_description,
              name);
        } else {
          return Messages.format(
              CorrectionMessages.RemoveDeclarationCorrectionProposal_removeunusedvar_description,
              name);
        }
      default:
        return super.getDisplayString();
    }
  }

  /*(non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ASTRewriteCorrectionProposal#getRewrite()
   */
  @Override
  protected ASTRewrite getRewrite() {
    IBinding binding = fName.resolveBinding();
    CompilationUnit root = (CompilationUnit) fName.getRoot();
    ASTRewrite rewrite;
    if (binding.getKind() == IBinding.METHOD) {
      IMethodBinding decl = ((IMethodBinding) binding).getMethodDeclaration();
      ASTNode declaration = root.findDeclaringNode(decl);
      rewrite = ASTRewrite.create(root.getAST());
      rewrite.remove(declaration, null);
    } else if (binding.getKind() == IBinding.TYPE) {
      ITypeBinding decl = ((ITypeBinding) binding).getTypeDeclaration();
      ASTNode declaration = root.findDeclaringNode(decl);
      rewrite = ASTRewrite.create(root.getAST());
      rewrite.remove(declaration, null);
    } else if (binding.getKind() == IBinding.VARIABLE) {
      // needs full AST
      CompilationUnit completeRoot =
          SharedASTProvider.getAST(getCompilationUnit(), SharedASTProvider.WAIT_YES, null);

      SimpleName nameNode =
          (SimpleName)
              NodeFinder.perform(completeRoot, fName.getStartPosition(), fName.getLength());

      rewrite = ASTRewrite.create(completeRoot.getAST());
      SimpleName[] references =
          LinkedNodeFinder.findByBinding(completeRoot, nameNode.resolveBinding());
      for (int i = 0; i < references.length; i++) {
        removeVariableReferences(rewrite, references[i]);
      }

      IVariableBinding bindingDecl =
          ((IVariableBinding) nameNode.resolveBinding()).getVariableDeclaration();
      ASTNode declaringNode = completeRoot.findDeclaringNode(bindingDecl);
      if (declaringNode instanceof SingleVariableDeclaration) {
        removeParamTag(rewrite, (SingleVariableDeclaration) declaringNode);
      }
    } else {
      throw new IllegalArgumentException("Unexpected binding"); // $NON-NLS-1$
    }
    return rewrite;
  }

  private void removeParamTag(ASTRewrite rewrite, SingleVariableDeclaration varDecl) {
    if (varDecl.getParent() instanceof MethodDeclaration) {
      Javadoc javadoc = ((MethodDeclaration) varDecl.getParent()).getJavadoc();
      if (javadoc != null) {
        TagElement tagElement =
            JavadocTagsSubProcessor.findParamTag(javadoc, varDecl.getName().getIdentifier());
        if (tagElement != null) {
          rewrite.remove(tagElement, null);
        }
      }
    }
  }

  /**
   * Remove the field or variable declaration including the initializer.
   *
   * @param rewrite the ast rewrite
   * @param reference the reference
   */
  private void removeVariableReferences(ASTRewrite rewrite, SimpleName reference) {
    ASTNode parent = reference.getParent();
    while (parent instanceof QualifiedName) {
      parent = parent.getParent();
    }
    if (parent instanceof FieldAccess) {
      parent = parent.getParent();
    }

    int nameParentType = parent.getNodeType();
    if (nameParentType == ASTNode.ASSIGNMENT) {
      Assignment assignment = (Assignment) parent;
      Expression rightHand = assignment.getRightHandSide();

      ASTNode assignParent = assignment.getParent();
      if (assignParent.getNodeType() == ASTNode.EXPRESSION_STATEMENT
          && rightHand.getNodeType() != ASTNode.ASSIGNMENT) {
        removeVariableWithInitializer(rewrite, rightHand, assignParent);
      } else {
        rewrite.replace(assignment, rewrite.createCopyTarget(rightHand), null);
      }
    } else if (nameParentType == ASTNode.SINGLE_VARIABLE_DECLARATION) {
      rewrite.remove(parent, null);
    } else if (nameParentType == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
      VariableDeclarationFragment frag = (VariableDeclarationFragment) parent;
      ASTNode varDecl = frag.getParent();
      List<VariableDeclarationFragment> fragments;
      if (varDecl instanceof VariableDeclarationExpression) {
        fragments = ((VariableDeclarationExpression) varDecl).fragments();
      } else if (varDecl instanceof FieldDeclaration) {
        fragments = ((FieldDeclaration) varDecl).fragments();
      } else {
        fragments = ((VariableDeclarationStatement) varDecl).fragments();
      }
      if (fragments.size() == 1) {
        rewrite.remove(varDecl, null);
      } else {
        rewrite.remove(frag, null); // don't try to preserve
      }
    }
  }

  private void removeVariableWithInitializer(
      ASTRewrite rewrite, ASTNode initializerNode, ASTNode statementNode) {
    ArrayList<Expression> sideEffectNodes = new ArrayList<Expression>();
    initializerNode.accept(new SideEffectFinder(sideEffectNodes));
    int nSideEffects = sideEffectNodes.size();
    if (nSideEffects == 0) {
      if (ASTNodes.isControlStatementBody(statementNode.getLocationInParent())) {
        rewrite.replace(statementNode, rewrite.getAST().newBlock(), null);
      } else {
        rewrite.remove(statementNode, null);
      }
    } else {
      // do nothing yet
    }
  }
}

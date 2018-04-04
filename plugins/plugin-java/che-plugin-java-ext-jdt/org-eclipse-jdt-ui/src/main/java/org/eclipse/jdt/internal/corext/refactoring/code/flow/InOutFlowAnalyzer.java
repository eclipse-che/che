/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code.flow;

import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class InOutFlowAnalyzer extends FlowAnalyzer {

  public InOutFlowAnalyzer(FlowContext context) {
    super(context);
  }

  public FlowInfo perform(ASTNode[] selectedNodes) {
    FlowContext context = getFlowContext();
    GenericSequentialFlowInfo result = createSequential();
    for (int i = 0; i < selectedNodes.length; i++) {
      ASTNode node = selectedNodes[i];
      node.accept(this);
      result.merge(getFlowInfo(node), context);
    }
    return result;
  }

  @Override
  protected boolean traverseNode(ASTNode node) {
    // we are only traversing the selected nodes.
    return true;
  }

  @Override
  protected boolean createReturnFlowInfo(ReturnStatement node) {
    // we are only traversing selected nodes.
    return true;
  }

  @Override
  public void endVisit(Block node) {
    super.endVisit(node);
    clearAccessMode(accessFlowInfo(node), node.statements());
  }

  @Override
  public void endVisit(CatchClause node) {
    super.endVisit(node);
    clearAccessMode(accessFlowInfo(node), node.getException());
  }

  @Override
  public void endVisit(EnhancedForStatement node) {
    super.endVisit(node);
    clearAccessMode(accessFlowInfo(node), node.getParameter());
  }

  @Override
  public void endVisit(ForStatement node) {
    super.endVisit(node);
    clearAccessMode(accessFlowInfo(node), node.initializers());
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    super.endVisit(node);
    FlowInfo info = accessFlowInfo(node);
    for (Iterator<SingleVariableDeclaration> iter = node.parameters().iterator();
        iter.hasNext(); ) {
      clearAccessMode(info, iter.next());
    }
  }

  private void clearAccessMode(FlowInfo info, SingleVariableDeclaration decl) {
    IVariableBinding binding = decl.resolveBinding();
    if (binding != null && !binding.isField()) info.clearAccessMode(binding, fFlowContext);
  }

  private void clearAccessMode(FlowInfo info, List<? extends ASTNode> nodes) {
    if (nodes == null || nodes.isEmpty() || info == null) return;
    for (Iterator<? extends ASTNode> iter = nodes.iterator(); iter.hasNext(); ) {
      Object node = iter.next();
      Iterator<VariableDeclarationFragment> fragments = null;
      if (node instanceof VariableDeclarationStatement) {
        fragments = ((VariableDeclarationStatement) node).fragments().iterator();
      } else if (node instanceof VariableDeclarationExpression) {
        fragments = ((VariableDeclarationExpression) node).fragments().iterator();
      }
      if (fragments != null) {
        while (fragments.hasNext()) {
          clearAccessMode(info, fragments.next());
        }
      }
    }
  }

  private void clearAccessMode(FlowInfo info, VariableDeclarationFragment fragment) {
    IVariableBinding binding = fragment.resolveBinding();
    if (binding != null && !binding.isField()) info.clearAccessMode(binding, fFlowContext);
  }
}

/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/** Maps a selection to a set of AST nodes. */
public class SelectionAnalyzer extends GenericVisitor {

  private Selection fSelection;
  private boolean fTraverseSelectedNode;
  private ASTNode fLastCoveringNode;

  // Selected nodes
  private List<ASTNode> fSelectedNodes;

  public SelectionAnalyzer(Selection selection, boolean traverseSelectedNode) {
    super(true);
    Assert.isNotNull(selection);
    fSelection = selection;
    fTraverseSelectedNode = traverseSelectedNode;
  }

  protected void setSelection(Selection selection) {
    fSelection = selection;
  }

  public boolean hasSelectedNodes() {
    return fSelectedNodes != null && !fSelectedNodes.isEmpty();
  }

  public ASTNode[] getSelectedNodes() {
    if (fSelectedNodes == null || fSelectedNodes.isEmpty()) return new ASTNode[0];
    return fSelectedNodes.toArray(new ASTNode[fSelectedNodes.size()]);
  }

  public ASTNode getFirstSelectedNode() {
    if (fSelectedNodes == null || fSelectedNodes.isEmpty()) return null;
    return fSelectedNodes.get(0);
  }

  public ASTNode getLastSelectedNode() {
    if (fSelectedNodes == null || fSelectedNodes.isEmpty()) return null;
    return fSelectedNodes.get(fSelectedNodes.size() - 1);
  }

  public boolean isExpressionSelected() {
    if (!hasSelectedNodes()) return false;
    return fSelectedNodes.get(0) instanceof Expression;
  }

  public IRegion getSelectedNodeRange() {
    if (fSelectedNodes == null || fSelectedNodes.isEmpty()) return null;
    ASTNode firstNode = fSelectedNodes.get(0);
    ASTNode lastNode = fSelectedNodes.get(fSelectedNodes.size() - 1);
    int start = firstNode.getStartPosition();
    return new Region(start, lastNode.getStartPosition() + lastNode.getLength() - start);
  }

  public ASTNode getLastCoveringNode() {
    return fLastCoveringNode;
  }

  public Selection getSelection() {
    return fSelection;
  }

  // --- node management ---------------------------------------------------------

  @Override
  protected boolean visitNode(ASTNode node) {
    // The selection lies behind the node.
    if (fSelection.liesOutside(node)) {
      return false;
    } else if (fSelection.covers(node)) {
      if (isFirstNode()) {
        handleFirstSelectedNode(node);
      } else {
        handleNextSelectedNode(node);
      }
      return fTraverseSelectedNode;
    } else if (fSelection.coveredBy(node)) {
      fLastCoveringNode = node;
      return true;
    } else if (fSelection.endsIn(node)) {
      return handleSelectionEndsIn(node);
    }
    // There is a possibility that the user has selected trailing semicolons that don't belong
    // to the statement. So dive into it to check if sub nodes are fully covered.
    return true;
  }

  protected void reset() {
    fSelectedNodes = null;
  }

  protected void handleFirstSelectedNode(ASTNode node) {
    fSelectedNodes = new ArrayList<ASTNode>(5);
    fSelectedNodes.add(node);
  }

  protected void handleNextSelectedNode(ASTNode node) {
    if (getFirstSelectedNode().getParent() == node.getParent()) {
      fSelectedNodes.add(node);
    }
  }

  protected boolean handleSelectionEndsIn(ASTNode node) {
    return false;
  }

  protected List<ASTNode> internalGetSelectedNodes() {
    return fSelectedNodes;
  }

  private boolean isFirstNode() {
    return fSelectedNodes == null;
  }
}

/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.rename;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;

public class TempOccurrenceAnalyzer extends ASTVisitor {
  /** Set of SimpleName */
  private Set<SimpleName> fReferenceNodes;
  /** Set of SimpleName */
  private Set<SimpleName> fJavadocNodes;

  private VariableDeclaration fTempDeclaration;
  private IBinding fTempBinding;
  private boolean fAnalyzeJavadoc;

  private boolean fIsInJavadoc;

  public TempOccurrenceAnalyzer(VariableDeclaration tempDeclaration, boolean analyzeJavadoc) {
    Assert.isNotNull(tempDeclaration);
    fReferenceNodes = new HashSet<SimpleName>();
    fJavadocNodes = new HashSet<SimpleName>();
    fAnalyzeJavadoc = analyzeJavadoc;
    fTempDeclaration = tempDeclaration;
    fTempBinding = tempDeclaration.resolveBinding();
    fIsInJavadoc = false;
  }

  public void perform() {
    ASTNode cuNode = ASTNodes.getParent(fTempDeclaration, CompilationUnit.class);
    cuNode.accept(this);
  }

  public int[] getReferenceOffsets() {
    int[] offsets = new int[fReferenceNodes.size()];
    addOffsets(offsets, 0, fReferenceNodes);
    return offsets;
  }

  public int[] getReferenceAndJavadocOffsets() {
    int[] offsets = new int[fReferenceNodes.size() + fJavadocNodes.size()];
    addOffsets(offsets, 0, fReferenceNodes);
    addOffsets(offsets, fReferenceNodes.size(), fJavadocNodes);
    return offsets;
  }

  private void addOffsets(int[] offsets, int start, Set<SimpleName> nodeSet) {
    int i = start;
    for (Iterator<SimpleName> iter = nodeSet.iterator(); iter.hasNext(); i++) {
      ASTNode node = iter.next();
      offsets[i] = node.getStartPosition();
    }
  }

  public int getNumberOfReferences() {
    return fReferenceNodes.size();
  }

  public SimpleName[] getReferenceNodes() {
    return fReferenceNodes.toArray(new SimpleName[fReferenceNodes.size()]);
  }

  public SimpleName[] getJavadocNodes() {
    return fJavadocNodes.toArray(new SimpleName[fJavadocNodes.size()]);
  }

  public SimpleName[] getReferenceAndDeclarationNodes() {
    SimpleName[] nodes = fReferenceNodes.toArray(new SimpleName[fReferenceNodes.size() + 1]);
    nodes[fReferenceNodes.size()] = fTempDeclaration.getName();
    return nodes;
  }

  // ------- visit ------ (don't call)

  @Override
  public boolean visit(Javadoc node) {
    if (fAnalyzeJavadoc) fIsInJavadoc = true;
    return fAnalyzeJavadoc;
  }

  @Override
  public void endVisit(Javadoc node) {
    fIsInJavadoc = false;
  }

  @Override
  public boolean visit(SimpleName node) {
    if (node.getParent() instanceof VariableDeclaration) {
      if (((VariableDeclaration) node.getParent()).getName() == node)
        return true; // don't include declaration
    }

    if (fTempBinding != null && fTempBinding == node.resolveBinding()) {
      if (fIsInJavadoc) fJavadocNodes.add(node);
      else fReferenceNodes.add(node);
    }

    return true;
  }
}

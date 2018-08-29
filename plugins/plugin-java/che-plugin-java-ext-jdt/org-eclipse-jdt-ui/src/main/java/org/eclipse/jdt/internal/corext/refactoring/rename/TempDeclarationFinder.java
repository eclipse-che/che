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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.jdt.internal.corext.dom.SelectionAnalyzer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class TempDeclarationFinder {

  // no instances
  private TempDeclarationFinder() {}

  /**
   * @return <code>null</code> if the selection is invalid or does not cover a temp declaration or
   *     reference.
   */
  public static VariableDeclaration findTempDeclaration(
      CompilationUnit cu, int selectionOffset, int selectionLength) {
    TempSelectionAnalyzer analyzer = new TempSelectionAnalyzer(selectionOffset, selectionLength);
    cu.accept(analyzer);

    ASTNode[] selected = analyzer.getSelectedNodes();
    if (selected == null || selected.length != 1) return null;

    ASTNode selectedNode = selected[0];
    if (selectedNode instanceof VariableDeclaration) return (VariableDeclaration) selectedNode;

    if (selectedNode instanceof Name) {
      Name reference = (Name) selectedNode;
      IBinding binding = reference.resolveBinding();
      if (binding == null) return null;
      ASTNode declaringNode = cu.findDeclaringNode(binding);
      if (declaringNode instanceof VariableDeclaration) return (VariableDeclaration) declaringNode;
      else return null;
    } else if (selectedNode instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement vds = (VariableDeclarationStatement) selectedNode;
      if (vds.fragments().size() != 1) return null;
      return (VariableDeclaration) vds.fragments().get(0);
    }
    return null;
  }

  /*
   * Class used to extract selected nodes from an AST.
   * Subclassing <code>SelectionAnalyzer</code> is needed to support activation
   * when only a part of the <code>VariableDeclaration</code> node is selected
   */
  private static class TempSelectionAnalyzer extends SelectionAnalyzer {

    private ASTNode fNode;

    TempSelectionAnalyzer(int selectionOffset, int selectionLength) {
      super(Selection.createFromStartLength(selectionOffset, selectionLength), true);
    }

    // overridden
    @Override
    public boolean visitNode(ASTNode node) {
      if (node instanceof VariableDeclaration)
        return visitVariableDeclaration((VariableDeclaration) node);
      else if (node instanceof SimpleName) return visitSimpleName((SimpleName) node);
      else return super.visitNode(node);
    }

    private boolean addNodeAndStop(ASTNode node) {
      fNode = node;
      return false;
    }

    private boolean visitSimpleName(SimpleName name) {
      if (getSelection().coveredBy(name)) return addNodeAndStop(name);
      return super.visitNode(name);
    }

    private boolean visitVariableDeclaration(VariableDeclaration vd) {
      if (vd.getInitializer() != null) {
        int start = vd.getStartPosition();
        IRegion declarationRange =
            new Region(start, vd.getInitializer().getStartPosition() - start);
        if (getSelection().coveredBy(declarationRange)) return addNodeAndStop(vd);
        else return super.visitNode(vd);
      } else {
        if (getSelection().coveredBy(vd)) return addNodeAndStop(vd);
        else return super.visitNode(vd);
      }
    }

    // overridden
    @Override
    public ASTNode[] getSelectedNodes() {
      if (fNode != null) return new ASTNode[] {fNode};
      return super.getSelectedNodes();
    }
  }
}

/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.text.edits.TextEditGroup;

public class ASTNodeDeleteUtil {

  private static ASTNode[] getNodesToDelete(IJavaElement element, CompilationUnit cuNode)
      throws JavaModelException {
    // fields are different because you don't delete the whole declaration but only a fragment of it
    if (element.getElementType() == IJavaElement.FIELD) {
      if (JdtFlags.isEnum((IField) element))
        return new ASTNode[] {
          ASTNodeSearchUtil.getEnumConstantDeclaration((IField) element, cuNode)
        };
      else
        return new ASTNode[] {
          ASTNodeSearchUtil.getFieldDeclarationFragmentNode((IField) element, cuNode)
        };
    }
    if (element.getElementType() == IJavaElement.TYPE && ((IType) element).isLocal()) {
      IType type = (IType) element;
      if (type.isAnonymous()) {
        if (type.getParent().getElementType() == IJavaElement.FIELD) {
          EnumConstantDeclaration enumDecl =
              ASTNodeSearchUtil.getEnumConstantDeclaration((IField) element.getParent(), cuNode);
          if (enumDecl != null && enumDecl.getAnonymousClassDeclaration() != null) {
            return new ASTNode[] {enumDecl.getAnonymousClassDeclaration()};
          }
        }
        ClassInstanceCreation creation =
            ASTNodeSearchUtil.getClassInstanceCreationNode(type, cuNode);
        if (creation != null) {
          if (creation.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
            return new ASTNode[] {creation.getParent()};
          } else if (creation.getLocationInParent()
              == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
            return new ASTNode[] {creation};
          }
          return new ASTNode[] {creation.getAnonymousClassDeclaration()};
        }
        return new ASTNode[0];
      } else {
        ASTNode[] nodes = ASTNodeSearchUtil.getDeclarationNodes(element, cuNode);
        // we have to delete the TypeDeclarationStatement
        nodes[0] = nodes[0].getParent();
        return nodes;
      }
    }
    return ASTNodeSearchUtil.getDeclarationNodes(element, cuNode);
  }

  private static Set<ASTNode> getRemovedNodes(
      final List<ASTNode> removed, final CompilationUnitRewrite rewrite) {
    final Set<ASTNode> result = new HashSet<ASTNode>();
    rewrite
        .getRoot()
        .accept(
            new GenericVisitor(true) {

              @Override
              protected boolean visitNode(ASTNode node) {
                if (removed.contains(node)) result.add(node);
                return true;
              }
            });
    return result;
  }

  public static void markAsDeleted(
      IJavaElement[] javaElements, CompilationUnitRewrite rewrite, TextEditGroup group)
      throws JavaModelException {
    final List<ASTNode> removed = new ArrayList<ASTNode>();
    for (int i = 0; i < javaElements.length; i++) {
      markAsDeleted(removed, javaElements[i], rewrite, group);
    }
    propagateFieldDeclarationNodeDeletions(removed, rewrite, group);
  }

  private static void markAsDeleted(
      List<ASTNode> list, IJavaElement element, CompilationUnitRewrite rewrite, TextEditGroup group)
      throws JavaModelException {
    ASTNode[] declarationNodes = getNodesToDelete(element, rewrite.getRoot());
    for (int i = 0; i < declarationNodes.length; i++) {
      ASTNode node = declarationNodes[i];
      if (node != null) {
        list.add(node);
        rewrite.getASTRewrite().remove(node, group);
        rewrite.getImportRemover().registerRemovedNode(node);
      }
    }
  }

  private static void propagateFieldDeclarationNodeDeletions(
      final List<ASTNode> removed,
      final CompilationUnitRewrite rewrite,
      final TextEditGroup group) {
    Set<ASTNode> removedNodes = getRemovedNodes(removed, rewrite);
    for (Iterator<ASTNode> iter = removedNodes.iterator(); iter.hasNext(); ) {
      ASTNode node = iter.next();
      if (node instanceof VariableDeclarationFragment) {
        if (node.getParent() instanceof FieldDeclaration) {
          FieldDeclaration fd = (FieldDeclaration) node.getParent();
          if (!removed.contains(fd) && removedNodes.containsAll(fd.fragments()))
            rewrite.getASTRewrite().remove(fd, group);
          rewrite.getImportRemover().registerRemovedNode(fd);
        }
      }
    }
  }

  private ASTNodeDeleteUtil() {}
}

/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Benjamin Muskalla
 * <bmuskalla@eclipsesource.com> - [extract method] extracting return value results in compile error
 * - https://bugs.eclipse.org/bugs/show_bug.cgi?id=264606 Samrat Dhillon <samrat.dhillon@gmail.com>
 * - [extract method] Extracted method should be declared static if extracted expression is also
 * used in another static method https://bugs.eclipse.org/bugs/show_bug.cgi?id=393098
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;

/* package */ class SnippetFinder extends GenericVisitor {

  public static class Match {
    private List<ASTNode> fNodes;
    private Map<IVariableBinding, SimpleName> fLocalMappings;

    public Match() {
      fNodes = new ArrayList<ASTNode>(10);
      fLocalMappings = new HashMap<IVariableBinding, SimpleName>();
    }

    public void add(ASTNode node) {
      fNodes.add(node);
    }

    public boolean hasCorrectNesting(ASTNode node) {
      if (fNodes.size() == 0) return true;
      ASTNode parent = node.getParent();
      if (fNodes.get(0).getParent() != parent) return false;
      // Here we know that we have two elements. In this case the
      // parent must be a block or a switch statement. Otherwise a
      // snippet like "if (true) foo(); else foo();" would match
      // the pattern "foo(); foo();"
      int nodeType = parent.getNodeType();
      return nodeType == ASTNode.BLOCK || nodeType == ASTNode.SWITCH_STATEMENT;
    }

    public ASTNode[] getNodes() {
      return fNodes.toArray(new ASTNode[fNodes.size()]);
    }

    public void addLocal(IVariableBinding org, SimpleName local) {
      fLocalMappings.put(org, local);
    }

    public SimpleName getMappedName(IVariableBinding org) {
      return fLocalMappings.get(org);
    }

    public IVariableBinding getMappedBinding(IVariableBinding org) {
      SimpleName name = fLocalMappings.get(org);
      return ASTNodes.getVariableBinding(name);
    }

    public boolean isEmpty() {
      return fNodes.isEmpty() && fLocalMappings.isEmpty();
    }

    /**
     * Tests whether the node to be replaced is invalid.
     *
     * @return true if the node is invalid, false otherwise
     */
    public boolean isInvalidNode() {
      ASTNode first = fNodes.get(0);
      ASTNode candidate = first.getParent();
      if (candidate == null) return false;
      // return invalid if the opening and closing parenthesis of the method signature is part of
      // the node to be replaced
      if (candidate.getNodeType() == ASTNode.METHOD_DECLARATION) return true;
      return false;
    }

    public MethodDeclaration getEnclosingMethod() {
      ASTNode first = fNodes.get(0);
      return (MethodDeclaration) ASTNodes.getParent(first, ASTNode.METHOD_DECLARATION);
    }

    public boolean isNodeInStaticContext() {
      ASTNode first = fNodes.get(0);
      return ASTResolving.isInStaticContext(first);
    }
  }

  private class Matcher extends ASTMatcher {
    @Override
    public boolean match(SimpleName candidate, Object s) {
      if (!(s instanceof SimpleName)) return false;

      SimpleName snippet = (SimpleName) s;
      if (candidate.isDeclaration() != snippet.isDeclaration()) return false;

      IBinding cb = candidate.resolveBinding();
      IBinding sb = snippet.resolveBinding();
      if (cb == null || sb == null) return false;
      IVariableBinding vcb = ASTNodes.getVariableBinding(candidate);
      IVariableBinding vsb = ASTNodes.getVariableBinding(snippet);
      if (vcb == null || vsb == null) return Bindings.equals(cb, sb);
      if (!vcb.isField() && !vsb.isField() && Bindings.equals(vcb.getType(), vsb.getType())) {
        SimpleName mapped = fMatch.getMappedName(vsb);
        if (mapped != null) {
          IVariableBinding mappedBinding = ASTNodes.getVariableBinding(mapped);
          if (!Bindings.equals(vcb, mappedBinding)) return false;
        }
        fMatch.addLocal(vsb, candidate);
        return true;
      }
      return Bindings.equals(cb, sb);
    }
  }

  private List<Match> fResult = new ArrayList<Match>(2);
  private Match fMatch;
  private ASTNode[] fSnippet;
  private int fIndex;
  private Matcher fMatcher;
  private int fTypes;

  private SnippetFinder(ASTNode[] snippet) {
    super(true);
    fSnippet = snippet;
    fMatcher = new Matcher();
    reset();
  }

  public static Match[] perform(ASTNode start, ASTNode[] snippet) {
    Assert.isTrue(
        start instanceof AbstractTypeDeclaration || start instanceof AnonymousClassDeclaration);
    SnippetFinder finder = new SnippetFinder(snippet);
    start.accept(finder);
    for (Iterator<Match> iter = finder.fResult.iterator(); iter.hasNext(); ) {
      Match match = iter.next();
      ASTNode[] nodes = match.getNodes();
      // doesn't match if the candidate is the left hand side of an
      // assignment and the snippet consists of a single node.
      // Otherwise y= i; i= z; results in y= e(); e()= z;
      if (nodes.length == 1 && isLeftHandSideOfAssignment(nodes[0])) {
        iter.remove();
      }
    }
    return finder.fResult.toArray(new Match[finder.fResult.size()]);
  }

  private static boolean isLeftHandSideOfAssignment(ASTNode node) {
    Assignment assignment = (Assignment) ASTNodes.getParent(node, ASTNode.ASSIGNMENT);
    if (assignment != null) {
      Expression leftHandSide = assignment.getLeftHandSide();
      if (leftHandSide == node) {
        return true;
      }
      if (ASTNodes.isParent(node, leftHandSide)) {
        switch (leftHandSide.getNodeType()) {
          case ASTNode.SIMPLE_NAME:
            return true;
          case ASTNode.FIELD_ACCESS:
            return node == ((FieldAccess) leftHandSide).getName();
          case ASTNode.QUALIFIED_NAME:
            return node == ((QualifiedName) leftHandSide).getName();
          case ASTNode.SUPER_FIELD_ACCESS:
            return node == ((SuperFieldAccess) leftHandSide).getName();
          default:
            return false;
        }
      }
    }
    return false;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    if (++fTypes > 1) return false;
    return super.visit(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    --fTypes;
    super.endVisit(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    if (++fTypes > 1) return false;
    return super.visit(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    --fTypes;
    super.endVisit(node);
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    if (++fTypes > 1) return false;
    return super.visit(node);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    --fTypes;
    super.endVisit(node);
  }

  @Override
  protected boolean visitNode(ASTNode node) {
    if (matches(node)) {
      return false;
    } else if (!isResetted()) {
      reset();
      if (matches(node)) return false;
    }
    return true;
  }

  private boolean matches(ASTNode node) {
    if (isSnippetNode(node)) return false;
    if (node.subtreeMatch(fMatcher, fSnippet[fIndex]) && fMatch.hasCorrectNesting(node)) {
      fMatch.add(node);
      fIndex++;
      if (fIndex == fSnippet.length) {
        fResult.add(fMatch);
        reset();
      }
      return true;
    }
    return false;
  }

  private boolean isResetted() {
    return fIndex == 0 && fMatch.isEmpty();
  }

  private void reset() {
    fIndex = 0;
    fMatch = new Match();
  }

  private boolean isSnippetNode(ASTNode node) {
    for (int i = 0; i < fSnippet.length; i++) {
      if (node == fSnippet[i]) return true;
    }
    return false;
  }
}

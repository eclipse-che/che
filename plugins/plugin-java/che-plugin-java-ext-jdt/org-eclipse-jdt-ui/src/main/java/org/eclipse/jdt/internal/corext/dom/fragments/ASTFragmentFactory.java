/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom.fragments;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.internal.corext.SourceRangeFactory;
import org.eclipse.jdt.internal.corext.dom.HierarchicalASTVisitor;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.jdt.internal.corext.dom.SelectionAnalyzer;

/**
 * Creates various differing kinds of IASTFragments, all through a very narrow interface. The kind
 * of IASTFragment produced will depend on properties of the parameters supplied to the factory
 * methods, such as the types and characteristics of AST nodes, or the location of source ranges.
 *
 * <p>In general, the client will not be aware of exactly what kind of fragment is obtained from
 * these methods. Beyond the functionality provided by the IASTFragment interface, the client can
 * know, however, based on the parameters passed, some things about the created fragment. See the
 * documentation of the factory methods.
 *
 * @see IASTFragment
 */
public class ASTFragmentFactory {

  // Factory Methods: /////////////////////////////////////////////////////////////////////////

  /**
   * Creates and returns a fragment representing the entire subtree rooted at <code>node</code>. It
   * is not true in general that the node to which the produced IASTFragment maps (see {@link
   * org.eclipse.jdt.internal.corext.dom.fragments.IASTFragment IASTFragment}) will be <code>node
   * </code>.
   *
   * <p>XXX: more doc (current assertions about input vs. output)
   */
  public static IASTFragment createFragmentForFullSubtree(ASTNode node) {
    IASTFragment result = FragmentForFullSubtreeFactory.createFragmentFor(node);
    Assert.isNotNull(result);
    return result;
  }

  /**
   * If possible, this method creates a fragment whose source code range is <code>range</code>
   * within compilation unit <code>cu</code>, and which resides somewhere within the subtree
   * identified by <code>scope</code>.
   *
   * <p>XXX: more doc (current assertions about input vs. output)
   *
   * @param range The source range which the create fragment must have.
   * @param scope A node identifying the AST subtree in which the fragment must lie.
   * @param cu The compilation unit to which the source range applies, and to which the AST
   *     corresponds.
   * @return IASTFragment A fragment whose source range is <code>range</code> within compilation
   *     unit <code>cu</code>, residing somewhere within the AST subtree identified by <code>scope
   *     </code>.
   * @throws JavaModelException
   */
  public static IASTFragment createFragmentForSourceRange(
      ISourceRange range, ASTNode scope, ICompilationUnit cu) throws JavaModelException {
    SelectionAnalyzer sa =
        new SelectionAnalyzer(
            Selection.createFromStartLength(range.getOffset(), range.getLength()), false);
    scope.accept(sa);

    if (isSingleNodeSelected(sa, range, cu))
      return ASTFragmentFactory.createFragmentForFullSubtree(sa.getFirstSelectedNode());
    if (isEmptySelectionCoveredByANode(range, sa))
      return ASTFragmentFactory.createFragmentForFullSubtree(sa.getLastCoveringNode());
    return ASTFragmentFactory.createFragmentForSubPartBySourceRange(
        sa.getLastCoveringNode(), range, cu);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////

  private static boolean isEmptySelectionCoveredByANode(ISourceRange range, SelectionAnalyzer sa) {
    return range.getLength() == 0
        && sa.getFirstSelectedNode() == null
        && sa.getLastCoveringNode() != null;
  }

  private static boolean isSingleNodeSelected(
      SelectionAnalyzer sa, ISourceRange range, ICompilationUnit cu) throws JavaModelException {
    return sa.getSelectedNodes().length == 1
        && !rangeIncludesNonWhitespaceOutsideNode(range, sa.getFirstSelectedNode(), cu);
  }

  private static boolean rangeIncludesNonWhitespaceOutsideNode(
      ISourceRange range, ASTNode node, ICompilationUnit cu) throws JavaModelException {
    return Util.rangeIncludesNonWhitespaceOutsideRange(
        range, SourceRangeFactory.create(node), cu.getBuffer());
  }

  /**
   * Returns <code>null</code> if the indices, taken with respect to the node, do not correspond to
   * a valid node-sub-part fragment.
   */
  private static IASTFragment createFragmentForSubPartBySourceRange(
      ASTNode node, ISourceRange range, ICompilationUnit cu) throws JavaModelException {
    return FragmentForSubPartBySourceRangeFactory.createFragmentFor(node, range, cu);
  }

  private static class FragmentForFullSubtreeFactory extends FragmentFactory {
    public static IASTFragment createFragmentFor(ASTNode node) {
      return new FragmentForFullSubtreeFactory().createFragment(node);
    }

    @Override
    public boolean visit(InfixExpression node) {
      /* Try creating an associative infix expression fragment
      /* for the full subtree.  If this is not applicable,
       * try something more generic.
       */
      IASTFragment fragment = AssociativeInfixExpressionFragment.createFragmentForFullSubtree(node);
      if (fragment == null) return visit((Expression) node);

      setFragment(fragment);
      return false;
    }

    @Override
    public boolean visit(Expression node) {
      setFragment(new SimpleExpressionFragment(node));
      return false;
    }

    @Override
    public boolean visit(ASTNode node) {
      setFragment(new SimpleFragment(node));
      return false;
    }
  }

  private static class FragmentForSubPartBySourceRangeFactory extends FragmentFactory {
    private ISourceRange fRange;
    private ICompilationUnit fCu;

    private JavaModelException javaModelException = null;

    public static IASTFragment createFragmentFor(
        ASTNode node, ISourceRange range, ICompilationUnit cu) throws JavaModelException {
      return new FragmentForSubPartBySourceRangeFactory().createFragment(node, range, cu);
    }

    @Override
    public boolean visit(InfixExpression node) {
      try {
        setFragment(createInfixExpressionSubPartFragmentBySourceRange(node, fRange, fCu));
      } catch (JavaModelException e) {
        javaModelException = e;
      }
      return false;
    }

    @Override
    public boolean visit(ASTNode node) {
      // let fragment be null
      return false;
    }

    protected IASTFragment createFragment(ASTNode node, ISourceRange range, ICompilationUnit cu)
        throws JavaModelException {
      fRange = range;
      fCu = cu;
      IASTFragment result = createFragment(node);
      if (javaModelException != null) throw javaModelException;
      return result;
    }

    private static IExpressionFragment createInfixExpressionSubPartFragmentBySourceRange(
        InfixExpression node, ISourceRange range, ICompilationUnit cu) throws JavaModelException {
      return AssociativeInfixExpressionFragment.createSubPartFragmentBySourceRange(node, range, cu);
    }
  }

  private abstract static class FragmentFactory extends HierarchicalASTVisitor {
    private IASTFragment fFragment;

    protected IASTFragment createFragment(ASTNode node) {
      fFragment = null;
      node.accept(this);
      return fFragment;
    }

    protected final IASTFragment getFragment() {
      return fFragment;
    }

    protected final void setFragment(IASTFragment fragment) {
      Assert.isTrue(!isFragmentSet());
      fFragment = fragment;
    }

    protected final boolean isFragmentSet() {
      return getFragment() != null;
    }
  }
}

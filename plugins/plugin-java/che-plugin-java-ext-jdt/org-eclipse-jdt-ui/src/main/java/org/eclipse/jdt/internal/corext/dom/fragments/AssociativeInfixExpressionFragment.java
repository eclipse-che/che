/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.SourceRangeFactory;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.corext.dom.JdtASTMatcher;
import org.eclipse.text.edits.TextEditGroup;

class AssociativeInfixExpressionFragment extends ASTFragment implements IExpressionFragment {

  private final List<Expression> fOperands;
  private final InfixExpression fGroupRoot;

  public static IExpressionFragment createSubPartFragmentBySourceRange(
      InfixExpression node, ISourceRange range, ICompilationUnit cu) throws JavaModelException {
    Assert.isNotNull(node);
    Assert.isNotNull(range);
    Assert.isTrue(!Util.covers(range, node));
    Assert.isTrue(Util.covers(SourceRangeFactory.create(node), range));

    if (!isAssociativeInfix(node)) return null;

    InfixExpression groupRoot = findGroupRoot(node);
    Assert.isTrue(isAGroupRoot(groupRoot));

    List<Expression> groupMembers =
        AssociativeInfixExpressionFragment.findGroupMembersInOrderFor(groupRoot);
    List<Expression> subGroup = findSubGroupForSourceRange(groupMembers, range);
    if (subGroup.isEmpty() || rangeIncludesExtraNonWhitespace(range, subGroup, cu)) return null;

    return new AssociativeInfixExpressionFragment(groupRoot, subGroup);
  }

  public static IExpressionFragment createFragmentForFullSubtree(InfixExpression node) {
    Assert.isNotNull(node);

    if (!isAssociativeInfix(node)) return null;

    InfixExpression groupRoot = findGroupRoot(node);
    Assert.isTrue(isAGroupRoot(groupRoot));

    List<Expression> groupMembers =
        AssociativeInfixExpressionFragment.findGroupMembersInOrderFor(node);

    return new AssociativeInfixExpressionFragment(groupRoot, groupMembers);
  }

  private static InfixExpression findGroupRoot(InfixExpression node) {
    Assert.isTrue(isAssociativeInfix(node));

    while (!isAGroupRoot(node)) {
      ASTNode parent = node.getParent();

      Assert.isNotNull(parent);
      Assert.isTrue(isAssociativeInfix(parent));
      Assert.isTrue(((InfixExpression) parent).getOperator() == node.getOperator());

      node = (InfixExpression) parent;
    }

    return node;
  }

  private static List<Expression> findSubGroupForSourceRange(
      List<Expression> group, ISourceRange range) {
    Assert.isTrue(!group.isEmpty());

    List<Expression> subGroup = new ArrayList<Expression>();

    boolean entered = false, exited = false;
    if (range.getOffset() == group.get(0).getStartPosition()) entered = true;
    for (int i = 0; i < group.size() - 1; i++) {
      Expression member = group.get(i);
      Expression nextMember = group.get(i + 1);

      if (entered) {
        subGroup.add(member);
        if (rangeEndsBetween(range, member, nextMember)) {
          exited = true;
          break;
        }

      } else {
        if (rangeStartsBetween(range, member, nextMember)) entered = true;
      }
    }
    Expression lastGroupMember = group.get(group.size() - 1);
    if (Util.getEndExclusive(range)
        == Util.getEndExclusive(SourceRangeFactory.create(lastGroupMember))) {
      subGroup.add(lastGroupMember);
      exited = true;
    }

    if (!exited) return new ArrayList<Expression>(0);
    return subGroup;
  }

  private static boolean rangeStartsBetween(ISourceRange range, ASTNode first, ASTNode next) {
    int pos = range.getOffset();
    return first.getStartPosition() + first.getLength() <= pos && pos <= next.getStartPosition();
  }

  private static boolean rangeEndsBetween(ISourceRange range, ASTNode first, ASTNode next) {
    int pos = Util.getEndExclusive(range);
    return first.getStartPosition() + first.getLength() <= pos && pos <= next.getStartPosition();
  }

  private static boolean rangeIncludesExtraNonWhitespace(
      ISourceRange range, List<Expression> operands, ICompilationUnit cu)
      throws JavaModelException {
    return Util.rangeIncludesNonWhitespaceOutsideRange(
        range, getRangeOfOperands(operands), cu.getBuffer());
  }

  private static ISourceRange getRangeOfOperands(List<Expression> operands) {
    Expression first = operands.get(0);
    Expression last = operands.get(operands.size() - 1);
    return new SourceRange(
        first.getStartPosition(),
        last.getStartPosition() + last.getLength() - first.getStartPosition());
  }

  @Override
  public IASTFragment[] getMatchingFragmentsWithNode(ASTNode node) {
    IASTFragment fragmentForNode = ASTFragmentFactory.createFragmentForFullSubtree(node);
    if (fragmentForNode instanceof AssociativeInfixExpressionFragment) {
      AssociativeInfixExpressionFragment kin = (AssociativeInfixExpressionFragment) fragmentForNode;
      return kin.getSubFragmentsWithMyNodeMatching(this);
    } else {
      return new IASTFragment[0];
    }
  }

  /**
   * Returns all matching subsequences of <code>toMatch</code> in <code>source</code>.
   *
   * @param source the source to look for matching subsequences
   * @param toMatch the sequence to match
   * @return returns a List of Lists of <code>Expression</code>s
   */
  private static List<List<Expression>> getMatchingContiguousNodeSubsequences(
      List<Expression> source, List<Expression> toMatch) {
    // naive implementation:

    List<List<Expression>> subsequences = new ArrayList<List<Expression>>();

    for (int i = 0; i < source.size(); ) {
      if (matchesAt(i, source, toMatch)) {
        subsequences.add(source.subList(i, i + toMatch.size()));
        i += toMatch.size();
      } else i++;
    }

    return subsequences;
  }

  private static boolean matchesAt(int index, List<Expression> subject, List<Expression> toMatch) {
    if (index + toMatch.size() > subject.size()) return false;
    for (int i = 0; i < toMatch.size(); i++, index++) {
      if (!JdtASTMatcher.doNodesMatch(subject.get(index), toMatch.get(i))) return false;
    }
    return true;
  }

  private static boolean isAGroupRoot(ASTNode node) {
    Assert.isNotNull(node);

    return isAssociativeInfix(node) && !isParentInfixWithSameOperator((InfixExpression) node);
  }

  private static boolean isAssociativeInfix(ASTNode node) {
    return node instanceof InfixExpression
        && isOperatorAssociative(((InfixExpression) node).getOperator());
  }

  private static boolean isParentInfixWithSameOperator(InfixExpression node) {
    return node.getParent() instanceof InfixExpression
        && ((InfixExpression) node.getParent()).getOperator() == node.getOperator();
  }

  private static boolean isOperatorAssociative(InfixExpression.Operator operator) {
    return operator == InfixExpression.Operator.PLUS
        || operator == InfixExpression.Operator.TIMES
        || operator == InfixExpression.Operator.XOR
        || operator == InfixExpression.Operator.OR
        || operator == InfixExpression.Operator.AND
        || operator == InfixExpression.Operator.CONDITIONAL_OR
        || operator == InfixExpression.Operator.CONDITIONAL_AND;
  }

  private AssociativeInfixExpressionFragment(InfixExpression groupRoot, List<Expression> operands) {
    Assert.isTrue(isAGroupRoot(groupRoot));
    Assert.isTrue(operands.size() >= 2);
    fGroupRoot = groupRoot;
    fOperands = Collections.unmodifiableList(operands);
  }

  public boolean matches(IASTFragment other) {
    if (!other.getClass().equals(getClass())) return false;

    AssociativeInfixExpressionFragment otherOfKind = (AssociativeInfixExpressionFragment) other;
    return getOperator() == otherOfKind.getOperator() && doOperandsMatch(otherOfKind);
  }

  private boolean doOperandsMatch(AssociativeInfixExpressionFragment other) {
    if (getOperands().size() != other.getOperands().size()) return false;

    Iterator<Expression> myOperands = getOperands().iterator();
    Iterator<Expression> othersOperands = other.getOperands().iterator();

    while (myOperands.hasNext() && othersOperands.hasNext()) {
      Expression myOperand = myOperands.next();
      Expression othersOperand = othersOperands.next();

      if (!JdtASTMatcher.doNodesMatch(myOperand, othersOperand)) return false;
    }

    return true;
  }

  public IASTFragment[] getSubFragmentsMatching(IASTFragment toMatch) {
    return union(
        getSubFragmentsWithMyNodeMatching(toMatch),
        getSubFragmentsWithAnotherNodeMatching(toMatch));
  }

  private IASTFragment[] getSubFragmentsWithMyNodeMatching(IASTFragment toMatch) {
    if (toMatch.getClass() != getClass()) return new IASTFragment[0];

    AssociativeInfixExpressionFragment kinToMatch = (AssociativeInfixExpressionFragment) toMatch;
    if (kinToMatch.getOperator() != getOperator()) return new IASTFragment[0];

    List<List<Expression>> matchingSubsequences =
        getMatchingContiguousNodeSubsequences(getOperands(), kinToMatch.getOperands());

    IASTFragment[] matches = new IASTFragment[matchingSubsequences.size()];
    for (int i = 0; i < matchingSubsequences.size(); i++) {
      IASTFragment match =
          new AssociativeInfixExpressionFragment(fGroupRoot, matchingSubsequences.get(i));
      Assert.isTrue(match.matches(toMatch) || toMatch.matches(match));
      matches[i] = match;
    }
    return matches;
  }

  private IASTFragment[] getSubFragmentsWithAnotherNodeMatching(IASTFragment toMatch) {
    IASTFragment[] result = new IASTFragment[0];
    for (Iterator<Expression> iter = getOperands().iterator(); iter.hasNext(); ) {
      ASTNode operand = iter.next();
      result =
          union(
              result,
              ASTMatchingFragmentFinder.findMatchingFragments(operand, (ASTFragment) toMatch));
    }
    return result;
  }

  private static IASTFragment[] union(IASTFragment[] a1, IASTFragment[] a2) {
    IASTFragment[] union = new IASTFragment[a1.length + a2.length];
    System.arraycopy(a1, 0, union, 0, a1.length);
    System.arraycopy(a2, 0, union, a1.length, a2.length);
    return union;

    // TODO: this would be a REAL union...:
    //		ArrayList union= new ArrayList();
    //		for (int i= 0; i < a1.length; i++) {
    //			union.add(a1[i]);
    //		}
    //		for (int i= 0; i < a2.length; i++) {
    //			if (! union.contains(a2[i]))
    //				union.add(a2[i]);
    //		}
    //		return (IASTFragment[]) union.toArray(new IASTFragment[union.size()]);
  }

  /**
   * Note that this fragment does not directly represent this expression node, but rather a part of
   * it.
   *
   * @return returns the associated expression.
   */
  public Expression getAssociatedExpression() {
    return fGroupRoot;
  }

  /**
   * Note that this fragment does not directly represent this node, but rather a particular sort of
   * part of its subtree.
   *
   * @return returns the associated node.
   */
  public ASTNode getAssociatedNode() {
    return fGroupRoot;
  }

  public InfixExpression getGroupRoot() {
    return fGroupRoot;
  }

  public int getLength() {
    return getEndPositionExclusive() - getStartPosition();
  }

  private int getEndPositionExclusive() {
    List<Expression> operands = getOperands();
    ASTNode lastNode = operands.get(operands.size() - 1);
    return lastNode.getStartPosition() + lastNode.getLength();
  }

  public int getStartPosition() {
    return getOperands().get(0).getStartPosition();
  }

  public List<Expression> getOperands() {
    return fOperands;
  }

  public InfixExpression.Operator getOperator() {
    return fGroupRoot.getOperator();
  }

  public Expression createCopyTarget(ASTRewrite rewrite, boolean removeSurroundingParenthesis)
      throws JavaModelException {
    List<Expression> allOperands = findGroupMembersInOrderFor(fGroupRoot);
    if (allOperands.size() == fOperands.size()) {
      return (Expression) rewrite.createCopyTarget(fGroupRoot);
    }

    CompilationUnit root = (CompilationUnit) fGroupRoot.getRoot();
    ICompilationUnit cu = (ICompilationUnit) root.getJavaElement();
    String source = cu.getBuffer().getText(getStartPosition(), getLength());
    return (Expression) rewrite.createStringPlaceholder(source, ASTNode.INFIX_EXPRESSION);

    //		//Todo: see whether we could copy bigger chunks of the original selection
    //		// (probably only possible from extendedOperands list or from nested InfixExpressions)
    //		InfixExpression result= rewrite.getAST().newInfixExpression();
    //		result.setOperator(getOperator());
    //		Expression first= (Expression) fOperands.get(0);
    //		Expression second= (Expression) fOperands.get(1);
    //		result.setLeftOperand((Expression) rewrite.createCopyTarget(first));
    //		result.setRightOperand((Expression) rewrite.createCopyTarget(second));
    //		for (int i= 2; i < fOperands.size(); i++) {
    //			Expression next= (Expression) fOperands.get(i);
    //			result.extendedOperands().add(rewrite.createCopyTarget(next));
    //		}
    //		return result;
  }

  public void replace(ASTRewrite rewrite, ASTNode replacement, TextEditGroup textEditGroup) {
    ASTNode groupNode = getGroupRoot();

    List<Expression> allOperands = findGroupMembersInOrderFor(getGroupRoot());
    if (allOperands.size() == fOperands.size()) {
      if (replacement instanceof Name && groupNode.getParent() instanceof ParenthesizedExpression) {
        // replace including the parenthesized expression around it
        rewrite.replace(groupNode.getParent(), replacement, textEditGroup);
      } else {
        rewrite.replace(groupNode, replacement, textEditGroup);
      }
      return;
    }

    rewrite.replace(fOperands.get(0), replacement, textEditGroup);
    int first = allOperands.indexOf(fOperands.get(0));
    int after = first + fOperands.size();
    for (int i = first + 1; i < after; i++) {
      rewrite.remove(allOperands.get(i), textEditGroup);
    }
  }

  private static ArrayList<Expression> findGroupMembersInOrderFor(InfixExpression groupRoot) {
    return new GroupMemberFinder(groupRoot).fMembersInOrder;
  }

  private static class GroupMemberFinder extends GenericVisitor {
    private ArrayList<Expression> fMembersInOrder = new ArrayList<Expression>();
    private InfixExpression fGroupRoot;

    public GroupMemberFinder(InfixExpression groupRoot) {
      super(true);
      Assert.isTrue(isAssociativeInfix(groupRoot));
      fGroupRoot = groupRoot;
      fGroupRoot.accept(this);
    }

    @Override
    protected boolean visitNode(ASTNode node) {
      if (node instanceof InfixExpression
          && ((InfixExpression) node).getOperator() == fGroupRoot.getOperator()) return true;

      fMembersInOrder.add((Expression) node);
      return false;
    }
  }

  @Override
  public int hashCode() {
    return fGroupRoot.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AssociativeInfixExpressionFragment other = (AssociativeInfixExpressionFragment) obj;
    return fGroupRoot.equals(other.fGroupRoot) && fOperands.equals(other.fOperands);
  }
}

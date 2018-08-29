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
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Find all nodes connected to a given binding or node. e.g. Declaration of a field and all
 * references. For types this includes also the constructor declaration, for methods also overridden
 * methods or methods overriding (if existing in the same AST), for constructors also the type and
 * all other constructors.
 */
public class LinkedNodeFinder {

  private LinkedNodeFinder() {}

  /**
   * Find all nodes connected to the given binding. e.g. Declaration of a field and all references.
   * For types this includes also the constructor declaration, for methods also overridden methods
   * or methods overriding (if existing in the same AST)
   *
   * @param root The root of the AST tree to search
   * @param binding The binding of the searched nodes
   * @return Return
   */
  public static SimpleName[] findByBinding(ASTNode root, IBinding binding) {
    ArrayList<SimpleName> res = new ArrayList<SimpleName>();
    BindingFinder nodeFinder = new BindingFinder(binding, res);
    root.accept(nodeFinder);
    return res.toArray(new SimpleName[res.size()]);
  }

  /**
   * Find all nodes connected to the given name node. If the node has a binding then all nodes
   * connected to this binding are returned. If the node has no binding, then all nodes that also
   * miss a binding and have the same name are returned.
   *
   * @param root The root of the AST tree to search
   * @param name The node to find linked nodes for
   * @return Return
   */
  public static SimpleName[] findByNode(ASTNode root, SimpleName name) {
    IBinding binding = name.resolveBinding();
    if (binding != null) {
      return findByBinding(root, binding);
    }
    SimpleName[] names = findByProblems(root, name);
    if (names != null) {
      return names;
    }
    int parentKind = name.getParent().getNodeType();
    if (parentKind == ASTNode.LABELED_STATEMENT
        || parentKind == ASTNode.BREAK_STATEMENT
        || parentKind == ASTNode.CONTINUE_STATEMENT) {
      ArrayList<SimpleName> res = new ArrayList<SimpleName>();
      LabelFinder nodeFinder = new LabelFinder(name, res);
      root.accept(nodeFinder);
      return res.toArray(new SimpleName[res.size()]);
    }
    return new SimpleName[] {name};
  }

  private static final int FIELD = 1;
  private static final int METHOD = 2;
  private static final int TYPE = 4;
  private static final int LABEL = 8;
  private static final int NAME = FIELD | TYPE;

  private static int getProblemKind(IProblem problem) {
    switch (problem.getID()) {
      case IProblem.UndefinedField:
        return FIELD;
      case IProblem.UndefinedMethod:
        return METHOD;
      case IProblem.UndefinedLabel:
        return LABEL;
      case IProblem.UndefinedName:
      case IProblem.UnresolvedVariable:
        return NAME;
      case IProblem.UndefinedType:
        return TYPE;
    }
    return 0;
  }

  private static int getNameNodeProblemKind(IProblem[] problems, SimpleName nameNode) {
    int nameOffset = nameNode.getStartPosition();
    int nameInclEnd = nameOffset + nameNode.getLength() - 1;

    for (int i = 0; i < problems.length; i++) {
      IProblem curr = problems[i];
      if (curr.getSourceStart() == nameOffset && curr.getSourceEnd() == nameInclEnd) {
        int kind = getProblemKind(curr);
        if (kind != 0) {
          return kind;
        }
      }
    }
    return 0;
  }

  public static SimpleName[] findByProblems(ASTNode parent, SimpleName nameNode) {
    ArrayList<SimpleName> res = new ArrayList<SimpleName>();

    ASTNode astRoot = parent.getRoot();
    if (!(astRoot instanceof CompilationUnit)) {
      return null;
    }

    IProblem[] problems = ((CompilationUnit) astRoot).getProblems();
    int nameNodeKind = getNameNodeProblemKind(problems, nameNode);
    if (nameNodeKind == 0) { // no problem on node
      return null;
    }

    int bodyStart = parent.getStartPosition();
    int bodyEnd = bodyStart + parent.getLength();

    String name = nameNode.getIdentifier();

    for (int i = 0; i < problems.length; i++) {
      IProblem curr = problems[i];
      int probStart = curr.getSourceStart();
      int probEnd = curr.getSourceEnd() + 1;

      if (probStart > bodyStart && probEnd < bodyEnd) {
        int currKind = getProblemKind(curr);
        if ((nameNodeKind & currKind) != 0) {
          ASTNode node = NodeFinder.perform(parent, probStart, (probEnd - probStart));
          if (node instanceof SimpleName && name.equals(((SimpleName) node).getIdentifier())) {
            res.add((SimpleName) node);
          }
        }
      }
    }
    return res.toArray(new SimpleName[res.size()]);
  }

  private static class LabelFinder extends ASTVisitor {

    private SimpleName fLabel;
    private ASTNode fDefiningLabel;
    private ArrayList<SimpleName> fResult;

    public LabelFinder(SimpleName label, ArrayList<SimpleName> result) {
      super(true);
      fLabel = label;
      fResult = result;
      fDefiningLabel = null;
    }

    private boolean isSameLabel(SimpleName label) {
      return label != null && fLabel.getIdentifier().equals(label.getIdentifier());
    }

    @Override
    public boolean visit(BreakStatement node) {
      SimpleName label = node.getLabel();
      if (fDefiningLabel != null
          && isSameLabel(label)
          && ASTNodes.isParent(label, fDefiningLabel)) {
        fResult.add(label);
      }
      return false;
    }

    @Override
    public boolean visit(ContinueStatement node) {
      SimpleName label = node.getLabel();
      if (fDefiningLabel != null
          && isSameLabel(label)
          && ASTNodes.isParent(label, fDefiningLabel)) {
        fResult.add(label);
      }
      return false;
    }

    @Override
    public boolean visit(LabeledStatement node) {
      if (fDefiningLabel == null) {
        SimpleName label = node.getLabel();
        if (fLabel == label || isSameLabel(label) && ASTNodes.isParent(fLabel, node)) {
          fDefiningLabel = node;
          fResult.add(label);
        }
      }
      node.getBody().accept(this);
      return false;
    }
  }

  private static class BindingFinder extends ASTVisitor {

    private IBinding fBinding;
    private ArrayList<SimpleName> fResult;

    public BindingFinder(IBinding binding, ArrayList<SimpleName> result) {
      super(true);
      fBinding = getDeclaration(binding);
      fResult = result;
    }

    @Override
    public boolean visit(SimpleName node) {
      IBinding binding = node.resolveBinding();
      if (binding == null) {
        return false;
      }
      binding = getDeclaration(binding);

      if (fBinding == binding) {
        fResult.add(node);
      } else if (binding.getKind() != fBinding.getKind()) {
        return false;
      } else if (binding.getKind() == IBinding.METHOD) {
        IMethodBinding curr = (IMethodBinding) binding;
        IMethodBinding methodBinding = (IMethodBinding) fBinding;
        if (methodBinding.overrides(curr) || curr.overrides(methodBinding)) {
          fResult.add(node);
        }
      }
      return false;
    }

    private static IBinding getDeclaration(IBinding binding) {
      if (binding instanceof ITypeBinding) {
        return ((ITypeBinding) binding).getTypeDeclaration();
      } else if (binding instanceof IMethodBinding) {
        IMethodBinding methodBinding = (IMethodBinding) binding;
        if (methodBinding.isConstructor()) { // link all constructors with their type
          return methodBinding.getDeclaringClass().getTypeDeclaration();
        } else {
          return methodBinding.getMethodDeclaration();
        }
      } else if (binding instanceof IVariableBinding) {
        return ((IVariableBinding) binding).getVariableDeclaration();
      }
      return binding;
    }
  }
}

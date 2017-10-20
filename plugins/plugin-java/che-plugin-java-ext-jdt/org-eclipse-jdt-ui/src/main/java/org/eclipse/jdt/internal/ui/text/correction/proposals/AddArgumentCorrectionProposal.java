/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.lang.reflect.Modifier;
import java.util.List;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;

public class AddArgumentCorrectionProposal extends LinkedCorrectionProposal {

  private int[] fInsertIndexes;
  private ITypeBinding[] fParamTypes;
  private ASTNode fCallerNode;

  public AddArgumentCorrectionProposal(
      String label,
      ICompilationUnit cu,
      ASTNode callerNode,
      int[] insertIdx,
      ITypeBinding[] expectedTypes,
      int relevance) {
    super(label, cu, null, relevance, JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
    fCallerNode = callerNode;
    fInsertIndexes = insertIdx;
    fParamTypes = expectedTypes;
  }

  /*(non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ASTRewriteCorrectionProposal#getRewrite()
   */
  @Override
  protected ASTRewrite getRewrite() {
    AST ast = fCallerNode.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    ChildListPropertyDescriptor property = getProperty();

    for (int i = 0; i < fInsertIndexes.length; i++) {
      int idx = fInsertIndexes[i];
      String key = "newarg_" + i; // $NON-NLS-1$
      Expression newArg = evaluateArgumentExpressions(ast, fParamTypes[idx], key);
      ListRewrite listRewriter = rewrite.getListRewrite(fCallerNode, property);
      listRewriter.insertAt(newArg, idx, null);

      addLinkedPosition(rewrite.track(newArg), i == 0, key);
    }
    return rewrite;
  }

  private ChildListPropertyDescriptor getProperty() {
    List<StructuralPropertyDescriptor> list = fCallerNode.structuralPropertiesForType();
    for (int i = 0; i < list.size(); i++) {
      StructuralPropertyDescriptor curr = list.get(i);
      if (curr.isChildListProperty() && "arguments".equals(curr.getId())) { // $NON-NLS-1$
        return (ChildListPropertyDescriptor) curr;
      }
    }
    return null;
  }

  private Expression evaluateArgumentExpressions(AST ast, ITypeBinding requiredType, String key) {
    CompilationUnit root = (CompilationUnit) fCallerNode.getRoot();

    int offset = fCallerNode.getStartPosition();
    Expression best = null;
    ITypeBinding bestType = null;

    ScopeAnalyzer analyzer = new ScopeAnalyzer(root);
    IBinding[] bindings = analyzer.getDeclarationsInScope(offset, ScopeAnalyzer.VARIABLES);
    for (int i = 0; i < bindings.length; i++) {
      IVariableBinding curr = (IVariableBinding) bindings[i];
      ITypeBinding type = curr.getType();
      if (type != null && canAssign(type, requiredType) && testModifier(curr)) {
        if (best == null || isMoreSpecific(bestType, type)) {
          best = ast.newSimpleName(curr.getName());
          bestType = type;
        }
        addLinkedPositionProposal(key, curr.getName(), null);
      }
    }
    Expression defaultExpression = ASTNodeFactory.newDefaultExpression(ast, requiredType);
    if (best == null) {
      best = defaultExpression;
    }
    addLinkedPositionProposal(key, ASTNodes.asString(defaultExpression), null);
    return best;
  }

  private boolean isMoreSpecific(ITypeBinding best, ITypeBinding curr) {
    return (canAssign(best, curr) && !canAssign(curr, best));
  }

  private boolean canAssign(ITypeBinding curr, ITypeBinding best) {
    return curr.isAssignmentCompatible(best);
  }

  private boolean testModifier(IVariableBinding curr) {
    int modifiers = curr.getModifiers();
    int staticFinal = Modifier.STATIC | Modifier.FINAL;
    if ((modifiers & staticFinal) == staticFinal) {
      return false;
    }
    if (Modifier.isStatic(modifiers) && !ASTResolving.isInStaticContext(fCallerNode)) {
      return false;
    }
    return true;
  }
}

/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
/** */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.text.edits.TextEditGroup;

public abstract class ConvertLoopOperation extends CompilationUnitRewriteOperation {

  protected static final String FOR_LOOP_ELEMENT_IDENTIFIER = "element"; // $NON-NLS-1$

  protected static final IStatus ERROR_STATUS =
      new Status(IStatus.ERROR, JavaPlugin.getPluginId(), ""); // $NON-NLS-1$

  private final ForStatement fStatement;
  private ConvertLoopOperation fOperation;
  private final String[] fUsedNames;

  public ConvertLoopOperation(ForStatement statement, String[] usedNames) {
    fStatement = statement;
    fUsedNames = usedNames;
  }

  public void setBodyConverter(ConvertLoopOperation operation) {
    fOperation = operation;
  }

  public abstract String getIntroducedVariableName();

  public abstract IStatus satisfiesPreconditions();

  protected abstract Statement convert(
      CompilationUnitRewrite cuRewrite, TextEditGroup group, LinkedProposalModel positionGroups)
      throws CoreException;

  protected ForStatement getForStatement() {
    return fStatement;
  }

  protected Statement getBody(
      CompilationUnitRewrite cuRewrite, TextEditGroup group, LinkedProposalModel positionGroups)
      throws CoreException {
    if (fOperation != null) {
      return fOperation.convert(cuRewrite, group, positionGroups);
    } else {
      return (Statement) cuRewrite.getASTRewrite().createMoveTarget(getForStatement().getBody());
    }
  }

  protected String[] getUsedVariableNames() {
    final List<String> results = new ArrayList<String>();

    ForStatement forStatement = getForStatement();
    CompilationUnit root = (CompilationUnit) forStatement.getRoot();

    Collection<String> variableNames =
        new ScopeAnalyzer(root)
            .getUsedVariableNames(forStatement.getStartPosition(), forStatement.getLength());
    results.addAll(variableNames);

    forStatement.accept(
        new GenericVisitor() {
          @Override
          public boolean visit(SingleVariableDeclaration node) {
            results.add(node.getName().getIdentifier());
            return super.visit(node);
          }

          @Override
          public boolean visit(VariableDeclarationFragment fragment) {
            results.add(fragment.getName().getIdentifier());
            return super.visit(fragment);
          }
        });

    results.addAll(Arrays.asList(fUsedNames));

    return results.toArray(new String[results.size()]);
  }
}

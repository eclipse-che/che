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
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;

public class ConvertLoopFix extends CompilationUnitRewriteOperationsFix {

  private static final class ControlStatementFinder extends GenericVisitor {

    private final List<ConvertLoopOperation> fResult;
    private final Hashtable<ForStatement, String> fUsedNames;
    private final boolean fFindForLoopsToConvert;
    private final boolean fConvertIterableForLoops;
    private final boolean fMakeFinal;

    public ControlStatementFinder(
        boolean findForLoopsToConvert,
        boolean convertIterableForLoops,
        boolean makeFinal,
        List<ConvertLoopOperation> resultingCollection) {
      fFindForLoopsToConvert = findForLoopsToConvert;
      fConvertIterableForLoops = convertIterableForLoops;
      fMakeFinal = makeFinal;
      fResult = resultingCollection;
      fUsedNames = new Hashtable<ForStatement, String>();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visit(org.eclipse.jdt.core.dom.ForStatement)
     */
    @Override
    public boolean visit(ForStatement node) {
      if (fFindForLoopsToConvert || fConvertIterableForLoops) {
        ForStatement current = node;
        ConvertLoopOperation operation = getConvertOperation(current);
        ConvertLoopOperation oldOperation = null;
        while (operation != null) {
          if (oldOperation == null) {
            fResult.add(operation);
          } else {
            oldOperation.setBodyConverter(operation);
          }

          if (current.getBody() instanceof ForStatement) {
            current = (ForStatement) current.getBody();
            oldOperation = operation;
            operation = getConvertOperation(current);
          } else {
            operation = null;
          }
        }
        current.getBody().accept(this);
        return false;
      }

      return super.visit(node);
    }

    private ConvertLoopOperation getConvertOperation(ForStatement node) {

      Collection<String> usedNamesCollection = fUsedNames.values();
      String[] usedNames = usedNamesCollection.toArray(new String[usedNamesCollection.size()]);
      ConvertLoopOperation convertForLoopOperation =
          new ConvertForLoopOperation(node, usedNames, fMakeFinal);
      if (convertForLoopOperation.satisfiesPreconditions().isOK()) {
        if (fFindForLoopsToConvert) {
          fUsedNames.put(node, convertForLoopOperation.getIntroducedVariableName());
          return convertForLoopOperation;
        }
      } else if (fConvertIterableForLoops) {
        ConvertLoopOperation iterableConverter =
            new ConvertIterableLoopOperation(node, usedNames, fMakeFinal);
        if (iterableConverter.satisfiesPreconditions().isOK()) {
          fUsedNames.put(node, iterableConverter.getIntroducedVariableName());
          return iterableConverter;
        }
      }

      return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#endVisit(org.eclipse.jdt.core.dom.ForStatement)
     */
    @Override
    public void endVisit(ForStatement node) {
      if (fFindForLoopsToConvert || fConvertIterableForLoops) {
        fUsedNames.remove(node);
      }
      super.endVisit(node);
    }
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit,
      boolean convertForLoops,
      boolean convertIterableForLoops,
      boolean makeFinal) {
    if (!JavaModelUtil.is50OrHigher(compilationUnit.getJavaElement().getJavaProject())) return null;

    if (!convertForLoops && !convertIterableForLoops) return null;

    List<ConvertLoopOperation> operations = new ArrayList<ConvertLoopOperation>();
    ControlStatementFinder finder =
        new ControlStatementFinder(convertForLoops, convertIterableForLoops, makeFinal, operations);
    compilationUnit.accept(finder);

    if (operations.isEmpty()) return null;

    CompilationUnitRewriteOperation[] ops =
        operations.toArray(new CompilationUnitRewriteOperation[operations.size()]);
    return new ConvertLoopFix(
        FixMessages.ControlStatementsFix_change_name, compilationUnit, ops, null);
  }

  public static ConvertLoopFix createConvertForLoopToEnhancedFix(
      CompilationUnit compilationUnit, ForStatement loop) {
    ConvertLoopOperation convertForLoopOperation = new ConvertForLoopOperation(loop);
    if (!convertForLoopOperation.satisfiesPreconditions().isOK()) return null;

    return new ConvertLoopFix(
        FixMessages.Java50Fix_ConvertToEnhancedForLoop_description,
        compilationUnit,
        new CompilationUnitRewriteOperation[] {convertForLoopOperation},
        null);
  }

  public static ConvertLoopFix createConvertIterableLoopToEnhancedFix(
      CompilationUnit compilationUnit, ForStatement loop) {
    ConvertIterableLoopOperation loopConverter = new ConvertIterableLoopOperation(loop);
    IStatus status = loopConverter.satisfiesPreconditions();
    if (status.getSeverity() == IStatus.ERROR) return null;

    return new ConvertLoopFix(
        FixMessages.Java50Fix_ConvertToEnhancedForLoop_description,
        compilationUnit,
        new CompilationUnitRewriteOperation[] {loopConverter},
        status);
  }

  private final IStatus fStatus;

  protected ConvertLoopFix(
      String name,
      CompilationUnit compilationUnit,
      CompilationUnitRewriteOperation[] fixRewriteOperations,
      IStatus status) {
    super(name, compilationUnit, fixRewriteOperations);
    fStatus = status;
  }

  /** {@inheritDoc} */
  @Override
  public IStatus getStatus() {
    return fStatus;
  }
}

/**
 * ***************************************************************************** Copyright (c) 2014
 * IBM Corporation and others. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.fix;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.TypeParametersFix;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

public class TypeParametersCleanUp extends AbstractMultiFix {

  private Map<String, String> fOptions;

  public TypeParametersCleanUp(Map<String, String> options) {
    super(options);
    fOptions = options;
  }

  public TypeParametersCleanUp() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public CleanUpRequirements getRequirements() {
    boolean requireAST = requireAST();
    Map<String, String> requiredOptions = requireAST ? getRequiredOptions() : null;
    return new CleanUpRequirements(requireAST, false, false, requiredOptions);
  }

  private boolean requireAST() {
    boolean useTypeArguments = isEnabled(CleanUpConstants.USE_TYPE_ARGUMENTS);
    if (!useTypeArguments) return false;

    return isEnabled(CleanUpConstants.INSERT_INFERRED_TYPE_ARGUMENTS)
        || isEnabled(CleanUpConstants.REMOVE_REDUNDANT_TYPE_ARGUMENTS);
  }

  private Map<String, String> getRequiredOptions() {
    Map<String, String> result = new Hashtable<String, String>();

    if (isEnabled(CleanUpConstants.REMOVE_REDUNDANT_TYPE_ARGUMENTS))
      result.put(JavaCore.COMPILER_PB_REDUNDANT_TYPE_ARGUMENTS, JavaCore.WARNING);

    return result;
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit) throws CoreException {
    if (compilationUnit == null) return null;

    boolean useTypeParameters = isEnabled(CleanUpConstants.USE_TYPE_ARGUMENTS);
    if (!useTypeParameters) return null;

    return TypeParametersFix.createCleanUp(
        compilationUnit,
        isEnabled(CleanUpConstants.INSERT_INFERRED_TYPE_ARGUMENTS),
        isEnabled(CleanUpConstants.REMOVE_REDUNDANT_TYPE_ARGUMENTS));
  }

  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit, IProblemLocation[] problems)
      throws CoreException {
    if (compilationUnit == null) return null;

    boolean useTypeParameters = isEnabled(CleanUpConstants.USE_TYPE_ARGUMENTS);
    if (!useTypeParameters) return null;

    return TypeParametersFix.createCleanUp(
        compilationUnit,
        problems,
        isEnabled(CleanUpConstants.INSERT_INFERRED_TYPE_ARGUMENTS),
        isEnabled(CleanUpConstants.REMOVE_REDUNDANT_TYPE_ARGUMENTS));
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();
    if (isEnabled(CleanUpConstants.USE_TYPE_ARGUMENTS)
        && isEnabled(CleanUpConstants.INSERT_INFERRED_TYPE_ARGUMENTS)) {
      result.add(MultiFixMessages.TypeParametersCleanUp_InsertInferredTypeArguments_description);
    } else if (isEnabled(CleanUpConstants.USE_TYPE_ARGUMENTS)
        && isEnabled(CleanUpConstants.REMOVE_REDUNDANT_TYPE_ARGUMENTS)) {
      result.add(MultiFixMessages.TypeParametersCleanUp_RemoveRedundantTypeArguments_description);
    }

    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
    int problemId = problem.getProblemId();

    if (problemId == IProblem.RedundantSpecificationOfTypeArguments)
      return isEnabled(CleanUpConstants.REMOVE_REDUNDANT_TYPE_ARGUMENTS);
    if (problemId == IProblem.DiamondNotBelow17)
      return isEnabled(CleanUpConstants.INSERT_INFERRED_TYPE_ARGUMENTS);

    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int computeNumberOfFixes(CompilationUnit compilationUnit) {
    if (fOptions == null) return 0;
    int result = 0;
    IProblem[] problems = compilationUnit.getProblems();
    if (isEnabled(CleanUpConstants.REMOVE_REDUNDANT_TYPE_ARGUMENTS))
      result = getNumberOfProblems(problems, IProblem.RedundantSpecificationOfTypeArguments);
    else if (isEnabled(CleanUpConstants.INSERT_INFERRED_TYPE_ARGUMENTS))
      result = getNumberOfProblems(problems, IProblem.DiamondNotBelow17);
    return result;
  }
}

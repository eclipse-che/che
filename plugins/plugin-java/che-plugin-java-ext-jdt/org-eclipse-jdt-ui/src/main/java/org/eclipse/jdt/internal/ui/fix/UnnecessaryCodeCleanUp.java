/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
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
import org.eclipse.jdt.internal.corext.fix.UnusedCodeFix;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

public class UnnecessaryCodeCleanUp extends AbstractMultiFix {

  public UnnecessaryCodeCleanUp(Map<String, String> options) {
    super(options);
  }

  public UnnecessaryCodeCleanUp() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public CleanUpRequirements getRequirements() {
    boolean requireAST = isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_CASTS);
    Map<String, String> requiredOptions = requireAST ? getRequiredOptions() : null;
    return new CleanUpRequirements(requireAST, false, false, requiredOptions);
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit) throws CoreException {
    return UnusedCodeFix.createCleanUp(
        compilationUnit,
        false,
        false,
        false,
        false,
        false,
        false,
        isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_CASTS));
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit, IProblemLocation[] problems)
      throws CoreException {
    return UnusedCodeFix.createCleanUp(
        compilationUnit,
        problems,
        false,
        false,
        false,
        false,
        false,
        false,
        isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_CASTS));
  }

  private Map<String, String> getRequiredOptions() {
    Map<String, String> result = new Hashtable<String, String>();

    if (isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_CASTS))
      result.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.WARNING);

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();
    if (isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_CASTS))
      result.add(MultiFixMessages.UnusedCodeCleanUp_RemoveUnusedCasts_description);
    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    StringBuffer buf = new StringBuffer();

    if (isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_CASTS)) {
      buf.append("Boolean b= Boolean.TRUE;\n"); // $NON-NLS-1$
    } else {
      buf.append("Boolean b= (Boolean) Boolean.TRUE;\n"); // $NON-NLS-1$
    }

    return buf.toString();
  }

  /** {@inheritDoc} */
  public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
    if (problem.getProblemId() == IProblem.UnnecessaryCast)
      return isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_CASTS);

    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int computeNumberOfFixes(CompilationUnit compilationUnit) {
    int result = 0;
    IProblem[] problems = compilationUnit.getProblems();
    if (isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_CASTS))
      result += getNumberOfProblems(problems, IProblem.UnnecessaryCast);
    return result;
  }
}

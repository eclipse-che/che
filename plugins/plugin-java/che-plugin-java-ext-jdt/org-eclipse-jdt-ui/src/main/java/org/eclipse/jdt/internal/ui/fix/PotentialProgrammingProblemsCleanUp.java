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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.PotentialProgrammingProblemsFix;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class PotentialProgrammingProblemsCleanUp extends AbstractMultiFix {

  public PotentialProgrammingProblemsCleanUp(Map<String, String> options) {
    super(options);
  }

  public PotentialProgrammingProblemsCleanUp() {
    super();
  }

  /** A {@inheritDoc} */
  @Override
  public CleanUpRequirements getRequirements() {
    boolean requireAST = requireAST();
    Map<String, String> requiredOptions = requireAST ? getRequiredOptions() : null;
    return new CleanUpRequirements(requireAST, false, false, requiredOptions);
  }

  private boolean requireAST() {
    boolean addSUID = isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID);
    if (!addSUID) return false;

    return isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED)
        || isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT);
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit) throws CoreException {

    boolean addSUID = isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID);
    if (!addSUID) return null;

    return PotentialProgrammingProblemsFix.createCleanUp(
        compilationUnit,
        isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED)
            || isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT));
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit, IProblemLocation[] problems)
      throws CoreException {
    if (compilationUnit == null) return null;

    return PotentialProgrammingProblemsFix.createCleanUp(
        compilationUnit,
        problems,
        (isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
                && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED))
            || (isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
                && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT)));
  }

  private Map<String, String> getRequiredOptions() {
    Map<String, String> result = new Hashtable<String, String>();
    if ((isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
            && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED))
        || (isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
            && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT)))
      result.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.WARNING);
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();

    if ((isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
        && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED)))
      result.add(MultiFixMessages.SerialVersionCleanUp_Generated_description);
    if ((isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
        && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT)))
      result.add(MultiFixMessages.CodeStyleCleanUp_addDefaultSerialVersionId_description);

    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    StringBuffer buf = new StringBuffer();

    buf.append("class E implements java.io.Serializable {\n"); // $NON-NLS-1$
    if ((isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
        && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED))) {
      buf.append(
          "    private static final long serialVersionUID = -391484377137870342L;\n"); // $NON-NLS-1$
    } else if ((isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
        && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT))) {
      buf.append("    private static final long serialVersionUID = 1L;\n"); // $NON-NLS-1$
    }
    buf.append("}\n"); // $NON-NLS-1$

    return buf.toString();
  }

  /** {@inheritDoc} */
  public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
    if (problem.getProblemId() == IProblem.MissingSerialVersion)
      return (isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
              && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED))
          || (isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
              && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT));

    return false;
  }

  /** {@inheritDoc} */
  @Override
  public RefactoringStatus checkPreConditions(
      IJavaProject project, ICompilationUnit[] compilationUnits, IProgressMonitor monitor)
      throws CoreException {
    RefactoringStatus superStatus = super.checkPreConditions(project, compilationUnits, monitor);
    if (superStatus.hasFatalError()) return superStatus;

    return PotentialProgrammingProblemsFix.checkPreConditions(
        project,
        compilationUnits,
        monitor,
        isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
            && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED),
        isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
            && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT),
        false);
  }

  /** {@inheritDoc} */
  @Override
  public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
    return PotentialProgrammingProblemsFix.checkPostConditions(monitor);
  }

  /** {@inheritDoc} */
  @Override
  public int computeNumberOfFixes(CompilationUnit compilationUnit) {
    if ((isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
            && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED))
        || (isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID)
            && isEnabled(CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT)))
      return getNumberOfProblems(compilationUnit.getProblems(), IProblem.MissingSerialVersion);

    return 0;
  }
}

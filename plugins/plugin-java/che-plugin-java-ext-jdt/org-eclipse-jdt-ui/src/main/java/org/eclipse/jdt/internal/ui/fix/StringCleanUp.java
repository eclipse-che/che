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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.StringFix;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

/**
 * Create fixes which can solve problems in connection with Strings
 *
 * @see org.eclipse.jdt.internal.corext.fix.StringFix
 */
public class StringCleanUp extends AbstractMultiFix {

  public StringCleanUp(Map<String, String> options) {
    super(options);
  }

  public StringCleanUp() {
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
    return isEnabled(CleanUpConstants.ADD_MISSING_NLS_TAGS)
        || isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_NLS_TAGS);
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit) throws CoreException {
    if (compilationUnit == null) return null;

    return StringFix.createCleanUp(
        compilationUnit,
        isEnabled(CleanUpConstants.ADD_MISSING_NLS_TAGS),
        isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_NLS_TAGS));
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit, IProblemLocation[] problems)
      throws CoreException {
    if (compilationUnit == null) return null;

    return StringFix.createCleanUp(
        compilationUnit,
        problems,
        isEnabled(CleanUpConstants.ADD_MISSING_NLS_TAGS),
        isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_NLS_TAGS));
  }

  private Map<String, String> getRequiredOptions() {
    Map<String, String> result = new Hashtable<String, String>();

    if (isEnabled(CleanUpConstants.ADD_MISSING_NLS_TAGS)
        || isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_NLS_TAGS))
      result.put(JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL, JavaCore.WARNING);

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();
    if (isEnabled(CleanUpConstants.ADD_MISSING_NLS_TAGS))
      result.add(MultiFixMessages.StringMultiFix_AddMissingNonNls_description);
    if (isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_NLS_TAGS))
      result.add(MultiFixMessages.StringMultiFix_RemoveUnnecessaryNonNls_description);
    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    StringBuffer buf = new StringBuffer();

    if (isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_NLS_TAGS)) {
      buf.append("public String s;"); // $NON-NLS-1$
    } else {
      buf.append("public String s; //$NON-NLS-1$"); // $NON-NLS-1$
    }

    return buf.toString();
  }

  /** {@inheritDoc} */
  public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
    if (problem.getProblemId() == IProblem.UnnecessaryNLSTag)
      return isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_NLS_TAGS);

    if (problem.getProblemId() == IProblem.NonExternalizedStringLiteral)
      return isEnabled(CleanUpConstants.ADD_MISSING_NLS_TAGS);

    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int computeNumberOfFixes(CompilationUnit compilationUnit) {
    try {
      ICompilationUnit cu = (ICompilationUnit) compilationUnit.getJavaElement();
      if (!cu.isStructureKnown())
        return 0; // [clean up] 'Remove unnecessary $NLS-TAGS$' removes necessary ones in case of
      // syntax errors: https://bugs.eclipse.org/bugs/show_bug.cgi?id=285814 :
    } catch (JavaModelException e) {
      return 0;
    }

    int result = 0;
    IProblem[] problems = compilationUnit.getProblems();
    if (isEnabled(CleanUpConstants.ADD_MISSING_NLS_TAGS))
      result += getNumberOfProblems(problems, IProblem.NonExternalizedStringLiteral);

    if (isEnabled(CleanUpConstants.REMOVE_UNNECESSARY_NLS_TAGS))
      result += getNumberOfProblems(problems, IProblem.UnnecessaryNLSTag);

    return result;
  }
}

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
import org.eclipse.jdt.internal.corext.fix.Java50Fix;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

/**
 * Create fixes which can transform pre Java50 code to Java50 code
 *
 * @see org.eclipse.jdt.internal.corext.fix.Java50Fix
 */
public class Java50CleanUp extends AbstractMultiFix {

  public Java50CleanUp(Map<String, String> options) {
    super(options);
  }

  public Java50CleanUp() {
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
    boolean addAnotations = isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS);

    return addAnotations && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE)
        || addAnotations && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED)
        || isEnabled(
            CleanUpConstants.VARIABLE_DECLARATION_USE_TYPE_ARGUMENTS_FOR_RAW_TYPE_REFERENCES);
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit) throws CoreException {
    boolean addAnotations = isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS);
    boolean addOverride = isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE);
    return Java50Fix.createCleanUp(
        compilationUnit,
        addAnotations && addOverride,
        addAnotations
            && addOverride
            && isEnabled(
                CleanUpConstants
                    .ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION),
        addAnotations && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED),
        isEnabled(
            CleanUpConstants.VARIABLE_DECLARATION_USE_TYPE_ARGUMENTS_FOR_RAW_TYPE_REFERENCES));
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit, IProblemLocation[] problems)
      throws CoreException {
    if (compilationUnit == null) return null;

    boolean addAnnotations = isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS);
    boolean addOverride = isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE);
    return Java50Fix.createCleanUp(
        compilationUnit,
        problems,
        addAnnotations && addOverride,
        addAnnotations
            && addOverride
            && isEnabled(
                CleanUpConstants
                    .ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION),
        addAnnotations && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED),
        isEnabled(
            CleanUpConstants.VARIABLE_DECLARATION_USE_TYPE_ARGUMENTS_FOR_RAW_TYPE_REFERENCES));
  }

  private Map<String, String> getRequiredOptions() {
    Map<String, String> result = new Hashtable<String, String>();
    if (isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
        && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE)) {
      result.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.WARNING);
      if (isEnabled(
          CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION)) {
        result.put(
            JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION,
            JavaCore.ENABLED);
      }
    }

    if (isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
        && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED))
      result.put(JavaCore.COMPILER_PB_MISSING_DEPRECATED_ANNOTATION, JavaCore.WARNING);

    if (isEnabled(CleanUpConstants.VARIABLE_DECLARATION_USE_TYPE_ARGUMENTS_FOR_RAW_TYPE_REFERENCES))
      result.put(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.WARNING);

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();
    if (isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
        && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE)) {
      result.add(MultiFixMessages.Java50MultiFix_AddMissingOverride_description);
      if (isEnabled(
          CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION)) {
        result.add(MultiFixMessages.Java50MultiFix_AddMissingOverride_description2);
      }
    }
    if (isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
        && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED))
      result.add(MultiFixMessages.Java50MultiFix_AddMissingDeprecated_description);
    if (isEnabled(CleanUpConstants.VARIABLE_DECLARATION_USE_TYPE_ARGUMENTS_FOR_RAW_TYPE_REFERENCES))
      result.add(MultiFixMessages.Java50CleanUp_AddTypeParameters_description);
    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    StringBuffer buf = new StringBuffer();

    buf.append("class E {\n"); // $NON-NLS-1$
    buf.append("    /**\n"); // $NON-NLS-1$
    buf.append("     * @deprecated\n"); // $NON-NLS-1$
    buf.append("     */\n"); // $NON-NLS-1$
    if (isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
        && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED)) {
      buf.append("    @Deprecated\n"); // $NON-NLS-1$
    }
    buf.append("    public void foo() {}\n"); // $NON-NLS-1$
    buf.append("}\n"); // $NON-NLS-1$
    buf.append("class ESub extends E implements Runnable {\n"); // $NON-NLS-1$
    if (isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
        && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE)) {
      buf.append("    @Override\n"); // $NON-NLS-1$
    }
    buf.append("    public void foo() {}\n"); // $NON-NLS-1$
    if (isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
        && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE)
        && isEnabled(
            CleanUpConstants
                .ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION)) {
      buf.append("    @Override\n"); // $NON-NLS-1$
    }
    buf.append("    public void run() {}\n"); // $NON-NLS-1$
    buf.append("}\n"); // $NON-NLS-1$

    return buf.toString();
  }

  /** {@inheritDoc} */
  public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
    int id = problem.getProblemId();

    if (Java50Fix.isMissingOverrideAnnotationProblem(id)) {
      if (isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
          && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE)) {
        return !Java50Fix.isMissingOverrideAnnotationInterfaceProblem(id)
            || isEnabled(
                CleanUpConstants
                    .ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION);
      }

    } else if (Java50Fix.isMissingDeprecationProblem(id)) {
      return isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS)
          && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED);

    } else if (Java50Fix.isRawTypeReferenceProblem(id)) {
      return isEnabled(
          CleanUpConstants.VARIABLE_DECLARATION_USE_TYPE_ARGUMENTS_FOR_RAW_TYPE_REFERENCES);
    }

    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int computeNumberOfFixes(CompilationUnit compilationUnit) {
    int result = 0;

    boolean addAnnotations = isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS);
    boolean addMissingOverride =
        addAnnotations && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE);
    boolean addMissingOverrideInterfaceMethods =
        addMissingOverride
            && isEnabled(
                CleanUpConstants
                    .ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION);
    boolean addMissingDeprecated =
        addAnnotations && isEnabled(CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED);
    boolean useTypeArgs =
        isEnabled(CleanUpConstants.VARIABLE_DECLARATION_USE_TYPE_ARGUMENTS_FOR_RAW_TYPE_REFERENCES);

    IProblem[] problems = compilationUnit.getProblems();
    for (int i = 0; i < problems.length; i++) {
      int id = problems[i].getID();
      if (addMissingOverride && Java50Fix.isMissingOverrideAnnotationProblem(id))
        if (!Java50Fix.isMissingOverrideAnnotationInterfaceProblem(id)
            || addMissingOverrideInterfaceMethods) result++;
      if (addMissingDeprecated && Java50Fix.isMissingDeprecationProblem(id)) result++;
      if (useTypeArgs && Java50Fix.isRawTypeReferenceProblem(id)) result++;
    }
    return result;
  }
}

/**
 * ***************************************************************************** Copyright (c) 2011,
 * 2013 GK Software AG and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Stephan Herrmann - [quick fix] Add quick fixes for null annotations -
 * https://bugs.eclipse.org/337977 IBM Corporation - bug fixes
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
import org.eclipse.jdt.internal.corext.fix.NullAnnotationsFix;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

/**
 * Cleanup for adding required null annotations.
 *
 * <p>Crafted after the lead of Java50CleanUp
 */
public class NullAnnotationsCleanUp extends AbstractMultiFix {

  private int handledProblemID;

  public NullAnnotationsCleanUp(Map<String, String> options, int handledProblemID) {
    super(options);
    this.handledProblemID = handledProblemID;
  }

  /** {@inheritDoc} */
  @Override
  public CleanUpRequirements getRequirements() {
    Map<String, String> requiredOptions = getRequiredOptions();
    return new CleanUpRequirements(true, false, false, requiredOptions);
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit) throws CoreException {
    return this.createFix(compilationUnit, null);
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit, IProblemLocation[] problems)
      throws CoreException {
    if (compilationUnit == null) return null;
    IProblemLocation[] locations = null;
    ArrayList<IProblemLocation> filteredLocations = new ArrayList<IProblemLocation>();
    if (problems != null) {
      for (int i = 0; i < problems.length; i++) {
        if (problems[i].getProblemId() == this.handledProblemID) filteredLocations.add(problems[i]);
      }
      locations = filteredLocations.toArray(new IProblemLocation[filteredLocations.size()]);
    }
    return NullAnnotationsFix.createCleanUp(compilationUnit, locations, this.handledProblemID);
  }

  private Map<String, String> getRequiredOptions() {
    Map<String, String> result = new Hashtable<String, String>();
    // TODO(SH): might set depending on this.handledProblemID, not sure about the benefit
    result.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING);
    result.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
    result.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.WARNING);
    result.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.WARNING);
    result.put(JavaCore.COMPILER_PB_NULL_ANNOTATION_INFERENCE_CONFLICT, JavaCore.WARNING);
    result.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.WARNING);
    result.put(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.WARNING);
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();
    switch (this.handledProblemID) {
      case IProblem.NonNullLocalVariableComparisonYieldsFalse:
      case IProblem.RedundantNullCheckOnNonNullLocalVariable:
      case IProblem.RequiredNonNullButProvidedNull:
      case IProblem.RequiredNonNullButProvidedPotentialNull:
      case IProblem.RequiredNonNullButProvidedSpecdNullable:
      case IProblem.RequiredNonNullButProvidedUnknown:
      case IProblem.IllegalDefinitionToNonNullParameter:
      case IProblem.IllegalRedefinitionToNonNullParameter:
      case IProblem.ParameterLackingNullableAnnotation:
        result.add(MultiFixMessages.NullAnnotationsCleanUp_add_nullable_annotation);
        break;
      case IProblem.ParameterLackingNonNullAnnotation:
        result.add(MultiFixMessages.NullAnnotationsCleanUp_add_nonnull_annotation);
        break;
      case IProblem.RedundantNullAnnotation:
      case IProblem.RedundantNullDefaultAnnotationPackage:
      case IProblem.RedundantNullDefaultAnnotationType:
      case IProblem.RedundantNullDefaultAnnotationMethod:
        result.add(MultiFixMessages.NullAnnotationsCleanUp_remove_redundant_nullness_annotation);
        break;
    }
    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    // not used when not provided as a true cleanup(?)
    return "No preview available"; // $NON-NLS-1$
  }

  /** {@inheritDoc} */
  public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
    int id = problem.getProblemId();
    if (id == this.handledProblemID) {
      // FIXME search specifically: return param (which??)
      //			if (QuickFixes.hasExplicitNullnessAnnotation(compilationUnit, problem.getOffset()))
      //				return false;
      return true;
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int computeNumberOfFixes(CompilationUnit compilationUnit) {
    int result = 0;
    IProblem[] problems = compilationUnit.getProblems();
    for (int i = 0; i < problems.length; i++) {
      int id = problems[i].getID();
      if (id == this.handledProblemID) {
        // FIXME search specifically: return param (which??)
        //				if (!QuickFixes.hasExplicitNullnessAnnotation(compilationUnit,
        // problems[i].getSourceStart()))
        result++;
      }
    }
    return result;
  }
}

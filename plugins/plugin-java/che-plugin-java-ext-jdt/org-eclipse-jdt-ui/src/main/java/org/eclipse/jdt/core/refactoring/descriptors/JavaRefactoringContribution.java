/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.core.refactoring.descriptors;

import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Partial implementation of a Java refactoring contribution.
 *
 * <p>Note: this class is not intended to be extended outside the refactoring framework.
 *
 * @since 1.1
 * @noextend This class is not intended to be subclassed by clients outside JDT
 */
public abstract class JavaRefactoringContribution extends RefactoringContribution {

  /** {@inheritDoc} */
  public final Map retrieveArgumentMap(final RefactoringDescriptor descriptor) {
    Assert.isNotNull(descriptor);
    if (descriptor instanceof JavaRefactoringDescriptor)
      return ((JavaRefactoringDescriptor) descriptor).getArguments();
    return super.retrieveArgumentMap(descriptor);
  }

  /**
   * Creates the a new refactoring instance.
   *
   * @param descriptor the refactoring descriptor
   * @param status the status used for the resulting status
   * @return the refactoring, or <code>null</code>
   * @throws CoreException if an error occurs while creating the refactoring
   * @since 1.2
   */
  public abstract Refactoring createRefactoring(
      JavaRefactoringDescriptor descriptor, RefactoringStatus status) throws CoreException;
}

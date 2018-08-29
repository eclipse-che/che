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
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * Refactoring descriptor for the extract local variable refactoring.
 *
 * <p>An instance of this refactoring descriptor may be obtained by calling {@link
 * RefactoringContribution#createDescriptor()} on a refactoring contribution requested by invoking
 * {@link RefactoringCore#getRefactoringContribution(String)} with the appropriate refactoring id.
 *
 * <p>Note: this class is not intended to be instantiated by clients.
 *
 * @since 1.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ExtractLocalDescriptor extends JavaRefactoringDescriptor {

  /** Creates a new refactoring descriptor. */
  public ExtractLocalDescriptor() {
    super(IJavaRefactorings.EXTRACT_LOCAL_VARIABLE);
  }

  /**
   * Creates a new refactoring descriptor.
   *
   * @param project the non-empty name of the project associated with this refactoring, or <code>
   *     null</code> for a workspace refactoring
   * @param description a non-empty human-readable description of the particular refactoring
   *     instance
   * @param comment the human-readable comment of the particular refactoring instance, or <code>null
   *     </code> for no comment
   * @param arguments a map of arguments that will be persisted and describes all settings for this
   *     refactoring
   * @param flags the flags of the refactoring descriptor
   * @since 1.2
   */
  public ExtractLocalDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    super(
        IJavaRefactorings.EXTRACT_LOCAL_VARIABLE, project, description, comment, arguments, flags);
  }
}

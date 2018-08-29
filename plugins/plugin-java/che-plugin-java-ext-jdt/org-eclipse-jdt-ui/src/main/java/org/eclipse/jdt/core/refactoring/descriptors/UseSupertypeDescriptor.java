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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.internal.core.refactoring.descriptors.DescriptorMessages;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Refactoring descriptor for the use supertype refactoring.
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
public final class UseSupertypeDescriptor extends JavaRefactoringDescriptor {

  /** The instanceof attribute */
  private static final String ATTRIBUTE_INSTANCEOF = "instanceof"; // $NON-NLS-1$

  /** The instanceof attribute */
  private boolean fInstanceof = false;

  /** The subtype attribute */
  private IType fSubType = null;

  /** The supertype attribute */
  private IType fSupertype = null;

  /** Creates a new refactoring descriptor. */
  public UseSupertypeDescriptor() {
    super(IJavaRefactorings.USE_SUPER_TYPE);
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
   * @throws IllegalArgumentException if the argument map contains invalid keys/values
   * @since 1.2
   */
  public UseSupertypeDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    super(IJavaRefactorings.USE_SUPER_TYPE, project, description, comment, arguments, flags);
    fInstanceof =
        JavaRefactoringDescriptorUtil.getBoolean(arguments, ATTRIBUTE_INSTANCEOF, fInstanceof);
    fSubType =
        (IType) JavaRefactoringDescriptorUtil.getJavaElement(arguments, ATTRIBUTE_INPUT, project);
    fSupertype =
        (IType)
            JavaRefactoringDescriptorUtil.getJavaElement(
                arguments,
                JavaRefactoringDescriptorUtil.getAttributeName(ATTRIBUTE_ELEMENT, 1),
                project);
  }

  /** {@inheritDoc} */
  protected void populateArgumentMap() {
    super.populateArgumentMap();
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_INSTANCEOF, fInstanceof);
    JavaRefactoringDescriptorUtil.setJavaElement(
        fArguments, ATTRIBUTE_INPUT, getProject(), fSubType);
    JavaRefactoringDescriptorUtil.setJavaElement(
        fArguments,
        JavaRefactoringDescriptorUtil.getAttributeName(ATTRIBUTE_ELEMENT, 1),
        getProject(),
        fSupertype);
  }

  /**
   * Determines whether 'instanceof' statements are considered as candidates to replace the subtype
   * occurrence by one of its supertypes.
   *
   * <p>The default is to not replace the subtype occurrence.
   *
   * @param replace <code>true</code> to replace subtype occurrences in 'instanceof' statements,
   *     <code>false</code> otherwise
   */
  public void setReplaceInstanceof(final boolean replace) {
    fInstanceof = replace;
  }

  /**
   * Sets the subtype of the refactoring.
   *
   * <p>Occurrences of the subtype are replaced by the supertype set by {@link #setSupertype(IType)}
   * where possible.
   *
   * @param type the subtype to set
   */
  public void setSubtype(final IType type) {
    Assert.isNotNull(type);
    fSubType = type;
  }

  /**
   * Sets the supertype of the refactoring.
   *
   * <p>Occurrences of the subtype set by {@link #setSubtype(IType)} are replaced by the supertype
   * where possible.
   *
   * @param type the supertype to set
   */
  public void setSupertype(final IType type) {
    Assert.isNotNull(type);
    fSupertype = type;
  }

  /** {@inheritDoc} */
  public RefactoringStatus validateDescriptor() {
    RefactoringStatus status = super.validateDescriptor();
    if (fSubType == null)
      status.merge(
          RefactoringStatus.createFatalErrorStatus(
              DescriptorMessages.UseSupertypeDescriptor_no_subtype));
    if (fSupertype == null)
      status.merge(
          RefactoringStatus.createFatalErrorStatus(
              DescriptorMessages.UseSupertypeDescriptor_no_supertype));
    return status;
  }
}

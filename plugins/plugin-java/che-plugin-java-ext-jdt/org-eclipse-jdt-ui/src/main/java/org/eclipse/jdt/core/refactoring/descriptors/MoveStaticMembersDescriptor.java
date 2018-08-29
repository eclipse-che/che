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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.internal.core.refactoring.descriptors.DescriptorMessages;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Refactoring descriptor for the move static members refactoring.
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
public final class MoveStaticMembersDescriptor extends JavaRefactoringDescriptor {

  /** The delegate attribute */
  private static final String ATTRIBUTE_DELEGATE = "delegate"; // $NON-NLS-1$

  /** The deprecate attribute */
  private static final String ATTRIBUTE_DEPRECATE = "deprecate"; // $NON-NLS-1$

  /** The delegate attribute */
  private boolean fDelegate = false;

  /** The deprecate attribute */
  private boolean fDeprecate = false;

  /** The members attribute */
  private IMember[] fMembers;

  /** The type attribute */
  private IType fType = null;

  /** Creates a new refactoring descriptor. */
  public MoveStaticMembersDescriptor() {
    super(IJavaRefactorings.MOVE_STATIC_MEMBERS);
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
  public MoveStaticMembersDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    super(IJavaRefactorings.MOVE_STATIC_MEMBERS, project, description, comment, arguments, flags);
    fType =
        (IType) JavaRefactoringDescriptorUtil.getJavaElement(fArguments, ATTRIBUTE_INPUT, project);
    fDelegate = JavaRefactoringDescriptorUtil.getBoolean(fArguments, ATTRIBUTE_DELEGATE, fDelegate);
    fDeprecate =
        JavaRefactoringDescriptorUtil.getBoolean(fArguments, ATTRIBUTE_DEPRECATE, fDeprecate);
    fMembers =
        (IMember[])
            JavaRefactoringDescriptorUtil.getJavaElementArray(
                fArguments, null, ATTRIBUTE_ELEMENT, 1, project, IMember.class);
  }

  /** {@inheritDoc} */
  protected void populateArgumentMap() {
    super.populateArgumentMap();
    String project = getProject();
    JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_INPUT, project, fType);
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_DELEGATE, fDelegate);
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_DEPRECATE, fDeprecate);
    JavaRefactoringDescriptorUtil.setJavaElementArray(
        fArguments, null, ATTRIBUTE_ELEMENT, project, fMembers, 1);
  }

  /**
   * Determines whether the delegate for a member should be declared as deprecated.
   *
   * @param deprecate <code>true</code> to deprecate the delegate, <code>false</code> otherwise
   */
  public void setDeprecateDelegate(final boolean deprecate) {
    fDeprecate = deprecate;
  }

  /**
   * Sets the destination type of the move operation.
   *
   * @param type the destination type
   */
  public void setDestinationType(final IType type) {
    Assert.isNotNull(type);
    fType = type;
  }

  /**
   * Determines whether the the original members should be kept as delegates to the moved ones.
   *
   * @param delegate <code>true</code> to keep the originals, <code>false</code> otherwise
   */
  public void setKeepOriginal(final boolean delegate) {
    fDelegate = delegate;
  }

  /**
   * Sets the static members to move.
   *
   * @param members the members to move
   */
  public void setMembers(final IMember[] members) {
    Assert.isNotNull(members);
    fMembers = members;
  }

  /** {@inheritDoc} */
  public RefactoringStatus validateDescriptor() {
    final RefactoringStatus status = super.validateDescriptor();
    if (fType == null)
      status.merge(
          RefactoringStatus.createFatalErrorStatus(
              DescriptorMessages.MoveStaticMembersDescriptor_no_type));
    if (fMembers == null)
      status.merge(
          RefactoringStatus.createFatalErrorStatus(
              DescriptorMessages.MoveStaticMembersDescriptor_no_members));
    else {
      for (int index = 0; index < fMembers.length; index++) {
        if (fMembers[index] == null) {
          status.merge(
              RefactoringStatus.createFatalErrorStatus(
                  DescriptorMessages.MoveStaticMembersDescriptor_invalid_members));
          break;
        }
      }
    }
    return status;
  }
}

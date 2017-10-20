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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.refactoring.descriptors.DescriptorMessages;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Partial implementation of a java refactoring descriptor.
 *
 * <p>This class provides features common to all Java refactorings.
 *
 * <p>Note: this class is not intended to be extended outside the refactoring framework.
 *
 * @since 1.1
 * @noextend This class is not intended to be subclassed by clients outside JDT.
 */
public abstract class JavaRefactoringDescriptor extends RefactoringDescriptor {

  /**
   * Predefined argument called <code>element&lt;Number&gt;</code>.
   *
   * <p>This argument should be used to describe the elements being refactored. The value of this
   * argument does not necessarily have to uniquely identify the elements. However, it must be
   * possible to uniquely identify the elements using the value of this argument in conjunction with
   * the values of the other user-defined attributes.
   *
   * <p>The element arguments are simply distinguished by appending a number to the argument name,
   * e.g. element1. The indices of this argument are one-based.
   */
  protected static final String ATTRIBUTE_ELEMENT = "element"; // $NON-NLS-1$

  /**
   * Predefined argument called <code>input</code>.
   *
   * <p>This argument should be used to describe the element being refactored. The value of this
   * argument does not necessarily have to uniquely identify the input element. However, it must be
   * possible to uniquely identify the input element using the value of this argument in conjunction
   * with the values of the other user-defined attributes.
   */
  protected static final String ATTRIBUTE_INPUT = "input"; // $NON-NLS-1$

  /**
   * Predefined argument called <code>name</code>.
   *
   * <p>This argument should be used to name the element being refactored. The value of this
   * argument may be shown in the user interface.
   */
  protected static final String ATTRIBUTE_NAME = "name"; // $NON-NLS-1$

  /**
   * Predefined argument called <code>references</code>.
   *
   * <p>This argument should be used to describe whether references to the elements being refactored
   * should be updated as well. The value of this argument is either <code>"true"</code> or <code>
   * "false"</code>.
   */
  protected static final String ATTRIBUTE_REFERENCES = "references"; // $NON-NLS-1$

  /**
   * Predefined argument called <code>selection</code>.
   *
   * <p>This argument should be used to describe user input selections within a text file. The value
   * of this argument has the format "offset length".
   */
  protected static final String ATTRIBUTE_SELECTION = "selection"; // $NON-NLS-1$

  /** The version attribute */
  protected static final String ATTRIBUTE_VERSION = "version"; // $NON-NLS-1$

  /**
   * Constant describing the jar migration flag (value: <code>65536</code>).
   *
   * <p>Clients should set this flag to indicate that the refactoring can be stored to a JAR file in
   * order to be accessible to the Migrate JAR File refactoring, regardless whether there is a
   * source attachment to the JAR file or not. If this flag is set, <code>JAR_REFACTORING</code>
   * should be set as well.
   *
   * @see #JAR_REFACTORING
   */
  public static final int JAR_MIGRATION = 1 << 16;

  /**
   * Constant describing the jar refactoring flag (value: <code>524288</code>).
   *
   * <p>Clients should set this flag to indicate that the refactoring in principle can be performed
   * on binary elements originating from a JAR file. Refactorings which are able to run on binary
   * elements, but require a correctly configured source attachment to work must set the <code>
   * JAR_SOURCE_ATTACHMENT</code> flag as well.
   *
   * @see #JAR_SOURCE_ATTACHMENT
   */
  public static final int JAR_REFACTORING = 1 << 19;

  /**
   * Constant describing the jar source attachment flag (value: <code>262144</code>).
   *
   * <p>Clients should set this flag to indicate that the refactoring can be performed on binary
   * elements originating from a JAR file if and only if it has a correctly configured source
   * attachment.
   *
   * @see #JAR_REFACTORING
   */
  public static final int JAR_SOURCE_ATTACHMENT = 1 << 18;

  /** The version value <code>1.0</code> */
  protected static final String VALUE_VERSION_1_0 = "1.0"; // $NON-NLS-1$

  /**
   * Converts the specified element to an input handle.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param element the element
   * @return a corresponding input handle
   */
  protected static String elementToHandle(final String project, final IJavaElement element) {
    return JavaRefactoringDescriptorUtil.elementToHandle(project, element);
  }

  /**
   * Converts an input handle back to the corresponding java element.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @return the corresponding java element, or <code>null</code> if no such element exists
   */
  protected static IJavaElement handleToElement(final String project, final String handle) {
    return handleToElement(project, handle, true);
  }

  /**
   * Converts an input handle back to the corresponding java element.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @param check <code>true</code> to check for existence of the element, <code>false</code>
   *     otherwise
   * @return the corresponding java element, or <code>null</code> if no such element exists
   */
  protected static IJavaElement handleToElement(
      final String project, final String handle, final boolean check) {
    return handleToElement(null, project, handle, check);
  }

  /**
   * Converts an input handle back to the corresponding java element.
   *
   * @param owner the working copy owner
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @param check <code>true</code> to check for existence of the element, <code>false</code>
   *     otherwise
   * @return the corresponding java element, or <code>null</code> if no such element exists
   */
  protected static IJavaElement handleToElement(
      final WorkingCopyOwner owner,
      final String project,
      final String handle,
      final boolean check) {
    return JavaRefactoringDescriptorUtil.handleToElement(owner, project, handle, check);
  }

  /**
   * Converts an input handle with the given prefix back to the corresponding resource.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @return the corresponding resource, or <code>null</code> if no such resource exists
   */
  protected static IResource handleToResource(final String project, final String handle) {
    return JavaRefactoringDescriptorUtil.handleToResource(project, handle);
  }

  /**
   * Converts the specified resource to an input handle.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param resource the resource
   * @return the input handle
   */
  protected static String resourceToHandle(final String project, final IResource resource) {
    return JavaRefactoringDescriptorUtil.resourcePathToHandle(project, resource.getFullPath());
  }

  /** The argument map (key type: {@link String}, value type: {@link String}). */
  protected final Map fArguments;

  /**
   * Creates a new java refactoring descriptor.
   *
   * @param id the unique id of the refactoring
   */
  protected JavaRefactoringDescriptor(final String id) {
    this(
        id,
        null,
        DescriptorMessages.JavaRefactoringDescriptor_not_available,
        null,
        new HashMap(),
        RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
  }

  /**
   * Creates a new Java refactoring descriptor.
   *
   * @param id the unique id of the refactoring
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
  public JavaRefactoringDescriptor(
      final String id,
      final String project,
      final String description,
      final String comment,
      final Map arguments,
      final int flags) {
    super(id, project, description, comment, flags);
    fArguments = arguments;
    fArguments.put(ATTRIBUTE_VERSION, VALUE_VERSION_1_0);
  }

  /** {@inheritDoc} */
  public Refactoring createRefactoring(final RefactoringStatus status) throws CoreException {
    Refactoring refactoring = null;
    final String id = getID();
    final RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(id);
    if (contribution != null) {
      if (contribution instanceof JavaRefactoringContribution) {
        JavaRefactoringContribution javaContribution = (JavaRefactoringContribution) contribution;
        refactoring = javaContribution.createRefactoring(this, status);
      } else
        JavaPlugin.log(
            new Status(
                IStatus.ERROR,
                JavaPlugin.getPluginId(),
                0,
                MessageFormat.format(
                    DescriptorMessages.JavaRefactoringDescriptor_no_resulting_descriptor,
                    new Object[] {id}),
                null));
    }
    return refactoring;
  }

  /**
   * Returns the argument map of this refactoring descriptor.
   *
   * <p>The returned map is a copy of the argument map. Modifying the result does not change the
   * refactoring descriptor itself.
   *
   * <p>Note: This API must not be extended or reimplemented and should not be called from outside
   * the refactoring framework.
   *
   * @return the argument map (key type: {@link String}, value type: {@link String})
   */
  protected Map getArguments() {
    populateArgumentMap();
    return new HashMap(fArguments);
  }

  /**
   * Populates the refactoring descriptor argument map based on the specified arguments. Subclasses
   * should extend and add their arguments to {@link #fArguments}.
   */
  protected void populateArgumentMap() {
    RefactoringStatus status = validateDescriptor();
    if (status.hasFatalError())
      throw new RuntimeException(
          "Validation returns a fatal error status",
          new CoreException(status.getEntryWithHighestSeverity().toStatus())); // $NON-NLS-1$
  }

  /**
   * Sets the details comment of this refactoring.
   *
   * <p>This information is used in the user interface to show additional details about the
   * performed refactoring. The default is to use no details comment.
   *
   * @param comment the details comment to set, or <code>null</code> to set no details comment
   * @see #getComment()
   */
  public void setComment(final String comment) {
    super.setComment(comment);
  }

  /**
   * Sets the description of this refactoring.
   *
   * <p>This information is used to label a refactoring in the user interface. The default is an
   * unspecified, but legal description.
   *
   * @param description the non-empty description of the refactoring to set
   * @see #getDescription()
   */
  public void setDescription(final String description) {
    super.setDescription(description);
  }

  /**
   * Sets the flags of this refactoring.
   *
   * <p>The default is <code>
   * RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE</code>, unless
   * overridden by a concrete subclass. Clients may use refactoring flags to indicate special
   * capabilities of Java refactorings.
   *
   * @param flags the flags to set, or <code>RefactoringDescriptor.NONE</code> to clear the flags
   * @see #getFlags()
   * @see RefactoringDescriptor#NONE
   * @see RefactoringDescriptor#STRUCTURAL_CHANGE
   * @see RefactoringDescriptor#BREAKING_CHANGE
   * @see RefactoringDescriptor#MULTI_CHANGE
   * @see #JAR_MIGRATION
   * @see #JAR_REFACTORING
   * @see #JAR_SOURCE_ATTACHMENT
   */
  public void setFlags(final int flags) {
    super.setFlags(flags);
  }

  /**
   * Sets the project name of this refactoring.
   *
   * <p>The default is to associate the refactoring with the workspace. Subclasses should call this
   * method with the project name associated with the refactoring's input elements, if available.
   *
   * @param project the non-empty project name to set, or <code>null</code> for the workspace
   * @see #getProject()
   */
  public void setProject(final String project) {
    super.setProject(project);
  }

  /**
   * Validates the refactoring descriptor with respect to the constraints imposed by the represented
   * refactoring.
   *
   * <p>Clients must call this method to verify that all arguments have been correctly set and that
   * they satisfy the constraints imposed by specific refactorings. Returning a refactoring status
   * of severity {@link RefactoringStatus#FATAL} indicates that the refactoring descriptor cannot be
   * used to create a refactoring instance.
   *
   * @return a refactoring status describing the outcome of the validation
   */
  public RefactoringStatus validateDescriptor() {
    RefactoringStatus status = new RefactoringStatus();
    String description = getDescription();
    if (description == null || "".equals(description)) // $NON-NLS-1$
    status.merge(
          RefactoringStatus.createFatalErrorStatus(
              DescriptorMessages.JavaRefactoringDescriptor_no_description));
    return status;
  }
}

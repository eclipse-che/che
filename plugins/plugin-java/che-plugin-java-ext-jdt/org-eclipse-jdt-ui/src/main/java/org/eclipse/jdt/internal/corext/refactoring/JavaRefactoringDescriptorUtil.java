/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class JavaRefactoringDescriptorUtil {
  /* TODO: share implementation with
   * org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil
   */

  private JavaRefactoringDescriptorUtil() {}

  /**
   * Predefined argument called <code>element&lt;Number&gt;</code>.
   *
   * <p>This argument should be used to describe the elements being refactored. The value of this
   * argument does not necessarily have to uniquely identify the elements. However, it must be
   * possible to uniquely identify the elements using the value of this argument in conjunction with
   * the values of the other user-defined attributes.
   *
   * <p>The element arguments are simply distinguished by appending a number to the argument name,
   * e.g. element1. The indices of this argument are non zero-based.
   */
  public static final String ATTRIBUTE_ELEMENT = "element"; // $NON-NLS-1$

  /**
   * Predefined argument called <code>input</code>.
   *
   * <p>This argument should be used to describe the element being refactored. The value of this
   * argument does not necessarily have to uniquely identify the input element. However, it must be
   * possible to uniquely identify the input element using the value of this argument in conjunction
   * with the values of the other user-defined attributes.
   */
  public static final String ATTRIBUTE_INPUT = "input"; // $NON-NLS-1$

  /**
   * Predefined argument called <code>name</code>.
   *
   * <p>This argument should be used to name the element being refactored. The value of this
   * argument may be shown in the user interface.
   */
  public static final String ATTRIBUTE_NAME = "name"; // $NON-NLS-1$

  /**
   * Predefined argument called <code>references</code>.
   *
   * <p>This argument should be used to describe whether references to the elements being refactored
   * should be updated as well.
   */
  public static final String ATTRIBUTE_REFERENCES = "references"; // $NON-NLS-1$

  /**
   * Predefined argument called <code>selection</code>.
   *
   * <p>This argument should be used to describe user input selections within a text file. The value
   * of this argument has the format "offset length".
   */
  public static final String ATTRIBUTE_SELECTION = "selection"; // $NON-NLS-1$

  /**
   * Converts the specified element to an input handle.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param element the element
   * @return a corresponding input handle Note: if the given project is not the element's project,
   *     then the full handle is returned
   */
  public static String elementToHandle(final String project, final IJavaElement element) {
    final String handle = element.getHandleIdentifier();
    if (project != null && !(element instanceof IJavaProject)) {
      IJavaProject javaProject = element.getJavaProject();
      if (project.equals(javaProject.getElementName())) {
        final String id = javaProject.getHandleIdentifier();
        return handle.substring(id.length());
      }
    }
    return handle;
  }

  /**
   * Converts an input handle back to the corresponding java element.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @return the corresponding java element, or <code>null</code> if no such element exists
   */
  public static IJavaElement handleToElement(final String project, final String handle) {
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
  public static IJavaElement handleToElement(
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
  public static IJavaElement handleToElement(
      final WorkingCopyOwner owner,
      final String project,
      final String handle,
      final boolean check) {
    IJavaElement element = null;
    if (owner != null) element = JavaCore.create(handle, owner);
    else element = JavaCore.create(handle);
    if (element == null && project != null) {
      final IJavaProject javaProject =
          JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProject(project);
      final String identifier = javaProject.getHandleIdentifier();
      if (owner != null) element = JavaCore.create(identifier + handle, owner);
      else element = JavaCore.create(identifier + handle);
    }
    if (check && element instanceof IMethod) {
      /*
       * Resolve the method based on simple names of parameter types
       * (to accommodate for different qualifications when refactoring is e.g.
       * recorded in source but applied on binary method):
       */
      final IMethod method = (IMethod) element;
      final IMethod[] methods = method.getDeclaringType().findMethods(method);
      if (methods != null && methods.length > 0) element = methods[0];
    }
    if (element != null && (!check || element.exists())) return element;
    return null;
  }

  /**
   * Converts an input handle with the given prefix back to the corresponding resource.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @return the corresponding resource, or <code>null</code> if no such resource exists. Note: if
   *     the given handle is absolute, the project is not used to resolve.
   */
  public static IResource handleToResource(final String project, final String handle) {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    if ("".equals(handle)) // $NON-NLS-1$
    return null;
    final IPath path = Path.fromPortableString(handle);
    if (path == null) return null;
    if (project != null && !"".equals(project) && !path.isAbsolute()) // $NON-NLS-1$
    return root.getProject(project).findMember(path);
    return root.findMember(path);
  }

  /**
   * Converts the specified resource to an input handle.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param resource the resource
   * @return the input handle. Note: if the given project is not the resource's project, then the
   *     full handle is returned.
   */
  public static String resourceToHandle(final String project, final IResource resource) {
    if (project != null
        && !"".equals(project)
        && project.equals(resource.getProject().getName())) // $NON-NLS-1$
    return resource.getProjectRelativePath().toPortableString();
    return resource.getFullPath().toPortableString();
  }

  /**
   * Creates a fatal error status telling that the input element does not exist.
   *
   * @param element the input element, or <code>null</code>
   * @param name the name of the refactoring
   * @param id the id of the refactoring
   * @return the refactoring status
   */
  public static RefactoringStatus createInputFatalStatus(
      final Object element, final String name, final String id) {
    Assert.isNotNull(name);
    Assert.isNotNull(id);
    if (element != null)
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_input_not_exists,
              new String[] {
                JavaElementLabels.getTextLabel(element, JavaElementLabels.ALL_FULLY_QUALIFIED),
                name,
                id
              }));
    else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_inputs_do_not_exist,
              new String[] {name, id}));
  }

  /**
   * Creates a warning status telling that the input element does not exist.
   *
   * @param element the input element, or <code>null</code>
   * @param name the name of the refactoring
   * @param id the id of the refactoring
   * @return the refactoring status
   */
  public static RefactoringStatus createInputWarningStatus(
      final Object element, final String name, final String id) {
    Assert.isNotNull(name);
    Assert.isNotNull(id);
    if (element != null)
      return RefactoringStatus.createWarningStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_input_not_exists,
              new String[] {
                JavaElementLabels.getTextLabel(element, JavaElementLabels.ALL_FULLY_QUALIFIED),
                name,
                id
              }));
    else
      return RefactoringStatus.createWarningStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_inputs_do_not_exist,
              new String[] {name, id}));
  }
}

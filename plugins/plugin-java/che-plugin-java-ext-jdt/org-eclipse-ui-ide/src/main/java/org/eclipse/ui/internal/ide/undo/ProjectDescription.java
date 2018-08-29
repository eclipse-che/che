/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * ****************************************************************************
 */
package org.eclipse.ui.internal.ide.undo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * ProjectDescription is a lightweight description that describes a project to be created.
 *
 * <p>This class is not intended to be instantiated or used by clients.
 *
 * @since 3.3
 */
public class ProjectDescription extends ContainerDescription {

  private IProjectDescription projectDescription;
  private boolean openOnCreate = true;

  /**
   * Create a project description from a specified project.
   *
   * @param project The project to be described. The project must exist.
   */
  public ProjectDescription(IProject project) {
    super(project);
    Assert.isLegal(project.exists());
    if (project.isOpen()) {
      try {
        this.projectDescription = project.getDescription();
      } catch (CoreException e) {
        // Eat this exception because it only occurs when the project
        // is not accessible and we have already checked this. We
        // don't want to propagate the CoreException into the
        // constructor
        // API.
      }
    } else {
      openOnCreate = false;
    }
  }

  /**
   * Create a project description from a specified IProjectDescription. Used when the project does
   * not yet exist.
   *
   * @param projectDescription the project description for the future project
   */
  public ProjectDescription(IProjectDescription projectDescription) {
    super();
    this.projectDescription = projectDescription;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.internal.ide.undo.ContainerDescription#createResourceHandle()
   */
  public IResource createResourceHandle() {
    return ResourcesPlugin.getWorkspace().getRoot().getProject(getName());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.internal.ide.undo.ResourceDescription#createExistentResourceFromHandle(org.eclipse.core.resources.IResource,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  public void createExistentResourceFromHandle(IResource resource, IProgressMonitor monitor)
      throws CoreException {
    Assert.isLegal(resource instanceof IProject);
    if (resource.exists()) {
      return;
    }
    IProject projectHandle = (IProject) resource;
    monitor.beginTask("", 200); // $NON-NLS-1$
    monitor.setTaskName(UndoMessages.FolderDescription_NewFolderProgress);
    if (projectDescription == null) {
      projectHandle.create(new SubProgressMonitor(monitor, 100));
    } else {
      projectHandle.create(projectDescription, new SubProgressMonitor(monitor, 100));
    }

    if (monitor.isCanceled()) {
      throw new OperationCanceledException();
    }
    if (openOnCreate) {
      projectHandle.open(IResource.NONE, new SubProgressMonitor(monitor, 100));
    }
    monitor.done();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.internal.ide.undo.ContainerDescription#getName()
   */
  public String getName() {
    if (projectDescription != null) {
      return projectDescription.getName();
    }
    return super.getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.internal.ide.undo.ResourceDescription#verifyExistence(boolean)
   */
  public boolean verifyExistence(boolean checkMembers) {
    // We can only check members if the project is open.
    IProject projectHandle = (IProject) createResourceHandle();
    if (projectHandle.isAccessible()) {
      return super.verifyExistence(checkMembers);
    }
    return super.verifyExistence(false);
  }
}

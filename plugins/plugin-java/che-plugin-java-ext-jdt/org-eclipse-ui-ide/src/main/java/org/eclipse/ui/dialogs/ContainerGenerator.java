/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ui.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * For creating folder resources that currently do not exist, along a given workspace path.
 *
 * <p>This class may be instantiated; it is not intended to be subclassed.
 *
 * <p>Example usage:
 *
 * <pre>
 * ContainerGenerator gen = new ContainerGenerator(new Path("/A/B"));
 * IContainer res = null;
 * try {
 *   res = gen.getContainer(monitor); // creates project A and folder B if required
 * } catch (CoreException e) {
 *   // handle failure
 * } catch (OperationCanceledException e) {
 *   // handle cancelation
 * }
 * </pre>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ContainerGenerator {
  private IPath containerFullPath;

  private IContainer container;

  /**
   * Creates a generator for the container resource (folder or project) at the given workspace path.
   * Assumes the path has already been validated.
   *
   * <p>Call <code>getContainer</code> to create any missing resources along the path.
   *
   * @param containerPath the workspace path of the container
   */
  public ContainerGenerator(IPath containerPath) {
    super();
    this.containerFullPath = containerPath;
  }

  /**
   * Creates a folder resource for the given folder handle.
   *
   * @param folderHandle the handle to create a folder resource
   * @param monitor the progress monitor to show visual progress
   * @return the folder handle (<code>folderHandle</code>)
   * @exception CoreException if the operation fails
   * @exception OperationCanceledException if the operation is canceled
   */
  private IFolder createFolder(IFolder folderHandle, IProgressMonitor monitor)
      throws CoreException {
    folderHandle.create(false, true, monitor);

    if (monitor.isCanceled()) {
      throw new OperationCanceledException();
    }

    return folderHandle;
  }

  /**
   * Creates a folder resource handle for the folder with the given name. This method does not
   * create the folder resource; this is the responsibility of <code>createFolder</code>.
   *
   * @param container the resource container
   * @param folderName the name of the folder
   * @return the new folder resource handle
   */
  private IFolder createFolderHandle(IContainer container, String folderName) {
    return container.getFolder(new Path(folderName));
  }

  /**
   * Creates a project resource for the given project handle.
   *
   * @param projectHandle the handle to create a project resource
   * @param monitor the progress monitor to show visual progress
   * @return the project handle (<code>projectHandle</code>)
   * @exception CoreException if the operation fails
   * @exception OperationCanceledException if the operation is canceled
   */
  private IProject createProject(IProject projectHandle, IProgressMonitor monitor)
      throws CoreException {
    try {
      monitor.beginTask("", 2000); // $NON-NLS-1$

      projectHandle.create(new SubProgressMonitor(monitor, 1000));
      if (monitor.isCanceled()) {
        throw new OperationCanceledException();
      }

      projectHandle.open(new SubProgressMonitor(monitor, 1000));
      if (monitor.isCanceled()) {
        throw new OperationCanceledException();
      }
    } finally {
      monitor.done();
    }

    return projectHandle;
  }

  /**
   * Creates a project resource handle for the project with the given name. This method does not
   * create the project resource; this is the responsibility of <code>createProject</code>.
   *
   * @param root the workspace root resource
   * @param projectName the name of the project
   * @return the new project resource handle
   */
  private IProject createProjectHandle(IWorkspaceRoot root, String projectName) {
    return root.getProject(projectName);
  }

  /**
   * Ensures that this generator's container resource exists. Creates any missing resource
   * containers along the path; does nothing if the container resource already exists.
   *
   * <p>Note: This method should be called within a workspace modify operation since it may create
   * resources.
   *
   * @param monitor a progress monitor
   * @return the container resource
   * @exception CoreException if the operation fails
   * @exception OperationCanceledException if the operation is canceled
   */
  public IContainer generateContainer(IProgressMonitor monitor) throws CoreException {
    ResourcesPlugin.getWorkspace()
        .run(
            new IWorkspaceRunnable() {
              public void run(IProgressMonitor monitor) throws CoreException {
                monitor.beginTask(
                    "IDEWorkbenchMessages.ContainerGenerator_progressMessage",
                    1000 * containerFullPath.segmentCount());
                if (container != null) {
                  return;
                }

                // Does the container exist already?
                IWorkspaceRoot root = getWorkspaceRoot();
                container = (IContainer) root.findMember(containerFullPath);
                if (container != null) {
                  return;
                }

                // Create the container for the given path
                container = root;
                for (int i = 0; i < containerFullPath.segmentCount(); i++) {
                  String currentSegment = containerFullPath.segment(i);
                  IResource resource = container.findMember(currentSegment);
                  if (resource != null) {
                    if (resource.getType() == IResource.FILE) {
                      String msg =
                          NLS.bind(
                              "IDEWorkbenchMessages.ContainerGenerator_pathOccupied",
                              resource.getFullPath().makeRelative());
                      throw new CoreException(
                          new Status(
                              IStatus.ERROR, "IDEWorkbenchPlugin.IDE_WORKBENCH", 1, msg, null));
                    }
                    container = (IContainer) resource;
                    monitor.worked(1000);
                  } else {
                    if (i == 0) {
                      IProject projectHandle = createProjectHandle(root, currentSegment);
                      container =
                          createProject(projectHandle, new SubProgressMonitor(monitor, 1000));
                    } else {
                      IFolder folderHandle = createFolderHandle(container, currentSegment);
                      container = createFolder(folderHandle, new SubProgressMonitor(monitor, 1000));
                    }
                  }
                }
              }
            },
            null,
            IResource.NONE,
            monitor);
    return container;
  }

  /**
   * Returns the workspace root resource handle.
   *
   * @return the workspace root resource handle
   */
  private IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
}

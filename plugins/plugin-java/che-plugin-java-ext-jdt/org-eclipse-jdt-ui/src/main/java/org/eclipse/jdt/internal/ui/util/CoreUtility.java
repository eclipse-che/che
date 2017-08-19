package org.eclipse.jdt.internal.ui.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/** @author Evgen Vidolob */
public class CoreUtility {

  /**
   * Creates a folder and all parent folders if not existing. Project must exist. <code>
   *  org.eclipse.ui.dialogs.ContainerGenerator</code> is too heavy (creates a runnable)
   *
   * @param folder the folder to create
   * @param force a flag controlling how to deal with resources that are not in sync with the local
   *     file system
   * @param local a flag controlling whether or not the folder will be local after the creation
   * @param monitor the progress monitor
   * @throws CoreException thrown if the creation failed
   */
  public static void createFolder(
      IFolder folder, boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
    if (!folder.exists()) {
      IContainer parent = folder.getParent();
      if (parent instanceof IFolder) {
        createFolder((IFolder) parent, force, local, null);
      }
      folder.create(force, local, monitor);
    }
  }
}

/**
 * ***************************************************************************** Copyright (c) 2007
 * IBM Corporation and others. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources.team;

import org.eclipse.core.resources.IWorkspace;

/**
 * A context that is used in conjunction with the {@link FileModificationValidator} to indicate that
 * UI-based validation is desired.
 *
 * <p>This class is not intended to be instantiated or subclassed by clients.
 *
 * @see FileModificationValidator
 * @since 3.3
 */
public class FileModificationValidationContext {

  /**
   * Constant that can be passed to {@link
   * IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], Object)} to indicate that the
   * caller does not have access to a UI context but would still like to have UI-based validation if
   * possible.
   */
  public static final FileModificationValidationContext VALIDATE_PROMPT =
      new FileModificationValidationContext(null);

  private final Object shell;

  /**
   * Create a context with the given shell.
   *
   * @param shell the shell
   */
  FileModificationValidationContext(Object shell) {
    this.shell = shell;
  }

  /**
   * Return the <code>org.eclipse.swt.widgets.Shell</code> that is to be used to parent any dialogs
   * with the user, or <code>null</code> if there is no UI context available (declared as an <code>
   * Object</code> to avoid any direct references on the SWT component). If there is no shell, the
   * {@link FileModificationValidator} may still perform UI-based validation if they can obtain a
   * Shell from another source.
   *
   * @return the <code>org.eclipse.swt.widgets.Shell</code> that is to be used to parent any dialogs
   *     with the user, or <code>null</code>
   */
  public Object getShell() {
    return shell;
  }
}

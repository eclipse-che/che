/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.ide.runtime;

/**
 * A checked exception representing a failure.
 *
 * <p>Core exceptions contain a status object describing the cause of the exception.
 *
 * <p>This class can be used without OSGi running.
 *
 * @see IStatus
 */
public class CoreException extends Exception {

  /** All serializable objects should have a stable serialVersionUID */
  private static final long serialVersionUID = 1L;

  /** Status object. */
  private final IStatus status;

  /**
   * Creates a new exception with the given status object. The message of the given status is used
   * as the exception message.
   *
   * @param status the status object to be associated with this exception
   */
  public CoreException(IStatus status) {
    super(status.getMessage());
    this.status = status;
  }

  /**
   * Returns the cause of this exception, or <code>null</code> if none.
   *
   * @return the cause for this exception
   */
  public Throwable getCause() {
    return status.getException();
  }

  /**
   * Returns the status object for this exception.
   *
   * <p><b>IMPORTANT:</b><br>
   * The result must NOT be used for logging, error reporting, or as a method return value, since
   * that code pattern hides the original stack trace. Instead, create a new {@link Status} with
   * your plug-in ID and this <code>CoreException</code>, and use that new status for error
   * reporting or as a method return value. For example, instead of:
   *
   * <p>
   *
   * <pre>
   * yourPlugin.getLog().log(exception.getStatus());
   * </pre>
   *
   * <p>Use:
   *
   * <p>
   *
   * <pre>
   * IStatus result = new Status(exception.getStatus().getSeverity(), pluginId, message, exception);
   * yourPlugin.getLog().log(result);
   * </pre>
   *
   * <p>
   *
   * @return a status object
   */
  public final IStatus getStatus() {
    return status;
  }
}

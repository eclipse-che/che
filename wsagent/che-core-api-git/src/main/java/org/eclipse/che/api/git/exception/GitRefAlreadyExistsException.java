/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git.exception;

/** @author Yossi Balan (yossi.balan@sap.com) */
public class GitRefAlreadyExistsException extends GitException {

  /**
   * Construct a new GitRefAlreadyExistsException based on message
   *
   * @param message error message
   */
  public GitRefAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Construct a new GitRefAlreadyExistsException base on cause
   *
   * @param cause cause exception
   */
  public GitRefAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Construct a new GitRefAlreadyExistsException based on message and cause
   *
   * @param message error message
   * @param cause cause exception
   */
  public GitRefAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}

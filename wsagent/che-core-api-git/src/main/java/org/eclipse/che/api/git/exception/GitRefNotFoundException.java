/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git.exception;

/** @author Yossi Balan (yossi.balan@sap.com) */
public class GitRefNotFoundException extends GitException {

  /**
   * Construct a new GitRefNotFoundException based on message
   *
   * @param message error message
   */
  public GitRefNotFoundException(String message) {
    super(message);
  }

  /**
   * Construct a new GitRefNotFoundException based on cause
   *
   * @param cause cause exception
   */
  public GitRefNotFoundException(Throwable cause) {
    super(cause);
  }

  /**
   * Construct a new GitRefNotFoundException based on message and cause
   *
   * @param message error message
   * @param cause cause exception
   */
  public GitRefNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}

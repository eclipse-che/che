/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git.exception;

import java.util.Collections;
import java.util.List;

/** @author Yossi Balan (yossi.balan@sap.com) */
public class GitConflictException extends GitException {

  private List<String> conflictingPaths = Collections.emptyList();

  /**
   * Constrcut a new GitConflictException based on message
   *
   * @param message error message
   */
  public GitConflictException(String message) {
    super(message);
  }

  /**
   * Constrcut a new GitConflictException based on message and conflict paths
   *
   * @param message error messgae
   * @param conflictingPaths conflict path of the files
   */
  public GitConflictException(String message, List<String> conflictingPaths) {
    super(message);
    this.conflictingPaths = conflictingPaths;
  }

  /**
   * Constrcut a new GitConflictException based on cause
   *
   * @param cause cause exception
   */
  public GitConflictException(Throwable cause) {
    super(cause);
  }

  /**
   * Construct a new GitConflictException based on message and cause
   *
   * @param message error message
   * @param cause cause exception
   */
  public GitConflictException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construct a new GitConflictException based on message, conflict paths and cause
   *
   * @param message cause exception
   * @param conflictingPaths conflict paths of the files
   * @param cause cause exception
   */
  public GitConflictException(String message, List<String> conflictingPaths, Throwable cause) {
    super(message, cause);
    this.conflictingPaths = conflictingPaths;
  }

  /**
   * get the conflict paths
   *
   * @return conflict paths of the files
   */
  public List<String> getConflictPaths() {
    return this.conflictingPaths;
  }
}

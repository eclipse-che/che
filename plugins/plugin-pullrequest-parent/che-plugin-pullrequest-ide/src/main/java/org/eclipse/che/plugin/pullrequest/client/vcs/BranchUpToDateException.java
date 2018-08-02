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
package org.eclipse.che.plugin.pullrequest.client.vcs;

import javax.validation.constraints.NotNull;

/**
 * Exception raised when the branch pushed is up to date.
 *
 * @author Kevin Pollet
 */
public class BranchUpToDateException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs an instance of {@link BranchUpToDateException}.
   *
   * @param branchName the branch name.
   */
  public BranchUpToDateException(@NotNull final String branchName) {
    super("Branch '" + branchName + "' is up-to-date");
  }
}

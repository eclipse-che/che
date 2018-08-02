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
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

import javax.validation.constraints.NotNull;

/**
 * Exception raised when a pull request is created with no commits.
 *
 * @author Kevin Pollet
 */
public class NoCommitsInPullRequestException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs an instance of {@link NoCommitsInPullRequestException}.
   *
   * @param headBranch the head branch name.
   * @param baseBranch the base branch name.
   */
  public NoCommitsInPullRequestException(
      @NotNull final String headBranch, @NotNull final String baseBranch) {
    super("No commits between " + baseBranch + " and " + headBranch);
  }
}

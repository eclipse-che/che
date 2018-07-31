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
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

import javax.validation.constraints.NotNull;

public class NoPullRequestException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs an instance of {@link NoPullRequestException}.
   *
   * @param branchName the branch name.
   */
  public NoPullRequestException(@NotNull final String branchName) {
    super("No Pull Request for branch " + branchName);
  }
}

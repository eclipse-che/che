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
 * Exception raised when trying to get a fork of a repository for a user and no fork being found.
 */
public class NoUserForkException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs an instance of {@link NoUserForkException}.
   *
   * @param user the user.
   */
  public NoUserForkException(@NotNull final String user) {
    super("No fork for user: " + user);
  }
}

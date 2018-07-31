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
package org.eclipse.che.api.git.params;

import org.eclipse.che.api.git.shared.PullRequest;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#pull(PullParams)}.
 *
 * @author Igor Vinokur
 */
public class PullParams {

  private String refSpec;
  private String remote;
  private String username;
  private String password;
  private int timeout;
  private boolean rebase;

  private PullParams() {}

  /**
   * Create new {@link PullParams} instance.
   *
   * @param remote remote name to pull from
   */
  public static PullParams create(String remote) {
    return new PullParams().withRemote(remote);
  }

  /** @see PullRequest#getRefSpec() */
  public String getRefSpec() {
    return refSpec;
  }

  /** @see PullRequest#withRefSpec(String) */
  public PullParams withRefSpec(String refSpec) {
    this.refSpec = refSpec;
    return this;
  }

  /** @see PullRequest#getRemote() */
  public String getRemote() {
    return remote;
  }

  /** @see PullRequest#withRemote(String) */
  public PullParams withRemote(String remote) {
    this.remote = remote;
    return this;
  }

  /** @see PullRequest#getTimeout() */
  public int getTimeout() {
    return timeout;
  }

  /** @see PullRequest#withTimeout(int) */
  public PullParams withTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  /** Returns user name for authentication. */
  public String getUsername() {
    return username;
  }

  /** Returns {@link PullParams} with specified user name for authentication. */
  public PullParams withUsername(String username) {
    this.username = username;
    return this;
  }

  /** Returns password for authentication. */
  public String getPassword() {
    return password;
  }

  /** Returns {@link PullParams} with specified password for authentication. */
  public PullParams withPassword(String password) {
    this.password = password;
    return this;
  }

  /** Returns the value of 'Pull with rebase' flag. */
  public boolean getRebase() {
    return rebase;
  }

  /**
   * Set value of 'Pull with rebase' flag.
   *
   * @param rebase {@code true} if need to perform rebase instead of merge after fetch operation.
   */
  public PullParams withRebase(boolean rebase) {
    this.rebase = rebase;
    return this;
  }
}

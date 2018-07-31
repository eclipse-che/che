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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.PushRequest;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#push(PushParams)}.
 *
 * @author Igor Vinokur
 */
public class PushParams {

  private List<String> refSpec;
  private String remote;
  private String username;
  private String password;
  private boolean force;
  private int timeout;

  private PushParams() {}

  /**
   * Create new {@link PushParams} instance.
   *
   * @param remote remote name to push in
   */
  public static PushParams create(String remote) {
    return new PushParams().withRemote(remote);
  }

  /** @see PushRequest#getRefSpec() */
  public List<String> getRefSpec() {
    return refSpec == null ? new ArrayList<>() : refSpec;
  }

  /** @see PushRequest#withRefSpec(List) */
  public PushParams withRefSpec(List<String> refSpec) {
    this.refSpec = refSpec;
    return this;
  }

  /** @see PushRequest#getRemote() */
  public String getRemote() {
    return remote;
  }

  /** @see PushRequest#withRemote(String) */
  public PushParams withRemote(String remote) {
    this.remote = remote;
    return this;
  }

  /** @see PushRequest#isForce() */
  public boolean isForce() {
    return force;
  }

  /** @see PushRequest#withForce(boolean) */
  public PushParams withForce(boolean force) {
    this.force = force;
    return this;
  }

  /** @see PushRequest#getTimeout() */
  public int getTimeout() {
    return timeout;
  }

  /** @see PushRequest#withTimeout(int) */
  public PushParams withTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  /** Returns user name for authentication */
  public String getUsername() {
    return username;
  }

  /** Returns {@link PushParams} with specified user name for authentication. */
  public PushParams withUsername(String username) {
    this.username = username;
    return this;
  }

  /** Returns password for authentication. */
  public String getPassword() {
    return password;
  }

  /** Returns {@link PushParams} with specified password for authentication. */
  public PushParams withPassword(String password) {
    this.password = password;
    return this;
  }
}

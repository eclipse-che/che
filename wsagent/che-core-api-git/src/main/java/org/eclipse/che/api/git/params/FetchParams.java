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
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.FetchRequest;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#fetch(FetchParams)}.
 *
 * @author Igor Vinokur
 */
public class FetchParams {

  private List<String> refSpec;
  private String remote;
  private String username;
  private String password;
  private int timeout;
  private boolean isRemoveDeletedRefs;

  private FetchParams() {}

  /**
   * Create new {@link FetchParams} instance.
   *
   * @param remote remote name to fetch
   */
  public static FetchParams create(String remote) {
    return new FetchParams().withRemote(remote);
  }

  /** @see FetchRequest#getRefSpec() */
  public List<String> getRefSpec() {
    return refSpec == null ? new ArrayList<>() : refSpec;
  }

  /** @see FetchRequest#withRefSpec(List) */
  public FetchParams withRefSpec(List<String> refSpec) {
    this.refSpec = refSpec;
    return this;
  }

  /** @see FetchRequest#getRemote() */
  public String getRemote() {
    return remote;
  }

  /** @see FetchRequest#withRemote(String) */
  public FetchParams withRemote(String remote) {
    this.remote = remote;
    return this;
  }

  /** @see FetchRequest#isRemoveDeletedRefs() */
  public boolean isRemoveDeletedRefs() {
    return isRemoveDeletedRefs;
  }

  /** @see FetchRequest#withRemoveDeletedRefs(boolean) */
  public FetchParams withRemoveDeletedRefs(boolean removeDeletedRefs) {
    isRemoveDeletedRefs = removeDeletedRefs;
    return this;
  }

  /** @see FetchRequest#getTimeout() */
  public int getTimeout() {
    return timeout;
  }

  /** @see FetchRequest#withTimeout(int) */
  public FetchParams withTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  /** Returns user name for authentication. */
  public String getUsername() {
    return username;
  }

  /** Returns {@link FetchParams} with specified user name for authentication. */
  public FetchParams withUsername(String username) {
    this.username = username;
    return this;
  }

  /** Returns password for authentication. */
  public String getPassword() {
    return password;
  }

  /** Returns {@link FetchParams} with specified password for authentication. */
  public FetchParams withPassword(String password) {
    this.password = password;
    return this;
  }
}

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
import org.eclipse.che.api.git.shared.RemoteUpdateRequest;

/**
 * Arguments holder for {@link
 * org.eclipse.che.api.git.GitConnection#remoteUpdate(RemoteUpdateParams)}.
 *
 * @author Igor Vinokur
 */
public class RemoteUpdateParams {

  private List<String> branches;
  private List<String> addUrl;
  private List<String> removeUrl;
  private List<String> addPushUrl;
  private List<String> removePushUrl;
  private String name;
  private boolean isAddBranches;

  private RemoteUpdateParams() {}

  /**
   * Create new {@link RemoteUpdateParams} instance.
   *
   * @param name remote name
   */
  public static RemoteUpdateParams create(String name) {
    return new RemoteUpdateParams().withName(name);
  }

  /** @see RemoteUpdateRequest#getName() */
  public String getName() {
    return name;
  }

  public RemoteUpdateParams withName(String name) {
    this.name = name;
    return this;
  }

  /** @see RemoteUpdateRequest#getBranches() */
  public List<String> getBranches() {
    return branches == null ? new ArrayList<>() : branches;
  }

  public RemoteUpdateParams withBranches(List<String> branches) {
    this.branches = branches;
    return this;
  }

  /** @see RemoteUpdateRequest#isAddBranches() */
  public boolean isAddBranches() {
    return isAddBranches;
  }

  public RemoteUpdateParams withAddBranches(boolean addBranches) {
    isAddBranches = addBranches;
    return this;
  }

  /** @see RemoteUpdateRequest#getAddUrl() */
  public List<String> getAddUrl() {
    return addUrl == null ? new ArrayList<>() : addUrl;
  }

  public RemoteUpdateParams withAddUrl(List<String> addUrl) {
    this.addUrl = addUrl;
    return this;
  }

  /** @see RemoteUpdateRequest#getRemoveUrl() */
  public List<String> getRemoveUrl() {
    return removeUrl == null ? new ArrayList<>() : removeUrl;
  }

  public RemoteUpdateParams withRemoveUrl(List<String> removeUrl) {
    this.removeUrl = removeUrl;
    return this;
  }

  /** @see RemoteUpdateRequest#getAddPushUrl() */
  public List<String> getAddPushUrl() {
    return addPushUrl == null ? new ArrayList<>() : addPushUrl;
  }

  public RemoteUpdateParams withAddPushUrl(List<String> addPushUrl) {
    this.addPushUrl = addPushUrl;
    return this;
  }

  /** @see RemoteUpdateRequest#getRemovePushUrl() */
  public List<String> getRemovePushUrl() {
    return removePushUrl == null ? new ArrayList<>() : removePushUrl;
  }

  public RemoteUpdateParams withRemovePushUrl(List<String> removePushUrl) {
    this.removePushUrl = removePushUrl;
    return this;
  }
}

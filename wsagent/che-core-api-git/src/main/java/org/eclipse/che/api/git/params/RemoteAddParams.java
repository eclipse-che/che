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
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.RemoteAddRequest;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#remoteAdd(RemoteAddParams)}.
 *
 * @author Igor Vinokur
 */
public class RemoteAddParams {

  private List<String> branches;
  private String name;
  private String url;

  private RemoteAddParams() {}

  /**
   * Create new {@link RemoteAddParams} instance.
   *
   * @param name remote name
   * @param url url of remote
   */
  public static RemoteAddParams create(String name, String url) {
    return new RemoteAddParams().withName(name).withUrl(url);
  }

  /** @see RemoteAddRequest#getBranches() */
  public List<String> getBranches() {
    return branches == null ? new ArrayList<>() : branches;
  }

  public RemoteAddParams withBranches(List<String> branches) {
    this.branches = branches;
    return this;
  }

  /** @see RemoteAddRequest#getName() */
  public String getName() {
    return name;
  }

  /** @see RemoteAddRequest#withName(String) */
  public RemoteAddParams withName(String name) {
    this.name = name;
    return this;
  }

  /** @see RemoteAddRequest#getUrl() */
  public String getUrl() {
    return url;
  }

  /** @see RemoteAddRequest#withUrl(String) */
  public RemoteAddParams withUrl(String url) {
    this.url = url;
    return this;
  }
}

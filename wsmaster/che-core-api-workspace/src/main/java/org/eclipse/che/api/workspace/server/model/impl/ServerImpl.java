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
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;

/** @author gazarenkov */
public class ServerImpl implements Server {

  private String url;
  private ServerStatus status;
  private Map<String, String> attributes;

  public ServerImpl() {}

  public ServerImpl(String url, ServerStatus status, Map<String, String> attributes) {
    this.url = url;
    this.status = status;
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    } else {
      this.attributes = new HashMap<>();
    }
  }

  public ServerImpl(Server server) {
    this.url = server.getUrl();
    this.status = server.getStatus();
    if (server.getAttributes() != null) {
      this.attributes = new HashMap<>(server.getAttributes());
    } else {
      this.attributes = new HashMap<>();
    }
  }

  @Override
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public ServerImpl withUrl(String url) {
    this.url = url;
    return this;
  }

  @Override
  public ServerStatus getStatus() {
    return this.status;
  }

  public void setStatus(ServerStatus status) {
    this.status = status;
  }

  public ServerImpl withStatus(ServerStatus status) {
    this.status = status;
    return this;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    if (attributes != null) {
      this.attributes = attributes;
    } else {
      this.attributes = new HashMap<>();
    }
  }

  public ServerImpl withAttributes(Map<String, String> attributes) {
    setAttributes(attributes);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ServerImpl)) {
      return false;
    }
    ServerImpl server = (ServerImpl) o;
    return Objects.equals(getUrl(), server.getUrl())
        && getStatus() == server.getStatus()
        && Objects.equals(getAttributes(), server.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUrl(), getStatus(), getAttributes());
  }

  @Override
  public String toString() {
    return "ServerImpl{"
        + "url='"
        + url
        + '\''
        + ", status="
        + status
        + ", attributes="
        + attributes
        + '}';
  }
}

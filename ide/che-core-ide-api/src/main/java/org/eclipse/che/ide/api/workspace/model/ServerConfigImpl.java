/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.workspace.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;

public class ServerConfigImpl implements ServerConfig {

  private String port;
  private String protocol;
  private String path;
  private Map<String, String> attributes;

  public ServerConfigImpl(
      String port, String protocol, String path, Map<String, String> attributes) {
    this.port = port;
    this.protocol = protocol;
    this.path = path;
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    } else {
      this.attributes = new HashMap<>();
    }
  }

  public ServerConfigImpl(ServerConfig serverConf) {
    this(
        serverConf.getPort(),
        serverConf.getProtocol(),
        serverConf.getPath(),
        serverConf.getAttributes());
  }

  @Override
  public String getPort() {
    return port;
  }

  @Override
  public String getProtocol() {
    return protocol;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ServerConfigImpl)) {
      return false;
    }
    final ServerConfigImpl that = (ServerConfigImpl) obj;
    return Objects.equals(port, that.port)
        && Objects.equals(protocol, that.protocol)
        && Objects.equals(attributes, that.attributes)
        && getPath().equals(that.getPath());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(port);
    hash = 31 * hash + Objects.hashCode(protocol);
    hash = 31 * hash + Objects.hashCode(path);
    hash = 31 * hash + Objects.hashCode(attributes);
    return hash;
  }

  @Override
  public String toString() {
    return "ServerConfigImpl{"
        + "port='"
        + port
        + '\''
        + ", protocol='"
        + protocol
        + '\''
        + ", path='"
        + path
        + '\''
        + ", attributes="
        + attributes
        + '}';
  }
}

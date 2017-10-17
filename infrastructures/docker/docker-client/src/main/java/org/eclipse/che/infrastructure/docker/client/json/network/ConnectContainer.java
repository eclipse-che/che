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
package org.eclipse.che.infrastructure.docker.client.json.network;

import java.util.Objects;

/**
 * Represents configuration that should be passed to docker API to connect container to network.
 *
 * @author Alexander Garagatyi
 */
public class ConnectContainer {
  private String container;
  private EndpointConfig endpointConfig;

  public String getContainer() {
    return container;
  }

  public void setContainer(String container) {
    this.container = container;
  }

  public ConnectContainer withContainer(String container) {
    this.container = container;
    return this;
  }

  public EndpointConfig getEndpointConfig() {
    return endpointConfig;
  }

  public void setEndpointConfig(EndpointConfig endpointConfig) {
    this.endpointConfig = endpointConfig;
  }

  public ConnectContainer withEndpointConfig(EndpointConfig endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConnectContainer)) {
      return false;
    }
    final ConnectContainer that = (ConnectContainer) obj;
    return Objects.equals(container, that.container)
        && Objects.equals(endpointConfig, that.endpointConfig);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(container);
    hash = 31 * hash + Objects.hashCode(endpointConfig);
    return hash;
  }

  @Override
  public String toString() {
    return "ConnectContainer{"
        + "container='"
        + container
        + '\''
        + ", endpointConfig="
        + endpointConfig
        + '}';
  }
}

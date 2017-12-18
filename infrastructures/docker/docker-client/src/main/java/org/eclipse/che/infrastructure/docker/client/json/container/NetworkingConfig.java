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
package org.eclipse.che.infrastructure.docker.client.json.container;

import java.util.Map;
import org.eclipse.che.infrastructure.docker.client.json.network.EndpointConfig;

/**
 * Represents description of network inside {@link
 * org.eclipse.che.infrastructure.docker.client.json.ContainerConfig}
 *
 * @author Alexander Garagatyi
 */
public class NetworkingConfig {
  private Map<String, EndpointConfig> endpointsConfig;

  public Map<String, EndpointConfig> getEndpointsConfig() {
    return endpointsConfig;
  }

  public void setEndpointsConfig(Map<String, EndpointConfig> endpointsConfig) {
    this.endpointsConfig = endpointsConfig;
  }

  public NetworkingConfig withEndpointsConfig(Map<String, EndpointConfig> endpointsConfig) {
    this.endpointsConfig = endpointsConfig;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NetworkingConfig)) {
      return false;
    }
    final NetworkingConfig that = (NetworkingConfig) obj;
    return getEndpointsConfig().equals(that.getEndpointsConfig());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + getEndpointsConfig().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "NetworkingConfig{" + "endpointsConfig=" + endpointsConfig + '}';
  }
}

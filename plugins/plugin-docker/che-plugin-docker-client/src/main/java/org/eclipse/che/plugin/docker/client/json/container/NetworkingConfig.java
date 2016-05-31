/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client.json.container;

import org.eclipse.che.plugin.docker.client.json.network.EndpointConfig;

import java.util.Map;
import java.util.Objects;

/**
 * Represents description of network inside {@link org.eclipse.che.plugin.docker.client.json.ContainerConfig}
 *
 * author Alexander Garagatyi
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkingConfig)) return false;
        NetworkingConfig that = (NetworkingConfig)o;
        return Objects.equals(endpointsConfig, that.endpointsConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointsConfig);
    }

    @Override
    public String toString() {
        return "NetworkingConfig{" +
               "endpointsConfig=" + endpointsConfig +
               '}';
    }
}

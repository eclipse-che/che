/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

/**
 * Description of docker container environment as representation of environment of machines in Che.
 *
 * @author Alexander Garagatyi
 */
public class DockerEnvironment {
    private Map<String, DockerContainerConfig> services;
    private String                             network;

    public DockerEnvironment() {}

    public DockerEnvironment(DockerEnvironment environment) {
        if (environment.getServices() != null) {
            services = environment.getServices()
                                  .entrySet()
                                  .stream()
                                  .collect(toMap(Map.Entry::getKey,
                                                 entry -> new DockerContainerConfig(entry.getValue())));
        }
    }

    /**
     * Mapping of compose services names to services configuration.
     */
    public Map<String, DockerContainerConfig> getServices() {
        if (services == null) {
            services = new HashMap<>();
        }
        return services;
    }

    public void setServices(Map<String, DockerContainerConfig> services) {
        this.services = services;
    }

    public DockerEnvironment withServices(Map<String, DockerContainerConfig> services) {
        this.services = services;
        return this;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public DockerEnvironment withNetwork(String network) {
        this.network = network;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerEnvironment)) return false;
        DockerEnvironment that = (DockerEnvironment)o;
        return Objects.equals(getServices(), that.getServices()) &&
               Objects.equals(getNetwork(), that.getNetwork());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServices(), getNetwork());
    }

    @Override
    public String toString() {
        return "DockerEnvironment{" +
               "services=" + services +
               ", network='" + network + '\'' +
               '}';
    }
}

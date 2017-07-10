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
    private Map<String, DockerContainerConfig> containers;
    private String                             network;

    public DockerEnvironment() {}

    public DockerEnvironment(DockerEnvironment environment) {
        if (environment.getContainers() != null) {
            containers = environment.getContainers()
                                    .entrySet()
                                    .stream()
                                    .collect(toMap(Map.Entry::getKey,
                                                 entry -> new DockerContainerConfig(entry.getValue())));
        }
    }

    /**
     * Mapping of containers names to containers configuration.
     */
    public Map<String, DockerContainerConfig> getContainers() {
        if (containers == null) {
            containers = new HashMap<>();
        }
        return containers;
    }

    public void setContainers(Map<String, DockerContainerConfig> containers) {
        this.containers = containers;
    }

    public DockerEnvironment withContainers(Map<String, DockerContainerConfig> containers) {
        this.containers = containers;
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
        return Objects.equals(getContainers(), that.getContainers()) &&
               Objects.equals(getNetwork(), that.getNetwork());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContainers(), getNetwork());
    }

    @Override
    public String toString() {
        return "DockerEnvironment{" +
               "containers=" + containers +
               ", network='" + network + '\'' +
               '}';
    }
}

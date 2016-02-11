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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.MachineMetadata;
import org.eclipse.che.api.core.model.machine.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data object for {@link MachineMetadata}
 *
 * @author Alexander Garagatyi
 */
public class MachineMetadataImpl implements MachineMetadata {
    private Map<String, String> envVariables;
    private Map<String, String> properties;
    private Map<String, Server> servers;

    public static MachineMetadataImplBuilder builder() {
        return new MachineMetadataImplBuilder();
    }

    public MachineMetadataImpl() {
    }

    public MachineMetadataImpl(MachineMetadata metadata) {
        if (metadata != null && metadata.getEnvVariables() != null) {
            this.envVariables = new HashMap<>(metadata.getEnvVariables());
        }
        if (metadata != null && metadata.getProperties() != null) {
            this.properties = new HashMap<>(metadata.getProperties());
        }
        if (metadata != null && metadata.getServers() != null) {
            this.servers = new HashMap<>(metadata.getServers());
        }
    }

    public MachineMetadataImpl(Map<String, String> envVariables,
                               Map<String, String> properties,
                               Map<String, Server> servers) {
        if (envVariables != null) {
            this.envVariables = new HashMap<>(envVariables);
        }
        if (properties != null) {
            this.properties = new HashMap<>(properties);
        }
        if (servers != null) {
            this.servers = new HashMap<>(servers);
        }
    }

    @Override
    public Map<String, String> getEnvVariables() {
        if (envVariables == null) {
            envVariables = new HashMap<>();
        }
        return envVariables;
    }

    @Override
    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    @Override
    public Map<String, ? extends Server> getServers() {
        if (servers == null) {
            servers = new HashMap<>();
        }
        return servers;
    }

    @Override
    public String projectsRoot() {
        return getEnvVariables().get("CHE_PROJECTS_ROOT");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineMetadataImpl)) return false;
        MachineMetadataImpl that = (MachineMetadataImpl)o;
        return Objects.equals(getEnvVariables(), that.getEnvVariables()) &&
               Objects.equals(getProperties(), that.getProperties()) &&
               Objects.equals(getServers(), that.getServers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnvVariables(), getProperties(), getServers());
    }

    public static class MachineMetadataImplBuilder {
        private Map<String, Server> servers;
        private Map<String, String> properties;
        private Map<String, String> envVariables;

        public MachineMetadataImpl build() {
            return new MachineMetadataImpl(envVariables, properties, servers);
        }

        public MachineMetadataImplBuilder setServers(Map<String, Server> servers) {
            this.servers = servers;
            return this;
        }

        public MachineMetadataImplBuilder setProperties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        public MachineMetadataImplBuilder setEnvVariables(Map<String, String> envVariables) {
            this.envVariables = envVariables;
            return this;
        }
    }
}

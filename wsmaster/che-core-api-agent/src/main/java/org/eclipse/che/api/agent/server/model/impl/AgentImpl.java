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
package org.eclipse.che.api.agent.server.model.impl;

import com.google.common.base.MoreObjects;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Anatoliy Bazko
 */
public class AgentImpl implements Agent {
    private final String               name;
    private final String               version;
    private final List<String>         dependencies;
    private final Map<String, String>  properties;
    private final String               script;
    private final List<ServerConfImpl> servers;

    public AgentImpl(String name,
                     String version,
                     List<String> dependencies,
                     Map<String, String> properties,
                     String script,
                     List<? extends ServerConf> servers) {
        this.name = name;
        this.version = version;
        this.dependencies = dependencies;
        this.properties = properties;
        this.script = script;
        if (servers != null) {
            this.servers = servers.stream()
                                  .map(ServerConfImpl::new)
                                  .collect(Collectors.toList());
        } else {
            this.servers = new ArrayList<>();
        }
    }

    public AgentImpl(Agent agent) {
        this(agent.getName(),
             agent.getVersion(),
             agent.getDependencies(),
             agent.getProperties(),
             agent.getScript(),
             agent.getServers());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public List<String> getDependencies() {
        return MoreObjects.firstNonNull(dependencies, new ArrayList<String>());
    }

    @Override
    public Map<String, String> getProperties() {
        return MoreObjects.firstNonNull(properties, new HashMap<String, String>());
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public List<ServerConfImpl> getServers() {
        return servers;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AgentImpl)) {
            return false;
        }
        final AgentImpl that = (AgentImpl)obj;
        return Objects.equals(name, that.name)
               && Objects.equals(version, that.version)
               && getDependencies().equals(that.getDependencies())
               && getProperties().equals(that.getProperties())
               && Objects.equals(script, that.script)
               && Objects.equals(servers, that.servers);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(version);
        hash = 31 * hash + getDependencies().hashCode();
        hash = 31 * hash + getProperties().hashCode();
        hash = 31 * hash + Objects.hashCode(script);
        hash = 31 * hash + Objects.hashCode(servers);
        return hash;
    }

    @Override
    public String toString() {
        return "AgentImpl{" +
               "name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", dependencies='" + dependencies + '\'' +
               ", servers='" + servers + '\'' +
               ", properties='" + properties + "\'}";
    }
}


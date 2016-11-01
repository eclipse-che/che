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

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.model.workspace.ServerConf2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Anatoliy Bazko
 */
public class AgentImpl implements Agent {
    private final String                             id;
    private final String                             name;
    private final String                             version;
    private final String                             description;
    private final List<String>                       dependencies;
    private final Map<String, String>                properties;
    private final String                             script;
    private final Map<String, ? extends ServerConf2> servers;

    public AgentImpl(String id,
                     String name,
                     String version,
                     String description,
                     List<String> dependencies,
                     Map<String, String> properties,
                     String script,
                     Map<String, ? extends ServerConf2> servers) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.dependencies = dependencies;
        this.properties = properties;
        this.script = script;
        this.servers = servers;
    }

    public AgentImpl(Agent agent) {
        this(agent.getId(),
             agent.getName(),
             agent.getVersion(),
             agent.getDescription(),
             agent.getDependencies(),
             agent.getProperties(),
             agent.getScript(),
             agent.getServers());
    }

    @Override
    public String getId() {
        return id;
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
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> getDependencies() {
        return dependencies != null ? dependencies : new ArrayList<>();
    }

    @Override
    public Map<String, String> getProperties() {
        return properties != null ? properties : new HashMap<>();
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public Map<String, ? extends ServerConf2> getServers() {
        return servers != null ? servers : new HashMap<>();
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
        return Objects.equals(id, that.id)
               && Objects.equals(name, that.name)
               && Objects.equals(version, that.version)
               && getDependencies().equals(that.getDependencies())
               && getProperties().equals(that.getProperties())
               && Objects.equals(script, that.script)
               && Objects.equals(servers, that.servers);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
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
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", version='" + version + '\'' +
               ", dependencies='" + dependencies + '\'' +
               ", servers='" + servers + '\'' +
               ", properties='" + properties + "\'}";
    }
}


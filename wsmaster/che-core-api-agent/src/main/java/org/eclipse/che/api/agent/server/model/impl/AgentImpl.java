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
    private String                             id;
    private String                             name;
    private String                             version;
    private String                             description;
    private List<String>                       dependencies;
    private Map<String, String>                properties;
    private String                             script;
    private Map<String, ? extends ServerConf2> servers;

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
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        return dependencies;
    }

    @Override
    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public Map<String, ? extends ServerConf2> getServers() {
        if (servers == null) {
            servers = new HashMap<>();
        }
        return servers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentImpl)) return false;
        AgentImpl agent = (AgentImpl)o;
        return Objects.equals(getId(), agent.getId()) &&
               Objects.equals(getName(), agent.getName()) &&
               Objects.equals(getVersion(), agent.getVersion()) &&
               Objects.equals(getDescription(), agent.getDescription()) &&
               Objects.equals(getDependencies(), agent.getDependencies()) &&
               Objects.equals(getProperties(), agent.getProperties()) &&
               Objects.equals(getScript(), agent.getScript()) &&
               Objects.equals(getServers(), agent.getServers());
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(getId(),
                      getName(),
                      getVersion(),
                      getDescription(),
                      getDependencies(),
                      getProperties(),
                      getScript(),
                      getServers());
    }

    @Override
    public String toString() {
        return "AgentImpl{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", description='" + description + '\'' +
               ", dependencies=" + dependencies +
               ", properties=" + properties +
               ", script='" + script + '\'' +
               ", servers=" + servers +
               '}';
    }
}


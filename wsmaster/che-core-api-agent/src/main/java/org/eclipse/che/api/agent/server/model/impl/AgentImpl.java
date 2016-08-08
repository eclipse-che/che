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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Anatoliy Bazko
 */
public class AgentImpl implements Agent {
    private final String              name;
    private final String              version;
    private final List<String>        dependencies;
    private final Map<String, String> properties;
    private final String              script;

    public AgentImpl(String name,
                     String version,
                     List<String> dependencies,
                     Map<String, String> properties,
                     String script) {
        this.name = name;
        this.version = version;
        this.dependencies = dependencies;
        this.properties = properties;
        this.script = script;
    }

    public AgentImpl(Agent agent) {
        this(agent.getName(),
             agent.getVersion(),
             agent.getDependencies(),
             agent.getProperties(),
             agent.getScript());
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
        return dependencies;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentImpl)) return false;
        AgentImpl that = (AgentImpl)o;
        return Objects.equals(name, that.name) &&
               Objects.equals(version, that.version) &&
               Objects.equals(dependencies, that.dependencies) &&
               Objects.equals(properties, that.properties) &&
               Objects.equals(script, that.script);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, dependencies, properties, script);
    }
}


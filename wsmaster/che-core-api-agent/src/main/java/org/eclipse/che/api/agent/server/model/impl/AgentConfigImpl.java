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

import org.eclipse.che.api.agent.shared.model.AgentConfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Anatoliy Bazko
 */
public class AgentConfigImpl implements AgentConfig {
    private final String              fqn;
    private final String              version;
    private final List<String>        dependencies;
    private final Map<String, String> properties;
    private final String              script;

    public AgentConfigImpl(String fqn,
                           String version,
                           List<String> dependencies,
                           Map<String, String> properties,
                           String script) {
        this.fqn = fqn;
        this.version = version;
        this.dependencies = dependencies;
        this.properties = properties;
        this.script = script;
    }

    public AgentConfigImpl(AgentConfig agentConfig) {
        this(agentConfig.getFqn(),
             agentConfig.getVersion(),
             agentConfig.getDependencies(),
             agentConfig.getProperties(),
             agentConfig.getScript());
    }

    @Override
    public String getFqn() {
        return fqn;
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
        if (!(o instanceof AgentConfigImpl)) return false;
        AgentConfigImpl that = (AgentConfigImpl)o;
        return Objects.equals(fqn, that.fqn) &&
               Objects.equals(version, that.version) &&
               Objects.equals(dependencies, that.dependencies) &&
               Objects.equals(properties, that.properties) &&
               Objects.equals(script, that.script);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fqn, version, dependencies, properties, script);
    }
}


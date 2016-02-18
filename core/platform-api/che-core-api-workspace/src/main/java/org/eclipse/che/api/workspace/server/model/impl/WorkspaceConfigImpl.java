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
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link WorkspaceConfig}, contains information about workspace creation
 *
 * @author Eugene Voevodin
 * @author gazarenkov
 * @author Alexander Andrienko
 */
public class WorkspaceConfigImpl implements WorkspaceConfig {

    public static WorkspaceConfigBuilder builder() {
        return new WorkspaceConfigBuilder();
    }

    protected String                     name;
    protected String                     description;
    protected String                     defaultEnv;
    protected List<CommandImpl>          commands;
    protected List<ProjectConfigImpl>    projects;
    protected List<EnvironmentStateImpl> environments;
    protected Map<String, String>        attributes;

    public WorkspaceConfigImpl(String name,
                               String description,
                               String defaultEnvName,
                               List<? extends Command> commands,
                               List<? extends ProjectConfig> projects,
                               List<? extends Environment> environments,
                               Map<String, String> attributes) {
        this.name = name;
        this.description = description;

        if (environments != null) {
            this.environments = environments.stream()
                                            .map(EnvironmentStateImpl::new)
                                            .collect(toList());
        }
        if (commands != null) {
            this.commands = commands.stream()
                                    .map(CommandImpl::new)
                                    .collect(toList());
        }
        if (projects != null) {
            this.projects = projects.stream()
                                    .map(ProjectConfigImpl::new)
                                    .collect(toList());
        }
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        }
        setDefaultEnv(defaultEnvName);
    }

    public WorkspaceConfigImpl(WorkspaceConfig workspaceConfig) {
        this(workspaceConfig.getName(),
             workspaceConfig.getDescription(),
             workspaceConfig.getDefaultEnv(),
             workspaceConfig.getCommands(),
             workspaceConfig.getProjects(),
             workspaceConfig.getEnvironments(),
             workspaceConfig.getAttributes());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDefaultEnv() {
        return defaultEnv;
    }

    /**
     * Sets particular environment configured for this workspace  as default
     * Throws NullPointerException if no Env with incoming name configured
     */
    public void setDefaultEnv(String name) {
        if (!environments.stream().anyMatch(env -> env.getName().equals(name))) {
            throw new NullPointerException("No Environment named '" + name + "' found");
        }
        defaultEnv = name;
    }

    @Override
    public List<CommandImpl> getCommands() {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        return commands;
    }

    public void setCommands(List<CommandImpl> commands) {
        this.commands = commands;
    }

    @Override
    public List<ProjectConfigImpl> getProjects() {
        if (projects == null) {
            projects = new ArrayList<>();
        }
        return projects;
    }

    public void setProjects(List<ProjectConfigImpl> projects) {
        this.projects = projects;
    }

    @Override
    public List<EnvironmentStateImpl> getEnvironments() {
        if (environments == null) {
            environments = new ArrayList<>();
        }
        return environments;
    }

    public void setEnvironments(List<EnvironmentStateImpl> environments) {
        this.environments = environments;
    }

    @Override
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorkspaceConfigImpl)) return false;
        final WorkspaceConfigImpl other = (WorkspaceConfigImpl)obj;
        return Objects.equals(name, other.name) &&
               Objects.equals(defaultEnv, other.defaultEnv) &&
               getCommands().equals(other.getCommands()) &&
               getEnvironments().equals(other.getEnvironments()) &&
               getProjects().equals(other.getProjects()) &&
               getAttributes().equals(other.getAttributes()) &&
               Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(defaultEnv);
        hash = 31 * hash + getCommands().hashCode();
        hash = 31 * hash + getEnvironments().hashCode();
        hash = 31 * hash + getProjects().hashCode();
        hash = 31 * hash + getAttributes().hashCode();
        hash = 31 * hash + Objects.hashCode(description);
        return hash;
    }

    @Override
    public String toString() {
        return "WorkspaceConfigImpl{" +
               " name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", defaultEnv='" + defaultEnv + '\'' +
               ", commands=" + commands +
               ", projects=" + projects +
               ", environments=" + environments +
               ", attributes=" + attributes +
               '}';
    }

    public static class WorkspaceConfigBuilder {
        private String                        name;
        private String                        description;
        private String                        defaultEnv;
        private List<? extends Command>       commands;
        private List<? extends ProjectConfig> projects;
        private List<? extends Environment>   environments;
        private Map<String, String>           attributes;

        public WorkspaceConfigBuilder() {
        }

        public WorkspaceConfigBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public WorkspaceConfigBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public WorkspaceConfigBuilder setDefaultEnv(String defaultEnv) {
            this.defaultEnv = defaultEnv;
            return this;
        }

        public WorkspaceConfigBuilder setCommands(List<? extends Command> commands) {
            this.commands = commands;
            return this;
        }

        public WorkspaceConfigBuilder setProjects(List<? extends ProjectConfig> projects) {
            this.projects = projects;
            return this;
        }

        public WorkspaceConfigBuilder setEnvironments(List<? extends Environment> environments) {
            this.environments = environments;
            return this;
        }

        public WorkspaceConfigBuilder setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public WorkspaceConfigImpl build() {
            return new WorkspaceConfigImpl(name, description, defaultEnv, commands, projects, environments, attributes);
        }
    }
}

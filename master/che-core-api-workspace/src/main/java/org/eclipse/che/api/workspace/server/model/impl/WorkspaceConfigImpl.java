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
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Data object for {@link WorkspaceConfig}.
 *
 * @author Alexander Garagatyi
 */
public class WorkspaceConfigImpl implements WorkspaceConfig {

    public static WorkspaceConfigImplBuilder builder() {
        return new WorkspaceConfigImplBuilder();
    }

    private String                       name;
    private String                       description;
    private String                       defaultEnvName;
    private List<CommandImpl>            commands;
    private List<ProjectConfigImpl>      projects;
    private List<EnvironmentImpl>        environments;
    private Map<String, String>          attributes;

    public WorkspaceConfigImpl(String name,
                               String description,
                               String defaultEnvironment,
                               List<? extends Command> commands,
                               List<? extends ProjectConfig> projects,
                               List<? extends Environment> environments,
                               Map<String, String> attributes) {
        this.name = name;
        this.description = description;
        if (environments != null) {
            this.environments = environments.stream()
                                            .map(EnvironmentImpl::new)
                                            .collect(Collectors.toList());
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
        setDefaultEnv(defaultEnvironment);
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
        return defaultEnvName;
    }

    /**
     * Sets particular environment configured for this workspace  as default
     * Throws NullPointerException if no Env with incoming name configured
     */
    public void setDefaultEnv(String name) {
        defaultEnvName = name;
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
    public List<EnvironmentImpl> getEnvironments() {
        if (environments == null) {
            environments = new ArrayList<>();
        }
        return environments;
    }

    public void setEnvironments(List<EnvironmentImpl> environments) {
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
               Objects.equals(defaultEnvName, other.defaultEnvName) &&
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
        hash = 31 * hash + Objects.hashCode(defaultEnvName);
        hash = 31 * hash + getCommands().hashCode();
        hash = 31 * hash + getEnvironments().hashCode();
        hash = 31 * hash + getProjects().hashCode();
        hash = 31 * hash + getAttributes().hashCode();
        hash = 31 * hash + Objects.hashCode(description);
        return hash;
    }

    @Override
    public String toString() {
        return "UsersWorkspaceImpl{" +
               ", name='" + name + '\'' +
               ", defaultEnvName='" + defaultEnvName + '\'' +
               ", commands=" + commands +
               ", projects=" + projects +
               ", attributes=" + attributes +
               ", environments=" + environments +
               ", description='" + description + '\'' +
               '}';
    }

    /**
     * Helps to build complex {@link WorkspaceConfigImpl users workspace instance}.
     *
     * @see WorkspaceConfigImpl#builder()
     */
    public static class WorkspaceConfigImplBuilder {

        protected String                             name;
        protected String                             defaultEnvName;
        protected List<? extends Command>            commands;
        protected List<? extends ProjectConfig>      projects;
        protected Map<String, String>                attributes;
        protected List<? extends Environment>        environments;
        protected String                             description;

        WorkspaceConfigImplBuilder() {
        }

        public WorkspaceConfigImpl build() {
            return new WorkspaceConfigImpl(name,
                                           description,
                                           defaultEnvName,
                                           commands,
                                           projects,
                                           environments,
                                           attributes);
        }

        public WorkspaceConfigImplBuilder fromConfig(WorkspaceConfig workspaceConfig) {
            this.name = workspaceConfig.getName();
            this.description = workspaceConfig.getDescription();
            this.defaultEnvName = workspaceConfig.getDefaultEnv();
            this.projects = workspaceConfig.getProjects();
            this.commands = workspaceConfig.getCommands();
            this.environments = workspaceConfig.getEnvironments();
            this.attributes = workspaceConfig.getAttributes();
            return this;
        }

        public WorkspaceConfigImplBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public WorkspaceConfigImplBuilder setDefaultEnv(String defaultEnvName) {
            this.defaultEnvName = defaultEnvName;
            return this;
        }

        public WorkspaceConfigImplBuilder setCommands(List<? extends Command> commands) {
            this.commands = commands;
            return this;
        }

        public WorkspaceConfigImplBuilder setProjects(List<? extends ProjectConfig> projects) {
            this.projects = projects;
            return this;
        }

        public WorkspaceConfigImplBuilder setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public WorkspaceConfigImplBuilder setEnvironments(List<? extends Environment> environments) {
            this.environments = environments;
            return this;
        }

        public WorkspaceConfigImplBuilder setDescription(String description) {
            this.description = description;
            return this;
        }
    }
}

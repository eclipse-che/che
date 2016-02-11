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
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.commons.lang.NameGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Data object for {@link UsersWorkspace}.
 *
 * @author Eugene Voevodin
 * @author gazarenkov
 */
public class UsersWorkspaceImpl implements UsersWorkspace {

    public static UsersWorkspaceImplBuilder builder() {
        return new UsersWorkspaceImplBuilder();
    }

    private String                     id;
    private String                     name;
    private String                     owner;
    private String                     defaultEnv;
    private List<CommandImpl>          commands;
    private List<ProjectConfigImpl>    projects;
    private Map<String, String>        attributes;
    private List<EnvironmentStateImpl> environments;
    private String                     description;
    private boolean                    isTemporary;
    private WorkspaceStatus            status;

    public UsersWorkspaceImpl(String id,
                              String name,
                              String owner,
                              Map<String, String> attributes,
                              List<? extends Command> commands,
                              List<? extends ProjectConfig> projects,
                              List<? extends Environment> environments,
                              String defaultEnvironment,
                              String description) {
        this.id = id;
        this.name = name;
        this.owner = owner;
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
        setDefaultEnv(defaultEnvironment);
    }

    public UsersWorkspaceImpl(WorkspaceConfig workspaceConfig, String id, String owner) {
        this(id,
             workspaceConfig.getName(),
             owner,
             workspaceConfig.getAttributes(),
             workspaceConfig.getCommands(),
             workspaceConfig.getProjects(),
             workspaceConfig.getEnvironments(),
             workspaceConfig.getDefaultEnv(),
             workspaceConfig.getDescription());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }

    public WorkspaceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UsersWorkspaceImpl)) return false;
        final UsersWorkspaceImpl other = (UsersWorkspaceImpl)obj;
        return Objects.equals(owner, other.owner) &&
               Objects.equals(id, other.id) &&
               Objects.equals(name, other.name) &&
               Objects.equals(defaultEnv, other.defaultEnv) &&
               Objects.equals(status, other.status) &&
               isTemporary == other.isTemporary &&
               getCommands().equals(other.getCommands()) &&
               getEnvironments().equals(other.getEnvironments()) &&
               getProjects().equals(other.getProjects()) &&
               getAttributes().equals(other.getAttributes()) &&
               Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(owner);
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(defaultEnv);
        hash = 31 * hash + Objects.hashCode(status);
        hash = 31 * hash + Boolean.hashCode(isTemporary);
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
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", owner='" + owner + '\'' +
               ", defaultEnv='" + defaultEnv + '\'' +
               ", commands=" + commands +
               ", projects=" + projects +
               ", attributes=" + attributes +
               ", environments=" + environments +
               ", description='" + description + '\'' +
               ", isTemporary=" + isTemporary +
               ", status=" + status +
               '}';
    }

    /**
     * Helps to build complex {@link UsersWorkspaceImpl users workspace instance}.
     *
     * @see UsersWorkspaceImpl#builder()
     */
    public static class UsersWorkspaceImplBuilder {

        protected String                        id;
        protected String                        name;
        protected String                        owner;
        protected String                        defaultEnv;
        protected List<? extends Command>       commands;
        protected List<? extends ProjectConfig> projects;
        protected Map<String, String>           attributes;
        protected List<? extends Environment>   environments;
        protected String                        description;
        protected boolean                       isTemporary;
        protected WorkspaceStatus               status;

        UsersWorkspaceImplBuilder() {
        }

        public UsersWorkspaceImpl build() {
            final UsersWorkspaceImpl workspace = new UsersWorkspaceImpl(id,
                                                                        name,
                                                                        owner,
                                                                        attributes,
                                                                        commands,
                                                                        projects,
                                                                        environments,
                                                                        defaultEnv,
                                                                        description);
            workspace.setStatus(status);
            workspace.setTemporary(isTemporary);
            return workspace;
        }

        public UsersWorkspaceImplBuilder generateId() {
            id = NameGenerator.generate("workspace", 16);
            return this;
        }

        public UsersWorkspaceImplBuilder fromConfig(WorkspaceConfig workspaceConfig) {
            this.name = workspaceConfig.getName();
            this.description = workspaceConfig.getDescription();
            this.defaultEnv = workspaceConfig.getDefaultEnv();
            this.projects = workspaceConfig.getProjects();
            this.commands = workspaceConfig.getCommands();
            this.environments = workspaceConfig.getEnvironments();
            this.attributes = workspaceConfig.getAttributes();
            return this;
        }

        public UsersWorkspaceImplBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public UsersWorkspaceImplBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public UsersWorkspaceImplBuilder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public UsersWorkspaceImplBuilder setDefaultEnv(String defaultEnv) {
            this.defaultEnv = defaultEnv;
            return this;
        }

        public UsersWorkspaceImplBuilder setCommands(List<? extends Command> commands) {
            this.commands = commands;
            return this;
        }

        public UsersWorkspaceImplBuilder setProjects(List<? extends ProjectConfig> projects) {
            this.projects = projects;
            return this;
        }

        public UsersWorkspaceImplBuilder setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public UsersWorkspaceImplBuilder setEnvironments(List<? extends Environment> environments) {
            this.environments = environments;
            return this;
        }

        public UsersWorkspaceImplBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public UsersWorkspaceImplBuilder setTemporary(boolean isTemporary) {
            this.isTemporary = isTemporary;
            return this;
        }

        public UsersWorkspaceImplBuilder setStatus(WorkspaceStatus status) {
            this.status = status;
            return this;
        }
    }
}

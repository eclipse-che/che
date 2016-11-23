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
import org.eclipse.che.commons.annotation.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Data object for {@link WorkspaceConfig}.
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@Entity(name = "WorkspaceConfig")
@Table(name = "workspaceconfig")
public class WorkspaceConfigImpl implements WorkspaceConfig {

    public static WorkspaceConfigImplBuilder builder() {
        return new WorkspaceConfigImplBuilder();
    }

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "defaultenv", nullable = false)
    private String defaultEnv;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "commands_id")
    private List<CommandImpl> commands;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "projects_id")
    private List<ProjectConfigImpl> projects;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "environments_id")
    @MapKeyColumn(name = "environments_key")
    private Map<String, EnvironmentImpl> environments;

    public WorkspaceConfigImpl() {}

    public WorkspaceConfigImpl(String name,
                               String description,
                               String defaultEnv,
                               List<? extends Command> commands,
                               List<? extends ProjectConfig> projects,
                               Map<String, ? extends Environment> environments) {
        this.name = name;
        this.defaultEnv = defaultEnv;
        this.description = description;
        if (environments != null) {
            this.environments = environments.entrySet()
                                            .stream()
                                            .collect(toMap(Map.Entry::getKey,
                                                           entry -> new EnvironmentImpl(entry.getValue())));
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
    }

    public WorkspaceConfigImpl(WorkspaceConfig workspaceConfig) {
        this(workspaceConfig.getName(),
             workspaceConfig.getDescription(),
             workspaceConfig.getDefaultEnv(),
             workspaceConfig.getCommands(),
             workspaceConfig.getProjects(),
             workspaceConfig.getEnvironments());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireNonNull(name, "Non-null name required");
    }

    @Override
    @Nullable
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

    public void setDefaultEnv(String defaultEnv) {
        this.defaultEnv = defaultEnv;
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
    public Map<String, EnvironmentImpl> getEnvironments() {
        if (environments == null) {
            return new HashMap<>();
        }
        return environments;
    }

    public void setEnvironments(Map<String, EnvironmentImpl> environments) {
        this.environments = environments;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorkspaceConfigImpl)) return false;
        final WorkspaceConfigImpl other = (WorkspaceConfigImpl)obj;
        return Objects.equals(name, other.name)
               && Objects.equals(defaultEnv, other.defaultEnv)
               && getCommands().equals(other.getCommands())
               && getEnvironments().equals(other.getEnvironments())
               && getProjects().equals(other.getProjects())
               && Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(defaultEnv);
        hash = 31 * hash + getCommands().hashCode();
        hash = 31 * hash + getEnvironments().hashCode();
        hash = 31 * hash + getProjects().hashCode();
        hash = 31 * hash + Objects.hashCode(description);
        return hash;
    }

    @Override
    public String toString() {
        return "WorkspaceConfigImpl{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", defaultEnv='" + defaultEnv + '\'' +
               ", commands=" + commands +
               ", projects=" + projects +
               ", environments=" + environments +
               '}';
    }

    /**
     * Helps to build complex {@link WorkspaceConfigImpl users workspace instance}.
     *
     * @see WorkspaceConfigImpl#builder()
     */
    public static class WorkspaceConfigImplBuilder {

        private String                             name;
        private String                             defaultEnvName;
        private List<? extends Command>            commands;
        private List<? extends ProjectConfig>      projects;
        private Map<String, ? extends Environment> environments;
        private String                             description;

        private WorkspaceConfigImplBuilder() {}

        public WorkspaceConfigImpl build() {
            return new WorkspaceConfigImpl(name,
                                           description,
                                           defaultEnvName,
                                           commands,
                                           projects,
                                           environments);
        }

        public WorkspaceConfigImplBuilder fromConfig(WorkspaceConfig workspaceConfig) {
            this.name = workspaceConfig.getName();
            this.description = workspaceConfig.getDescription();
            this.defaultEnvName = workspaceConfig.getDefaultEnv();
            this.projects = workspaceConfig.getProjects();
            this.commands = workspaceConfig.getCommands();
            this.environments = workspaceConfig.getEnvironments();
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

        public WorkspaceConfigImplBuilder setEnvironments(Map<String, ? extends Environment> environments) {
            this.environments = environments;
            return this;
        }

        public WorkspaceConfigImplBuilder setDescription(String description) {
            this.description = description;
            return this;
        }
    }
}

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

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.workspace.server.jpa.WorkspaceEntityListener;
import org.eclipse.che.commons.lang.NameGenerator;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

/**
 * Data object for {@link Workspace}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "Workspace")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "namespace"}))
@NamedQueries(
        {
                @NamedQuery(name = "Workspace.getByNamespace",
                            query = "SELECT w FROM Workspace w WHERE w.namespace = :namespace"),
                @NamedQuery(name = "Workspace.getByName",
                            query = "SELECT w FROM Workspace w WHERE w.namespace = :namespace AND w.name = :name"),
                @NamedQuery(name = "Workspace.getAll",
                            query = "SELECT w FROM Workspace w")
        }
)
@EntityListeners(WorkspaceEntityListener.class)
public class WorkspaceImpl implements Workspace {

    public static WorkspaceImplBuilder builder() {
        return new WorkspaceImplBuilder();
    }

    @Id
    private String id;

    @Column(nullable = false)
    private String namespace;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkspaceConfigImpl config;

    @ElementCollection
    private Map<String, String> attributes;

    @Basic
    private boolean isTemporary;

    // This mapping is present here just for generation of the constraint between
    // snapshots and workspace, it's impossible to do so on snapshot side
    // as workspace and machine are different modules and cyclic reference will appear
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspaceId", insertable = false, updatable = false)
    private List<SnapshotImpl> snapshots;

    @Transient
    private WorkspaceStatus status = STOPPED;

    @Transient
    private WorkspaceRuntimeImpl runtime;

    public WorkspaceImpl() {}

    public WorkspaceImpl(String id, String namespace, WorkspaceConfig config) {
        this(id, namespace, config.getName(), config, null, null, false, STOPPED);
    }

    public WorkspaceImpl(String id,
                         String namespace,
                         String name,
                         WorkspaceConfig config,
                         WorkspaceRuntime runtime,
                         Map<String, String> attributes,
                         boolean isTemporary,
                         WorkspaceStatus status) {
        this.id = id;
        this.namespace = namespace;
        this.name = name;
        this.config = new WorkspaceConfigImpl(config);
        if (runtime != null) {
            this.runtime = new WorkspaceRuntimeImpl(runtime);
        }
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        }
        this.status = firstNonNull(status, STOPPED);
        this.isTemporary = isTemporary;
    }

    public WorkspaceImpl(Workspace workspace) {
        this(workspace.getId(), workspace.getNamespace(), workspace.getConfig());
        this.attributes = new HashMap<>(workspace.getAttributes());
        if (workspace.getRuntime() != null) {
            this.runtime = new WorkspaceRuntimeImpl(workspace.getRuntime());
        }
        this.isTemporary = workspace.isTemporary();
        this.status = firstNonNull(workspace.getStatus(), STOPPED);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public WorkspaceConfigImpl getConfig() {
        return config;
    }

    public void setConfig(WorkspaceConfigImpl config) {
        this.config = config;
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
    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean temporary) {
        isTemporary = temporary;
    }

    @Override
    public WorkspaceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
    }

    @Override
    public WorkspaceRuntimeImpl getRuntime() {
        return runtime;
    }

    public void setRuntime(WorkspaceRuntimeImpl runtime) {
        this.runtime = runtime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorkspaceImpl)) return false;
        final WorkspaceImpl other = (WorkspaceImpl)obj;
        return Objects.equals(id, other.id)
               && Objects.equals(namespace, other.namespace)
               && Objects.equals(name, other.name)
               && Objects.equals(status, other.status)
               && isTemporary == other.isTemporary
               && getAttributes().equals(other.getAttributes())
               && Objects.equals(config, other.config)
               && Objects.equals(runtime, other.runtime);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(namespace);
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(status);
        hash = 31 * hash + Objects.hashCode(config);
        hash = 31 * hash + getAttributes().hashCode();
        hash = 31 * hash + Boolean.hashCode(isTemporary);
        hash = 31 * hash + Objects.hashCode(runtime);
        return hash;
    }

    @Override
    public String toString() {
        return "WorkspaceImpl{" +
               "id='" + id + '\'' +
               ", namespace='" + namespace + '\'' +
               ", name='" + name + '\'' +
               ", config=" + config +
               ", isTemporary=" + isTemporary +
               ", status=" + status +
               ", attributes=" + attributes +
               ", runtime=" + runtime +
               '}';
    }

    /**
     * Helps to build complex {@link WorkspaceImpl workspace} instance.
     *
     * @see WorkspaceImpl#builder()
     */
    public static class WorkspaceImplBuilder {

        private String               id;
        private String               namespace;
        private String               name;
        private boolean              isTemporary;
        private WorkspaceStatus      status;
        private WorkspaceConfigImpl  config;
        private WorkspaceRuntimeImpl runtime;
        private Map<String, String>  attributes;

        private WorkspaceImplBuilder() {}

        public WorkspaceImpl build() {
            return new WorkspaceImpl(id, namespace, name, config, runtime, attributes, isTemporary, status);
        }

        public WorkspaceImplBuilder generateId() {
            id = NameGenerator.generate("workspace", 16);
            return this;
        }

        public WorkspaceImplBuilder setConfig(WorkspaceConfig workspaceConfig) {
            this.config = new WorkspaceConfigImpl(workspaceConfig);
            this.name = workspaceConfig.getName();
            return this;
        }

        public WorkspaceImplBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public WorkspaceImplBuilder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public WorkspaceImplBuilder setTemporary(boolean isTemporary) {
            this.isTemporary = isTemporary;
            return this;
        }

        public WorkspaceImplBuilder setStatus(WorkspaceStatus status) {
            this.status = status;
            return this;
        }

        public WorkspaceImplBuilder setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public WorkspaceImplBuilder setRuntime(WorkspaceRuntimeImpl runtime) {
            this.runtime = runtime;
            return this;
        }

        public WorkspaceImplBuilder setName(String name) {
            this.name = name;
            return this;
        }
    }
}

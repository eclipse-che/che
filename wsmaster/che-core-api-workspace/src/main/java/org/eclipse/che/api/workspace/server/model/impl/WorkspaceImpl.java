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

import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.workspace.server.jpa.WorkspaceEntityListener;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data object for {@link Workspace}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "Workspace")
@Table(name = "workspace")
@NamedQueries(
        {
                @NamedQuery(name = "Workspace.getByNamespace",
                            query = "SELECT w FROM Workspace w WHERE w.account.name = :namespace"),
                @NamedQuery(name = "Workspace.getByName",
                            query = "SELECT w FROM Workspace w WHERE w.account.name = :namespace AND w.name = :name"),
                @NamedQuery(name = "Workspace.getAll",
                            query = "SELECT w FROM Workspace w")
        }
)
@EntityListeners({WorkspaceEntityListener.class, WorkspaceImpl.SyncNameOnUpdateAndPersistEventListener.class})
public class WorkspaceImpl implements Workspace {

    public static WorkspaceImplBuilder builder() {
        return new WorkspaceImplBuilder();
    }

    @Id
    @Column(name = "id")
    private String id;

    /**
     * The original workspace name is workspace.config.name
     * this attribute is stored for unique constraint with account id.
     * See {@link #syncName()}.
     */
    @Column(name = "name")
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "config_id")
    private WorkspaceConfigImpl config;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workspace_attributes", joinColumns = @JoinColumn(name = "workspace_id"))
    @MapKeyColumn(name = "attributes_key")
    @Column(name = "attributes")
    private Map<String, String> attributes;

    @Column(name = "istemporary")
    private boolean isTemporary;

    @ManyToOne
    @JoinColumn(name = "accountid", nullable = false)
    private AccountImpl account;

    // This mapping is for explicit constraint between
    // snapshots and workspace, it's impossible to do so on snapshot side
    // as workspace and machine are different modules and cyclic reference will appear.
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspaceId", insertable = false, updatable = false)
    private List<SnapshotImpl> snapshots;

    @Transient
    private WorkspaceStatus status;

    @Transient
    private WorkspaceRuntimeImpl runtime;

    public WorkspaceImpl() {}

    public WorkspaceImpl(String id, Account account, WorkspaceConfig config) {
        this(id, account, config, null, null, false, null);
    }

    public WorkspaceImpl(String id,
                         Account account,
                         WorkspaceConfig config,
                         WorkspaceRuntime runtime,
                         Map<String, String> attributes,
                         boolean isTemporary,
                         WorkspaceStatus status) {
        this.id = id;
        if (account != null) {
            this.account = new AccountImpl(account);
        }
        if (config != null) {
            this.config = new WorkspaceConfigImpl(config);
        }
        if (runtime != null) {
            this.runtime = new WorkspaceRuntimeImpl(runtime);
        }
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        }
        this.isTemporary = isTemporary;
        this.status = status;
    }

    public WorkspaceImpl(Workspace workspace, Account account) {
        this(workspace.getId(),
             account,
             workspace.getConfig(),
             workspace.getRuntime(),
             workspace.getAttributes(),
             workspace.isTemporary(),
             workspace.getStatus());
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
        if (account != null) {
            return account.getName();
        }
        return null;
    }

    public void setAccount(AccountImpl account) {
        this.account = account;
    }

    public AccountImpl getAccount() {
        return account;
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
               && Objects.equals(getNamespace(), other.getNamespace())
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
        hash = 31 * hash + Objects.hashCode(getNamespace());
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
               ", namespace='" + getNamespace() + '\'' +
               ", name='" + name + '\'' +
               ", config=" + config +
               ", isTemporary=" + isTemporary +
               ", status=" + status +
               ", attributes=" + attributes +
               ", runtime=" + runtime +
               '}';
    }

    /** Syncs {@link #name} with config name. */
    private void syncName() {
        name = config == null ? null : config.getName();
    }

    /**
     * {@link PreUpdate} and {@link PrePersist} methods are not called when
     * the configuration is updated and workspace object is not, while listener
     * methods are always called, even if workspace instance is not changed.
     */
    public static class SyncNameOnUpdateAndPersistEventListener extends DescriptorEventAdapter {
        @Override
        public void preUpdate(DescriptorEvent event) {
            ((WorkspaceImpl)event.getObject()).syncName();
        }

        @Override
        public void prePersist(DescriptorEvent event) {
            ((WorkspaceImpl)event.getObject()).syncName();
        }

        @Override
        public void preUpdateWithChanges(DescriptorEvent event) {
            ((WorkspaceImpl)event.getObject()).syncName();
        }
    }

    /**
     * Helps to build complex {@link WorkspaceImpl workspace} instance.
     *
     * @see WorkspaceImpl#builder()
     */
    public static class WorkspaceImplBuilder {

        private String              id;
        private Account             account;
        private boolean             isTemporary;
        private WorkspaceStatus     status;
        private WorkspaceConfig     config;
        private WorkspaceRuntime    runtime;
        private Map<String, String> attributes;

        private WorkspaceImplBuilder() {}

        public WorkspaceImpl build() {
            return new WorkspaceImpl(id, account, config, runtime, attributes, isTemporary, status);
        }

        public WorkspaceImplBuilder generateId() {
            id = NameGenerator.generate("workspace", 16);
            return this;
        }

        public WorkspaceImplBuilder setConfig(WorkspaceConfig workspaceConfig) {
            this.config = workspaceConfig;
            return this;
        }

        public WorkspaceImplBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public WorkspaceImplBuilder setAccount(Account account) {
            this.account = account;
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

        public WorkspaceImplBuilder setRuntime(WorkspaceRuntime runtime) {
            this.runtime = runtime;
            return this;
        }
    }
}

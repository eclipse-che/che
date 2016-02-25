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

import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.commons.lang.NameGenerator;

import java.util.Objects;

/**
 * Data object for {@link UsersWorkspace}.
 *
 * @author Eugene Voevodin
 * @author gazarenkov
 * @author Alexander Garagatyi
 */
public class UsersWorkspaceImpl implements UsersWorkspace {

    public static UsersWorkspaceImplBuilder builder() {
        return new UsersWorkspaceImplBuilder();
    }

    private String              id;
    private String              owner;
    private boolean             isTemporary;
    private WorkspaceStatus     status;
    private WorkspaceConfigImpl workspaceConfig;

    public UsersWorkspaceImpl(WorkspaceConfig config,
                              String id,
                              String owner) {
        this.id = id;
        this.owner = owner;
        this.workspaceConfig = new WorkspaceConfigImpl(config);
    }

    public UsersWorkspaceImpl(UsersWorkspace workspace) {
        this(workspace.getConfig(),
             workspace.getId(),
             workspace.getOwner());
        setStatus(workspace.getStatus());
        setTemporary(workspace.isTemporary());
    }

    @Override
    public WorkspaceConfigImpl getConfig() {
        return workspaceConfig;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UsersWorkspaceImpl)) return false;
        final UsersWorkspaceImpl other = (UsersWorkspaceImpl)obj;
        return Objects.equals(owner, other.owner) &&
               Objects.equals(id, other.id) &&
               Objects.equals(status, other.status) &&
               isTemporary == other.isTemporary &&
               Objects.equals(workspaceConfig, other.getConfig());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(owner);
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(status);
        hash = 31 * hash + Boolean.hashCode(isTemporary);
        hash = 31 * hash + Objects.hashCode(workspaceConfig);
        return hash;
    }

    @Override
    public String toString() {
        return "UsersWorkspaceImpl{" +
               "id='" + id + '\'' +
               ", owner='" + owner + '\'' +
               ", isTemporary=" + isTemporary +
               ", status=" + status +
               ", config=" + workspaceConfig +
               '}';
    }

    /**
     * Helps to build complex {@link UsersWorkspaceImpl users workspace instance}.
     *
     * @see UsersWorkspaceImpl#builder()
     */
    public static class UsersWorkspaceImplBuilder {

        protected String              id;
        protected String              owner;
        protected boolean             isTemporary;
        protected WorkspaceStatus     status;
        protected WorkspaceConfigImpl workspaceConfig;

        UsersWorkspaceImplBuilder() {
        }

        public UsersWorkspaceImpl build() {
            final UsersWorkspaceImpl workspace = new UsersWorkspaceImpl(workspaceConfig,
                                                                        id,
                                                                        owner);
            workspace.setStatus(status);
            workspace.setTemporary(isTemporary);
            return workspace;
        }

        public UsersWorkspaceImplBuilder generateId() {
            id = NameGenerator.generate("workspace", 16);
            return this;
        }

        public UsersWorkspaceImplBuilder fromConfig(WorkspaceConfig workspaceConfig) {
            this.workspaceConfig = new WorkspaceConfigImpl(workspaceConfig);
            return this;
        }

        public UsersWorkspaceImplBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public UsersWorkspaceImplBuilder setOwner(String owner) {
            this.owner = owner;
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

        public UsersWorkspaceImplBuilder setConfig(WorkspaceConfigImpl workspaceConfig) {
            this.workspaceConfig = workspaceConfig;
            return this;
        }
    }
}

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
import org.eclipse.che.commons.lang.NameGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

/**
 * Data object for {@link Workspace}.
 *
 * @author Yevhenii Voevodin
 */
public class WorkspaceImpl implements Workspace {

    public static WorkspaceImplBuilder builder() {
        return new WorkspaceImplBuilder();
    }

    private String id;
    private String namespace;

    private WorkspaceConfigImpl  config;
    private boolean              isTemporary;
    private WorkspaceStatus      status;
    private Map<String, String>  attributes;
    private WorkspaceRuntimeImpl runtime;

    public WorkspaceImpl(String id, String namespace, WorkspaceConfig config) {
        this(id, namespace, config, null, null, false, STOPPED);
    }

    public WorkspaceImpl(String id,
                         String namespace,
                         WorkspaceConfig config,
                         WorkspaceRuntime runtime,
                         Map<String, String> attributes,
                         boolean isTemporary,
                         WorkspaceStatus status) {
        this.id = id;
        this.namespace = namespace;
        this.config = new WorkspaceConfigImpl(config);
        if (runtime  != null) {
            this.runtime = new WorkspaceRuntimeImpl(runtime);
        }
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        }
        this.status = firstNonNull(status, STOPPED);
        this.isTemporary = isTemporary;
    }

    public WorkspaceImpl(Workspace workspace) {
        this(workspace.getId(),
             workspace.getNamespace(),
             workspace.getConfig());
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

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public WorkspaceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
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

    public void setTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }

    @Override
    public WorkspaceConfigImpl getConfig() {
        return config;
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
               ", config=" + config +
               ", isTemporary=" + isTemporary +
               ", status=" + status +
               ", attributes=" + attributes +
               ", runtime=" + runtime +
               '}';
    }

    /**
     * Helps to build complex {@link WorkspaceImpl users workspace instance}.
     *
     * @see WorkspaceImpl#builder()
     */
    public static class WorkspaceImplBuilder {

        private String              id;
        private String              namespace;
        private boolean             isTemporary;
        private WorkspaceStatus     status;
        private WorkspaceConfigImpl config;
        private WorkspaceRuntimeImpl runtime;
        private Map<String, String> attributes;

        private WorkspaceImplBuilder() {}

        public WorkspaceImpl build() {
            return new WorkspaceImpl(id, namespace, config, runtime, attributes, isTemporary, status);
        }

        public WorkspaceImplBuilder generateId() {
            id = NameGenerator.generate("workspace", 16);
            return this;
        }

        public WorkspaceImplBuilder setConfig(WorkspaceConfig workspaceConfig) {
            this.config = new WorkspaceConfigImpl(workspaceConfig);
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
    }
}

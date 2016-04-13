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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.machine.server.spi.impl.InstanceKeyImpl;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.core.model.machine.Snapshot;
import org.eclipse.che.commons.lang.NameGenerator;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Saved state of {@link org.eclipse.che.api.machine.server.spi.Instance}.
 *
 * @author Yevhenii Voevodin
 */
public class SnapshotImpl implements Snapshot {

    public static SnapshotBuilder builder() {
        return new SnapshotBuilder();
    }

    private final String  workspaceId;
    private final String  machineName;
    private final String  envName;
    private final String  id;
    private final String  type;
    private final String  namespace;
    private final boolean isDev;
    private final long    creationDate;

    private String          description;
    private InstanceKeyImpl instanceKey;

    public SnapshotImpl(Snapshot snapshot) {
        this(snapshot.getId(),
             snapshot.getType(),
             null,
             snapshot.getNamespace(),
             snapshot.getCreationDate(),
             snapshot.getWorkspaceId(),
             snapshot.getDescription(),
             snapshot.isDev(),
             snapshot.getMachineName(),
             snapshot.getEnvName());
    }

    public SnapshotImpl(String id,
                        String type,
                        InstanceKey instanceKey,
                        String namespace,
                        long creationDate,
                        String workspaceId,
                        String description,
                        boolean isDev,
                        String machineName,
                        String envName) {
        this.id = requireNonNull(id, "Required non-null snapshot id");
        this.type = requireNonNull(type, "Required non-null snapshot type");
        this.namespace = requireNonNull(namespace, "Required non-null snapshot namespace");
        this.workspaceId = requireNonNull(workspaceId, "Required non-null workspace id for snapshot");
        this.machineName = requireNonNull(machineName, "Required non-null snapshot machine name");
        this.envName = requireNonNull(envName, "Required non-null environment name for snapshot");
        this.instanceKey = instanceKey != null ? new InstanceKeyImpl(instanceKey) : null;
        this.description = description;
        this.isDev = isDev;
        this.creationDate = creationDate;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    public InstanceKey getInstanceKey() {
        return instanceKey;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    @Override
    public String getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public String getMachineName() {
        return machineName;
    }

    @Override
    public String getEnvName() {
        return envName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isDev() {
        return this.isDev;
    }

    public void setInstanceKey(InstanceKey instanceKey) {
        this.instanceKey = instanceKey != null ? new InstanceKeyImpl(instanceKey.getFields()) : null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SnapshotImpl)) {
            return false;
        }
        final SnapshotImpl snapshot = (SnapshotImpl)o;
        return creationDate == snapshot.creationDate
               && isDev == snapshot.isDev
               && Objects.equals(id, snapshot.id)
               && Objects.equals(type, snapshot.type)
               && Objects.equals(instanceKey, snapshot.instanceKey)
               && Objects.equals(namespace, snapshot.namespace)
               && Objects.equals(workspaceId, snapshot.workspaceId)
               && Objects.equals(description, snapshot.description)
               && Objects.equals(machineName, snapshot.machineName)
               && Objects.equals(envName, snapshot.envName);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(creationDate);
        hash = hash * 31 + Boolean.hashCode(isDev);
        hash = hash * 31 + Objects.hashCode(id);
        hash = hash * 31 + Objects.hashCode(type);
        hash = hash * 31 + Objects.hashCode(instanceKey);
        hash = hash * 31 + Objects.hashCode(namespace);
        hash = hash * 31 + Objects.hashCode(workspaceId);
        hash = hash * 31 + Objects.hashCode(description);
        hash = hash * 31 + Objects.hashCode(machineName);
        hash = hash * 31 + Objects.hashCode(envName);
        return hash;
    }

    @Override
    public String toString() {
        return "SnapshotImpl{" +
               "id='" + id + '\'' +
               ", type='" + type + '\'' +
               ", instanceKey=" + instanceKey +
               ", namespace='" + namespace + '\'' +
               ", creationDate=" + creationDate +
               ", isDev=" + isDev +
               ", description='" + description + '\'' +
               ", workspaceId='" + workspaceId + '\'' +
               ", machineName='" + machineName + '\'' +
               ", envName='" + envName + '\'' +
               '}';
    }

    /**
     * Helps to build {@link Snapshot snapshot} instance.
     */
    public static class SnapshotBuilder {

        private String      workspaceId;
        private String      machineName;
        private String      envName;
        private String      id;
        private String      type;
        private String      namespace;
        private String      description;
        private InstanceKey instanceKey;
        private boolean     isDev;
        private long        creationDate;

        public SnapshotBuilder fromConfig(MachineConfig machineConfig) {
            machineName = machineConfig.getName();
            type = machineConfig.getType();
            return this;
        }

        public SnapshotBuilder generateId() {
            id = NameGenerator.generate("snapshot", 16);
            return this;
        }

        public SnapshotBuilder setWorkspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public SnapshotBuilder setMachineName(String machineName) {
            this.machineName = machineName;
            return this;
        }

        public SnapshotBuilder setEnvName(String envName) {
            this.envName = envName;
            return this;
        }

        public SnapshotBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public SnapshotBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public SnapshotBuilder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public SnapshotBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public SnapshotBuilder setInstanceKey(InstanceKey instanceKey) {
            this.instanceKey = instanceKey;
            return this;
        }

        public SnapshotBuilder setDev(boolean dev) {
            isDev = dev;
            return this;
        }

        public SnapshotBuilder setCreationDate(long creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public SnapshotBuilder useCurrentCreationDate() {
            creationDate = System.currentTimeMillis();
            return this;
        }

        public SnapshotImpl build() {
            return new SnapshotImpl(id, type, instanceKey, namespace, creationDate, workspaceId, description, isDev, machineName, envName);
        }
    }
}

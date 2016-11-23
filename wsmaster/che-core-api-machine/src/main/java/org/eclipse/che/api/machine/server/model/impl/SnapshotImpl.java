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
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.Snapshot;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.commons.lang.NameGenerator;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Saved state of {@link Instance}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "Snapshot")
@NamedQueries(
        {
                @NamedQuery(name = "Snapshot.getByMachine",
                            query = "SELECT snapshot " +
                                    "FROM Snapshot snapshot " +
                                    "WHERE snapshot.workspaceId = :workspaceId" +
                                    "  AND snapshot.envName     = :envName" +
                                    "  AND snapshot.machineName = :machineName"),
                @NamedQuery(name = "Snapshot.findSnapshots",
                            query = "SELECT snapshot " +
                                    "FROM Snapshot snapshot " +
                                    "WHERE snapshot.workspaceId = :workspaceId"),
                @NamedQuery(name = "Snapshot.findByWorkspaceAndEnvironment",
                            query = "SELECT snapshot " +
                                    "FROM Snapshot snapshot " +
                                    "WHERE snapshot.workspaceId = :workspaceId " +
                                    "  AND snapshot.envName = :envName")
        }
)
@Table(name = "snapshot")
public class SnapshotImpl implements Snapshot {

    public static SnapshotBuilder builder() {
        return new SnapshotBuilder();
    }

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "workspaceid", nullable = false)
    private String workspaceId;

    @Column(name = "machinename", nullable = false)
    private String machineName;

    @Column(name = "envname", nullable = false)
    private String envName;

    @Column(name = "type")
    private String type;

    @Column(name = "isdev")
    private boolean isDev;

    @Column(name = "creationdate")
    private long creationDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Embedded
    private MachineSourceImpl machineSource;

    public SnapshotImpl() {}

    public SnapshotImpl(Snapshot snapshot) {
        this(snapshot.getId(),
             snapshot.getType(),
             null,
             snapshot.getCreationDate(),
             snapshot.getWorkspaceId(),
             snapshot.getDescription(),
             snapshot.isDev(),
             snapshot.getMachineName(),
             snapshot.getEnvName());
    }

    public SnapshotImpl(SnapshotImpl snapshot) {
        this(snapshot.getId(),
             snapshot.getType(),
             snapshot.getMachineSource(),
             snapshot.getCreationDate(),
             snapshot.getWorkspaceId(),
             snapshot.getDescription(),
             snapshot.isDev(),
             snapshot.getMachineName(),
             snapshot.getEnvName());
    }

    public SnapshotImpl(String id,
                        String type,
                        MachineSource machineSource,
                        long creationDate,
                        String workspaceId,
                        String description,
                        boolean isDev,
                        String machineName,
                        String envName) {
        this.id = id;
        this.type = type;
        this.workspaceId = workspaceId;
        this.machineName = machineName;
        this.envName = envName;
        this.machineSource = machineSource != null ? new MachineSourceImpl(machineSource) : null;
        this.description = description;
        this.isDev = isDev;
        this.creationDate = creationDate;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MachineSourceImpl getMachineSource() {
        return machineSource;
    }

    public void setMachineSource(MachineSourceImpl machineSource) {
        this.machineSource = machineSource;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    @Override
    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    @Override
    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isDev() {
        return this.isDev;
    }

    public void setDev(boolean dev) {
        isDev = dev;
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
               && Objects.equals(machineSource, snapshot.machineSource)
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
        hash = hash * 31 + Objects.hashCode(machineSource);
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
               ", machineSource=" + machineSource +
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

        private String        workspaceId;
        private String        machineName;
        private String        envName;
        private String        id;
        private String        type;
        private String        description;
        private MachineSource machineSource;
        private boolean       isDev;
        private long          creationDate;

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

        public SnapshotBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public SnapshotBuilder setMachineSource(MachineSource machineSource) {
            this.machineSource = machineSource;
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
            return new SnapshotImpl(id, type, machineSource, creationDate, workspaceId, description, isDev, machineName, envName);
        }
    }
}

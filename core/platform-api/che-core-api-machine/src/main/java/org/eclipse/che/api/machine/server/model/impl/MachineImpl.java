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

import org.eclipse.che.api.core.model.machine.Channels;
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineMetadata;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineStatus;

import java.util.Objects;

/**
 * Data object for {@link Machine}.
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public class MachineImpl extends MachineStateImpl implements Machine {

    public static MachineImplBuilder builder() {
        return new MachineImplBuilder();
    }

    private MachineMetadataImpl metadata;

    public MachineImpl(boolean isDev,
                       String name,
                       String type,
                       MachineSource source,
                       Limits limits,
                       String id,
                       MachineMetadata metadata,
                       Channels channels,
                       String workspace,
                       String owner,
                       String envName,
                       MachineStatus status) {
        super(isDev, type, name, source, limits, id, channels, workspace, owner, envName, status);
        this.metadata = new MachineMetadataImpl(metadata);
    }

    public MachineImpl(Machine machine) {
        this(machine.isDev(),
             machine.getName(),
             machine.getType(),
             machine.getSource(),
             machine.getLimits(),
             machine.getId(),
             machine.getMetadata(),
             machine.getChannels(),
             machine.getWorkspaceId(),
             machine.getOwner(),
             machine.getEnvName(),
             machine.getStatus());
    }

    @Override
    public MachineMetadataImpl getMetadata() {
        if (metadata == null) {
            metadata = new MachineMetadataImpl();
        }
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineImpl)) return false;
        if (!super.equals(o)) return false;
        final MachineImpl other = (MachineImpl)o;
        return Objects.equals(getMetadata(), other.getMetadata());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash * 31 + getMetadata().hashCode();
        return hash;
    }

    /**
     * Helps to build complex {@link MachineImpl machine impl}.
     *
     * @see MachineImpl#builder()
     */
    public static class MachineImplBuilder {

        private boolean         isDev;
        private Limits          limits;
        private String          name;
        private String          type;
        private String          id;
        private MachineSource   source;
        private MachineMetadata metadata;
        private Channels        channels;
        private String          workspaceId;
        private String          owner;
        private String          envName;
        private MachineStatus   machineStatus;

        public MachineImpl build() {
            return new MachineImpl(isDev,
                                   name,
                                   type,
                                   source,
                                   limits,
                                   id,
                                   metadata,
                                   channels,
                                   workspaceId,
                                   owner,
                                   envName,
                                   machineStatus);
        }

        public MachineImplBuilder setDev(boolean isDev) {
            this.isDev = isDev;
            return this;
        }

        public MachineImplBuilder setLimits(Limits limits) {
            this.limits = limits;
            return this;
        }

        public MachineImplBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public MachineImplBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public MachineImplBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public MachineImplBuilder setSource(MachineSource source) {
            this.source = source;
            return this;
        }

        public MachineImplBuilder setMetadata(MachineMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public MachineImplBuilder setChannels(Channels channels) {
            this.channels = channels;
            return this;
        }

        public MachineImplBuilder setWorkspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public MachineImplBuilder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public MachineImplBuilder setStatus(MachineStatus status) {
            this.machineStatus = status;
            return this;
        }

        public MachineImplBuilder setEnvName(String envName){
            this.envName = envName;
            return this;
        }
    }
}

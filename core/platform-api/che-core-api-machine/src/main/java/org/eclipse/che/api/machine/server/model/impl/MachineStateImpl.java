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
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineState;
import org.eclipse.che.api.core.model.machine.MachineStatus;

import java.util.Objects;

/**
 * Data object for {@link MachineState}.
 *
 * @author Alexander Garagatyi
 */
public class MachineStateImpl extends MachineConfigImpl implements MachineState {

    private String        id;
    private ChannelsImpl  channels;
    private String        workspace;
    private String        envName;
    private String        owner;
    private MachineStatus machineStatus;

    public MachineStateImpl(boolean isDev,
                            String name,
                            String type,
                            MachineSource source,
                            Limits limits,
                            String id,
                            Channels channels,
                            String workspaceId,
                            String owner,
                            String envName,
                            MachineStatus machineStatus) {
        super(isDev, name, type, source, limits);
        this.id = id;
        this.channels = new ChannelsImpl(channels);
        this.workspace = workspaceId;
        this.owner = owner;
        this.machineStatus = machineStatus;
        this.envName = envName;
    }

    public MachineStateImpl(MachineState machine) {
        this(machine.isDev(),
             machine.getName(),
             machine.getType(),
             machine.getSource(),
             machine.getLimits(),
             machine.getId(),
             machine.getChannels(),
             machine.getWorkspaceId(),
             machine.getOwner(),
             machine.getEnvName(),
             machine.getStatus());
    }

    public MachineStateImpl(MachineConfig machine) {
        this(machine.isDev(),
             machine.getName(),
             machine.getType(),
             machine.getSource(),
             machine.getLimits(),
             null,
             null,
             null,
             null,
             null,
             null);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Channels getChannels() {
        if (channels == null) {
            channels = new ChannelsImpl(null, null);
        }
        return channels;
    }

    @Override
    public String getWorkspaceId() {
        return workspace;
    }

    @Override
    public String getEnvName() {
        return envName;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setChannels(ChannelsImpl channels) {
        this.channels = channels;
    }

    @Override
    public MachineStatus getStatus() {
        return machineStatus;
    }

    public void setStatus(MachineStatus status) {
        this.machineStatus = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineStateImpl)) return false;
        if (!super.equals(o)) return false;
        final MachineStateImpl other = (MachineStateImpl)o;
        return Objects.equals(id, other.id)
               && Objects.equals(getChannels(), other.getChannels())
               && Objects.equals(workspace, other.workspace)
               && Objects.equals(owner, other.owner)
               && Objects.equals(envName, other.envName);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash * 31 + Objects.hashCode(id);
        hash = hash * 31 + getChannels().hashCode();
        hash = hash * 31 + Objects.hashCode(workspace);
        hash = hash * 31 + Objects.hashCode(owner);
        hash = hash * 31 + Objects.hashCode(envName);
        return hash;
    }
}

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
package org.eclipse.che.api.machine.server.impl;

import org.eclipse.che.api.core.model.machine.Channels;
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineState;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.machine.server.spi.Instance;

/**
 * @author Alexander Garagatyi
 */
public abstract class AbstractInstance implements Instance {
    private final String              id;
    private final String              type;
    private final String              owner;
    private final String              workspaceId;
    private final boolean             isDev;
    private final String              displayName;
    private final Channels            channels;
    private final Limits              limits;
    private final MachineSource       source;
    private final String              envName;

    private MachineStatus machineStatus;

    public AbstractInstance(String id,
                            String type,
                            String workspaceId,
                            String owner,
                            boolean isDev,
                            String displayName,
                            Channels channels,
                            Limits limits,
                            MachineSource source,
                            MachineStatus machineStatus,
                            String envName) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.workspaceId = workspaceId;
        this.isDev = isDev;
        this.displayName = displayName;
        this.channels = channels;
        this.limits = limits;
        this.source = source;
        this.machineStatus = machineStatus;
        this.envName = envName;
    }

    public AbstractInstance(MachineState machineState) {
        this.id = machineState.getId();
        this.type = machineState.getType();
        this.owner = machineState.getOwner();
        this.workspaceId = machineState.getWorkspaceId();
        this.isDev = machineState.isDev();
        this.displayName = machineState.getName();
        this.channels = machineState.getChannels();
        this.limits = machineState.getLimits();
        this.source = machineState.getSource();
        this.machineStatus = machineState.getStatus();
        this.envName = machineState.getEnvName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public boolean isDev() {
        return isDev;
    }

    @Override
    public Channels getChannels() {
        return channels;
    }

    @Override
    public Limits getLimits() {
        return limits;
    }

    @Override
    public MachineSource getSource() {
        return source;
    }

    @Override
    public String getEnvName() {
        return envName;
    }

    @Override
    public synchronized MachineStatus getStatus() {
        return machineStatus;
    }

    @Override
    public synchronized void setStatus(MachineStatus status) {
        this.machineStatus = status;
    }
}

/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.MachineStatus;

import java.util.Objects;

/**
 * Data object for {@link Machine}.
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public class MachineImpl implements Machine {

    public static MachineImplBuilder builder() {
        return new MachineImplBuilder();
    }

    private final MachineConfigImpl      machineConfig;
    private final MachineRuntimeInfoImpl machineRuntime;
    private final String                 workspace;
    private final String                 envName;
    private final String                 owner;

    private MachineStatus status;
    private String        id;

    public MachineImpl(MachineConfig machineConfig,
                       String id,
                       String workspace,
                       String envName,
                       String owner,
                       MachineStatus status,
                       MachineRuntimeInfo machineRuntime) {
        this.workspace = workspace;
        this.envName = envName;
        this.owner = owner;
        this.machineConfig = new MachineConfigImpl(machineConfig);
        this.id = id;
        this.status = status;
        this.machineRuntime = machineRuntime != null ? new MachineRuntimeInfoImpl(machineRuntime) : null;
    }

    public MachineImpl(Machine machine) {
        this(machine.getConfig(),
             machine.getId(),
             machine.getWorkspaceId(),
             machine.getEnvName(),
             machine.getOwner(),
             machine.getStatus(),
             machine.getRuntime());
    }

    @Override
    public MachineConfigImpl getConfig() {
        return machineConfig;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public MachineStatus getStatus() {
        return status;
    }

    @Override
    public MachineRuntimeInfoImpl getRuntime() {
        return machineRuntime;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineImpl)) return false;
        MachineImpl machine = (MachineImpl)o;
        return Objects.equals(machineConfig, machine.machineConfig) &&
               Objects.equals(id, machine.id) &&
               Objects.equals(machineRuntime, machine.machineRuntime) &&
               Objects.equals(workspace, machine.workspace) &&
               Objects.equals(envName, machine.envName) &&
               Objects.equals(owner, machine.owner) &&
               status == machine.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(machineConfig, id, machineRuntime, workspace, envName, owner, status);
    }

    /**
     * Helps to build complex {@link MachineImpl machine impl}.
     *
     * @see MachineImpl#builder()
     */
    public static class MachineImplBuilder {

        private MachineConfig      machineConfig;
        private String             id;
        private String             envName;
        private String             owner;
        private String             workspaceId;
        private MachineStatus      machineStatus;
        private MachineRuntimeInfo machineRuntime;

        public MachineImpl build() {
            return new MachineImpl(machineConfig,
                                   id,
                                   workspaceId,
                                   envName,
                                   owner,
                                   machineStatus,
                                   machineRuntime);
        }

        public MachineImplBuilder fromMachine(Machine machine) {
            this.envName = machine.getEnvName();
            this.id = machine.getId();
            this.machineConfig = machine.getConfig();
            this.machineRuntime = machine.getRuntime();
            this.machineStatus = machine.getStatus();
            this.owner = machine.getOwner();
            this.workspaceId = machine.getWorkspaceId();
            return this;
        }

        public MachineImplBuilder setConfig(MachineConfig machineConfig) {
            this.machineConfig = machineConfig;
            return this;
        }

        public MachineImplBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public MachineImplBuilder setStatus(MachineStatus status) {
            this.machineStatus = status;
            return this;
        }

        public MachineImplBuilder setEnvName(String envName) {
            this.envName = envName;
            return this;
        }

        public MachineImplBuilder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public MachineImplBuilder setWorkspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public MachineImplBuilder setRuntime(MachineRuntimeInfo machineRuntime) {
            this.machineRuntime = machineRuntime;
            return this;
        }
    }
}

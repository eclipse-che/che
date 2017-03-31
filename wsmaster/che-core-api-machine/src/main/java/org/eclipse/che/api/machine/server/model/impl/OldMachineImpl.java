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

import org.eclipse.che.api.core.model.machine.OldMachine;
import org.eclipse.che.api.core.model.machine.OldMachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.machine.MachineStatus;

import java.util.Objects;

/**
 * Data object for {@link OldMachine}.
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public class OldMachineImpl implements OldMachine {

    public static MachineImplBuilder builder() {
        return new MachineImplBuilder();
    }

    private final OldMachineConfigImpl machineConfig;
    private final MachineImpl          machineRuntime;
    private final String               workspace;
    private final String               envName;
    private final String               owner;

    private MachineStatus status;
    private String        id;

    public OldMachineImpl(OldMachineConfig machineConfig,
                          String id,
                          String workspace,
                          String envName,
                          String owner,
                          MachineStatus status,
                          Machine machine) {
        this.workspace = workspace;
        this.envName = envName;
        this.owner = owner;
        this.machineConfig = new OldMachineConfigImpl(machineConfig);
        this.id = id;
        this.status = status;
        this.machineRuntime = machine != null ? new MachineImpl(machine.getProperties(), machine.getServers()) : null;
    }

    public OldMachineImpl(OldMachine machine) {
        this(machine.getConfig(),
             machine.getId(),
             machine.getWorkspaceId(),
             machine.getEnvName(),
             machine.getOwner(),
             machine.getStatus(),
             machine.getRuntime());
    }

    @Override
    public OldMachineConfigImpl getConfig() {
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
    public MachineImpl getRuntime() {
        return machineRuntime;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OldMachineImpl)) return false;
        OldMachineImpl machine = (OldMachineImpl)o;
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
     * Helps to build complex {@link OldMachineImpl machine impl}.
     *
     * @see OldMachineImpl#builder()
     */
    public static class MachineImplBuilder {

        private OldMachineConfig machineConfig;
        private String           id;
        private String           envName;
        private String           owner;
        private String           workspaceId;
        private MachineStatus    machineStatus;
        private Machine          machine;

        public OldMachineImpl build() {
            return new OldMachineImpl(machineConfig,
                                      id,
                                      workspaceId,
                                      envName,
                                      owner,
                                      machineStatus,
                                      machine);
        }

        public MachineImplBuilder fromMachine(OldMachine machine) {
            this.envName = machine.getEnvName();
            this.id = machine.getId();
            this.machineConfig = machine.getConfig();
            this.machine = machine.getRuntime();
            this.machineStatus = machine.getStatus();
            this.owner = machine.getOwner();
            this.workspaceId = machine.getWorkspaceId();
            return this;
        }

        public MachineImplBuilder setConfig(OldMachineConfig machineConfig) {
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

        public MachineImplBuilder setRuntime(Machine machine) {
            this.machine = machine;
            return this;
        }
    }
}

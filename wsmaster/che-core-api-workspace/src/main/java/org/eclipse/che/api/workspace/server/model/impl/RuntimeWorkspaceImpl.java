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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.RuntimeWorkspace;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Data object for {@link RuntimeWorkspace}.
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public class RuntimeWorkspaceImpl extends UsersWorkspaceImpl implements RuntimeWorkspace {

    public static RuntimeWorkspaceBuilder builder() {
        return new RuntimeWorkspaceBuilder();
    }

    private final String rootFolder;

    private MachineImpl       devMachine;
    private List<MachineImpl> machines;
    private String            activeEnvName;

    public RuntimeWorkspaceImpl(UsersWorkspace workspace,
                                Machine devMachine,
                                List<? extends Machine> machines,
                                String rootFolder,
                                String currentEnvironment) {
        super(workspace);
        if (devMachine != null) {
            this.devMachine = new MachineImpl(devMachine);
        }
        this.activeEnvName = currentEnvironment;
        this.rootFolder = rootFolder;
        if (machines != null) {
            this.machines = machines.stream()
                                    .map(MachineImpl::new)
                                    .collect(toList());
        }
    }

    public RuntimeWorkspaceImpl(RuntimeWorkspace runtimeWorkspace) {
        this(runtimeWorkspace,
             runtimeWorkspace.getDevMachine(),
             runtimeWorkspace.getMachines(),
             runtimeWorkspace.getRootFolder(),
             runtimeWorkspace.getActiveEnv());
    }

    @Override
    public MachineImpl getDevMachine() {
        return devMachine;
    }

    @Override
    public List<MachineImpl> getMachines() {
        if (machines == null) {
            machines = new ArrayList<>();
        }
        return machines;
    }

    @Override
    public String getRootFolder() {
        return rootFolder;
    }

    @Override
    public String getActiveEnv() {
        return activeEnvName;
    }

    public void setDevMachine(MachineImpl devMachine) {
        this.devMachine = devMachine;
    }

    public void setMachines(List<MachineImpl> machines) {
        this.machines = machines;
    }

    public void setActiveEnv(String activeEnvName) {
        this.activeEnvName = activeEnvName;
    }

    public EnvironmentImpl getActiveEnvironment() {
        return getConfig().getEnvironments()
                          .stream()
                          .filter(env -> env.getName().equals(activeEnvName))
                          .findAny()
                          .get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuntimeWorkspaceImpl)) return false;
        if (!super.equals(o)) return false;
        RuntimeWorkspaceImpl that = (RuntimeWorkspaceImpl)o;
        return Objects.equals(devMachine, that.devMachine) &&
               Objects.equals(machines, that.machines) &&
               Objects.equals(activeEnvName, that.activeEnvName) &&
               Objects.equals(rootFolder, that.rootFolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), devMachine, machines, rootFolder);
    }

    /**
     * Helps to build complex {@link RuntimeWorkspaceImpl runtime workspace}.
     *
     * @see RuntimeWorkspaceImpl#builder()
     */
    public static class RuntimeWorkspaceBuilder extends UsersWorkspaceImplBuilder {

        private String                  rootFolder;
        private String                  activeEnvName;
        private Machine                 devMachine;
        private List<? extends Machine> machines;

        public RuntimeWorkspaceImpl build() {
            return new RuntimeWorkspaceImpl(super.build(),
                                            devMachine,
                                            machines,
                                            rootFolder,
                                            activeEnvName);
        }

        public RuntimeWorkspaceBuilder fromWorkspace(UsersWorkspace workspace) {
            this.id = workspace.getId();
            this.owner = workspace.getOwner();
            this.isTemporary = workspace.isTemporary();
            this.status = workspace.getStatus();
            this.workspaceConfig = new WorkspaceConfigImpl(workspace.getConfig());
            return this;
        }

        @Override
        public RuntimeWorkspaceBuilder setId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public RuntimeWorkspaceBuilder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public RuntimeWorkspaceBuilder setRootFolder(String rootFolder) {
            this.rootFolder = rootFolder;
            return this;
        }

        public RuntimeWorkspaceBuilder setActiveEnv(String activeEnvName) {
            this.activeEnvName = activeEnvName;
            return this;
        }

        public RuntimeWorkspaceBuilder setDevMachine(Machine devMachine) {
            this.devMachine = devMachine;
            return this;
        }

        @Override
        public RuntimeWorkspaceBuilder setStatus(WorkspaceStatus status) {
            super.setStatus(status);
            return this;
        }

        public RuntimeWorkspaceBuilder setMachines(List<? extends Machine> machines) {
            this.machines = machines;
            return this;
        }

        @Override
        public RuntimeWorkspaceBuilder setTemporary(boolean isTemporary) {
            super.setTemporary(isTemporary);
            return this;
        }
    }
}

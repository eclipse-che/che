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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitalii Parfonov
 */

public class ActiveRuntime implements WorkspaceRuntime {

    protected WorkspaceRuntime workspaceRuntime;
    private String id;
    private String rootFolder;
    private DevMachine devMachine;
    private List<MachineEntity> machines;

    public ActiveRuntime(WorkspaceRuntime workspaceRuntime) {
        this.workspaceRuntime = workspaceRuntime;
        if (workspaceRuntime != null) {
            id = workspaceRuntime.getActiveEnv();
            rootFolder = workspaceRuntime.getRootFolder();
            devMachine = new DevMachine(workspaceRuntime.getDevMachine());
            machines = new ArrayList<>();
            for(Machine machine : workspaceRuntime.getMachines()) {
                machines.add(new MachineEntityImpl(machine));
            }
        }
    }


    @Override
    public String getActiveEnv() {
        return id;
    }

    @Override
    public String getRootFolder() {
        return rootFolder;
    }

    @Override
    public DevMachine getDevMachine() {
        return devMachine;
    }

    @Override
    public List<MachineEntity> getMachines() {
        return machines;
    }
}

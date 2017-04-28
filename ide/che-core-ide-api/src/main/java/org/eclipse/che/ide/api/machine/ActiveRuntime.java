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

import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.workspace.shared.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Vitalii Parfonov
 */
public class ActiveRuntime {

    private DevMachine          devMachine;
    private List<MachineEntity> machines;

    public ActiveRuntime(Workspace workspace) {
        Runtime workspaceRuntime = workspace.getRuntime();

        WorkspaceConfig workspaceConfig = workspace.getConfig();
        String defaultEnv = workspaceConfig.getDefaultEnv();
        Environment defEnvironment = workspaceConfig.getEnvironments().get(defaultEnv);

        String devMachineName = Utils.getDevMachineName(defEnvironment);
        Machine devMachine = workspaceRuntime.getMachines().get(devMachineName);

        this.devMachine = new DevMachine(devMachineName, devMachine);
        machines = new ArrayList<>();

        for (Entry<String, ? extends Machine> entry : workspaceRuntime.getMachines().entrySet()) {
            machines.add(new MachineEntityImpl(entry.getKey(), entry.getValue()));
        }
    }

    public DevMachine getDevMachine() {
        return devMachine;
    }

    public List<MachineEntity> getMachines() {
        return machines;
    }
}

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
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;

/**
 * @author Vitalii Parfonov
 */
public class ActiveRuntime {

    private DevMachine                 devMachine;
    private Map<String, MachineEntity> machines = new HashMap<>();

    public ActiveRuntime(Workspace workspace) {
        Runtime workspaceRuntime = workspace.getRuntime();

        Log.info(ActiveRuntime.class, workspaceRuntime);
        Log.info(ActiveRuntime.class, workspaceRuntime.getMachines().entrySet());
//        WorkspaceConfig workspaceConfig = workspace.getConfig();
//        String envName = workspaceRuntime.getActiveEnv();
////        String defaultEnv = workspaceConfig.getDefaultEnv();
//        Environment env = workspaceConfig.getEnvironments().get(envName);
//
//        String devMachineName = Utils.getDevMachineName(env);
//
//        Machine devMachine = workspaceRuntime.getMachines().get(devMachineName);
//
//        this.devMachine = new DevMachine(devMachineName, devMachine);

        if(workspaceRuntime.getMachines() != null)
        for (Entry<String, ? extends Machine> entry : workspaceRuntime.getMachines().entrySet()) {
            machines.put(entry.getKey(), new MachineEntityImpl(entry.getKey(), entry.getValue()));
            if(entry.getValue().getServers().containsKey(WSAGENT_REFERENCE)) {
                this.devMachine = new DevMachine(entry.getKey(), entry.getValue());
            }
        }

        Log.info(ActiveRuntime.class, devMachine);
//        if(this.devMachine == null)
//            throw new RuntimeException("No WS-AGENT Server configured for workspace: " + workspace.getId());

    }

    public DevMachine getDevMachine() {
        return devMachine;
    }

    public List<MachineEntity> getMachines() {
        return new ArrayList<>(machines.values());
    }

    public Optional<MachineEntity> getMachineByName(String name) {
        return Optional.ofNullable(machines.get(name));
    }
}

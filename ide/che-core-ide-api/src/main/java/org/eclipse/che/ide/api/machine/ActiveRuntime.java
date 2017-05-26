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
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * @author Vitalii Parfonov
 * @deprecated use {@link org.eclipse.che.ide.api.workspace.model.RuntimeImpl}
 */
@Deprecated
public class ActiveRuntime {

    private DevMachine                 devMachine;
    private Map<String, MachineEntity> machines;

    public ActiveRuntime(Workspace workspace) {
        Runtime workspaceRuntime = workspace.getRuntime();

        WorkspaceConfig workspaceConfig = workspace.getConfig();
        String defaultEnv = workspaceConfig.getDefaultEnv();
        Environment defEnvironment = workspaceConfig.getEnvironments().get(defaultEnv);

        String devMachineName = Utils.getDevMachineName(defEnvironment);
        Machine devMachine = workspaceRuntime.getMachines().get(devMachineName);

        this.devMachine = new DevMachine(devMachineName, devMachine);
        machines = new HashMap<>();

        for (Entry<String, ? extends Machine> entry : workspaceRuntime.getMachines().entrySet()) {
            machines.put(entry.getKey(), new MachineEntityImpl(entry.getKey(), entry.getValue()));
        }
    }

    @Deprecated
    public DevMachine getDevMachine() {
        return devMachine;
    }

    @Deprecated
    /** @deprecated use {@link RuntimeImpl#getMachines()} */
    public List<MachineEntity> getMachines() {
        return new ArrayList<>(machines.values());
    }

    @Deprecated
    /** @deprecated use {@link org.eclipse.che.ide.api.workspace.model.RuntimeImpl#getMachineByName(String)} */
    public Optional<MachineEntity> getMachineByName(String name) {
        return Optional.ofNullable(machines.get(name));
    }
}

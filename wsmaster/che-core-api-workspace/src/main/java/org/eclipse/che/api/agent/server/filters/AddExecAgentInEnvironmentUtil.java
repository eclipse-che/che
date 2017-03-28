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
package org.eclipse.che.api.agent.server.filters;

import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;

import java.util.ArrayList;
import java.util.Map;

/**
 * Adds exec agent into each agents list in {@link WorkspaceConfigDto} where terminal agent is present.
 *
 * @author Alexander Garagatyi
 */
public class AddExecAgentInEnvironmentUtil {
    public static void addExecAgent(WorkspaceConfigDto workspaceConfig) {
        if (workspaceConfig != null) {
            Map<String, EnvironmentDto> environments = workspaceConfig.getEnvironments();
            if (environments != null) {
                for (EnvironmentDto environment : environments.values()) {
                    if (environment != null && environment.getMachines() != null) {
                        for (ExtendedMachineDto machine : environment.getMachines().values()) {
                            if (machine.getAgents() != null) {
                                if (machine.getAgents().contains("org.eclipse.che.terminal") &&
                                    !machine.getAgents().contains("org.eclipse.che.exec")) {
                                    ArrayList<String> updatedAgents = new ArrayList<>(machine.getAgents());
                                    updatedAgents.add("org.eclipse.che.exec");
                                    machine.setAgents(updatedAgents);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

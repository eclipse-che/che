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
package org.eclipse.che.api.machine.shared.dto;

import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.DelegateRule;
import org.eclipse.che.dto.shared.DelegateTo;

import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface MachineRuntimeInfoDto extends MachineRuntimeInfo {
    @Override
    @DelegateTo(client = @DelegateRule(type = ProjectsRootResolver.class, method = "getProjectsRoot"),
                server = @DelegateRule(type = ProjectsRootResolver.class, method = "getProjectsRoot"))
    String projectsRoot();

    MachineRuntimeInfoDto withEnvVariables(Map<String, String> envVariables);

    MachineRuntimeInfoDto withProperties(Map<String, String> properties);

    @Override
    Map<String, ServerDto> getServers();

    MachineRuntimeInfoDto withServers(Map<String, ServerDto> servers);

    class ProjectsRootResolver {
        public static String getProjectsRoot(MachineRuntimeInfo machineRuntime) {
            return machineRuntime.getEnvVariables().get("CHE_PROJECTS_ROOT");
        }
    }
}

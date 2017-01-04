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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface MachineDto extends Machine, Hyperlinks {

    @Override
    MachineConfigDto getConfig();

    MachineDto withConfig(MachineConfigDto machineConfig);

    MachineDto withId(String id);

    MachineDto withWorkspaceId(String workspaceId);

    MachineDto withEnvName(String envName);

    MachineDto withOwner(String owner);

    MachineDto withStatus(MachineStatus machineStatus);

    @Override
    MachineRuntimeInfoDto getRuntime();

    MachineDto withRuntime(MachineRuntimeInfoDto machineRuntime);

    List<Link> getLinks();

    @Override
    MachineDto withLinks(List<Link> links);
}

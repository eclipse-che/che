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

import org.eclipse.che.api.core.model.machine.OldMachine;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface OldMachineDto extends OldMachine, Hyperlinks {

    @Override
    OldMachineConfigDto getConfig();

    OldMachineDto withConfig(OldMachineConfigDto machineConfig);

    OldMachineDto withId(String id);

    OldMachineDto withWorkspaceId(String workspaceId);

    OldMachineDto withEnvName(String envName);

    OldMachineDto withOwner(String owner);

    OldMachineDto withStatus(MachineStatus machineStatus);

    @Override
    MachineDto getRuntime();

    OldMachineDto withRuntime(MachineDto machineRuntime);

    List<Link> getLinks();

    @Override
    OldMachineDto withLinks(List<Link> links);
}

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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface WorkspaceRuntimeDto extends WorkspaceRuntime, Hyperlinks {

    WorkspaceRuntimeDto withActiveEnv(String activeEnvName);

    @Override
    MachineDto getDevMachine();

    WorkspaceRuntimeDto withDevMachine(MachineDto machine);

    @Override
    List<MachineDto> getMachines();

    WorkspaceRuntimeDto withMachines(List<MachineDto> machines);

    WorkspaceRuntimeDto withRootFolder(String rootFolder);

    WorkspaceRuntimeDto withLinks(List<Link> links);
}

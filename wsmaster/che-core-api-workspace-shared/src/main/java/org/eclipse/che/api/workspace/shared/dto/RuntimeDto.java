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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface RuntimeDto extends Runtime {

    @Override
    String getActiveEnv();

    void setActiveEnv(String activeEnv);

    RuntimeDto withActiveEnv(String activeEnvName);

//    @Override
//    OldMachineDto getDevMachine();

//    void setDevMachine(OldMachineDto machine);

//    RuntimeDto withDevMachine(OldMachineDto machine);

    @Override
    Map<String, MachineDto> getMachines();

    void setMachines(Map<String, MachineDto> machines);

    RuntimeDto withMachines(Map<String, MachineDto> machines);

    @Override
    String getOwner();

    RuntimeDto withOwner(String owner);

    String getUserToken();

    RuntimeDto withUserToken(String userToken);

    void setUserToken(String userToken);


    //    void setRootFolder(String rootFolder);
//
//    RuntimeDto withRootFolder(String rootFolder);

//    RuntimeDto withLinks(List<Link> links);
}

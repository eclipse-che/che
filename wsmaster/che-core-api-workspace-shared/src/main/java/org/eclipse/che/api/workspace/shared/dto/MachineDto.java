/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface MachineDto extends Machine {

    @Override
    Map<String, String> getProperties();

    MachineDto withProperties(Map<String, String> properties);

    @Override
    Map<String, ServerDto> getServers();

    MachineDto withServers(Map<String, ServerDto> servers);


}

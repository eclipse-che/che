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
package org.eclipse.che.api.machine.shared.dto;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface MachineDto extends MachineStateDto, Machine {
    @Override
    MachineMetadataDto getMetadata();

    MachineDto withMetadata(MachineMetadataDto metadata);

    @Override
    MachineDto withId(String id);

    @Override
    MachineDto withWorkspaceId(String workspaceId);

    @Override
    MachineDto withOwner(String owner);

    @Override
    MachineStatus getStatus();

    @Override
    MachineDto withStatus(MachineStatus machineStatus);

    @Override
    ChannelsDto getChannels();

    @Override
    MachineDto withChannels(ChannelsDto channels);

    @Override
    MachineDto withName(String name);

    @Override
    MachineSourceDto getSource();

    @Override
    MachineDto withSource(MachineSourceDto source);

    @Override
    MachineDto withDev(boolean dev);

    @Override
    LimitsDto getLimits();

    @Override
    MachineDto withLimits(LimitsDto limits);

    @Override
    MachineDto withType(String type);

    @Override
    List<Link> getLinks();

    @Override
    MachineDto withLinks(List<Link> links);
}

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

import org.eclipse.che.api.core.model.machine.MachineState;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface MachineStateDto extends MachineConfigDto, MachineState {
    @Override
    String getId();

    MachineStateDto withId(String id);

    @Override
    String getWorkspaceId();

    MachineStateDto withWorkspaceId(String workspaceId);

    @Override
    String getOwner();

    MachineStateDto withOwner(String owner);

    @Override
    MachineStatus getStatus();

    MachineStateDto withStatus(MachineStatus machineStatus);

    @Override
    ChannelsDto getChannels();

    MachineStateDto withChannels(ChannelsDto channels);

    @Override
    MachineStateDto withName(String name);

    @Override
    MachineSourceDto getSource();

    @Override
    MachineStateDto withSource(MachineSourceDto source);

    @Override
    MachineStateDto withDev(boolean dev);

    @Override
    LimitsDto getLimits();

    @Override
    MachineStateDto withLimits(LimitsDto limits);

    @Override
    MachineStateDto withType(String type);

    List<Link> getLinks();

    MachineStateDto withLinks(List<Link> links);
}

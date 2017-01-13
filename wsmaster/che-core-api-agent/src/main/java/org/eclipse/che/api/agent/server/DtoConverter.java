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
package org.eclipse.che.api.agent.server;

import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Anatolii Bazko
 */
public class DtoConverter {

    public static AgentDto asDto(Agent agent) {
        return newDto(AgentDto.class).withId(agent.getId())
                                     .withName(agent.getName())
                                     .withVersion(agent.getVersion())
                                     .withDescription(agent.getDescription())
                                     .withProperties(agent.getProperties())
                                     .withScript(agent.getScript())
                                     .withDependencies(agent.getDependencies());
    }

    private DtoConverter() { }
}

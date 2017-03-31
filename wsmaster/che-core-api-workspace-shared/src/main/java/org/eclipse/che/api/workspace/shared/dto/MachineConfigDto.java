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

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface MachineConfigDto extends MachineConfig {
    @Override
    @FactoryParameter(obligation = OPTIONAL)
    List<String> getAgents();

    void setAgents(List<String> agents);

    MachineConfigDto withAgents(List<String> agents);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    Map<String, ServerConfigDto> getServers();

    void setServers(Map<String, ServerConfigDto> servers);

    MachineConfigDto withServers(Map<String, ServerConfigDto> servers);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    MachineConfigDto withAttributes(Map<String, String> attributes);
}

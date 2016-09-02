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

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@Deprecated
@DTO
public interface MachineConfigDto extends MachineConfig, Hyperlinks {
    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getName();

    void setName(String name);

    MachineConfigDto withName(String name);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    MachineSourceDto getSource();

    void setSource(MachineSourceDto source);

    MachineConfigDto withSource(MachineSourceDto source);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    boolean isDev();

    void setDev(boolean dev);

    MachineConfigDto withDev(boolean dev);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getType();

    void setType(String type);

    MachineConfigDto withType(String type);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    LimitsDto getLimits();

    void setLimits(LimitsDto limits);

    MachineConfigDto withLimits(LimitsDto limits);

    @Override
    List<ServerConfDto> getServers();

    void setServers(List<ServerConfDto> servers);

    MachineConfigDto withServers(List<ServerConfDto> servers);

    @Override
    Map<String, String> getEnvVariables();

    void setEnvVariables(Map<String, String> envVariables);

    MachineConfigDto withEnvVariables(Map<String, String> envVariables);

    @Override
    MachineConfigDto withLinks(List<Link> links);
}

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
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author andrew00x
 */
@DTO
public interface WorkspaceConfigDto extends WorkspaceConfig, Hyperlinks {

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getName();

    WorkspaceConfigDto withName(String name);

    void setName(String name);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getDefaultEnv();

    void setDefaultEnv(String defaultEnvironment);

    WorkspaceConfigDto withDefaultEnv(String defaultEnvironment);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getDescription();

    void setDescription(String description);

    WorkspaceConfigDto withDescription(String description);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    List<CommandDto> getCommands();

    void setCommands(List<CommandDto> commands);

    WorkspaceConfigDto withCommands(List<CommandDto> commands);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    List<ProjectConfigDto> getProjects();

    void setProjects(List<ProjectConfigDto> projects);

    WorkspaceConfigDto withProjects(List<ProjectConfigDto> projects);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    Map<String, EnvironmentDto> getEnvironments();

    void setEnvironments(Map<String, EnvironmentDto> environments);

    WorkspaceConfigDto withEnvironments(Map<String, EnvironmentDto> environments);

    @Override
    WorkspaceConfigDto withLinks(List<Link> links);
}

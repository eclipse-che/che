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
package org.eclipse.che.api.agent.shared.dto;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@DTO
public interface AgentDto extends Agent {

    @Override
    String getId();

    void setId(String id);

    AgentDto withId(String id);

    @Override
    String getName();

    void setName(String name);

    AgentDto withName(String name);

    @Override
    String getVersion();

    void setVersion(String version);

    AgentDto withVersion(String version);

    @Override
    String getDescription();

    void setDescription(String description);

    AgentDto withDescription(String description);

    @Override
    List<String> getDependencies();

    void setDependencies(List<String> dependencies);

    AgentDto withDependencies(List<String> dependencies);

    @Override
    String getScript();

    void setScript(String script);

    AgentDto withScript(String script);

    @Override
    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    AgentDto withProperties(Map<String, String> properties);

    @Override
    Map<String, ServerConf2Dto> getServers();

    void setServers(Map<String, ServerConf2Dto> servers);

    AgentDto withServers(Map<String, ServerConf2Dto> servers);
}

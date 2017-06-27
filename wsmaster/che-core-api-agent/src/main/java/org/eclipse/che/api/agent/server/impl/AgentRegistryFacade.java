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

package org.eclipse.che.api.agent.server.impl;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.AgentRegistryService;
import org.eclipse.che.api.agent.server.DtoConverter;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.rest.HttpRequestHelper;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Composite registry to use by interesting parties inside "master"
 * It is supposed that it can me configured to use remote registry
 * in this case "registry.agent.remote" property should point to the valid
 * Agent Registry REST service, otherwise LocalAgentregistry will be used
 *
 * @author gazarenkov
 */
@Singleton
// TODO implement and test it properly
public class AgentRegistryFacade implements AgentRegistry {

    private final AgentRegistryService remote;
    private final LocalAgentRegistry   local;

    @Inject
    @Nullable
    @Named("registry.agent.remote") String remoteUrl;

    @Inject
    public AgentRegistryFacade(AgentRegistryService remote, LocalAgentRegistry local) {
        this.remote = remote;
        this.local = local;
    }

    @Override
    public Agent getAgent(AgentKey agentKey) throws AgentException {
        if(isRemote()) {
            try {
                return HttpRequestHelper.createJsonRequest(remoteUrl).useGetMethod().
                        setBody(DtoConverter.asDto(agentKey)).request().asDto(AgentDto.class);
            } catch (Exception e) {
                throw new AgentException(e.getMessage());
            }
        } else {
            return local.getAgent(agentKey);
        }
    }

    @Override
    public List<String> getVersions(String id) throws AgentException {
        //TODO same as getAgent
        return null;
    }

    @Override
    public Collection<Agent> getAgents() throws AgentException {
        ///TODO same as getAgent
        return null;
    }

    @Override
    public List<Agent> getOrderedAgents(List<AgentKey> keys) {
        //TODO same as getAgent
        return null;
    }

    private boolean isRemote() {
        return remoteUrl != null;
    }
}

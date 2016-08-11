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
package org.eclipse.che.api.agent.server.impl;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.shared.dto.AgentgDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.dto.server.DtoFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.IoUtil.downloadFile;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class RemoteAgentRegistryImpl implements AgentRegistry {
    private final RemoteAgentRegistryUrlProvider urlProvider;
    private final HttpJsonRequestFactory         requestFactory;

    @Inject
    public RemoteAgentRegistryImpl(RemoteAgentRegistryUrlProvider urlProvider, HttpJsonRequestFactory requestFactory) {
        this.urlProvider = urlProvider;
        this.requestFactory = requestFactory;
    }

    @Override
    public Agent createAgent(String name, String version) throws AgentException {
        URL url = urlProvider.getAgentUrl(name, version);
        return createAgent(url);
    }

    @Override
    public Agent createAgent(AgentKey agentKey) throws AgentException {
        URL url = agentKey.getVersion() != null ? urlProvider.getAgentUrl(agentKey.getName(), agentKey.getVersion())
                                                : urlProvider.getAgentUrl(agentKey.getName());
        return createAgent(url);
    }

    @Override
    public Agent createAgent(String name) throws AgentException {
        URL url = urlProvider.getAgentUrl(name);
        return createAgent(url);
    }

    @Override
    public Collection<String> getVersions(String name) throws AgentException {
        URL url = urlProvider.getAgentVersions(name);
        try {
            HttpJsonResponse response = requestFactory.fromUrl(url.toString()).useGetMethod().request();

            @SuppressWarnings("unchecked")
            List<String> versions = response.as(List.class, new TypeToken<List<String>>() { }.getType());

            return versions;
        } catch (IOException | ServerException | UnauthorizedException | ConflictException | BadRequestException | ForbiddenException e) {
            throw new AgentException(format("Can't fetch available %s version.", name), e);
        } catch (NotFoundException e) {
            throw new AgentNotFoundException(format("Agent %s not found", name), e);
        }
    }

    private Agent createAgent(URL url) throws AgentException {
        try {
            File agent = downloadFile(new File(System.getProperty("java.io.tmpdir")), "agent", ".tmp", url);
            String json = readAndCloseQuietly(new FileInputStream(agent));
            return DtoFactory.getInstance().createDtoFromJson(json, AgentgDto.class);
        } catch (IOException | IllegalArgumentException e) {
            throw new AgentException("Can't fetch agent configuration", e);
        }
    }
}

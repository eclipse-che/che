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
import org.eclipse.che.api.agent.server.AgentRegistryUrlProvider;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.shared.dto.AgentConfigDto;
import org.eclipse.che.api.agent.shared.model.AgentConfig;
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
    private final AgentRegistryUrlProvider urlProvider;
    private final HttpJsonRequestFactory   requestFactory;

    @Inject
    public RemoteAgentRegistryImpl(AgentRegistryUrlProvider urlProvider, HttpJsonRequestFactory requestFactory) {
        this.urlProvider = urlProvider;
        this.requestFactory = requestFactory;
    }

    @Override
    public AgentConfig getConfig(String fqn, String version) throws AgentException {
        URL url = urlProvider.getAgentUrl(fqn, version);
        return getConfig(url);
    }

    @Override
    public AgentConfig getConfig(String fqn) throws AgentException {
        URL url = urlProvider.getAgentUrl(fqn);
        return getConfig(url);
    }

    @Override
    public Collection<String> getVersions(String fqn) throws AgentException {
        URL url = urlProvider.getAgentVersions(fqn);
        try {
            HttpJsonResponse response = requestFactory.fromUrl(url.toString()).useGetMethod().request();

            @SuppressWarnings("unchecked")
            List<String> versions = response.as(List.class, new TypeToken<List<String>>() { }.getType());

            return versions;
        } catch (IOException | ServerException | UnauthorizedException | ConflictException | BadRequestException | ForbiddenException e) {
            throw new AgentException(format("Can't fetch available %s version.", fqn), e);
        } catch (NotFoundException e) {
            throw new AgentNotFoundException(format("Agent %s not found", fqn), e);
        }
    }

    private AgentConfig getConfig(URL url) throws AgentException {
        try {
            File agentConf = downloadFile(new File(System.getProperty("java.io.tmpdir")), "agent", ".tmp", url);
            String json = readAndCloseQuietly(new FileInputStream(agentConf));
            return DtoFactory.getInstance().createDtoFromJson(json, AgentConfigDto.class);
        } catch (IOException | IllegalArgumentException e) {
            throw new AgentException("Can't fetch agent configuration", e);
        }
    }
}

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
import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.dto.server.DtoFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.commons.lang.IoUtil.downloadFile;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class AgentRegistryImpl implements AgentRegistry {
    private final AgentRegistryUrlProvider urlProvider;
    private final HttpJsonRequestFactory   requestFactory;

    @Inject
    public AgentRegistryImpl(AgentRegistryUrlProvider urlProvider,
                             HttpJsonRequestFactory requestFactory) {
        this.urlProvider = urlProvider;
        this.requestFactory = requestFactory;
    }

    @Override
    public Agent getAgent(AgentKey agentKey) throws AgentException {
        return doGetRemoteAgent(urlProvider.getAgentUrl(agentKey));
    }

    @Override
    public List<String> getVersions(String name) throws AgentException {
        URL url = urlProvider.getAgentVersionsUrl(name);
        try {
            HttpJsonResponse response = requestFactory.fromUrl(url.toString()).useGetMethod().request();

            @SuppressWarnings("unchecked")
            List<String> versions = response.as(List.class, new TypeToken<List<String>>() { }.getType());

            return versions;
        } catch (NotFoundException e) {
            throw new AgentNotFoundException(format("Agent %s not found", name), e);
        } catch (IOException | ApiException e) {
            throw new AgentException(format("Can't fetch available %s version.", name), e);
        }
    }

    @Override
    public List<String> getAgents() throws AgentException {
        return asList("org.eclipse.che.terminal",
                      "org.eclipse.che.ws-agent",
                      "org.eclipse.che.ssh");
    }

    protected Agent doGetRemoteAgent(URL url) throws AgentException {
        File agent = null;
        try {
            agent = downloadFile(new File(System.getProperty("java.io.tmpdir")), "agent", ".tmp", url);
            String json = readAndCloseQuietly(new FileInputStream(agent));
            return DtoFactory.getInstance().createDtoFromJson(json, AgentDto.class);
        } catch (IOException | IllegalArgumentException e) {
            throw new AgentException("Can't fetch agent configuration", e);
        } finally {
            if (agent != null) {
                try {
                    Files.delete(agent.toPath());
                } catch (IOException e) {
                    FileCleaner.addFile(agent);
                }
            }
        }
    }
}

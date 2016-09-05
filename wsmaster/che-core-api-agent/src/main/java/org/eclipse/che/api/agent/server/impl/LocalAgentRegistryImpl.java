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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;
import static org.eclipse.che.commons.lang.IoUtil.downloadFile;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * Local implementation of the {@link AgentRegistry}.
 * The name of the agent might represent a url.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class LocalAgentRegistryImpl implements AgentRegistry {

    protected static final Logger  LOG    = LoggerFactory.getLogger(LocalAgentRegistryImpl.class);
    private static final   Pattern AGENTS = Pattern.compile(".*[//]?agents/[^//]+[.]json");

    private final Map<String, Agent>     agents;

    @Inject
    public LocalAgentRegistryImpl() throws IOException {
        this.agents = new HashMap<>();

        IoUtil.getResources(this.getClass(), AGENTS, inputStream -> {
            final Agent agent;
            try {
                agent = DtoFactory.getInstance().createDtoFromJson(inputStream, AgentDto.class);
                agents.put(agent.getName(), agent);
            } catch (IOException e) {
                LOG.error("Can't create agent.", e);
            }
        });
    }

    @Override
    public Agent createAgent(String name, String version) throws AgentException {
        return doCreateAgent(name);
    }

    @Override
    public Agent createAgent(AgentKey agentKey) throws AgentException {
        return doCreateAgent(agentKey.getName());
    }

    @Override
    public Agent createAgent(String name) throws AgentException {
        return doCreateAgent(name);
    }

    @Override
    public Collection<String> getVersions(String name) throws AgentException {
        Agent agent = doCreateAgent(name);
        return singleton(agent.getVersion());
    }

    @Override
    public Set<String> getAgents() throws AgentException {
        return unmodifiableSet(agents.keySet());
    }

    protected Agent doCreateAgent(String name) throws AgentException {
        try {
            URL url = new URL(name);
            return doCreateRemoteAgent(url);
        } catch (MalformedURLException ignored) {
            // name doesn't represent a url
        }

        Optional<Agent> agent = Optional.ofNullable(agents.get(name));
        return agent.orElseThrow(() -> new AgentNotFoundException(format("Agent %s not found", name)));
    }

    protected Agent doCreateRemoteAgent(URL url) throws AgentException {
        try {
            File agent = downloadFile(new File(System.getProperty("java.io.tmpdir")), "agent", ".tmp", url);
            String json = readAndCloseQuietly(new FileInputStream(agent));
            return DtoFactory.getInstance().createDtoFromJson(json, AgentDto.class);
        } catch (IOException | IllegalArgumentException e) {
            throw new AgentException("Can't fetch agent configuration", e);
        }
    }
}

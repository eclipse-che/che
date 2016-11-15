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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.eclipse.che.commons.lang.IoUtil.downloadFile;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;
import static org.eclipse.che.commons.lang.ZipUtils.isZipFile;

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

    private final Map<String, Agent> agents;
    private final List<String>       agentIds;

    @Inject
    public LocalAgentRegistryImpl(Set<Agent> agents) throws IOException {

        this.agents = new HashMap<>();
        findAgents();
        for (Agent agent : agents) {
            this.agents.put(agent.getId(), agent);
        }
        this.agentIds = ImmutableList.copyOf(this.agents.keySet());
    }

    @Override
    public Agent getAgent(AgentKey agentKey) throws AgentException {
        return doGetAgent(agentKey.getId());
    }

    @Override
    public List<String> getVersions(String id) throws AgentException {
        Agent agent = doGetAgent(id);
        String version = agent.getVersion();
        return version == null ? Collections.emptyList() : singletonList(version);
    }

    @Override
    public Collection<Agent> getAgents() throws AgentException {
        return agents.values();
    }

    protected Agent doGetAgent(String id) throws AgentException {
        try {
            URL url = new URL(id);
            return doGetRemoteAgent(url);
        } catch (MalformedURLException ignored) {
            // id doesn't represent a url
        }

        Optional<Agent> agent = Optional.ofNullable(agents.get(id));
        return agent.orElseThrow(() -> new AgentNotFoundException(format("Agent %s not found", id)));
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

    protected void findAgents() throws IOException {
        File context = new File(LocalAgentRegistryImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        if (isZipFile(context)) {
            try (ZipFile zip = new ZipFile(context)) {
                ZipUtils.getResources(zip, AGENTS, createAgentsConsumer);
            }
        } else {
            Files.walk(context.toPath())
                 .filter(path -> AGENTS.matcher(path.toString()).matches())
                 .forEach(path -> {
                     try (InputStream in = Files.newInputStream(path)) {
                         createAgentsConsumer.accept(in);
                     } catch (IOException ignored) {
                         // ignore
                     }
                 });
        }
    }

    private Consumer<InputStream> createAgentsConsumer = new Consumer<InputStream>() {
        @Override
        public void accept(InputStream inputStream) {
            try {
                final Agent agent = DtoFactory.getInstance().createDtoFromJson(inputStream, AgentDto.class);
                agents.put(agent.getId(), agent);
            } catch (IOException e) {
                LOG.error("Can't create agent.", e);
            }
        }
    };
}

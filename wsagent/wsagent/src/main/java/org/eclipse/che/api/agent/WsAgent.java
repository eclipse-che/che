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
package org.eclipse.che.api.agent;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.model.workspace.ServerConf2;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * Workspace agent.
 *
 * @see Agent
 *
 * @author Anatolii Bazko
 */
@Singleton
public class WsAgent implements Agent {
    private static final String AGENT_DESCRIPTOR = "org.eclipse.che.ws-agent.json";
    private static final String AGENT_SCRIPT     = "org.eclipse.che.ws-agent.script.sh";

    private final Agent internal;

    @Inject
    public WsAgent() throws IOException {
        internal = readAgentDescriptor();
    }

    @Override
    public String getId() {
        return internal.getId();
    }

    @Override
    public String getName() {
        return internal.getName();
    }

    @Override
    public String getVersion() {
        return internal.getVersion();
    }

    @Override
    public String getDescription() {
        return internal.getDescription();
    }

    @Override
    public List<String> getDependencies() {
        return unmodifiableList(internal.getDependencies());
    }

    @Override
    public String getScript() {
        return internal.getScript();
    }

    @Override
    public Map<String, String> getProperties() {
        return unmodifiableMap(internal.getProperties());
    }

    @Override
    public Map<String, ? extends ServerConf2> getServers() {
        return unmodifiableMap(internal.getServers());
    }

    private Agent readAgentDescriptor() throws IOException {
        InputStream inputStream = readResource(AGENT_DESCRIPTOR);
        AgentDto agent = DtoFactory.getInstance().createDtoFromJson(inputStream, AgentDto.class);
        return agent.withScript(readAgentScript());
    }

    private String readAgentScript() throws IOException {
        InputStream inputStream = readResource(AGENT_SCRIPT);
        return IoUtil.readStream(inputStream);
    }

    private InputStream readResource(String name) throws IOException {
        InputStream inputStream = WsAgent.class.getResourceAsStream("/" + name);
        if (inputStream == null) {
            throw new IOException(format("Can't initialize workspace agent. Resource %s not found", name));
        }
        return inputStream;
    }
}

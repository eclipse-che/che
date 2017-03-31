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
package org.eclipse.che.api.agent.shared.model.impl;

import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
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
 * Basic implementation of the {@link Agent}.
 *
 * It is supposed that agent descriptor and agent script are located
 * as resources in the jar.
 *
 * If resources aren't found then {@link Agent} won't be initialized.
 *
 * @author Anatolii Bazko
 */
public abstract class BasicAgent implements Agent {
    private final Agent internal;

    public BasicAgent(String agentDescriptor, String agentScript) throws IOException {
        internal = readAgentDescriptor(agentDescriptor, agentScript);
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
    public Map<String, ? extends ServerConfig> getServers() {
        return unmodifiableMap(internal.getServers());
    }

    private Agent readAgentDescriptor(String agentDescriptor, String agentScript) throws IOException {
        InputStream inputStream = readResource(agentDescriptor);
        AgentDto agent = DtoFactory.getInstance().createDtoFromJson(inputStream, AgentDto.class);
        return agent.withScript(readAgentScript(agentScript));
    }

    private String readAgentScript(String agentScript) throws IOException {
        InputStream inputStream = readResource(agentScript);
        return IoUtil.readStream(inputStream);
    }

    private InputStream readResource(String resource) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/" + resource);
        if (inputStream == null) {
            throw new IOException(format("Can't initialize agent. Resource %s not found", resource));
        }
        return inputStream;
    }

}

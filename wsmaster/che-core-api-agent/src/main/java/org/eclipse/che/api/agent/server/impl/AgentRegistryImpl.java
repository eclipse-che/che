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

import org.eclipse.che.api.agent.server.AgentException;
import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.shared.dto.AgentConfigDto;
import org.eclipse.che.api.agent.shared.model.AgentConfig;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.eclipse.che.commons.lang.IoUtil.downloadFile;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class AgentRegistryImpl implements AgentRegistry {

    public static final String FQN_TEMPLATE     = "${fqn}";
    public static final String VERSION_TEMPLATE = "${version}";

    private final String agentUrl;
    private final String latestAgentUrl;

    @Inject
    public AgentRegistryImpl(@Named("che.agent.url") String agentUrl,
                             @Named("che.agent.latest.url") String latestAgentUrl) {

        this.agentUrl = agentUrl.endsWith("/") ? agentUrl
                                               : agentUrl.concat("/");
        this.latestAgentUrl = latestAgentUrl.endsWith("/") ? latestAgentUrl
                                                           : latestAgentUrl.concat("/");
    }

    @Override
    public AgentConfig getConfig(String fqn, String version) throws AgentException {
        String url = agentUrl.replace(FQN_TEMPLATE, fqn).replace(VERSION_TEMPLATE, version);
        try {
            return getConfig(new URL(url));
        } catch (MalformedURLException e) {
            throw new AgentException("Malformed url " + url, e);
        }
    }

    @Override
    public AgentConfig getConfig(String fqn) throws AgentException {
        String url = latestAgentUrl.replace(FQN_TEMPLATE, fqn);
        try {
            return getConfig(new URL(url));
        } catch (MalformedURLException e) {
            throw new AgentException("Malformed url " + url, e);
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

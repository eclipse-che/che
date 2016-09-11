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

import org.eclipse.che.api.agent.server.AgentRegistryUrlProvider;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.shared.model.AgentKey;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides urls to agents.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class AgentRegistryUrlProviderImpl implements AgentRegistryUrlProvider {
    public static final String VERSION = "${version}";
    public static final String NAME    = "${name}";

    private final String agentLatestVersionUrl;
    private final String agentSpecificVersionUrl;
    private final String agentVersionsUrl;

    @Inject
    public AgentRegistryUrlProviderImpl(@Named("machine.agent.latest_version_url") String agentLatestVersionUrl,
                                        @Named("machine.agent.specific_version_url") String agentSpecificVersionUrl,
                                        @Named("machine.agent.all_versions_url") String agentVersionsUrl) {
        this.agentLatestVersionUrl = agentLatestVersionUrl;
        this.agentSpecificVersionUrl = agentSpecificVersionUrl;
        this.agentVersionsUrl = agentVersionsUrl;
    }

    @Override
    public URL getAgentUrl(AgentKey agentKey) throws AgentException {
        String url;
        if (agentKey.getVersion() == null) {
            url = agentLatestVersionUrl.replace(NAME, agentKey.getName());
        } else {
            url = agentSpecificVersionUrl.replace(NAME, agentKey.getName()).replace(VERSION, agentKey.getVersion());
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AgentException("Malformed url " + url, e);
        }
    }


    @Override
    public URL getAgentVersionsUrl(String name) throws AgentException {
        String url = agentVersionsUrl.replace(NAME, name);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AgentException("Malformed url " + url, e);
        }
    }
}

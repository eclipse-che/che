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

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.AgentRegistryUrlProvider;

import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;

/**
 * Provides urls to Codenvy update server.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class RemoteAgentRegistryUrlProviderImpl implements AgentRegistryUrlProvider {

    public static final String BASE_URL = "https://codenvy.com/update/repository/";

    @Inject
    public RemoteAgentRegistryUrlProviderImpl() { }

    @Override
    public URL getAgentUrl(String fqn, String version) throws AgentException {
        String url = format(BASE_URL + "public/download/%s/%s", fqn, version);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AgentException("Malformed url " + url, e);
        }
    }

    @Override
    public URL getAgentUrl(String fqn) throws AgentException {
        String url = format(BASE_URL + "public/download/%s", fqn);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AgentException("Malformed url " + url, e);
        }
    }

    @Override
    public URL getAgentVersions(String fqn) throws AgentException {
        String url = format(BASE_URL + "/updates/%s", fqn);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AgentException("Malformed url " + url, e);
        }
    }
}

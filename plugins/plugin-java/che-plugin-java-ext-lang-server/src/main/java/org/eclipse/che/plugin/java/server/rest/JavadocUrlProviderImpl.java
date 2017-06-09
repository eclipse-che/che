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
package org.eclipse.che.plugin.java.server.rest;

import org.eclipse.che.JavadocUrlProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;

/**
 * Default implementation of the {@link JavadocUrlProvider}.
 * Use <code>wsagent.endpoint</code> constant and {@link UriBuilder} to build URL to {@link JavadocService#get(String, String)} service
 */
@Singleton
public class JavadocUrlProviderImpl implements JavadocUrlProvider {


    private final String agentEndpoint;

    @Inject
    public JavadocUrlProviderImpl(@Named("wsagent.endpoint") String agentEndpoint) {
        this.agentEndpoint = agentEndpoint;
    }

    @Override
    public String getJavadocUrl(String projectPath) {
        return UriBuilder.fromUri(agentEndpoint)
                .queryParam("projectpath", projectPath)
                .path(JavadocService.class)
                .path(JavadocService.class, "get")
                .build()
                .toString() + "&handle=";
    }
}

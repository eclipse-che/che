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
package org.eclipse.che.plugin.docker.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DockerConnectorProvider implements Provider<DockerConnector> {

    private static final Logger LOG  = LoggerFactory.getLogger(DockerConnectorProvider.class);
    private DockerConnector connector;

    @Inject
    public DockerConnectorProvider(Map<String, DockerConnector> connectors,
                                   @Named("che.docker_connector.provider") String property) {
        if (connectors.containsKey(property)) {
            this.connector = connectors.get(property);
        } else {
            LOG.warn("Property 'che.docker_connector.provider' did not match any bound "
                    + "implementation of DockerConnector. Using default.");
            LOG.warn("\t Value of che.docker_connector.provider: " + property);
            LOG.warn("\t Bound implementations: " + connectors.toString());
            this.connector = connectors.get("default");
        }
    }

    @Override
    public DockerConnector get() {
        return connector;
    }
}

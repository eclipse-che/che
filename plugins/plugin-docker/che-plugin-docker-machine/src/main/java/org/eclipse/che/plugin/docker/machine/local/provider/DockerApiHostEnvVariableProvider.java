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
package org.eclipse.che.plugin.docker.machine.local.provider;

import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.URI;

/**
 * Provides DOCKER_HOST env variable for the sake of access to docker API within docker container
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DockerApiHostEnvVariableProvider implements Provider<String> {
    @Inject
    private DockerConnectorConfiguration dockerConnectorConfiguration;

    @Override
    public String get() {
        final URI dockerDaemonUri = dockerConnectorConfiguration.getDockerDaemonUri();
        if ("http".equals(dockerDaemonUri.getScheme())) {
            return DockerConnectorConfiguration.DOCKER_HOST_PROPERTY
                   + "=tcp://"
                   + dockerConnectorConfiguration.getDockerHost()
                   + ':'
                   + dockerDaemonUri.getPort();
        }
        return "";
    }
}

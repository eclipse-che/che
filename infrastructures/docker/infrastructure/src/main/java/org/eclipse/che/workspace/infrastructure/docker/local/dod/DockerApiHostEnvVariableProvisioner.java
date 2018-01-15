/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.local.dod;

import static java.lang.String.format;

import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.infrastructure.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

/**
 * Provides DOCKER_HOST env variable for the sake of access to docker API within docker container
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DockerApiHostEnvVariableProvisioner implements ConfigurationProvisioner {
  private final String value;

  @Inject
  public DockerApiHostEnvVariableProvisioner(
      DockerConnectorConfiguration dockerConnectorConfiguration) {
    URI dockerDaemonUri = dockerConnectorConfiguration.getDockerDaemonUri();
    if ("http".equals(dockerDaemonUri.getScheme())) {
      value =
          format(
              "tcp://%s:%s",
              dockerConnectorConfiguration.getDockerHost(), dockerDaemonUri.getPort());
    } else {
      value = null;
    }
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    if (value != null) {
      for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
        containerConfig
            .getEnvironment()
            .put(DockerConnectorConfiguration.DOCKER_HOST_PROPERTY, value);
      }
    }
  }
}

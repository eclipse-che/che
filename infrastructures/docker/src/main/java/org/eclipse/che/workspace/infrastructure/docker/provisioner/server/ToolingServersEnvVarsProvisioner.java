/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner.server;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

/**
 * Adds environment variables needed for servers used by Che for providing IDE features such as FS
 * access, terminal, command execution, etc.
 *
 * @author Alexander Garagatyi
 */
public class ToolingServersEnvVarsProvisioner implements ConfigurationProvisioner {
  private final Set<ServerEnvironmentVariableProvider> providers;

  @Inject
  public ToolingServersEnvVarsProvisioner(Set<ServerEnvironmentVariableProvider> providers) {
    this.providers = providers;
  }

  @Override
  public void provision(
      EnvironmentImpl envConfig, DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    Map<String, String> envVars =
        providers
            .stream()
            .map(p -> p.get(identity))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(e -> e.first, e -> e.second));

    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      containerConfig.getEnvironment().putAll(envVars);
    }
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.provision.env;

import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds environment variables needed for servers used by Che.
 *
 * @author Alexander Garagatyi
 * @author Sergii Leshchenko
 */
public class EnvVarEnvironmentProvisioner implements InternalEnvironmentProvisioner {

  private static final Logger LOG = LoggerFactory.getLogger(EnvVarEnvironmentProvisioner.class);

  private final Set<EnvVarProvider> envVarProviders;

  @Inject
  public EnvVarEnvironmentProvisioner(Set<EnvVarProvider> envVarProviders) {
    this.envVarProviders = envVarProviders;
  }

  @Override
  public void provision(RuntimeIdentity id, InternalEnvironment internalEnvironment)
      throws InfrastructureException {
    LOG.debug("Provisioning environment variables for workspace '{}'", id.getWorkspaceId());
    for (EnvVarProvider envVarProvider : envVarProviders) {
      Pair<String, String> envVar = envVarProvider.get(id);
      internalEnvironment
          .getMachines()
          .values()
          .forEach(m -> m.getEnv().putIfAbsent(envVar.first, envVar.second));
    }
    LOG.debug("Environment variables provisioning done for workspace '{}'", id.getWorkspaceId());
  }
}

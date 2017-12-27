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
package org.eclipse.che.api.workspace.server.spi.provision.env;

import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.commons.lang.Pair;

/**
 * Adds environment variables needed for servers used by Che.
 *
 * @author Alexander Garagatyi
 * @author Sergii Leshchenko
 */
public class EnvVarEnvironmentProvisioner implements InternalEnvironmentProvisioner {
  private final Set<EnvVarProvider> envVarProviders;

  @Inject
  public EnvVarEnvironmentProvisioner(Set<EnvVarProvider> envVarProviders) {
    this.envVarProviders = envVarProviders;
  }

  @Override
  public void provision(RuntimeIdentity id, InternalEnvironment internalEnvironment)
      throws InfrastructureException {
    for (EnvVarProvider envVarProvider : envVarProviders) {
      Pair<String, String> envVar = envVarProvider.get(id);
      internalEnvironment
          .getMachines()
          .values()
          .forEach(m -> m.getEnv().putIfAbsent(envVar.first, envVar.second));
    }
  }
}

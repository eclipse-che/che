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
package org.eclipse.che.api.workspace.server.spi.provision;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies OpenShift specific properties of the installers to {@link InternalMachineConfig}.
 *
 * <p>This class must be called before environment is started, otherwise changing configuration has
 * no effect.
 *
 * <p>This class performs following changes to environment: <br>
 * - adds environment variables which are specified in properties of installer configuration. The
 * environment property contains environment variables in the following format:
 * "env1=value1,env2=value2,..."; <br>
 * - add servers that is declared by configure installers.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class InstallerConfigProvisioner implements InternalEnvironmentProvisioner {

  private static final Logger LOG = LoggerFactory.getLogger(RuntimeInfrastructure.class);

  @Override
  public void provision(RuntimeIdentity id, InternalEnvironment internalEnvironment)
      throws InfrastructureException {
    for (InternalMachineConfig machineConfig : internalEnvironment.getMachines().values()) {
      fillEnv(machineConfig.getEnv(), machineConfig.getInstallers());
      fillServers(machineConfig.getServers(), machineConfig.getInstallers());
    }
  }

  /**
   * Fill the provided map with environment variables that are provided by installers.
   *
   * @param env map to fill
   * @param installers installers to retrieve env
   */
  private void fillEnv(Map<String, String> env, List<Installer> installers) {
    for (Installer installer : installers) {
      String envVars = installer.getProperties().get(Installer.ENVIRONMENT_PROPERTY);
      if (isNullOrEmpty(envVars)) {
        return;
      }

      for (String envVar : envVars.split(",")) {
        String[] items = envVar.split("=");
        if (items.length != 2) {
          LOG.warn(format("Illegal environment variable '%s' format", envVar));
          continue;
        }
        String name = items[0];
        String value = items[1];

        env.put(name, value);
      }
    }
  }

  /**
   * Fill the provided map with servers that are provided by installers.
   *
   * @param servers map to fill
   * @param installers installers to retrieve servers
   * @throws InfrastructureException if any installer has server that conflicts with already
   *     configured one
   */
  private void fillServers(Map<String, ServerConfig> servers, List<Installer> installers)
      throws InfrastructureException {
    for (Installer installer : installers) {
      for (Map.Entry<String, ? extends ServerConfig> serverEntry :
          installer.getServers().entrySet()) {
        if (servers.putIfAbsent(serverEntry.getKey(), serverEntry.getValue()) != null
            && !servers.get(serverEntry.getKey()).equals(serverEntry.getValue())) {
          throw new InfrastructureException(
              format(
                  "Installer '%s' contains server '%s' conflicting with machine configuration",
                  installer.getId(), serverEntry.getKey()));
        }
      }
    }
  }
}

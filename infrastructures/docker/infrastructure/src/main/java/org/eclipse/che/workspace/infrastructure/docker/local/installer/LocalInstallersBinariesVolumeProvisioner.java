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
package org.eclipse.che.workspace.infrastructure.docker.local.installer;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

/**
 * Provisions an environment with binaries that comes from installers of machines in the
 * environment.
 *
 * @author Alexander Garagatyi
 */
public class LocalInstallersBinariesVolumeProvisioner implements ConfigurationProvisioner {
  public static final String LOCAL_INSTALLERS_PROVISIONERS =
      "infrastructure.docker.local_installers_provisioners";

  private final Set<ConfigurationProvisioner> localInstallerProvisioners;

  @Inject
  public LocalInstallersBinariesVolumeProvisioner(
      @Named(LOCAL_INSTALLERS_PROVISIONERS)
          Set<ConfigurationProvisioner> localInstallerProvisioners) {
    this.localInstallerProvisioners = localInstallerProvisioners;
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (ConfigurationProvisioner infrastructureProvisioner : localInstallerProvisioners) {
      infrastructureProvisioner.provision(internalEnv, identity);
    }
  }
}

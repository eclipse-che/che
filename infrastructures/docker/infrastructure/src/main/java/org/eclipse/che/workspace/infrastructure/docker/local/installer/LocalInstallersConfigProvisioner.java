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
package org.eclipse.che.workspace.infrastructure.docker.local.installer;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.installer.InstallerConfigApplier;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.installer.InstallersConfigProvisioner;

/**
 * Provisions an environment with configuration and binaries that comes from installers of machines
 * in the environment.
 *
 * @author Alexander Garagatyi
 */
public class LocalInstallersConfigProvisioner extends InstallersConfigProvisioner {
  public static final String LOCAL_INSTALLERS_PROVISIONERS =
      "infrastructure.docker.local_installers_provisioners";

  private final Set<ConfigurationProvisioner> localInstallerProvisioners;

  @Inject
  public LocalInstallersConfigProvisioner(
      InstallerConfigApplier installerConfigApplier,
      @Named(LOCAL_INSTALLERS_PROVISIONERS)
          Set<ConfigurationProvisioner> localInstallerProvisioners) {
    super(installerConfigApplier);
    this.localInstallerProvisioners = localInstallerProvisioners;
  }

  @Override
  public void provision(
      InternalEnvironment envConfig, DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    super.provision(envConfig, internalEnv, identity);
    for (ConfigurationProvisioner infrastructureProvisioner : localInstallerProvisioners) {
      infrastructureProvisioner.provision(envConfig, internalEnv, identity);
    }
  }
}

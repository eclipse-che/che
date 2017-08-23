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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.installer;

import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

/**
 * Provisions an environment with configuration that comes from installers of machines in the
 * environment.
 *
 * @author Alexander Garagatyi
 */
public class InstallersConfigProvisioner implements ConfigurationProvisioner {
  private final InstallerConfigApplier installerConfigApplier;

  @Inject
  public InstallersConfigProvisioner(InstallerConfigApplier installerConfigApplier) {
    this.installerConfigApplier = installerConfigApplier;
  }

  @Override
  public void provision(
      InternalEnvironment envConfig, DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    try {
      installerConfigApplier.apply(envConfig, internalEnv);
    } catch (InstallerException e) {
      throw new InfrastructureException(e.getLocalizedMessage(), e);
    }
  }
}

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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.securityopt;

import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Adds securityOpt configuration to {@link DockerContainerConfig}.
 *
 * @author Hanno Kolvenbach
 */
public class SecurityOptProvisioner implements ContainerSystemSettingsProvisioner {
  private SecurityOptProvider securityOptProvider;

  @Inject
  public SecurityOptProvisioner(SecurityOptProvider securityOptProvider) {
    this.securityOptProvider = securityOptProvider;
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      containerConfig.getSecurityOpt().addAll(securityOptProvider.get());
    }
  }
}

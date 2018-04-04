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

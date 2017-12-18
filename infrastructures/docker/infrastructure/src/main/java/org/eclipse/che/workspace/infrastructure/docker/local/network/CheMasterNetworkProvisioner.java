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
package org.eclipse.che.workspace.infrastructure.docker.local.network;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Adds Che master network to each container to allow communication of workspace containers with the
 * master.
 *
 * @author Alexander Garagatyi
 */
public class CheMasterNetworkProvisioner implements ContainerSystemSettingsProvisioner {
  private String cheMasterNetwork;

  @Inject
  public CheMasterNetworkProvisioner(
      @Nullable @Named("che.docker.network") String cheMasterNetwork) {
    this.cheMasterNetwork = cheMasterNetwork;
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    if (cheMasterNetwork != null) {
      for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
        containerConfig.getNetworks().add(cheMasterNetwork);
      }
    }
  }
}

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
package org.eclipse.che.workspace.infrastructure.docker.local.network;

import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.CHE_HOST;

import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.infrastructure.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Adds Che master hosts entry to each container to allow communication of workspace containers with
 * the master.
 *
 * @author Alexander Garagatyi
 */
public class CheMasterExtraHostProvisioner implements ContainerSystemSettingsProvisioner {
  private String cheMasterExtraHostsEntry;

  @Inject
  public CheMasterExtraHostProvisioner(DockerConnectorConfiguration dockerConnectorConfiguration) {
    // add Che server to hosts list
    String cheHost = dockerConnectorConfiguration.getDockerHostIp();
    cheMasterExtraHostsEntry = CHE_HOST.concat(":").concat(cheHost);
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      containerConfig.getExtraHosts().add(cheMasterExtraHostsEntry);
    }
  }
}

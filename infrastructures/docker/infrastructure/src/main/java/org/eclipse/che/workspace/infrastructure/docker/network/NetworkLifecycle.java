/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.network;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.exception.NetworkNotFoundException;
import org.eclipse.che.infrastructure.docker.client.json.network.NewNetwork;
import org.eclipse.che.infrastructure.docker.client.params.network.CreateNetworkParams;
import org.eclipse.che.infrastructure.docker.client.params.network.RemoveNetworkParams;

/** @author Alexander Garagatyi */
public class NetworkLifecycle {

  private final DockerConnector docker;
  private final String networkDriver;

  @Inject
  public NetworkLifecycle(
      DockerConnector docker, @Nullable @Named("che.docker.network_driver") String networkDriver) {
    this.docker = docker;
    this.networkDriver = networkDriver;
  }

  public void createNetwork(String networkName) throws InternalInfrastructureException {
    try {
      docker.createNetwork(
          CreateNetworkParams.create(
              new NewNetwork()
                  .withName(networkName)
                  .withDriver(networkDriver)
                  .withCheckDuplicate(true)));
    } catch (IOException e) {
      throw new InternalInfrastructureException(e.getLocalizedMessage(), e);
    }
  }

  public void destroyNetwork(String networkName) throws InternalInfrastructureException {
    try {
      docker.removeNetwork(RemoveNetworkParams.create(networkName));
    } catch (NetworkNotFoundException ignore) {
    } catch (IOException e) {
      throw new InternalInfrastructureException(e.getLocalizedMessage(), e);
    }
  }
}

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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.limits.swap;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Sets SWAP memory limit in accordance with Che configuration and container RAM settings.
 *
 * @author Alexander Garagatyi
 */
public class SwapLimitProvisioner implements ContainerSystemSettingsProvisioner {
  private final double memorySwapMultiplier;

  @Inject
  public SwapLimitProvisioner(@Named("che.docker.swap") double memorySwapMultiplier) {
    // use-cases:
    //  -1  enable unlimited swap
    //  0   disable swap
    //  0.5 enable swap with size equal to half of current memory size
    //  1   enable swap with size equal to current memory size
    //
    //  according to docker docs field  memorySwap should be equal to memory+swap
    //  we calculate this field as memorySwap=memory * (1 + multiplier) so we just add 1 to
    // multiplier
    this.memorySwapMultiplier = memorySwapMultiplier == -1 ? -1 : memorySwapMultiplier + 1;
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      long machineMemorySwap =
          memorySwapMultiplier == -1
              ? -1
              : (long) (containerConfig.getMemLimit() * memorySwapMultiplier);
      containerConfig.setMemSwapLimit(machineMemorySwap);
    }
  }
}

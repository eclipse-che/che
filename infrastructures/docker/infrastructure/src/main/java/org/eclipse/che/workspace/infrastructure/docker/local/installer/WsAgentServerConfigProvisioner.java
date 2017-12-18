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

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.workspace.infrastructure.docker.local.server.DockerExtConfBindingProvider;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

/**
 * Provides volumes configuration of machine for wsagent server.
 *
 * <p>On Windows MUST be locate in "user.home" directory in case limitation windows+docker.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WsAgentServerConfigProvisioner implements ConfigurationProvisioner {
  private final String extConfBinding;

  @Inject
  public WsAgentServerConfigProvisioner(DockerExtConfBindingProvider dockerExtConfBindingProvider) {
    this.extConfBinding = dockerExtConfBindingProvider.get();
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    if (extConfBinding == null) {
      // nothing to bind
      return;
    }
    Optional<String> devMachineOptional =
        WsAgentMachineFinderUtil.getWsAgentServerMachine(internalEnv);
    if (!devMachineOptional.isPresent()) {
      // no wsagent server found - do nothing
      return;
    }
    String devMachineName = devMachineOptional.get();
    DockerContainerConfig containerConfig = internalEnv.getContainers().get(devMachineName);
    containerConfig.getVolumes().add(extConfBinding);
    containerConfig
        .getEnvironment()
        .put(CheBootstrap.CHE_LOCAL_CONF_DIR, DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
  }
}

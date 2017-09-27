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
package org.eclipse.che.workspace.infrastructure.openshift.provision.installer;

import io.fabric8.kubernetes.api.model.Container;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.machine.authentication.server.MachineTokenRegistry;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * //TODO Fix java doc
 *
 * @author Sergii Leshchenko
 */
public class MultiuserInstallerConfigProvisioner extends InstallerConfigProvisioner {

  private final MachineTokenRegistry tokenRegistry;

  @Inject
  public MultiuserInstallerConfigProvisioner(
      @Named("che.api") String cheServerEndpoint, MachineTokenRegistry tokenRegistry) {
    super(cheServerEndpoint);
    this.tokenRegistry = tokenRegistry;
  }

  @Override
  protected void doProvisionContainer(
      OpenShiftEnvironment osEnv,
      Container container,
      RuntimeIdentity identity,
      String machineName,
      InternalMachineConfig machineConf) {
    super.doProvisionContainer(osEnv, container, identity, machineName, machineConf);

    String currentUserId = EnvironmentContext.getCurrent().getSubject().getUserId();
    String machineToken = tokenRegistry.generateToken(currentUserId, identity.getWorkspaceId());
    putEnv(container.getEnv(), "USER_TOKEN", machineToken);
  }
}

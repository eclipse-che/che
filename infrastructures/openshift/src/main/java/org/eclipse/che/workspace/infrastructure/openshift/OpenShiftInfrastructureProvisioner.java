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
package org.eclipse.che.workspace.infrastructure.openshift;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.installer.InstallerConfigProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.volume.PersistentVolumeClaimProvisioner;

/**
 * Applies the set of configurations to the OpenShift environment and environment configuration with
 * the desired order, which corresponds to the needs of the OpenShift infrastructure.
 *
 * @author Anton Korneta
 */
@Singleton
public class OpenShiftInfrastructureProvisioner {

  private final InstallerConfigProvisioner installerConfigProvisioner;
  private final PersistentVolumeClaimProvisioner persistentVolumeClaimProvisioner;

  @Inject
  public OpenShiftInfrastructureProvisioner(
      InstallerConfigProvisioner installerConfigProvisioner,
      PersistentVolumeClaimProvisioner projectVolumeProvisioner) {
    this.installerConfigProvisioner = installerConfigProvisioner;
    this.persistentVolumeClaimProvisioner = projectVolumeProvisioner;
  }

  public void provision(
      InternalEnvironment environment, OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    installerConfigProvisioner.provision(environment, osEnv, identity);
    persistentVolumeClaimProvisioner.provision(environment, osEnv, identity);
  }
}

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
package org.eclipse.che.workspace.infrastructure.openshift.provision.volume;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.WorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftInternalEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;

/**
 * Provides persistent volume claim into OpenShift environment.
 *
 * @author Anton Korneta
 */
@Singleton
public class PersistentVolumeClaimProvisioner implements ConfigurationProvisioner {

  private final boolean pvcEnable;
  private final WorkspacePVCStrategy pvcStrategy;

  @Inject
  public PersistentVolumeClaimProvisioner(
      @Named("che.infra.openshift.pvc.enabled") boolean pvcEnable,
      WorkspacePVCStrategy pvcStrategy) {
    this.pvcEnable = pvcEnable;
    this.pvcStrategy = pvcStrategy;
  }

  @Override
  public void provision(OpenShiftInternalEnvironment osEnv, RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {
    if (pvcEnable) {
      pvcStrategy.prepare(osEnv, runtimeIdentity.getWorkspaceId());
    }
  }
}

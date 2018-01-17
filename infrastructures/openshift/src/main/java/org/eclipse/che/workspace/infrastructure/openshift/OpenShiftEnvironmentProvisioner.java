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
package org.eclipse.che.workspace.infrastructure.openshift;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.limits.ram.RamLimitProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.route.TlsRouteProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.server.ServersConverter;

/**
 * Applies the set of configurations to the OpenShift environment and environment configuration with
 * the desired order, which corresponds to the needs of the OpenShift infrastructure.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
@Singleton
public class OpenShiftEnvironmentProvisioner {

  private final boolean pvcEnabled;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final UniqueNamesProvisioner uniqueNamesProvisioner;
  private final TlsRouteProvisioner tlsRouteProvisioner;
  private final ServersConverter serversConverter;
  private final EnvVarsConverter envVarsConverter;
  private final RestartPolicyRewriter restartPolicyRewriter;
  private final RamLimitProvisioner ramLimitProvisioner;

  @Inject
  public OpenShiftEnvironmentProvisioner(
      @Named("che.infra.openshift.pvc.enabled") boolean pvcEnabled,
      UniqueNamesProvisioner uniqueNamesProvisioner,
      TlsRouteProvisioner tlsRouteProvisioner,
      ServersConverter serversConverter,
      EnvVarsConverter envVarsConverter,
      RestartPolicyRewriter restartPolicyRewriter,
      WorkspaceVolumesStrategy volumesStrategy,
      RamLimitProvisioner ramLimitProvisioner) {
    this.pvcEnabled = pvcEnabled;
    this.volumesStrategy = volumesStrategy;
    this.uniqueNamesProvisioner = uniqueNamesProvisioner;
    this.tlsRouteProvisioner = tlsRouteProvisioner;
    this.serversConverter = serversConverter;
    this.envVarsConverter = envVarsConverter;
    this.restartPolicyRewriter = restartPolicyRewriter;
    this.ramLimitProvisioner = ramLimitProvisioner;
  }

  public void provision(OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    // 1 stage - converting Che model env to OpenShift env
    serversConverter.provision(osEnv, identity);
    envVarsConverter.provision(osEnv, identity);
    if (pvcEnabled) {
      volumesStrategy.provision(osEnv, identity);
    }

    // 2 stage - add OpenShift env items
    restartPolicyRewriter.provision(osEnv, identity);
    uniqueNamesProvisioner.provision(osEnv, identity);
    tlsRouteProvisioner.provision(osEnv, identity);
    ramLimitProvisioner.provision(osEnv, identity);
  }
}

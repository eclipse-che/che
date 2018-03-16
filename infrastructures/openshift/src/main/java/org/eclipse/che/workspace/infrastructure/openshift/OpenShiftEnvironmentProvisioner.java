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
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.InstallerServersPortProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PodTerminationGracePeriodProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.limits.ram.RamLimitProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftServersConverter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftUniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.RouteTlsProvisioner;

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
  private final UniqueNamesProvisioner<OpenShiftEnvironment> uniqueNamesProvisioner;
  private final RouteTlsProvisioner routeTlsProvisioner;
  private final OpenShiftServersConverter openShiftServersConverter;
  private final EnvVarsConverter envVarsConverter;
  private final RestartPolicyRewriter restartPolicyRewriter;
  private final RamLimitProvisioner ramLimitProvisioner;
  private final InstallerServersPortProvisioner installerServersPortProvisioner;
  private final LogsVolumeMachineProvisioner logsVolumeMachineProvisioner;
  private final PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner;

  @Inject
  public OpenShiftEnvironmentProvisioner(
      @Named("che.infra.kubernetes.pvc.enabled") boolean pvcEnabled,
      OpenShiftUniqueNamesProvisioner uniqueNamesProvisioner,
      RouteTlsProvisioner routeTlsProvisioner,
      OpenShiftServersConverter openShiftServersConverter,
      EnvVarsConverter envVarsConverter,
      RestartPolicyRewriter restartPolicyRewriter,
      WorkspaceVolumesStrategy volumesStrategy,
      RamLimitProvisioner ramLimitProvisioner,
      InstallerServersPortProvisioner installerServersPortProvisioner,
      LogsVolumeMachineProvisioner logsVolumeMachineProvisioner,
      PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner) {
    this.pvcEnabled = pvcEnabled;
    this.volumesStrategy = volumesStrategy;
    this.uniqueNamesProvisioner = uniqueNamesProvisioner;
    this.routeTlsProvisioner = routeTlsProvisioner;
    this.openShiftServersConverter = openShiftServersConverter;
    this.envVarsConverter = envVarsConverter;
    this.restartPolicyRewriter = restartPolicyRewriter;
    this.ramLimitProvisioner = ramLimitProvisioner;
    this.installerServersPortProvisioner = installerServersPortProvisioner;
    this.logsVolumeMachineProvisioner = logsVolumeMachineProvisioner;
    this.podTerminationGracePeriodProvisioner = podTerminationGracePeriodProvisioner;
  }

  public void provision(OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    // 1 stage - update environment according Infrastructure specific
    installerServersPortProvisioner.provision(osEnv, identity);
    if (pvcEnabled) {
      logsVolumeMachineProvisioner.provision(osEnv, identity);
    }

    // 2 stage - converting Che model env to OpenShift env
    openShiftServersConverter.provision(osEnv, identity);
    envVarsConverter.provision(osEnv, identity);
    if (pvcEnabled) {
      volumesStrategy.provision(osEnv, identity);
    }

    // 3 stage - add OpenShift env items
    restartPolicyRewriter.provision(osEnv, identity);
    uniqueNamesProvisioner.provision(osEnv, identity);
    routeTlsProvisioner.provision(osEnv, identity);
    ramLimitProvisioner.provision(osEnv, identity);
    podTerminationGracePeriodProvisioner.provision(osEnv, identity);
  }
}

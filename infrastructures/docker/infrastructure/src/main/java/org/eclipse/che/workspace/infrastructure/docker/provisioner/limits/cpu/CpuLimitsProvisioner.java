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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.limits.cpu;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Sets CPU limits to docker containers and docker image build configuration in case limits are
 * configured.
 *
 * @author Alexander Garagatyi
 */
public class CpuLimitsProvisioner implements ContainerSystemSettingsProvisioner {
  private final String cpuSet;
  private final long cpuPeriod;
  private final long cpuQuota;

  @Inject
  public CpuLimitsProvisioner(
      @Nullable @Named("che.docker.cpuset_cpus") String cpuSet,
      @Named("che.docker.cpu_period") long cpuPeriod,
      @Named("che.docker.cpu_quota") long cpuQuota) {
    this.cpuSet = cpuSet;
    this.cpuPeriod = cpuPeriod;
    this.cpuQuota = cpuQuota;
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      containerConfig.setCpuPeriod(cpuPeriod);
      containerConfig.setCpuQuota(cpuQuota);
      containerConfig.setCpuSet(cpuSet);
      DockerBuildContext build = containerConfig.getBuild();
      if (build != null) {
        build.setCpuPeriod(cpuPeriod);
        build.setCpuQuota(cpuQuota);
        build.setCpuSet(cpuSet);
      }
    }
  }
}

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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.limits.cpu;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class CpuLimitsProvisionerTest {
  private static final String CPU_SET = "1-3";
  private static final long CPU_PERIOD = 100;
  private static final long CPU_QUOTA = 75;

  private CpuLimitsProvisioner provisioner;
  private DockerEnvironment dockerEnvironment;

  @BeforeMethod
  public void setUp() throws Exception {
    provisioner = new CpuLimitsProvisioner(CPU_SET, CPU_PERIOD, CPU_QUOTA);
    dockerEnvironment = new DockerEnvironment();
  }

  @Test
  public void shouldSetCPULimitsForEachContainer() throws Exception {
    // given
    dockerEnvironment.getContainers().put("cont1", new DockerContainerConfig());
    dockerEnvironment.getContainers().put("cont2", new DockerContainerConfig());
    DockerEnvironment expected = new DockerEnvironment();
    expected
        .getContainers()
        .put(
            "cont1",
            new DockerContainerConfig()
                .setCpuPeriod(CPU_PERIOD)
                .setCpuQuota(CPU_QUOTA)
                .setCpuSet(CPU_SET));
    expected
        .getContainers()
        .put(
            "cont2",
            new DockerContainerConfig()
                .setCpuPeriod(CPU_PERIOD)
                .setCpuQuota(CPU_QUOTA)
                .setCpuSet(CPU_SET));

    // when
    provisioner.provision(dockerEnvironment);

    // then
    assertEquals(expected, dockerEnvironment);
  }

  @Test
  public void shouldSetCPULimitsInBuildArgs() throws Exception {
    // given
    dockerEnvironment
        .getContainers()
        .put("cont1", new DockerContainerConfig().setBuild(new DockerBuildContext()));
    dockerEnvironment.getContainers().put("cont2", new DockerContainerConfig());
    DockerEnvironment expected = new DockerEnvironment();
    expected
        .getContainers()
        .put(
            "cont1",
            new DockerContainerConfig()
                .setCpuPeriod(CPU_PERIOD)
                .setCpuQuota(CPU_QUOTA)
                .setCpuSet(CPU_SET)
                .setBuild(
                    new DockerBuildContext()
                        .setCpuPeriod(CPU_PERIOD)
                        .setCpuQuota(CPU_QUOTA)
                        .setCpuSet(CPU_SET)));
    expected
        .getContainers()
        .put(
            "cont2",
            new DockerContainerConfig()
                .setCpuPeriod(CPU_PERIOD)
                .setCpuQuota(CPU_QUOTA)
                .setCpuSet(CPU_SET));

    // when
    provisioner.provision(dockerEnvironment);

    // then
    assertEquals(expected, dockerEnvironment);
  }
}

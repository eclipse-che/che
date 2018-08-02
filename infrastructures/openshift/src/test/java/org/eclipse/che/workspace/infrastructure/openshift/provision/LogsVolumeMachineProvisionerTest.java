/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner.LOGS_VOLUME_NAME;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link LogsVolumeMachineProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class LogsVolumeMachineProvisionerTest {

  private static final String WORKSPACE_LOGS_ROOT_PATH = "/workspace_logs";
  private static final String MACHINE_NAME_1 = "web/main";
  private static final String MACHINE_NAME_2 = "db/main";

  @Mock private OpenShiftEnvironment openShiftEnvironment;
  @Mock private RuntimeIdentity identity;
  @Mock private InternalMachineConfig machine1;
  @Mock private InternalMachineConfig machine2;

  private LogsVolumeMachineProvisioner logsVolumeProvisioner;

  @BeforeMethod
  public void setup() {
    logsVolumeProvisioner = new LogsVolumeMachineProvisioner(WORKSPACE_LOGS_ROOT_PATH);
    when(machine1.getVolumes()).thenReturn(new HashMap<>());
    when(machine2.getVolumes()).thenReturn(new HashMap<>());
    when(openShiftEnvironment.getMachines())
        .thenReturn(ImmutableMap.of(MACHINE_NAME_1, machine1, MACHINE_NAME_2, machine2));
  }

  @Test
  public void testProvisionLogsVolumeToAllMachineInEnvironment() throws Exception {
    logsVolumeProvisioner.provision(openShiftEnvironment, identity);

    InternalMachineConfig m1 = openShiftEnvironment.getMachines().get(MACHINE_NAME_1);
    InternalMachineConfig m2 = openShiftEnvironment.getMachines().get(MACHINE_NAME_2);
    assertTrue(m1.getVolumes().containsKey(LOGS_VOLUME_NAME));
    assertEquals(m1.getVolumes().get(LOGS_VOLUME_NAME).getPath(), WORKSPACE_LOGS_ROOT_PATH);
    assertTrue(m2.getVolumes().containsKey(LOGS_VOLUME_NAME));
    assertEquals(m2.getVolumes().get(LOGS_VOLUME_NAME).getPath(), WORKSPACE_LOGS_ROOT_PATH);
  }
}

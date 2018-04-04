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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.volume;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class VolumesConverterTest {

  static final String WS_ID = "testWSId";
  static final String MACHINE_1_NAME = "machine1";
  static final String MACHINE_2_NAME = "machine2";

  @Mock InternalMachineConfig machineConfig1;
  @Mock InternalMachineConfig machineConfig2;
  @Mock DockerContainerConfig container1;
  @Mock DockerContainerConfig container2;
  @Mock DockerEnvironment internalEnvironment;
  @Mock RuntimeIdentity runtimeIdentity;
  @InjectMocks VolumesConverter converter;

  @BeforeMethod
  public void setUp() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WS_ID);
    when(internalEnvironment.getMachines())
        .thenReturn(
            ImmutableMap.of(MACHINE_1_NAME, machineConfig1, MACHINE_2_NAME, machineConfig2));
    when(internalEnvironment.getContainers())
        .thenReturn(
            new LinkedHashMap<>(
                ImmutableMap.of(MACHINE_1_NAME, container1, MACHINE_2_NAME, container2)));
  }

  @Test
  public void shouldAddContainerVolumesFromEachMachine() throws Exception {
    // given
    String volume1Name = "vol1";
    String volume1Path = "/some/path";
    String volume2Name = "vol2";
    String volume2Path = "/another/path";

    List<String> machine1ActualVolumes = new ArrayList<>();
    List<String> machine2ActualVolumes = new ArrayList<>();

    when(machineConfig1.getVolumes())
        .thenReturn(singletonMap(volume1Name, new VolumeImpl().withPath(volume1Path)));
    when(machineConfig2.getVolumes())
        .thenReturn(
            ImmutableMap.of(
                volume1Name,
                new VolumeImpl().withPath(volume1Path),
                volume2Name,
                new VolumeImpl().withPath(volume2Path)));
    when(container1.getVolumes()).thenReturn(machine1ActualVolumes);
    when(container2.getVolumes()).thenReturn(machine2ActualVolumes);

    // when
    converter.provision(internalEnvironment, runtimeIdentity);

    // then
    verify(container1).getVolumes();
    verify(container2, times(2)).getVolumes();
    assertEquals(
        machine1ActualVolumes,
        singletonList(VolumeNames.generate(WS_ID, volume1Name) + ":" + volume1Path));
    assertEquals(
        machine2ActualVolumes,
        asList(
            VolumeNames.generate(WS_ID, volume1Name) + ":" + volume1Path,
            VolumeNames.generate(WS_ID, volume2Name) + ":" + volume2Path));
  }
}

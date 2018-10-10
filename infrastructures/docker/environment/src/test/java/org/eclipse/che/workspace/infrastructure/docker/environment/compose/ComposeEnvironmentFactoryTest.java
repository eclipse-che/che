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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.MemoryAttributeProvisioner;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link ComposeEnvironmentFactory}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class ComposeEnvironmentFactoryTest {

  private static final long BYTES_IN_MB = 1024 * 1024;
  private static final String MACHINE_NAME_1 = "machine1";
  private static final String MACHINE_NAME_2 = "machine2";

  @Mock private InstallerRegistry installerRegistry;
  @Mock private RecipeRetriever recipeRetriever;
  @Mock private MachineConfigsValidator machinesValidator;
  @Mock private ComposeEnvironmentValidator composeValidator;
  @Mock private ComposeServicesStartStrategy startStrategy;
  @Mock private MemoryAttributeProvisioner memoryProvisioner;

  private ComposeEnvironmentFactory composeEnvironmentFactory;

  @BeforeMethod
  public void setup() {
    composeEnvironmentFactory =
        new ComposeEnvironmentFactory(
            installerRegistry,
            recipeRetriever,
            machinesValidator,
            composeValidator,
            startStrategy,
            memoryProvisioner);
  }

  @Test
  public void testRamProvisionerIsInvokedForEachMachine() throws Exception {
    final Map<String, InternalMachineConfig> machines = new HashMap<>();
    machines.put(MACHINE_NAME_1, mock(InternalMachineConfig.class));
    final Map<String, ComposeService> services =
        ImmutableMap.of(
            MACHINE_NAME_1,
            mockComposeService(0L, 0L),
            MACHINE_NAME_2,
            mockComposeService(4608L, 2048L));

    composeEnvironmentFactory.addRamAttributes(machines, services);

    verify(memoryProvisioner).provision(any(), eq(0L), eq(0L));
    verify(memoryProvisioner).provision(any(), eq(4608L), eq(2048L));
  }

  private static ComposeService mockComposeService(long ramLimit, long ramRequest) {
    final ComposeService composeServiceMock = mock(ComposeService.class);
    when(composeServiceMock.getMemLimit()).thenReturn(ramLimit);
    when(composeServiceMock.getMemRequest()).thenReturn(ramRequest);
    return composeServiceMock;
  }
}

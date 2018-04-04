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
package org.eclipse.che.multiuser.resource.api.usage.tracker;

import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link EnvironmentRamCalculator}.
 *
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class EnvironmentRamCalculatorTest {

  public static final String RECIPE_TYPE = "compose";
  public static final String MACHINE_NAME_1 = "web-app";
  public static final String MACHINE_NAME_2 = "db";

  @Mock private Recipe recipeMock;
  @Mock private InternalEnvironmentFactory environmentFactory;
  @Mock private Environment environment;
  @Mock private InternalEnvironment internalEnv;
  @Mock private InternalMachineConfig machineConfig1;
  @Mock private InternalMachineConfig machineConfig2;
  @Mock private Runtime runtime;
  @Mock private MachineImpl machine1;
  @Mock private MachineImpl machine2;

  private EnvironmentRamCalculator envRamCalculator;

  @BeforeMethod
  public void setUp() throws Exception {
    envRamCalculator =
        new EnvironmentRamCalculator(ImmutableMap.of(RECIPE_TYPE, environmentFactory));
    when(environmentFactory.create(environment)).thenReturn(internalEnv);
    when(internalEnv.getMachines())
        .thenReturn(
            ImmutableMap.of(
                MACHINE_NAME_1, machineConfig1,
                MACHINE_NAME_2, machineConfig2));
    when(environment.getRecipe()).thenReturn(recipeMock);
    doReturn(ImmutableMap.of(MACHINE_NAME_1, machine1, MACHINE_NAME_2, machine2))
        .when(runtime)
        .getMachines();
  }

  @Test
  public void testCalculatesRamOfEnvironmentWithMultipleMachines() throws Exception {
    when(machineConfig1.getAttributes())
        .thenReturn(ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, "2147483648"));
    when(machineConfig2.getAttributes())
        .thenReturn(ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, "536870912"));
    when(recipeMock.getType()).thenReturn(RECIPE_TYPE);

    final long ram = envRamCalculator.calculate(environment);

    assertEquals(ram, 2560);
  }

  @Test
  public void testCalculatesRamOfEnvironmentWhenSomeMachineConfigHasNoAttribute() throws Exception {
    when(machineConfig1.getAttributes()).thenReturn(Collections.emptyMap());
    when(machineConfig2.getAttributes())
        .thenReturn(ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, "536870912"));
    when(recipeMock.getType()).thenReturn(RECIPE_TYPE);

    final long ram = envRamCalculator.calculate(environment);

    assertEquals(ram, 512);
  }

  @Test(expectedExceptions = ServerException.class)
  public void testThrowServerExceptionWhenNoEnvFactoryForGivenRecipeTypeFound() throws Exception {
    when(recipeMock.getType()).thenReturn("unsupported");

    envRamCalculator.calculate(environment);
  }

  @Test
  public void testCalculatesRamOfRuntimeWithMultipleMachines() throws Exception {
    when(machine1.getAttributes()).thenReturn(ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, "805306368"));
    when(machine2.getAttributes()).thenReturn(ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, "805306368"));

    final long ram = envRamCalculator.calculate(runtime);

    assertEquals(ram, 1536);
  }

  @Test
  public void testCalculatesRamOfRuntimeWhenSomeMachineHasNoAttribute() throws Exception {
    when(machine1.getAttributes()).thenReturn(ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, "805306368"));
    when(machine2.getAttributes()).thenReturn(Collections.emptyMap());

    final long ram = envRamCalculator.calculate(runtime);

    assertEquals(ram, 768);
  }
}

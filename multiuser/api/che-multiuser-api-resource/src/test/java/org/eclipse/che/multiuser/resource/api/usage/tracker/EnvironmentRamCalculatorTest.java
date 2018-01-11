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

/*
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
*/

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.usage.tracker.EnvironmentRamCalculator}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class EnvironmentRamCalculatorTest {
  /*
  private static final long MEGABYTES_TO_BYTES_MULTIPLIER = 1024L * 1024L;

  @Mock private EnvironmentParser environmentParser;
  @Mock private Environment environment;

  private EnvironmentRamCalculator environmentRamCalculator;

  @BeforeMethod
  public void setUp() throws Exception {
    environmentRamCalculator = new EnvironmentRamCalculator(environmentParser, 2048);
  }

  @Test
  public void shouldCalculateRamOfEnvironmentWithMultipleMachines() throws Exception {
    Map<String, CheServiceImpl> services = new HashMap<>();
    services.put(
        "service1", new CheServiceImpl().withMemLimit(1024 * MEGABYTES_TO_BYTES_MULTIPLIER));
    services.put(
        "service2", new CheServiceImpl().withMemLimit(512 * MEGABYTES_TO_BYTES_MULTIPLIER));

    when(environmentParser.parse(anyObject()))
        .thenReturn(new CheServicesEnvironmentImpl().withServices(services));

    long ram = environmentRamCalculator.calculate(environment);

    assertEquals(ram, 1536L);
  }

  @Test
  public void
      shouldUseDefaultMachineRamWhenCalculatingRamOfEnvironmentWithMultipleMachinesIncludingMachineWithoutLimit()
          throws Exception {
    Map<String, CheServiceImpl> services = new HashMap<>();
    services.put("service1", new CheServiceImpl().withMemLimit(null));

    when(environmentParser.parse(anyObject()))
        .thenReturn(new CheServicesEnvironmentImpl().withServices(services));

    long ram = environmentRamCalculator.calculate(environment);

    assertEquals(ram, 2048L);
  }

  @Test
  public void
      shouldUseDefaultMachineRamWhenCalculatingRamOfEnvironmentIncludingMachineWithZeroLimit()
          throws Exception {
    Map<String, CheServiceImpl> services = new HashMap<>();
    services.put("service2", new CheServiceImpl().withMemLimit(0L));

    when(environmentParser.parse(anyObject()))
        .thenReturn(new CheServicesEnvironmentImpl().withServices(services));

    long ram = environmentRamCalculator.calculate(environment);

    assertEquals(ram, 2048L);
  }
  */
}

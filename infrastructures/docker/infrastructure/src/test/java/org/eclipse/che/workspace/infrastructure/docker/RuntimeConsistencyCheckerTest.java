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
package org.eclipse.che.workspace.infrastructure.docker;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Tests {@link RuntimeConsistencyChecker}. */
public class RuntimeConsistencyCheckerTest {

  @Test(dataProvider = "consistentRuntimesProvider")
  public void consistentRuntimes(InternalEnvironment environment, DockerInternalRuntime runtime)
      throws ValidationException {
    new RuntimeConsistencyChecker().check(environment, runtime);
  }

  @Test(
    dataProvider = "inconsistentRuntimesProvider",
    expectedExceptions = ValidationException.class
  )
  public void inconsistentRuntimes(InternalEnvironment environment, DockerInternalRuntime runtime)
      throws ValidationException {
    new RuntimeConsistencyChecker().check(environment, runtime);
  }

  @DataProvider
  private static Object[][] consistentRuntimesProvider() {
    return new Object[][] {
      {environment("a", "b"), runtime("b", "a")}, {environment("b", "a"), runtime("b", "a")}
    };
  }

  @DataProvider
  private static Object[][] inconsistentRuntimesProvider() {
    return new Object[][] {
      {environment("a", "b"), runtime("a")},
      {environment("a", "b"), runtime("a", "c")},
      {environment("a", "b"), runtime("a", "b", "c")},
      {environment("a", "b"), runtime("a")},
      {environment("a"), runtime("a", "b")}
    };
  }

  private static InternalEnvironment environment(String... names) {
    Map<String, InternalMachineConfig> configs = new HashMap<>();
    for (String name : names) {
      configs.put(name, mock(InternalMachineConfig.class));
    }

    InternalEnvironment environment = mock(InternalEnvironment.class);
    doReturn(configs).when(environment).getMachines();
    when(environment.toString()).thenReturn(Arrays.toString(names));
    return environment;
  }

  private static DockerInternalRuntime runtime(String... names) {
    Map<String, DockerMachine> machines = new HashMap<>();
    for (String name : names) {
      machines.put(name, mock(DockerMachine.class));
    }

    DockerInternalRuntime runtime = mock(DockerInternalRuntime.class);
    doReturn(machines).when(runtime).getMachines();
    when(runtime.toString()).thenReturn(Arrays.toString(names));
    return runtime;
  }
}

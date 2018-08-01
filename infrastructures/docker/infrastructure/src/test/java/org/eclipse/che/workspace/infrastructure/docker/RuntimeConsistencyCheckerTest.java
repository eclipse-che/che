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
      throws Exception {
    new RuntimeConsistencyChecker().check(environment, runtime);
  }

  @Test(
    dataProvider = "inconsistentRuntimesProvider",
    expectedExceptions = ValidationException.class
  )
  public void inconsistentRuntimes(InternalEnvironment environment, DockerInternalRuntime runtime)
      throws Exception {
    new RuntimeConsistencyChecker().check(environment, runtime);
  }

  @DataProvider
  private static Object[][] consistentRuntimesProvider() throws Exception {
    return new Object[][] {
      {environment("a", "b"), runtime("b", "a")}, {environment("b", "a"), runtime("b", "a")}
    };
  }

  @DataProvider
  private static Object[][] inconsistentRuntimesProvider() throws Exception {
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

  private static DockerInternalRuntime runtime(String... names) throws Exception {
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

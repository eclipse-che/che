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
package org.eclipse.che.ide.processes.runtime;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.EnvironmentImpl;
import org.eclipse.che.ide.api.workspace.model.MachineConfigImpl;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.ServerConfigImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceConfigImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/**
 * Unit tests for the {@link ContextBasedRuntimeInfoProvider}.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
@RunWith(GwtMockitoTestRunner.class)
public class ContextBasedRuntimeInfoProviderTest {

  private static final String DEV_MACHINE = "dev-machine";
  private static final String DEFAULT_ENV = "default";

  private static final String CONFIG_REF = "web";
  private static final String CONFIG_PORT = "666";
  private static final String CONFIG_PROTOCOL = "http";
  private static final String CONFIG_URL = "http://example.com/";

  private static final String RUNTIME_URL = "http://example.org/";
  private static final String RUNTIME_REF = "wsagent";

  @Mock private AppContext appContext;

  private ContextBasedRuntimeInfoProvider provider;

  @Before
  public void setUp() throws Exception {
    provider = new ContextBasedRuntimeInfoProvider(appContext);
  }

  @Test
  public void testShouldReturnRuntimeInfoList() throws Exception {
    WorkspaceImpl workspace = mock(WorkspaceImpl.class);
    WorkspaceConfigImpl workspaceConfig = mock(WorkspaceConfigImpl.class);
    EnvironmentImpl environment = mock(EnvironmentImpl.class);
    MachineConfigImpl machineConfig = mock(MachineConfigImpl.class);

    when(appContext.getWorkspace()).thenReturn(workspace);
    when(workspace.getConfig()).thenReturn(workspaceConfig);
    when(workspaceConfig.getDefaultEnv()).thenReturn(DEFAULT_ENV);
    when(workspaceConfig.getEnvironments()).thenReturn(singletonMap(DEFAULT_ENV, environment));
    when(environment.getMachines()).thenReturn(singletonMap(DEV_MACHINE, machineConfig));

    RuntimeImpl runtime = mock(RuntimeImpl.class);
    MachineImpl runtimeDevMachine = mock(MachineImpl.class);

    when(workspace.getRuntime()).thenReturn(runtime);
    when(runtime.getMachineByName(eq(DEV_MACHINE))).thenReturn(Optional.of(runtimeDevMachine));

    ServerConfigImpl serverConfig1 = mock(ServerConfigImpl.class);
    when(serverConfig1.getPort()).thenReturn(CONFIG_PORT);
    when(serverConfig1.getProtocol()).thenReturn(CONFIG_PROTOCOL);

    when(machineConfig.getServers()).thenReturn(singletonMap(CONFIG_REF, serverConfig1));

    ServerImpl runtimeServer1 = mock(ServerImpl.class);
    when(runtimeServer1.getUrl()).thenReturn(CONFIG_URL);

    ServerImpl runtimeServer2 = mock(ServerImpl.class);
    when(runtimeServer2.getUrl()).thenReturn(RUNTIME_URL);

    Map<String, ServerImpl> runtimeServers = new HashMap<>();
    runtimeServers.put(CONFIG_REF, runtimeServer1);
    runtimeServers.put(RUNTIME_REF, runtimeServer2);

    when(runtimeDevMachine.getServers()).thenReturn(runtimeServers);

    List<RuntimeInfo> expectedList = provider.get(DEV_MACHINE);

    assertEquals(expectedList.size(), 2);

    RuntimeInfo expectedRuntime1 = new RuntimeInfo("web", "666", "http", "http://example.com/");
    RuntimeInfo expectedRuntime2 = new RuntimeInfo("wsagent", null, "http", "http://example.org/");

    assertTrue(expectedList.contains(expectedRuntime1));
    assertTrue(expectedList.contains(expectedRuntime2));
  }

  @Test(expected = NullPointerException.class)
  public void testShouldCatchNullPointerExceptionWhenMachineNameIsNull() throws Exception {
    provider.get(null);
  }

  @Test
  public void testShouldReturnEmptyListWhenWorkspaceIsNotSet() throws Exception {
    List<RuntimeInfo> expectedList = provider.get(DEV_MACHINE);

    assertTrue(expectedList.isEmpty());
  }
}

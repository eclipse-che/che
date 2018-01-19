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
package org.eclipse.che.api.workspace.server.spi.provision;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link InstallerConfigProvisioner}.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class InstallerConfigProvisionerTest {
  @Mock private RuntimeIdentity runtimeIdentity;

  @Mock private InternalEnvironment internalEnvironment;
  @Mock private InternalMachineConfig machine1;
  @Mock private InternalMachineConfig machine2;

  @Mock private InstallerImpl installer1;
  @Mock private InstallerImpl installer2;

  private InstallerConfigProvisioner installerConfigProvisioner;

  @BeforeMethod
  public void setUp() throws Exception {
    installerConfigProvisioner = new InstallerConfigProvisioner();

    doReturn(ImmutableMap.of("machine1", machine1, "machine2", machine2))
        .when(internalEnvironment)
        .getMachines();

    when(machine1.getInstallers()).thenReturn(singletonList(installer1));
    when(installer1.getProperties()).thenReturn(emptyMap());
    when(installer1.getServers()).thenReturn(emptyMap());

    when(machine2.getInstallers()).thenReturn(singletonList(installer2));
    when(installer2.getProperties()).thenReturn(emptyMap());
    when(installer2.getServers()).thenReturn(emptyMap());
  }

  @Test
  public void provisionWithEnvVarsFromInstallersAttributes() throws Exception {
    // given
    when(installer1.getProperties())
        .thenReturn(
            ImmutableMap.of(Installer.ENVIRONMENT_PROPERTY, "envVar1=value1,envVar2=value2"));
    when(installer2.getProperties())
        .thenReturn(ImmutableMap.of(Installer.ENVIRONMENT_PROPERTY, "envVar3=value3"));

    Map<String, String> machine1Env = new HashMap<>();
    when(machine1.getEnv()).thenReturn(machine1Env);

    Map<String, String> machine2Env = new HashMap<>();
    when(this.machine2.getEnv()).thenReturn(machine2Env);
    machine2Env.put("existingEnv", "value");

    // when
    installerConfigProvisioner.provision(runtimeIdentity, internalEnvironment);

    // then
    assertEquals(machine1Env, ImmutableMap.of("envVar1", "value1", "envVar2", "value2"));
    assertEquals(machine2Env, ImmutableMap.of("existingEnv", "value", "envVar3", "value3"));
  }

  @Test
  public void provisionWithServersThatAreDeclaredByInstallers() throws Exception {
    // given
    ServerConfig installer1server = mock(ServerConfig.class);
    ServerConfig commonInstallerServer = mock(ServerConfig.class);
    ServerConfig installer2Server = mock(ServerConfig.class);
    ServerConfig machine2Server = mock(ServerConfig.class);

    doReturn(
            ImmutableMap.of(
                "installer1server",
                installer1server,
                "commonInstallerServer",
                commonInstallerServer))
        .when(installer1)
        .getServers();
    doReturn(
            ImmutableMap.of(
                "commonInstallerServer",
                commonInstallerServer,
                "installer2Server",
                installer2Server))
        .when(installer2)
        .getServers();

    Map<String, ServerConfig> machine1Servers = new HashMap<>();
    when(machine1.getServers()).thenReturn(machine1Servers);

    Map<String, ServerConfig> machine2Servers = new HashMap<>();
    when(this.machine2.getServers()).thenReturn(machine2Servers);
    machine2Servers.put("machine2Server", machine2Server);

    // when
    installerConfigProvisioner.provision(runtimeIdentity, internalEnvironment);

    // then
    assertEquals(
        machine1Servers,
        ImmutableMap.of(
            "installer1server", installer1server, "commonInstallerServer", commonInstallerServer));
    assertEquals(
        machine2Servers,
        ImmutableMap.of(
            "machine2Server",
            machine2Server,
            "commonInstallerServer",
            commonInstallerServer,
            "installer2Server",
            installer2Server));
  }
}

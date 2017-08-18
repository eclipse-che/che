/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner.installer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
@Listeners(value = {MockitoTestNGListener.class})
public class InstallerConfigApplierTest {
  @Mock private InstallerImpl installer1;
  @Mock private InstallerImpl installer2;
  @Mock private InstallerImpl installer3;
  @Mock private InstallerRegistry installerRegistry;

  private InstallerConfigApplier installerConfigApplier;

  @BeforeMethod
  public void setUp() throws Exception {
    installerConfigApplier = new InstallerConfigApplier(installerRegistry);
    when(installerRegistry.getInstaller("installer1")).thenReturn(installer1);
    when(installerRegistry.getInstaller("installer2")).thenReturn(installer2);
    when(installerRegistry.getInstaller("installer3")).thenReturn(installer3);

    when(installer1.getScript()).thenReturn("script1");
    when(installer1.getDependencies()).thenReturn(singletonList("installer3"));

    when(installer2.getScript()).thenReturn("script2");
    when(installer2.getDependencies()).thenReturn(singletonList("installer2"));

    when(installer3.getScript()).thenReturn("script3");
  }

  @Test
  public void shouldAddLabels() throws Exception {
    final ServerConfig serverConf1 = mock(ServerConfig.class);
    when(serverConf1.getPort()).thenReturn("1111/udp");
    when(serverConf1.getProtocol()).thenReturn("http");
    when(serverConf1.getPath()).thenReturn("b");

    when(installerRegistry.getOrderedInstallers(any())).thenReturn(singletonList(installer1));
    when(installer1.getServers()).thenAnswer(invocation -> singletonMap("a", serverConf1));
    DockerContainerConfig service = new DockerContainerConfig();

    installerConfigApplier.apply(
        new MachineConfigImpl(singletonList("installer1"), emptyMap(), emptyMap()), service);

    Map<String, String> labels = service.getLabels();
    assertEquals(labels.size(), 3);
    assertEquals(labels.get("org.eclipse.che.server.a.port"), "1111/udp");
    assertEquals(labels.get("org.eclipse.che.server.a.protocol"), "http");
    assertEquals(labels.get("org.eclipse.che.server.a.path"), "b");
  }

  @Test
  public void shouldAddExposedPorts() throws Exception {
    final ServerConfig serverConf1 = mock(ServerConfig.class);
    final ServerConfig serverConfig = mock(ServerConfig.class);
    when(serverConf1.getPort()).thenReturn("1111/udp");
    when(serverConfig.getPort()).thenReturn("2222/tcp");

    when(installerRegistry.getOrderedInstallers(any()))
        .thenReturn(asList(installer1, installer2, installer3));
    when(installer1.getServers()).thenAnswer(invocation -> singletonMap("a", serverConf1));
    when(installer2.getServers()).thenAnswer(invocation -> singletonMap("b", serverConfig));
    when(installer3.getServers()).thenReturn(emptyMap());
    DockerContainerConfig service = new DockerContainerConfig();

    installerConfigApplier.apply(
        new MachineConfigImpl(
            asList("installer1", "installer2", "installer3"), emptyMap(), emptyMap()),
        service);

    List<String> exposedPorts = service.getExpose();
    assertTrue(exposedPorts.contains("1111/udp"));
    assertTrue(exposedPorts.contains("2222/tcp"));
  }

  @Test
  public void shouldAddEnvVariables() throws Exception {
    when(installerRegistry.getOrderedInstallers(any())).thenReturn(asList(installer1, installer2));
    when(installer1.getProperties()).thenReturn(singletonMap("environment", "p1=v1,p2=v2"));
    when(installer2.getProperties()).thenReturn(singletonMap("environment", "p3=v3"));
    DockerContainerConfig service = new DockerContainerConfig();

    installerConfigApplier.apply(
        new MachineConfigImpl(asList("installer1", "installer2"), emptyMap(), emptyMap()), service);

    Map<String, String> env = service.getEnvironment();
    assertEquals(env.size(), 3);
    assertEquals(env.get("p1"), "v1");
    assertEquals(env.get("p2"), "v2");
    assertEquals(env.get("p3"), "v3");
  }

  @Test
  public void shouldIgnoreEnvironmentIfIllegalFormat() throws Exception {
    when(installerRegistry.getOrderedInstallers(any())).thenReturn(singletonList(installer1));
    when(installer1.getProperties()).thenReturn(singletonMap("environment", "p1"));
    DockerContainerConfig service = new DockerContainerConfig();

    installerConfigApplier.apply(
        new MachineConfigImpl(singletonList("installer1"), emptyMap(), emptyMap()), service);

    Map<String, String> env = service.getEnvironment();
    assertEquals(env.size(), 0);
  }
}

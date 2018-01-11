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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.server;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.Labels;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class ServersConverterTest {
  @Mock RuntimeIdentity identity;
  @Mock DockerEnvironment dockerEnvironment;
  @Mock InternalMachineConfig machine1;
  @Mock InternalMachineConfig machine2;
  @Mock DockerContainerConfig container1;
  @Mock DockerContainerConfig container2;
  @Mock Map<String, String> container1Labels;
  @Mock Map<String, String> container2Labels;
  @Mock List<String> ports1;
  @Mock List<String> ports2;

  Map<String, ServerConfig> servers1;
  Map<String, ServerConfig> servers2;
  ServersConverter serversConverter = new ServersConverter();

  @BeforeMethod
  public void setUp() throws Exception {
    when(dockerEnvironment.getContainers())
        .thenReturn(
            new LinkedHashMap<>(ImmutableMap.of("machine1", container1, "machine2", container2)));
    when(dockerEnvironment.getMachines())
        .thenReturn(ImmutableMap.of("machine1", machine1, "machine2", machine2));
    when(container1.getLabels()).thenReturn(container1Labels);
    when(container2.getLabels()).thenReturn(container2Labels);
    servers1 =
        ImmutableMap.of(
            "server1",
            new ServerConfigImpl("7800", "http", "/path", singletonMap("key", "value")),
            "server2",
            new ServerConfigImpl(
                "7979",
                "tcp",
                "",
                singletonMap(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, Boolean.TRUE.toString())));
    servers2 = ImmutableMap.of("server3", new ServerConfigImpl("20000/udp", "udp", null, null));
    when(machine1.getServers()).thenReturn(servers1);
    when(machine2.getServers()).thenReturn(servers2);
    when(container1.getPorts()).thenReturn(ports1);
    when(container2.getPorts()).thenReturn(ports2);
  }

  @Test
  public void shouldAddLabelsOfServersToAContainer() throws Exception {
    serversConverter.provision(dockerEnvironment, identity);

    verify(container1Labels).putAll(eq(Labels.newSerializer().servers(servers1).labels()));
    verify(container2Labels).putAll(eq(Labels.newSerializer().servers(servers2).labels()));
  }

  @Test
  public void shouldExposePortsForBothInternalAndExternalServers() throws Exception {
    serversConverter.provision(dockerEnvironment, identity);

    verify(container1).addExpose("7800");
    verify(container1).addExpose("7979");
    verify(container2).addExpose("20000/udp");
  }

  @Test
  public void shouldPublishPortsForExternalServersOnly() throws Exception {
    serversConverter.provision(dockerEnvironment, identity);

    verify(ports1).add("7800");
    verify(ports1, never()).add("7979");
    verify(ports2).add("20000/udp");
  }
}

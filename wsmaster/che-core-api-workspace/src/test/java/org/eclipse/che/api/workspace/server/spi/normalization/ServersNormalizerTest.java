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
package org.eclipse.che.api.workspace.server.spi.normalization;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link ServersNormalizer}.
 *
 * @author Alexander Garagatyi
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class ServersNormalizerTest {
  @Mock private InternalEnvironment internalEnvironment;
  @Mock private InternalMachineConfig machine1;
  @Mock private InternalMachineConfig machine2;

  private ServersNormalizer serversNormalizer;

  @BeforeMethod
  public void setUp() throws Exception {
    serversNormalizer = new ServersNormalizer();

    doReturn(ImmutableMap.of("machine1", machine1, "machine2", machine2))
        .when(internalEnvironment)
        .getMachines();
  }

  @Test
  public void normalizeServersProtocols() throws InfrastructureException {
    ServerConfigImpl serverWithoutProtocol = new ServerConfigImpl("8080", "http", "/api");
    ServerConfigImpl udpServer = new ServerConfigImpl("8080/udp", "http", "/api");
    ServerConfigImpl normalizedServer = new ServerConfigImpl("8080/tcp", "http", "/api");
    HashMap<String, ServerConfig> machine1Servers = new HashMap<>();
    machine1Servers.put("serverWithoutProtocol", serverWithoutProtocol);
    when(machine1.getServers()).thenReturn(machine1Servers);

    HashMap<String, ServerConfig> machine2Servers = new HashMap<>();
    machine2Servers.put("serverWithoutProtocol", serverWithoutProtocol);
    machine2Servers.put("udpServer", udpServer);
    when(machine2.getServers()).thenReturn(machine2Servers);

    serversNormalizer.normalize(internalEnvironment);

    assertEquals(machine1Servers, ImmutableMap.of("serverWithoutProtocol", normalizedServer));
    assertEquals(
        machine2Servers,
        ImmutableMap.of("serverWithoutProtocol", normalizedServer, "udpServer", udpServer));
  }
}

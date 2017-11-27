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
package org.eclipse.che.workspace.infrastructure.openshift;

import static java.util.Collections.emptyMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.testng.annotations.Test;

/**
 * Test for {@link RoutesAnnotations}.
 *
 * @author Sergii Leshchenko
 */
public class RoutesAnnotationsTest {
  @Test
  public void serialization() {
    Map<String, String> serialized =
        RoutesAnnotations.newSerializer()
            .server(
                "my-server1/http",
                new ServerConfigImpl("8000/tcp", "http", "/api/info", emptyMap()))
            .servers(
                ImmutableMap.of(
                    "my-server2", new ServerConfigImpl("8080/tcp", "ws", "/connect", emptyMap())))
            .annotations();
    Map<String, String> expected =
        ImmutableMap.<String, String>builder()
            .put("org.eclipse.che.server.my-server1/http.port", "8000/tcp")
            .put("org.eclipse.che.server.my-server1/http.protocol", "http")
            .put("org.eclipse.che.server.my-server1/http.path", "/api/info")
            .put("org.eclipse.che.server.my-server2.port", "8080/tcp")
            .put("org.eclipse.che.server.my-server2.protocol", "ws")
            .put("org.eclipse.che.server.my-server2.path", "/connect")
            .build();

    assertEquals(serialized, expected);
  }

  @Test
  public void deserialization() {
    ImmutableMap<String, String> annotations =
        ImmutableMap.<String, String>builder()
            .put("custom-label", "value")
            .put("org.eclipse.che.server.my-server1/http.port", "8000/tcp")
            .put("org.eclipse.che.server.my-server1/http.protocol", "http")
            .put("org.eclipse.che.server.my-server1/http.path", "/api/info")
            .put("org.eclipse.che.server.my-server2.port", "8080/tcp")
            .put("org.eclipse.che.server.my-server2.protocol", "ws")
            .put("org.eclipse.che.server.my-server2.path", "/connect")
            .build();

    RoutesAnnotations.Deserializer deserializer = RoutesAnnotations.newDeserializer(annotations);

    Map<String, ServerConfigImpl> servers = deserializer.servers();
    ServerConfig server1 = servers.get("my-server1/http");
    assertNotNull(server1, "first server");
    assertEquals(server1.getPort(), "8000/tcp");
    assertEquals(server1.getProtocol(), "http");
    assertEquals(server1.getPath(), "/api/info");

    ServerConfig server2 = servers.get("my-server2");
    assertNotNull(server2, "second server");
    assertEquals(server2.getPort(), "8080/tcp");
    assertEquals(server2.getProtocol(), "ws");
    assertEquals(server2.getPath(), "/connect");
  }
}

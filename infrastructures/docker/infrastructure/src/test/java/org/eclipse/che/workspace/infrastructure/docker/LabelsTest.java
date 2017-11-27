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
package org.eclipse.che.workspace.infrastructure.docker;

import static java.util.Collections.emptyMap;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.testng.annotations.Test;

/**
 * Tests {@link Labels}.
 *
 * @author Yevhenii Voevodin
 */
public class LabelsTest {

  @Test
  public void serialization() {
    Map<String, String> serialized =
        Labels.newSerializer()
            .machineName("dev-machine")
            .runtimeId(new RuntimeIdentityImpl("workspace123", "my-env", "owner"))
            .server(
                "my-server1/http",
                new ServerConfigImpl("8000/tcp", "http", "/api/info", emptyMap()))
            .server("my-server2", new ServerConfigImpl("8080/tcp", "ws", "/connect", emptyMap()))
            .server("my-server3", new ServerConfigImpl("7070/tcp", "http", null, emptyMap()))
            .server(
                "my.dot.separated.server",
                new ServerConfigImpl("9090/tcp", "http", null, emptyMap()))
            .labels();
    Map<String, String> expected =
        ImmutableMap.<String, String>builder()
            .put("org.eclipse.che.machine.name", "dev-machine")
            .put("org.eclipse.che.workspace.id", "workspace123")
            .put("org.eclipse.che.workspace.env", "my-env")
            .put("org.eclipse.che.workspace.owner", "owner")
            .put("org.eclipse.che.server.my-server1/http.port", "8000/tcp")
            .put("org.eclipse.che.server.my-server1/http.protocol", "http")
            .put("org.eclipse.che.server.my-server1/http.path", "/api/info")
            .put("org.eclipse.che.server.my-server2.port", "8080/tcp")
            .put("org.eclipse.che.server.my-server2.protocol", "ws")
            .put("org.eclipse.che.server.my-server2.path", "/connect")
            .put("org.eclipse.che.server.my-server3.port", "7070/tcp")
            .put("org.eclipse.che.server.my-server3.protocol", "http")
            .put("org.eclipse.che.server.my.dot.separated.server.port", "9090/tcp")
            .put("org.eclipse.che.server.my.dot.separated.server.protocol", "http")
            .build();

    assertEquals(serialized, expected);
  }

  @Test
  public void deserialization() {
    ImmutableMap<String, String> labels =
        ImmutableMap.<String, String>builder()
            .put("custom-label", "value")
            .put("org.eclipse.che.machine.unknown-label", "value")
            .put("org.eclipse.che.machine.name", "dev-machine")
            .put("org.eclipse.che.workspace.id", "workspace123")
            .put("org.eclipse.che.workspace.env", "my-env")
            .put("org.eclipse.che.workspace.owner", "owner")
            .put("org.eclipse.che.server.my-server1/http.port", "8000/tcp")
            .put("org.eclipse.che.server.my-server1/http.protocol", "http")
            .put("org.eclipse.che.server.my-server1/http.path", "/api/info")
            .put("org.eclipse.che.server.my-server2.port", "8080/tcp")
            .put("org.eclipse.che.server.my-server2.protocol", "ws")
            .put("org.eclipse.che.server.my-server2.path", "/connect")
            .put("org.eclipse.che.server.my-server3.port", "7070/tcp")
            .put("org.eclipse.che.server.my-server3.protocol", "http")
            .put("org.eclipse.che.server.my.dot.separated.server.port", "9090/tcp")
            .put("org.eclipse.che.server.my.dot.separated.server.protocol", "http")
            .build();

    Labels.Deserializer deserializer = Labels.newDeserializer(labels);
    Map<String, ServerConfig> expectedServers = new HashMap<>();
    expectedServers.put(
        "my-server1/http", new ServerConfigImpl("8000/tcp", "http", "/api/info", emptyMap()));
    expectedServers.put(
        "my-server2", new ServerConfigImpl("8080/tcp", "ws", "/connect", emptyMap()));
    expectedServers.put("my-server3", new ServerConfigImpl("7070/tcp", "http", null, emptyMap()));
    expectedServers.put(
        "my.dot.separated.server", new ServerConfigImpl("9090/tcp", "http", null, emptyMap()));

    assertEquals(deserializer.machineName(), "dev-machine");

    RuntimeIdentity runtimeId = deserializer.runtimeId();
    assertEquals(runtimeId.getWorkspaceId(), "workspace123", "workspace id");
    assertEquals(runtimeId.getEnvName(), "my-env", "workspace environment name");
    assertEquals(runtimeId.getOwner(), "owner", "workspace owner");

    Map<String, ServerConfig> servers = deserializer.servers();
    assertEquals(servers, expectedServers);
  }
}

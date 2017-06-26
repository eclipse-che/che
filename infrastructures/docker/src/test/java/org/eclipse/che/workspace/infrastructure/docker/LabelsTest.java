/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentityImpl;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests {@link Labels}.
 *
 * @author Yevhenii Voevodin
 */
public class LabelsTest {

    @Test
    public void serialization() {
        Map<String, String> serialized = Labels.newSerializer()
                                               .machineName("dev-machine")
                                               .runtimeId(new RuntimeIdentityImpl("workspace123", "my-env", "owner"))
                                               .server("my-server1", new ServerConfigImpl("8000/tcp", "http", "/api/info"))
                                               .server("my-server2", new ServerConfigImpl("8080/tcp", "ws", "/connect"))
                                               .labels();
        Map<String, String> expected =
                ImmutableMap.<String, String>builder()
                        .put("org.eclipse.che.machine.name", "dev-machine")
                        .put("org.eclipse.che.workspace.id", "workspace123")
                        .put("org.eclipse.che.workspace.env", "my-env")
                        .put("org.eclipse.che.workspace.owner", "owner")
                        .put("org.eclipse.che.server.my-server1.port", "8000/tcp")
                        .put("org.eclipse.che.server.my-server1.protocol", "http")
                        .put("org.eclipse.che.server.my-server1.path", "/api/info")
                        .put("org.eclipse.che.server.my-server2.port", "8080/tcp")
                        .put("org.eclipse.che.server.my-server2.protocol", "ws")
                        .put("org.eclipse.che.server.my-server2.path", "/connect")
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
                        .put("org.eclipse.che.server.my-server1.port", "8000/tcp")
                        .put("org.eclipse.che.server.my-server1.protocol", "http")
                        .put("org.eclipse.che.server.my-server1.path", "/api/info")
                        .put("org.eclipse.che.server.my-server2.port", "8080/tcp")
                        .put("org.eclipse.che.server.my-server2.protocol", "ws")
                        .put("org.eclipse.che.server.my-server2.path", "/connect")
                        .build();

        Labels.Deserializer deserializer = Labels.newDeserializer(labels);

        assertEquals(deserializer.machineName(), "dev-machine");

        RuntimeIdentity runtimeId = deserializer.runtimeId();
        assertEquals(runtimeId.getWorkspaceId(), "workspace123", "workspace id");
        assertEquals(runtimeId.getEnvName(), "my-env", "workspace environment name");
        assertEquals(runtimeId.getOwner(), "owner", "workspace owner");

        Map<String, ServerConfig> servers = deserializer.servers();
        ServerConfig server1 = servers.get("my-server1");
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

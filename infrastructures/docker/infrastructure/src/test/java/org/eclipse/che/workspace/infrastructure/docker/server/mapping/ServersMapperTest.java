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
package org.eclipse.che.workspace.infrastructure.docker.server.mapping;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.infrastructure.docker.client.json.PortBinding;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Tests {@link ServersMapper}. */
public class ServersMapperTest {
  static final Map<String, String> ONE_ATTRIBUTE_MAP = singletonMap("testAttr", "testValue");
  static final Map<String, String> ATTRIBUTES_MAP =
      ImmutableMap.of("testAttr", "testValue", "anotherTestAttr", "secondValue");
  static final Map<String, String> INTERNAL_SERVER_ATTRIBUTE_MAP =
      singletonMap(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, Boolean.TRUE.toString());

  private final String hostname = "localhost";
  private final String machine = "app";
  private final ServersMapper mapper = new ServersMapper(hostname, machine);

  @Test(dataProvider = "servers")
  public void mapsServers(
      Map<String, String> dockerBindings,
      Map<String, ServerConfig> configs,
      Map<String, ServerImpl> expected)
      throws Exception {
    Map<String, List<PortBinding>> bindings = createBindings(dockerBindings);

    Map<String, ServerImpl> result = mapper.map(bindings, configs);

    assertEquals(result, expected);
  }

  @DataProvider(name = "servers")
  private Object[][] serversProvider() {
    return new Object[][] {
      {
        ImmutableMap.of(
            "8080/tcp", "0.0.0.0:32080",
            "8081/tcp", "0.0.0.0:32081"),
        ImmutableMap.of(
            "server1", new ServerConfigImpl("8080", "http", "no-slash-path", ONE_ATTRIBUTE_MAP),
            "server2", new ServerConfigImpl("8081", "http", "/slash-path", null)),
        ImmutableMap.of(
            "server1",
                new ServerImpl()
                    .withUrl("http://" + hostname + ":32080/no-slash-path")
                    .withAttributes(ONE_ATTRIBUTE_MAP)
                    .withStatus(ServerStatus.UNKNOWN),
            "server2",
                new ServerImpl()
                    .withUrl("http://" + hostname + ":32081/slash-path")
                    .withAttributes(emptyMap())
                    .withStatus(ServerStatus.UNKNOWN))
      },
      {
        ImmutableMap.of("8080/tcp", "0.0.0.0:32080"),
        ImmutableMap.of(
            "server1", new ServerConfigImpl("8080", "http", "http-endpoint", emptyMap()),
            "server2", new ServerConfigImpl("8080", "ws", "ws-endpoint", ATTRIBUTES_MAP)),
        ImmutableMap.of(
            "server1",
                new ServerImpl()
                    .withUrl("http://" + hostname + ":32080/http-endpoint")
                    .withAttributes(emptyMap())
                    .withStatus(ServerStatus.UNKNOWN),
            "server2",
                new ServerImpl()
                    .withUrl("ws://" + hostname + ":32080/ws-endpoint")
                    .withAttributes(ATTRIBUTES_MAP)
                    .withStatus(ServerStatus.UNKNOWN))
      },
      {
        ImmutableMap.of("8080/tcp", "0.0.0.0:32080"),
        ImmutableMap.of(
            "server1", new ServerConfigImpl("8080", "http", "http-endpoint", emptyMap()),
            "server2", new ServerConfigImpl("8080/tcp", "ws", "ws-endpoint", null)),
        ImmutableMap.of(
            "server1",
                new ServerImpl()
                    .withUrl("http://" + hostname + ":32080/http-endpoint")
                    .withAttributes(emptyMap())
                    .withStatus(ServerStatus.UNKNOWN),
            "server2",
                new ServerImpl()
                    .withUrl("ws://" + hostname + ":32080/ws-endpoint")
                    .withAttributes(emptyMap())
                    .withStatus(ServerStatus.UNKNOWN))
      },
      // ensure that ports that don't have matching server config are not shown as servers
      {
        ImmutableMap.of(
            "8080/tcp", "0.0.0.0:32080",
            "8081/udp", "0.0.0.0:32081",
            "8082", "0.0.0.0:32082"),
        ImmutableMap.of(),
        emptyMap()
      },
      {
        ImmutableMap.of(
            "8000/tcp", "0.0.0.0:32000",
            "8080/tcp", "0.0.0.0:32080",
            "2288/udp", "0.0.0.0:32288",
            "4401/tcp", "0.0.0.0:32401"),
        ImmutableMap.of(
            "ws-master", new ServerConfigImpl("8080", "http", "/api", emptyMap()),
            "exec-agent-api", new ServerConfigImpl("4401", "http", "/process", ONE_ATTRIBUTE_MAP),
            "exec-agent-ws", new ServerConfigImpl("4401", "ws", "/connect", ATTRIBUTES_MAP)),
        ImmutableMap.of(
            "ws-master",
                new ServerImpl()
                    .withUrl("http://" + hostname + ":32080/api")
                    .withAttributes(emptyMap())
                    .withStatus(ServerStatus.UNKNOWN),
            "exec-agent-api",
                new ServerImpl()
                    .withUrl("http://" + hostname + ":32401/process")
                    .withAttributes(ONE_ATTRIBUTE_MAP)
                    .withStatus(ServerStatus.UNKNOWN),
            "exec-agent-ws",
                new ServerImpl()
                    .withUrl("ws://" + hostname + ":32401/connect")
                    .withAttributes(ATTRIBUTES_MAP)
                    .withStatus(ServerStatus.UNKNOWN))
      },
      // mapping of internal servers
      {
        mapOf("4401/tcp", null),
        ImmutableMap.of(
            "ls-api", new ServerConfigImpl("4401", "tcp", null, INTERNAL_SERVER_ATTRIBUTE_MAP)),
        ImmutableMap.of(
            "ls-api",
            new ServerImpl()
                .withUrl("tcp://" + machine + ":4401")
                .withAttributes(INTERNAL_SERVER_ATTRIBUTE_MAP)
                .withStatus(ServerStatus.UNKNOWN))
      }
    };
  }

  private Map<String, String> mapOf(String key, String value) {
    HashMap<String, String> result = new HashMap<>();
    result.put(key, value);
    return result;
  }

  private Map<String, String> mapOf(String key, String value, String key2, String value2) {
    HashMap<String, String> result = new HashMap<>();
    result.put(key, value);
    result.put(key2, value2);
    return result;
  }

  private static Map<String, List<PortBinding>> createBindings(Map<String, String> bindings) {
    return bindings
        .entrySet()
        .stream()
        .collect(
            HashMap::new,
            (hashMap, entry) -> {
              if (entry.getValue() == null) {
                hashMap.put(entry.getKey(), null);
              } else {
                String[] split = entry.getValue().split(":");
                PortBinding pb = new PortBinding(split[0], split[1]);
                hashMap.put(entry.getKey(), singletonList(pb));
              }
            },
            HashMap::putAll);
  }
}

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
package org.eclipse.che.workspace.infrastructure.docker.server.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.infrastructure.docker.client.json.ContainerPort;
import org.eclipse.che.infrastructure.docker.client.json.NetworkSettings;
import org.eclipse.che.infrastructure.docker.client.json.PortBinding;

/** Maps container ports bindings to machine servers. */
public class ServersMapper {

  private final String hostname;

  /**
   * Creates mapper using given {@code hostname} as hostname for all the servers urls produced by
   * mapper.
   */
  public ServersMapper(String hostname) {
    this.hostname = hostname;
  }

  /**
   * Maps container ports to machine servers resolving references from the given configuration map.
   *
   * @param ports container ports to map
   * @param configs servers configuration map used to resolve server references
   * @return server reference -> server map. Note that if there is no server configuration for
   *     container bound port, port+type itself(like 4022/tpc) will be used as a reference
   */
  public Map<String, ServerImpl> map(ContainerPort[] ports, Map<String, ServerConfig> configs) {
    if (ports == null || ports.length == 0) {
      return Collections.emptyMap();
    }

    // 4011/tcp -> [ exec-agent-rest, exec-agent-ws ]
    // 4012     -> [ terminal ]
    Map<String, List<String>> port2refs = new HashMap<>();
    for (Map.Entry<String, ServerConfig> entry : configs.entrySet()) {
      port2refs.compute(
          entry.getValue().getPort(),
          (port, list) -> {
            if (list == null) {
              list = new ArrayList<>();
            }
            list.add(entry.getKey());
            return list;
          });
    }

    Map<String, ServerImpl> mapped = new HashMap<>();
    for (ContainerPort port : ports) {
      List<String> refs = null;

      // configs which define port in format 'numPort/type' e.g. 4011/tcp
      String rawPort = port.getPrivatePort() + "/" + port.getType();
      if (port2refs.containsKey(rawPort)) {
        refs = port2refs.get(rawPort);
      }

      // configs which define numPort in format 'numPort' e.g. 4011
      String numPort = Integer.toString(port.getPrivatePort());
      if (port2refs.containsKey(numPort)) {
        if (refs == null) {
          refs = port2refs.get(numPort);
        } else {
          refs.addAll(port2refs.get(numPort));
        }
      }

      if (refs == null) {
        mapped.put(rawPort, new ServerImpl().withUrl(makeUrl(port, null, null)));
      } else {
        for (String ref : refs) {
          ServerConfig cfg = configs.get(ref);
          mapped.put(
              ref, new ServerImpl().withUrl(makeUrl(port, cfg.getProtocol(), cfg.getPath())));
        }
      }
    }
    return mapped;
  }

  /**
   * Maps ports in the same way {@link #map(ContainerPort[], Map)} method does, but for convenience
   * allows to use port bindings in different format, like defined by {@link
   * NetworkSettings#getPorts()}.
   */
  public Map<String, ServerImpl> map(
      Map<String, List<PortBinding>> ports, Map<String, ServerConfig> configs) {
    if (ports == null) {
      return Collections.emptyMap();
    }
    return map(
        ports
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null && entry.getValue().size() == 1)
            .map(entry -> toContainerPort(entry.getKey(), entry.getValue().get(0)))
            .toArray(ContainerPort[]::new),
        configs);
  }

  /**
   * Makes url from given binding, protocol and path.
   *
   * <p>Examples:
   *
   * <pre>
   * |------------------------------------------------------------------------|
   * | binding           | protocol | path    | url                           |
   * |------------------------------------------------------------------------|
   * | 4011/tcp -> 32011 | http     | /api    | http://hostname:32011/api     |
   * | 4012/tcp -> 32012 | wss      | connect | wss://hostname:32012/connect  |
   * | 4013     -> 32013 | https    |         | https://hostname:32013        |
   * | 4014/upd -> 32014 |          |         | upd://hostname:32014          |
   * | 4015     -> 32015 |          |         | tcp://hostname:32015          |
   * |------------------------------------------------------------------------|
   * </pre>
   */
  private String makeUrl(ContainerPort port, String protocol, String path) {
    if (protocol == null) {
      if (port.getType() == null) {
        protocol = "tcp";
      } else {
        protocol = port.getType();
      }
    }

    // null -> "", "path" -> "/path"
    if (path == null) {
      path = "";
    } else if (!path.startsWith("/")) {
      path = '/' + path;
    }

    return protocol + "://" + hostname + ':' + port.getPublicPort() + path;
  }

  private ContainerPort toContainerPort(String rawPort, PortBinding binding) {
    ContainerPort result = new ContainerPort();
    result.setPublicPort(Integer.parseInt(binding.getHostPort()));
    int slashIdx = rawPort.indexOf('/');
    if (slashIdx != -1) {
      result.setType(rawPort.substring(slashIdx + 1));
      result.setPrivatePort(Integer.parseInt(rawPort.substring(0, slashIdx)));
    } else {
      result.setType("tcp");
      result.setPrivatePort(Integer.parseInt(rawPort));
    }
    return result;
  }
}

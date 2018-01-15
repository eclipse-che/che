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
package org.eclipse.che.workspace.infrastructure.docker.server.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.infrastructure.docker.client.json.ContainerPort;
import org.eclipse.che.infrastructure.docker.client.json.NetworkSettings;
import org.eclipse.che.infrastructure.docker.client.json.PortBinding;

/**
 * Maps container ports bindings to machine servers.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
public class ServersMapper {

  private final String hostname;
  private final String machineName;

  /**
   * Creates mapper using given {@code hostname} as hostname for all the servers urls which are
   * publicly published. For workspace-wide available servers provided {@code machineName} is used
   * as a hostname.
   */
  public ServersMapper(String hostname, String machineName) {
    this.hostname = hostname;
    this.machineName = machineName;
  }

  /**
   * Maps container ports to machine servers resolving references from the given configuration map.
   *
   * @param ports container ports to map
   * @param configs servers configuration map used to resolve server references
   * @return server reference -> server map. Note that if there is no server configuration for
   *     container bound port, port+type itself(like 4022/tcp) will be used as a reference
   */
  public Map<String, ServerImpl> map(ContainerPort[] ports, Map<String, ServerConfig> configs)
      throws InternalInfrastructureException {
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

      String hostname;
      if (port.getPublicPort() == 0) {
        hostname = machineName;
      } else {
        hostname = this.hostname;
      }

      // if there is no matching ServerConfig we do not show it as Server
      if (refs != null) {
        for (String ref : refs) {
          ServerConfig cfg = configs.get(ref);
          mapped.put(
              ref,
              new ServerImpl()
                  .withUrl(makeUrl(port, cfg.getProtocol(), cfg.getPath(), hostname))
                  .withAttributes(cfg.getAttributes()));
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
      Map<String, List<PortBinding>> ports, Map<String, ServerConfig> configs)
      throws InternalInfrastructureException {
    if (ports == null) {
      return Collections.emptyMap();
    }
    return map(
        ports
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() == null || entry.getValue().size() == 1)
            .map(entry -> toContainerPort(entry.getKey(), entry.getValue()))
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
  private String makeUrl(ContainerPort port, String protocol, String path, String hostname)
      throws InternalInfrastructureException {
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

    int serverPort = port.getPublicPort() != 0 ? port.getPublicPort() : port.getPrivatePort();

    try {
      return new URI(protocol, null, hostname, serverPort, path, null, null).toString();
    } catch (URISyntaxException e) {
      throw new InternalInfrastructureException(
          "Constructing of URI of server failed. Error: " + e.getLocalizedMessage());
    }
  }

  /**
   * Creates {@link ContainerPort} from provided port information. When {@code binding} is {@code
   * null} result represents workspace-wide accessible port, otherwise publicly available.
   */
  private ContainerPort toContainerPort(String rawPort, @Nullable List<PortBinding> binding) {
    ContainerPort result = new ContainerPort();
    if (binding != null) {
      result.setPublicPort(Integer.parseInt(binding.get(0).getHostPort()));
    }
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

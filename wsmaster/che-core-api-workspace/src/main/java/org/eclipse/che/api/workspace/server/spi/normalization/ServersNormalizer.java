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

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;

/**
 * Normalizes servers of {@link InternalEnvironment}.
 *
 * <p>Normalization includes:
 *
 * <ul>
 *   <li>Setting default protocol to `tcp` if it is absent in port configuration
 * </ul>
 *
 * @author Sergii Leshchenko
 */
public class ServersNormalizer {

  public void normalize(InternalEnvironment internalEnvironment) throws InfrastructureException {
    for (InternalMachineConfig machineConfig : internalEnvironment.getMachines().values()) {
      Map<String, ServerConfig> normalizedServers = normalizeServers(machineConfig.getServers());

      machineConfig.getServers().putAll(normalizedServers);
    }
  }

  private Map<String, ServerConfig> normalizeServers(Map<String, ? extends ServerConfig> servers) {
    return servers
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, e -> normalizeServer(e.getValue())));
  }

  private ServerConfig normalizeServer(ServerConfig serverConfig) {
    String port = serverConfig.getPort();
    if (port != null && !port.contains("/")) {
      port = port + "/tcp";
    }
    return new ServerConfigImpl(port, serverConfig.getProtocol(), serverConfig.getPath());
  }
}

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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;

/**
 * Helps to build service that expose servers.
 *
 * @author Sergii Leshchenko
 */
public class ServerServiceBuilder {

  private String name;
  private String machineName;
  private final Map<String, String> selector = new HashMap<>();
  private List<ServicePort> ports = Collections.emptyList();
  private Map<String, ? extends ServerConfig> serversConfigs = Collections.emptyMap();

  public ServerServiceBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public ServerServiceBuilder withSelectorEntry(String key, String value) {
    selector.put(key, value);
    return this;
  }

  public ServerServiceBuilder withPorts(List<ServicePort> ports) {
    this.ports = ports;
    return this;
  }

  public ServerServiceBuilder withServers(Map<String, ? extends ServerConfig> serversConfigs) {
    this.serversConfigs = serversConfigs;
    return this;
  }

  public ServerServiceBuilder withMachineName(String machineName) {
    this.machineName = machineName;
    return this;
  }

  public Service build() {
    io.fabric8.kubernetes.api.model.ServiceBuilder builder =
        new io.fabric8.kubernetes.api.model.ServiceBuilder();
    return builder
        .withNewMetadata()
        .withName(name.replace("/", "-"))
        .withAnnotations(
            Annotations.newSerializer()
                .servers(serversConfigs)
                .machineName(machineName)
                .annotations())
        .endMetadata()
        .withNewSpec()
        .withSelector(selector)
        .withPorts(ports)
        .endSpec()
        .build();
  }
}

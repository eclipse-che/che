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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.fabric8.kubernetes.api.model.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.RuntimeServerBuilder;

public abstract class AbstractServerResolver implements ServerResolver {
  private final Multimap<String, Service> services;

  public AbstractServerResolver(Iterable<Service> services) {
    this.services = ArrayListMultimap.create();
    for (Service service : services) {
      String machineName =
          Annotations.newDeserializer(service.getMetadata().getAnnotations()).machineName();
      this.services.put(machineName, service);
    }
  }

  /**
   * Resolves servers by the specified machine name.
   *
   * @param machineName machine to resolve servers
   * @return resolved servers
   */
  @Override
  public final Map<String, ServerImpl> resolve(String machineName) {
    Map<String, ServerImpl> servers = new HashMap<>();
    servers.putAll(resolveInternalServers(machineName));
    servers.putAll(resolveExternalServers(machineName));
    return servers;
  }

  private Map<String, ServerImpl> resolveInternalServers(String machineName) {
    return services
        .get(machineName)
        .stream()
        .map(this::resolveServiceServers)
        .flatMap(m -> m.entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2));
  }

  private Map<String, ServerImpl> resolveServiceServers(Service service) {
    return Annotations.newDeserializer(service.getMetadata().getAnnotations())
        .servers()
        .entrySet()
        .stream()
        .collect(
            Collectors.toMap(
                Entry::getKey,
                e ->
                    new RuntimeServerBuilder()
                        .protocol(e.getValue().getProtocol())
                        .host(service.getMetadata().getName())
                        .port(e.getValue().getPort())
                        .path(e.getValue().getPath())
                        .attributes(e.getValue().getAttributes())
                        .targetPort(e.getValue().getPort())
                        .build(),
                (s1, s2) -> s2));
  }

  protected abstract Map<String, ServerImpl> resolveExternalServers(String machineName);
}

/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.RuntimeServerBuilder;

/** Resolves servers from ConfigMaps, used with Gateway based single-host */
public class ConfigMapServerResolver extends AbstractServerResolver {

  private final Multimap<String, ConfigMap> configMaps;
  private final String cheHost;
  private final ServerResolver nativeServerResolver;

  public ConfigMapServerResolver(
      Iterable<Service> services,
      Iterable<ConfigMap> configMaps,
      String cheHost,
      ServerResolver nativeServerResolver) {
    super(services);
    this.nativeServerResolver = nativeServerResolver;
    this.cheHost = cheHost;

    this.configMaps = ArrayListMultimap.create();
    for (ConfigMap configMap : configMaps) {
      String machineName =
          Annotations.newDeserializer(configMap.getMetadata().getAnnotations()).machineName();
      if (machineName != null) {
        this.configMaps.put(machineName, configMap);
      }
    }
  }

  @Override
  public Map<String, ServerImpl> resolveExternalServers(String machineName) {
    Map<String, ServerImpl> serverMap = new HashMap<>();
    serverMap.putAll(nativeServerResolver.resolveExternalServers(machineName));
    serverMap.putAll(
        configMaps
            .get(machineName)
            .stream()
            .map(this::fillGatewayServers)
            .flatMap(m -> m.entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (s1, s2) -> s2)));
    return serverMap;
  }

  private Map<String, ServerImpl> fillGatewayServers(ConfigMap configMap) {
    return Annotations.newDeserializer(configMap.getMetadata().getAnnotations())
        .servers()
        .entrySet()
        .stream()
        .collect(
            Collectors.toMap(
                Entry::getKey,
                e -> {
                  boolean requiresSubdomain = e.getValue().isRequireSubdomain();
                  String root = requiresSubdomain ? "/" : e.getValue().getEndpointOrigin();
                  String path = buildPath(root, e.getValue().getPath());

                  return new RuntimeServerBuilder()
                      .protocol(e.getValue().getProtocol())
                      .host(cheHost)
                      .path(path)
                      .endpointOrigin(root)
                      .attributes(e.getValue().getAttributes())
                      .targetPort(e.getValue().getPort())
                      .build();
                },
                (s1, s2) -> s2));
  }
}

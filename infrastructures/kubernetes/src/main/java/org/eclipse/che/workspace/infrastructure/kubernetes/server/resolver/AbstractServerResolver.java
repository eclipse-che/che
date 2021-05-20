/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.fabric8.kubernetes.api.model.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.RuntimeServerBuilder;

/**
 * {@link ServerResolver} implementations that uses {@link Service} for internal servers can use
 * this abstract class. The implementation then must define how to resolve external servers by
 * implementing {@link AbstractServerResolver#resolveExternalServers(String)}.
 */
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
   * Joins together the two URL path fragments together and makes sure the returned path ends with a
   * slash.
   *
   * @param fragment1 the root path fragment
   * @param fragment2 the sub-path fragment
   * @return the two path fragments joined together
   */
  protected static String buildPath(String fragment1, @Nullable String fragment2) {
    StringBuilder sb = new StringBuilder(fragment1);

    if (!isNullOrEmpty(fragment2)) {
      if (!fragment1.endsWith("/")) {
        sb.append('/');
      }

      if (fragment2.startsWith("/")) {
        sb.append(fragment2.substring(1));
      } else {
        sb.append(fragment2);
      }
    }

    // always end server URLs with a slash, so that they can be safely sub-path'd..
    if (sb.charAt(sb.length() - 1) != '/') {
      sb.append('/');
    }

    return sb.toString();
  }

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
        .flatMap(s -> s.entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (s1, s2) -> s2));
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
                    // this is only used for internal servers, for which it doesn't make sense to be
                    // secure. Therefore we don't
                    // define the authOrigin() on these...
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
}

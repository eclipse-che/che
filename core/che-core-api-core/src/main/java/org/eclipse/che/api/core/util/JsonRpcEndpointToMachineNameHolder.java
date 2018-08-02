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
package org.eclipse.che.api.core.util;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;

@Singleton
public class JsonRpcEndpointToMachineNameHolder {
  private final Map<String, Set<String>> endpointIds = new ConcurrentHashMap<>();

  @Inject
  private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("event:environment-output:subscribe-by-machine-name")
        .paramsAsString()
        .noResult()
        .withBiConsumer(
            (endpointId, workspaceIdPlusMachineName) -> {
              endpointIds.putIfAbsent(endpointId, newConcurrentHashSet());
              endpointIds.get(endpointId).add(workspaceIdPlusMachineName);
            });
  }

  @Inject
  private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("event:environment-output:un-subscribe-by-machine-name")
        .paramsAsString()
        .noResult()
        .withBiConsumer(
            (endpointId, workspaceIdPlusMachineName) -> {
              endpointIds.getOrDefault(endpointId, emptySet()).remove(workspaceIdPlusMachineName);
              endpointIds.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            });
  }

  public Set<String> getEndpointIdsByWorkspaceIdPlusMachineName(String workspaceIdPlusMachineName) {
    return endpointIds
        .entrySet()
        .stream()
        .filter(it -> it.getValue().contains(workspaceIdPlusMachineName))
        .map(Map.Entry::getKey)
        .collect(toSet());
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
public class JsonRpcEndpointIdsHolder {
  private final Map<String, Set<String>> endpointIds = new ConcurrentHashMap<>();

  @Inject
  private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("event:ws-agent-output:subscribe")
        .paramsAsString()
        .noResult()
        .withBiConsumer(
            (endpointId, workspaceId) -> {
              endpointIds.putIfAbsent(endpointId, newConcurrentHashSet());
              endpointIds.get(endpointId).add(workspaceId);
            });
  }

  @Inject
  private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("event:ws-agent-output:un-subscribe")
        .paramsAsString()
        .noResult()
        .withBiConsumer(
            (endpointId, workspaceId) -> {
              endpointIds.getOrDefault(endpointId, emptySet()).remove(workspaceId);
              endpointIds.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            });
  }

  public Set<String> getEndpointIdsByWorkspaceId(String workspaceId) {
    return endpointIds
        .entrySet()
        .stream()
        .filter(it -> it.getValue().contains(workspaceId))
        .map(Map.Entry::getKey)
        .collect(toSet());
  }
}

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
package org.eclipse.che.ide.api.command.exec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.eclipse.che.agent.exec.shared.dto.DtoWithPid;

public abstract class AbstractExecAgentEventHandler<P extends DtoWithPid>
    implements BiConsumer<String, P> {

  private final Map<String, Set<Consumer<P>>> operationRegistry = new HashMap<>();

  protected void handle(String endpointId, P params) {
    int pid = params.getPid();
    String key = endpointId + '@' + pid;

    if (!operationRegistry.containsKey(key)) {
      return;
    }

    for (Consumer<P> consumer : operationRegistry.get(key)) {
      consumer.accept(params);
    }
  }

  public void registerConsumer(String endpointId, int pid, Consumer<P> consumer) {
    String key = endpointId + '@' + pid;
    if (!operationRegistry.containsKey(key)) {
      operationRegistry.put(key, new HashSet<>());
    }

    operationRegistry.get(key).add(consumer);
  }

  public void unregisterConsumer(String endpointId, int pid, Consumer<P> consumer) {
    String key = endpointId + '@' + pid;
    if (operationRegistry.containsKey(key)) {
      operationRegistry.get(key).remove(consumer);
    }
  }

  public void unregisterConsumers(String endpointId, int pid) {
    String key = endpointId + '@' + pid;
    operationRegistry.remove(key);
  }
}

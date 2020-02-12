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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodLogHandlerToEventPublisher implements PodLogHandler {

  private final RuntimeEventsPublisher eventsPublisher;
  private final RuntimeIdentity identity;
  private final KubernetesMachineCache machines;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  public PodLogHandlerToEventPublisher(
      RuntimeEventsPublisher eventsPublisher,
      RuntimeIdentity identity,
      KubernetesMachineCache machines) {
    this.eventsPublisher = eventsPublisher;
    this.identity = identity;
    this.machines = machines;
  }

  @Override
  public boolean matchPod(String podName) {
    try {
      return machines.getMachines(identity).values().stream()
          .filter(m -> m.getPodName() != null)
          .anyMatch(m -> m.getPodName().equals(podName));
    } catch (InfrastructureException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean handle(PrefixedPipedInputStream is) {
    LOG.info("start watchin log");
    try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
      String line;
      while ((line = in.readLine()) != null) {
        LOG.info("[{}] -> {}", is.prefix(), line);
        if (line.contains("\"code\":40")) {
          LOG.info("failed to get the logs, should try again");
          return false;
        }
        eventsPublisher.sendRuntimeLogEvent(
            String.format("[%s] -> %s", is.prefix(), line),
            ZonedDateTime.now().toString(),
            identity);
      }
      LOG.info("endehere, done, finito");
    } catch (IOException e) {
      LOG.info("ended beautifuly eh ?", e);
    }
    return true;
  }
}

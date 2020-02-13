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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.time.ZonedDateTime;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class is responsible for reading the logs. It is aware of machines it should follow. */
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

  /**
   * Check if given pod is in interest of this log handler.
   *
   * @param podName pod to check
   * @return true if this class cares about given podName, false otherwise
   */
  @Override
  public boolean matchPod(String podName) {
    try {
      return machines
          .getMachines(identity)
          .values()
          .stream()
          .filter(m -> m.getPodName() != null)
          .anyMatch(m -> m.getPodName().equals(podName));
    } catch (InfrastructureException e) {
      LOG.error(
          "Failed to get the machines when checking whether LogHandler do care about pod [{}]. Not much to do here.",
          podName,
          e);
      return false;
    }
  }

  /**
   * Read the logs from given inputStream. It recognizes if received message has error state and
   * returns false immediately in that case. When there is no error, this method keeps reading the
   * logs from given inputStream, which is blocking operation.
   *
   * <p>Method can't recognize intentional close, which is "Broken pipe" IOException, and real
   * communication failure. It returns true in both case, which is considered as finished
   * communication (which is ok maybe).
   *
   * @param inputStream to read from
   * @param containerName source container of the inputStream
   * @return false if error message. true at the end of the stream or interrupted stream
   */
  @Override
  public boolean handle(InputStream inputStream, String containerName) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
      String logMessage;
      while ((logMessage = in.readLine()) != null) {
        if (logMessage.contains("\"code\":40")) {
          LOG.debug("failed to get the logs, should try again");
          return false;
        } else {
          eventsPublisher.sendRuntimeLogEvent(
              String.format("[%s] -> %s", containerName, logMessage),
              ZonedDateTime.now().toString(),
              identity);
        }
      }
    } catch (IOException e) {
      // TODO: do more clever cut-off
      LOG.debug("End of watching log. It could be either intended or some connection failure.");
    }
    return true;
  }
}

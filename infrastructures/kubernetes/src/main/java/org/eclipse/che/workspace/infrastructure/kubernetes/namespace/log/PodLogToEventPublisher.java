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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import java.time.ZonedDateTime;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class is responsible for reading the logs. It is aware of machines it should follow. */
public class PodLogToEventPublisher implements PodLogHandler {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final String LOG_MESSAGE_FORMAT = "[%s] -> %s";

  private final RuntimeEventsPublisher eventsPublisher;
  private final RuntimeIdentity identity;

  public PodLogToEventPublisher(RuntimeEventsPublisher eventsPublisher, RuntimeIdentity identity) {
    this.eventsPublisher = eventsPublisher;
    this.identity = identity;
  }

  /**
   * Receives the message, formats it and send it to {@link PodLogToEventPublisher#eventsPublisher}
   *
   * @param message to handle
   * @param containerName source container of the log message
   */
  @Override
  public void handle(String message, String containerName) {
    LOG.trace("forwarding message '{}' from the container '{}'", message, containerName);
    eventsPublisher.sendRuntimeLogEvent(
        String.format(LOG_MESSAGE_FORMAT, containerName, message),
        ZonedDateTime.now().toString(),
        identity);
  }
}

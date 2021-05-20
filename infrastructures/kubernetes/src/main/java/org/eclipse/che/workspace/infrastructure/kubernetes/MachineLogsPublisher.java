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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.slf4j.LoggerFactory.getLogger;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;

/** Listens pod events and publish them as machine logs. */
public class MachineLogsPublisher implements PodEventHandler {

  private static final Logger LOG = getLogger(MachineLogsPublisher.class);

  private final RuntimeEventsPublisher eventPublisher;
  private final KubernetesMachineCache machines;
  private final RuntimeIdentity runtimeIdentity;

  public MachineLogsPublisher(
      RuntimeEventsPublisher eventPublisher,
      KubernetesMachineCache machines,
      RuntimeIdentity runtimeIdentity) {
    this.eventPublisher = eventPublisher;
    this.machines = machines;
    this.runtimeIdentity = runtimeIdentity;
  }

  @Override
  public void handle(PodEvent event) {
    final String podName = event.getPodName();
    try {
      for (KubernetesMachineImpl machine : machines.getMachines(runtimeIdentity).values()) {
        if (machine.getPodName().equals(podName)) {
          eventPublisher.sendMachineLogEvent(
              machine.getName(), event.getMessage(), event.getCreationTimeStamp(), runtimeIdentity);
          return;
        }
      }
    } catch (InfrastructureException e) {
      LOG.error("Error while machine fetching for logs publishing. Cause: {}", e.getMessage());
    }
  }
}

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

import java.util.Set;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;

/** Listens pod events and publish them as runtime logs. */
public class RuntimeLogsPublisher implements PodEventHandler {

  private final RuntimeEventsPublisher eventPublisher;
  private final RuntimeIdentity runtimeIdentity;
  private final Set<String> pods;

  public RuntimeLogsPublisher(
      RuntimeEventsPublisher eventPublisher, RuntimeIdentity runtimeIdentity, Set<String> pods) {
    this.eventPublisher = eventPublisher;
    this.pods = pods;
    this.runtimeIdentity = runtimeIdentity;
  }

  @Override
  public void handle(PodEvent event) {
    final String podName = event.getPodName();
    if (pods.contains(podName)) {
      eventPublisher.sendRuntimeLogEvent(
          event.getMessage(), event.getCreationTimeStamp(), runtimeIdentity);
    }
  }
}

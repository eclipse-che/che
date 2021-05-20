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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import com.google.common.base.Strings;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;

/**
 * Listens Pod events and propagates unrecoverable events via the specified handler.
 *
 * @author Sergii Leshchenko
 * @author Ilya Buziuk
 */
public class UnrecoverablePodEventListener implements PodEventHandler {

  private final Set<String> pods;
  private final Consumer<PodEvent> unrecoverableEventHandler;
  private final Set<String> unrecoverableEvents;

  public UnrecoverablePodEventListener(
      Set<String> unrecoverableEvents,
      Set<String> pods,
      Consumer<PodEvent> unrecoverableEventHandler) {
    this.unrecoverableEvents = unrecoverableEvents;
    this.pods = pods;
    this.unrecoverableEventHandler = unrecoverableEventHandler;
  }

  @Override
  public void handle(PodEvent event) {
    if (isWorkspaceEvent(event) && (isFailedContainer(event) || isUnrecoverable(event))) {
      unrecoverableEventHandler.accept(event);
    }
  }

  /** Returns true if event belongs to one of the workspace pods, false otherwise */
  private boolean isWorkspaceEvent(PodEvent event) {
    String podName = event.getPodName();
    if (Strings.isNullOrEmpty(podName)) {
      return false;
    }
    // Note it is necessary to compare via startsWith rather than equals here, as pods managed by
    // deployments have their name set as [deploymentName]-[hash]. `workspacePodName` is used to
    // define the deployment name, so pods that are created aren't an exact match.
    return pods.stream().anyMatch(podName::startsWith);
  }

  /**
   * Returns true if event reason or message matches one of the comma separated values defined in
   * 'che.infra.kubernetes.workspace_unrecoverable_events',false otherwise
   *
   * @param event event to check
   */
  private boolean isUnrecoverable(PodEvent event) {
    boolean isUnrecoverable = false;
    String reason = event.getReason();
    String message = event.getMessage();
    // Consider unrecoverable if event reason 'equals' one of the property values e.g. "Failed
    // Mount"
    if (unrecoverableEvents.contains(reason)) {
      isUnrecoverable = true;
    } else {
      for (String e : unrecoverableEvents) {
        // Consider unrecoverable if event message 'startsWith' one of the property values e.g.
        // "Failed to pull image"
        if (message != null && message.startsWith(e)) {
          isUnrecoverable = true;
        }
      }
    }
    return isUnrecoverable;
  }

  /**
   * This method detects whether the pod event corresponds to a container that failed to start. Note
   * that this is handled differently from the the {@link #isUnrecoverable(PodEvent)} because we are
   * specifically looking at failed containers and don't consider the event message in the logic.
   *
   * @param event an event on the pod
   * @return true if the event is about a failed container, false otherwise
   */
  private boolean isFailedContainer(PodEvent event) {
    return "Failed".equals(event.getReason()) && event.getContainerName() != null;
  }
}

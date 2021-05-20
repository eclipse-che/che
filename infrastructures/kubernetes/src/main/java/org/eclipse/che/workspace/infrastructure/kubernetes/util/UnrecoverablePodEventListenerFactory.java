/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;

/**
 * Helps to create {@link UnrecoverablePodEventListener} instaces.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class UnrecoverablePodEventListenerFactory {

  private final Set<String> unrecoverableEvents;

  @Inject
  public UnrecoverablePodEventListenerFactory(
      @Named("che.infra.kubernetes.workspace_unrecoverable_events") String[] unrecoverableEvents) {
    this.unrecoverableEvents = ImmutableSet.copyOf(unrecoverableEvents);
  }

  /**
   * Creates unrecoverable events listener.
   *
   * @param pods pods which unrecoverable events should be propagated
   * @param unrecoverableEventHandler handler which is invoked when unrecoverable event occurs
   * @return created unrecoverable events listener
   * @throws IllegalStateException is unrecoverable events are not configured.
   * @see #isConfigured()
   */
  public UnrecoverablePodEventListener create(
      Set<String> pods, Consumer<PodEvent> unrecoverableEventHandler) {
    if (!isConfigured()) {
      throw new IllegalStateException("Unrecoverable events are not configured");
    }

    return new UnrecoverablePodEventListener(unrecoverableEvents, pods, unrecoverableEventHandler);
  }

  public UnrecoverablePodEventListener create(
      KubernetesEnvironment environment, Consumer<PodEvent> unrecoverableEventHandler) {
    if (!isConfigured()) {
      throw new IllegalStateException("Unrecoverable events are not configured");
    }

    Set<String> toWatch =
        environment
            .getPodsData()
            .values()
            .stream()
            .map(podData -> podData.getMetadata().getName())
            .collect(Collectors.toSet());

    return new UnrecoverablePodEventListener(
        unrecoverableEvents, toWatch, unrecoverableEventHandler);
  }

  /**
   * Returns true if unrecoverable events are configured and it's possible to create unrecoverable
   * events listener, false otherwise
   */
  public boolean isConfigured() {
    return !unrecoverableEvents.isEmpty();
  }
}

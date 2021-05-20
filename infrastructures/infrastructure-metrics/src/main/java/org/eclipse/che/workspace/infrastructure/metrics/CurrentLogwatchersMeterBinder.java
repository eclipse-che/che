/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.event.WatchLogStartedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.event.WatchLogStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Counts sent messages and bytes to runtime log by listening to {@link RuntimeLogEvent}s. */
@Singleton
public class CurrentLogwatchersMeterBinder implements MeterBinder {

  private static final Logger LOG = LoggerFactory.getLogger(CurrentLogwatchersMeterBinder.class);

  private final EventService eventService;
  private final AtomicLong currentWatchersCounter;

  @Inject
  CurrentLogwatchersMeterBinder(EventService eventService) {
    this.eventService = eventService;
    this.currentWatchersCounter = new AtomicLong();
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    Gauge.builder("log_watchers", this::current).register(registry);

    eventService.subscribe(
        (e) -> currentWatchersCounter.incrementAndGet(), WatchLogStartedEvent.class);
    eventService.subscribe(
        (e) -> {
          long counter = currentWatchersCounter.decrementAndGet();
          if (counter < 0) {
            LOG.warn(
                "WatchLog current instances counter decremented below 0. Counter set explicitly to 0. This should not happen. Please report a bug if you see this message in the log.");
            currentWatchersCounter.set(0);
          }
        },
        WatchLogStoppedEvent.class);
  }

  private long current() {
    return currentWatchersCounter.get();
  }
}

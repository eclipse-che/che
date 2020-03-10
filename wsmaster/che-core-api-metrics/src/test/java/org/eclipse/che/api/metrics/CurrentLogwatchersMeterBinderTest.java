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
package org.eclipse.che.api.metrics;

import static org.testng.Assert.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.workspace.infrastructure.metrics.event.WatchLogStartedEvent;
import org.eclipse.che.workspace.infrastructure.metrics.event.WatchLogStoppedEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CurrentLogwatchersMeterBinderTest {

  private final String metricsKey = "log_watchers";

  private MeterRegistry registry;
  private EventService eventService;
  private CurrentLogwatchersMeterBinder binder;

  @BeforeMethod
  public void setUp() {
    registry = new SimpleMeterRegistry();
    eventService = new EventService();
    binder = new CurrentLogwatchersMeterBinder(eventService);
    binder.bindTo(registry);
  }

  @Test
  public void testCurrentLogwatcherGaugeReactsOnEvents() {
    assertEquals(registry.get(metricsKey).gauge().value(), 0.0);

    eventService.publish(new WatchLogStartedEvent("container"));
    assertEquals(registry.get(metricsKey).gauge().value(), 1.0);

    eventService.publish(new WatchLogStartedEvent("container"));
    assertEquals(registry.get(metricsKey).gauge().value(), 2.0);

    eventService.publish(new WatchLogStoppedEvent("container"));
    assertEquals(registry.get(metricsKey).gauge().value(), 1.0);

    eventService.publish(new WatchLogStartedEvent("container"));
    assertEquals(registry.get(metricsKey).gauge().value(), 2.0);

    eventService.publish(new WatchLogStoppedEvent("container"));
    eventService.publish(new WatchLogStoppedEvent("container"));
    assertEquals(registry.get(metricsKey).gauge().value(), 0.0);
  }

  @Test
  public void testLogwatcherGaugeCantGoBelowZero() {
    assertEquals(registry.get(metricsKey).gauge().value(), 0.0);

    eventService.publish(new WatchLogStoppedEvent("container"));
    assertEquals(registry.get(metricsKey).gauge().value(), 0.0);

    eventService.publish(new WatchLogStartedEvent("container"));
    eventService.publish(new WatchLogStoppedEvent("container"));
    eventService.publish(new WatchLogStoppedEvent("container"));
    assertEquals(registry.get(metricsKey).gauge().value(), 0.0);
  }
}

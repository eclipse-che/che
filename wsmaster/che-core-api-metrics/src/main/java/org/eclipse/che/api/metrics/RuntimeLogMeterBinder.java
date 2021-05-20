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
package org.eclipse.che.api.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;

/** Counts sent messages and bytes to runtime log by listening to {@link RuntimeLogEvent}s. */
@Singleton
public class RuntimeLogMeterBinder implements MeterBinder {

  private final EventService eventService;

  private Counter messages;
  private Counter bytes;

  @Inject
  RuntimeLogMeterBinder(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    messages =
        Counter.builder("runtime_log_messages")
            .baseUnit("message")
            .description("number of sent messages to runtime log")
            .register(registry);
    bytes =
        Counter.builder("runtime_log_bytes")
            .baseUnit("byte")
            .description("number of sent bytes to runtime log")
            .register(registry);

    eventService.subscribe(
        (e) -> {
          messages.increment();
          bytes.increment(e.getText().getBytes(StandardCharsets.UTF_8).length);
        },
        RuntimeLogEvent.class);
  }
}

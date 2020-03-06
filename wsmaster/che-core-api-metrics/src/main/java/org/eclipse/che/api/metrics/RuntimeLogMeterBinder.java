package org.eclipse.che.api.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
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
          bytes.increment(e.getText().getBytes().length);
        },
        RuntimeLogEvent.class);
  }
}

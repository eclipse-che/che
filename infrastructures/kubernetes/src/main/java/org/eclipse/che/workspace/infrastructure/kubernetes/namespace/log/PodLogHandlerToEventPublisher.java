package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import io.fabric8.kubernetes.client.dsl.LogWatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodLogHandlerToEventPublisher implements PodLogHandler {

  private final RuntimeEventsPublisher eventsPublisher;
  private final RuntimeIdentity identity;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  public PodLogHandlerToEventPublisher(
      RuntimeEventsPublisher eventsPublisher,
      RuntimeIdentity identity) {
    this.eventsPublisher = eventsPublisher;
    this.identity = identity;
  }

  @Override
  public void handle(LogWatch log, String podName, String containerName) {
    LOG.info("start watchin log");
    try (BufferedReader in = new BufferedReader(new InputStreamReader(log.getOutput()))) {
      String line;
      while ((line = in.readLine()) != null) {
        LOG.info("[{}: {}] -> {}", podName, containerName, line);
        eventsPublisher.sendRuntimeLogEvent(
            String.format("[%s: %s] -> %s", podName, containerName, line),
            ZonedDateTime.now().toString(),
            identity);
      }
      LOG.info("endehere, done, finito");
    } catch (IOException e) {
      LOG.info("ended beautifuly eh ?", e);
    }
  }
}

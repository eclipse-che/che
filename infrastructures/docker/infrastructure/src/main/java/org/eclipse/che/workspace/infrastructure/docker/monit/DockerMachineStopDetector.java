/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.monit;

import static java.lang.String.format;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.MessageProcessor;
import org.eclipse.che.infrastructure.docker.client.json.Event;
import org.eclipse.che.infrastructure.docker.client.json.Filters;
import org.eclipse.che.infrastructure.docker.client.params.GetEventsParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Track docker containers events to detect containers stop or failure.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DockerMachineStopDetector {
  private static final Logger LOG = LoggerFactory.getLogger(DockerMachineStopDetector.class);

  private final DockerConnector dockerConnector;
  private final ExecutorService executorService;
  private final Map<String, ContainerDeathHandlerHolder> handlers;
  /*
     Helps differentiate container main process OOM from other processes OOM
     Algorithm:
     1) put container id to cache if OOM was detected
     2) on container DIE event check if container id is cached that indicates
     that OOM for this container was detected.
     3) if container id is in the cache fire OOM event otherwise fire die event
     4) if die was detected later than X seconds after OOM was detected
     we consider this OOM as OOM of non-main process of container.
     That's why cache expires in X seconds.
     X was set as 10 empirically.
  */
  private final Cache<String, String> containersOomTimestamps;

  private long lastProcessedEventDate = 0;

  @Inject
  public DockerMachineStopDetector(DockerConnector dockerConnector) {
    this.dockerConnector = dockerConnector;
    this.handlers = new ConcurrentHashMap<>();
    this.containersOomTimestamps =
        CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    this.executorService =
        Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                .setNameFormat("DockerMachineStopDetector-%d")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setDaemon(true)
                .build());
  }

  /**
   * Start container stop detection.
   *
   * @param containerId id of a container to start detection for
   * @param machineName name of machine represented by provided container
   * @param handler handler that should be called when machine abnormal stop is detected
   */
  public void startDetection(
      String containerId, String machineName, AbnormalMachineStopHandler handler) {
    handlers.put(containerId, new ContainerDeathHandlerHolder(machineName, handler));
  }

  /**
   * Stop container stop detection.
   *
   * @param containerId id of a container to start detection for
   */
  public void stopDetection(String containerId) {
    handlers.remove(containerId);
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void detectContainersEvents() {
    executorService.execute(
        () -> {
          // noinspection InfiniteLoopStatement
          while (true) {
            try {
              dockerConnector.getEvents(
                  GetEventsParams.create()
                      .withSinceSecond(lastProcessedEventDate)
                      .withFilters(new Filters().withFilter("event", "die", "oom")),
                  new EventsProcessor());
            } catch (IOException e) {
              // usually connection timeout
              LOG.debug(e.getLocalizedMessage(), e);
            }
          }
        });
  }

  private class EventsProcessor implements MessageProcessor<Event> {
    @Override
    public void process(Event message) {
      if (message.getType() != null && !"container".equals(message.getType())) {
        // this check is added because of bug in the docker swarm which do not filter events
        // in case of new response format of 'get events' we should skip all not filtered by swarm
        // event types
        return;
      }

      switch (message.getStatus()) {
        case "oom":
          containersOomTimestamps.put(message.getId(), message.getId());
          LOG.debug("OOM of process in container {} has been detected", message.getId());
          break;
        case "die":
          String stopReason;
          if (containersOomTimestamps.getIfPresent(message.getId()) != null) {
            containersOomTimestamps.invalidate(message.getId());
            stopReason = "OOM of main process of container was detected.";
          } else {
            stopReason =
                "Please, check that container is designed to run in non-interactive terminal.";
          }
          lastProcessedEventDate = message.getTime();
          ContainerDeathHandlerHolder holder = handlers.get(message.getId());
          if (holder != null) {
            holder.handler.handle(
                format(
                    "Container of machine '%s' unexpectedly stopped. %s",
                    holder.machineName, stopReason));
          }
          break;
        default:
          // we don't care about other event types
      }
    }
  }

  private static class ContainerDeathHandlerHolder {
    String machineName;
    AbnormalMachineStopHandler handler;

    public ContainerDeathHandlerHolder(String machineName, AbnormalMachineStopHandler handler) {
      this.machineName = machineName;
      this.handler = handler;
    }
  }
}

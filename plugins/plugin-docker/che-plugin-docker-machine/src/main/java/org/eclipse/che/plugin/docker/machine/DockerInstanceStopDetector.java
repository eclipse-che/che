/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.event.InstanceStateEvent;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.plugin.docker.client.json.Event;
import org.eclipse.che.plugin.docker.client.json.Filters;
import org.eclipse.che.plugin.docker.client.params.GetEventsParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Track docker containers events to detect containers stop or failure.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DockerInstanceStopDetector {
    private static final Logger LOG = LoggerFactory.getLogger(DockerInstanceStopDetector.class);

    private final EventService          eventService;
    private final DockerConnector       dockerConnector;
    private final ExecutorService       executorService;
    private final Map<String, String>   instances;
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
    public DockerInstanceStopDetector(EventService eventService, DockerConnector dockerConnector) {
        this.eventService = eventService;
        this.dockerConnector = dockerConnector;
        this.instances = new ConcurrentHashMap<>();
        this.containersOomTimestamps = CacheBuilder.newBuilder()
                                                   .expireAfterWrite(10, TimeUnit.SECONDS)
                                                   .build();
        this.executorService = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setNameFormat("DockerInstanceStopDetector-%d")
                                          .setDaemon(true)
                                          .build());
    }

    /**
     * Start container stop detection.
     *
     * @param containerId
     *         id of a container to start detection for
     * @param machineId
     *         id of a machine which container implements
     */
    public void startDetection(String containerId, String machineId) {
        instances.put(containerId, machineId);
    }

    /**
     * Stop container stop detection.
     *
     * @param containerId
     *         id of a container to start detection for
     */
    public void stopDetection(String containerId) {
        instances.remove(containerId);
    }

    @PostConstruct
    private void detectContainersEvents() {
        executorService.execute(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    dockerConnector.getEvents(GetEventsParams.create()
                                                             .withSinceSecond(lastProcessedEventDate)
                                                             .withUntilSecond(0)
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
                // in case of new response format of 'get events' we should skip all not filtered by swarm event types
                return;
            }

            switch (message.getStatus()) {
                case "oom":
                    containersOomTimestamps.put(message.getId(), message.getId());
                    LOG.info("OOM of process in container {} has been detected", message.getId());
                    break;
                case "die":
                    InstanceStateEvent.Type instanceStateChangeType;
                    if (containersOomTimestamps.getIfPresent(message.getId()) != null) {
                        instanceStateChangeType = InstanceStateEvent.Type.OOM;
                        containersOomTimestamps.invalidate(message.getId());
                        LOG.info("OOM of container '{}' has been detected", message.getId());
                    } else {
                        instanceStateChangeType = InstanceStateEvent.Type.DIE;
                    }
                    final String instanceId = instances.get(message.getId());
                    if (instanceId != null) {
                        eventService.publish(new InstanceStateEvent(instanceId, instanceStateChangeType));
                        lastProcessedEventDate = message.getTime();
                    }
                    break;
                default:
                    // we don't care about other event types
            }
        }
    }
}

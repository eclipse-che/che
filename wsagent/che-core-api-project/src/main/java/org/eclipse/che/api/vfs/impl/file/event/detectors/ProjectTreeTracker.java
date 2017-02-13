/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.impl.file.event.detectors;

import org.eclipse.che.api.core.jsonrpc.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeStateUpdateDto;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.vfs.watcher.FileWatcherManager.EMPTY_CONSUMER;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class ProjectTreeTracker {
    private static final Logger LOG = getLogger(ProjectTreeTracker.class);

    private static final String OUTGOING_METHOD = "event:project-tree-state-changed";
    private static final String INCOMING_METHOD = "track:project-tree";

    private final Map<String, Integer> watchIdRegistry = new HashMap<>();
    private final Set<String>          timers          = newConcurrentHashSet();


    private final RequestTransmitter transmitter;
    private final FileWatcherManager fileWatcherManager;

    @Inject
    public ProjectTreeTracker(FileWatcherManager fileWatcherManager, RequestTransmitter transmitter) {
        this.fileWatcherManager = fileWatcherManager;
        this.transmitter = transmitter;
    }

    @Inject
    public void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName(INCOMING_METHOD)
                    .paramsAsDto(ProjectTreeTrackingOperationDto.class)
                    .noResult()
                    .withConsumer(getProjectTreeTrackingOperationConsumer());
    }

    private BiConsumer<String, ProjectTreeTrackingOperationDto> getProjectTreeTrackingOperationConsumer() {
        return (String endpointId, ProjectTreeTrackingOperationDto operation) -> {
            final Type type = operation.getType();
            final String path = operation.getPath();

            switch (type) {
                case START: {
                    LOG.debug("Received project tree tracking operation START trigger.");

                    int pathRegistrationId = fileWatcherManager.registerByPath(path,
                                                                               getCreateOperation(endpointId),
                                                                               getModifyConsumer(endpointId),
                                                                               getDeleteOperation(endpointId));
                    watchIdRegistry.put(path + endpointId, pathRegistrationId);
                    break;
                }
                case STOP: {
                    LOG.debug("Received project tree tracking operation STOP trigger.");

                    Predicate<Entry<String, Integer>> isSubPath = it -> it.getKey().startsWith(path) && it.getKey().endsWith(endpointId);

                    watchIdRegistry.entrySet()
                                   .stream()
                                   .filter(isSubPath)
                                   .map(Entry::getKey)
                                   .collect(toSet())
                                   .stream()
                                   .map(watchIdRegistry::remove)
                                   .forEach(fileWatcherManager::unRegisterByPath);

                    break;
                }
                case SUSPEND: {
                    LOG.debug("Received project tree tracking operation SUSPEND trigger.");

                    fileWatcherManager.suspend();

                    break;
                }
                case RESUME: {
                    LOG.debug("Received project tree tracking operation RESUME trigger.");

                    fileWatcherManager.resume();

                    break;
                }
                default: {
                    LOG.error("Received file tracking operation UNKNOWN trigger.");

                    break;
                }
            }
        };
    }

    private Consumer<String> getCreateOperation(String endpointId) {
        return it -> {
            if (timers.contains(it)) {
                timers.remove(it);
            } else {
                ProjectTreeStateUpdateDto params = newDto(ProjectTreeStateUpdateDto.class).withPath(it).withType(CREATED);
                transmitter.transmitOneToNone(endpointId, OUTGOING_METHOD, params);
            }
        };
    }

    private Consumer<String> getModifyConsumer(String endpointId) {
        return EMPTY_CONSUMER;
    }

    private Consumer<String> getDeleteOperation(String endpointId) {
        return it -> {
            timers.add(it);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (timers.contains(it)) {
                        timers.remove(it);
                        ProjectTreeStateUpdateDto params = newDto(ProjectTreeStateUpdateDto.class).withPath(it).withType(DELETED);
                        transmitter.transmitOneToNone(endpointId, OUTGOING_METHOD, params);
                    }

                }
            }, 1_000L);
        };
    }
}

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
package org.eclipse.che.api.vfs.impl.file.event.detectors;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent.EventType;
import org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto;
import org.eclipse.che.api.project.shared.dto.event.FileInVfsStatusDto;
import org.eclipse.che.api.project.shared.dto.event.FileInVfsStatusDto.Status;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto.Status.CLOSED;
import static org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto.Status.OPENED;
import static org.eclipse.che.api.project.shared.dto.event.FileInVfsStatusDto.Status.REMOVED;
import static org.eclipse.che.api.project.shared.dto.event.FileInVfsStatusDto.Status.UPDATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Dmitry Kuleshov
 */
@Beta
@Singleton
public class EditorFileStatusDetector implements HiEventDetector<FileInVfsStatusDto>,
                                                 JsonRpcRequestReceiver,
                                                 EventSubscriber<ProjectItemModifiedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(EditorFileStatusDetector.class);

    private final Map<String, Set<Integer>> endpointRegistry = new HashMap<>();
    private final Set<String>                skipping         = new HashSet<>();

    private final ProjectItemModifiedEventSubscriber subscriber = new ProjectItemModifiedEventSubscriber();
    private final NotificationReceiver               receiver   = new NotificationReceiver();

    private final JsonRpcRequestTransmitter transmitter;
    private final EventService              eventService;

    @Inject
    public EditorFileStatusDetector(JsonRpcRequestTransmitter transmitter, EventService eventService) {
        this.transmitter = transmitter;
        this.eventService = eventService;
    }

    @PostConstruct
    public void subscribe() {
        LOG.debug("Subscribing {}", ProjectItemModifiedEventSubscriber.class);
        eventService.subscribe(subscriber);
    }

    @PreDestroy
    public void unsubscribe() {
        LOG.debug("Unsubscribing {}", ProjectItemModifiedEventSubscriber.class);
        eventService.unsubscribe(subscriber);
    }

    @Override
    public Optional<HiEvent<FileInVfsStatusDto>> detect(EventTreeNode eventTreeNode) {
        if (!eventTreeNode.isRoot() || eventTreeNode.getChildren().isEmpty()) {
            return empty();
        }

        final Set<EventTreeNode> files = eventTreeNode.stream()
                                                      .filter(EventTreeNode::modificationOccurred)
                                                      .filter(EventTreeNode::isFile)
                                                      .filter(event -> endpointRegistry.keySet().contains(event.getPath()))
                                                      .collect(toSet());

        transmit(files.stream().filter(event -> MODIFIED == event.getLastEventType()).collect(toList()),
                 UPDATED,
                 true);

        transmit(files.stream().filter(event -> DELETED == event.getLastEventType()).collect(toList()),
                 REMOVED,
                 false);

        return Optional.empty();
    }

    private void transmit(List<EventTreeNode> files, Status status, boolean checkForSkipping) {
        files.forEach(file -> {
            final String path = file.getPath();

            if (checkForSkipping && skipping.remove(path)) {
                return;
            }

            final JsonRpcRequest request = newDto(JsonRpcRequest.class)
                    .withMethod("event:file-in-vfs-status-changed")
                    .withJsonrpc("2.0")
                    .withParams(newDto(FileInVfsStatusDto.class).withPath(path).withStatus(status).toString());

            endpointRegistry.get(path).forEach(endpoint -> transmitter.transmit(request, endpoint));
        });
    }

    @Override
    public void onEvent(ProjectItemModifiedEvent event) {
        subscriber.onEvent(event);
    }

    @Override
    public void receive(JsonRpcRequest request, Integer endpoint) {
        receiver.receive(request, endpoint);
    }

    private class NotificationReceiver implements JsonRpcRequestReceiver {

        @Override
        public void receive(JsonRpcRequest request, Integer endpoint) {
            final FileInEditorStatusDto dto = DtoFactory.getInstance().createDtoFromJson(request.getParams(), FileInEditorStatusDto.class);
            final FileInEditorStatusDto.Status status = dto.getStatus();
            final String path = dto.getPath();

            skipping.remove(path);

            if (status == OPENED) {
                track(endpoint, path);
            } else if (status == CLOSED) {
                untrack(endpoint, path);
            }
        }

        private void track(int endpoint, String path) {
            Optional.ofNullable(endpointRegistry.get(path))
                    .orElseGet(() -> {
                        final Set<Integer> endpoints = new HashSet<>();
                        endpointRegistry.put(path, endpoints);
                        return endpoints;
                    })
                    .add(endpoint);

        }

        private void untrack(int endpoint, String path) {
            Optional.ofNullable(endpointRegistry.get(path))
                    .ifPresent(endpoints -> endpoints.remove(endpoint));
        }
    }

    private class ProjectItemModifiedEventSubscriber implements EventSubscriber<ProjectItemModifiedEvent> {
        @Override
        public void onEvent(ProjectItemModifiedEvent event) {
            if (EventType.UPDATED == event.getType()) {
                skipping.add(event.getPath());
            }
        }
    }
}

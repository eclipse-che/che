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
import org.eclipse.che.api.project.shared.dto.event.FileClosedDto;
import org.eclipse.che.api.project.shared.dto.event.FileOpenedDto;
import org.eclipse.che.api.project.shared.dto.event.FileUpdatedDto;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 */
@Beta
@Singleton
public class OpenedFileContentUpdateEventDetector implements HiEventDetector<FileUpdatedDto>,
                                                             JsonRpcRequestReceiver,
                                                             EventSubscriber<ProjectItemModifiedEvent> {

    private static final Logger LOG = getLogger(OpenedFileContentUpdateEventDetector.class);

    //TODO improve to keep all endpoints for opened file
    private final Map<String, Integer> endpointRegistry  = new HashMap<>();
    private final Set<String>          ignoranceRegistry = new HashSet<>();

    private final JsonRpcRequestTransmitter transmitter;

    @Inject
    public OpenedFileContentUpdateEventDetector(JsonRpcRequestTransmitter transmitter, EventService eventService) {
        this.transmitter = transmitter;
        eventService.subscribe(this);
    }

    @Override
    public Optional<HiEvent<FileUpdatedDto>> detect(EventTreeNode eventTreeNode) {
        if (!eventTreeNode.isRoot() || eventTreeNode.getChildren().isEmpty()) {
            return empty();
        }

        final Collection<String> registeredFiles = endpointRegistry.keySet();

        final List<EventTreeNode> files = eventTreeNode.stream()
                                                       .filter(EventTreeNode::modificationOccurred)
                                                       .filter(EventTreeNode::isFile)
                                                       .filter(event -> registeredFiles.contains(event.getPath()))
                                                       .filter(event -> MODIFIED.equals(event.getLastEventType()))
                                                       .collect(Collectors.toList());


        for (EventTreeNode file : files) {
            final String path = file.getPath();

            if (!ignoranceRegistry.contains(path)) {

                final Integer endpoint = endpointRegistry.get(path);

                final FileUpdatedDto fileUpdatedDto = DtoFactory.getInstance()
                                                                .createDto(FileUpdatedDto.class)
                                                                .withPath(path);

                final JsonRpcRequest notification = DtoFactory.getInstance()
                                                              .createDto(JsonRpcRequest.class)
                                                              .withMethod("event:file-updated")
                                                              .withJsonrpc("2.0")
                                                              .withParams(fileUpdatedDto.toString());
                transmitter.transmit(notification, endpoint);
            } else {
                ignoranceRegistry.remove(path);
            }
        }
        return Optional.empty();
    }


    @Override
    public void onEvent(ProjectItemModifiedEvent event) {
        if (ProjectItemModifiedEvent.EventType.UPDATED.equals(event.getType())) {
            ignoranceRegistry.add(event.getPath());
        }
    }

    @Override
    public void receive(JsonRpcRequest request, Integer endpoint) {
        final String params = request.getParams();
        final String method = request.getMethod();


        if (method.equals("event:file-opened")) {
            final FileOpenedDto dto = DtoFactory.getInstance().createDtoFromJson(params, FileOpenedDto.class);
            endpointRegistry.put(dto.getPath(), endpoint);
        } else {
            final FileClosedDto dto = DtoFactory.getInstance().createDtoFromJson(params, FileClosedDto.class);
            endpointRegistry.remove(dto.getPath());
        }
    }
}

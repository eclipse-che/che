/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Sets.newConcurrentHashSet;

/**
 * Send workspace events using JSON RPC to the clients
 */
@Singleton
public class WorkspaceJsonRpcMessenger implements EventSubscriber<WorkspaceStatusEvent> {
    private final RequestTransmitter transmitter;
    private final EventService       eventService;

    private final Map<String, Set<String>> endpointIds = new ConcurrentHashMap<>();

    @Inject
    public WorkspaceJsonRpcMessenger(RequestTransmitter transmitter, EventService eventService) {
        this.transmitter = transmitter;
        this.eventService = eventService;
    }

    @Override
    public void onEvent(WorkspaceStatusEvent event) {
        String id = event.getWorkspaceId();
        endpointIds.entrySet()
                   .stream()
                   .filter(it -> it.getValue().contains(id))
                   .map(Map.Entry::getKey)
                   .forEach(it -> transmitter.newRequest()
                                             .endpointId(it)
                                             .methodName("event:workspace-status:changed")
                                             .paramsAsDto(event)
                                             .sendAndSkipResult());
    }

    @Inject
    private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {

        configurator.newConfiguration()
                    .methodName("event:workspace-status:subscribe")
                    .paramsAsString()
                    .noResult()
                    .withBiConsumer((endpointId, workspaceId) -> {
                        endpointIds.putIfAbsent(endpointId, newConcurrentHashSet());
                        endpointIds.get(endpointId).add(workspaceId);
                    });
    }

    @Inject
    private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("event:workspace-status:un-subscribe")
                    .paramsAsString()
                    .noResult()
                    .withBiConsumer((endpointId, workspaceId) -> {
                        Set<String> workspaceIds = endpointIds.get(endpointId);
                        if (workspaceIds != null) {
                            workspaceIds.remove(workspaceId);

                            if (workspaceIds.isEmpty()) {
                                endpointIds.remove(endpointId);
                            }
                        }
                    });
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }
}

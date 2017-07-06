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
package org.eclipse.che.api.git;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.git.shared.GitCheckoutEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Sets.newConcurrentHashSet;

@Singleton
public class GitJsonRpcMessenger implements EventSubscriber<GitCheckoutEvent> {
    private final Map<String, Set<String>> endpointIds = new ConcurrentHashMap<>();

    private final EventService       eventService;
    private final RequestTransmitter transmitter;

    @Inject
    public GitJsonRpcMessenger(EventService eventService, RequestTransmitter transmitter) {
        this.eventService = eventService;
        this.transmitter = transmitter;
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(GitCheckoutEvent event) {
        String workspaceIdAndProjectName = event.getWorkspaceId() + event.getProjectName();
        endpointIds.entrySet()
                   .stream()
                   .filter(it -> it.getValue().contains(workspaceIdAndProjectName))
                   .map(Entry::getKey)
                   .forEach(it -> transmitter.newRequest()
                                             .endpointId(it)
                                             .methodName("git/checkoutOutput")
                                             .paramsAsDto(event)
                                             .sendAndSkipResult());
    }

    @Inject
    private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("git/checkoutOutput/subscribe")
                    .paramsAsString()
                    .noResult()
                    .withBiConsumer((endpointId, workspaceIdAndProjectName) -> {
                        endpointIds.putIfAbsent(endpointId, newConcurrentHashSet());
                        endpointIds.get(endpointId).add(workspaceIdAndProjectName);
                    });
    }

    @Inject
    private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("git/checkoutOutput/unsubscribe")
                    .paramsAsString()
                    .noResult()
                    .withBiConsumer((endpointId, workspaceIdAndProjectName) -> {
                        endpointIds.getOrDefault(endpointId, newConcurrentHashSet()).remove(workspaceIdAndProjectName);
                        endpointIds.computeIfPresent(endpointId, (key, value) -> value.isEmpty() ? null : value);
                    });
    }
}

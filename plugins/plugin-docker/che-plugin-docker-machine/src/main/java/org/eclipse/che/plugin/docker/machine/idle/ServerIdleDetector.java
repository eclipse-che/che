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
package org.eclipse.che.plugin.docker.machine.idle;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.che.api.core.event.ServerIdleEvent;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@Singleton
public class ServerIdleDetector implements EventSubscriber<WorkspaceStatusEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerIdleDetector.class);

    private final long               timeout;
    private ScheduledFuture<?>       future;
    private ScheduledExecutorService executor;
    private WorkspaceManager         workspaceManager;
    private final EventService       eventService;

    @Inject
    public ServerIdleDetector(WorkspaceManager workspaceManager,
                                 EventService eventService,
                                 @Named("che.openshift.server.inactive.stop.timeout.ms") long timeout) {
        this.timeout = timeout;
        this.eventService = eventService;
        this.workspaceManager = workspaceManager;
        if (timeout > 0) {
            this.executor = Executors.newSingleThreadScheduledExecutor();
            this.future = executor.schedule(this::run, timeout, TimeUnit.MILLISECONDS);
            LOG.info("Idling che server scheduled [timeout=" + timeout/1000 + " seconds]");
        }
    }

    @Override
    public void onEvent(WorkspaceStatusEvent event) {
        if (future != null) {
            String workspaceId = event.getWorkspaceId();
            switch (event.getEventType()) {
            case RUNNING:
                if (!future.isCancelled()) {
                    future.cancel(true);
                    LOG.info("Idling che server canceled");
                }
                break;
            case STOPPED:
                Set<String> ids = workspaceManager.getRunningWorkspacesIds();
                ids.remove(workspaceId);
                if (ids.size() <= 0) {
                    if (!future.isCancelled()) {
                        future.cancel(true);
                    }
                    future = executor.schedule(this::run, timeout, TimeUnit.MILLISECONDS);
                    LOG.info("Idling che server scheduled [timeout=" + timeout / 1000 + " seconds]");
                }
                break;
            default:
                break;
            }
        }
    }

    private void run() {
        Set<String> ids = workspaceManager.getRunningWorkspacesIds();
        if (ids.size() <= 0) {
            eventService.publish(new ServerIdleEvent(timeout));
        }
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

}

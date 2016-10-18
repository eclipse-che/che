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
package org.eclipse.che.plugin.docker.machine.cleaner;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listener for remove workspace files after {@code WorkspaceRemovedEvent}.
 *
 * @author Alexander Andrienko
 */
@Singleton
public class RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber implements EventSubscriber<WorkspaceRemovedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber.class);

    private final WorkspaceFilesCleaner workspaceFilesCleaner;
    private final ExecutorService       executor;
    private final EventService          eventService;

    @Inject
    public RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber(EventService eventService, WorkspaceFilesCleaner workspaceFilesCleaner) {
        this.workspaceFilesCleaner = workspaceFilesCleaner;
        this.eventService = eventService;
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber-%d")
                                                                           .setDaemon(true)
                                                                           .build());
    }

    @Override
    public void onEvent(WorkspaceRemovedEvent event) {
        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            Workspace workspace = event.getWorkspace();
            try {
                workspaceFilesCleaner.clear(workspace);
            } catch (IOException | ServerException e) {
                LOG.error("Failed to remove workspace files for workspace with id: '{}'. Cause: '{}'", workspace.getId(), e.getMessage());
            }
        }));
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }
}

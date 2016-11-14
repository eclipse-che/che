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
package org.eclipse.che.ide.ext.machine.server.ssh;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.event.WorkspaceCreatedEvent;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Creates SSH keyPair each time a workspace is created (and delete it when workspace is removed)
 *
 * @author Florent Benoit
 */
@Singleton // must be eager
public class WorkspaceSshKeys {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceSshKeys.class);

    /**
     * The event service used to subscribe on create and delete events on any workspaces.
     */
    private final EventService eventService;

    /**
     * SSH manager handling ssh keys. Used to generate ssh keypair or remove the default keypair when workspace is removed.
     */
    private final SshManager sshManager;

    /**
     * User manager used to get user from given namespace.
     */
    private final UserManager userManager;


    /**
     * Default injection by using event service and ssh manager.
     *
     * @param eventService
     *         used to get CREATE/DELETE events for workspace
     * @param sshManager
     *         used to generate/remove default ssh keys
     */
    @Inject
    public WorkspaceSshKeys(final EventService eventService, final SshManager sshManager, final UserManager userManager) {
        this.eventService = eventService;
        this.sshManager = sshManager;
        this.userManager = userManager;
    }

    /**
     * When component is initialized, subscribe to workspace events in order to generate/delete ssh keys.
     */
    @PostConstruct
    public void start() {
        eventService.subscribe(new EventSubscriber<WorkspaceCreatedEvent>() {
            @Override
            public void onEvent(WorkspaceCreatedEvent workspaceCreatedEvent) {
                final String userId;
                try {
                    final User user = userManager.getByName(workspaceCreatedEvent.getWorkspace().getNamespace());
                    userId = user.getId();
                } catch (NotFoundException | ServerException e) {
                    LOG.error("Unable to get owner of the workspace {} with namespace {}", workspaceCreatedEvent.getWorkspace().getId(), workspaceCreatedEvent.getWorkspace().getNamespace());
                    return;
                }

                // Register default SSH keypair for this workspace.
                try {
                    sshManager.generatePair(userId, "workspace",
                                            workspaceCreatedEvent.getWorkspace().getId());
                } catch (ServerException | ConflictException e) {
                    // Conflict shouldn't happen as workspace id is new each time.
                    LOG.error("Unable to generate a default ssh pair for the workspace with ID {}",
                                            workspaceCreatedEvent.getWorkspace().getId(), e);
                }
            }
        });

        eventService.subscribe(new EventSubscriber<WorkspaceRemovedEvent>() {
            @Override
            public void onEvent(WorkspaceRemovedEvent workspaceRemovedEvent) {

                final String userId;
                try {
                    final User user = userManager.getByName(workspaceRemovedEvent.getWorkspace().getNamespace());
                    userId = user.getId();
                } catch (NotFoundException | ServerException e) {
                    LOG.error("Unable to get owner of the workspace {} with namespace {}", workspaceRemovedEvent.getWorkspace().getId(), workspaceRemovedEvent.getWorkspace().getNamespace());
                    return;
                }

                // Unregister default SSH keypair for this workspace (if any)
                try {
                    sshManager.removePair(userId, "workspace",
                                          workspaceRemovedEvent.getWorkspace().getId());
                } catch (NotFoundException e) {
                    LOG.debug("Do not remove default keypair from workspace {} as it is not existing (workspace ID {})",
                                            workspaceRemovedEvent.getWorkspace().getConfig().getName(),
                                            workspaceRemovedEvent.getWorkspace().getId());
                } catch (ServerException e) {
                    LOG.error("Error when trying to remove default ssh pair for the workspace {} (workspace ID {})",
                            workspaceRemovedEvent.getWorkspace().getConfig().getName(), workspaceRemovedEvent.getWorkspace().getId());
                }
            }
        });

    }
}

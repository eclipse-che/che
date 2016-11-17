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

import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.event.WorkspaceCreatedEvent;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link WorkspaceSshKeys}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceSshKeysTest {

    /**
     * The workspace ID that will be used in the test.
     */
    private static final String WORKSPACE_ID = "workspace123";

    /**
     * Name of the workspace returned through workspace.getConfig().getName()
     */
    private static final String WORKSPACE_NAME = "myworkspace";

    /**
     * User namespace for the current @{link EnvironmentContext}
     */
    private static final String NAMESPACE = "userNS";

    /**
     * Dummy Owner identifier used
     * This will be used for registering ssh keys.
     */
    private static final String OWNER_NAME = "userName";

    /**
     * Dummy UserID of the owner name
     * This will be used for registering ssh keys.
     */
    private static final String USER_ID = "user123";

    /**
     * Capturing the events on the event service.
     */
    @Captor
    private ArgumentCaptor<EventSubscriber<? extends Object>> subscriberCaptor;

    /**
     * Event service on which {@link WorkspaceSshKeys} will subscribe
     */
    @Mock
    private EventService eventService;

    /**
     * Manager used by {@link WorkspaceSshKeys} to generate/remove ssh keys
     */
    @Mock
    private SshManager sshManager;

    /**
     * Manager used by {@link WorkspaceSshKeys} to get userId from workspace owner
     */
    @Mock
    private UserManager userManager;

    /**
     * User instance used for returning calls to userManager.
     */
    @Mock
    private User user;

    /**
     * Mock of workspace used by events (create/remove)
     */
    @Mock
    private Workspace workspace;

    /**
     * Custom configuration used to provide custom name through workspace.getConfig().
     */
    @Mock
    private WorkspaceConfig workspaceConfig;

    /**
     * Subscriber for calls on eventService with {@link WorkspaceCreatedEvent} events.
     */
    private EventSubscriber<WorkspaceCreatedEvent> workspaceCreatedEventEventSubscriber;

    /**
     * Subscriber for calls on eventService with {@link WorkspaceRemovedEvent} events.
     */
    private EventSubscriber<WorkspaceRemovedEvent> workspaceRemovedEventEventSubscriber;

    /**
     * The instance that is tested
     */
    @InjectMocks
    private WorkspaceSshKeys workspaceSshKeys;


    /**
     * Setup initial objects by grabbing the two event subscribers, populating the environment context and initializing workspace data.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {

        workspaceSshKeys.start();
        verify(eventService, times(2)).subscribe(subscriberCaptor.capture());
        workspaceCreatedEventEventSubscriber = (EventSubscriber<WorkspaceCreatedEvent>)subscriberCaptor.getAllValues().get(0);
        workspaceRemovedEventEventSubscriber = (EventSubscriber<WorkspaceRemovedEvent>)subscriberCaptor.getAllValues().get(1);

        when(workspace.getId()).thenReturn(WORKSPACE_ID);
        when(workspace.getConfig()).thenReturn(workspaceConfig);
        when(workspaceConfig.getName()).thenReturn(WORKSPACE_NAME);
        when(workspace.getNamespace()).thenReturn(OWNER_NAME);

        when(userManager.getByName(eq(OWNER_NAME))).thenReturn(user);
        when(user.getId()).thenReturn(USER_ID);

    }

    /**
     * Ensure that when a new workspace is created, a new default ssh keypair is generated by asking sshManager
     */
    @Test
    public void shouldGenerateSshKeyPairWhenWorkspaceIsCreated() throws Exception {

        // given
        workspaceCreatedEventEventSubscriber.onEvent(new WorkspaceCreatedEvent(this.workspace));

        // then
        verify(sshManager).generatePair(eq(USER_ID), eq("workspace"), eq(WORKSPACE_ID));

    }

    /**
     * Ensure that when a workspace is removed, any associated ssh keypair is removed on sshManager
     */
    @Test
    public void shouldRemoveSshKeyPairWhenWorkspaceIsRemoved() throws Exception {

        // given
        workspaceRemovedEventEventSubscriber.onEvent(new WorkspaceRemovedEvent(this.workspace));

        // then
        verify(sshManager).removePair(eq(USER_ID), eq("workspace"), eq(WORKSPACE_ID));

    }
}

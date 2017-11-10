/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author David Festal */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceSubjectRegistryTest {

  @Spy private EventService eventService;
  @Mock private WorkspaceStatusEvent event;

  private WorkspaceSubjectRegistry workspaceSubjectRegistry;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    workspaceSubjectRegistry = new WorkspaceSubjectRegistry(eventService);
  }

  @Test
  public void shouldSubscribeToEventService() throws Exception {
    WorkspaceSubjectRegistry spiedWorkspaceSubjectRegistry = Mockito.spy(workspaceSubjectRegistry);
    spiedWorkspaceSubjectRegistry.subscribe();
    verify(eventService).subscribe(spiedWorkspaceSubjectRegistry);
    eventService.publish(event);
    verify(spiedWorkspaceSubjectRegistry).onEvent(event);
  }

  @Test
  public void shouldAddSubjectOnStartingEvent() throws Exception {
    final String workspaceId = "myWorkspaceId";
    when(event.getEventType()).thenReturn(WorkspaceStatusEvent.EventType.STARTING);
    when(event.getWorkspaceId()).thenReturn(workspaceId);
    Subject workspaceStarter = new SubjectImpl("userName", "userId", "userToken", false);
    EnvironmentContext.getCurrent().setSubject(workspaceStarter);
    workspaceSubjectRegistry.onEvent(event);
    assertTrue(workspaceSubjectRegistry.getWorkspaceStarter(workspaceId) == workspaceStarter);
  }

  @Test(expectedExceptions = {IllegalStateException.class})
  public void shouldThrowWhenStartingWorkspaceAsAnonymous() throws Exception {
    final String workspaceId = "myWorkspaceId";
    when(event.getEventType()).thenReturn(WorkspaceStatusEvent.EventType.STARTING);
    when(event.getWorkspaceId()).thenReturn(workspaceId);
    EnvironmentContext.getCurrent().setSubject(Subject.ANONYMOUS);
    workspaceSubjectRegistry.onEvent(event);
  }

  @Test
  public void shouldUpdateSubjectWithSameId() throws Exception {
    final String workspaceId = "myWorkspaceId";
    when(event.getEventType()).thenReturn(WorkspaceStatusEvent.EventType.STARTING);
    when(event.getWorkspaceId()).thenReturn(workspaceId);
    Subject workspaceStarter = new SubjectImpl("user", "userId", "userToken", false);
    EnvironmentContext.getCurrent().setSubject(workspaceStarter);
    workspaceSubjectRegistry.onEvent(event);

    Subject updatedSubject = new SubjectImpl("newUserName", "userId", "newUserToken", false);
    workspaceSubjectRegistry.updateSubject(updatedSubject);
    assertTrue(workspaceSubjectRegistry.getWorkspaceStarter(workspaceId) == updatedSubject);
  }

  @Test
  public void shouldNotUpdateSubjectWithDifferentId() throws Exception {
    final String workspaceId = "myWorkspaceId";
    when(event.getEventType()).thenReturn(WorkspaceStatusEvent.EventType.STARTING);
    when(event.getWorkspaceId()).thenReturn(workspaceId);
    Subject workspaceStarter = new SubjectImpl("user", "userId", "userToken", false);
    EnvironmentContext.getCurrent().setSubject(workspaceStarter);
    workspaceSubjectRegistry.onEvent(event);

    Subject updatedSubject = new SubjectImpl("newUserName", "newUserId", "newUserToken", false);
    workspaceSubjectRegistry.updateSubject(updatedSubject);
    assertTrue(workspaceSubjectRegistry.getWorkspaceStarter(workspaceId) == workspaceStarter);
  }

  @Test
  public void shouldRemoveUsersOnWorkspaceStop() throws Exception {
    final String workspaceId1 = "myWorkspaceId";
    final String workspaceId2 = "myWorkspaceId2";
    Subject workspaceStarter = new SubjectImpl("user", "userId", "userToken", false);
    EnvironmentContext.getCurrent().setSubject(workspaceStarter);
    when(event.getEventType()).thenReturn(WorkspaceStatusEvent.EventType.STARTING);
    when(event.getWorkspaceId()).thenReturn(workspaceId1);
    workspaceSubjectRegistry.onEvent(event);

    when(event.getEventType()).thenReturn(WorkspaceStatusEvent.EventType.STARTING);
    when(event.getWorkspaceId()).thenReturn(workspaceId2);
    workspaceSubjectRegistry.onEvent(event);

    assertTrue(workspaceSubjectRegistry.getWorkspaceStarter(workspaceId1) == workspaceStarter);
    assertTrue(workspaceSubjectRegistry.getWorkspaceStarter(workspaceId2) == workspaceStarter);

    when(event.getEventType()).thenReturn(WorkspaceStatusEvent.EventType.STOPPED);
    when(event.getWorkspaceId()).thenReturn(workspaceId2);
    workspaceSubjectRegistry.onEvent(event);

    assertTrue(workspaceSubjectRegistry.isUserKnown(workspaceStarter.getUserId()));

    when(event.getEventType()).thenReturn(WorkspaceStatusEvent.EventType.STOPPED);
    when(event.getWorkspaceId()).thenReturn(workspaceId1);
    workspaceSubjectRegistry.onEvent(event);

    assertFalse(workspaceSubjectRegistry.isUserKnown(workspaceStarter.getUserId()));

    assertNull(workspaceSubjectRegistry.getWorkspaceStarter(workspaceId1));
    assertNull(workspaceSubjectRegistry.getWorkspaceStarter(workspaceId2));
  }
}

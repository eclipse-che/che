/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.spi.jpa;

import static java.util.Map.of;
import static org.eclipse.che.api.workspace.shared.Constants.REMOVE_WORKSPACE_AFTER_STOP;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.MultiuserJpaWorkspaceDao.RemoveWorkspaceBeforeAccountRemovedEventSubscriber;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link RemoveWorkspaceBeforeAccountRemovedEventSubscriber} */
@Listeners(MockitoTestNGListener.class)
public class RemoveWorkspaceBeforeAccountRemovedEventSubscriberTest {
  @Mock private EventService eventService;
  @Mock private WorkspaceManager workspaceManager;

  @InjectMocks RemoveWorkspaceBeforeAccountRemovedEventSubscriber subscriber;

  private static final String user = NameGenerator.generate("user", 6);
  private static final String otherUser = NameGenerator.generate("otherUser", 6);
  private static final Subject SUBJECT = new SubjectImpl("user", user, "token", false);
  private final String workspaceId = NameGenerator.generate("workspace", 6);
  private AccountImpl account;
  private WorkspaceImpl workspace;

  @BeforeMethod
  public void setUp() throws Exception {
    account = new AccountImpl("id", "name", "test");
    workspace = new WorkspaceImpl(workspaceId, account, new WorkspaceConfigImpl());
    when(workspaceManager.getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong()))
        .thenReturn(new Page<>(Arrays.asList(workspace), 0, 1, 1));
  }

  @Test
  public void shouldSubscribeItself() {
    subscriber.subscribe();

    verify(eventService).subscribe(eq(subscriber), eq(BeforeAccountRemovedEvent.class));
  }

  @Test
  public void shouldUnsubscribeItself() {
    subscriber.unsubscribe();

    verify(eventService).unsubscribe(eq(subscriber), eq(BeforeAccountRemovedEvent.class));
  }

  @Test
  public void shouldRemoveStoppedWorkspace() throws Exception {
    workspace.setStatus(WorkspaceStatus.STOPPED);
    subscriber.onCascadeEvent(new BeforeAccountRemovedEvent(account));

    verify(workspaceManager).removeWorkspace(workspaceId);
  }

  @Test
  public void shouldStopAndRemoveRunningWorkspaceByOwner() throws Exception {
    workspace.setStatus(WorkspaceStatus.RUNNING);
    Runtime runtime = mock(Runtime.class);
    when(runtime.getOwner()).thenReturn(user);
    workspace.setRuntime(runtime);
    EnvironmentContext.getCurrent().setSubject(SUBJECT);

    doAnswer(
            invocation -> {
              workspace.setStatus(WorkspaceStatus.STOPPED);
              return null;
            })
        .when(workspaceManager)
        .stopWorkspace(workspaceId, of(REMOVE_WORKSPACE_AFTER_STOP, "true"));

    subscriber.onCascadeEvent(new BeforeAccountRemovedEvent(account));

    verify(workspaceManager).stopWorkspace(workspaceId, of(REMOVE_WORKSPACE_AFTER_STOP, "true"));
  }

  @Test
  public void shouldStopAndRemoveStartingWorkspaceByOwner() throws Exception {
    workspace.setStatus(WorkspaceStatus.STARTING);
    Runtime runtime = mock(Runtime.class);
    when(runtime.getOwner()).thenReturn(user);
    workspace.setRuntime(runtime);
    EnvironmentContext.getCurrent().setSubject(SUBJECT);

    doAnswer(
            invocation -> {
              workspace.setStatus(WorkspaceStatus.STOPPED);
              return null;
            })
        .when(workspaceManager)
        .stopWorkspace(workspaceId, of(REMOVE_WORKSPACE_AFTER_STOP, "true"));

    subscriber.onCascadeEvent(new BeforeAccountRemovedEvent(account));

    verify(workspaceManager).stopWorkspace(workspaceId, of(REMOVE_WORKSPACE_AFTER_STOP, "true"));
  }

  @Test
  public void shouldStopAndRemoveWorkspaceByAdmin() throws Exception {
    workspace.setStatus(WorkspaceStatus.STARTING);
    Runtime runtime = mock(Runtime.class);
    when(runtime.getOwner()).thenReturn(otherUser);
    workspace.setRuntime(runtime);
    EnvironmentContext.getCurrent().setSubject(SUBJECT);

    doAnswer(
            invocation -> {
              workspace.setStatus(WorkspaceStatus.STOPPED);
              return null;
            })
        .when(workspaceManager)
        .stopWorkspace(workspaceId, of(REMOVE_WORKSPACE_AFTER_STOP, "true"));

    subscriber.onCascadeEvent(new BeforeAccountRemovedEvent(account));

    verify(workspaceManager).stopWorkspace(workspaceId, of(REMOVE_WORKSPACE_AFTER_STOP, "true"));
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowExceptionIfWorkspaceStopping() throws Exception {
    workspace.setStatus(WorkspaceStatus.STARTING);
    Runtime runtime = mock(Runtime.class);
    when(runtime.getOwner()).thenReturn(otherUser);
    workspace.setRuntime(runtime);
    EnvironmentContext.getCurrent().setSubject(SUBJECT);

    subscriber.onCascadeEvent(new BeforeAccountRemovedEvent(account));

    verify(workspaceManager).stopWorkspace(workspaceId, of(REMOVE_WORKSPACE_AFTER_STOP, "true"));
  }
}

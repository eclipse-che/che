/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link WorkspacePVCCleaner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspacePVCCleanerTest {

  private static final String WORKSPACE_ID = "workspace132";
  private static final String NAMESPACE_NAME = "che";

  @Mock private WorkspaceVolumesStrategy pvcStrategy;
  @Mock private EventService eventService;

  private WorkspacePVCCleaner workspacePVCCleaner;

  @BeforeMethod
  public void setUp() {
    workspacePVCCleaner = new WorkspacePVCCleaner(true, NAMESPACE_NAME, pvcStrategy);
  }

  @Test
  public void testDoNotSubscribesCleanerWhenPVCDisabled() {
    workspacePVCCleaner = spy(new WorkspacePVCCleaner(false, NAMESPACE_NAME, pvcStrategy));

    workspacePVCCleaner.subscribe(eventService);

    verify(eventService, never()).subscribe(any());
  }

  @Test
  public void testSubscribesDeleteKubernetesOnWorkspaceRemove() throws Exception {
    workspacePVCCleaner.subscribe(eventService);

    verify(eventService).subscribe(any(EventSubscriber.class));
  }

  @Test
  public void testInvokeCleanupWhenWorkspaceRemovedEventPublished() throws Exception {
    doAnswer(
            invocationOnMock -> {
              final EventSubscriber<WorkspaceRemovedEvent> argument =
                  invocationOnMock.getArgument(0);
              final WorkspaceRemovedEvent event = mock(WorkspaceRemovedEvent.class);
              final Workspace removed = mock(Workspace.class);
              when(event.getWorkspace()).thenReturn(removed);
              when(removed.getId()).thenReturn(WORKSPACE_ID);
              argument.onEvent(event);
              return invocationOnMock;
            })
        .when(eventService)
        .subscribe(any());

    workspacePVCCleaner.subscribe(eventService);

    verify(pvcStrategy).cleanup(WORKSPACE_ID);
  }

  @Test
  public void testDoNotRethrowExceptionWhenErrorOnCleanupOccurs() throws Exception {
    doAnswer(
            invocationOnMock -> {
              final EventSubscriber<WorkspaceRemovedEvent> argument =
                  invocationOnMock.getArgument(0);
              final WorkspaceRemovedEvent event = mock(WorkspaceRemovedEvent.class);
              final Workspace removed = mock(Workspace.class);
              when(event.getWorkspace()).thenReturn(removed);
              when(removed.getId()).thenReturn(WORKSPACE_ID);
              argument.onEvent(event);
              return invocationOnMock;
            })
        .when(eventService)
        .subscribe(any());
    doThrow(InfrastructureException.class).when(pvcStrategy).cleanup(WORKSPACE_ID);

    workspacePVCCleaner.subscribe(eventService);

    verify(pvcStrategy).cleanup(WORKSPACE_ID);
  }
}

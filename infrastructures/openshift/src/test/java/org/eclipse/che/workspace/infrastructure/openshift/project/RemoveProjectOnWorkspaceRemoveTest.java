/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link RemoveProjectOnWorkspaceRemove}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveProjectOnWorkspaceRemoveTest {

  private static final String WORKSPACE_ID = "workspace123";

  @Mock private Workspace workspace;
  @Mock private OpenShiftProjectFactory projectFactory;

  private RemoveProjectOnWorkspaceRemove removeProjectOnWorkspaceRemove;

  @BeforeMethod
  public void setUp() throws Exception {
    removeProjectOnWorkspaceRemove = spy(new RemoveProjectOnWorkspaceRemove(projectFactory));

    lenient().doNothing().when(projectFactory).delete(anyString());

    when(workspace.getId()).thenReturn(WORKSPACE_ID);
  }

  @Test
  public void shouldSubscribeListenerToEventService() {
    EventService eventService = mock(EventService.class);

    removeProjectOnWorkspaceRemove.subscribe(eventService);

    verify(eventService).subscribe(removeProjectOnWorkspaceRemove);
  }

  @Test
  public void shouldRemoveProjectOnWorkspaceRemovedEventIfFactoryIsManagingNamespaces()
      throws Exception {
    when(projectFactory.isManagingNamespace(any())).thenReturn(true);

    removeProjectOnWorkspaceRemove.onEvent(new WorkspaceRemovedEvent(workspace));

    verify(projectFactory).delete(WORKSPACE_ID);
  }

  @Test
  public void shouldNotRemoveProjectOnWorkspaceRemovedEventIfFactoryIsNotManagingNamespaces()
      throws Exception {
    when(projectFactory.isManagingNamespace(any())).thenReturn(false);

    removeProjectOnWorkspaceRemove.onEvent(new WorkspaceRemovedEvent(workspace));

    verify(projectFactory, never()).delete(any());
  }
}

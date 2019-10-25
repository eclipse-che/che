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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

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
 * Test for {@link RemoveNamespaceOnWorkspaceRemove}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveNamespaceOnWorkspaceRemoveTest {

  private static final String WORKSPACE_ID = "workspace123";

  @Mock private Workspace workspace;
  @Mock private KubernetesNamespaceFactory namespaceFactory;

  private RemoveNamespaceOnWorkspaceRemove removeNamespaceOnWorkspaceRemove;

  @BeforeMethod
  public void setUp() throws Exception {
    removeNamespaceOnWorkspaceRemove = spy(new RemoveNamespaceOnWorkspaceRemove(namespaceFactory));

    lenient().doNothing().when(namespaceFactory).delete(anyString());

    when(workspace.getId()).thenReturn(WORKSPACE_ID);
  }

  @Test
  public void shouldSubscribeListenerToEventService() {
    EventService eventService = mock(EventService.class);

    removeNamespaceOnWorkspaceRemove.subscribe(eventService);

    verify(eventService).subscribe(removeNamespaceOnWorkspaceRemove);
  }

  @Test
  public void shouldRemoveNamespaceOnWorkspaceRemovedEventIfNamespaceIsManaged() throws Exception {
    when(namespaceFactory.isManagingNamespace(any())).thenReturn(true);

    removeNamespaceOnWorkspaceRemove.onEvent(new WorkspaceRemovedEvent(workspace));

    verify(namespaceFactory).delete(WORKSPACE_ID);
  }

  @Test
  public void shouldNotRemoveNamespaceOnWorkspaceRemovedEventIfNamespaceIsNotManaged()
      throws Exception {
    when(namespaceFactory.isManagingNamespace(any())).thenReturn(false);

    removeNamespaceOnWorkspaceRemove.onEvent(new WorkspaceRemovedEvent(workspace));

    verify(namespaceFactory, never()).delete(any());
  }
}

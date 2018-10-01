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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link EphemeralWorkspaceAdapter}.
 *
 * @author Ilya Buziuk
 */
@Listeners(MockitoTestNGListener.class)
public class EphemeralWorkspaceAdapterTest {
  private static final String EPHEMERAL_WORKSPACE_ID = "workspace123";
  private static final String NON_EPHEMERAL_WORKSPACE_ID = "workspace234";

  @Mock Workspace nonEphemeralWorkspace;
  @Mock Workspace ephemeralWorkspace;
  @Mock WorkspaceManager workspaceManager;

  private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;

  @BeforeMethod
  public void setup() throws Exception {
    ephemeralWorkspaceAdapter = new EphemeralWorkspaceAdapter();

    // ephemeral workspace configuration
    when(ephemeralWorkspace.getId()).thenReturn(EPHEMERAL_WORKSPACE_ID);
    WorkspaceConfig ephemeralWorkspaceConfig = mock(WorkspaceConfig.class);
    when(ephemeralWorkspace.getConfig()).thenReturn(ephemeralWorkspaceConfig);
    Map<String, String> ephemeralConfigAttributes =
        Collections.singletonMap(PERSIST_VOLUMES_ATTRIBUTE, "false");
    when(ephemeralWorkspaceConfig.getAttributes()).thenReturn(ephemeralConfigAttributes);

    // regular / non-ephemeral workspace configuration
    when(nonEphemeralWorkspace.getId()).thenReturn(NON_EPHEMERAL_WORKSPACE_ID);
    WorkspaceConfig nonEphemeralWorkspaceConfig = mock(WorkspaceConfig.class);
    when(nonEphemeralWorkspace.getConfig()).thenReturn(nonEphemeralWorkspaceConfig);
    Map<String, String> nonEphemeralConfigAttributes = Collections.emptyMap();
    when(nonEphemeralWorkspace.getAttributes()).thenReturn(nonEphemeralConfigAttributes);
  }

  @Test
  public void testIsEphemeralWorkspace() throws Exception {
    assertTrue(ephemeralWorkspaceAdapter.isEphemeral(ephemeralWorkspace));
    assertFalse(ephemeralWorkspaceAdapter.isEphemeral(nonEphemeralWorkspace));
  }
}

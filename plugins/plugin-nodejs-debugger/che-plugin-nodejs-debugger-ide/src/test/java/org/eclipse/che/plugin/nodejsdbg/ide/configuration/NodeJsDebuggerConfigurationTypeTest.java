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
package org.eclipse.che.plugin.nodejsdbg.ide.configuration;

import static org.junit.Assert.assertEquals;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.nodejsdbg.ide.NodeJsDebugger;
import org.eclipse.che.plugin.nodejsdbg.ide.NodeJsDebuggerResources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** @author Anatolii Bazko */
@RunWith(GwtMockitoTestRunner.class)
public class NodeJsDebuggerConfigurationTypeTest {

  @Mock private NodeJsDebuggerResources resources;
  @Mock private NodeJsDebuggerConfigurationPagePresenter nodeJsDebuggerConfigurationPagePresenter;
  @Mock private IconRegistry iconRegistry;

  @InjectMocks private NodeJsDebuggerConfigurationType nodeJsDebuggerConfigurationType;

  @Test
  public void testGetId() throws Exception {
    final String id = nodeJsDebuggerConfigurationType.getId();

    assertEquals(NodeJsDebugger.ID, id);
  }

  @Test
  public void testGetDisplayName() throws Exception {
    final String displayName = nodeJsDebuggerConfigurationType.getDisplayName();

    assertEquals(NodeJsDebuggerConfigurationType.DISPLAY_NAME, displayName);
  }

  @Test
  public void testGetConfigurationPage() throws Exception {
    final DebugConfigurationPage<? extends DebugConfiguration> page =
        nodeJsDebuggerConfigurationType.getConfigurationPage();

    assertEquals(nodeJsDebuggerConfigurationPagePresenter, page);
  }
}

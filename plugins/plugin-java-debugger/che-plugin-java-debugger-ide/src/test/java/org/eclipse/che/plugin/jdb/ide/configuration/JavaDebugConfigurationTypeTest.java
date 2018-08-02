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
package org.eclipse.che.plugin.jdb.ide.configuration;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.jdb.ide.JavaDebuggerResources;
import org.eclipse.che.plugin.jdb.ide.debug.JavaDebugger;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Artem Zatsarynnyi */
@Listeners(MockitoTestNGListener.class)
public class JavaDebugConfigurationTypeTest {

  @Mock private JavaDebuggerResources resources;
  @Mock private JavaDebugConfigurationPagePresenter javaDebugConfigurationPagePresenter;
  @Mock private IconRegistry iconRegistry;

  @InjectMocks private JavaDebugConfigurationType javaDebugConfigurationType;

  @Test
  public void testGetId() throws Exception {
    final String id = javaDebugConfigurationType.getId();

    assertEquals(JavaDebugger.ID, id);
  }

  @Test
  public void testGetDisplayName() throws Exception {
    final String displayName = javaDebugConfigurationType.getDisplayName();

    assertEquals(JavaDebugConfigurationType.DISPLAY_NAME, displayName);
  }

  @Test
  public void testGetConfigurationPage() throws Exception {
    final DebugConfigurationPage<? extends DebugConfiguration> page =
        javaDebugConfigurationType.getConfigurationPage();

    assertEquals(javaDebugConfigurationPagePresenter, page);
  }
}

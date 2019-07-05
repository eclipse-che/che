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
package org.eclipse.che.plugin.gdb.ide.configuration;

import static org.junit.Assert.assertEquals;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.gdb.ide.GdbDebugger;
import org.eclipse.che.plugin.gdb.ide.GdbResources;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** @author Artem Zatsarynnyi */
@RunWith(GwtMockitoTestRunner.class)
public class GdbConfigurationTypeTest {

  @Mock private GdbResources resources;
  @Mock private GdbConfigurationPagePresenter gdbConfigurationPagePresenter;
  @Mock private IconRegistry iconRegistry;

  @InjectMocks private GdbConfigurationType gdbConfigurationType;

  @Test
  public void testGetId() throws Exception {
    final String id = gdbConfigurationType.getId();

    Assert.assertEquals(GdbDebugger.ID, id);
  }

  @Test
  public void testGetDisplayName() throws Exception {
    final String displayName = gdbConfigurationType.getDisplayName();

    assertEquals(GdbConfigurationType.DISPLAY_NAME, displayName);
  }

  @Test
  public void testGetConfigurationPage() throws Exception {
    final DebugConfigurationPage<? extends DebugConfiguration> page =
        gdbConfigurationType.getConfigurationPage();

    assertEquals(gdbConfigurationPagePresenter, page);
  }
}

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
package org.eclipse.che.plugin.debugger.ide.configuration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class DebugConfigurationsGroupTest {

  @Mock private ActionManager actionManager;
  @Mock private DebugConfigurationsManager debugConfigurationsManager;
  @Mock private DebugConfigurationActionFactory debugConfigurationActionFactory;
  @Mock private DebugConfiguration debugConfiguration;

  @InjectMocks private DebugConfigurationsGroup actionGroup;

  @Before
  public void setUp() {
    List<DebugConfiguration> debugConfigurations = new ArrayList<>();
    debugConfigurations.add(debugConfiguration);
    when(debugConfigurationsManager.getConfigurations()).thenReturn(debugConfigurations);
  }

  @Test
  public void verifyActionConstruction() {
    verify(debugConfigurationsManager).addConfigurationsChangedListener(actionGroup);
    verify(debugConfigurationsManager).getConfigurations();
  }

  @Test
  public void shouldFillActionsOnConfigurationAdded() {
    actionGroup.onConfigurationAdded(mock(DebugConfiguration.class));

    verifyChildActionsFilled();
  }

  @Test
  public void shouldFillActionsOnConfigurationRemoved() {
    actionGroup.onConfigurationRemoved(mock(DebugConfiguration.class));

    verifyChildActionsFilled();
  }

  private void verifyChildActionsFilled() {
    verify(debugConfigurationsManager, times(2)).getConfigurations();
    verify(debugConfigurationActionFactory).createAction(debugConfiguration);

    assertEquals(1, actionGroup.getChildrenCount());
  }
}

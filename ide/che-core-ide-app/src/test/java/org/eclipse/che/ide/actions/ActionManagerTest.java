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
package org.eclipse.che.ide.actions;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Evgen Vidolob */
@RunWith(MockitoJUnitRunner.class)
public class ActionManagerTest {

  @Mock private KeyBindingAgent agent;
  private ActionManager actionManager;

  @Before
  public void init() {
    actionManager = new ActionManagerImpl(null, null);
  }

  @Test
  public void shouldUnregister() {
    DefaultActionGroup defaultActionGroup = new DefaultActionGroup(actionManager);
    actionManager.registerAction(IdeActions.GROUP_MAIN_MENU, defaultActionGroup);
    actionManager.unregisterAction(IdeActions.GROUP_MAIN_MENU);
    Action action = actionManager.getAction(IdeActions.GROUP_MAIN_MENU);
    assertNull(action);
  }

  @Test
  public void testIsGroup() {
    DefaultActionGroup defaultActionGroup = new DefaultActionGroup(actionManager);
    actionManager.registerAction(IdeActions.GROUP_MAIN_MENU, defaultActionGroup);
    boolean isGroup = actionManager.isGroup(IdeActions.GROUP_MAIN_MENU);
    assertTrue(isGroup);
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.processes.actions;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.menu.ContextMenu;

/**
 * Console tree context menu.
 *
 * @author Vitaliy Guliy
 */
public class ConsoleTreeContextMenu extends ContextMenu {

  @Inject
  public ConsoleTreeContextMenu(
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      Provider<PerspectiveManager> managerProvider) {
    super(actionManager, keyBindingAgent, managerProvider);
  }

  /** {@inheritDoc} */
  @Override
  protected String getGroupMenu() {
    return IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;
  }
}

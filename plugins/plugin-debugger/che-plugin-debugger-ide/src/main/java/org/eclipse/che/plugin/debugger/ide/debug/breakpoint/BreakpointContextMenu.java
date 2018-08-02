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
package org.eclipse.che.plugin.debugger.ide.debug.breakpoint;

import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.BREAKPOINT;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.menu.ContextMenu;
import org.eclipse.che.plugin.debugger.ide.DebuggerExtension;

/** @author Anatolii Bazko */
public class BreakpointContextMenu extends ContextMenu {

  private final Breakpoint breakpoint;

  @Inject
  public BreakpointContextMenu(
      @Assisted Breakpoint breakpoint,
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      Provider<PerspectiveManager> managerProvider) {
    super(actionManager, keyBindingAgent, managerProvider);
    this.breakpoint = breakpoint;
  }

  @Override
  protected String getGroupMenu() {
    return DebuggerExtension.BREAKPOINT_CONTEXT_MENU;
  }

  @Override
  protected ActionGroup updateActions() {
    ActionGroup actionGroup = super.updateActions();
    for (Action action : actionGroup.getChildren(null)) {
      Presentation presentation = action.getTemplatePresentation();
      presentation.putClientProperty(BREAKPOINT, breakpoint);
    }

    return actionGroup;
  }
}

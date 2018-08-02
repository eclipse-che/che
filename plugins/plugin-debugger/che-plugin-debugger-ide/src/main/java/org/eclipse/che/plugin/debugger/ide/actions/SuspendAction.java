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
package org.eclipse.che.plugin.debugger.ide.actions;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;

/**
 * Action which allows to suspend debugger session
 *
 * @author Roman Nikitenko
 */
public class SuspendAction extends AbstractPerspectiveAction {

  private final DebuggerManager debuggerManager;

  @Inject
  public SuspendAction(DebuggerManager debuggerManager, DebuggerLocalizationConstant locale) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.suspend(),
        locale.suspendDescription(),
        FontAwesome.PAUSE);
    this.debuggerManager = debuggerManager;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.suspend();
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    final Presentation presentation = event.getPresentation();
    final Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger == null) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    // Workaround: we don't support this action for another types of debugger
    presentation.setVisible("gdb".equals(debugger.getDebuggerType()));
    presentation.setEnabled(debugger.isConnected() && !debugger.isSuspended());
  }
}

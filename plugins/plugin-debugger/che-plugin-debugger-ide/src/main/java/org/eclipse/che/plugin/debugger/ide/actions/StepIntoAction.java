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
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

/**
 * Action which allows step into in debugger session
 *
 * @author Mykola Morhun
 */
public class StepIntoAction extends AbstractPerspectiveAction {

  private final DebuggerManager debuggerManager;

  @Inject
  public StepIntoAction(
      DebuggerManager debuggerManager,
      DebuggerLocalizationConstant locale,
      DebuggerResources resources) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.stepInto(),
        locale.stepIntoDescription(),
        resources.stepInto());
    this.debuggerManager = debuggerManager;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.stepInto();
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    event.getPresentation().setEnabled(debugger != null && debugger.isSuspended());
  }
}

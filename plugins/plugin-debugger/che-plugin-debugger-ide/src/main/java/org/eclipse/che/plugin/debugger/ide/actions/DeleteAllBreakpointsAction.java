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
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

/**
 * Action which allows remove all breakpoints
 *
 * @author Mykola Morhun
 */
public class DeleteAllBreakpointsAction extends AbstractPerspectiveAction {

  private final BreakpointManager breakpointManager;

  @Inject
  public DeleteAllBreakpointsAction(
      BreakpointManager breakpointManager,
      DebuggerLocalizationConstant locale,
      DebuggerResources resources) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.deleteAllBreakpoints(),
        locale.deleteAllBreakpointsDescription(),
        resources.deleteAllBreakpoints());
    this.breakpointManager = breakpointManager;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    breakpointManager.deleteAll();
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    event.getPresentation().setEnabled(!breakpointManager.getAll().isEmpty());
  }
}

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
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.BREAKPOINT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;

/**
 * Action which allows disabling breakpoint.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class DisableBreakpointAction extends AbstractPerspectiveAction {

  private final BreakpointManager breakpointManager;

  @Inject
  public DisableBreakpointAction(
      BreakpointManager breakpointManager, DebuggerLocalizationConstant locale) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.disableBreakpoint(),
        locale.disableBreakpointDescription());

    this.breakpointManager = breakpointManager;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Breakpoint breakpoint = (Breakpoint) getTemplatePresentation().getClientProperty(BREAKPOINT);
    if (breakpoint.isEnabled()) {
      breakpoint.setEnabled(false);
      breakpointManager.update(breakpoint);
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    Breakpoint breakpoint = (Breakpoint) getTemplatePresentation().getClientProperty(BREAKPOINT);
    event.getPresentation().setVisible(breakpoint.isEnabled());
  }
}

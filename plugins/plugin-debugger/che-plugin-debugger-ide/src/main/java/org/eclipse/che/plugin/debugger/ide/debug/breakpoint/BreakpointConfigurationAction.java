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

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.BREAKPOINT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;

/**
 * Actions allows to configure breakpoint.
 *
 * @author Mykola Morhun
 */
@Singleton
public class BreakpointConfigurationAction extends AbstractPerspectiveAction {

  private final BreakpointConfigurationPresenter breakpointConfigurationPresenter;

  @Inject
  public BreakpointConfigurationAction(
      DebuggerLocalizationConstant locale,
      BreakpointConfigurationPresenter breakpointConfigurationPresenter) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.breakpointConfiguration(),
        locale.breakpointConfiguration(),
        null,
        null);
    this.breakpointConfigurationPresenter = breakpointConfigurationPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Breakpoint breakpoint = (Breakpoint) getTemplatePresentation().getClientProperty(BREAKPOINT);
    breakpointConfigurationPresenter.showDialog(breakpoint);
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    event.getPresentation().setEnabled(true);
  }
}

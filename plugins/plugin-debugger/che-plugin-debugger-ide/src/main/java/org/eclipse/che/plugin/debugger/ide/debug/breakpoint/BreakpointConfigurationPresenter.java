/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug.breakpoint;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.ide.api.debug.BreakpointManager;

/** @author Anatolii Bazko */
@Singleton
public class BreakpointConfigurationPresenter
    implements BreakpointConfigurationView.ActionDelegate {

  private final BreakpointConfigurationView view;
  private final BreakpointManager breakpointManager;

  private Breakpoint breakpoint;

  @Inject
  public BreakpointConfigurationPresenter(
      BreakpointConfigurationView view, BreakpointManager breakpointManager) {
    this.view = view;
    this.breakpointManager = breakpointManager;
    this.view.setDelegate(this);
  }

  public void showDialog(Breakpoint breakpoint) {
    this.breakpoint = breakpoint;
    view.setBreakpoint(breakpoint);
    view.showDialog();
  }

  @Override
  public void onApplyClicked() {
    view.close();

    breakpoint.setCondition(
        Strings.isNullOrEmpty(view.getBreakpointCondition())
            ? null
            : view.getBreakpointCondition());
    breakpointManager.update(breakpoint);
  }
}

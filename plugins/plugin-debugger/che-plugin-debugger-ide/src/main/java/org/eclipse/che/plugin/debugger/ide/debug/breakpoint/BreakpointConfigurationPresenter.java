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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
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

    breakpoint.setEnabled(view.isBreakpointEnabled());
    ((BreakpointImpl) breakpoint).setBreakpointConfiguration(view.getBreakpointConfiguration());
    breakpointManager.update(breakpoint);
  }

  @Override
  public void onCloseClicked() {
    view.close();
  }
}

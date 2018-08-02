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

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.ide.api.mvp.View;

/**
 * Provides methods which allow to change view representation of breakpoint configuration window.
 *
 * @author Anatolii Bazko
 */
public interface BreakpointConfigurationView
    extends View<BreakpointConfigurationView.ActionDelegate> {

  interface ActionDelegate {
    /** Apply changes. */
    void onApplyClicked();

    /** Discard changes */
    void onCloseClicked();
  }

  void showDialog();

  void close();

  void setBreakpoint(Breakpoint breakpoint);

  BreakpointConfiguration getBreakpointConfiguration();

  boolean isBreakpointEnabled();
}

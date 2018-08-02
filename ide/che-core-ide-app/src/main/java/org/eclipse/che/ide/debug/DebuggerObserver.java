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
package org.eclipse.che.ide.debug;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;

/** @author Anatoliy Bazko */
public interface DebuggerObserver {

  /** Event happens when debugger client connected to the server. */
  void onDebuggerAttached(DebuggerDescriptor debuggerDescriptor);

  /** Event happens when debugger client disconnected from the server. */
  void onDebuggerDisconnected();

  /** Event happens when breakpoint added. */
  void onBreakpointAdded(Breakpoint breakpoint);

  /** Event happens when breakpoint activated. */
  void onBreakpointActivated(String filePath, int lineNumber);

  /** Event happens when breakpoint deleted. */
  void onBreakpointDeleted(Breakpoint breakpoint);

  /** Event happens when all breakpoint deleted. */
  void onAllBreakpointsDeleted();

  /** Event happens on step in. */
  void onPreStepInto();

  /** Event happens on step out. */
  void onPreStepOut();

  /** Event happens on step out. */
  void onPreStepOver();

  /** Event happens when debugger resumed. */
  void onPreResume();

  /** Event happens when debugger stopped at breakpoint. */
  void onBreakpointStopped(String filePath, Location location);

  /** Event happens when value changed. */
  void onValueChanged(Variable variable, long threadId, int frameIndex);
}

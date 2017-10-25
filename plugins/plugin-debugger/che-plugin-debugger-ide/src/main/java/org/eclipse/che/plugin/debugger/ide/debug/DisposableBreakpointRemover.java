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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;

/**
 * Deletes given breakpoint after invocation of this or any other breakpoint.
 *
 * @author Igor Vinokur
 */
public class DisposableBreakpointRemover implements DebuggerManagerObserver {

  private Breakpoint breakpoint;
  private final Debugger debugger;

  @Inject
  public DisposableBreakpointRemover(@Assisted Breakpoint breakpoint, @Assisted Debugger debugger) {
    this.breakpoint = breakpoint;
    this.debugger = debugger;
  }

  @Override
  public void onBreakpointStopped(String filePath, Location location) {
    debugger.deleteBreakpoint(breakpoint);
    debugger.removeObserver(this);
  }

  @Override
  public void onDebuggerDisconnected() {
    debugger.removeObserver(this);
  }

  @Override
  public void onActiveDebuggerChanged(Debugger activeDebugger) {}

  @Override
  public void onDebuggerAttached(DebuggerDescriptor debuggerDescriptor, Promise<Void> connect) {}

  @Override
  public void onBreakpointAdded(Breakpoint breakpoint) {}

  @Override
  public void onBreakpointActivated(String filePath, int lineNumber) {}

  @Override
  public void onBreakpointDeleted(Breakpoint breakpoint) {}

  @Override
  public void onAllBreakpointsDeleted() {}

  @Override
  public void onPreStepInto() {}

  @Override
  public void onPreStepOut() {}

  @Override
  public void onPreStepOver() {}

  @Override
  public void onPreResume() {}

  @Override
  public void onValueChanged(Variable variable, long threadId, int frameIndex) {}
}

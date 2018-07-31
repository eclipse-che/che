/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.debug;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.debug.shared.dto.ThreadStateDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Client-side debugger.
 *
 * @author Andrey Plotnikov
 * @author Anatoliy Bazko
 */
public interface Debugger extends DebuggerObservable {

  /** Returns debugger type */
  String getDebuggerType();

  /** Creates breakpoint. */
  Breakpoint createBreakpoint(VirtualFile file, int lineNumber);

  /**
   * Adds new breakpoint.
   *
   * @param breakpoint the breakpoint to add
   */
  Promise<Void> addBreakpoint(Breakpoint breakpoint);

  /**
   * Deletes the given breakpoint on server.
   *
   * @param breakpoint the breakpoint to delete
   */
  Promise<Void> deleteBreakpoint(Breakpoint breakpoint);

  /** Deletes all breakpoints. */
  Promise<Void> deleteAllBreakpoints();

  /** Returns breakpoints. */
  Promise<List<? extends Breakpoint>> getAllBreakpoints();

  /**
   * Connects to server.
   *
   * @param connectionProperties the connection properties
   */
  Promise<Void> connect(Map<String, String> connectionProperties);

  /**
   * Disconnects from process is being debugged. When debugger is disconnected it should invoke
   * {@link DebuggerManager#setActiveDebugger(Debugger)} with {@code null}.
   */
  void disconnect();

  /** Does step into. */
  void stepInto();

  /** Does step over. */
  void stepOver();

  /** Does step out. */
  void stepOut();

  /** Resumes application. */
  void resume();

  /** Resumes application to specified location. */
  void runToLocation(Location location);

  /** Suspends application. */
  void suspend();

  /**
   * Evaluates the given expression inside a specific frame.
   *
   * @param expression the expression to evaluate
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  Promise<String> evaluate(String expression, long threadId, int frameIndex);

  /**
   * Gets the value of the given variable inside a specific frame.
   *
   * @param variable the variable to get value from
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  Promise<? extends SimpleValue> getValue(Variable variable, long threadId, int frameIndex);

  /**
   * Gets a stack frame dump.
   *
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  Promise<? extends StackFrameDump> getStackFrameDump(long threadId, int frameIndex);

  /** Gets thread dump. */
  Promise<List<ThreadStateDto>> getThreadDump();

  /**
   * Sets a new value in the variable inside a specific frame.
   *
   * @param variable the variable to update
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  void setValue(Variable variable, long threadId, int frameIndex);

  /**
   * Gets a location of the resources for the given frame.
   *
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  Promise<? extends Location> getStackFrameLocation(long threadId, int frameIndex);

  /** Indicates if connection is established with the server. */
  boolean isConnected();

  /** Indicates if debugger is in suspended state. */
  boolean isSuspended();
}

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
package org.eclipse.che.api.debugger.server;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StartAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.DisconnectEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/**
 * Server side debugger. All methods throws {@link DebuggerException}.
 *
 * @author Anatoliy Bazko
 */
public interface Debugger {

  /**
   * Gets info about current debug sessions.
   *
   * @return {@link DebuggerInfo}
   * @throws DebuggerException if any error occur
   */
  DebuggerInfo getInfo() throws DebuggerException;

  /**
   * Disconnects from the process is being debugged. Must be fired {@link DisconnectEvent} if
   * succeeded.
   *
   * @throws DebuggerException if any error occur
   */
  void disconnect() throws DebuggerException;

  /**
   * Starts debugger when connection is established. Some implementations might not required it.
   * When process stops then {@link SuspendEvent} must be fired.
   *
   * @param action contains specific parameters
   * @throws DebuggerException if any error occur
   */
  void start(StartAction action) throws DebuggerException;

  /**
   * Suspends the application is being debugged. When process stops then {@link SuspendEvent} must
   * be fired.
   *
   * @throws DebuggerException if any error occur
   */
  default void suspend() throws DebuggerException {
    throw new DebuggerException("Unsupported operation for current debugger implementation.");
  }

  /**
   * Adds given breakpoint. When breakpoint is accepted by server then {@link
   * BreakpointActivatedEvent} must be fired. If breakpoint becomes deferred or just ignored then no
   * events should be fired.
   *
   * @param breakpoint the breakpoint to add
   * @throws DebuggerException if any error occur
   */
  void addBreakpoint(Breakpoint breakpoint) throws DebuggerException;

  /**
   * Deletes given breakpoint.
   *
   * @param location the location of the breakpoint to delete
   * @throws DebuggerException if any error occur
   */
  void deleteBreakpoint(Location location) throws DebuggerException;

  /**
   * Deletes all breakpoints.
   *
   * @throws DebuggerException if any error occur
   */
  void deleteAllBreakpoints() throws DebuggerException;

  /**
   * Gets all breakpoints.
   *
   * @throws DebuggerException if any error occur
   */
  List<Breakpoint> getAllBreakpoints() throws DebuggerException;

  /**
   * Gets the current value of the given variable.
   *
   * @deprecated
   * @see #getValue(VariablePath, long, int)
   * @param variablePath the path to the variable
   * @return {@link SimpleValue}
   * @throws DebuggerException if any error occur
   */
  @Deprecated
  SimpleValue getValue(VariablePath variablePath) throws DebuggerException;

  /**
   * Gets the value of the given variable.
   *
   * @param variablePath the path to the variable
   * @param threadId the unique thread id
   * @param frameIndex the frame index inside thread
   * @return {@link SimpleValue}
   * @throws DebuggerException if any error occur
   */
  default SimpleValue getValue(VariablePath variablePath, long threadId, int frameIndex)
      throws DebuggerException {
    throw new DebuggerException("Unsupported operation for current debugger implementation.");
  }
  /**
   * Sets the new value {@link Variable#getValue()} of the variable {@link
   * Variable#getVariablePath()}.
   *
   * @deprecated
   * @see #setValue(Variable, long, int)
   * @param variable the variable to update
   * @throws DebuggerException if any error occur
   */
  @Deprecated
  void setValue(Variable variable) throws DebuggerException;

  /**
   * Sets the new value {@link Variable#getValue()} of the variable {@link
   * Variable#getVariablePath()}.
   *
   * @param variable the variable to update
   * @param threadId the unique thread id
   * @param frameIndex the frame index inside thread
   * @throws DebuggerException if any error occur
   */
  default void setValue(Variable variable, long threadId, int frameIndex) throws DebuggerException {
    throw new DebuggerException("Unsupported operation for current debugger implementation.");
  }

  /**
   * Evaluates the given expression.
   *
   * @deprecated
   * @see #evaluate(String, long, int)
   * @param expression the expression to evaluate
   * @return the result
   * @throws DebuggerException if any error occur
   */
  @Deprecated
  String evaluate(String expression) throws DebuggerException;

  /**
   * Evaluates the given expression.
   *
   * @param expression the expression to evaluate
   * @param threadId the unique thread id
   * @param frameIndex the frame index inside thread
   * @return the result
   * @throws DebuggerException if any error occur
   */
  default String evaluate(String expression, long threadId, int frameIndex)
      throws DebuggerException {
    throw new DebuggerException("Unsupported operation for current debugger implementation.");
  }

  /**
   * Performs step over action. When process stops then {@link SuspendEvent} must be fired.
   *
   * @param action contains specific parameters
   * @throws DebuggerException if any error occur
   */
  void stepOver(StepOverAction action) throws DebuggerException;

  /**
   * Performs step into action. When process stops then {@link SuspendEvent} must be fired.
   *
   * @param action contains specific parameters
   * @throws DebuggerException if any error occur
   */
  void stepInto(StepIntoAction action) throws DebuggerException;

  /**
   * Performs step out action. When process stops then {@link SuspendEvent} must be fired.
   *
   * @param action contains specific parameters
   * @throws DebuggerException if any error occur
   */
  void stepOut(StepOutAction action) throws DebuggerException;

  /**
   * Resume application is being debugged. When process stops then {@link SuspendEvent} must be
   * fired.
   *
   * @param action contains specific parameters
   * @throws DebuggerException if any error occur
   */
  void resume(ResumeAction action) throws DebuggerException;

  /**
   * Dump values of local variables, fields and method arguments of the current frame.
   *
   * @deprecated Use {@link #getStackFrameDump(long, int)}
   * @return {@link StackFrameDump}
   * @throws DebuggerException if any error occur
   */
  @Deprecated
  StackFrameDump dumpStackFrame() throws DebuggerException;

  /**
   * Dump values of local variables, fields and method arguments of the current frame.
   *
   * @return {@link StackFrameDump}
   * @throws DebuggerException if any error occur
   */
  default StackFrameDump getStackFrameDump(long threadId, int frameIndex) throws DebuggerException {
    throw new DebuggerException("Unsupported operation for current debugger implementation.");
  }

  /**
   * Gets a thread dump.
   *
   * @return snapshot of the state of all threads
   * @throws DebuggerException if any error occur
   */
  default List<ThreadState> getThreadDump() throws DebuggerException {
    return Collections.emptyList();
  }

  /**
   * Gets a location of the resources for the given frame.
   *
   * @throws DebuggerException if any error occur
   */
  default Location getStackFrameLocation(long threadId, int frameIndex) throws DebuggerException {
    if (threadId == -1) {
      return dumpStackFrame().getLocation();
    }

    return getStackFrameDump(threadId, frameIndex).getLocation();
  }

  /** Is used to send back any events to client. */
  interface DebuggerCallback {
    void onEvent(DebuggerEvent event);
  }
}

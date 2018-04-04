/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.debug;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.ThreadStateDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.promises.client.Promise;

/**
 * Client for the service to debug application.
 *
 * @author Vitaly Parfonov
 * @author Anatoliy Bazko
 */
public interface DebuggerServiceClient {

  /**
   * Establishes connection with debug server.
   *
   * @param debuggerType the debugger server type, for instance: gdb, jdb etc
   * @param connectionProperties the connection properties
   */
  Promise<DebugSessionDto> connect(String debuggerType, Map<String, String> connectionProperties);

  /**
   * Disconnects from debugger server.
   *
   * @param id debug session id
   */
  Promise<Void> disconnect(String id);

  /**
   * Suspends the application is being debugged.
   *
   * @param id debug session id
   * @param action the suspend action parameters
   */
  Promise<Void> suspend(String id, SuspendActionDto action);

  /**
   * Gets debug session info.
   *
   * @param id debug session id
   */
  Promise<DebugSessionDto> getSessionInfo(String id);

  /**
   * Starts debug session when connection is established. Some debug server might not required this
   * step.
   *
   * @param id debug session id
   * @param action the start action parameters
   */
  Promise<Void> start(String id, StartActionDto action);

  /**
   * Adds breakpoint.
   *
   * @param id debug session id
   * @param breakpointDto the breakpoint to add
   */
  Promise<Void> addBreakpoint(String id, BreakpointDto breakpointDto);

  /**
   * Deletes breakpoint.
   *
   * @param id debug session id
   * @param locationDto the location of the breakpoint to delete
   */
  Promise<Void> deleteBreakpoint(String id, LocationDto locationDto);

  /**
   * Deletes all breakpoints.
   *
   * @param id debug session id
   */
  Promise<Void> deleteAllBreakpoints(String id);

  /**
   * Returns all breakpoints.
   *
   * @param id debug session id
   */
  Promise<List<BreakpointDto>> getAllBreakpoints(String id);

  /**
   * Gets the stack frame dump.
   *
   * @param id debug session id
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  Promise<StackFrameDumpDto> getStackFrameDump(String id, long threadId, int frameIndex);

  /**
   * Gets thread dump.
   *
   * @param id debug session id
   */
  Promise<List<ThreadStateDto>> getThreadDump(String id);

  /**
   * Resumes application.
   *
   * @param id debug session id
   */
  Promise<Void> resume(String id, ResumeActionDto action);

  /**
   * Returns a value of the variable inside the specific frame.
   *
   * @param id debug session id
   * @param variableDto the variable to get value from
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  Promise<SimpleValueDto> getValue(
      String id, VariableDto variableDto, long threadId, int frameIndex);

  /**
   * Sets the new value of the variable inside the specific frame.
   *
   * @param id debug session id
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  Promise<Void> setValue(String id, VariableDto variableDto, long threadId, int frameIndex);

  /**
   * Does step into.
   *
   * @param id debug session id
   * @param action the step into action parameters
   */
  Promise<Void> stepInto(String id, StepIntoActionDto action);

  /**
   * Does step over.
   *
   * @param id debug session id
   * @param action the step over action parameters
   */
  Promise<Void> stepOver(String id, StepOverActionDto action);

  /**
   * Does step out.
   *
   * @param id debug session id
   * @param action the step out action parameters
   */
  Promise<Void> stepOut(String id, StepOutActionDto action);

  /**
   * Evaluate the expression inside specific frame.
   *
   * @param id debug session id
   * @param expression the expression to evaluate
   * @param threadId the unique thread id {@link ThreadState#getId()}
   * @param frameIndex the frame index inside the thread
   */
  Promise<String> evaluate(String id, String expression, long threadId, int frameIndex);
}

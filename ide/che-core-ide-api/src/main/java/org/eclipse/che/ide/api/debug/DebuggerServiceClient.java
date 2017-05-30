/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.debug;

import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto;
import org.eclipse.che.api.promises.client.Promise;

import java.util.List;
import java.util.Map;

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
     * @param debuggerType
     *      the debugger server type, for instance: gdb, jdb etc
     * @param connectionProperties
     *      the connection properties
     */
    Promise<DebugSessionDto> connect(String debuggerType, Map<String, String> connectionProperties);

    /**
     * Disconnects from debugger server.
     *
     * @param id
     *      debug session id
     */
    Promise<Void> disconnect(String id);

    /**
     * Suspends the application is being debugged.
     *
     * @param id
     *         debug session id
     * @param action
     *         the suspend action parameters
     */
    Promise<Void> suspend(String id, SuspendActionDto action);

    /**
     * Gets debug session info.
     *
     * @param id
     *      debug session id
     */
    Promise<DebugSessionDto> getSessionInfo(String id);

    /**
     * Starts debug session when connection is established.
     * Some debug server might not required this step.
     *
     * @param id
     *      debug session id
     * @param action
     *      the start action parameters
     */
    Promise<Void> start(String id, StartActionDto action);

    /**
     * Adds breakpoint.
     *
     * @param id
     *      debug session id
     * @param breakpointDto
     *      the breakpoint to add
     */
    Promise<Void> addBreakpoint(String id, BreakpointDto breakpointDto);

    /**
     * Deletes breakpoint.
     *
     * @param id
     *      debug session id
     * @param locationDto
     *      the location of the breakpoint to delete
     */
    Promise<Void> deleteBreakpoint(String id, LocationDto locationDto);

    /**
     * Deletes all breakpoints.
     *
     * @param id
     *      debug session id
     */
    Promise<Void> deleteAllBreakpoints(String id);

    /**
     * Returns all breakpoints.
     *
     * @param id
     *      debug session id
     */
    Promise<List<BreakpointDto>> getAllBreakpoints(String id);

    /**
     * Gets dump of fields and local variables of the current frame.
     *
     * @param id
     *      debug session id
     */
    Promise<StackFrameDumpDto> getStackFrameDump(String id);

    /**
     * Resumes application.
     *
     * @param id
     *      debug session id
     */
    Promise<Void> resume(String id, ResumeActionDto action);

    /**
     * Returns a value of the variable.
     *
     * @param id
     *      debug session id
     */
    Promise<SimpleValueDto> getValue(String id, VariableDto variableDto);

    /**
     * Sets the new value of the variable.
     *
     * @param id
     *      debug session id
     */
    Promise<Void> setValue(String id, VariableDto variableDto);

    /**
     * Does step into.
     *
     * @param id
     *      debug session id
     * @param action
     *      the step into action parameters
     */
    Promise<Void> stepInto(String id, StepIntoActionDto action);

    /**
     * Does step over.
     *
     * @param id
     *      debug session id
     * @param action
     *      the step over action parameters
     */
    Promise<Void> stepOver(String id, StepOverActionDto action);

    /**
     * Does step out.
     *
     * @param id
     *      debug session id
     * @param action
     *      the step out action parameters
     */
    Promise<Void> stepOut(String id, StepOutActionDto action);

    /**
     * Evaluate the expression.
     *
     * @param id
     *      debug session id
     * @param expression
     *      the expression to evaluate
     */
    Promise<String> evaluate(String id, String expression);
}

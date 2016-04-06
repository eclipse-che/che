/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.debugger.client.debug;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.debugger.shared.Breakpoint;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointList;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerInfo;
import org.eclipse.che.ide.ext.debugger.shared.StackFrameDump;
import org.eclipse.che.ide.ext.debugger.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.debugger.shared.Value;
import org.eclipse.che.ide.ext.debugger.shared.Variable;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * The client for service to debug application.
 * All methods requires debug {@code id} which is returned in {@link DebuggerInfo} invoking {@link #connect(Map)} method.
 *
 * @author Vitaly Parfonov
 */
public interface DebuggerServiceClient {

    /**
     * Establishes connection with debugger server. Some debugger might require starting debug process.
     * For that purpose {@link #start(String)} method is invoked after connection is established.
     *
     * @param connectionProperties
     *      the connection properties
     */
    Promise<DebuggerInfo> connect(Map<String, String> connectionProperties);

    /**
     * Starts debug process when connection is established.
     *
     * @param id
     *      debug id
     */
    Promise<Void> start(@NotNull String id);

    /**
     * Stops and disconnects from debug process.
     *
     * @param id
     *      debug id
     */
    Promise<Void> disconnect(@NotNull String id);

    /**
     * Gets {@link DebuggerInfo}.
     *
     * @param id
     *      debug id
     */
    Promise<DebuggerInfo> getInfo(@NotNull String id);

    /**
     * Adds breakpoint.
     *
     * @param id
     *      debug id
     */
    Promise<Void> addBreakpoint(@NotNull String id, @NotNull Breakpoint breakpoint);

    /**
     * Deletes breakpoint.
     *
     * @param id
     *      debug id
     */
    Promise<Void> deleteBreakpoint(@NotNull String id, @NotNull Breakpoint breakpoint);

    /**
     * Returns all server breakpoints.
     *
     * @param id
     *      debug id
     */
    Promise<BreakpointList> getAllBreakpoints(@NotNull String id);

    /**
     * Deletes all breakpoints.
     *
     * @param id
     *      debug id
     */
    Promise<Void> deleteAllBreakpoints(@NotNull String id);

    /**
     * Gets dump of fields and local variable of current stack frame.
     *
     * @param id
     *      debug id
     */
    Promise<StackFrameDump> getStackFrameDump(@NotNull String id);

    /**
     * Resumes process.
     *
     * @param id
     *      debug id
     */
    Promise<Void> resume(@NotNull String id);

    /**
     * Returns a value of the variable.
     *
     * @param id
     *      debug id
     */
    Promise<Value> getValue(@NotNull String id, @NotNull Variable var);

    /**
     * Sets the value of the variable.
     *
     * @param id
     *      debug id
     */
    Promise<Void> setValue(@NotNull String id, @NotNull UpdateVariableRequest request);

    /**
     * Does step into.
     *
     * @param id
     *      debug id
     */
    Promise<Void> stepInto(@NotNull String id);

    /**
     * Does step over.
     *
     * @param id
     *      debug id
     */
    Promise<Void> stepOver(@NotNull String id);

    /**
     * Does step out.
     *
     * @param id
     *      debug id
     */
    Promise<Void> stepOut(@NotNull String id);

    /**
     * Evaluate the expression.
     *
     * @param id
     *      debug id
     */
    Promise<String> evaluateExpression(@NotNull String id, @NotNull String expression);
}

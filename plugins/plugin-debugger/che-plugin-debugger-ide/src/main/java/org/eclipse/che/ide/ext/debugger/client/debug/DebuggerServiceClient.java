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
 *
 * @author Vitaly Parfonov
 */
public interface DebuggerServiceClient {

    /**
     * Connects to debugger server.
     *
     * @param connectionProperties
     *      the connection properties
     */
    Promise<DebuggerInfo> connect(Map<String, String> connectionProperties);

    /**
     * Starts debugger.
     */
    Promise<Void> start(@NotNull String id);

    /**
     * Disconnect debugger.
     */
    Promise<Void> disconnect(@NotNull String id);

    /**
     * Attach debugger.
     */
    Promise<DebuggerInfo> getInfo(@NotNull String id);

    /**
     * Adds breakpoint.
     */
    Promise<Void> addBreakpoint(@NotNull String id, @NotNull Breakpoint breakpoint);

    /**
     * Deletes breakpoint.
     */
    Promise<Void> deleteBreakpoint(@NotNull String id, @NotNull Breakpoint breakpoint);

    /**
     * Returns list of active breakpoints.
     */
    Promise<BreakpointList> getAllBreakpoints(@NotNull String id);

    /**
     * Remove all breakpoints.
     */
    Promise<Void> deleteAllBreakpoints(@NotNull String id);

    /**
     * Get dump of fields and local variable of current stack frame.
     */
    Promise<StackFrameDump> getStackFrameDump(@NotNull String id);

    /**
     * Resume process.
     */
    Promise<Void> resume(@NotNull String id);

    /**
     * Returns value of a variable.
     */
    Promise<Value> getValue(@NotNull String id, @NotNull Variable var);

    /**
     * Sets value of a variable.
     */
    Promise<Void> setValue(@NotNull String id, @NotNull UpdateVariableRequest request);

    /**
     * Do step into.
     */
    Promise<Void> stepInto(@NotNull String id);

    /**
     * Do step over.
     */
    Promise<Void> stepOver(@NotNull String id);

    /**
     * Do step out.
     */
    Promise<Void> stepOut(@NotNull String id);

    /**
     * Evaluate an expression.
     */
    Promise<String> evaluateExpression(@NotNull String id, @NotNull String expression);
}

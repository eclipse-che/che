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
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPoint;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPointList;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.java.jdi.shared.JavaDebuggerInfo;
import org.eclipse.che.ide.ext.java.jdi.shared.StackFrameDump;
import org.eclipse.che.ide.ext.java.jdi.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.java.jdi.shared.Value;
import org.eclipse.che.ide.ext.java.jdi.shared.Variable;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * The client for service to debug java application.
 *
 * @author Vitaly Parfonov
 */
public interface DebuggerServiceClient {

    /**
     * Attach debugger.
     */
    Promise<JavaDebuggerInfo> connect(Map<String, String> connectionProperties);

    /**
     * Disconnect debugger.
     */
    Promise<Void> disconnect(@NotNull String id);

    /**
     * Adds breakpoint.
     */
    Promise<Void> addBreakpoint(@NotNull String id, @NotNull BreakPoint breakPoint);

    /**
     * Deletes breakpoint.
     */
    Promise<Void> deleteBreakpoint(@NotNull String id, @NotNull BreakPoint breakPoint);

    /**
     * Returns list of active breakpoints.
     */
    Promise<BreakPointList> getAllBreakpoints(@NotNull String id);

    /**
     * Remove all breakpoints.
     */
    Promise<Void> deleteAllBreakpoints(@NotNull String id);

    /**
     * Checks event.
     */
    Promise<DebuggerEventList> checkEvents(@NotNull String id);

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

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
package org.eclipse.che.ide.debug;

import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.VirtualFile;

import java.util.Map;

/**
 * Client-side debugger.
 *
 * @author Andrey Plotnikov
 * @author Anatoliy Bazko
 */
public interface Debugger extends DebuggerObservable {

    /** Returns debugger type */
    String getDebuggerType();

    /**
     * Adds new breakpoint.
     *
     * @param file
     *      the file
     * @param lineNumber
     *      the line number
     */
    void addBreakpoint(VirtualFile file, int lineNumber);

    /**
     * Deletes the given breakpoint on server.
     *
     * @param file
     *      the file
     * @param lineNumber
     *      the line number
     */
    void deleteBreakpoint(VirtualFile file, int lineNumber);

    /**
     * Deletes all breakpoints.
     */
    void deleteAllBreakpoints();

    /**
     * Connects to server.
     *
     * @param connectionProperties
     *      the connection properties
     */
    Promise<Void> connect(Map<String, String> connectionProperties);

    /**
     * Disconnects from process is being debugged.
     * When debugger is disconnected it should invoke {@link DebuggerManager#setActiveDebugger(Debugger)} with {@code null}.
     */
    void disconnect();

    /**
     * Does step into.
     */
    void stepInto();

    /**
     * Does step over.
     */
    void stepOver();

    /**
     * Does step out.
     */
    void stepOut();

    /**
     * Resumes application.
     */
    void resume();

    /**
     * Suspends application.
     */
    void suspend();

    /**
     * Evaluates the given expression
     */
    Promise<String> evaluate(String expression);

    /**
     * Gets the value of the given variable.
     */
    Promise<SimpleValue> getValue(Variable variable);

    /**
     * Gets dump the current frame.
     */
    Promise<StackFrameDump> dumpStackFrame();

    /**
     * Updates the value of the given variable.
     *
     * @param variable
     *      the variable to update
     */
    void setValue(Variable variable);

    /**
     * Indicates if connection is established with the server.
     */
    boolean isConnected();

    /**
     * Indicates if debugger is in suspended state.
     */
    boolean isSuspended();
}

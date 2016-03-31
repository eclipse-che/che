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
package org.eclipse.che.ide.debug;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.project.tree.VirtualFile;

import java.util.List;
import java.util.Map;

/**
 * The general class which provides to manage breakpoints on server.
 *
 * @author Andrey Plotnikov
 * @author Anatoliy Bazko
 */
public interface Debugger extends DebuggerObservable {

    /**
     * Adds new breakpoint on server.
     *
     * @param file
     * @param lineNumber
     */
    void addBreakpoint(VirtualFile file, int lineNumber);

    /**
     * Deletes breakpoint on server.
     *
     * @param file
     * @param lineNumber
     */
    void deleteBreakpoint(VirtualFile file, int lineNumber);

    /**
     * Deletes all breakpoints on server.
     */
    void deleteAllBreakpoints();

    /**
     * Attaches debugger.
     */
    Promise<Void> attachDebugger(Map<String, String> connectionProperties);

    /**
     * Disconnects from process under debugger.
     * When debugger is disconnected it should invoke {@link DebuggerManager#setActiveDebugger(Debugger)} with {@code null}.
     */
    void disconnectDebugger();

    /**
     * Steps into a method
     */
    void stepInto();

    /**
     * Steps without entering into a method
     */
    void stepOver();

    /**
     * Returns from a method
     */
    void stepOut();

    /**
     * Resumes execution
     */
    void resume();

    /**
     * Evaluates given expression
     */
    Promise<String> evaluateExpression(String expression);

    /**
     * @return the value of the variable
     */
    Promise<String> getValue(String variable);

    /**
     * @return stack frame dump
     */
    Promise<String> getStackFrameDump();

    /**
     * Changes value of given variable
     *
     * @param path
     *         path to changing variable
     * @param newValue
     *         new value for given variable
     */
    void changeVariableValue(List<String> path, String newValue);

    /**
     * Indicates if debugger is connected.
     */
    boolean isConnected();

    /**
     * Indicates if debugger is suspended.
     */
    boolean isSuspended();
}

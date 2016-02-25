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

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * The general class which provides to manage breakpoints on server.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface Debugger {
    /**
     * Adds new breakpoint on server.
     *
     * @param file
     * @param lineNumber
     * @param callback
     */
    void addBreakpoint(VirtualFile file, int lineNumber, AsyncCallback<Breakpoint> callback);

    /**
     * Deletes breakpoint on server.
     *
     * @param file
     * @param lineNumber
     * @param callback
     */
    void deleteBreakpoint(VirtualFile file, int lineNumber, AsyncCallback<Void> callback);

    /**
     * Deletes all breakpoints
     */
    void deleteAllBreakpoints();

    /**
     * Attaches debugger using given port and host.
     *
     * @param host host to which debugger will be connected
     * @param port port to which debugger will be connected in specified host
     */
    void attachDebugger(final String host, final int port);

    /**
     * Disconnects from process under a debugger.
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
     *
     * @param expression expression to evaluate
     * @return result of evaluating expression
     */
    Promise<String> evaluateExpression(String expression);

    /**
     * Changes value of given variable
     *
     * @param path path to changing variable
     * @param newValue new value for given variable
     */
    void changeVariableValue(List<String> path, String newValue);

    /**
     * @return current debugger state
     */
    DebuggerState getDebuggerState();

}

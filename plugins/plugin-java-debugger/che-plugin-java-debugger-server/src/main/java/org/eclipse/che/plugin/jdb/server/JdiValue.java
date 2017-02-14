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
package org.eclipse.che.plugin.jdb.server;

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/**
 * Value of JdiVariable.
 *
 * @author andrew00x
 */
public interface JdiValue {
    /**
     * Get value in String representation.
     *
     * @return value in String representation
     * @throws DebuggerException
     *         if an error occurs
     */
    String getAsString() throws DebuggerException;

    /**
     * Get nested variables.
     *
     * @return nested variables. This method always returns empty array for primitive type since primitive type has not
     *         any fields. If value represents array this method returns array members
     * @throws DebuggerException
     *         if an error occurs
     */
    JdiVariable[] getVariables() throws DebuggerException;

    /**
     * Get nested variable by name.
     *
     * @param name
     *         name of variable. Typically it is name of field. If this value represents array then name should be in form:
     *         <i>[i]</i>, where <i>i</i> is index of element
     * @return nested variable with specified name or <code>null</code> if there is no such variable
     * @throws DebuggerException
     *         if an error occurs
     * @see JdiVariable#getName()
     */
    JdiVariable getVariableByName(String name) throws DebuggerException;
}

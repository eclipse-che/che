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
 * Variable at debuggee JVM.
 *
 * @author andrew00x
 * @see JdiField
 * @see JdiLocalVariable
 * @see JdiArrayElement
 */
public interface JdiVariable {
    /**
     * Name of variable. If this variable is element of array then name is: <i>[i]</i>, where <i>i</i> - index of element
     *
     * @return name of variable
     * @throws DebuggerException
     *         if an error occurs
     */
    String getName() throws DebuggerException;

    /**
     * Check is this variable is array.
     *
     * @return <code>true</code> if variable is array and <code>false</code> otherwise
     * @throws DebuggerException
     *         if an error occurs
     */
    boolean isArray() throws DebuggerException;

    /**
     * Check is this variable is primitive.
     *
     * @return <code>true</code> if variable is primitive and <code>false</code> otherwise
     * @throws DebuggerException
     *         if an error occurs
     */
    boolean isPrimitive() throws DebuggerException;

    /**
     * Get value of variable.
     *
     * @return value
     * @throws DebuggerException
     *         if an error occurs
     */
    JdiValue getValue() throws DebuggerException;

    /**
     * Name of variable type.
     *
     * @return type name
     * @throws DebuggerException
     *         if an error occurs
     */
    String getTypeName() throws DebuggerException;
}

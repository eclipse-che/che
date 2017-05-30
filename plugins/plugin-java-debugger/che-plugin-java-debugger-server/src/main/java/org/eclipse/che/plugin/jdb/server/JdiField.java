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
 * Instance of class member in debugger JVM.
 *
 * @author andrew00x
 */
public interface JdiField extends JdiVariable {
    /**
     * Check is this field is final.
     *
     * @return <code>true</code> if field is final and <code>false</code> otherwise
     * @throws DebuggerException
     *         if an error occurs
     */
    boolean isFinal() throws DebuggerException;

    /**
     * Check is this field is static.
     *
     * @return <code>true</code> if field is static and <code>false</code> otherwise
     * @throws DebuggerException
     *         if an error occurs
     */
    boolean isStatic() throws DebuggerException;

    /**
     * Check is this transient is transient.
     *
     * @return <code>true</code> if field is transient and <code>false</code> otherwise
     * @throws DebuggerException
     *         if an error occurs
     */
    boolean isTransient() throws DebuggerException;

    /**
     * Check is this field is volatile.
     *
     * @return <code>true</code> if field is volatile and <code>false</code> otherwise
     * @throws DebuggerException
     *         if an error occurs
     */
    boolean isVolatile() throws DebuggerException;
}

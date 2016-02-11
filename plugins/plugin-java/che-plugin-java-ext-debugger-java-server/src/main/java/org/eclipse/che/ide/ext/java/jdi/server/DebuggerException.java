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
package org.eclipse.che.ide.ext.java.jdi.server;

/**
 * Main exception to throw my Debugger. Used as wrapper for JDI (Java Debug Interface) exceptions.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class DebuggerException extends Exception {
    public DebuggerException(String message) {
        super(message);
    }

    public DebuggerException(String message, Throwable cause) {
        super(message, cause);
    }
}

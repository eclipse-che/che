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
 * Thrown by {@link Debugger} when request to add a new breakpoint is invalid.
 * Typically it means the location (class name or line number) is invalid.
 *
 * @author andrew00x
 * @see Debugger#addBreakpoint(org.eclipse.che.ide.ext.java.jdi.shared.BreakPoint)
 */
@SuppressWarnings("serial")
public final class InvalidBreakPointException extends DebuggerException {
    public InvalidBreakPointException(String message) {
        super(message);
    }

    public InvalidBreakPointException(String message, Throwable cause) {
        super(message, cause);
    }
}

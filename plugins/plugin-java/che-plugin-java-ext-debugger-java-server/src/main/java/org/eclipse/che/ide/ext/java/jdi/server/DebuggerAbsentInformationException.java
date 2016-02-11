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
 * Absent Information exception. Used for prepare and transmit problem to the client side.
 *
 * @author Oleksii Orel
 */

public class DebuggerAbsentInformationException extends Exception {
    public DebuggerAbsentInformationException() {
        super();
    }

    public DebuggerAbsentInformationException(String message) {
        super(message);
    }

    public DebuggerAbsentInformationException(String message, Throwable cause) {
        super(message, cause);
    }
}

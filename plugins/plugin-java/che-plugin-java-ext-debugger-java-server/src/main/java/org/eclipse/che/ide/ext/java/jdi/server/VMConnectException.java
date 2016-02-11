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
 * Thrown if connection to JVM is not established or lost.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public final class VMConnectException extends DebuggerException {
    public VMConnectException(String message) {
        super(message);
    }

    public VMConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}

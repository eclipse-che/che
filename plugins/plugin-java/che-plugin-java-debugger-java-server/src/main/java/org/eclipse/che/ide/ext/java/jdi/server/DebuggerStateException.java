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
 * Thrown if Debugger is in inappropriate state to execute requested operation.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public final class DebuggerStateException extends DebuggerException {
    public DebuggerStateException(String message) {
        super(message);
    }
}

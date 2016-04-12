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
package org.eclipse.che.ide.gdb.server;

/**
 * @author Anatoliy Bazko
 */
@SuppressWarnings("serial")
public class GdbDebuggerNotFoundException extends GdbDebuggerException {
    public GdbDebuggerNotFoundException(String message) {
        super(message);
    }
}

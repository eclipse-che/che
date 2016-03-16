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
package org.eclipse.che.api.crypt.server;

public class EncryptException extends Exception {

    private static final long serialVersionUID = 1L;

    public EncryptException() {
    }

    public EncryptException(String message) {
        super(message);
    }

    public EncryptException(Throwable cause) {
        super(cause);
    }

    public EncryptException(String message, Throwable cause) {
        super(message, cause);
    }

}

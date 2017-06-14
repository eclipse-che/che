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
package org.eclipse.che.keycloak.token.provider.exception;

public class KeycloakException extends Exception {

    private static final long serialVersionUID = 1L;

    public KeycloakException() {
    }

    public KeycloakException(String message) {
        super(message);
    }

    public KeycloakException(Throwable cause) {
        super(cause);
    }

    public KeycloakException(String message, Throwable cause) {
        super(message, cause);
    }
}

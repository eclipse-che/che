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
package org.eclipse.che.plugin.svn.server.credentials;

public class CredentialsException extends Exception {

    private static final long serialVersionUID = 1L;

    public CredentialsException(final Throwable e) {
        super(e);
    }

    public CredentialsException(final String message) {
        super(message);
    }

}

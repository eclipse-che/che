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
package org.eclipse.che.api.git.exception;

/**
 * @author Yossi Balan (yossi.balan@sap.com)
 */
public class GitInvalidRefNameException extends GitException {

    /**
     * Construct a new GitInvalidRefNameException based on message
     *
     * @param message
     *        error message
     */
    public GitInvalidRefNameException(String message) {
        super(message);
    }

    /**
     * Construct a new GitInvalidRefNameException based on cause
     *
     * @param cause
     *        cause exception
     */
    public GitInvalidRefNameException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new GitInvalidRefNameException based on message and cause
     *
     * @param message
     *        error message
     * @param cause
     *        cause exception
     */
    public GitInvalidRefNameException(String message, Throwable cause) {
        super(message, cause);
    }
}

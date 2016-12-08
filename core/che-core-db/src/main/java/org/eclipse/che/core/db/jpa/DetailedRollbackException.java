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
package org.eclipse.che.core.db.jpa;

import org.eclipse.che.core.db.DBErrorCode;

import javax.persistence.RollbackException;

/**
 * Extends the standard {@link RollbackException} with an error code from {@link DBErrorCode}.
 *
 * @author Yevhenii Voevodin
 */
public class DetailedRollbackException extends RollbackException {

    private DBErrorCode code;

    public DetailedRollbackException(String message, Throwable cause, DBErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public DBErrorCode getCode() {
        return code;
    }
}

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
package org.eclipse.che.core.db.h2.jpa.eclipselink;

import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ExceptionHandler;

import java.sql.SQLException;

/**
 * Rethrows vendor specific exceptions as common exceptions.
 * See <a href="http://www.h2database.com/javadoc/org/h2/api/ErrorCode.html">H2 error codes</a>.
 *
 * @author Yevhenii Voevodin
 */
public class H2ExceptionHandler implements ExceptionHandler {

    public Object handleException(RuntimeException exception) {
        if (exception instanceof DatabaseException && exception.getCause() instanceof SQLException) {
            final SQLException sqlEx = (SQLException)exception.getCause();
            switch (sqlEx.getErrorCode()) {
                case 23505:
                    throw new DuplicateKeyException(exception.getMessage(), exception);
                case 23506:
                    throw new IntegrityConstraintViolationException(exception.getMessage(), exception);
            }
        }
        throw exception;
    }
}

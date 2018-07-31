/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.db.h2.jpa.eclipselink;

import java.sql.SQLException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ExceptionHandler;

/**
 * Rethrows vendor specific exceptions as common exceptions. See <a
 * href="http://www.h2database.com/javadoc/org/h2/api/ErrorCode.html">H2 error codes</a>.
 *
 * @author Yevhenii Voevodin
 */
public class H2ExceptionHandler implements ExceptionHandler {

  public Object handleException(RuntimeException exception) {
    if (exception instanceof DatabaseException && exception.getCause() instanceof SQLException) {
      final SQLException sqlEx = (SQLException) exception.getCause();
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

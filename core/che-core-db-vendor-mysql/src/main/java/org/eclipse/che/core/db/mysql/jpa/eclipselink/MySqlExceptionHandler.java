/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.db.mysql.jpa.eclipselink;

import java.sql.SQLException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ExceptionHandler;

/**
 * Rethrows vendor specific exceptions as common exceptions. See <a
 * href="https://dev.mysql.com/doc/refman/8.0/en/error-messages-server.html">MySQL error codes</a>.
 *
 * @author Barry Dresdner
 */
public class MySqlExceptionHandler implements ExceptionHandler {
  @Override
  public Object handleException(RuntimeException exception) {
    if (exception instanceof DatabaseException && exception.getCause() instanceof SQLException) {
      final SQLException sqlEx = (SQLException) exception.getCause();
      switch (sqlEx.getErrorCode()) {
        case 1062:
          throw new DuplicateKeyException(exception.getMessage(), exception);
        case 1452:
          throw new IntegrityConstraintViolationException(exception.getMessage(), exception);
      }
    }
    throw exception;
  }
}

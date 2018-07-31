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
package org.eclipse.che.core.db.postgresql.jpa.eclipselink;

import java.sql.SQLException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ExceptionHandler;

/**
 * Rethrows vendor specific exceptions as common exceptions. See <a
 * href="https://www.postgresql.org/docs/9.4/static/errcodes-appendix.html">PostgreSQL error
 * codes</a>.
 *
 * @author Yevhenii Voevodin
 * @author Sergii Kabashniuk
 */
public class PostgreSqlExceptionHandler implements ExceptionHandler {
  public Object handleException(RuntimeException exception) {
    if (exception instanceof DatabaseException && exception.getCause() instanceof SQLException) {
      final SQLException sqlEx = (SQLException) exception.getCause();
      switch (sqlEx.getSQLState()) {
        case "23505":
          throw new DuplicateKeyException(exception.getMessage(), exception);
        case "23503":
          throw new IntegrityConstraintViolationException(exception.getMessage(), exception);
      }
    }
    throw exception;
  }
}

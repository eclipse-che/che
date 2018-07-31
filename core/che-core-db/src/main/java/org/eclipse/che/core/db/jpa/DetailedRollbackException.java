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
package org.eclipse.che.core.db.jpa;

import javax.persistence.RollbackException;
import org.eclipse.che.core.db.DBErrorCode;

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

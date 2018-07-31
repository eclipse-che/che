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

import org.eclipse.che.core.db.DBErrorCode;

/**
 * Throws during inserts/updates entity that restricted by referential integrity and given
 * insert/update refers to non-existing entity.
 *
 * @author Anton Korneta
 * @see DBErrorCode#INTEGRITY_CONSTRAINT_VIOLATION
 */
public class IntegrityConstraintViolationException extends DetailedRollbackException {

  public IntegrityConstraintViolationException(String message, Throwable cause) {
    super(message, cause, DBErrorCode.INTEGRITY_CONSTRAINT_VIOLATION);
  }
}

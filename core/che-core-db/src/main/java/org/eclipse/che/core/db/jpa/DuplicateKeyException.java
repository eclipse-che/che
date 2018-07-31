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
 * Thrown when data couldn't be updated/stored due to unique constrain violation.
 *
 * @author Yevhenii Voevodin
 * @see DBErrorCode#DUPLICATE_KEY
 */
public class DuplicateKeyException extends DetailedRollbackException {

  public DuplicateKeyException(String message, Throwable cause) {
    super(message, cause, DBErrorCode.DUPLICATE_KEY);
  }
}

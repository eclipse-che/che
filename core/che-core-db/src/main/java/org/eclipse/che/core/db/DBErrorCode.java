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
package org.eclipse.che.core.db;

/**
 * Defines common database error codes which should be used throughout the application in preference
 * to vendor specific error codes.
 *
 * @author Yevhenii Voevodin
 */
public enum DBErrorCode {

  /** When database error can't be described with one of the other values of this enumeration. */
  UNDEFINED(-1),

  /**
   * When any of the unique constraints is violated e.g. duplicate key or unique index violation.
   */
  DUPLICATE_KEY(1),

  /** When entity referenced foreign key does not exist */
  INTEGRITY_CONSTRAINT_VIOLATION(2);

  private final int code;

  DBErrorCode(int code) {
    this.code = code;
  }

  /** Returns the code of this error. */
  public int getCode() {
    return code;
  }
}

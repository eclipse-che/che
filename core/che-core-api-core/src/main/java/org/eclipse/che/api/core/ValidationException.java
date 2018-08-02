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
package org.eclipse.che.api.core;

/**
 * Might be thrown by those system components that validate objects state before performing
 * operations with them so all conditions are met and the system is in consistent state.
 *
 * @author Yevhenii Voevodin
 */
public class ValidationException extends Exception {

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates an exception with a formatted message. Please follow {@link String#format(String,
   * Object...)} formatting patterns.
   */
  public ValidationException(String fmt, Object... args) {
    this(String.format(fmt, args));
  }
}

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
package org.eclipse.che.ide.runtime;

/**
 * <code>AssertionFailedException</code> is a runtime exception thrown by some of the methods in
 * <code>Assert</code>.
 *
 * <p>This class can be used without OSGi running.
 *
 * <p>This class is not intended to be instantiated or sub-classed by clients.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @see Assert
 * @since org.eclipse.equinox.common 3.2
 */
public class AssertionFailedException extends RuntimeException {

  /** All serializable objects should have a stable serialVersionUID */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the given message.
   *
   * @param detail the message
   */
  public AssertionFailedException(String detail) {
    super(detail);
  }
}

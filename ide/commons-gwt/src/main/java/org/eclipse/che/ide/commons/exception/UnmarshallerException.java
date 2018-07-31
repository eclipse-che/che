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
package org.eclipse.che.ide.commons.exception;

/**
 * Notifies about unmarshalling error accured.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 */
@SuppressWarnings("serial")
public class UnmarshallerException extends Exception {

  /**
   * Creates an Instance of {@link UnauthorizedException} with message and root cause
   *
   * @param message
   * @param cause
   */
  public UnmarshallerException(String message, Throwable cause) {
    super(message, cause);
  }
}

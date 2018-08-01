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
package org.eclipse.che.api.promises.client;

public class FunctionException extends Exception {

  private static final long serialVersionUID = 1L;

  public FunctionException() {}

  public FunctionException(String message) {
    super(message);
  }

  public FunctionException(Throwable cause) {
    super(cause);
  }

  public FunctionException(String message, Throwable cause) {
    super(message, cause);
  }
}

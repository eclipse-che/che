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
package org.eclipse.che.ide.commons;

/** @author andrew00x */
@SuppressWarnings("serial")
public class ParsingResponseException extends Exception {
  public ParsingResponseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParsingResponseException(String message) {
    super(message);
  }

  public ParsingResponseException(Throwable cause) {
    super(cause);
  }
}

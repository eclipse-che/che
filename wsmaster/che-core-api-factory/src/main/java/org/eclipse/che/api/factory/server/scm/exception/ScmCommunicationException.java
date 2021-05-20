/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.scm.exception;

/** Thrown when problem occurred during communication with scm provider */
public class ScmCommunicationException extends Exception {
  public ScmCommunicationException(String message) {
    super(message);
  }

  public ScmCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }
}

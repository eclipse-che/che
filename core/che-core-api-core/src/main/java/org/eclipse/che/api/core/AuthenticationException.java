/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

@SuppressWarnings("serial")
public class AuthenticationException extends ApiException {
  /**
   * Response status if any exception occurs, <br>
   * Default value: 400
   */
  int responseStatus;

  public AuthenticationException() {
    this(400);
  }

  public AuthenticationException(String message, Throwable cause) {
    this(400, message, cause);
  }

  public AuthenticationException(String message) {
    this(400, message);
  }

  public AuthenticationException(Throwable cause) {
    this(400, cause);
  }

  public AuthenticationException(int responseStatus) {
    super("Authentication failed.");
    this.responseStatus = responseStatus;
  }

  public AuthenticationException(int responseStatus, String message, Throwable cause) {
    super(message, cause);
    this.responseStatus = responseStatus;
  }

  public AuthenticationException(int responseStatus, String message) {
    super(message);
    this.responseStatus = responseStatus;
  }

  public AuthenticationException(int responseStatus, Throwable cause) {
    super(cause);
    this.responseStatus = responseStatus;
  }

  public int getResponseStatus() {
    return responseStatus;
  }

  public void setResponseStatus(int responseStatus) {
    this.responseStatus = responseStatus;
  }
}

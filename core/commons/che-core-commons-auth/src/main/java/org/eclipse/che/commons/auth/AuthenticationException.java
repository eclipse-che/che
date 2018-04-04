/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.auth;

import org.eclipse.che.api.core.ApiException;

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

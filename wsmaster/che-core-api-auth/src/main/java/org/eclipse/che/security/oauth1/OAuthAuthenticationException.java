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
package org.eclipse.che.security.oauth1;

import org.eclipse.che.api.core.ServerException;

/**
 * Exception raised when the OAuth authentication failed.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
public class OAuthAuthenticationException extends ServerException {

  /**
   * Constructs an instance of {@link OAuthAuthenticationException}.
   *
   * @param message the exception message.
   */
  public OAuthAuthenticationException(final String message) {
    super(message);
  }

  /**
   * Constructs an instance of {@link OAuthAuthenticationException}.
   *
   * @param cause the cause of the exception.
   */
  public OAuthAuthenticationException(final Throwable cause) {
    super(cause);
  }
}

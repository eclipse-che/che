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

/** Exception used when a user denies access on the OAuth authorization page. */
public final class UserDeniedOAuthAuthenticationException extends OAuthAuthenticationException {
  public UserDeniedOAuthAuthenticationException(String message) {
    super(message);
  }

  public UserDeniedOAuthAuthenticationException(Throwable cause) {
    super(cause);
  }
}

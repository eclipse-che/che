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
package org.eclipse.che.security.oauth.shared;

import java.io.IOException;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;

/** Retrieves user token from OAuth providers. */
public interface OAuthTokenProvider {
  /**
   * Get oauth token.
   *
   * @param oauthProviderName - name of provider.
   * @param userId - user
   * @return oauth token or <code>null</code>
   * @throws IOException if i/o error occurs when try to refresh expired oauth token
   */
  OAuthToken getToken(String oauthProviderName, String userId) throws IOException;
}

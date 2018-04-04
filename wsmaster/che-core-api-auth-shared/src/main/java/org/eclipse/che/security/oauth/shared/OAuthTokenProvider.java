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

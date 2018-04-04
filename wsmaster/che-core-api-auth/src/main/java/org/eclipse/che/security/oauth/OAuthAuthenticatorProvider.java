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
package org.eclipse.che.security.oauth;

import java.util.Set;

/** Allow store and provide services which implementations of OAuthAuthenticator. */
public interface OAuthAuthenticatorProvider {

  /**
   * Get authentication service by name.
   *
   * @param oauthProviderName name of OAuth provider
   * @return OAuthAuthenticator instance or <code>null</code> if specified OAuth provider is not
   *     supported
   */
  OAuthAuthenticator getAuthenticator(String oauthProviderName);

  /**
   * Gets registered OAuth provider names
   *
   * @return set of registered OAuth provider names
   */
  Set<String> getRegisteredProviderNames();
}

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
package org.eclipse.che.ide.api.oauth;

/**
 * Authenticators registry.
 *
 * @author Vitalii Parfonov
 */
public interface OAuth2AuthenticatorRegistry {

  void registerAuthenticator(String providerName, OAuth2Authenticator oAuth2Authenticator);

  OAuth2Authenticator getAuthenticator(String providerName);
}

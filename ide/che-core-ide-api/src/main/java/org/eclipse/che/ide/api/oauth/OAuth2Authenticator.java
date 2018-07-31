/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.oauth;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.security.oauth.OAuthStatus;

/** @author Roman Nikitenko */
public interface OAuth2Authenticator {

  void authenticate(String authenticationUrl, AsyncCallback<OAuthStatus> callback);

  Promise<OAuthStatus> authenticate(String authenticationUrl);

  String getProviderName();
}

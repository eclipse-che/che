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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.security.oauth.OAuthStatus;

/** @author Roman Nikitenko */
public interface OAuth2Authenticator {

  void authenticate(String authenticationUrl, AsyncCallback<OAuthStatus> callback);

  Promise<OAuthStatus> authenticate(String authenticationUrl);

  String getProviderName();
}

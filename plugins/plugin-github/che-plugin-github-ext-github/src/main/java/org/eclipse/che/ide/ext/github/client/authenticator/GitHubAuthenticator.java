/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.github.client.authenticator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.security.oauth.OAuthStatus;

/**
 * @author Roman Nikitenko
 */
public interface GitHubAuthenticator {
    void authorize(AsyncCallback<OAuthStatus> callback);
}

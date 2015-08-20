/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.server;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;

import javax.inject.Singleton;
import java.io.IOException;

/**
 * //
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class RemoteTokenProvider implements OAuthTokenProvider {
    @Override
    public OAuthToken getToken(String oauthProviderName, String userId) throws IOException {
        throw new NotImplementedException("Not implement yet");
    }
}

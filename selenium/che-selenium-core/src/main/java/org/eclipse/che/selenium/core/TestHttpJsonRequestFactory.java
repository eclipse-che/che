/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.selenium.core;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.selenium.core.user.DefaultTestUser;

import javax.validation.constraints.NotNull;

/**
 * @author Dmytro Nochevnov
 */
@Singleton
public class TestHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {
    private Provider<DefaultTestUser> testUserProvider;
    private String authToken;

    @Inject
    public TestHttpJsonRequestFactory(Provider<DefaultTestUser> testUserProvider) {
        this.testUserProvider = testUserProvider;
    }

    public TestHttpJsonRequestFactory(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public HttpJsonRequest fromUrl(@NotNull String url) {
        return super.fromUrl(url)
                    .setAuthorizationHeader(getAuthToken());
    }

    @Override
    public HttpJsonRequest fromLink(@NotNull Link link) {
        return super.fromLink(link)
                    .setAuthorizationHeader(getAuthToken());
    }

    private String getAuthToken() {
        if (authToken == null) {
            if (testUserProvider == null) {
                throw new IllegalStateException("Both autToken and testUserProvider are undefined.");
            }

            authToken = testUserProvider.get().getAuthToken();
        }

        return authToken;
    }
}

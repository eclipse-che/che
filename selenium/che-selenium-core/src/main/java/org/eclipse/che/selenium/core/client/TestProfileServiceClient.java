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
package org.eclipse.che.selenium.core.client;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

import java.util.Map;

/**
 * @author Musienko Maxim
 */
@Singleton
public class TestProfileServiceClient {
    private final String                 apiEndpoint;
    private final HttpJsonRequestFactory requestFactory;

    @Inject
    public TestProfileServiceClient(TestApiEndpointUrlProvider apiEndpointProvider,
                                    HttpJsonRequestFactory requestFactory) {
        this.apiEndpoint = apiEndpointProvider.get().toString();
        this.requestFactory = requestFactory;
    }

    public void setAttributes(Map<String, String> attributes, String authToken) throws Exception {
        requestFactory.fromUrl(apiEndpoint + "profile/attributes")
                      .usePutMethod()
                      .setAuthorizationHeader(authToken)
                      .setBody(attributes)
                      .request();
    }

    public void setUserNames(String name, String lastName, String authToken) throws Exception {
        Map<String, String> attributes = ImmutableMap.of("firstName", name,
                                                         "lastName", lastName);

        setAttributes(attributes, authToken);
    }
}

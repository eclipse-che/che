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
package org.eclipse.che.ide.ext.java.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Checks whether API is accessible from WS agent or not on app start to prevent usage of illegal configuration.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ApiEndpointAccessibilityChecker {
    private final String                 apiEndpoint;
    private final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public ApiEndpointAccessibilityChecker(@Named("api.endpoint") String apiEndpoint,
                                           HttpJsonRequestFactory httpJsonRequestFactory) {
        // everest respond 404 to path to rest without trailing slash
        this.apiEndpoint = apiEndpoint.endsWith("/") ? apiEndpoint : apiEndpoint + "/";
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    @PostConstruct
    public void start()
            throws ForbiddenException, BadRequestException, ConflictException, NotFoundException,
                   ServerException, UnauthorizedException, IOException {
        final HttpJsonResponse pingResponse = httpJsonRequestFactory.fromUrl(apiEndpoint)
                                                                    .setMethod(HttpMethod.GET)
                                                                    .setTimeout(2000)
                                                                    .request();
        if (pingResponse.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException(
                    "The workspace agent that we inject into your workspace machine has failed to start due to a DNS resolution error. " +
                    "This error happens when the agent cannot resolve the location of Che due to unusual configuration. " +
                    "Please post details of your configuration in an issue on Che's GitHub page.");
        }
    }
}

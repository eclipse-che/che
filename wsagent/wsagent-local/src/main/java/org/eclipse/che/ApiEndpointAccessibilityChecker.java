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
package org.eclipse.che;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.io.File;

/**
 * Checks whether API is accessible from WS agent or not on app start to prevent usage of illegal configuration.
 *
 * @author Alexander Garagatyi
 * @author Eugene Ivantsov
 */
@Singleton
public class ApiEndpointAccessibilityChecker {
    private static final Logger LOG = LoggerFactory.getLogger(ApiEndpointAccessibilityChecker.class);

    private final String                 apiEndpoint;
    private final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public ApiEndpointAccessibilityChecker(@Named("che.api") String apiEndpoint,
                                           HttpJsonRequestFactory httpJsonRequestFactory) {
        // everest respond 404 to path to rest without trailing slash
        this.apiEndpoint = apiEndpoint.endsWith("/") ? apiEndpoint : apiEndpoint + "/";
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    @PostConstruct
    public void start() {
            try {
                final HttpJsonResponse pingResponse = httpJsonRequestFactory.fromUrl(apiEndpoint)
                                                                            .setMethod(HttpMethod.GET)
                                                                            .setTimeout(2000)
                                                                            .request();
                if (pingResponse.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return;
                }
            } catch (ApiException | IOException e) {
                LOG.error( e.getLocalizedMessage(), e);
            }

            LOG.error("The workspace agent has attempted to start, but it is unable to ping the Che server at " + apiEndpoint);
            LOG.error("The workspace agent has been forcefully stopped. " +
                      "This error happens when the agent cannot resolve the location of the Che server. " +
                      "This error can usually be fixed with additional configuration settings in /conf/che.properties. " +
                      "The Che server will stop this workspace after a short timeout. " +
                      "You can get help by posting your config, stacktrace and workspace /etc/hosts below as a GitHub issue.");

            // content of /etc/hosts file may provide clues on why the connection failed, e.g. how che-host is resolved
            try {
                  LOG.info("Workspace /etc/hosts: " + IoUtil.readAndCloseQuietly(new FileInputStream(new File("/etc/hosts"))));
            } catch (Exception e) {
                LOG.info(e.getLocalizedMessage(), e);
            }

            System.exit(0);
        }
}

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
package org.eclipse.che.plugin.docker.machine.ext.provider;

import com.google.common.base.Strings;

import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Add env variable to docker dev-machine with url of Che API
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ApiEndpointEnvVariableProvider implements Provider<String> {
    @Inject
    @Named("machine.docker.che_api.endpoint")
    private String apiEndpoint;

    @Override
    public String get() {
        String apiEndpointEnvVar = System.getenv(DockerInstanceRuntimeInfo.API_ENDPOINT_URL_VARIABLE);
        if (Strings.isNullOrEmpty(apiEndpoint) &&
            !Strings.isNullOrEmpty(apiEndpointEnvVar)) {
            apiEndpoint = apiEndpointEnvVar;
        }
        return DockerInstanceRuntimeInfo.API_ENDPOINT_URL_VARIABLE + '=' + apiEndpoint;
    }
}

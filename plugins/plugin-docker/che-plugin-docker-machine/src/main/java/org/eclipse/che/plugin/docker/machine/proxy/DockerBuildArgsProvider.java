/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine.proxy;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides docker build arguments with proxy settings.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DockerBuildArgsProvider implements Provider<Map<String, String>> {
    private final Map<String, String> buildArgs;

    @Inject
    public DockerBuildArgsProvider(HttpProxyEnvVariableProvider httpProxy,
                                   HttpsProxyEnvVariableProvider httpsProxy,
                                   NoProxyEnvVariableProvider noProxy) {
        Map<String, String> buildArgs = new HashMap<>();
        splitVarAndAdd(httpProxy.get(), buildArgs);
        splitVarAndAdd(httpsProxy.get(), buildArgs);
        splitVarAndAdd(noProxy.get(), buildArgs);
        this.buildArgs = Collections.unmodifiableMap(buildArgs);
    }

    private void splitVarAndAdd(String envVariable, Map<String, String> splitVariables) {
        if (!envVariable.isEmpty()) {
            String[] keyValue = envVariable.split("=", 2);
            splitVariables.put(keyValue[0], keyValue[1]);
        }
    }

    @Override
    public Map<String, String> get() {
        return buildArgs;
    }
}

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
package org.eclipse.che.plugin.docker.machine.proxy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Add env variable to docker environment with http proxy settings.
 *
 * @author Mykola Morhun
 */
public class HttpProxyEnvVariableProvider implements Provider<String> {

    private static final String HTTP_PROXY = "http_proxy=";

    @Inject
    @Named("che.workspace.http_proxy")
    private String httpProxy;

    @Override
    public String get() {
        return httpProxy.isEmpty() ? "" : HTTP_PROXY + httpProxy;
    }

}

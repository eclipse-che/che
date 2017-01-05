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
package org.eclipse.che.api.core.websocket.impl;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Allows inject Guice instances on WEB SOCKET endpoint creation.
 *
 * @author Dmitry Kuleshov
 */
public class GuiceInjectorEndpointConfigurator extends ServerEndpointConfig.Configurator {
    @Inject
    private static Injector injector;

    public <T> T getEndpointInstance(Class<T> endpointClass) {
        return injector.getInstance(endpointClass);
    }
}

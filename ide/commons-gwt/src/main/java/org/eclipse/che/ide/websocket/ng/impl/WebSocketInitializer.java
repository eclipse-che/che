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
package org.eclipse.che.ide.websocket.ng.impl;

import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Contain all routines related to a web socket connection initialization
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketInitializer {
    private final WebSocketConnectionManager connectionManager;
    private final WebSocketPropertyManager   propertyManager;
    private final UrlResolver                urlResolver;

    @Inject
    public WebSocketInitializer(WebSocketConnectionManager connectionManager,
                                WebSocketPropertyManager propertyManager,
                                UrlResolver urlResolver) {
        this.connectionManager = connectionManager;
        this.propertyManager = propertyManager;
        this.urlResolver = urlResolver;
    }

    /**
     * Initializes a web socket connection, set default values, perform
     * mandatory preparation work.
     *
     * @param endpointId
     *         high level identifier of a web socket connection, used by
     *         high level service (e.g. json rpc infrastructure)
     * @param url
     *         url of a web socket endpoint
     */
    public void initialize(String endpointId, String url) {
        Log.debug(getClass(), "Initializing with url: " + url);

        urlResolver.setMapping(endpointId, url);

        propertyManager.initializeConnection(url);
        connectionManager.initializeConnection(url);

        connectionManager.establishConnection(url);
    }

    /**
     * Terminate web socket connection and clean up resources
     *
     * @param endpointId
     *         high level identifier of a web socket connection, used by
     *         high level service (e.g. json rpc infrastructure)
     */
    public void terminate(String endpointId) {
        Log.debug(getClass(), "Stopping");

        final String url = urlResolver.removeMapping(endpointId);

        propertyManager.disableSustainer(url);

        connectionManager.closeConnection(url);
    }
}

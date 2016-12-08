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
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Dispatches messages received from web socket endpoint.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketDispatcher {
    private final WebSocketMessageReceiver receiver;
    private final UrlResolver              urlResolver;

    @Inject
    public WebSocketDispatcher(WebSocketMessageReceiver receiver, UrlResolver urlResolver) {
        this.receiver = receiver;
        this.urlResolver = urlResolver;
    }

    /**
     * Dispatch a specific message among receiver implementations, currently implementd only
     * JsonRPC receiver.
     *
     * @param url
     *         url of a web socket endpoint that passed a message
     * @param message
     *         plain text message
     */
    public void dispatch(String url, String message) {
        Log.debug(getClass(), "Receiving a web socket message: " + message);

        final String id = urlResolver.resolve(url);
        receiver.receive(id, message);
    }
}

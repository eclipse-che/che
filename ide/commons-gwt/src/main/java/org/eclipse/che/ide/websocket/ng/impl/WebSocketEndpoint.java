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

/**
 * Handles WEB SOCKET related events
 *
 * @author Dmitry Kuleshov
 */
public interface WebSocketEndpoint {
    /**
     * Is called when connection is opened
     *
     * @param url
     *         url of a web socket where event happened, used as a low level identifier inside
     *         web socket infrastructure
     */
    void onOpen(String url);

    /**
     * Is called when connection is closed
     *
     * @param url
     *         url of a web socket where event happened, used as a low level identifier inside
     *         web socket infrastructure
     */
    void onClose(String url);

    /**
     * Is called when connection has errors
     *
     * @param url
     *         url of a web socket where event happened, used as a low level identifier inside
     *         web socket infrastructure
     */
    void onError(String url);

    /**
     * Is called when connection receives a text message
     *
     * @param url
     *         url of a web socket where event happened, used as a low level identifier inside
     *         web socket infrastructure
     */
    void onMessage(String url, String message);
}

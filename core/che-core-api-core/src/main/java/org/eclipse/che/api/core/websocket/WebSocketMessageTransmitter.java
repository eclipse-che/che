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
package org.eclipse.che.api.core.websocket;

/**
 * Transmits WEB SOCKET messages to an endpoint or broadcasts them
 * to all available endpoints.
 *
 * @author Dmitry Kuleshov
 */
public interface WebSocketMessageTransmitter {
    /**
     * Transmits WEB SOCKET messages to an endpoint
     *
     * @param protocol
     *         message protocol
     * @param message
     *         message body
     * @param endpointId
     *         endpoint identifier
     */
    void transmit(String protocol, String message, Integer endpointId);

    /**
     * Broadcasts WEB SOCKET messages to all endpoints
     *
     * @param protocol
     *         message protocol
     * @param message
     *         message body
     */
    void transmit(String protocol, String message);
}

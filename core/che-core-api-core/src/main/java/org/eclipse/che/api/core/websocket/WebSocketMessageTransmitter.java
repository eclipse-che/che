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
 * Plain text transmitter over a web socket protocol. In current specification
 * it is not required from and the implementor to fulfill strict ordering of
 * sequential transmissions along with message delivery notification. The only
 * guarantee that is required is to send a text message to predefined endpoint.
 *
 * @author Dmitry Kuleshov
 */
public interface WebSocketMessageTransmitter {
    /**
     * Transmit a string message to an endpoint over wer socket protocol. The
     * connection should be considered to be opened at the moment of calling,
     * however the some of implementation may provide ability to cache messages
     * until the connection is opened.
     *
     * @param endpointId
     *         identifier of an endpoint known to an transmitter implementation
     * @param message
     *         plain text message
     *
     */
    void transmit(String endpointId, String message);

    /**
     * Transmit (broadcast) a string message to all endpoints registered over
     * web socket protocol. The connection should be considered to be opened at
     * the moment of calling, however the some of implementation may provide
     * ability to cache messages  until the connection is opened.
     *
     * @param message
     *         plain text message
     */
    void transmit(String message);
}

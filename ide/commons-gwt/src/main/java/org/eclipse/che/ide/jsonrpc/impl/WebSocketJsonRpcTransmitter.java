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
package org.eclipse.che.ide.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Transmits JSON RPC objects to {@link WebSocketMessageTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcTransmitter {
    private final WebSocketMessageTransmitter transmitter;
    private final DtoFactory                  dtoFactory;
    private final JsonRpcObjectValidator      validator;

    @Inject
    public WebSocketJsonRpcTransmitter(WebSocketMessageTransmitter transmitter, DtoFactory dtoFactory, JsonRpcObjectValidator validator) {
        this.transmitter = transmitter;
        this.dtoFactory = dtoFactory;
        this.validator = validator;
    }

    public void transmit(String type, String message) {
        Log.debug(getClass(), "Transmitting a json rpc object. Message: " + message + "of type: " + message);

        final JsonRpcObject jsonRpcObject = dtoFactory.createDto(JsonRpcObject.class).withType(type).withMessage(message);
        validator.validate(jsonRpcObject);

        transmitter.transmit("jsonrpc-2.0", jsonRpcObject.toString());
    }
}

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
package org.eclipse.che.ide.jsonrpc.transmission;

import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.ResponseDispatcher;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import java.util.List;

/**
 * Params configurator provide means to configure params type in a
 * request that is to be sent. Params types that are supported:
 * {@link String}, {@link Boolean}, {@link Double}, {@link Void} and
 * DTO.
 */
public class ParamsConfigurator {
    private final ResponseDispatcher          responseDispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              jsonRpcFactory;
    private final String                      method;
    private final String                      endpointId;

    public ParamsConfigurator(ResponseDispatcher responseDispatcher, WebSocketMessageTransmitter transmitter,
                              JsonRpcFactory jsonRpcFactory, String method, String endpointId) {
        this.responseDispatcher = responseDispatcher;
        this.transmitter = transmitter;
        this.jsonRpcFactory = jsonRpcFactory;
        this.method = method;
        this.endpointId = endpointId;
    }

    public <P> SendConfiguratorFromOne<P> paramsAsDto(P paramsValue) {
        return new SendConfiguratorFromOne<>(responseDispatcher, transmitter, jsonRpcFactory, method, paramsValue, endpointId);
    }

    public SendConfiguratorFromOne<Double> paramsAsDouble(Double paramsValue) {
        return new SendConfiguratorFromOne<>(responseDispatcher, transmitter, jsonRpcFactory, method, paramsValue, endpointId);

    }

    public SendConfiguratorFromOne<String> paramsAsString(String paramsValue) {
        return new SendConfiguratorFromOne<>(responseDispatcher, transmitter, jsonRpcFactory, method, paramsValue, endpointId);
    }

    public SendConfiguratorFromOne<Boolean> paramsAsBoolean(Boolean paramsValue) {
        return new SendConfiguratorFromOne<>(responseDispatcher, transmitter, jsonRpcFactory, method, paramsValue, endpointId);
    }

    public SendConfiguratorFromOne<Void> paramsAsEmpty() {
        return new SendConfiguratorFromOne<>(responseDispatcher, transmitter, jsonRpcFactory, method, null, endpointId);
    }

    public <P> SendConfiguratorFromList<P> paramsAsListOfDto(List<P> paramsValue) {
        return new SendConfiguratorFromList<>(responseDispatcher, transmitter, jsonRpcFactory, method, paramsValue, endpointId);
    }

    public SendConfiguratorFromList<String> paramsAsListOfString(List<String> paramsValue) {
        return new SendConfiguratorFromList<>(responseDispatcher, transmitter, jsonRpcFactory, method, paramsValue, endpointId);
    }

    public SendConfiguratorFromList<Double> paramsAsListOfDouble(List<Double> paramsValue) {
        return new SendConfiguratorFromList<>(responseDispatcher, transmitter, jsonRpcFactory, method, paramsValue, endpointId);
    }

    public SendConfiguratorFromList<Boolean> paramsAsListOfBoolean(List<Boolean> paramsValue) {
        return new SendConfiguratorFromList<>(responseDispatcher, transmitter, jsonRpcFactory, method, paramsValue, endpointId);
    }
}

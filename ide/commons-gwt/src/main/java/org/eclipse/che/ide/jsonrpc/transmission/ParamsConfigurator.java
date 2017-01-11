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
package org.eclipse.che.ide.jsonrpc.transmission;

import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.ResponseDispatcher;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Params configurator provide means to configure params type in a
 * request that is to be sent. Params types that are supported:
 * {@link String}, {@link Boolean}, {@link Double}, {@link Void} and
 * DTO.
 */
public class ParamsConfigurator {
    private final ResponseDispatcher          dispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              factory;

    private final String                      method;
    private final String                      endpointId;

    public ParamsConfigurator(ResponseDispatcher dispatcher, WebSocketMessageTransmitter transmitter,
                              JsonRpcFactory factory, String method, String endpointId) {
        this.dispatcher = dispatcher;
        this.transmitter = transmitter;
        this.factory = factory;
        this.method = method;
        this.endpointId = endpointId;
    }

    public <P> SendConfiguratorFromOne<P> paramsAsDto(P pValue) {
        checkNotNull(pValue, "Params value must not be null");

        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params object value: " + pValue);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, pValue, endpointId);
    }

    public SendConfiguratorFromOne<Double> paramsAsDouble(Double pValue) {
        checkNotNull(pValue, "Params value must not be null");

        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params object value: " + pValue);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, pValue, endpointId);

    }

    public SendConfiguratorFromOne<String> paramsAsString(String pValue) {
        checkNotNull(pValue, "Params value must not be null");

        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params object value: " + pValue);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, pValue, endpointId);
    }

    public SendConfiguratorFromOne<Boolean> paramsAsBoolean(Boolean pValue) {
        checkNotNull(pValue, "Params value must not be null");

        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params object value: " + pValue);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, pValue, endpointId);
    }

    public SendConfiguratorFromOne<Void> paramsAsEmpty() {
        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + Void.class);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, null, endpointId);
    }

    public <P> SendConfiguratorFromList<P> paramsAsListOfDto(List<P> pListValue) {
        checkNotNull(pListValue, "Params list value must not be null");
        checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value: " + pListValue);

        return new SendConfiguratorFromList<>(dispatcher, transmitter, factory, method, pListValue, endpointId);
    }

    public SendConfiguratorFromList<String> paramsAsListOfString(List<String> pListValue) {
        checkNotNull(pListValue, "Params list value must not be null");
        checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + String.class + ", " +
                              "params list value: " + pListValue);

        return new SendConfiguratorFromList<>(dispatcher, transmitter, factory, method, pListValue, endpointId);
    }

    public SendConfiguratorFromList<Double> paramsAsListOfDouble(List<Double> pListValue) {
        checkNotNull(pListValue, "Params list value must not be null");
        checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + Double.class + ", " +
                              "params list value: " + pListValue);

        return new SendConfiguratorFromList<>(dispatcher, transmitter, factory, method, pListValue, endpointId);
    }

    public SendConfiguratorFromList<Boolean> paramsAsListOfBoolean(List<Boolean> pListValue) {
        checkNotNull(pListValue, "Params list value must not be null");
        checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

        Log.debug(getClass(), "Configuring outgoing request params: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + Boolean.class + ", " +
                              "params list value: " + pListValue);

        return new SendConfiguratorFromList<>(dispatcher, transmitter, factory, method, pListValue, endpointId);
    }
}

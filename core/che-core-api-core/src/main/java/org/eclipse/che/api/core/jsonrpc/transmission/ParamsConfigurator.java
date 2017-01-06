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
package org.eclipse.che.api.core.jsonrpc.transmission;

import org.eclipse.che.api.core.jsonrpc.JsonRpcFactory;
import org.eclipse.che.api.core.jsonrpc.ResponseDispatcher;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(MethodNameConfigurator.class);

    private final ResponseDispatcher          dispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              factory;

    private final String method;
    private final String endpointId;

    ParamsConfigurator(ResponseDispatcher dispatcher, WebSocketMessageTransmitter transmitter, JsonRpcFactory factory, String method,
                       String endpointId) {
        this.dispatcher = dispatcher;
        this.transmitter = transmitter;
        this.factory = factory;
        this.method = method;
        this.endpointId = endpointId;
    }

    public <P> SendConfiguratorFromOne<P> paramsAsDto(P pValue) {
        checkNotNull(pValue, "Params value must not be null");

        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params object class: {}, params object value: {}",
                  endpointId, method, pValue.getClass(), pValue);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, pValue, endpointId);
    }

    public SendConfiguratorFromOne<Double> paramsAsDouble(Double pValue) {
        checkNotNull(pValue, "Params value must not be null");

        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params object class: {}, params object value: {}",
                  endpointId, method, pValue.getClass(), pValue);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, pValue, endpointId);

    }

    public SendConfiguratorFromOne<String> paramsAsString(String pValue) {
        checkNotNull(pValue, "Params value must not be null");

        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params object class: {}, params object value: {}",
                  endpointId, method, pValue.getClass(), pValue);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, pValue, endpointId);
    }

    public SendConfiguratorFromOne<Boolean> paramsAsBoolean(Boolean pValue) {
        checkNotNull(pValue, "Params value must not be null");

        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params object class: {}, params object value: {}",
                  endpointId, method, pValue.getClass(), pValue);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, pValue, endpointId);
    }

    public SendConfiguratorFromOne<Void> paramsAsEmpty() {
        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params object class: {}",
                  endpointId, method, Void.class);

        return new SendConfiguratorFromOne<>(dispatcher, transmitter, factory, method, null, endpointId);
    }

    public <P> SendConfiguratorFromMany<P> paramsAsListOfDto(List<P> pListValue) {
        checkNotNull(pListValue, "Params list value must not be null");
        checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params list items class: {}, params list value: {}",
                  endpointId, method, pListValue.iterator().next().getClass(), pListValue);

        return new SendConfiguratorFromMany<>(dispatcher, transmitter, factory, method, pListValue, endpointId);
    }

    public SendConfiguratorFromMany<String> paramsAsListOfString(List<String> pListValue) {
        checkNotNull(pListValue, "Params list value must not be null");
        checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params list items class: {}, params list value: {}",
                  endpointId, method, String.class, pListValue);

        return new SendConfiguratorFromMany<>(dispatcher, transmitter, factory, method, pListValue, endpointId);
    }

    public SendConfiguratorFromMany<Double> paramsAsListOfDouble(List<Double> pListValue) {
        checkNotNull(pListValue, "Params list value must not be null");
        checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params list items class: {}, params list value: {}",
                  endpointId, method, Double.class, pListValue);

        return new SendConfiguratorFromMany<>(dispatcher, transmitter, factory, method, pListValue, endpointId);
    }

    public SendConfiguratorFromMany<Boolean> paramsAsListOfBoolean(List<Boolean> pListValue) {
        checkNotNull(pListValue, "Params list value must not be null");
        checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

        LOG.debug("Configuring outgoing request params: endpoint ID: {}, method: {}, params list items class: {}, params list value: {}",
                  endpointId, method, Boolean.class, pListValue);

        return new SendConfiguratorFromMany<>(dispatcher, transmitter, factory, method, pListValue, endpointId);
    }
}

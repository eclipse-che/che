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
package org.eclipse.che.api.core.util;

import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class JsonRpcMessageConsumer<T> implements MessageConsumer<T> {
    private static final Logger LOG = getLogger(JsonRpcMessageConsumer.class);

    private final String                   method;
    private final RequestTransmitter       transmitter;
    private final Map<String, Set<String>> endpointIds;

    public JsonRpcMessageConsumer(String method, RequestTransmitter transmitter, Map<String, Set<String>> endpointIds) {
        this.method = method;
        this.transmitter = transmitter;
        this.endpointIds = endpointIds;
    }

    @Override
    public void consume(T message) throws IOException {
        try {
            endpointIds.entrySet()
                       .stream()
                       .map(Map.Entry::getKey)
                       .forEach(it -> transmitter.transmitOneToNone(it, method, message));
        } catch (IllegalStateException e) {
            LOG.error("Error trying send line {}", message);
        }
    }

    @Override
    public void close() throws IOException {
    }
}

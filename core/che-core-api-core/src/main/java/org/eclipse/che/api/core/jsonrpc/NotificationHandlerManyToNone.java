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
package org.eclipse.che.api.core.jsonrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handler to contain an operation and all related metadata required for
 * processing incoming notification. This handler is used when we have
 * parameters represented by a list.
 *
 * @param <P>
 *         type of notification params list items
 */
public class NotificationHandlerManyToNone<P> implements NotificationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationHandlerManyToNone.class);

    private final Class<P>                    pClass;
    private final BiConsumer<String, List<P>> consumer;

    public NotificationHandlerManyToNone(Class<P> pClass, BiConsumer<String, List<P>> consumer) {
        checkNotNull(pClass, "Params class must not be null");
        checkNotNull(consumer, "Binary operation must not be null");

        this.pClass = pClass;
        this.consumer = consumer;
    }

    public void handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");
        checkNotNull(params, "Params must not be null");

        LOG.debug("Handling notification from: {}, with list params: {}", endpointId, params);

        consumer.accept(endpointId, params.getAsListOf(pClass));
    }
}

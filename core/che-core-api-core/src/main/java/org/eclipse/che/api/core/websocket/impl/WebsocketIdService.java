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
package org.eclipse.che.api.core.websocket.impl;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import java.util.Random;

/**
 * Identification service provide means to set and get unique identifiers for
 * websocket clients. There are several identifier elements to distinguish:
 * clientId, endpointId, combinedId. Client id is the identifier of a client
 * that is passed over websocket to the client and back. EndpointId is called
 * to identify a websocket endpoint client connects through. CombinedId is a
 * combination of client and endpoint identifiers separated by a sequence of
 * special charaters, it is used internally.
 */
@Singleton
public class WebsocketIdService {
    private static final String ERROR_MESSAGE = "No session is associated with provided endpoint id";
    private static final String SEPARATOR     = "<-:->";
    private static final Random GENERATOR     = new Random();

    private final WebSocketSessionRegistry registry;

    @Inject
    public WebsocketIdService(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    private static String clientId() {
        return String.valueOf(GENERATOR.nextInt(Integer.MAX_VALUE));
    }

    @Inject
    private void configureHandler(RequestHandlerConfigurator requestHandlerConfigurator) {
        requestHandlerConfigurator.newConfiguration()
                                  .methodName("websocketIdService/getId")
                                  .noParams()
                                  .resultAsString()
                                  .withFunction(this::extractClientId);

        requestHandlerConfigurator.newConfiguration()
                                  .methodName("websocketIdService/setId")
                                  .paramsAsString()
                                  .noResult()
                                  .withBiConsumer((oldCombinedId, newClientId) -> {
                                      String endpointId = extractEndpointId(oldCombinedId);
                                      String newCombinedId = getCombinedId(endpointId, newClientId);
                                      Session session = registry.remove(oldCombinedId)
                                                                .orElseThrow(() -> new JsonRpcException(-27000, ERROR_MESSAGE));

                                      registry.add(newCombinedId, session);
                                  });
    }

    public String getCombinedId(String endpointId) {
        return clientId() + SEPARATOR + endpointId;
    }

    public String getCombinedId(String endpointId, String clientId) {
        return clientId + SEPARATOR + endpointId;
    }

    public String extractClientId(String combinedId) {
        return combinedId.split(SEPARATOR)[0];
    }

    public String extractEndpointId(String combinedId) {
        return combinedId.split(SEPARATOR)[1];
    }
}

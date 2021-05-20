/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.websocket.impl;

import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;

/**
 * Identification service provide means to set and get unique identifiers for websocket clients.
 * There are several identifier elements to distinguish: clientId, endpointId, combinedId. Client id
 * is the identifier of a client that is passed over websocket to the client and back. EndpointId is
 * called to identify a websocket endpoint client connects through. CombinedId is a combination of
 * client and endpoint identifiers separated by a sequence of special charaters, it is used
 * internally.
 */
@Singleton
public class WebsocketIdService {
  public static final String SEPARATOR = "<-:->";
  private static final Random GENERATOR = new Random();

  public static String randomClientId() {
    return String.valueOf(GENERATOR.nextInt(Integer.MAX_VALUE));
  }

  @Inject
  private void configureHandler(RequestHandlerConfigurator requestHandlerConfigurator) {
    requestHandlerConfigurator
        .newConfiguration()
        .methodName("websocketIdService/getId")
        .noParams()
        .resultAsString()
        .withFunction(this::extractClientId);
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

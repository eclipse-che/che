/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons;

import static com.google.common.collect.Sets.newConcurrentHashSet;

import com.google.inject.Singleton;
import java.util.Set;
import javax.inject.Inject;

/** A mechanism for handling subscription from the client and registered its endpointId. */
@Singleton
public class ClientSubscriptionHandler {
  public static final String CLIENT_SUBSCRIBE_METHOD_NAME = "client/subscribe";
  public static final String CLIENT_UNSUBSCRIBE_METHOD_NAME = "client/unsubscribe";

  private final Set<String> endpointIds = newConcurrentHashSet();

  @Inject
  private void configureHandlers(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(CLIENT_SUBSCRIBE_METHOD_NAME)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);

    configurator
        .newConfiguration()
        .methodName(CLIENT_UNSUBSCRIBE_METHOD_NAME)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);
  }

  /** returns set of endpoint ids of all registered clients. */
  public Set<String> getEndpointIds() {
    return endpointIds;
  }
}

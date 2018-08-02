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
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

/**
 * Operation configurator to define an operation to be applied when we handle incoming JSON RPC
 * notification with params object that is represented by a list. As it is an operation there is no
 * result.
 *
 * @param <P> type of params list items
 */
public class ConsumerConfiguratorManyToNone<P> {
  private static final Logger LOGGER = getLogger(ConsumerConfiguratorManyToNone.class);

  private final RequestHandlerManager handlerManager;

  private final String method;
  private final Class<P> pClass;

  ConsumerConfiguratorManyToNone(
      RequestHandlerManager handlerManager, String method, Class<P> pClass) {
    this.handlerManager = handlerManager;

    this.method = method;
    this.pClass = pClass;
  }

  public void withBiConsumer(BiConsumer<String, List<P>> biConsumer) {
    checkNotNull(biConsumer, "Notification consumer must not be null");
    LOGGER.debug(
        "Configuring incoming request: "
            + "binary consumer for method: "
            + method
            + ", "
            + "params list items class: "
            + pClass);

    handlerManager.registerManyToNone(method, pClass, biConsumer);
  }

  public void withConsumer(Consumer<List<P>> consumer) {
    withBiConsumer((endpointId, pValue) -> consumer.accept(pValue));
  }
}

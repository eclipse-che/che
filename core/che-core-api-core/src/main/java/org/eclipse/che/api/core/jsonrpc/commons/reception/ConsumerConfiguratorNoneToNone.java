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

import java.util.function.Consumer;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

/**
 * Operation configurator to define an operation to be applied when we handle incoming JSON RPC
 * notification with not params and no result value.
 */
public class ConsumerConfiguratorNoneToNone {
  private static final Logger LOGGER = getLogger(ConsumerConfiguratorNoneToNone.class);

  private final RequestHandlerManager handlerManager;

  private final String method;

  ConsumerConfiguratorNoneToNone(RequestHandlerManager handlerManager, String method) {
    this.handlerManager = handlerManager;

    this.method = method;
  }

  public void withConsumer(Consumer<String> consumer) {
    checkNotNull(consumer, "Notification consumer must not be null");
    LOGGER.debug("Configuring incoming request binary: consumer for method: " + method);
    handlerManager.registerNoneToNone(method, consumer);
  }

  public void withRunnable(Runnable runnable) {
    withConsumer(s -> runnable.run());
  }
}

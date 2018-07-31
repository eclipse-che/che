/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

/**
 * Function configurator to define a function to be applied when we handle incoming JSON RPC request
 * with no params object while the result of a function is a single object.
 *
 * @param <R> type of result object
 */
public class FunctionConfiguratorNoneToOne<R> {
  private static final Logger LOGGER = getLogger(FunctionConfiguratorNoneToOne.class);
  private final RequestHandlerManager handlerManager;

  private final String method;
  private final Class<R> rClass;

  FunctionConfiguratorNoneToOne(
      RequestHandlerManager handlerManager, String method, Class<R> rClass) {
    this.handlerManager = handlerManager;

    this.method = method;
    this.rClass = rClass;
  }

  /**
   * Define a function to be applied
   *
   * @param function function
   */
  public void withFunction(Function<String, R> function) {
    checkNotNull(function, "Request function must not be null");

    LOGGER.debug(
        "Configuring incoming request binary: "
            + "function for method: "
            + method
            + ", "
            + "result object class: "
            + rClass);

    handlerManager.registerNoneToOne(method, rClass, function);
  }

  /**
   * Define a supplier to be applied
   *
   * @param supplier supplier
   */
  public void withSupplier(Supplier<R> supplier) {
    withFunction(s -> supplier.get());
  }
}

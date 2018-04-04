/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.function.BiFunction;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

/**
 * Function configurator to define a function to be applied when we handle incoming JSON RPC request
 * with params object that is represented by a list. The result of a function is a single object.
 *
 * @param <P> type of params list items
 * @param <R> type of result object
 */
public class FunctionConfiguratorManyToOne<P, R> {
  private static final Logger LOGGER = getLogger(FunctionConfiguratorManyToOne.class);

  private final RequestHandlerManager handlerManager;

  private final String method;
  private final Class<P> pClass;
  private final Class<R> rClass;

  FunctionConfiguratorManyToOne(
      RequestHandlerManager handlerManager, String method, Class<P> pClass, Class<R> rClass) {
    this.handlerManager = handlerManager;

    this.method = method;
    this.pClass = pClass;
    this.rClass = rClass;
  }

  /**
   * Define a function to be applied
   *
   * @param function function
   */
  public void withFunction(BiFunction<String, List<P>, R> function) {
    checkNotNull(function, "Request function must not be null");

    LOGGER.debug(
        "Configuring incoming request: "
            + "binary function for method: "
            + method
            + ", "
            + "params list items class: "
            + pClass
            + ", "
            + "result object class: "
            + rClass);

    handlerManager.registerManyToOne(method, pClass, rClass, function);
  }
}

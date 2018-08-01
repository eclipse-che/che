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

import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

/**
 * Function configurator to define a function to be applied when we handle incoming JSON RPC request
 * with params object that is represented by a single object while the result of a function is also
 * a single object.
 *
 * @param <P> type of params object
 * @param <R> type of result object
 */
public class PromiseConfigurationOneToOne<P, R> {
  private static final Logger LOGGER = getLogger(PromiseConfigurationOneToOne.class);

  private final RequestHandlerManager handlerManager;

  private final String method;
  private final Class<P> pClass;
  private final Class<R> rClass;

  PromiseConfigurationOneToOne(
      RequestHandlerManager handlerManager, String method, Class<P> pClass, Class<R> rClass) {
    this.handlerManager = handlerManager;

    this.method = method;
    this.pClass = pClass;
    this.rClass = rClass;
  }

  /**
   * Define a binary function to be applied
   *
   * @param function function
   */
  public void withPromiseBiFunction(BiFunction<String, P, JsonRpcPromise<R>> function) {
    checkNotNull(function, "Request promise must not be null");

    LOGGER.debug(
        "Configuring incoming request binary: "
            + "function for method: "
            + method
            + ", "
            + "params object class: "
            + pClass
            + ", "
            + "result object class: "
            + rClass);

    handlerManager.registerOneToPromiseOne(method, pClass, rClass, function);
  }

  /**
   * Define a function to be applied
   *
   * @param function function
   */
  public void withPromise(Function<P, JsonRpcPromise<R>> function) {
    withPromiseBiFunction((s, p) -> function.apply(p));
  }
}

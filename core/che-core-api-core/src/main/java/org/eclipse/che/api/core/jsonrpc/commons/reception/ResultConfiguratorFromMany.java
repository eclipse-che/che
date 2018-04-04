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

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

/**
 * Result configurator provide means to configure result type in a response that is to be received.
 * Result types that are supported: {@link String}, {@link Boolean}, {@link Double}, {@link Void}
 * and DTO. This configurator is used when we have defined request params as a list.
 */
public class ResultConfiguratorFromMany<P> {
  private static final Logger LOGGER = getLogger(ResultConfiguratorFromMany.class);

  private final RequestHandlerManager requestHandlerManager;

  private final String method;
  private final Class<P> pClass;

  ResultConfiguratorFromMany(
      RequestHandlerManager requestHandlerManager, String method, Class<P> pClass) {
    this.requestHandlerManager = requestHandlerManager;

    this.method = method;
    this.pClass = pClass;
  }

  public <R> FunctionConfiguratorManyToMany<P, R> resultAsListOfDto(Class<R> rClass) {
    checkNotNull(rClass, "Result class must not be null");

    LOGGER.debug(
        "Configuring incoming request result: method: "
            + method
            + ", result list items class: "
            + rClass);

    return new FunctionConfiguratorManyToMany<>(requestHandlerManager, method, pClass, rClass);
  }

  public FunctionConfiguratorManyToMany<P, String> resultAsListOfString() {
    LOGGER.debug(
        "Configuring incoming request result: method: "
            + method
            + ", result list items class: "
            + String.class);

    return new FunctionConfiguratorManyToMany<>(
        requestHandlerManager, method, pClass, String.class);
  }

  public FunctionConfiguratorManyToMany<P, Double> resultAsListOfDouble() {
    LOGGER.debug(
        "Configuring incoming request result: method: "
            + method
            + ", result list items class: "
            + Double.class);

    return new FunctionConfiguratorManyToMany<>(
        requestHandlerManager, method, pClass, Double.class);
  }

  public FunctionConfiguratorManyToMany<P, Boolean> resultAsListOfBoolean() {
    LOGGER.debug(
        "Configuring incoming request result: method: "
            + method
            + ", result list items class: "
            + Boolean.class);

    return new FunctionConfiguratorManyToMany<>(
        requestHandlerManager, method, pClass, Boolean.class);
  }

  public <R> FunctionConfiguratorManyToOne<P, R> resultAsDto(Class<R> rClass) {
    checkNotNull(rClass, "Result class must not be null");

    LOGGER.debug(
        "Configuring incoming request result: method: "
            + method
            + ", result object class: "
            + rClass);

    return new FunctionConfiguratorManyToOne<>(requestHandlerManager, method, pClass, rClass);
  }

  public FunctionConfiguratorManyToOne<P, String> resultAsString() {
    LOGGER.debug(
        "Configuring incoming request result: method: "
            + method
            + ", result object class: "
            + String.class);

    return new FunctionConfiguratorManyToOne<>(requestHandlerManager, method, pClass, String.class);
  }

  public FunctionConfiguratorManyToOne<P, Double> resultAsDouble() {
    LOGGER.debug(
        "Configuring incoming request result: method: "
            + method
            + ", result object class: "
            + Double.class);

    return new FunctionConfiguratorManyToOne<>(requestHandlerManager, method, pClass, Double.class);
  }

  public FunctionConfiguratorManyToOne<P, Boolean> resultAsBoolean() {
    LOGGER.debug(
        "Configuring incoming request result: method: "
            + method
            + ", result object class: "
            + Double.class);

    return new FunctionConfiguratorManyToOne<>(
        requestHandlerManager, method, pClass, Boolean.class);
  }

  public ConsumerConfiguratorManyToNone<P> noResult() {
    LOGGER.debug("Configuring incoming request having no result");

    return new ConsumerConfiguratorManyToNone<>(requestHandlerManager, method, pClass);
  }
}

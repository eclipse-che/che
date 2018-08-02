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

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

/**
 * Result configurator provide means to configure result type in a response that is to be received.
 * Result types that are supported: {@link String}, {@link Boolean}, {@link Double}, {@link Void}
 * and DTO. This configurator is used when we have defined request params as a single object.
 */
public class ResultConfiguratorFromOne<P> {
  private static final Logger LOGGER = getLogger(ResultConfiguratorFromOne.class);

  private final RequestHandlerManager requestHandlerManager;

  private final String method;
  private final Class<P> pClass;

  ResultConfiguratorFromOne(
      RequestHandlerManager requestHandlerManager, String method, Class<P> pClass) {
    this.requestHandlerManager = requestHandlerManager;

    this.method = method;
    this.pClass = pClass;
  }

  public <R> FunctionConfiguratorOneToMany<P, R> resultAsListOfDto(Class<R> rClass) {
    checkNotNull(rClass, "Result class must not be null");

    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result list items class: "
            + rClass);

    return new FunctionConfiguratorOneToMany<>(requestHandlerManager, method, pClass, rClass);
  }

  public FunctionConfiguratorOneToMany<P, String> resultAsListOfString() {
    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result list items class: "
            + String.class);

    return new FunctionConfiguratorOneToMany<>(requestHandlerManager, method, pClass, String.class);
  }

  public FunctionConfiguratorOneToMany<P, Double> resultAsListOfDouble() {
    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result list items class: "
            + Double.class);

    return new FunctionConfiguratorOneToMany<>(requestHandlerManager, method, pClass, Double.class);
  }

  public FunctionConfiguratorOneToMany<P, Boolean> resultAsListOfBoolean() {
    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result list items class: "
            + Boolean.class);

    return new FunctionConfiguratorOneToMany<>(
        requestHandlerManager, method, pClass, Boolean.class);
  }

  public <R> FunctionConfiguratorOneToOne<P, R> resultAsDto(Class<R> rClass) {
    checkNotNull(rClass, "Result class must not be null");

    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + rClass);

    return new FunctionConfiguratorOneToOne<>(requestHandlerManager, method, pClass, rClass);
  }

  public <R> PromiseConfigurationOneToOne<P, R> resultAsPromiseDto(Class<R> rClass) {
    checkNotNull(rClass, "Result class must not be null");

    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + rClass);

    return new PromiseConfigurationOneToOne<>(requestHandlerManager, method, pClass, rClass);
  }

  public FunctionConfiguratorOneToOne<P, String> resultAsString() {
    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + String.class);

    return new FunctionConfiguratorOneToOne<>(requestHandlerManager, method, pClass, String.class);
  }

  public FunctionConfiguratorOneToOne<P, Double> resultAsDouble() {
    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + Double.class);

    return new FunctionConfiguratorOneToOne<>(requestHandlerManager, method, pClass, Double.class);
  }

  public FunctionConfiguratorOneToOne<P, Boolean> resultAsBoolean() {
    LOGGER.debug(
        "Configuring incoming request result: "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + Boolean.class);

    return new FunctionConfiguratorOneToOne<>(requestHandlerManager, method, pClass, Boolean.class);
  }

  public ConsumerConfiguratorOneToNone<P> noResult() {
    LOGGER.debug("Configuring incoming request having no result");

    return new ConsumerConfiguratorOneToNone<>(requestHandlerManager, method, pClass);
  }
}

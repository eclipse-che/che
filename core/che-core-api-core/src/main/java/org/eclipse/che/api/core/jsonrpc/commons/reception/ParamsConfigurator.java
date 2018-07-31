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

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

/**
 * Params configurator provide means to configure params type in a request that is to be handled.
 * Params types that are supported: {@link String}, {@link Boolean}, {@link Double}, {@link Void}
 * and DTO.
 */
public class ParamsConfigurator {
  private static final Logger LOGGER = getLogger(ParamsConfigurator.class);

  private final RequestHandlerManager requestHandlerManager;

  private final String method;

  ParamsConfigurator(RequestHandlerManager requestHandlerManager, String method) {
    this.requestHandlerManager = requestHandlerManager;

    this.method = method;
  }

  public <P> ResultConfiguratorFromMany<P> paramsAsListOfDto(Class<P> pClass) {
    checkNotNull(pClass, "Params class must not be null");

    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params list items class: "
            + pClass);

    return new ResultConfiguratorFromMany<>(requestHandlerManager, method, pClass);
  }

  public ResultConfiguratorFromMany<Double> paramsAsListOfDouble() {
    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params list items class: "
            + Double.class);

    return new ResultConfiguratorFromMany<>(requestHandlerManager, method, Double.class);
  }

  public ResultConfiguratorFromMany<String> paramsAsListOfString() {
    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params list items class: "
            + String.class);

    return new ResultConfiguratorFromMany<>(requestHandlerManager, method, String.class);
  }

  public ResultConfiguratorFromMany<Boolean> paramsAsListOfBoolean() {
    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params list items class: "
            + Boolean.class);

    return new ResultConfiguratorFromMany<>(requestHandlerManager, method, Boolean.class);
  }

  public <P> ResultConfiguratorFromOne<P> paramsAsDto(Class<P> pClass) {
    checkNotNull(pClass, "Params class must not be null");

    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + pClass);

    return new ResultConfiguratorFromOne<>(requestHandlerManager, method, pClass);
  }

  public ResultConfiguratorFromNone noParams() {
    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + Void.class);

    return new ResultConfiguratorFromNone(requestHandlerManager, method);
  }

  public ResultConfiguratorFromOne<String> paramsAsString() {
    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + String.class);

    return new ResultConfiguratorFromOne<>(requestHandlerManager, method, String.class);
  }

  public ResultConfiguratorFromOne<Double> paramsAsDouble() {
    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + Double.class);

    return new ResultConfiguratorFromOne<>(requestHandlerManager, method, Double.class);
  }

  public ResultConfiguratorFromOne<Boolean> paramsAsBoolean() {
    LOGGER.debug(
        "Configuring incoming request params: "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + Boolean.class);

    return new ResultConfiguratorFromOne<>(requestHandlerManager, method, Boolean.class);
  }
}

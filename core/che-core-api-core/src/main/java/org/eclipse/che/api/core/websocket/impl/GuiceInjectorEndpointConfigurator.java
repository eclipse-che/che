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
package org.eclipse.che.api.core.websocket.impl;

import com.google.inject.Injector;
import javax.inject.Inject;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Allows inject Guice instances on WEB SOCKET endpoint creation.
 *
 * @author Dmitry Kuleshov
 */
public class GuiceInjectorEndpointConfigurator extends ServerEndpointConfig.Configurator {
  @Inject private static Injector injector;

  public <T> T getEndpointInstance(Class<T> endpointClass) {
    return injector.getInstance(endpointClass);
  }
}

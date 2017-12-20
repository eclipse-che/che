/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.command.exec;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.ide.api.command.exec.dto.event.ConnectedEventDto;

/**
 * Handles 'connected' event, the event is fired when we firstly connect to exec agent.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ConnectedEventHandler implements BiConsumer<String, ConnectedEventDto> {

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("connected")
        .paramsAsDto(ConnectedEventDto.class)
        .noResult()
        .withBiConsumer(this);
  }

  @Override
  public void accept(String endpointId, ConnectedEventDto params) {}
}

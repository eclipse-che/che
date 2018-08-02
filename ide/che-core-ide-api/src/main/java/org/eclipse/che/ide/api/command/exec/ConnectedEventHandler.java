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
package org.eclipse.che.ide.api.command.exec;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.function.BiConsumer;
import org.eclipse.che.agent.exec.shared.dto.event.ConnectedEventDto;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;

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

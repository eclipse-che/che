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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events;

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerStatusChangedEvent;

/**
 * Configure JSON_RPC consumers of Che plugin broker events. Also converts {@link
 * BrokerStatusChangedEvent} to {@link BrokerEvent}.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksander Garagatyi
 */
@Beta
@Singleton
public class BrokerService {

  public static final String BROKER_RESULT_METHOD = "broker/result";
  public static final String BROKER_STATUS_CHANGED_METHOD = "broker/statusChanged";
  public static final String BROKER_LOG_METHOD = "broker/log";

  private BrokerEventsHandler eventsHandler;

  @Inject
  public BrokerService(BrokerEventsHandler eventsHandler) {
    this.eventsHandler = eventsHandler;
  }

  @Inject
  public void configureMethods(RequestHandlerConfigurator requestHandler) {
    requestHandler
        .newConfiguration()
        .methodName(BROKER_STATUS_CHANGED_METHOD)
        .paramsAsDto(BrokerStatusChangedEvent.class)
        .noResult()
        .withConsumer(eventsHandler::handle);

    requestHandler
        .newConfiguration()
        .methodName(BROKER_RESULT_METHOD)
        .paramsAsDto(BrokerStatusChangedEvent.class)
        .noResult()
        .withConsumer(eventsHandler::handle);

    requestHandler
        .newConfiguration()
        .methodName(BROKER_LOG_METHOD)
        .paramsAsDto(BrokerLogEvent.class)
        .noResult()
        .withConsumer(eventsHandler::handle);
  }
}

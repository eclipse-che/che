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
package org.eclipse.che.api.debugger.server;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.eclipse.che.api.debugger.server.DtoConverter.asDto;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.Singleton;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto;
import org.eclipse.che.api.debug.shared.dto.event.DisconnectEventDto;
import org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;

/** Send debugger events using JSON RPC to the clients */
@Singleton
public class DebuggerJsonRpcMessenger implements EventSubscriber<DebuggerMessage> {
  private static final String EVENT_DEBUGGER_MESSAGE_BREAKPOINT = "event:debugger:breakpoint";
  private static final String EVENT_DEBUGGER_MESSAGE_DISCONNECT = "event:debugger:disconnect";
  private static final String EVENT_DEBUGGER_MESSAGE_SUSPEND = "event:debugger:suspend";
  private static final String EVENT_DEBUGGER_UN_SUBSCRIBE = "event:debugger:un-subscribe";
  private static final String EVENT_DEBUGGER_SUBSCRIBE = "event:debugger:subscribe";

  private final EventService eventService;
  private final RequestTransmitter transmitter;

  private final Set<String> endpointIds = newConcurrentHashSet();

  @Inject
  public DebuggerJsonRpcMessenger(EventService eventService, RequestTransmitter transmitter) {
    this.eventService = eventService;
    this.transmitter = transmitter;
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this);
  }

  @PreDestroy
  private void unsubscribe() {
    eventService.unsubscribe(this);
  }

  @Override
  public void onEvent(DebuggerMessage event) {
    switch (event.getDebuggerEvent().getType()) {
      case SUSPEND:
        SuspendEvent suspendEvent = (SuspendEvent) event.getDebuggerEvent();
        final SuspendEventDto suspendEventDto =
            newDto(SuspendEventDto.class)
                .withType(DebuggerEvent.TYPE.SUSPEND)
                .withLocation(asDto(suspendEvent.getLocation()))
                .withSuspendPolicy(suspendEvent.getSuspendPolicy());
        endpointIds.forEach(
            it ->
                transmitter
                    .newRequest()
                    .endpointId(it)
                    .methodName(EVENT_DEBUGGER_MESSAGE_SUSPEND)
                    .paramsAsDto(suspendEventDto)
                    .sendAndSkipResult());
        break;
      case BREAKPOINT_ACTIVATED:
        final BreakpointDto breakpointDto =
            asDto(((BreakpointActivatedEvent) event.getDebuggerEvent()).getBreakpoint());
        final BreakpointActivatedEventDto breakpointActivatedEvent =
            newDto(BreakpointActivatedEventDto.class)
                .withType(DebuggerEvent.TYPE.BREAKPOINT_ACTIVATED)
                .withBreakpoint(breakpointDto);
        endpointIds.forEach(
            it ->
                transmitter
                    .newRequest()
                    .endpointId(it)
                    .methodName(EVENT_DEBUGGER_MESSAGE_BREAKPOINT)
                    .paramsAsDto(breakpointActivatedEvent)
                    .sendAndSkipResult());
        break;
      case DISCONNECT:
        final DisconnectEventDto disconnectEvent =
            newDto(DisconnectEventDto.class).withType(DebuggerEvent.TYPE.DISCONNECT);
        endpointIds.forEach(
            it ->
                transmitter
                    .newRequest()
                    .endpointId(it)
                    .methodName(EVENT_DEBUGGER_MESSAGE_DISCONNECT)
                    .paramsAsDto(disconnectEvent)
                    .sendAndSkipResult());
        break;
      default:
    }
  }

  @Inject
  private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(EVENT_DEBUGGER_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);
  }

  @Inject
  private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(EVENT_DEBUGGER_UN_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);
  }
}

/*
 * Copyright (c) 2016-2017 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.server.executor;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_CHANNEL_OUTPUT;
import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_CHANNEL_SUBSCRIBE;
import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_CHANNEL_UNSUBSCRIBE;

import com.google.inject.Singleton;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.composer.shared.dto.ComposerOutput;

/**
 * Mechanism which sends events of Composer by using JSON RPC to the client.
 *
 * @author Kaloyan Raev
 */
@Singleton
public class ComposerJsonRpcMessenger implements EventSubscriber<ComposerOutput> {
  private final Set<String> endpointIds = newConcurrentHashSet();
  private EventService eventService;
  private RequestTransmitter transmitter;

  @Inject
  public ComposerJsonRpcMessenger(EventService eventService, RequestTransmitter transmitter) {
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

  @Inject
  private void configureHandlers(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(COMPOSER_CHANNEL_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);

    configurator
        .newConfiguration()
        .methodName(COMPOSER_CHANNEL_UNSUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);
  }

  @Override
  public void onEvent(ComposerOutput event) {
    ComposerOutput composerOutput = DtoFactory.newDto(ComposerOutput.class);
    composerOutput.setOutput(event.getOutput());
    composerOutput.setState(event.getState());

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(COMPOSER_CHANNEL_OUTPUT)
                .paramsAsDto(composerOutput)
                .sendAndSkipResult());
  }
}

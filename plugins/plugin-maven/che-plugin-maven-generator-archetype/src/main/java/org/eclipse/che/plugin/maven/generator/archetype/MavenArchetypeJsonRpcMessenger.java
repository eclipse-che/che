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
package org.eclipse.che.plugin.maven.generator.archetype;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ARCHETYPE_CHANEL_OUTPUT;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ARCHETYPE_CHANEL_SUBSCRIBE;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ARCHETYPE_CHANEL_UNSUBSCRIBE;

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
import org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput;

/** Mechanism which sends events of maven archetype generation by using JSON RPC to the client. */
@Singleton
public class MavenArchetypeJsonRpcMessenger implements EventSubscriber<ArchetypeOutput> {
  private final Set<String> endpointIds = newConcurrentHashSet();
  private EventService eventService;
  private RequestTransmitter transmitter;

  @Inject
  public MavenArchetypeJsonRpcMessenger(EventService eventService, RequestTransmitter transmitter) {
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
        .methodName(MAVEN_ARCHETYPE_CHANEL_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);

    configurator
        .newConfiguration()
        .methodName(MAVEN_ARCHETYPE_CHANEL_UNSUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);
  }

  @Override
  public void onEvent(ArchetypeOutput event) {
    ArchetypeOutput archetypeOutput = DtoFactory.newDto(ArchetypeOutput.class);
    archetypeOutput.setOutput(event.getOutput());
    archetypeOutput.setState(event.getState());

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(MAVEN_ARCHETYPE_CHANEL_OUTPUT)
                .paramsAsDto(archetypeOutput)
                .sendAndSkipResult());
  }
}

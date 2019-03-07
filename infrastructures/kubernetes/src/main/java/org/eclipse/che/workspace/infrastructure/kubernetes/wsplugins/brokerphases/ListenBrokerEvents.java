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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import com.google.common.annotations.Beta;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import javax.annotation.Nullable;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesPluginsToolingValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscribes to Che plugin broker events, passes future that should be completed upon broker result
 * received to {@link BrokerStatusListener} and calls next {@link BrokerPhase}.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class ListenBrokerEvents extends BrokerPhase {

  private static final Logger LOG = LoggerFactory.getLogger(ListenBrokerEvents.class);

  private final String workspaceId;
  private final BrokersResult brokersResult;
  private final EventService eventService;
  private final KubernetesPluginsToolingValidator pluginsValidator;
  @Nullable private final Tracer tracer;

  public ListenBrokerEvents(
      String workspaceId,
      KubernetesPluginsToolingValidator pluginsValidator,
      BrokersResult brokersResult,
      EventService eventService,
      @Nullable Tracer tracer) {
    this.workspaceId = workspaceId;
    this.pluginsValidator = pluginsValidator;
    this.brokersResult = brokersResult;
    this.eventService = eventService;
    this.tracer = tracer;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    Span tracingSpan = startTracingPhase(tracer, "ListenBrokerEvents", workspaceId);
    BrokerStatusListener brokerStatusListener =
        new BrokerStatusListener(workspaceId, pluginsValidator, brokersResult);
    try {
      LOG.debug("Subscribing broker events listener for workspace '{}'", workspaceId);
      eventService.subscribe(brokerStatusListener, BrokerEvent.class);

      finishSpanIfExists(tracingSpan);
      return nextPhase.execute();
    } finally {
      eventService.unsubscribe(brokerStatusListener);
    }
  }
}

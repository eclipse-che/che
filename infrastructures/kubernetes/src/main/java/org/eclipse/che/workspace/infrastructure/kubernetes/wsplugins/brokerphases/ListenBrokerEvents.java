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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesPluginsToolingValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerStatusListener;

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

  private final String workspaceId;
  private final CompletableFuture<List<ChePlugin>> toolingFuture;
  private final EventService eventService;
  private final KubernetesPluginsToolingValidator pluginsValidator;

  public ListenBrokerEvents(
      String workspaceId,
      KubernetesPluginsToolingValidator pluginsValidator,
      CompletableFuture<List<ChePlugin>> toolingFuture,
      EventService eventService) {
    this.workspaceId = workspaceId;
    this.pluginsValidator = pluginsValidator;
    this.toolingFuture = toolingFuture;
    this.eventService = eventService;
  }

  public List<ChePlugin> execute() throws InfrastructureException {
    BrokerStatusListener brokerStatusListener =
        new BrokerStatusListener(workspaceId, pluginsValidator, toolingFuture);
    try {
      eventService.subscribe(brokerStatusListener, BrokerEvent.class);

      return nextPhase.execute();
    } finally {
      eventService.unsubscribe(brokerStatusListener);
    }
  }
}

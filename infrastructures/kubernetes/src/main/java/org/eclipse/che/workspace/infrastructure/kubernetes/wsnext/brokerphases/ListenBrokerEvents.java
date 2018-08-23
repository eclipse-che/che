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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.brokerphases;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.events.BrokerEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.events.BrokerResultListener;

/**
 * Subscribes to Che plugin broker events, passes future that should be completed upon broker result
 * received to {@link BrokerResultListener} and calls next {@link BrokerPhase}.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class ListenBrokerEvents implements BrokerPhase {

  private final String workspaceId;
  private final CompletableFuture<List<ChePlugin>> toolingFuture;
  private final BrokerPhase nextPhase;
  private final EventService eventService;

  public ListenBrokerEvents(
      BrokerPhase nextPhase,
      String workspaceId,
      CompletableFuture<List<ChePlugin>> toolingFuture,
      EventService eventService) {
    this.workspaceId = workspaceId;
    this.toolingFuture = toolingFuture;
    this.nextPhase = nextPhase;
    this.eventService = eventService;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    BrokerResultListener brokerResultListener =
        new BrokerResultListener(workspaceId, toolingFuture);
    try {
      eventService.subscribe(brokerResultListener, BrokerEvent.class);

      return nextPhase.execute();
    } finally {
      eventService.unsubscribe(brokerResultListener);
    }
  }
}

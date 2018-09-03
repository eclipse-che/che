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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;

/**
 * Listens for {@link BrokerEvent} and completes or exceptionally completes a future depending on
 * the event state.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class BrokerResultListener implements EventSubscriber<BrokerEvent> {

  private final String workspaceId;
  private final CompletableFuture<List<ChePlugin>> finishFuture;

  public BrokerResultListener(String workspaceId, CompletableFuture<List<ChePlugin>> finishFuture) {
    this.workspaceId = workspaceId;
    this.finishFuture = finishFuture;
  }

  @Override
  public void onEvent(BrokerEvent event) {
    if (!workspaceId.equals(event.getWorkspaceId())) {
      return;
    }

    switch (event.getStatus()) {
      case DONE:
        finishFuture.complete(event.getTooling());
        break;
      case FAILED:
        finishFuture.completeExceptionally(
            new InfrastructureException("Broker process failed with error: " + event.getError()));
        break;
      default:
        // do nothing
    }
  }
}

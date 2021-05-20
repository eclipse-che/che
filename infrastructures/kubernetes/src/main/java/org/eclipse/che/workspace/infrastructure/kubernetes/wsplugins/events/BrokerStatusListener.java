/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesPluginsToolingValidator;
import org.slf4j.Logger;

/**
 * Listens for {@link BrokerEvent} and completes or exceptionally completes a start and done futures
 * depending on the event state.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class BrokerStatusListener implements EventSubscriber<BrokerEvent> {

  private static final Logger LOG = getLogger(BrokerStatusListener.class);

  private final String workspaceId;
  private final KubernetesPluginsToolingValidator pluginsValidator;
  private final BrokersResult brokersResult;

  public BrokerStatusListener(
      String workspaceId,
      KubernetesPluginsToolingValidator pluginsValidator,
      BrokersResult brokersResult) {
    this.workspaceId = workspaceId;
    this.pluginsValidator = pluginsValidator;
    this.brokersResult = brokersResult;
  }

  @Override
  public void onEvent(BrokerEvent event) {
    if (event.getRuntimeId() == null
        || !workspaceId.equals(event.getRuntimeId().getWorkspaceId())) {
      return;
    }

    switch (event.getStatus()) {
      case DONE:
        List<ChePlugin> tooling = event.getTooling();
        if (tooling != null) {
          try {
            pluginsValidator.validatePluginNames(tooling);
          } catch (ValidationException e) {
            brokersResult.error(e);
            return;
          }
          try {
            brokersResult.setResult(tooling);
          } catch (InfrastructureException e) {
            LOG.error(e.getLocalizedMessage(), e);
          }
        } else {
          brokersResult.error(
              new InternalInfrastructureException(
                  format(
                      "Plugin brokering process for workspace `%s` is finished but plugins list is missing",
                      workspaceId)));
        }
        break;
      case FAILED:
        brokersResult.error(
            new InfrastructureException(
                format(
                    "Plugin brokering process for workspace %s failed with error: %s",
                    workspaceId, event.getError())));
        break;
      case STARTED:
      default:
        // do nothing
    }
  }
}

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
package org.eclipse.che.ide.workspace.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.util.loging.Log;

@Singleton
public class WorkspaceAgentOutputHandler {
  @Inject
  public void configureWorkspaceOutputMessageHandler(
      AppContext appContext, RequestHandlerConfigurator configurator, EventBus eventBus) {
    BiConsumer<String, List<String>> operation =
        (String endpointId, List<String> messages) -> {
          Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

          if (appContext.getActiveRuntime() == null || appContext.getDevMachine() == null) {
            Log.error(
                getClass(), "Runtime or dev machine is not properly initialized for this action");
            return;
          }

          if (messages.isEmpty()) {
            Log.error(getClass(), "Received empty messages list");
            return;
          }

          eventBus.fireEvent(
              new EnvironmentOutputEvent(
                  messages.get(0), appContext.getDevMachine().getDisplayName()));
        };

    configurator
        .newConfiguration()
        .methodName("event:ws-agent-output:message")
        .paramsAsListOfString()
        .noResult()
        .withBiConsumer(operation);
  }
}

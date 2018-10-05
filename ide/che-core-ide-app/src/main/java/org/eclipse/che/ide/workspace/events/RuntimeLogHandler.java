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
package org.eclipse.che.ide.workspace.events;

import static org.eclipse.che.api.workspace.shared.Constants.RUNTIME_LOG_METHOD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;
import org.eclipse.che.ide.processes.panel.EnvironmentOutputEvent;

@Singleton
class RuntimeLogHandler {

  @Inject
  RuntimeLogHandler(RequestHandlerConfigurator configurator, EventBus eventBus) {
    configurator
        .newConfiguration()
        .methodName(RUNTIME_LOG_METHOD)
        .paramsAsDto(RuntimeLogEvent.class)
        .noResult()
        .withBiConsumer(
            (endpointId, log) ->
                eventBus.fireEvent(
                    new EnvironmentOutputEvent(log.getText(), log.getMachineName())));
  }
}

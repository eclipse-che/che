/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace.events;

import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_STATUS_CHANGED_METHOD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerStatusEvent;
import org.eclipse.che.ide.api.workspace.event.InstallerFailedEvent;
import org.eclipse.che.ide.api.workspace.event.InstallerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.InstallerStartingEvent;

@Singleton
class InstallerStatusEventHandler {

  @Inject
  InstallerStatusEventHandler(RequestHandlerConfigurator configurator, EventBus eventBus) {
    configurator
        .newConfiguration()
        .methodName(INSTALLER_STATUS_CHANGED_METHOD)
        .paramsAsDto(InstallerStatusEvent.class)
        .noResult()
        .withBiConsumer(
            (endpointId, event) -> {
              switch (event.getStatus()) {
                case STARTING:
                  eventBus.fireEvent(
                      new InstallerStartingEvent(event.getInstaller(), event.getMachineName()));
                  break;
                case RUNNING:
                  eventBus.fireEvent(
                      new InstallerRunningEvent(
                          event.getInstaller(), event.getMachineName(), true));
                  break;
                case DONE:
                  eventBus.fireEvent(
                      new InstallerRunningEvent(
                          event.getInstaller(), event.getMachineName(), false));
                  break;
                case FAILED:
                  eventBus.fireEvent(
                      new InstallerFailedEvent(
                          event.getInstaller(), event.getError(), event.getMachineName()));
                  break;
              }
            });
  }
}

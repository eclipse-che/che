/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_LOG_METHOD;

/**
 * Defines a JSON-RPC methods for handling machine output.
 *
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
@Singleton
public class OutputService {

    private final EventService eventService;

    @Inject
    public OutputService(EventService eventService) {
        this.eventService = eventService;
    }

    @Inject
    public void configureMethods(RequestHandlerConfigurator requestHandler) {
        requestHandler.newConfiguration()
                      .methodName(INSTALLER_LOG_METHOD)
                      .paramsAsDto(InstallerLogEvent.class)
                      .noResult()
                      .withConsumer(this::handleInstallerLog);
    }

    private void handleInstallerLog(InstallerLogEvent installerStatusEvent) {
        eventService.publish(installerStatusEvent);
    }

}

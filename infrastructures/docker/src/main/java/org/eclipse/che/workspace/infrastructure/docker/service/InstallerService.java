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
package org.eclipse.che.workspace.infrastructure.docker.service;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerOutputEvent;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class InstallerService {

    private static final Logger LOG = LoggerFactory.getLogger(InstallerService.class);

    private final RequestHandlerConfigurator requestHandler;
    private final EventService eventService;

    @Inject
    public InstallerService(RequestHandlerConfigurator requestHandler, EventService eventService) {
        this.requestHandler = requestHandler;
        this.eventService = eventService;
    }

    @PostConstruct
    public void configureMethods() {

        requestHandler.newConfiguration()
                      .methodName("status")
                      .paramsAsDto(InstallerStatusEvent.class)
                      .noResult()
                      .withConsumer(this::handleInstallerStatus);

        requestHandler.newConfiguration()
                      .methodName("server/status")
                      .paramsAsDto(ServerStatusEvent.class)
                      .noResult()
                      .withConsumer(this::handleServerStatus);

        requestHandler.newConfiguration()
                      .methodName("logs/stdout")
                      .paramsAsDto(InstallerOutputEvent.class)
                      .noResult()
                      .withConsumer(this::handleInstallerOutput);

        requestHandler.newConfiguration()
                      .methodName("logs/stderr")
                      .paramsAsDto(InstallerOutputEvent.class)
                      .noResult()
                      .withConsumer(this::handleInstallerError);
    }



    private void handleInstallerStatus(InstallerStatusEvent installerStatusEvent) {
        //TODO: spi actions here
       eventService.publish(installerStatusEvent);
    }

    private void handleServerStatus(ServerStatusEvent serverStatusEvent) {
        //TODO: spi actions here
        eventService.publish(serverStatusEvent);
    }

    private void handleInstallerOutput(InstallerOutputEvent installerOutputEvent) {
        //TODO: spi actions here
        eventService.publish(installerOutputEvent);
    }

    private void handleInstallerError(InstallerOutputEvent installerOutputEvent) {
        //TODO: spi actions here
        eventService.publish(installerOutputEvent);
    }
}

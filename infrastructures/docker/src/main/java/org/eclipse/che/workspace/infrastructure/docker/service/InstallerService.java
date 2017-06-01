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
                      .methodName("installAgent/status")
                      .paramsAsDto(InstallerStatusEvent.class)
                      .noResult()
                      .withConsumer(this::handleInstallerStatus);

        requestHandler.newConfiguration()
                      .methodName("status")
                      .paramsAsDto(InstallerStatusEvent.class)
                      .noResult()
                      .withConsumer(this::handleAgentStatus);

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


    private void handleInstallerStatus(InstallerStatusEvent installerStatusEvent ) {
      LOG.info("Status:" + installerStatusEvent.getStatus() + ", timestamp:" + installerStatusEvent.getTimestamp());
      eventService.publish(installerStatusEvent);
    }

    private void handleAgentStatus(InstallerStatusEvent installerStatusEvent ) {
        LOG.info("Status:" + installerStatusEvent.getStatus() + ", timestamp:" + installerStatusEvent.getTimestamp());
       eventService.publish(installerStatusEvent);
    }

    private void handleInstallerOutput(InstallerOutputEvent installerOutputEvent) {
        LOG.info("Text:" + installerOutputEvent.getText() + ", timestamp:" + installerOutputEvent.getTimestamp());
        eventService.publish(installerOutputEvent);
    }

    private void handleInstallerError(InstallerOutputEvent installerOutputEvent) {
        LOG.info("Text:" + installerOutputEvent.getText() + ", timestamp:" + installerOutputEvent.getTimestamp());
        eventService.publish(installerOutputEvent);
    }
}

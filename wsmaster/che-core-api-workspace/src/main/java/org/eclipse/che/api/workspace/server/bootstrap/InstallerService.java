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
package org.eclipse.che.api.workspace.server.bootstrap;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.BootstrapperStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Service for handling bootstrapper & installer events.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class InstallerService {

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
                      .methodName("bootstrapper/statusChanged")
                      .paramsAsDto(BootstrapperStatusEvent.class)
                      .noResult()
                      .withConsumer(this::handleBootstrapperStatus);

        requestHandler.newConfiguration()
                      .methodName("installer/statusChanged")
                      .paramsAsDto(InstallerStatusEvent.class)
                      .noResult()
                      .withConsumer(this::handleInstallerStatus);
    }

    private void handleInstallerStatus(InstallerStatusEvent installerStatusEvent) {
        //TODO: spi actions here
       eventService.publish(installerStatusEvent);
    }

    private void handleBootstrapperStatus(BootstrapperStatusEvent bootstrapperStatusEvent) {
        //TODO: spi actions here
        eventService.publish(bootstrapperStatusEvent);
    }

}

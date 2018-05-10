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
package org.eclipse.che.api.workspace.server.bootstrap;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.BootstrapperStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.api.workspace.shared.Constants.*;

/**
 * Defines a JSON-RPC service for handling bootstrapper statuses and installer statuses/output.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Anton Korneta
 */
@Singleton
public class InstallerService {

  private final EventService eventService;

  private static final Logger LOG = LoggerFactory.getLogger(InstallerService.class);

  @Inject
  public InstallerService(EventService eventService) {
    this.eventService = eventService;
  }

  @Inject
  public void configureMethods(RequestHandlerConfigurator requestHandler) {
    requestHandler
        .newConfiguration()
        .methodName(INSTALLER_LOG_METHOD)
        .paramsAsDto(InstallerLogEvent.class)
        .noResult()
        .withConsumer(this::handle);

    requestHandler
        .newConfiguration()
        .methodName(BOOTSTRAPPER_STATUS_CHANGED_METHOD)
        .paramsAsDto(BootstrapperStatusEvent.class)
        .noResult()
        .withConsumer(this::handle);

    requestHandler
        .newConfiguration()
        .methodName(INSTALLER_STATUS_CHANGED_METHOD)
        .paramsAsDto(InstallerStatusEvent.class)
        .noResult()
        .withConsumer(this::handle);
  }

  private <T> void handle(T event) {
    if (event instanceof InstallerLogEvent) {
      InstallerLogEvent e = (InstallerLogEvent) event;
      RuntimeIdentityDto identity = e.getRuntimeId();
      LOG.error(
          "Installer {} machine {} owner {} env {} workspace {} stream {} text {} time {} ",
          e.getInstaller(),
          e.getMachineName(),
          identity.getOwnerId(),
          identity.getEnvName(),
          identity.getWorkspaceId(),
          e.getStream(),
          e.getText(),
          e.getTime());
    }
    eventService.publish(event);
  }
}

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
package org.eclipse.che.api.logger;

import static com.google.common.base.Strings.isNullOrEmpty;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The goal of this class is to catch all InstallerLogEvent events from error stream and dump them
 * to slf4j log.
 */
@Singleton
public class ErrorInstallerLogEventLogger implements EventSubscriber<InstallerLogEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(ErrorInstallerLogEventLogger.class);

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this, InstallerLogEvent.class);
  }

  @Override
  public void onEvent(InstallerLogEvent event) {
    if (event.getStream() == InstallerLogEvent.Stream.STDERR && !isNullOrEmpty(event.getText())) {
      RuntimeIdentityDto identity = event.getRuntimeId();
      LOG.error(
          "Installer `{}` error from machine=`{}` owner=`{}` env=`{}` workspace=`{}` text=`{}` time=`{}`",
          event.getInstaller(),
          event.getMachineName(),
          identity.getOwnerId(),
          identity.getEnvName(),
          identity.getWorkspaceId(),
          event.getText(),
          event.getTime());
    }
  }
}

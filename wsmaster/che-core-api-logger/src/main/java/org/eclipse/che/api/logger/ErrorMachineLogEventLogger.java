/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.che.api.workspace.shared.dto.event.MachineLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The goal of this class is to catch all MachineLogEvent events from error stream and dump them to
 * slf4j log.
 */
@Singleton
public class ErrorMachineLogEventLogger implements EventSubscriber<MachineLogEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(ErrorMachineLogEventLogger.class);

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this, MachineLogEvent.class);
  }

  @Override
  public void onEvent(MachineLogEvent event) {
    if ("stderr".equalsIgnoreCase(event.getStream()) && !isNullOrEmpty(event.getText())) {
      RuntimeIdentityDto identity = event.getRuntimeId();
      LOG.error(
          "Machine `{}` error from owner=`{}` env=`{}` workspace=`{}` text=`{}` time=`{}`",
          event.getMachineName(),
          identity.getOwnerId(),
          identity.getEnvName(),
          identity.getWorkspaceId(),
          event.getText(),
          event.getTime());
    }
  }
}

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
package org.eclipse.che.multiuser.machine.authentication.server;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

/**
 * Clears all machine tokens associated with stopped workspace.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class MachineSessionInvalidator implements EventSubscriber<WorkspaceStatusEvent> {

  private final MachineTokenRegistry tokenRegistry;
  private final EventService eventService;

  @Inject
  public MachineSessionInvalidator(MachineTokenRegistry tokenRegistry, EventService eventService) {
    this.tokenRegistry = tokenRegistry;
    this.eventService = eventService;
  }

  @Override
  public void onEvent(WorkspaceStatusEvent event) {
    if (WorkspaceStatus.STOPPED.equals(event.getStatus())) {
      tokenRegistry.removeTokens(event.getWorkspaceId()).values();
    }
  }

  @PostConstruct
  @VisibleForTesting
  void subscribe() {
    eventService.subscribe(this);
  }
}

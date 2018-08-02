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
package org.eclipse.che.multiuser.machine.authentication.server;

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

  @Inject
  public MachineSessionInvalidator(MachineTokenRegistry tokenRegistry) {
    this.tokenRegistry = tokenRegistry;
  }

  @Override
  public void onEvent(WorkspaceStatusEvent event) {
    if (WorkspaceStatus.STOPPED.equals(event.getStatus())) {
      tokenRegistry.removeTokens(event.getWorkspaceId()).values();
    }
  }

  @Inject
  private void subscribe(EventService eventService) {
    eventService.subscribe(this);
  }
}

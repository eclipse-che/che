/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for removing OpenShift project on {@code WorkspaceRemovedEvent}.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class RemoveProjectOnWorkspaceRemove implements EventSubscriber<WorkspaceRemovedEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(RemoveProjectOnWorkspaceRemove.class);

  private final OpenShiftProjectFactory projectFactory;

  @Inject
  public RemoveProjectOnWorkspaceRemove(OpenShiftProjectFactory projectFactory) {
    this.projectFactory = projectFactory;
  }

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(WorkspaceRemovedEvent event) {
    try {
      projectFactory.deleteIfManaged(event.getWorkspace());
    } catch (InfrastructureException e) {
      LOG.warn(
          "Fail to remove OpenShift project for workspace with id {}. Cause: {}",
          event.getWorkspace().getId(),
          e.getMessage());
    }
  }
}

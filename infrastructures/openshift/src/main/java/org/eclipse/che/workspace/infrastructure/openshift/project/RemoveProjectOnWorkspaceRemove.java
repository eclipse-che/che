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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
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

  private final OpenShiftClientFactory clientFactory;
  private final String projectName;

  @Inject
  public RemoveProjectOnWorkspaceRemove(
      @Nullable @Named("che.infra.kubernetes.namespace") String projectName,
      OpenShiftClientFactory clientFactory) {
    this.projectName = projectName;
    this.clientFactory = clientFactory;
  }

  @Inject
  public void subscribe(EventService eventService) {
    if (isNullOrEmpty(projectName)) {
      eventService.subscribe(this);
    }
  }

  @Override
  public void onEvent(WorkspaceRemovedEvent event) {
    try {
      doRemoveProject(event.getWorkspace().getId());
    } catch (InfrastructureException e) {
      LOG.warn(
          "Fail to remove OpenShift project for workspace with id {}. Cause: {}",
          event.getWorkspace().getId(),
          e.getMessage());
    }
  }

  @VisibleForTesting
  void doRemoveProject(String projectName) throws InfrastructureException {
    clientFactory.create().projects().withName(projectName).delete();
  }
}

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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Listener for removing OpenShift project on {@code WorkspaceRemovedEvent}.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class RemoveProjectOnWorkspaceRemove implements EventSubscriber<WorkspaceRemovedEvent> {

  private final OpenShiftClientFactory clientFactory;

  @Inject
  public RemoveProjectOnWorkspaceRemove(OpenShiftClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(WorkspaceRemovedEvent event) {
    doRemoveProject(event.getWorkspace().getId());
  }

  @VisibleForTesting
  void doRemoveProject(String projectName) {
    try (OpenShiftClient client = clientFactory.create()) {
      client.projects().withName(projectName).delete();
    }
  }
}

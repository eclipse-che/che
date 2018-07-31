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
package org.eclipse.che.multiuser.permission.workspace.server;

import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.event.WorkspaceCreatedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.WorkerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds permissions for creator after workspace creation
 *
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspaceCreatorPermissionsProvider implements EventSubscriber<WorkspaceCreatedEvent> {
  private static final Logger LOG =
      LoggerFactory.getLogger(WorkspaceCreatorPermissionsProvider.class);

  private final WorkerDao workerDao;
  private final EventService eventService;

  @Inject
  public WorkspaceCreatorPermissionsProvider(EventService eventService, WorkerDao workerDao) {
    this.workerDao = workerDao;
    this.eventService = eventService;
  }

  @PostConstruct
  void subscribe() {
    eventService.subscribe(this);
  }

  @PreDestroy
  void unsubscribe() {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(WorkspaceCreatedEvent event) {
    try {
      workerDao.store(
          new WorkerImpl(
              event.getWorkspace().getId(),
              EnvironmentContext.getCurrent().getSubject().getUserId(),
              new ArrayList<>(new WorkspaceDomain().getAllowedActions())));
    } catch (ServerException e) {
      LOG.error(
          "Can't add creator's permissions for workspace with id '"
              + event.getWorkspace().getId()
              + "'",
          e);
    }
  }
}

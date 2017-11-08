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
package org.eclipse.che.api.workspace.server;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;

@Singleton
public class WorkspaceSubjectRegistry implements EventSubscriber<WorkspaceStatusEvent> {

  private static final Logger LOG = getLogger(WorkspaceSubjectRegistry.class);

  private final EventService eventService;
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<String, Subject> workspaceOwners = new HashMap<>();
  private final Multimap<String, String> userIdToWorkspaces =
      ArrayListMultimap.<String, String>create();

  @Inject
  public WorkspaceSubjectRegistry(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void onEvent(WorkspaceStatusEvent event) {

    String workspaceId = event.getWorkspaceId();
    if (WorkspaceStatusEvent.EventType.STOPPED.equals(event.getEventType())) {
      workspaceOwners.remove(workspaceId);
      while (userIdToWorkspaces.values().remove(workspaceId)) {}
    }

    if (WorkspaceStatusEvent.EventType.STARTING.equals(event.getEventType())) {
      Subject subject = EnvironmentContext.getCurrent().getSubject();
      if (subject == Subject.ANONYMOUS) {
        LOG.warn("Workspace {} is being started by the 'anonymous' user.", workspaceId);
        return;
      }
      userIdToWorkspaces.put(subject.getUserId(), workspaceId);
      updateSubject(subject);
    }
  }

  @PostConstruct
  @VisibleForTesting
  void subscribe() {
    eventService.subscribe(this);
  }

  public Subject getWorkspaceStarter(String workspaceId) {
    lock.writeLock().lock();
    try {
      return workspaceOwners.get(workspaceId);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void updateSubject(Subject subject) {
    String token = subject != null ? subject.getToken() : null;
    if (token != null && token.startsWith("machine")) {
      // We are not interested in machine tokens here, but in
      // having the up-to-date token used by the user to connect
      // to the front-end application and create the workspace
      return;
    }
    lock.readLock().lock();
    try {
      String userId = subject.getUserId();
      for (String workspaceId : userIdToWorkspaces.get(userId)) {
        workspaceOwners.put(workspaceId, subject);
      }
    } finally {
      lock.readLock().unlock();
    }
  }
}

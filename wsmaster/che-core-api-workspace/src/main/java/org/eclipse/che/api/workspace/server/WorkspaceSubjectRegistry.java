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

/**
 * This class allows maintaining a link between a started workspace and the user who started it.
 * This also allows updating, at any time, the user informations (<code>Subject</code>) of users
 * that started workspaces.
 *
 * <p></br>
 *
 * <p>In particular, this allows having access to the up-to-date connection information (userName
 * and Keycloak token) of the user that was used when creating a workspace.
 *
 * <p>This is required for all the use-cases where these user informations would be necessary to
 * perform batch-like operations on the user workspaces (such as idling, stop at shutdown, etc ...).
 *
 * <p></br>
 *
 * <p>An example of such use-case is the multi-tenant scenario when deployment is done to Openshift:
 * the user connection informations are required to have access to the Openshift cluster / namespace
 * where the workspace has been created.
 *
 * @author David Festal
 */
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
      while (userIdToWorkspaces.values().remove(workspaceId)) {}
      workspaceOwners.remove(workspaceId);
    }

    if (WorkspaceStatusEvent.EventType.STARTING.equals(event.getEventType())) {
      Subject subject = EnvironmentContext.getCurrent().getSubject();
      if (subject == Subject.ANONYMOUS) {
        throw new IllegalStateException(
            "Workspace "
                + workspaceId
                + " is being started by the 'Anonymous' user.\n"
                + "This shouldn't happen, and workspaces should always be created by a real user.");
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

  /*
   * If some workspaces have been started by the userId contained
   * in this <code>Subject</code>, then the subject (with
   * the userName and token) is updated in the workspace-to-subject
   * cache for all these workspaces.
   */
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

  /*
   * Only for tests
   */
  @VisibleForTesting
  boolean isUserKnown(String userId) {
    return userIdToWorkspaces.containsKey(userId);
  }
}

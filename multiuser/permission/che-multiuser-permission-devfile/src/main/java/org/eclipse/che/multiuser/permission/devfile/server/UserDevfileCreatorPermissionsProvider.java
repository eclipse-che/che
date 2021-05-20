/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server;

import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.devfile.shared.event.DevfileCreatedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.eclipse.che.multiuser.permission.devfile.server.spi.UserDevfilePermissionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adds permissions for creator after user devfile creation */
@Singleton
public class UserDevfileCreatorPermissionsProvider implements EventSubscriber<DevfileCreatedEvent> {
  private static final Logger LOG =
      LoggerFactory.getLogger(UserDevfileCreatorPermissionsProvider.class);

  private final UserDevfilePermissionDao userDevfilePermissionDao;
  private final EventService eventService;

  @Inject
  public UserDevfileCreatorPermissionsProvider(
      EventService eventService, UserDevfilePermissionDao userDevfilePermissionDao) {
    this.userDevfilePermissionDao = userDevfilePermissionDao;
    this.eventService = eventService;
  }

  @PostConstruct
  void subscribe() {
    eventService.subscribe(this);
  }

  @PreDestroy
  void unsubscribe() {
    eventService.unsubscribe(this);
  }

  @Override
  public void onEvent(DevfileCreatedEvent event) {
    try {
      userDevfilePermissionDao.store(
          new UserDevfilePermissionImpl(
              event.getUserDevfile().getId(),
              EnvironmentContext.getCurrent().getSubject().getUserId(),
              new ArrayList<>(new UserDevfileDomain().getAllowedActions())));
    } catch (ServerException e) {
      LOG.error(
          "Can't add creator's permissions for user devfile with id '"
              + event.getUserDevfile().getId()
              + "'",
          e);
    }
  }
}

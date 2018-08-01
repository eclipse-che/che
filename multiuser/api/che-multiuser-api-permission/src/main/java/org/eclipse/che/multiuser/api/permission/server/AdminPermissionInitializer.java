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
package org.eclipse.che.multiuser.api.permission.server;

import static java.lang.String.format;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grant system permission for 'che.admin.name' user. If the user already exists it'll happen on
 * component startup, if not - during the first login when user is persisted in the database.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class AdminPermissionInitializer implements EventSubscriber<PostUserPersistedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(AdminPermissionInitializer.class);

  private final UserManager userManager;

  private final PermissionsManager permissionsManager;

  private final EventService eventService;

  private final String name;

  @Inject
  public AdminPermissionInitializer(
      @Named("che.system.admin_name") String name,
      UserManager userManager,
      PermissionsManager permissionsManager,
      EventService eventService) {
    this.userManager = userManager;
    this.permissionsManager = permissionsManager;
    this.eventService = eventService;
    this.name = name;
  }

  @PostConstruct
  public void init() throws ServerException {
    try {
      User adminUser = userManager.getByName(name);
      grantSystemPermissions(adminUser.getId());
    } catch (NotFoundException ex) {
      LOG.warn("Admin {} not found yet.", name);
    } finally {
      eventService.subscribe(this);
    }
  }

  @PreDestroy
  public void unsubscribe() {
    eventService.unsubscribe(this);
  }

  @Override
  public void onEvent(PostUserPersistedEvent event) {
    if (event.getUser().getName().equals(name)) {
      grantSystemPermissions(event.getUser().getId());
    }
  }

  public void grantSystemPermissions(String userId) {
    // Add all possible system permissions
    try {
      AbstractPermissionsDomain<? extends AbstractPermissions> systemDomain =
          permissionsManager.getDomain(SystemDomain.DOMAIN_ID);
      permissionsManager.storePermission(
          systemDomain.newInstance(userId, null, systemDomain.getAllowedActions()));
    } catch (ServerException | NotFoundException | ConflictException e) {
      LOG.warn(format("System permissions creation failed for user %s", userId), e);
    }
  }
}

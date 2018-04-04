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
package org.eclipse.che.multiuser.api.permission.server.jpa.listener;

import static org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who have
 * "setPermissions" role to any of the permission domain, and if it is, then removes domain entity
 * itself.
 *
 * @author Max Shaposhnik
 */
public abstract class RemovePermissionsOnLastUserRemovedEventSubscriber<
        T extends PermissionsDao<? extends AbstractPermissions>>
    extends CascadeEventSubscriber<BeforeUserRemovedEvent> {

  @Inject private EventService eventService;

  @Inject T storage;

  @PostConstruct
  public void subscribe() {
    eventService.subscribe(this, BeforeUserRemovedEvent.class);
  }

  @PreDestroy
  public void unsubscribe() {
    eventService.unsubscribe(this, BeforeUserRemovedEvent.class);
  }

  @Override
  public void onCascadeEvent(BeforeUserRemovedEvent event) throws Exception {
    for (AbstractPermissions permissions : storage.getByUser(event.getUser().getId())) {
      // This method can potentially be source of race conditions,
      // e.g. when performing search by permissions, another thread can add/or remove another
      // setPermission,
      // so appropriate domain object (stack or recipe) will not be deleted, or vice versa,
      // deleted when it's not required anymore.
      // As a result, a solitary objects may be present in the DB.
      if (userHasLastSetPermissions(permissions.getUserId(), permissions.getInstanceId())) {
        remove(permissions.getInstanceId());
      } else {
        storage.remove(event.getUser().getId(), permissions.getInstanceId());
      }
    }
  }

  private boolean userHasLastSetPermissions(String userId, String instanceId)
      throws ServerException {
    try {
      Page<? extends AbstractPermissions> page = storage.getByInstance(instanceId, 30, 0);
      boolean hasSetPermission;
      while (!(hasSetPermission = hasForeignSetPermission(page.getItems(), userId))
          && page.hasNextPage()) {

        final Page.PageRef nextPageRef = page.getNextPageRef();
        page =
            storage.getByInstance(
                instanceId, nextPageRef.getPageSize(), (int) nextPageRef.getItemsBefore());
      }
      return !hasSetPermission;
    } catch (NotFoundException e) {
      return true;
    }
  }

  private boolean hasForeignSetPermission(
      List<? extends AbstractPermissions> permissions, String userId) {
    for (AbstractPermissions permission : permissions) {
      if (!permission.getUserId().equals(userId)
          && permission.getActions().contains(SET_PERMISSIONS)) {
        return true;
      }
    }
    return false;
  }

  public abstract void remove(String instanceId) throws ServerException;
}

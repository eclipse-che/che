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
package org.eclipse.che.multiuser.organization.api.permissions;

import static org.eclipse.che.multiuser.organization.api.listener.OrganizationEventsWebsocketBroadcaster.ORGANIZATION_CHANGED_METHOD_NAME;
import static org.eclipse.che.multiuser.organization.api.listener.OrganizationEventsWebsocketBroadcaster.ORGANIZATION_MEMBERSHIP_METHOD_NAME;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionCheck;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionManager;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.organization.api.listener.OrganizationEventsWebsocketBroadcaster;

/**
 * Holds and registers permissions checks for organization related events.
 *
 * <p>Covers events published via {@link OrganizationEventsWebsocketBroadcaster}.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class OrganizationRemoteSubscriptionPermissionsChecks {

  private final PermissionsManager permissionsManager;

  @Inject
  public OrganizationRemoteSubscriptionPermissionsChecks(PermissionsManager permissionsManager) {
    this.permissionsManager = permissionsManager;
  }

  @Inject
  public void register(RemoteSubscriptionPermissionManager permissionFilter) {
    MembershipsChangedSubscriptionCheck membershipsEventsCheck =
        new MembershipsChangedSubscriptionCheck();

    permissionFilter.registerCheck(membershipsEventsCheck, ORGANIZATION_MEMBERSHIP_METHOD_NAME);

    OrganizationChangedSubscriptionCheck organizationChangedCheck =
        new OrganizationChangedSubscriptionCheck(permissionsManager);
    permissionFilter.registerCheck(organizationChangedCheck, ORGANIZATION_CHANGED_METHOD_NAME);
  }

  @VisibleForTesting
  static class MembershipsChangedSubscriptionCheck implements RemoteSubscriptionPermissionCheck {

    @Override
    public void check(String methodName, Map<String, String> scope) throws ForbiddenException {
      String userId = scope.get("userId");
      if (userId == null) {
        throw new ForbiddenException("User id must be specified in scope");
      }

      String currentUserId = EnvironmentContext.getCurrent().getSubject().getUserId();

      if (!currentUserId.equals(userId)) {
        throw new ForbiddenException("It is only allowed to listen to own memberships changes");
      }
    }
  }

  @VisibleForTesting
  static class OrganizationChangedSubscriptionCheck implements RemoteSubscriptionPermissionCheck {

    private final PermissionsManager permissionsManager;

    public OrganizationChangedSubscriptionCheck(PermissionsManager permissionsManager) {
      this.permissionsManager = permissionsManager;
    }

    @Override
    public void check(String methodName, Map<String, String> scope) throws ForbiddenException {
      String organizationId = scope.get("organizationId");
      if (organizationId == null) {
        throw new ForbiddenException("Organization id must be specified in scope");
      }

      String currentUserId = EnvironmentContext.getCurrent().getSubject().getUserId();

      try {
        // check if user has any permissions in organisation
        // to listen to related events
        AbstractPermissions permissions =
            permissionsManager.get(currentUserId, OrganizationDomain.DOMAIN_ID, organizationId);
      } catch (ConflictException | ServerException e) {
        throw new ForbiddenException("Error occurred while permission fetching: " + e.getMessage());
      } catch (NotFoundException e) {
        throw new ForbiddenException(
            "User doesn't have any permissions for the specified organization");
      }
    }
  }
}

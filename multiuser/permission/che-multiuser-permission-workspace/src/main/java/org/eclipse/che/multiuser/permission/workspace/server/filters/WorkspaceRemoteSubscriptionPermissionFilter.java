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
package org.eclipse.che.multiuser.permission.workspace.server.filters;

import static org.eclipse.che.api.workspace.shared.Constants.BOOTSTRAPPER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STATUS_CHANGED_METHOD;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionCheck;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionManager;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;

/**
 * Holds and registers permissions checks for workspaces related events.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class WorkspaceRemoteSubscriptionPermissionFilter
    implements RemoteSubscriptionPermissionCheck {

  @Inject
  public void register(RemoteSubscriptionPermissionManager permissionFilter) {
    permissionFilter.registerCheck(
        this,
        WORKSPACE_STATUS_CHANGED_METHOD,
        MACHINE_STATUS_CHANGED_METHOD,
        SERVER_STATUS_CHANGED_METHOD,
        MACHINE_LOG_METHOD,
        INSTALLER_LOG_METHOD,
        INSTALLER_STATUS_CHANGED_METHOD,
        BOOTSTRAPPER_STATUS_CHANGED_METHOD);
  }

  @Override
  public void check(String methodName, Map<String, String> scope) throws ForbiddenException {
    String workspaceId = scope.get("workspaceId");

    if (workspaceId == null) {
      throw new ForbiddenException("Workspace id must be specified in scope");
    }

    Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
    if (!currentSubject.hasPermission(WorkspaceDomain.DOMAIN_ID, workspaceId, WorkspaceDomain.RUN)
        && !currentSubject.hasPermission(
            WorkspaceDomain.DOMAIN_ID, workspaceId, WorkspaceDomain.USE)) {
      throw new ForbiddenException(
          "The current user doesn't have permissions to listen to the specified workspace events");
    }
  }
}

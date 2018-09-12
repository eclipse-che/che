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

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.workspace.shared.dto.event.BootstrapperStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerStatusEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.JsonRpcPermissionsFilterAdapter;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;

/**
 * Add permissions checking before {@link
 * org.eclipse.che.api.workspace.server.bootstrap.InstallerService} methods invocation.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class InstallerServicePermissionFilter extends JsonRpcPermissionsFilterAdapter {
  @Inject
  public void register(RequestHandlerManager requestHandlerManager) {
    requestHandlerManager.registerMethodInvokerFilter(
        this,
        BOOTSTRAPPER_STATUS_CHANGED_METHOD,
        INSTALLER_STATUS_CHANGED_METHOD,
        INSTALLER_LOG_METHOD);
  }

  @Override
  public void doAccept(String method, Object... params) throws ForbiddenException {
    String workspaceId;
    switch (method) {
      case BOOTSTRAPPER_STATUS_CHANGED_METHOD:
        workspaceId = ((BootstrapperStatusEvent) params[0]).getRuntimeId().getWorkspaceId();
        break;
      case INSTALLER_LOG_METHOD:
        workspaceId = ((InstallerLogEvent) params[0]).getRuntimeId().getWorkspaceId();
        break;
      case INSTALLER_STATUS_CHANGED_METHOD:
        workspaceId = ((InstallerStatusEvent) params[0]).getRuntimeId().getWorkspaceId();
        break;
      default:
        throw new ForbiddenException("Unknown method is configured to be filtered.");
    }

    Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
    if (!currentSubject.hasPermission(
        WorkspaceDomain.DOMAIN_ID, workspaceId, WorkspaceDomain.RUN)) {
      throw new ForbiddenException(
          "User doesn't have the required permissions to the specified workspace");
    }
  }
}

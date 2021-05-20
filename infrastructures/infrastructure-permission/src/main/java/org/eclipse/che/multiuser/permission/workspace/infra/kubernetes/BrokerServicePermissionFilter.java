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
package org.eclipse.che.multiuser.permission.workspace.infra.kubernetes;

import static org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerService.BROKER_RESULT_METHOD;
import static org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerService.BROKER_STATUS_CHANGED_METHOD;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerStatusChangedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.JsonRpcPermissionsFilterAdapter;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerService;

/**
 * Add permissions checking before {@link BrokerService} methods invocation.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class BrokerServicePermissionFilter extends JsonRpcPermissionsFilterAdapter {
  @Inject
  public void register(RequestHandlerManager requestHandlerManager) {
    requestHandlerManager.registerMethodInvokerFilter(
        this, BROKER_STATUS_CHANGED_METHOD, BROKER_RESULT_METHOD);
  }

  @Override
  public void doAccept(String method, Object... params) throws ForbiddenException {
    String workspaceId;
    switch (method) {
      case BROKER_STATUS_CHANGED_METHOD:
      case BROKER_RESULT_METHOD:
        RuntimeIdentityDto runtimeId = ((BrokerStatusChangedEvent) params[0]).getRuntimeId();
        if (runtimeId == null || runtimeId.getWorkspaceId() == null) {
          throw new ForbiddenException("Workspace id must be specified");
        }
        workspaceId = runtimeId.getWorkspaceId();
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

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
package org.eclipse.che.multiuser.machine.authentication.server;

import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;

public class MachineTokenAuthorizedSubject extends AuthorizedSubject {

  private final String scopeWorkspaceId;

  public MachineTokenAuthorizedSubject(Subject baseSubject,
      PermissionChecker permissionChecker, String scopeWorkspaceId) {
    super(baseSubject, permissionChecker);
    this.scopeWorkspaceId = scopeWorkspaceId;
  }


  @Override
  public boolean hasPermission(String domain, String instance, String action) {
    if (domain.equals(WorkspaceDomain.DOMAIN_ID) && !instance.equals(scopeWorkspaceId)) {
      return false;
    }
    return super.hasPermission(domain, instance, action);
  }
}

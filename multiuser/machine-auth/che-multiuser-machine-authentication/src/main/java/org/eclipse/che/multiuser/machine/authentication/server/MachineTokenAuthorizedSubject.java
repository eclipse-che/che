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

/**
 * An implementation of {@link Subject} which should be used when request was signed by machine
 * token. This implementation limits all workspace related permissions to the single workspace for
 * which the machine token was issued.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class MachineTokenAuthorizedSubject extends AuthorizedSubject {

  private final String claimsWorkspaceId;

  public MachineTokenAuthorizedSubject(
      Subject baseSubject, PermissionChecker permissionChecker, String claimsWorkspaceId) {
    super(baseSubject, permissionChecker);
    this.claimsWorkspaceId = claimsWorkspaceId;
  }

  @Override
  public boolean hasPermission(String domain, String instance, String action) {
    if (domain.equals(WorkspaceDomain.DOMAIN_ID) && !instance.equals(claimsWorkspaceId)) {
      return false;
    }
    return super.hasPermission(domain, instance, action);
  }
}

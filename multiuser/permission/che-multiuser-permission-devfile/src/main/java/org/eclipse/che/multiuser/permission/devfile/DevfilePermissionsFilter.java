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
package org.eclipse.che.multiuser.permission.devfile;

import javax.inject.Inject;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.devfile.DevfileService;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/** Restricts access to methods of {@link DevfileService} by user's permissions. */
@Filter
@Path("/devfile{path:(/.*)?}")
public class DevfilePermissionsFilter extends CheMethodInvokerFilter {

  private final WorkspaceManager workspaceManager;

  @Inject
  public DevfilePermissionsFilter(WorkspaceManager workspaceManager) {
    this.workspaceManager = workspaceManager;
  }

  @Override
  protected void filter(GenericResourceMethod genericResourceMethod, Object[] arguments)
      throws ForbiddenException, NotFoundException, ServerException {
    final String methodName = genericResourceMethod.getMethod().getName();
    switch (methodName) {
        // public methods
      case "getSchema":
        return;
      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }
  }
}

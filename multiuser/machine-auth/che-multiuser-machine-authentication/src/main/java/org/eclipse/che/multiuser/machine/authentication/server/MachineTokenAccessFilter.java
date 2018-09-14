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

import javax.ws.rs.Path;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

@Filter
@Path("/{path:.*}")
public class MachineTokenAccessFilter extends CheMethodInvokerFilter {

  @Override
  protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ForbiddenException {
    if (!(EnvironmentContext.getCurrent().getSubject() instanceof MachineTokenAuthorizedSubject)) {
      return;
    }
    final String methodName = genericMethodResource.getMethod().getName();
    final String parentResourcePath =
        genericMethodResource.getParentResource().getPathValue().getPath();

    switch (parentResourcePath) {
      case "/workspace":
        validateWorkspaceMethods(methodName);
        break;
      case "/preferences":
        validatePreferencesMethods(methodName);
        break;
      case "/ssh":
        validateSshMethods(methodName);
        break;
      case "/activity":
        validateActivityMethods(methodName);
        break;
      default:
        throw new ForbiddenException("This operation cannot be performed using machine token.");
    }
  }

  private void validateWorkspaceMethods(String methodName) throws ForbiddenException {
    if ("getByKey".equals(methodName)
        || "addProject".equals(methodName)
        || "updateProject".equals(methodName)
        || "deleteProject".equals(methodName)) {
      return;
    }
    throw new ForbiddenException("This operation cannot be performed using machine token.");
  }

  private void validatePreferencesMethods(String methodName) throws ForbiddenException {
    if ("find".equals(methodName)) {
      return;
    }
    throw new ForbiddenException("This operation cannot be performed using machine token.");
  }

  private void validateSshMethods(String methodName) throws ForbiddenException {
    if ("getPair".equals(methodName) || "generatePair".equals(methodName)) {
      return;
    }
    throw new ForbiddenException("This operation cannot be performed using machine token.");
  }

  private void validateActivityMethods(String methodName) throws ForbiddenException {
    if ("active".equals(methodName)) {
      return;
    }
    throw new ForbiddenException("This operation cannot be performed using machine token.");
  }
}

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
import org.everrest.core.impl.resource.ResourceMethodDescriptorImpl;
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
    final String httpMethod =
        ((ResourceMethodDescriptorImpl) genericMethodResource).getHttpMethod();
    final String methodName = genericMethodResource.getMethod().getName();
    final String parentResourcePath =
        genericMethodResource.getParentResource().getPathValue().getPath();

    switch (httpMethod) {
      case "GET":
        validateGetMethods(methodName, parentResourcePath);
        break;
      case "POST":
        validatePostMethods(methodName, parentResourcePath);
        break;
      case "PUT":
        validatePutMethods(methodName, parentResourcePath);
        break;
      case "DELETE":
        validateDeleteMethods(methodName, parentResourcePath);
        break;
      default:
        throw new ForbiddenException("This operation cannot be performed using machine token.");
    }
  }

  private void validateGetMethods(String methodName, String parentResourcePath)
      throws ForbiddenException {
    if (("/workspace".equals(parentResourcePath) && "getByKey".equals(methodName)) ||
        ("/preferences".equals(parentResourcePath) && "find".equals(methodName)) ||
        ("/ssh".equals(parentResourcePath) && "getPair".equals(methodName))) {
      return;
    }
    throw new ForbiddenException("This operation cannot be performed using machine token.");
  }

  private void validatePostMethods(String methodName, String parentResourcePath)
      throws ForbiddenException {
    if (("/workspace".equals(parentResourcePath) && "addProject".equals(methodName)) ||
        ("/ssh".equals(parentResourcePath) && "generatePair".equals(methodName))) {
      return;
    }
    throw new ForbiddenException("This operation cannot be performed using machine token.");
  }

  private void validatePutMethods(String methodName, String parentResourcePath)
      throws ForbiddenException {
    if (("/workspace".equals(parentResourcePath) && "updateProject".equals(methodName)) ||
        ("/activity".equals(parentResourcePath) && "active".equals(methodName))) {
      return;
    }
    throw new ForbiddenException("This operation cannot be performed using machine token.");
  }

  private void validateDeleteMethods(String methodName, String parentResourcePath)
      throws ForbiddenException {
    if (("/workspace".equals(parentResourcePath) && "deleteProject".equals(methodName)) {
      return;
    }
    throw new ForbiddenException("This operation cannot be performed using machine token.");
  }

}

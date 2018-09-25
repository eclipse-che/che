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
package org.eclipse.che.multiuser.permission.factory;

import javax.inject.Inject;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.factory.server.FactoryManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to methods of FactoryService by user's permissions.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
@Filter
@Path("/factory/{path:.*}")
public class FactoryPermissionsFilter extends CheMethodInvokerFilter {

  private final FactoryManager factoryManager;

  @Inject
  public FactoryPermissionsFilter(FactoryManager factoryManager) {
    this.factoryManager = factoryManager;
  }

  @Override
  protected void filter(GenericResourceMethod genericResourceMethod, Object[] arguments)
      throws ApiException {
    final String methodName = genericResourceMethod.getMethod().getName();

    final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();

    switch (methodName) {
      case "getFactoryJson":
        {
          String workspaceId = ((String) arguments[0]);

          currentSubject.checkPermission(
              WorkspaceDomain.DOMAIN_ID, workspaceId, WorkspaceDomain.READ);
          return;
        }
      case "removeFactory":
        checkSubjectIsCreator((String) arguments[0], currentSubject, "remove");
        return;
      case "updateFactory":
        checkSubjectIsCreator((String) arguments[0], currentSubject, "update");
        return;

      case "getFactory":
      case "saveFactory":
      case "getFactoryByAttribute":
      case "resolveFactory":
        // public methods
        // do nothing
        return;

      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }
  }

  private void checkSubjectIsCreator(String factoryId, Subject currentSubject, String action)
      throws NotFoundException, ServerException, ForbiddenException {
    Factory factory = factoryManager.getById(factoryId);
    String creatorId = factory.getCreator().getUserId();
    if (!creatorId.equals(currentSubject.getUserId())) {
      throw new ForbiddenException("It is not allowed to " + action + " foreign factory");
    }
  }
}

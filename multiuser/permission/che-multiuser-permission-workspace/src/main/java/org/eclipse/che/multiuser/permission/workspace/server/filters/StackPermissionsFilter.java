/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.filters;

import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.DELETE;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.DOMAIN_ID;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.READ;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.SEARCH;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.UPDATE;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.stack.StackService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to methods of {@link StackService} by users' permissions
 *
 * <p>Filter should contain rules for protecting of all methods of {@link StackService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 * @author Mykola Morhun
 */
@Filter
@Path("/stack{path:(/.*)?}")
public class StackPermissionsFilter extends CheMethodInvokerFilter {

  private final PermissionsManager permissionsManager;

  @Inject
  public StackPermissionsFilter(PermissionsManager permissionsManager) {
    this.permissionsManager = permissionsManager;
  }

  @Override
  public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments)
      throws ForbiddenException, ServerException {
    final String methodName = genericResourceMethod.getMethod().getName();

    final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
    String action;
    String stackId;

    switch (methodName) {
      case "getStack":
      case "getIcon":
        stackId = ((String) arguments[0]);
        action = READ;

        if (currentSubject.hasPermission(DOMAIN_ID, stackId, SEARCH)) {
          // allow to read stack if user has 'search' permission
          return;
        }
        break;

      case "updateStack":
      case "uploadIcon":
        stackId = ((String) arguments[1]);
        action = UPDATE;
        break;

      case "removeIcon":
        stackId = ((String) arguments[0]);
        action = UPDATE;
        break;

      case "removeStack":
        stackId = ((String) arguments[0]);
        action = DELETE;
        break;

      case "createStack":
      case "searchStacks":
        // available for all
        return;
      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }

    if (currentSubject.hasPermission(
            SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION)
        && isStackPredefined(stackId)) {
      // allow any operation with predefined stack if user has 'manageSystem' permission
      return;
    }

    if (!currentSubject.hasPermission(DOMAIN_ID, stackId, action)) {
      throw new ForbiddenException(
          "The user does not have permission to " + action + " stack with id '" + stackId + "'");
    }
  }

  /**
   * Determines whether stack is predefined. Note, that 'predefined' means public for all users (not
   * necessary provided with system from the box).
   *
   * @param stackId id of stack to test
   * @return true if stack is predefined, false otherwise
   * @throws ServerException when any error occurs during permissions fetching
   */
  @VisibleForTesting
  boolean isStackPredefined(String stackId) throws ServerException {
    try {
      Page<AbstractPermissions> permissionsPage =
          permissionsManager.getByInstance(DOMAIN_ID, stackId, 25, 0);
      do {
        for (AbstractPermissions stackPermission : permissionsPage.getItems()) {
          if ("*".equals(stackPermission.getUserId())) {
            return true;
          }
        }
      } while ((permissionsPage = getNextPermissionsPage(stackId, permissionsPage)) != null);
    } catch (NotFoundException e) {
      // should never happen
      throw new ServerException(e);
    }
    return false;
  }

  /**
   * Retrieves next permissions page for given stack.
   *
   * @param stackId id of stack to which permissions will be obtained
   * @param permissionsPage previous permissions page
   * @return next permissions page for given stack or null if next page doesn't exist
   * @throws ServerException when any error occurs during permissions fetching
   */
  @VisibleForTesting
  Page<AbstractPermissions> getNextPermissionsPage(
      String stackId, Page<AbstractPermissions> permissionsPage)
      throws NotFoundException, ServerException {
    if (!permissionsPage.hasNextPage()) {
      return null;
    }

    final Page.PageRef nextPageRef = permissionsPage.getNextPageRef();
    return permissionsManager.getByInstance(
        DOMAIN_ID, stackId, nextPageRef.getPageSize(), nextPageRef.getItemsBefore());
  }
}

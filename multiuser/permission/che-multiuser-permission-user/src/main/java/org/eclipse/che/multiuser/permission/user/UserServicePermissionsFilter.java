/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.user;

import static org.eclipse.che.api.user.server.UserService.USER_SELF_CREATION_ALLOWED;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Filter that covers calls to {@link UserService} with authorization
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/user{path:.*}")
public class UserServicePermissionsFilter extends CheMethodInvokerFilter {
  public static final String MANAGE_USERS_ACTION = "manageUsers";

  private final boolean userSelfCreationAllowed;

  @Inject
  public UserServicePermissionsFilter(
      @Named(USER_SELF_CREATION_ALLOWED) boolean userSelfCreationAllowed) {
    this.userSelfCreationAllowed = userSelfCreationAllowed;
  }

  @Override
  protected void filter(GenericResourceMethod genericResourceMethod, Object[] arguments)
      throws ApiException {
    final String methodName = genericResourceMethod.getMethod().getName();
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    switch (methodName) {
      case "getCurrent":
      case "updatePassword":
      case "getById":
      case "find":
      case "getSettings":
        // public methods
        return;
      case "create":
        final String token = (String) arguments[1];
        if (token != null) {
          // it is available to create user from token without permissions
          if (!userSelfCreationAllowed
              && !subject.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION)) {
            throw new ForbiddenException(
                "Currently only admins can create accounts. Please contact our Admin Team for further info.");
          }

          return;
        }

        subject.checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
        break;
      case "remove":
        final String userToRemove = (String) arguments[0];
        if (subject.getUserId().equals(userToRemove)) {
          // everybody should be able to remove himself
          return;
        }
        subject.checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
        break;
      default:
        // unknown method
        throw new ForbiddenException("User is not authorized to perform this operation");
    }
  }
}

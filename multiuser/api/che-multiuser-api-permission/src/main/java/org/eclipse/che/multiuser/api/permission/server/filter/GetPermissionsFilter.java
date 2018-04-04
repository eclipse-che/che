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
package org.eclipse.che.multiuser.api.permission.server.filter;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.InstanceParameterValidator;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to reading permissions of instance by users' readPermissions permission
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/permissions/{domain}/all")
public class GetPermissionsFilter extends CheMethodInvokerFilter {
  @PathParam("domain")
  private String domain;

  @QueryParam("instance")
  private String instance;

  @Inject private PermissionsManager permissionsManager;

  @Inject private SuperPrivilegesChecker superPrivilegesChecker;

  @Inject private InstanceParameterValidator instanceValidator;

  @Override
  public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments)
      throws BadRequestException, NotFoundException, ConflictException, ForbiddenException,
          ServerException {

    final String methodName = genericResourceMethod.getMethod().getName();
    if (methodName.equals("getUsersPermissions")) {
      instanceValidator.validate(domain, instance);
      if (superPrivilegesChecker.isPrivilegedToManagePermissions(domain)) {
        return;
      }
      final String userId = EnvironmentContext.getCurrent().getSubject().getUserId();
      try {
        permissionsManager.get(userId, domain, instance);
        // user should have ability to see another users' permissions if he has any permission there
      } catch (NotFoundException e) {
        throw new ForbiddenException("User is not authorized to perform this operation");
      }
    }
  }
}

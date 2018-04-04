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
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.InstanceParameterValidator;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.api.permission.server.filter.check.DomainsPermissionsCheckers;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to removing permissions of instance by users' setPermissions permission
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/permissions/{domain}")
public class RemovePermissionsFilter extends CheMethodInvokerFilter {
  @PathParam("domain")
  private String domain;

  @QueryParam("instance")
  private String instance;

  @QueryParam("user")
  private String user;

  @Inject private SuperPrivilegesChecker superPrivilegesChecker;

  @Inject private InstanceParameterValidator instanceValidator;

  @Inject private DomainsPermissionsCheckers domainsPermissionsCheckers;

  @Override
  public void filter(GenericResourceMethod genericResourceMethod, Object[] args)
      throws BadRequestException, ForbiddenException, NotFoundException, ServerException {
    if (genericResourceMethod.getMethod().getName().equals("removePermissions")) {
      instanceValidator.validate(domain, instance);
      final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
      if (currentSubject.getUserId().equals(user)
          || superPrivilegesChecker.isPrivilegedToManagePermissions(domain)) {
        return;
      }
      domainsPermissionsCheckers.getRemoveChecker(domain).check(user, domain, instance);
    }
  }
}

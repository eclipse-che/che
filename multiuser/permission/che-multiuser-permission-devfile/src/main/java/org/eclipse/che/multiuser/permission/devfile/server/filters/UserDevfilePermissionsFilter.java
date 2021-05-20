/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server.filters;

import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.DELETE;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.DOMAIN_ID;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.READ;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.UPDATE;

import com.google.common.annotations.VisibleForTesting;
import javax.inject.Inject;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.devfile.server.DevfileService;
import org.eclipse.che.api.devfile.server.UserDevfileManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to methods of {@link DevfileService} by users' permissions.
 *
 * <p>Filter contains rules for protecting of all methods of {@link DevfileService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 */
@Filter
@Path("/devfile{path:(/.*)?}")
public class UserDevfilePermissionsFilter extends CheMethodInvokerFilter {
  private final UserDevfileManager userDevfileManager;

  @Inject
  public UserDevfilePermissionsFilter(UserDevfileManager userDevfileManager) {
    this.userDevfileManager = userDevfileManager;
  }

  @Override
  public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments)
      throws ForbiddenException {
    final String methodName = genericResourceMethod.getMethod().getName();
    switch (methodName) {
      case "getById":
        doCheckPermission(DOMAIN_ID, ((String) arguments[0]), READ);
        break;
      case "update":
        doCheckPermission(DOMAIN_ID, ((String) arguments[0]), UPDATE);
        break;
      case "delete":
        doCheckPermission(DOMAIN_ID, ((String) arguments[0]), DELETE);
        break;
      case "createFromDevfileYaml":
      case "createFromUserDevfile":
      case "getUserDevfiles":
      case "getSchema":
        return;
      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }
  }

  @VisibleForTesting
  void doCheckPermission(String domain, String instance, String action) throws ForbiddenException {
    EnvironmentContext.getCurrent().getSubject().checkPermission(domain, instance, action);
  }
}

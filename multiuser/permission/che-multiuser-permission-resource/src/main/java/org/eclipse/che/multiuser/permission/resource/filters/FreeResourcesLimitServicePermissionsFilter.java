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
package org.eclipse.che.multiuser.permission.resource.filters;

import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.resource.api.free.FreeResourcesLimitService;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to methods of {@link FreeResourcesLimitService} by users' permissions.
 *
 * <p>Filter contains rules for protecting of all methods of {@link FreeResourcesLimitService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/resource/free{path:(/.*)?}")
public class FreeResourcesLimitServicePermissionsFilter extends CheMethodInvokerFilter {
  static final String STORE_FREE_RESOURCES_LIMIT_METHOD = "storeFreeResourcesLimit";
  static final String GET_FREE_RESOURCES_LIMITS_METHOD = "getFreeResourcesLimits";
  static final String GET_FREE_RESOURCES_LIMIT_METHOD = "getFreeResourcesLimit";
  static final String REMOVE_FREE_RESOURCES_LIMIT_METHOD = "removeFreeResourcesLimit";

  @Override
  protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ApiException {
    switch (genericMethodResource.getMethod().getName()) {
      case STORE_FREE_RESOURCES_LIMIT_METHOD:
      case GET_FREE_RESOURCES_LIMITS_METHOD:
      case GET_FREE_RESOURCES_LIMIT_METHOD:
      case REMOVE_FREE_RESOURCES_LIMIT_METHOD:
        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        if (currentSubject.hasPermission(
            SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION)) {
          return;
        }
        // fall through
        // user doesn't have permissions and request should not be processed
      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }
  }
}

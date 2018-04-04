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
package org.eclipse.che.multiuser.organization.api.permissions;

import javax.inject.Inject;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.api.resource.OrganizationResourcesDistributionService;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to methods of {@link OrganizationResourcesDistributionService} by users'
 * permissions.
 *
 * <p>Filter contains rules for protecting of all methods of {@link
 * OrganizationResourcesDistributionService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}.
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/organization/resource{path:(/.*)?}")
public class OrganizationResourceDistributionServicePermissionsFilter
    extends CheMethodInvokerFilter {
  static final String CAP_RESOURCES_METHOD = "capResources";
  static final String GET_RESOURCES_CAP_METHOD = "getResourcesCap";
  static final String GET_DISTRIBUTED_RESOURCES = "getDistributedResources";

  @Inject private OrganizationManager organizationManager;
  @Inject private SuperPrivilegesChecker superPrivilegesChecker;

  @Override
  protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ApiException {
    final String methodName = genericMethodResource.getMethod().getName();

    final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
    String organizationId;
    switch (methodName) {
      case GET_RESOURCES_CAP_METHOD:
        if (superPrivilegesChecker.hasSuperPrivileges()) {
          // user is able to see information about all organizations
          return;
        }
        // fall through
      case CAP_RESOURCES_METHOD:
        // we should check permissions on parent organization level
        Organization organization = organizationManager.getById((String) arguments[0]);
        organizationId = organization.getParent();
        if (organizationId == null) {
          // requested organization is root so manager should throw exception
          return;
        }
        break;

      case GET_DISTRIBUTED_RESOURCES:
        organizationId = (String) arguments[0];
        // get organization to ensure that organization exists
        organizationManager.getById(organizationId);
        if (superPrivilegesChecker.hasSuperPrivileges()) {
          // user is able to see information about all organizations
          return;
        }
        break;

      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }

    if (!currentSubject.hasPermission(
        OrganizationDomain.DOMAIN_ID, organizationId, OrganizationDomain.MANAGE_RESOURCES)) {
      throw new ForbiddenException(
          "The user does not have permission to manage resources of organization with id '"
              + organizationId
              + "'");
    }
  }
}

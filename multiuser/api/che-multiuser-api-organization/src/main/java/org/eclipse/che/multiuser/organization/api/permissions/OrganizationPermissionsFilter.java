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
package org.eclipse.che.multiuser.organization.api.permissions;

import static org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain.DOMAIN_ID;
import static org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain.MANAGE_SUBORGANIZATIONS;

import javax.inject.Inject;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.api.OrganizationService;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to methods of {@link OrganizationService} by users' permissions
 *
 * <p>Filter contains rules for protecting of all methods of {@link OrganizationService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/organization{path:(?!/resource)(/.*)?}")
public class OrganizationPermissionsFilter extends CheMethodInvokerFilter {
  static final String CREATE_METHOD = "create";
  static final String UPDATE_METHOD = "update";
  static final String REMOVE_METHOD = "remove";
  static final String GET_BY_PARENT_METHOD = "getByParent";
  static final String GET_ORGANIZATIONS_METHOD = "getOrganizations";
  static final String GET_BY_ID_METHOD = "getById";
  static final String FIND_METHOD = "find";

  @Inject private OrganizationManager manager;
  @Inject private SuperPrivilegesChecker superPrivilegesChecker;

  @Override
  protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ApiException {
    final String methodName = genericMethodResource.getMethod().getName();

    final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
    String action;
    String organizationId;

    switch (methodName) {
      case CREATE_METHOD:
        final OrganizationDto organization = (OrganizationDto) arguments[0];
        if (organization.getParent() != null) {
          organizationId = organization.getParent();
          action = OrganizationDomain.MANAGE_SUBORGANIZATIONS;
          break;
        }
        // anybody can create root organization
        return;

      case UPDATE_METHOD:
        organizationId = ((String) arguments[0]);
        action = OrganizationDomain.UPDATE;
        break;

      case REMOVE_METHOD:
        organizationId = ((String) arguments[0]);
        action = OrganizationDomain.DELETE;
        break;

      case GET_BY_PARENT_METHOD:
        organizationId = ((String) arguments[0]);
        action = OrganizationDomain.MANAGE_SUBORGANIZATIONS;
        if (superPrivilegesChecker.hasSuperPrivileges()) {
          return;
        }
        break;

      case GET_ORGANIZATIONS_METHOD:
        final String userId = (String) arguments[0];
        if (userId != null
            && !userId.equals(currentSubject.getUserId())
            && !superPrivilegesChecker.hasSuperPrivileges()) {
          throw new ForbiddenException("The user is able to specify only his own id");
        }
        // user specified his user id or has super privileges
        return;

        // methods accessible to every user
      case GET_BY_ID_METHOD:
      case FIND_METHOD:
        return;

      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }

    // user is not admin and it is need to check permissions on organization instance level
    final Organization organization = manager.getById(organizationId);
    final String parentOrganizationId = organization.getParent();
    // check permissions on parent organization level when updating or removing child organization
    if (parentOrganizationId != null
        && (OrganizationDomain.UPDATE.equals(action) || OrganizationDomain.DELETE.equals(action))) {
      if (currentSubject.hasPermission(
          OrganizationDomain.DOMAIN_ID, parentOrganizationId, MANAGE_SUBORGANIZATIONS)) {
        // user has permissions to manage organization on parent organization level
        return;
      }
    }

    if (!currentSubject.hasPermission(DOMAIN_ID, organizationId, action)) {
      throw new ForbiddenException(
          "The user does not have permission to "
              + action
              + " organization with id '"
              + organizationId
              + "'");
    }
  }
}

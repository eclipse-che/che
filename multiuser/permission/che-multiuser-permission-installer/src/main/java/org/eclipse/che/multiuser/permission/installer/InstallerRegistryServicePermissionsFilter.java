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
package org.eclipse.che.multiuser.permission.installer;

import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.installer.server.InstallerRegistryService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Protect access to the modifying methods of {@link InstallerRegistryService}
 *
 * @author Max Shaposhnyk
 */
@Filter
@Path("/installer{path:.*}")
public class InstallerRegistryServicePermissionsFilter extends CheMethodInvokerFilter {
  @Override
  protected void filter(GenericResourceMethod resource, Object[] args) throws ApiException {
    switch (resource.getMethod().getName()) {
        // Public methods
      case "getInstaller":
      case "getVersions":
      case "getInstallers":
      case "getOrderedInstallers":
        break;
      case "add":
      case "remove":
      case "update":
        EnvironmentContext.getCurrent()
            .getSubject()
            .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
        break;
      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }
  }
}

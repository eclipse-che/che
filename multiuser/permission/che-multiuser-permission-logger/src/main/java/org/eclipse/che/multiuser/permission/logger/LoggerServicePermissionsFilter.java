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
package org.eclipse.che.multiuser.permission.logger;

import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Filter that covers calls to {@link org.eclipse.che.api.logger.LoggerService} with authorization
 *
 * @author Florent Benoit
 */
@Filter
@Path("/logger{path:.*}")
public class LoggerServicePermissionsFilter extends CheMethodInvokerFilter {

  @Override
  protected void filter(GenericResourceMethod resource, Object[] args) throws ApiException {
    switch (resource.getMethod().getName()) {
      case "getLoggerByName":
      case "getLoggers":
      case "updateLogger":
      case "createLogger":
        EnvironmentContext.getCurrent()
            .getSubject()
            .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
        break;
      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }
  }
}

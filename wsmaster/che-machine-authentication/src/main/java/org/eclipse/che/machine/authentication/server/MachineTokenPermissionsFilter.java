/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.machine.authentication.server;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;


/**
 * Restricts access to methods of {@link MachineTokenService} by user's permissions
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Filter
@Path("/machine/token{path:(/.*)?}")
public class MachineTokenPermissionsFilter extends CheMethodInvokerFilter {

    @Override
    protected void filter(GenericResourceMethod genericResourceMethod, Object[] arguments) throws ApiException {
//        final String methodName = genericResourceMethod.getMethod().getName();
//
//        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
//        String action;
//        String workspaceId;
//
//        switch (methodName) {
//            case "getMachineToken": {
//                workspaceId = ((String)arguments[0]);
//                action = USE;
//                break;
//            }
//            case "getUser": {
//                return;
//            }
//            default:
//                throw new ForbiddenException("The user does not have permission to perform this operation");
//        }
//        currentSubject.checkPermission(DOMAIN_ID, workspaceId, action);
    }
}

/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.plugin.internal.api.PluginResolverNotFoundException;
import org.eclipse.che.plugin.internal.api.PluginException;
import org.eclipse.che.plugin.internal.api.PluginInstallerNotFoundException;
import org.eclipse.che.plugin.internal.api.PluginManagerAlreadyExistsException;
import org.eclipse.che.plugin.internal.api.PluginManagerNotFoundException;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Customize the exceptions to bring some HTTP status
 * @author Florent Benoit
 */
@Provider
@Singleton
public class CustomExceptionMapper implements ExceptionMapper<PluginException> {
    @Override
    public Response toResponse(PluginException exception) {

        if (exception instanceof PluginManagerNotFoundException || exception instanceof PluginInstallerNotFoundException || exception instanceof PluginResolverNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(DtoFactory.getInstance()
                                             .toJson(DtoFactory.getInstance().createDto(ServiceError.class)
                                                               .withMessage(exception.getMessage())))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        } else if (exception instanceof PluginManagerAlreadyExistsException) {
            return Response.status(Response.Status.CONFLICT)
                           .entity(DtoFactory.getInstance()
                                             .toJson(DtoFactory.getInstance().createDto(ServiceError.class)
                                                               .withMessage(exception.getMessage())))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }
        return Response.serverError()
                       .entity(DtoFactory.getInstance().createDto(ServiceError.class).withMessage(exception.getMessage()))
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
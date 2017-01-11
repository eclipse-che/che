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
package org.eclipse.che.api.core.rest;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author andrew00x
 * @author gazarenkov
 */
@Provider
@Singleton
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Override
    public Response toResponse(ApiException exception) {

        if (exception instanceof ForbiddenException)
            return Response.status(Response.Status.FORBIDDEN)
                           .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        else if (exception instanceof NotFoundException)
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        else if (exception instanceof UnauthorizedException)
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        else if (exception instanceof BadRequestException)
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        else if (exception instanceof ConflictException)
            return Response.status(Response.Status.CONFLICT)
                           .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        else if (exception instanceof ServerException)
            return Response.serverError()
                           .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        else
            return Response.serverError()
                           .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
    }
}

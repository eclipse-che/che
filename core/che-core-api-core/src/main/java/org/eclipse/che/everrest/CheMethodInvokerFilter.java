/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.everrest;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Abstract implementation of {@link MethodInvokerFilter} which allow to throw instances of {@link ApiException}
 * which will be mapped to corresponded response
 *
 * @author Sergii Leschenko
 */
public abstract class CheMethodInvokerFilter implements MethodInvokerFilter {
    @Override
    public void accept(GenericResourceMethod genericMethodResource, Object[] arguments) throws WebApplicationException {
        try {
            filter(genericMethodResource, arguments);
        } catch (ApiException exception) {
            Response response;
            if (exception instanceof ForbiddenException) {
                response = Response.status(Response.Status.FORBIDDEN)
                                   .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                                   .type(MediaType.APPLICATION_JSON)
                                   .build();
            } else if (exception instanceof NotFoundException) {
                response = Response.status(Response.Status.NOT_FOUND)
                                   .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                                   .type(MediaType.APPLICATION_JSON)
                                   .build();
            } else if (exception instanceof UnauthorizedException)
                response = Response.status(Response.Status.UNAUTHORIZED)
                                   .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                                   .type(MediaType.APPLICATION_JSON)
                                   .build();
            else if (exception instanceof BadRequestException) {
                response = Response.status(Response.Status.BAD_REQUEST)
                                   .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                                   .type(MediaType.APPLICATION_JSON)
                                   .build();
            } else if (exception instanceof ConflictException) {
                response = Response.status(Response.Status.CONFLICT)
                                   .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                                   .type(MediaType.APPLICATION_JSON)
                                   .build();
            } else if (exception instanceof ServerException) {
                response = Response.serverError()
                                   .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                                   .type(MediaType.APPLICATION_JSON)
                                   .build();
            } else {
                response = Response.serverError()
                                   .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                                   .type(MediaType.APPLICATION_JSON)
                                   .build();
            }

            throw new WebApplicationException(response);
        }
    }

    /**
     * Check does supplied method can be invoked.
     *
     * @param genericMethodResource
     *         See {@link GenericResourceMethod}
     * @param arguments
     *         actual method arguments that were created from request
     * @throws ApiException
     *         if method can not be invoked cause current environment context, e.g. for current user, with current request attributes, etc.
     */
    protected abstract void filter(GenericResourceMethod genericMethodResource, Object[] arguments) throws ApiException;
}

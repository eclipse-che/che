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
package org.eclipse.che.everrest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

/**
 * Abstract implementation of {@link MethodInvokerFilter} which allow to throw instances of {@link
 * ApiException} which will be mapped to corresponded response
 *
 * @author Sergii Leschenko
 */
public abstract class CheMethodInvokerFilter implements MethodInvokerFilter {
  @Override
  public void accept(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws WebApplicationException {
    try {

      filter(genericMethodResource, arguments);
    } catch (ApiException exception) {
      Response response;
      if (exception instanceof ForbiddenException) {
        response =
            Response.status(Response.Status.FORBIDDEN)
                .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                .type(MediaType.APPLICATION_JSON)
                .build();
      } else if (exception instanceof NotFoundException) {
        response =
            Response.status(Response.Status.NOT_FOUND)
                .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                .type(MediaType.APPLICATION_JSON)
                .build();
      } else if (exception instanceof UnauthorizedException)
        response =
            Response.status(Response.Status.UNAUTHORIZED)
                .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                .type(MediaType.APPLICATION_JSON)
                .build();
      else if (exception instanceof BadRequestException) {
        response =
            Response.status(Response.Status.BAD_REQUEST)
                .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                .type(MediaType.APPLICATION_JSON)
                .build();
      } else if (exception instanceof ConflictException) {
        response =
            Response.status(Response.Status.CONFLICT)
                .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                .type(MediaType.APPLICATION_JSON)
                .build();
      } else if (exception instanceof ServerException) {
        response =
            Response.serverError()
                .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                .type(MediaType.APPLICATION_JSON)
                .build();
      } else {
        response =
            Response.serverError()
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
   * @param genericMethodResource See {@link GenericResourceMethod}
   * @param arguments actual method arguments that were created from request
   * @throws ApiException if method can not be invoked cause current environment context, e.g. for
   *     current user, with current request attributes, etc.
   */
  protected abstract void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ApiException;
}

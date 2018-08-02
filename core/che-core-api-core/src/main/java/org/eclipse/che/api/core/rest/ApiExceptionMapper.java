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
package org.eclipse.che.api.core.rest;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.dto.server.DtoFactory;

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

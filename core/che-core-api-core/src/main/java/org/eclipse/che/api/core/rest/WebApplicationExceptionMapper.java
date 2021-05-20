/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Mapper for the {@link WebApplicationException} exceptions.
 *
 * @author Max Shaposhnyk
 */
@Provider
@Singleton
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

  @Override
  public Response toResponse(WebApplicationException exception) {

    ServiceError error = newDto(ServiceError.class).withMessage(exception.getMessage());

    if (exception instanceof BadRequestException) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(DtoFactory.getInstance().toJson(error))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } else if (exception instanceof ForbiddenException) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(DtoFactory.getInstance().toJson(error))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } else if (exception instanceof NotFoundException) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(DtoFactory.getInstance().toJson(error))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } else if (exception instanceof NotAuthorizedException) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(DtoFactory.getInstance().toJson(error))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } else if (exception instanceof NotAcceptableException) {
      return Response.status(Status.NOT_ACCEPTABLE)
          .entity(DtoFactory.getInstance().toJson(error))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } else if (exception instanceof NotAllowedException) {
      return Response.status(Status.METHOD_NOT_ALLOWED)
          .entity(DtoFactory.getInstance().toJson(error))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } else if (exception instanceof NotSupportedException) {
      return Response.status(Status.UNSUPPORTED_MEDIA_TYPE)
          .entity(DtoFactory.getInstance().toJson(error))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } else {
      return Response.serverError()
          .entity(DtoFactory.getInstance().toJson(error))
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
  }
}

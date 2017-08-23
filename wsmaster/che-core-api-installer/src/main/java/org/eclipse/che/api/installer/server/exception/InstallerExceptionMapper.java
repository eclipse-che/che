/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server.exception;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.dto.server.DtoFactory;

/** @author Anatolii Bazko */
@Provider
@Singleton
public class InstallerExceptionMapper implements ExceptionMapper<InstallerException> {
  @Override
  public Response toResponse(InstallerException exception) {
    ServiceError serviceError =
        DtoFactory.newDto(ServiceError.class).withMessage(exception.getMessage());
    String errorAsJson = DtoFactory.getInstance().toJson(serviceError);

    if (exception instanceof InstallerNotFoundException) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(errorAsJson)
          .type(MediaType.APPLICATION_JSON)
          .build();

    } else if (exception instanceof InstallerAlreadyExistsException) {
      return Response.status(Response.Status.CONFLICT)
          .entity(errorAsJson)
          .type(MediaType.APPLICATION_JSON)
          .build();

    } else if (exception instanceof IllegalInstallerKeyException) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(errorAsJson)
          .type(MediaType.APPLICATION_JSON)
          .build();

    } else {
      return Response.serverError().entity(errorAsJson).type(MediaType.APPLICATION_JSON).build();
    }
  }
}

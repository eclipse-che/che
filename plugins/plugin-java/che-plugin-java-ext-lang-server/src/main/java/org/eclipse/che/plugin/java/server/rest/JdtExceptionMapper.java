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
package org.eclipse.che.plugin.java.server.rest;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.java.server.JdtException;
import org.eclipse.che.plugin.java.server.search.SearchException;

/**
 * Exception mapper for all JDT exceptions
 *
 * @author Evgen Vidolob
 */
@Provider
@Singleton
public class JdtExceptionMapper implements ExceptionMapper<JdtException> {
  @Override
  public Response toResponse(JdtException exception) {
    if (exception instanceof SearchException) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
          .type(MediaType.APPLICATION_JSON)
          .build();
    } else
      return Response.serverError()
          .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
          .type(MediaType.APPLICATION_JSON)
          .build();
  }
}

/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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
import org.eclipse.che.api.core.AuthenticationException;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * javax.ws.rs.ext.ExceptionMapper for AuthenticationException
 *
 * @author Alexander Garagatyi
 */
@Provider
@Singleton
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {
  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationExceptionMapper.class);

  @Override
  public Response toResponse(AuthenticationException exception) {
    LOG.debug(exception.getLocalizedMessage());

    int responseStatus = exception.getResponseStatus();
    String message = exception.getMessage();
    if (message != null) {
      return Response.status(responseStatus)
          .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    return Response.status(responseStatus).build();
  }
}

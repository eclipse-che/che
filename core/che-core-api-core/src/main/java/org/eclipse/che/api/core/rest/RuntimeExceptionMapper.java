/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception mapper that provide error message with error time.
 *
 * @author Roman Nikitenko
 * @author Sergii Kabashniuk
 */
@Provider
@Singleton
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
  private static final Logger LOG = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

  @Override
  public Response toResponse(RuntimeException exception) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    final String utcTime = dateFormat.format(new Date());
    final String errorMessage = format("Internal Server Error occurred, error time: %s", utcTime);

    LOG.error(errorMessage, exception);

    ServiceError serviceError = DtoFactory.newDto(ServiceError.class).withMessage(errorMessage);
    return Response.serverError()
        .entity(DtoFactory.getInstance().toJson(serviceError))
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}

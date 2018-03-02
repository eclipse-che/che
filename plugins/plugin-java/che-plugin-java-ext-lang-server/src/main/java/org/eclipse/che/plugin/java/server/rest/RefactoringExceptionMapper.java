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
package org.eclipse.che.plugin.java.server.rest;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.java.server.refactoring.RefactoringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception mapper for all JDT exceptions
 *
 * @author Evgen Vidolob
 */
@Provider
@Singleton
public class RefactoringExceptionMapper implements ExceptionMapper<RefactoringException> {

  private static final Logger LOG = LoggerFactory.getLogger(RefactoringExceptionMapper.class);

  @Override
  public Response toResponse(RefactoringException exception) {
    LOG.error(exception.getMessage());
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}

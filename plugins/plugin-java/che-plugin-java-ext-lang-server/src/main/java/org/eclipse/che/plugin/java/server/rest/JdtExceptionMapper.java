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
package org.eclipse.che.plugin.java.server.rest;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.java.server.JdtException;
import org.eclipse.che.plugin.java.server.search.SearchException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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

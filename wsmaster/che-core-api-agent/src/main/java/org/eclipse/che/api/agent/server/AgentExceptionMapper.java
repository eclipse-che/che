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
package org.eclipse.che.api.agent.server;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception mapper for all Agent exceptions
 *
 * @author Anatolii Bazko
 */
@Provider
@Singleton
public class AgentExceptionMapper implements ExceptionMapper<AgentException> {

    /**
     * check the exception type and build Response with the status body and the type of the error
     */
    @Override
    public Response toResponse(AgentException exception) {
        if (exception instanceof AgentNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                           .type(MediaType.APPLICATION_JSON).build();
        }

        return Response.serverError()
                       .entity(DtoFactory.getInstance().toJson(exception.getServiceError()))
                       .type(MediaType.APPLICATION_JSON).build();
    }
}

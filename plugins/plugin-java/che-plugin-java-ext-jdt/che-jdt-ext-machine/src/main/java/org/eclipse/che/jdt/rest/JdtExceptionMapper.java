package org.eclipse.che.jdt.rest;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.jdt.JdtException;
import org.eclipse.che.jdt.search.SearchException;

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

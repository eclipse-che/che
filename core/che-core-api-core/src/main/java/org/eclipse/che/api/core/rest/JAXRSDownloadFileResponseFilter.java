/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.rest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Request;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;

/**
 * JAX-RS implementation of download filter.
 * @author Florent Benoit
 */
public class JAXRSDownloadFileResponseFilter extends DownloadFileResponseFilter implements ContainerResponseFilter {

    /**
     * JAX-RS Filter method called after a response has been provided for a request
     * <p>
     * Filters in the filter chain are ordered according to their {@code javax.annotation.Priority}
     * class-level annotation value.
     * </p>
     *
     * @param requestContext
     *         request context.
     * @param responseContext
     *         response context.
     * @throws IOException
     *         if an I/O exception occurs.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        // Apply header if all if correct
        Request request = requestContext.getRequest();
        String filename = getFileName(request, responseContext.getMediaType(), requestContext.getUriInfo(), responseContext.getStatus());
        if (filename != null) {
            if (hasCompliantEntity(responseContext.getEntity())) {
                responseContext.getHeaders().putSingle(CONTENT_DISPOSITION, "attachment; filename=" + filename);
            }
        }
    }
}

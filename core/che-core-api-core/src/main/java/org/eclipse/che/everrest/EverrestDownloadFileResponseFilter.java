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
package org.eclipse.che.everrest;

import org.eclipse.che.api.core.rest.DownloadFileResponseFilter;
import org.everrest.core.ApplicationContext;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ResponseFilter;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;

/**
 * JAX-RS implementation of download filter.
 * @author Florent Benoit
 */
@Filter
public class EverrestDownloadFileResponseFilter extends DownloadFileResponseFilter implements ResponseFilter {

    /**
     * Filter the given container response.
     *
     * @param containerResponse
     *         the response to use
     */
    public void doFilter(GenericContainerResponse containerResponse) {
        containerResponse.getResponse();

        // Get the request
        ApplicationContext applicationContext = ApplicationContext.getCurrent();
        Request request = applicationContext.getRequest();

        // Apply header if all if correct
        String filename = getFileName(request, containerResponse.getContentType(), applicationContext, containerResponse.getStatus());
        if (filename != null) {
            if (hasCompliantEntity(containerResponse.getEntity())) {
                // it has been changed, so send response with updated header
                Response.ResponseBuilder responseBuilder =
                        Response.fromResponse(containerResponse.getResponse()).header(CONTENT_DISPOSITION, "attachment; filename=" + filename);
                containerResponse.setResponse(responseBuilder.build());
            }
        }

    }
}

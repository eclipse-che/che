/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.jdt.rest;

import org.eclipse.che.plugin.java.server.classpath.ClassPathBuilder;
import org.eclipse.che.ide.ext.java.shared.dto.ClassPathBuilderResult;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;

/**
 * Service for building project classpath.
 *
 * @author Evgen Vidolob
 */
@Path("jdt/{wsId}/classpath")
public class JavaClasspathService {
    @Inject
    private ClassPathBuilder classPathBuilder;
    @PathParam("wsId")
    private String           workspaceId;

    /**
     * Update dependencies.
     *
     * @param projectPath
     *         the path to the current project
     * @return information about updating dependencies
     */
    @Path("update")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public ClassPathBuilderResult update(@QueryParam("projectpath") final String projectPath) throws ExecutionException,
                                                                                                     InterruptedException {
        return classPathBuilder.buildClassPath(workspaceId, projectPath);
    }
}

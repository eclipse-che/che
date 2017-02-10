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
package org.eclipse.che.plugin.java.server.rest;

import org.eclipse.che.jdt.JavadocFinder;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;

/**
 * Provides Javadoc API.
 *
 * @author Evgen Vidolob
 */
@Path("java/javadoc")
public class JavadocService {

    private final String agentEndpoint;

    @Inject
    public JavadocService(@Named("wsagent.endpoint") String agentEndpoint) {
        this.agentEndpoint = agentEndpoint;
    }

    @GET
    @Path("find")
    @Produces("text/html")
    public String findJavadoc(@QueryParam("fqn")
                              String fqn,
                              @QueryParam("projectpath")
                              String projectPath,
                              @QueryParam("offset")
                              int offset) throws JavaModelException {
        final IJavaProject project = JavaModelManager.getJavaModelManager()
                                                     .getJavaModel()
                                                     .getJavaProject(projectPath);
        final String urlPart = getUrlPart(projectPath);
        return new JavadocFinder(urlPart).findJavadoc(project, fqn, offset);
    }

    @GET
    @Path("get")
    @Produces("text/html")
    public String get(@QueryParam("handle")
                      String handle,
                      @QueryParam("projectpath")
                      String projectPath) {
        final IJavaProject project = JavaModelManager.getJavaModelManager()
                                                     .getJavaModel()
                                                     .getJavaProject(projectPath);
        final String urlPart = getUrlPart(projectPath);
        return new JavadocFinder(urlPart).findJavadoc4Handle(project, handle);
    }

    private String getUrlPart(String projectPath) {
        return UriBuilder.fromUri(agentEndpoint)
                         .queryParam("projectpath", projectPath)
                         .path(JavadocService.class)
                         .path(JavadocService.class, "get")
                         .build()
                         .toString() + "&handle=";
    }
}

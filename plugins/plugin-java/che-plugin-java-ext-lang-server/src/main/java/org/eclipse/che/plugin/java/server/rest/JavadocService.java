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

import org.eclipse.che.jdt.JavadocFinder;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * @author Evgen Vidolob
 */
@Path("java/javadoc")
public class JavadocService {

    @Path("find")
    @GET
    @Produces("text/html")
    public String findJavadoc(@QueryParam("fqn") String fqn, @QueryParam("projectpath") String projectPath,
                              @QueryParam("offset") int offset, @Context UriInfo uriInfo) throws JavaModelException {
        IJavaProject project = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectPath);
        String urlPart = getUrlPart(projectPath, uriInfo.getBaseUriBuilder());
        return new JavadocFinder(urlPart).findJavadoc(project, fqn, offset);
    }

    @Path("get")
    @Produces("text/html")
    @GET
    public String get(@QueryParam("handle") String handle, @QueryParam("projectpath") String projectPath, @Context UriInfo uriInfo) {
        IJavaProject project = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectPath);
        String urlPart = getUrlPart(projectPath, uriInfo.getBaseUriBuilder());
        return new JavadocFinder(urlPart).findJavadoc4Handle(project, handle);
    }

    private String getUrlPart(String projectPath, UriBuilder uriBuilder) {
        return uriBuilder.clone().path(JavadocService.class).path(JavadocService.class, "get").build().toString() + "?projectpath=" + projectPath + "&handle=";
    }

}

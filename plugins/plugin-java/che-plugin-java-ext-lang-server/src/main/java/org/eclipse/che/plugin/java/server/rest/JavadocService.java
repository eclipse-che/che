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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.che.JavadocUrlProvider;
import org.eclipse.che.jdt.JavadocFinder;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Provides Javadoc API.
 *
 * @author Evgen Vidolob
 */
@Path("java/javadoc")
public class JavadocService {

  private final JavadocUrlProvider urlProvider;

  @Inject
  public JavadocService(JavadocUrlProvider urlProvider) {
    this.urlProvider = urlProvider;
  }

  @GET
  @Path("find")
  @Produces("text/html")
  public String findJavadoc(
      @QueryParam("fqn") String fqn,
      @QueryParam("projectpath") String projectPath,
      @QueryParam("offset") int offset)
      throws JavaModelException {
    final IJavaProject project =
        JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectPath);
    final String urlPart = urlProvider.getJavadocUrl(projectPath);
    return new JavadocFinder(urlPart).findJavadoc(project, fqn, offset);
  }

  @GET
  @Path("get")
  @Produces("text/html")
  public String get(
      @QueryParam("handle") String handle, @QueryParam("projectpath") String projectPath) {
    final IJavaProject project =
        JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectPath);
    final String urlPart = urlProvider.getJavadocUrl(projectPath);
    return new JavadocFinder(urlPart).findJavadoc4Handle(project, handle);
  }
}

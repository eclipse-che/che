/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.rest;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;
import org.eclipse.che.plugin.java.server.JavaNavigation;
import org.eclipse.che.plugin.java.server.ParametersHints;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/** @author Evgen Vidolob */
@Path("java/navigation")
public class JavaNavigationService {

  JavaModel MODEL = JavaModelManager.getJavaModelManager().getJavaModel();

  @Inject private JavaNavigation navigation;
  @Inject private ParametersHints parametersHints;

  @GET
  @Path("get/projects/and/packages")
  @Produces("application/json")
  public List<JavaProject> getProjectsAndPackages(
      @QueryParam("includepackages") boolean includePackages) throws JavaModelException {
    return navigation.getAllProjectsAndPackages(includePackages);
  }

  @GET
  @Path("parameters")
  public List<MethodParameters> getParameters(
      @QueryParam("projectpath") String projectPath,
      @QueryParam("fqn") String fqn,
      @QueryParam("offset") int offset,
      @QueryParam("lineStart") int lineStartOffset)
      throws JavaModelException {
    IJavaProject project = MODEL.getJavaProject(projectPath);

    return parametersHints.findHints(project, fqn, offset, lineStartOffset);
  }
}

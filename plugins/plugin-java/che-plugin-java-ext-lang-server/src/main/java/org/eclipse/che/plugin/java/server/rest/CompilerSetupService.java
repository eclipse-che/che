/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Special service which allows control parameters of compiler for current project or current
 * workspace.
 *
 * @author Dmitry Shnurenko
 */
@Path("/java/compiler-settings")
public class CompilerSetupService {

  private static final JavaModel JAVA_MODEL = JavaModelManager.getJavaModelManager().getJavaModel();

  /**
   * Set java compiler preferences {@code changedParameters} for project by not empty path {@code
   * projectpath}. If {@code projectpath} is empty then java compiler preferences will be set for
   * current workspace.
   *
   * @param projectPath project path
   * @param changedParameters java compiler preferences
   */
  @POST
  @Path("/set")
  @Consumes(APPLICATION_JSON)
  public void setParameters(
      @QueryParam("projectpath") String projectPath,
      @NotNull Map<String, String> changedParameters) {
    if (projectPath == null || projectPath.isEmpty()) {
      JavaCore.setOptions(new Hashtable<>(changedParameters));
      return;
    }
    IJavaProject project = JAVA_MODEL.getJavaProject(projectPath);
    project.setOptions(changedParameters);
  }

  /**
   * Return java compiler preferences for current project by not empty path {@code projectpath}. If
   * {@code projectpath} if empty then return java compile preferences for current workspace.
   *
   * @param projectPath project path
   * @return java compiler preferences
   */
  @GET
  @Path("/all")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Map<String, String> getAllParameters(@QueryParam("projectpath") String projectPath) {
    if (projectPath == null || projectPath.isEmpty()) {
      // noinspection unchecked
      CompilerOptions options = new CompilerOptions(new HashMap<>(JavaCore.getOptions()));
      // noinspection unchecked
      return options.getMap();
    }

    IJavaProject project = JAVA_MODEL.getJavaProject(projectPath);

    // noinspection unchecked
    Map<String, String> map = project.getOptions(true);
    CompilerOptions options = new CompilerOptions(map);

    // noinspection unchecked
    return options.getMap();
  }
}

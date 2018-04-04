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

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.che.ide.ext.java.shared.Jar;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.ext.java.shared.dto.ClassContent;
import org.eclipse.che.ide.ext.java.shared.dto.ImplementationsDescriptorDTO;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;
import org.eclipse.che.plugin.java.server.JavaNavigation;
import org.eclipse.che.plugin.java.server.JavaTypeHierarchy;
import org.eclipse.che.plugin.java.server.ParametersHints;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/** @author Evgen Vidolob */
@Path("java/navigation")
public class JavaNavigationService {
  JavaModel MODEL = JavaModelManager.getJavaModelManager().getJavaModel();

  @Inject private JavaNavigation navigation;
  @Inject private JavaTypeHierarchy javaTypeHierarchy;
  @Inject private ParametersHints parametersHints;

  @GET
  @Path("contentbyfqn")
  @Produces("application/json")
  public ClassContent getContent(
      @QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn)
      throws JavaModelException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return navigation.getContent(project, fqn);
  }

  @GET
  @Path("find-declaration")
  @Produces("application/json")
  public OpenDeclarationDescriptor findDeclaration(
      @QueryParam("projectpath") String projectPath,
      @QueryParam("fqn") String fqn,
      @QueryParam("offset") int offset)
      throws JavaModelException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return navigation.findDeclaration(project, fqn, offset);
  }

  @GET
  @Path("libraries")
  @Produces("application/json")
  public List<Jar> getExternalLibraries(@QueryParam("projectpath") String projectPath)
      throws JavaModelException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return navigation.getProjectDependecyJars(project);
  }

  /**
   * Get all implementations of selected Java Element.
   *
   * @param projectPath path to the opened project
   * @param fqn fully qualified name of the class file
   * @param offset cursor position
   * @return descriptor of the implementations
   * @throws JavaModelException when JavaModel has a failure
   */
  @GET
  @Path("implementations")
  @Produces("application/json")
  public ImplementationsDescriptorDTO getImplementations(
      @QueryParam("projectpath") String projectPath,
      @QueryParam("fqn") String fqn,
      @QueryParam("offset") int offset)
      throws JavaModelException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return javaTypeHierarchy.getImplementations(project, fqn, offset);
  }

  @GET
  @Path("lib/children")
  @Produces("application/json")
  public List<JarEntry> getLibraryChildren(
      @QueryParam("projectpath") String projectPath, @QueryParam("root") int rootId)
      throws JavaModelException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return navigation.getPackageFragmentRootContent(project, rootId);
  }

  @GET
  @Path("children")
  @Produces("application/json")
  public List<JarEntry> getChildren(
      @QueryParam("projectpath") String projectPath,
      @QueryParam("path") String path,
      @QueryParam("root") int rootId)
      throws JavaModelException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return navigation.getChildren(project, rootId, path);
  }

  /**
   * Create compilation unit model for the opened java class.
   *
   * @param projectPath path to the project which is contained class file
   * @param fqn fully qualified name of the class file
   * @param showInherited <code>true</code> iff inherited members are shown
   * @return compilation unit of the java source file
   * @throws JavaModelException when JavaModel has a failure
   */
  @GET
  @Path("compilation-unit")
  @Produces("application/json")
  public CompilationUnit getCompilationUnit(
      @QueryParam("projectpath") String projectPath,
      @QueryParam("fqn") String fqn,
      @QueryParam("showinherited") boolean showInherited)
      throws JavaModelException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return navigation.getCompilationUnitByPath(project, fqn, showInherited);
  }

  @GET
  @Path("content")
  @Produces("application/json")
  public ClassContent getContent(
      @QueryParam("projectpath") String projectPath,
      @QueryParam("path") String path,
      @QueryParam("root") int rootId)
      throws CoreException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return navigation.getContent(project, rootId, path);
  }

  @GET
  @Path("entry")
  public JarEntry getEntry(
      @QueryParam("projectpath") String projectPath,
      @QueryParam("path") String path,
      @QueryParam("root") int rootId)
      throws CoreException {
    IJavaProject project = MODEL.getJavaProject(projectPath);
    return navigation.getEntry(project, rootId, path);
  }

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

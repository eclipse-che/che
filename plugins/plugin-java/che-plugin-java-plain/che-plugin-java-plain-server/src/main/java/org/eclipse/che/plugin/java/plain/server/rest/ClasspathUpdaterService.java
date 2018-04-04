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
package org.eclipse.che.plugin.java.plain.server.rest;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.core.runtime.Path.fromOSString;
import static org.eclipse.jdt.core.JavaCore.newContainerEntry;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;
import static org.eclipse.jdt.core.JavaCore.newProjectEntry;
import static org.eclipse.jdt.core.JavaCore.newSourceEntry;
import static org.eclipse.jdt.core.JavaCore.newVariableEntry;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.NewProjectConfigImpl;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Service for updating classpath.
 *
 * @author Valeriy Svydenko
 */
@Path("jdt/classpath/update")
public class ClasspathUpdaterService {

  private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();

  private final ProjectManager projectManager;
  private final PathTransformer pathTransformer;

  @Inject
  public ClasspathUpdaterService(ProjectManager projectManager, PathTransformer pathTransformer) {
    this.projectManager = projectManager;
    this.pathTransformer = pathTransformer;
  }

  /**
   * Updates the information about classpath.
   *
   * @param projectPath path to the current project
   * @param entries list of classpath entries which need to set
   * @throws JavaModelException if JavaModel has a failure
   * @throws ServerException if some server error
   * @throws ForbiddenException if operation is forbidden
   * @throws ConflictException if update operation causes conflicts
   * @throws NotFoundException if Project with specified path doesn't exist in workspace
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateClasspath(
      @QueryParam("projectpath") String projectPath, List<ClasspathEntryDto> entries)
      throws JavaModelException, ServerException, ForbiddenException, ConflictException,
          NotFoundException, IOException, BadRequestException {
    IJavaProject javaProject = model.getJavaProject(projectPath);

    javaProject.setRawClasspath(
        createModifiedEntry(entries), javaProject.getOutputLocation(), new NullProgressMonitor());

    updateProjectConfig(projectPath);
  }

  private void updateProjectConfig(String projectWsPath)
      throws IOException, ForbiddenException, ConflictException, NotFoundException, ServerException,
          BadRequestException {
    String wsPath = absolutize(projectWsPath);
    RegisteredProject project =
        projectManager
            .get(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find project: " + projectWsPath));

    NewProjectConfig projectConfig =
        new NewProjectConfigImpl(
            projectWsPath, project.getName(), project.getType(), project.getSource());
    projectManager.update(projectConfig);
  }

  private IClasspathEntry[] createModifiedEntry(List<ClasspathEntryDto> entries) {
    List<IClasspathEntry> coreClasspathEntries = new ArrayList<>(entries.size());
    for (ClasspathEntryDto entry : entries) {
      IPath path = fromOSString(entry.getPath());
      int entryKind = entry.getEntryKind();
      if (IClasspathEntry.CPE_LIBRARY == entryKind) {
        coreClasspathEntries.add(newLibraryEntry(path, null, null));
      } else if (IClasspathEntry.CPE_SOURCE == entryKind) {
        coreClasspathEntries.add(newSourceEntry(path));
      } else if (IClasspathEntry.CPE_VARIABLE == entryKind) {
        coreClasspathEntries.add(newVariableEntry(path, null, null));
      } else if (IClasspathEntry.CPE_CONTAINER == entryKind) {
        coreClasspathEntries.add(newContainerEntry(path));
      } else if (IClasspathEntry.CPE_PROJECT == entryKind) {
        coreClasspathEntries.add(newProjectEntry(path));
      }
    }
    return coreClasspathEntries.toArray(new IClasspathEntry[coreClasspathEntries.size()]);
  }
}

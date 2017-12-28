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
package org.eclipse.che.plugin.java.plain.server.rest;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;

import com.google.inject.Inject;
import java.io.IOException;
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
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.NewProjectConfigImpl;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;

/**
 * Service for updating classpath.
 *
 * @author Valeriy Svydenko
 */
@Path("jdt/classpath/update")
public class ClasspathUpdaterService {
  private final ProjectManager projectManager;
  private JavaLanguageServerExtensionService extensionService;

  @Inject
  public ClasspathUpdaterService(
      ProjectManager projectManager, JavaLanguageServerExtensionService extensionService) {
    this.projectManager = projectManager;
    this.extensionService = extensionService;
  }

  /**
   * Updates the information about classpath.
   *
   * @param projectPath path to the current project
   * @param entries list of classpath entries which need to set
   * @throws ServerException if some server error
   * @throws ForbiddenException if operation is forbidden
   * @throws ConflictException if update operation causes conflicts
   * @throws NotFoundException if Project with specified path doesn't exist in workspace
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateClasspath(
      @QueryParam("projectpath") String projectPath, List<ClasspathEntry> entries)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException, IOException,
          BadRequestException {
    extensionService.updateClasspath(prefixURI(projectPath), entries);

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
}

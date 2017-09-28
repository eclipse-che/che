/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jsonexample;

import com.google.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.api.FsManager;
import org.eclipse.che.api.fs.api.PathResolver;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.api.ProjectManager;

/**
 * Service for counting lines of code within all JSON files in a given project.
 */
@Path("json-example/{ws-id}")
@Singleton
public class JsonLocService {

  private final ProjectManager projectManager;
  private final PathResolver pathResolver;
  private final FsManager fsManager;


  /**
   * Constructor for the JSON Exapmle lines of code service.
   *
   * @param projectManager the {@link ProjectManager} that is used to access the project resources
   */
  @Inject
  public JsonLocService(ProjectManager projectManager,
      PathResolver pathResolver, FsManager fsManager) {
    this.projectManager = projectManager;
    this.pathResolver = pathResolver;
    this.fsManager = fsManager;
  }

  private int countLines(String fileWsPath) throws ServerException, ForbiddenException {
    try {
      return fsManager.readFileAsString(fileWsPath).split("\r\n|\r|\n").length;
    } catch (NotFoundException e) {
      throw new ServerException(e);
    }
  }

  private boolean isJsonFile(String fileWsPath) {
    return pathResolver.getName(fileWsPath).endsWith("json");
  }

  /**
   * Count LOC for all JSON files within the given project.
   *
   * @param projectPath the path to the project that contains the JSON files for which to calculate
   * the LOC
   * @return a Map mapping the file name to their respective LOC value
   * @throws ServerException in case the server encounters an error
   * @throws NotFoundException in case the project couldn't be found
   * @throws ForbiddenException in case the operation is forbidden
   */
  @GET
  @Path("{projectPath}")
  public Map<String, String> countLinesPerFile(@PathParam("projectPath") String projectPath)
      throws ServerException, NotFoundException, ForbiddenException {
    String projectWsPath = pathResolver.toAbsoluteWsPath(projectPath);
    Map<String, String> linesPerFile = new LinkedHashMap<>();
    RegisteredProject project = projectManager.get(projectWsPath)
        .orElseThrow(() -> new NotFoundException("Can't find project: " + projectPath));
    Set<String> fileWsPaths = fsManager.getFileWsPaths(projectWsPath);
    for (String fileWsPath : fileWsPaths) {
      if (isJsonFile(fileWsPath)) {
        String name = pathResolver.getName(fileWsPath);
        linesPerFile.put(name, Integer.toString(countLines(fileWsPath)));
      }
    }

    return linesPerFile;
  }
}

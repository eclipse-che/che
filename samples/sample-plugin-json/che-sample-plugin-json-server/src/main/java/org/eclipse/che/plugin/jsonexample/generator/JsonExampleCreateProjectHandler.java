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
package org.eclipse.che.plugin.jsonexample.generator;

import static org.eclipse.che.plugin.jsonexample.shared.Constants.JSON_EXAMPLE_PROJECT_TYPE_ID;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.FsPaths;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;

/**
 * Generates a new project which contains a package.json with default content and a default
 * person.json file within an myJsonFiles folder.
 */
public class JsonExampleCreateProjectHandler implements CreateProjectHandler {

  private final FsManager fsManager;
  private final FsPaths fsPaths;

  @Inject
  public JsonExampleCreateProjectHandler(FsManager fsManager, FsPaths fsPaths) {
    this.fsManager = fsManager;
    this.fsPaths = fsPaths;
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {

    try (InputStream packageJsonContent =
            getClass().getClassLoader().getResourceAsStream("files/default_package");
        InputStream personJsonContent =
            getClass().getClassLoader().getResourceAsStream("files/default_person")) {

      String packageJsonWsPath = fsPaths.resolve(projectWsPath, "package.json");
      fsManager.createFile(packageJsonWsPath, packageJsonContent);

      String myJsonFilesWsPath = fsPaths.resolve(projectWsPath, "myJsonFiles");
      fsManager.createDirectory(myJsonFilesWsPath);

      String personJsonWsPath = fsPaths.resolve(myJsonFilesWsPath, "myJsonFiles");
      fsManager.createFile(personJsonWsPath, personJsonContent);

    } catch (IOException ioEx) {
      throw new ServerException(ioEx.getLocalizedMessage(), ioEx);
    }
  }

  @Override
  public String getProjectType() {
    return JSON_EXAMPLE_PROJECT_TYPE_ID;
  }
}

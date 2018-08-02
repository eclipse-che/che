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
package org.eclipse.che.plugin.cpp.generator;

import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.plugin.cpp.shared.Constants;

public class CppProjectGenerator implements CreateProjectHandler {

  private static final String RESOURCE_NAME = "files/default_cpp_content";
  private static final String FILE_NAME = "hello.cpp";

  private final FsManager fsManager;

  @Inject
  public CppProjectGenerator(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
      fsManager.createDir(projectWsPath);
      String wsPath = resolve(projectWsPath, FILE_NAME);
      fsManager.createFile(wsPath, inputStream);
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public String getProjectType() {
    return Constants.CPP_PROJECT_TYPE_ID;
  }
}

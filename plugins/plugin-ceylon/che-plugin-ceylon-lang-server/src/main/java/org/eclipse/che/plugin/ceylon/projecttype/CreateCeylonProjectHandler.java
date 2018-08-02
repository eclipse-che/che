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
package org.eclipse.che.plugin.ceylon.projecttype;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;

import java.io.ByteArrayInputStream;
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
import org.eclipse.che.plugin.ceylon.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author David Festal */
public class CreateCeylonProjectHandler implements CreateProjectHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CreateCeylonProjectHandler.class);

  private static final String[] PROJECT_FILES =
      new String[] {
        "ceylonb",
        "ceylonb.bat",
        ".ceylon/config",
        ".ceylon/ide-config",
        ".ceylon/bootstrap/ceylon-bootstrap.jar",
        ".ceylon/bootstrap/ceylon-bootstrap.properties",
        "source/run.ceylon"
      };

  private final FsManager fsManager;

  @Inject
  public CreateCeylonProjectHandler(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {

    fsManager.createDir(projectWsPath);
    fsManager.createDir(resolve(projectWsPath, "source"));
    fsManager.createDir(resolve(projectWsPath, "resource"));
    for (String file : PROJECT_FILES) {
      InputStream inputStream = new ByteArrayInputStream(getProjectContent("project/" + file));
      String wsPath = resolve(projectWsPath, file);
      fsManager.createFile(wsPath, inputStream, true, true);
      if (file.startsWith("ceylonb")) {
        fsManager.toIoFile(wsPath).setExecutable(true);
      }
    }
  }

  private byte[] getProjectContent(String file) {
    try {
      return toByteArray(getResource(file));
    } catch (IOException e) {
      LOG.warn("File %s not found so its content will be empty.", file);
      return new byte[0];
    }
  }

  @Override
  public String getProjectType() {
    return Constants.CEYLON_PROJECT_TYPE_ID;
  }
}

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
package org.eclipse.che.plugin.csharp.projecttype;

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
import org.eclipse.che.plugin.csharp.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
public class CreateNetCoreProjectHandler implements CreateProjectHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CreateNetCoreProjectHandler.class);

  private static final String PROJECT_FILE_NAME = "project.json";

  private final FsManager fsManager;

  @Inject
  public CreateNetCoreProjectHandler(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {

    try (InputStream inputStream = new ByteArrayInputStream(getProjectContent()); ) {
      fsManager.createDir(projectWsPath);
      String wsPath = resolve(projectWsPath, PROJECT_FILE_NAME);
      fsManager.createFile(wsPath, inputStream);
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  private byte[] getProjectContent() {
    String filename = "project.json.default";
    try {
      return toByteArray(getResource(filename));
    } catch (IOException e) {
      LOG.warn("File %s not found so content of %s will be empty.", filename, PROJECT_FILE_NAME);
      return new byte[0];
    }
  }

  @Override
  public String getProjectType() {
    return Constants.CSHARP_PROJECT_TYPE_ID;
  }
}

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
package org.eclipse.che.api.project.server.impl;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
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
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle creation new Blank project and create README file inside root folder of project.
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class CreateBaseProjectTypeHandler implements CreateProjectHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CreateBaseProjectTypeHandler.class);

  private final FsManager fsManager;

  private final String README_FILE_NAME = "README";

  @Inject
  public CreateBaseProjectTypeHandler(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {
    try (InputStream inputStream = getReadmeContent()) {
      fsManager.createDir(projectWsPath, true, true);
      String wsPath = resolve(projectWsPath, README_FILE_NAME);
      fsManager.createFile(wsPath, inputStream);
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public String getProjectType() {
    return BaseProjectType.ID;
  }

  @VisibleForTesting
  protected InputStream getReadmeContent() {
    String filename = "README.blank";
    try {
      return new ByteArrayInputStream(toByteArray(getResource(filename)));
    } catch (IOException e) {
      LOG.warn("File %s not found so content of %s will be empty.", filename, README_FILE_NAME);
      return new ByteArrayInputStream(new byte[0]);
    }
  }
}

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
package org.eclipse.che.api.project.server.impl;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import java.io.IOException;
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
  private final FsPaths fsPaths;

  private final String README_FILE_NAME = "README";

  @Inject
  public CreateBaseProjectTypeHandler(FsManager fsManager, FsPaths fsPaths) {
    this.fsManager = fsManager;
    this.fsPaths = fsPaths;
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {
    fsManager.createDirectory(projectWsPath);
    String wsPath = fsPaths.resolve(projectWsPath, README_FILE_NAME);
    fsManager.createFile(wsPath, getReadmeContent());
  }

  @Override
  public String getProjectType() {
    return BaseProjectType.ID;
  }

  @VisibleForTesting
  protected byte[] getReadmeContent() {
    String filename = "README.blank";
    try {
      return toByteArray(getResource(filename));
    } catch (IOException e) {
      LOG.warn("File %s not found so content of %s will be empty.", filename, README_FILE_NAME);
      return new byte[0];
    }
  }
}

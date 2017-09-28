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
package org.eclipse.che.api.git;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.api.FsManager;
import org.eclipse.che.api.fs.api.PathResolver;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.VcsStatusProvider;
import org.eclipse.che.api.project.server.api.ProjectManager;

/**
 * Git implementation of {@link VcsStatusProvider}.
 *
 * @author Igor Vinokur
 */
public class GitStatusProvider implements VcsStatusProvider {

  private final GitConnectionFactory gitConnectionFactory;
  private final FsManager fsManager;
  private final PathResolver pathResolver;
  private final ProjectManager projectManager;

  @Inject
  public GitStatusProvider(
      GitConnectionFactory gitConnectionFactory,
      FsManager fsManager,
      PathResolver pathResolver,
      ProjectManager projectManager) {
    this.gitConnectionFactory = gitConnectionFactory;
    this.fsManager = fsManager;
    this.pathResolver = pathResolver;
    this.projectManager = projectManager;
  }

  @Override
  public String getVcsName() {
    return GitProjectType.TYPE_ID;
  }

  @Override
  public VcsStatus getStatus(String wsPath) throws ServerException {
    try {
      RegisteredProject project =
          projectManager
              .getClosest(wsPath)
              .orElseThrow(() -> new NotFoundException("Can't find project"));
      String projectFsPath = pathResolver.toFsPath(project.getPath()).toString();
      String projectName = pathResolver.getName(project.getPath());
      String itemPath = wsPath.substring(wsPath.indexOf(projectName + "/"));
      Status status =
          gitConnectionFactory.getConnection(projectFsPath).status(singletonList(itemPath));
      if (status.getUntracked().contains(itemPath)) {
        return VcsStatus.UNTRACKED;
      } else if (status.getAdded().contains(itemPath)) {
        return VcsStatus.ADDED;
      } else if (status.getModified().contains(itemPath)
          || status.getChanged().contains(itemPath)) {
        return VcsStatus.MODIFIED;
      } else {
        return VcsStatus.NOT_MODIFIED;
      }
    } catch (GitException | NotFoundException e) {
      throw new ServerException(e.getMessage());
    }
  }

  @Override
  public Map<String, VcsStatus> getStatus(String wsPath, List<String> paths)
      throws ServerException {
    Map<String, VcsStatus> statusMap = new HashMap<>();
    try {
      RegisteredProject project =
          projectManager
              .getClosest(wsPath)
              .orElseThrow(() -> new NotFoundException("Can't find project"));
      String projectFsPath = pathResolver.toFsPath(project.getPath()).toString();
      Status status = gitConnectionFactory.getConnection(projectFsPath).status(paths);
      paths.forEach(
          path -> {
            if (status.getUntracked().contains(path)) {
              statusMap.put("/" + project + "/" + path, VcsStatus.UNTRACKED);
            } else if (status.getAdded().contains(path)) {
              statusMap.put("/" + project + "/" + path, VcsStatus.ADDED);
            } else if (status.getModified().contains(path) || status.getChanged().contains(path)) {
              statusMap.put("/" + project + "/" + path, VcsStatus.MODIFIED);
            } else {
              statusMap.put("/" + project + "/" + path, VcsStatus.NOT_MODIFIED);
            }
          });

    } catch (GitException | NotFoundException e) {
      throw new ServerException(e.getMessage());
    }
    return statusMap;
  }
}

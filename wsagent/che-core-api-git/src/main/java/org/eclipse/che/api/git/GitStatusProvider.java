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
import static org.eclipse.che.api.project.server.impl.VcsStatusProvider.VcsStatus.ADDED;
import static org.eclipse.che.api.project.server.impl.VcsStatusProvider.VcsStatus.MODIFIED;
import static org.eclipse.che.api.project.server.impl.VcsStatusProvider.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.api.project.server.impl.VcsStatusProvider.VcsStatus.UNTRACKED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.FsPathResolver;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.project.server.impl.VcsStatusProvider;
import org.eclipse.che.api.project.server.ProjectManager;

/**
 * Git implementation of {@link VcsStatusProvider}.
 *
 * @author Igor Vinokur
 */
public class GitStatusProvider implements VcsStatusProvider {

  private final GitConnectionFactory gitConnectionFactory;
  private final FsManager fsManager;
  private final FsPathResolver fsPathResolver;
  private final ProjectManager projectManager;

  @Inject
  public GitStatusProvider(
      GitConnectionFactory gitConnectionFactory,
      FsManager fsManager,
      FsPathResolver fsPathResolver,
      ProjectManager projectManager) {
    this.gitConnectionFactory = gitConnectionFactory;
    this.fsManager = fsManager;
    this.fsPathResolver = fsPathResolver;
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
      String projectFsPath = fsPathResolver.toFsPath(project.getPath()).toString();
      String projectName = fsPathResolver.getName(project.getPath());
      String itemPath = wsPath.substring(wsPath.indexOf(projectName + "/"));
      Status status =
          gitConnectionFactory.getConnection(projectFsPath).status(singletonList(itemPath));
      if (status.getUntracked().contains(itemPath)) {
        return UNTRACKED;
      } else if (status.getAdded().contains(itemPath)) {
        return ADDED;
      } else if (status.getModified().contains(itemPath)
          || status.getChanged().contains(itemPath)) {
        return MODIFIED;
      } else {
        return NOT_MODIFIED;
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
      String projectFsPath = fsPathResolver.toFsPath(project.getPath()).toString();
      Status status = gitConnectionFactory.getConnection(projectFsPath).status(paths);
      paths.forEach(
          path -> {
            String itemWsPath = project.getPath() + "/" + path;
            if (status.getUntracked().contains(path)) {
              statusMap.put(itemWsPath, UNTRACKED);
            } else if (status.getAdded().contains(path)) {
              statusMap.put(itemWsPath, ADDED);
            } else if (status.getModified().contains(path) || status.getChanged().contains(path)) {
              statusMap.put(itemWsPath, MODIFIED);
            } else {
              statusMap.put(itemWsPath, NOT_MODIFIED);
            }
          });

    } catch (GitException | NotFoundException e) {
      throw new ServerException(e.getMessage());
    }
    return statusMap;
  }
}

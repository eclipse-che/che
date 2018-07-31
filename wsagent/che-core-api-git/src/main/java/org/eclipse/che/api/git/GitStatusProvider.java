/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.fs.server.WsPathUtils.SEPARATOR;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.ADDED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.MODIFIED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.UNTRACKED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VcsStatusProvider;

/**
 * Git implementation of {@link VcsStatusProvider}.
 *
 * @author Igor Vinokur
 */
public class GitStatusProvider implements VcsStatusProvider {

  private final GitConnectionFactory gitConnectionFactory;
  private final PathTransformer pathTransformer;
  private final ProjectManager projectManager;

  @Inject
  public GitStatusProvider(
      GitConnectionFactory gitConnectionFactory,
      PathTransformer pathTransformer,
      ProjectManager projectManager) {
    this.gitConnectionFactory = gitConnectionFactory;
    this.pathTransformer = pathTransformer;
    this.projectManager = projectManager;
  }

  @Override
  public String getVcsName() {
    return GitProjectType.TYPE_ID;
  }

  @Override
  public VcsStatus getStatus(String wsPath) throws ServerException {
    try {
      ProjectConfig project =
          projectManager
              .getClosest(wsPath)
              .orElseThrow(() -> new NotFoundException("Can't find project"));
      String projectFsPath = pathTransformer.transform(project.getPath()).toString();
      wsPath = wsPath.substring(wsPath.startsWith(SEPARATOR) ? 1 : 0);
      String itemPath = wsPath.substring(wsPath.indexOf(SEPARATOR) + 1);
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
      ProjectConfig project =
          projectManager
              .getClosest(absolutize(wsPath))
              .orElseThrow(() -> new NotFoundException("Can't find project"));
      String projectFsPath = pathTransformer.transform(project.getPath()).toString();
      Status status = gitConnectionFactory.getConnection(projectFsPath).status(paths);
      paths.forEach(
          path -> {
            String itemWsPath = resolve(project.getPath(), path);
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

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
import javax.inject.Provider;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
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
  private final Provider<ProjectManager> projectManagerProvider;

  @Inject
  public GitStatusProvider(
      GitConnectionFactory gitConnectionFactory, Provider<ProjectManager> projectManagerProvider) {
    this.gitConnectionFactory = gitConnectionFactory;
    this.projectManagerProvider = projectManagerProvider;
  }

  @Override
  public String getVcsName() {
    return GitProjectType.TYPE_ID;
  }

  @Override
  public VcsStatus getStatus(String path) throws ServerException {
    try {
      String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
      String projectPath =
          projectManagerProvider
              .get()
              .getProject(normalizedPath.split("/")[0])
              .getBaseFolder()
              .getVirtualFile()
              .toIoFile()
              .getAbsolutePath();
      Status status =
          gitConnectionFactory
              .getConnection(projectPath)
              .status(singletonList(normalizedPath.substring(normalizedPath.indexOf("/") + 1)));
      String itemPath = normalizedPath.substring(normalizedPath.indexOf("/") + 1);
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
  public Map<String, VcsStatus> getStatus(String project, List<String> paths)
      throws ServerException {
    Map<String, VcsStatus> statusMap = new HashMap<>();
    try {
      String projectPath =
          projectManagerProvider
              .get()
              .getProject(project)
              .getBaseFolder()
              .getVirtualFile()
              .toIoFile()
              .getAbsolutePath();
      Status status = gitConnectionFactory.getConnection(projectPath).status(paths);
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

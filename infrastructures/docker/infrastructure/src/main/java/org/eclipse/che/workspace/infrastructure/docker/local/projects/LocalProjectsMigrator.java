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
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static org.eclipse.che.api.core.Pages.iterate;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs migration of workspace project files, that are stored in old format (in directories
 * named after their workspace name), so they will be stored in folders named after their workspace
 * ID.
 *
 * @author Mykhailo Kuznietsov
 */
public class LocalProjectsMigrator {

  private final WorkspaceDao workspaceDao;

  @Inject
  public LocalProjectsMigrator(WorkspaceDao workspaceDao) {
    this.workspaceDao = workspaceDao;
  }

  private static final Logger LOG = LoggerFactory.getLogger(LocalProjectsMigrator.class);

  public void performMigration(String workspaceProjectsRootFolder) {
    LOG.info("Starting migration of workspace project files");
    Map<String, String> workspaceName2id = getId2NameWorkspaceMapping();
    for (Entry<String, String> entry : workspaceName2id.entrySet()) {
      Path workspaceStoredByNameLocation =
          Paths.get(workspaceProjectsRootFolder).resolve(entry.getValue());
      if (!Files.exists(workspaceStoredByNameLocation)) {
        // migration is not needed for this workspace
        continue;
      }
      Path workspaceStoredByIdLocation =
          Paths.get(workspaceProjectsRootFolder).resolve(entry.getKey());
      try {
        Files.move(
            workspaceStoredByNameLocation,
            workspaceStoredByIdLocation,
            StandardCopyOption.ATOMIC_MOVE);
        LOG.info(
            "Successfully migrated projects of workspace with id '{}' and name '{}'",
            entry.getKey(),
            entry.getValue());
      } catch (IOException e) {
        LOG.error(
            "Failed to migrate projects of workspace with id '{}' and name '{}'",
            entry.getKey(),
            entry.getValue());
      }
    }
  }

  private Map<String, String> getId2NameWorkspaceMapping() {
    try {
      Map<String, String> result = new HashMap<>();

      for (WorkspaceImpl workspace :
          iterate(
              (maxItems, skipCount) -> workspaceDao.getWorkspaces(false, maxItems, skipCount))) {
        result.put(workspace.getId(), workspace.getName());
      }
      return result;
    } catch (ServerException e) {
      throw new RuntimeException(e);
    }
  }
}

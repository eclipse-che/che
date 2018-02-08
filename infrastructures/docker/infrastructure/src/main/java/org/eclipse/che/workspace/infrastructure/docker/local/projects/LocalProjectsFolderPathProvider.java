/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.WindowsHostUtils;

/**
 * Provides path to workspace projects folder on host.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class LocalProjectsFolderPathProvider {

  static final String ALLOW_FOLDERS_CREATION_PROPERTY = "che.workspace.storage.create.folders";
  static final String WORKSPACE_STORAGE_MIGRATION_FLAG_PROPERTY =
      "che.workspace.migrate_workspace_projects_on_startup";
  static final String WORKSPACE_PROJECTS_MOUNT_POINT_PROPERTY = "che.workspace.storage";
  static final String CHE_MASTER_SPECIFIC_WORKSPACE_PROJECTS_MOUNT_POINT_PROPERTY =
      "che.workspace.storage_master_path";
  static final String SINGLE_MOUNTPOINT_FOR_ALL_WORKSPACES_PROPERTY = "host.projects.root";

  private final boolean isWindows;
  private final boolean migrationOnStartup;
  private final LocalProjectsMigrator localProjectsMigrator;
  private final WorkspaceDao workspaceDao;

  /**
   * Value provide path to directory on host machine where will by all created and mount to the
   * created workspaces folder that become root of workspace inside machine. Inside machine it will
   * point to the directory described by {@literal che.workspace.projects.storage}.
   *
   * <p>For example: if you set {@literal che.workspaces.storage} to the /home/user/che/workspaces
   * after creating new workspace will be created new folder /home/user/che/workspaces/{workspaceId}
   * and it will be mount to the machine with projects volume to {@literal
   * che.workspace.projects.storage}
   */
  private final String workspacesMountPoint;

  /**
   * This value provide path to projects on local host if this value will be set all workspace will
   * manage same projects from your host
   */
  @Inject(optional = true)
  @Named(SINGLE_MOUNTPOINT_FOR_ALL_WORKSPACES_PROPERTY)
  private String singleMountPointForAllWorkspaces;

  @Inject(optional = true)
  @Named(CHE_MASTER_SPECIFIC_WORKSPACE_PROJECTS_MOUNT_POINT_PROPERTY)
  private String workspacesMountPointForCheMaster;

  @Inject(optional = true)
  @Named(ALLOW_FOLDERS_CREATION_PROPERTY)
  private boolean createFolders = true;

  @Inject
  public LocalProjectsFolderPathProvider(
      @Named(WORKSPACE_PROJECTS_MOUNT_POINT_PROPERTY) String workspacesMountPoint,
      @Named(WORKSPACE_STORAGE_MIGRATION_FLAG_PROPERTY) boolean migrationOnStartup,
      WorkspaceDao workspaceDao,
      LocalProjectsMigrator localProjectsMigrator)
      throws IOException {
    this(
        workspacesMountPoint,
        null,
        migrationOnStartup,
        null,
        null,
        workspaceDao,
        localProjectsMigrator,
        SystemInfo.isWindows());
  }

  @VisibleForTesting
  protected LocalProjectsFolderPathProvider(
      String workspacesMountPoint,
      @Nullable String workspacesMountPointForCheMaster,
      boolean migrationOnStartup,
      @Nullable String singleMountPointForAllWorkspaces,
      @Nullable Boolean createFolders,
      WorkspaceDao workspaceDao,
      LocalProjectsMigrator localProjectsMigrator,
      boolean isWindows)
      throws IOException {
    this.workspaceDao = workspaceDao;
    this.migrationOnStartup = migrationOnStartup;
    this.isWindows = isWindows;
    this.localProjectsMigrator = localProjectsMigrator;

    if (createFolders != null) {
      this.createFolders = createFolders;
    }
    if (singleMountPointForAllWorkspaces != null) {
      this.singleMountPointForAllWorkspaces = singleMountPointForAllWorkspaces;
    }
    // In case workspace mount path specific for che master is not defined we treat it equal to
    // regular workspace mount point
    if (workspacesMountPointForCheMaster != null) {
      this.workspacesMountPointForCheMaster = workspacesMountPointForCheMaster;
    } else {
      this.workspacesMountPointForCheMaster = workspacesMountPoint;
    }
    // Priority of workspace storage path sources:
    // If Che is running on Windows
    //     che-home-location/vfs
    // Otherwise
    //     use value from property injected into constructor
    // find root directory for projects in workspaces
    if (isWindows) {
      final Path vfs = WindowsHostUtils.getCheHome().resolve("vfs");
      this.workspacesMountPoint = vfs.toString();
    } else {
      this.workspacesMountPoint = workspacesMountPoint;
    }
  }

  public String getPath(String workspaceId) throws IOException {
    if (!isWindows && singleMountPointForAllWorkspaces != null) {
      return singleMountPointForAllWorkspaces;
    }
    return doGetPathById(workspaceId);
  }

  @Deprecated
  String getPathByName(String workspaceName, String workspaceNamespace) throws IOException {
    if (!isWindows && singleMountPointForAllWorkspaces != null) {
      return singleMountPointForAllWorkspaces;
    }

    try {
      Workspace workspace = workspaceDao.get(workspaceName, workspaceNamespace);
      return getPath(workspace.getId());
    } catch (NotFoundException | ServerException e) {
      throw new IOException(e.getLocalizedMessage());
    }
  }

  private String doGetPathById(String workspaceId) throws IOException {

    String workspaceFolderPath = Paths.get(workspacesMountPoint).resolve(workspaceId).toString();
    // Since Che may be running inside of container and workspaces are mounted not in the same way
    // they will be mounted from host to workspace's containers we use Che master specific paths.
    // In cases when Che master specific path is not defined it will be equal to the workspace's
    // containers one.
    String workspaceFolderPathForCheMaster =
        Paths.get(workspacesMountPointForCheMaster).resolve(workspaceId).toString();
    ensureExist(workspaceFolderPathForCheMaster, null);
    return workspaceFolderPath;
  }

  @VisibleForTesting
  @PostConstruct
  void init() throws IOException {
    // create directories if needed
    if (singleMountPointForAllWorkspaces != null) {
      ensureExist(singleMountPointForAllWorkspaces, SINGLE_MOUNTPOINT_FOR_ALL_WORKSPACES_PROPERTY);
    } else {
      ensureExist(workspacesMountPoint, WORKSPACE_PROJECTS_MOUNT_POINT_PROPERTY);

      ensureExist(
          workspacesMountPointForCheMaster,
          CHE_MASTER_SPECIFIC_WORKSPACE_PROJECTS_MOUNT_POINT_PROPERTY);

      if (migrationOnStartup && !isWindows) {
        localProjectsMigrator.performMigration(workspacesMountPointForCheMaster);
      }
    }
  }

  private void ensureExist(String path, String prop) throws IOException {

    if (createFolders) {
      Path folder = Paths.get(path);
      if (Files.exists(folder)) {
        if (!Files.isDirectory(folder)) {
          if (prop != null) {
            throw new IOException(
                format(
                    "Workspace folder '%s' is not directory. Check %s configuration property",
                    path, prop));
          } else {
            throw new IOException(format("Workspace folder '%s' is not directory", path));
          }
        }
      } else {
        try {
          // TODO we should not create folders in this provider
          Files.createDirectories(folder);
        } catch (AccessDeniedException e) {
          throw new IOException(
              format(
                  "Workspace folder '%s' creation failed. Please check permissions of this folder. Cause: %s",
                  path, e.getLocalizedMessage()),
              e);
        } catch (IOException e) {
          throw new IOException(
              format(
                  "Workspace folder '%s' creation failed. Cause: %s",
                  path, e.getLocalizedMessage()),
              e);
        }
      }
    }
  }
}

/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine.local.node.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides path to workspace folder in CHE.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class LocalWorkspaceFolderPathProvider implements WorkspaceFolderPathProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LocalWorkspaceFolderPathProvider.class);
    /**
     * Value provide path to directory on host machine where will by all created and mount to the
     * created workspaces folder that become root of workspace inside machine.
     * Inside machine it will point to the directory described by @see che.machine.projects.internal.storage.
     * <p>
     * For example:
     * if you set "che.user.workspaces.storage" to the /home/user/che/workspaces after creating new workspace will be created new folder
     * /home/user/che/workspaces/{workspaceName} and it will be mount to the  dev-machine to "che.machine.projects.internal.storage"
     */
    private final String workspacesMountPoint;

    private final Provider<WorkspaceManager> workspaceManager;
    /**
     * this value provide path to projects on local host
     * if this value will be set all workspace will manage
     * same projects from your host
     */
    @Inject(optional = true)
    @Named("host.projects.root")
    private String hostProjectsFolder;

    @Inject
    public LocalWorkspaceFolderPathProvider(@Named("host.workspaces.root") String workspacesMountPoint,
                                            Provider<WorkspaceManager> workspaceManager) throws IOException {
        this.workspacesMountPoint = workspacesMountPoint;
        this.workspaceManager = workspaceManager;
        checkProps(workspacesMountPoint, hostProjectsFolder);
    }

    //used for testing
    protected LocalWorkspaceFolderPathProvider(String workspacesMountPoint,
                                               String projectsFolder,
                                               Provider<WorkspaceManager> workspaceManager) throws IOException {
        checkProps(workspacesMountPoint, projectsFolder);
        this.workspaceManager = workspaceManager;
        this.workspacesMountPoint = workspacesMountPoint;
        this.hostProjectsFolder = projectsFolder;
    }

    private void checkProps(String workspacesFolder, String projectsFolder) throws IOException {
        if (workspacesFolder == null && projectsFolder == null) {
            throw new IOException(
                    "Can't mount host file system. Check che.user.workspaces.storage or host.projects.root configuration property.");
        }
        if (workspacesFolder != null) {
            ensureExist(workspacesFolder, "che.user.workspaces.storage");
        }
        if (projectsFolder != null) {
            ensureExist(projectsFolder, "host.projects.root");
        }
    }


    private void ensureExist(String path, String prop) throws IOException {
        Path folder = Paths.get(path);
        if (Files.notExists(folder)) {
            Files.createDirectory(folder);
        }
        if (!Files.isDirectory(folder)) {
            throw new IOException(String.format("Projects %s is not directory. Check %s configuration property.", path, prop));
        }
    }

    @Override
    public String getPath(@Assisted("workspace") String workspaceId) throws IOException {
        if (hostProjectsFolder != null) {
            return hostProjectsFolder;
        } else {
            String wsName;
            try {
                WorkspaceManager workspaceManager = this.workspaceManager.get();
                final UsersWorkspace workspace = workspaceManager.getWorkspace(workspaceId);
                wsName = workspace.getConfig().getName();
            } catch (BadRequestException | NotFoundException | ServerException e) {
                //should never happens
                LOG.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
            Path folder = Paths.get(workspacesMountPoint).resolve(wsName);
            if (Files.notExists(folder)) {
                Files.createDirectory(folder);
            }
            return folder.toString();
        }
    }
}

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
package org.eclipse.che.ide.extension.maven.server.projecttype;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;

import java.nio.file.Paths;

/**
 * Filter for maven target folders.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class MavenTargetFilter implements VirtualFileFilter {
    private static final String TARGET_FRAGMENT = "/target/";

    private final String         workspaceId;
    private final ProjectManager projectManager;

    @Inject
    public MavenTargetFilter(ProjectManager projectManager) {
        this.projectManager = projectManager;
        this.workspaceId = System.getenv("CHE_WORKSPACE_ID");
    }

    @Override
    public boolean accept(VirtualFile file) {
        return !file.getPath().contains(TARGET_FRAGMENT) || !isMavenTargetFolder(file);
    }

    private boolean isMavenTargetFolder(VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        String rootPath = path.substring(0, path.indexOf(TARGET_FRAGMENT));
        try {
            FolderEntry rootFolder = (FolderEntry)projectManager.getProjectsRoot(workspaceId).getChild(rootPath);
            return (projectManager.isModuleFolder(rootFolder) || projectManager.isProjectFolder(rootFolder)) && isMavenModule(rootFolder);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isMavenModule(FolderEntry rootFolder) throws ProjectTypeConstraintException,
                                                                 ForbiddenException,
                                                                 ValueStorageException,
                                                                 ServerException {
        String projectPath = Paths.get(rootFolder.getPath()).subpath(0, 1).toString();
        ProjectConfigDto project = projectManager.getProjectFromWorkspace(workspaceId, projectPath);
        if (rootFolder.getName().equals(project.getName())) {
            return MavenAttributes.MAVEN_ID.equals(project.getType());
        } else {
            //TODO It's temporary solution, it isn't best place to use DTO.
            ProjectConfigDto module = project.findModule(rootFolder.getPath());
            return module != null && MavenAttributes.MAVEN_ID.equals(module.getType());
        }
    }
}

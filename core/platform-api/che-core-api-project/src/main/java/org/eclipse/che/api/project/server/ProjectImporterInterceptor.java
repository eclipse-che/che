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
package org.eclipse.che.api.project.server;

import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;


import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * The interceptor is called when project importer starts importing resources.
 *
 * @author Dmitry Shnurenko
 */
public class ProjectImporterInterceptor implements MethodInterceptor {
    @Inject
    private ProjectManager projectManager;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object result = methodInvocation.proceed();

        FolderEntry baseFolder = (FolderEntry)methodInvocation.getArguments()[0];

        String workspaceId = baseFolder.getWorkspace();
        String pathToFolder = baseFolder.getPath();
        String projectPath = pathToFolder.substring(pathToFolder.lastIndexOf("/"));
        String projectName = projectPath.substring(1);

        ProjectConfigDto projectFromWorkspace = projectManager.getProjectFromWorkspace(workspaceId, projectPath);

        if (projectFromWorkspace != null) {
            return result;
        }

        SourceStorage sourceStorage = (SourceStorage)methodInvocation.getArguments()[1];

        SourceStorageDto sourceStorageDto = newDto(SourceStorageDto.class).withLocation(sourceStorage.getLocation())
                                                                          .withParameters(sourceStorage.getParameters())
                                                                          .withType(sourceStorage.getType());

        ProjectConfigDto blankProject = newDto(ProjectConfigDto.class).withName(projectName)
                                                                      .withPath(projectPath)
                                                                      .withType("blank")
                                                                      .withContentRoot("")
                                                                      .withDescription("")
                                                                      .withSource(sourceStorageDto);

        projectManager.convertFolderToProject(workspaceId, projectPath, blankProject);

        return result;
    }
}

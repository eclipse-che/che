/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server.handlers;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author gazarenkov
 */
@Singleton
public class ProjectHandlerRegistry {

    private final Map<String, CreateProjectHandler>     createProjectHandlers;
    private final Map<String, PostImportProjectHandler> postImportProjectHandlers;
    private final Map<String, GetItemHandler>           getItemHandlers;
    private final Map<String, ProjectInitHandler>       projectInitHandlers;

    @Inject
    public ProjectHandlerRegistry(Set<ProjectHandler> projectHandlers) {
        createProjectHandlers = new HashMap<>();
        postImportProjectHandlers = new HashMap<>();
        getItemHandlers = new HashMap<>();
        projectInitHandlers = new HashMap<>();
        projectHandlers.forEach(this::register);
    }

    public void register(@NotNull ProjectHandler handler) {
        if (handler instanceof CreateProjectHandler) {
            createProjectHandlers.put(handler.getProjectType(), (CreateProjectHandler)handler);
        } else if (handler instanceof GetItemHandler) {
            getItemHandlers.put(handler.getProjectType(), (GetItemHandler)handler);
        } else if (handler instanceof PostImportProjectHandler) {
            postImportProjectHandlers.put(handler.getProjectType(), (PostImportProjectHandler)handler);
        } else if (handler instanceof ProjectInitHandler) {
            projectInitHandlers.put(handler.getProjectType(), (ProjectInitHandler)handler);
        }
    }

    @Nullable
    public CreateProjectHandler getCreateProjectHandler(@NotNull String projectType) {
        return createProjectHandlers.get(projectType);
    }

    @Nullable
    public GetItemHandler getGetItemHandler(@NotNull String projectType) {
        return getItemHandlers.get(projectType);
    }

    @Nullable
    public PostImportProjectHandler getPostImportProjectHandler(@NotNull String projectType) {
        return postImportProjectHandlers.get(projectType);
    }

    @Nullable
    public ProjectInitHandler getProjectInitHandler(@NotNull String projectType) {
        return projectInitHandlers.get(projectType);
    }

}

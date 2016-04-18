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
package org.eclipse.che.ide.projectimport.wizard;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.Constants;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.wizard.Wizard.CompleteCallback;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

/**
 * The class contains business logic which allows resolve project type and call updater.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectResolver {

    private final DtoUnmarshallerFactory        dtoUnmarshallerFactory;
    private final ProjectServiceClient          projectService;
    private final ProjectTypeRegistry           projectTypeRegistry;
    private final AppContext                    appContext;
    private final ProjectNotificationSubscriber projectNotificationSubscriber;
    private final ProjectUpdater                projectUpdater;

    @Inject
    public ProjectResolver(DtoUnmarshallerFactory dtoUnmarshallerFactory,
                           ProjectServiceClient projectService,
                           ProjectTypeRegistry projectTypeRegistry,
                           AppContext appContext,
                           ProjectNotificationSubscriber projectNotificationSubscriber,
                           ProjectUpdater projectUpdater) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.projectService = projectService;
        this.projectTypeRegistry = projectTypeRegistry;
        this.appContext = appContext;
        this.projectNotificationSubscriber = projectNotificationSubscriber;
        this.projectUpdater = projectUpdater;
    }

    /**
     * The method defines project type of passed project and take resolution should project be configured or not.
     *
     * @param callback
     *         callback which is necessary to inform that resolving completed
     * @param projectConfig
     *         project which will be resolved
     */
    public void resolveProject(@NotNull final CompleteCallback callback, @NotNull final ProjectConfigDto projectConfig) {
        final String projectName = projectConfig.getName();
        final String projectPath = projectConfig.getPath();

        String path = projectPath == null ? projectName : projectPath;
        Unmarshallable<List<SourceEstimation>> unmarshaller = dtoUnmarshallerFactory.newListUnmarshaller(SourceEstimation.class);
        projectService.resolveSources(appContext.getDevMachine(), path, new AsyncRequestCallback<List<SourceEstimation>>(unmarshaller) {

            Function<SourceEstimation, ProjectTypeDto> estimateToType = new Function<SourceEstimation, ProjectTypeDto>() {
                @Nullable
                @Override
                public ProjectTypeDto apply(@Nullable SourceEstimation input) {
                    if (input != null) {
                        return projectTypeRegistry.getProjectType(input.getType());
                    }

                    return null;
                }
            };

            Predicate<ProjectTypeDto> isPrimaryable = new Predicate<ProjectTypeDto>() {
                @Override
                public boolean apply(@Nullable ProjectTypeDto input) {
                    return input != null && input.isPrimaryable();

                }
            };

            @Override
            protected void onSuccess(List<SourceEstimation> result) {
                Iterable<ProjectTypeDto> types = filter(transform(result, estimateToType), isPrimaryable);

                if (size(types) == 1) {
                    ProjectTypeDto typeDto = getFirst(types, null);

                    if (typeDto != null) {
                        projectConfig.withType(typeDto.getId());
                    }
                }

                boolean configRequire = false;

                if (isNullOrEmpty(projectConfig.getType())) {
                    projectConfig.withType(Constants.BLANK_ID);
                    configRequire = true;
                }

                projectUpdater.updateProject(callback, projectConfig, configRequire);
            }

            @Override
            protected void onFailure(Throwable exception) {
                projectNotificationSubscriber.onFailure(exception.getMessage());
                callback.onFailure(new Exception(exception.getMessage()));
            }
        });
    }
}

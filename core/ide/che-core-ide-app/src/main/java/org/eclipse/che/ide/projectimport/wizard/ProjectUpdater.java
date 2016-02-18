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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ConfigureProjectEvent;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.wizard.Wizard.CompleteCallback;
import org.eclipse.che.ide.projectimport.ErrorMessageUtils;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;

import javax.validation.constraints.NotNull;

/**
 * The class contains business logic which allows update project.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectUpdater {

    private final DtoUnmarshallerFactory        dtoUnmarshallerFactory;
    private final ProjectServiceClient          projectService;
    private final ProjectNotificationSubscriber projectNotificationSubscriber;
    private final EventBus                      eventBus;
    private final String                        workspaceId;

    @Inject
    public ProjectUpdater(DtoUnmarshallerFactory dtoUnmarshallerFactory,
                          ProjectServiceClient projectService,
                          ProjectNotificationSubscriber projectNotificationSubscriber,
                          EventBus eventBus,
                          AppContext appContext) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.projectService = projectService;
        this.projectNotificationSubscriber = projectNotificationSubscriber;
        this.eventBus = eventBus;
        this.workspaceId = appContext.getWorkspaceId();
    }

    /**
     * The method updates project and take resolution should project be configured or not.
     *
     * @param callback
     *         callback which is necessary to inform that resolving completed
     * @param projectConfig
     *         project which will be resolved
     * @param isConfigurationRequired
     *         special flag which defines will project be configured or not.<code>true</code> project will be configured,
     *         <code>false</code> project will not be configured
     */
    public void updateProject(@NotNull final CompleteCallback callback,
                              @NotNull ProjectConfigDto projectConfig,
                              final boolean isConfigurationRequired) {
        final String projectPath = projectConfig.getPath();

        Unmarshallable<ProjectConfigDto> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ProjectConfigDto.class);
        projectService.updateProject(workspaceId,
                                     projectPath == null ? '/' + projectConfig.getName() : projectPath,
                                     projectConfig,
                                     new AsyncRequestCallback<ProjectConfigDto>(unmarshaller) {
                                         @Override
                                         protected void onSuccess(final ProjectConfigDto result) {
                                             if (result.getProblems().isEmpty() && !isConfigurationRequired) {
                                                 eventBus.fireEvent(new ProjectUpdatedEvent(projectPath, result));
                                                 projectNotificationSubscriber.onSuccess();
                                                 callback.onCompleted();
                                                 return;
                                             }
                                             eventBus.fireEvent(new CreateProjectEvent(result));
                                             eventBus.fireEvent(new ConfigureProjectEvent(result));
                                             projectNotificationSubscriber.onSuccess();
                                             callback.onCompleted();
                                         }

                                         @Override
                                         protected void onFailure(Throwable exception) {
                                             projectNotificationSubscriber.onFailure(exception.getMessage());
                                             callback.onFailure(new Exception(exception.getMessage()));
                                         }
                                     });
    }
}

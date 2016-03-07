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
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ConfigureProjectEvent;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.wizard.Wizard.CompleteCallback;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The class contains business logic which allows update project.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectUpdater {

    private final ProjectServiceClient          projectService;
    private final ProjectNotificationSubscriber projectNotificationSubscriber;
    private final EventBus                      eventBus;
    private final AppContext                    appContext;
    private final String                        workspaceId;

    @Inject
    public ProjectUpdater(ProjectServiceClient projectService,
                          ProjectNotificationSubscriber projectNotificationSubscriber,
                          EventBus eventBus,
                          AppContext appContext) {
        this.projectService = projectService;
        this.projectNotificationSubscriber = projectNotificationSubscriber;
        this.eventBus = eventBus;
        this.appContext = appContext;
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

        projectService.updateProject(workspaceId,
                                     projectPath == null ? '/' + projectConfig.getName() : projectPath,
                                     projectConfig)
                      .then(new Operation<ProjectConfigDto>() {
                          @Override
                          public void apply(final ProjectConfigDto updated) throws OperationException {

                              //dirty hack. here we have to load from server new list of projects, because after project configuring
                              //they may appear, so we need to have actual projects configuration state, need to find better way to
                              //process it
                              projectService.getProjects(workspaceId, true).then(new Operation<List<ProjectConfigDto>>() {
                                  @Override
                                  public void apply(List<ProjectConfigDto> projects) throws OperationException {

                                      appContext.getWorkspace().getConfig().withProjects(projects);

                                      if (updated.getProblems().isEmpty() && !isConfigurationRequired) {
                                          eventBus.fireEvent(new ProjectUpdatedEvent(projectPath, updated));
                                          projectNotificationSubscriber.onSuccess();
                                          callback.onCompleted();
                                          return;
                                      }
                                      eventBus.fireEvent(new CreateProjectEvent(updated));
                                      eventBus.fireEvent(new ConfigureProjectEvent(updated));
                                      projectNotificationSubscriber.onSuccess();
                                      callback.onCompleted();
                                  }
                              }).catchError(new Operation<PromiseError>() {
                                  @Override
                                  public void apply(PromiseError arg) throws OperationException {
                                      projectNotificationSubscriber.onFailure(arg.getMessage());
                                      callback.onFailure(new Exception(arg.getMessage()));
                                  }
                              });
                          }
                      })
                      .catchError(new Operation<PromiseError>() {
                          @Override
                          public void apply(PromiseError arg) throws OperationException {
                              projectNotificationSubscriber.onFailure(arg.getMessage());
                              callback.onFailure(new Exception(arg.getMessage()));
                          }
                      });
    }
}

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
package org.eclipse.che.ide.ext.git.client.delete;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Delete repository command handler, performs deleting Git repository.
 *
 * @author Ann Zhuleva
 */
@Singleton
public class DeleteRepositoryPresenter {
    public static final String DELETE_REPO_COMMAND_NAME = "Git delete repository";

    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final ConsolesPanelPresenter  consolesPanelPresenter;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;
    private final ProjectServiceClient    projectService;
    private final DtoUnmarshallerFactory  dtoUnmarshaller;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final EventBus                eventBus;

    /**
     * Create presenter.
     *
     * @param service
     * @param constant
     * @param appContext
     * @param notificationManager
     */
    @Inject
    public DeleteRepositoryPresenter(GitServiceClient service,
                                     GitLocalizationConstant constant,
                                     GitOutputConsoleFactory gitOutputConsoleFactory,
                                     ConsolesPanelPresenter consolesPanelPresenter,
                                     AppContext appContext,
                                     NotificationManager notificationManager,
                                     ProjectServiceClient projectServiceClient,
                                     DtoUnmarshallerFactory dtoUnmarshaller,
                                     EventBus eventBus) {
        this.service = service;
        this.constant = constant;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.projectService = projectServiceClient;
        this.dtoUnmarshaller = dtoUnmarshaller;
        this.eventBus = eventBus;
    }

    /** Delete Git repository. */
    public void deleteRepository() {
        final CurrentProject project = appContext.getCurrentProject();
        final GitOutputConsole console = gitOutputConsoleFactory.create(DELETE_REPO_COMMAND_NAME);
        service.deleteRepository(appContext.getDevMachine(), project.getRootProject(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                console.print(constant.deleteGitRepositorySuccess());
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.deleteGitRepositorySuccess(), project.getRootProject());
                getRootProject(project.getRootProject());
            }

            @Override
            protected void onFailure(Throwable exception) {
                console.printError(exception.getMessage());
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.failedToDeleteRepository(), FAIL, FLOAT_MODE, project.getRootProject());
            }
        });
    }

    private void getRootProject(final ProjectConfigDto config) {
        projectService.getProject(appContext.getDevMachine(),
                                  config.getPath(),
                                  new AsyncRequestCallback<ProjectConfigDto>(dtoUnmarshaller.newUnmarshaller(ProjectConfigDto.class)) {
                                      @Override
                                      protected void onSuccess(ProjectConfigDto projectConfig) {
                                          eventBus.fireEvent(new ProjectUpdatedEvent(config.getPath(), projectConfig));
                                      }

                                      @Override
                                      protected void onFailure(Throwable exception) {

                                      }
                                  });
    }
}

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
package org.eclipse.che.ide.ext.git.client.init;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitRepositoryInitializer;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for Git command Init Repository.
 *
 * @author Ann Zhuleva
 * @author Roman Nikitenko
 */
@Singleton
public class InitRepositoryPresenter {
    public static final String INIT_COMMAND_NAME = "Git init";

    private final GitRepositoryInitializer gitRepositoryInitializer;
    private final ProjectServiceClient     projectService;
    private final DtoUnmarshallerFactory   dtoUnmarshaller;
    private final EventBus                 eventBus;
    private final GitOutputConsoleFactory  gitOutputConsoleFactory;
    private final ConsolesPanelPresenter   consolesPanelPresenter;
    private final AppContext               appContext;
    private final GitLocalizationConstant  constant;
    private final NotificationManager      notificationManager;

    @Inject
    public InitRepositoryPresenter(AppContext appContext,
                                   GitLocalizationConstant constant,
                                   NotificationManager notificationManager,
                                   GitRepositoryInitializer gitRepositoryInitializer,
                                   ProjectServiceClient projectServiceClient,
                                   DtoUnmarshallerFactory dtoUnmarshaller,
                                   EventBus eventBus,
                                   GitOutputConsoleFactory gitOutputConsoleFactory,
                                   ConsolesPanelPresenter consolesPanelPresenter) {
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.gitRepositoryInitializer = gitRepositoryInitializer;
        this.projectService = projectServiceClient;
        this.dtoUnmarshaller = dtoUnmarshaller;
        this.eventBus = eventBus;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
    }

    public void initRepository() {
        final CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject == null || currentProject.getRootProject() == null) {
            Log.error(getClass(), "Open the project before initialize repository");
            return;
        }
        final GitOutputConsole console = gitOutputConsoleFactory.create(INIT_COMMAND_NAME);
        gitRepositoryInitializer.initGitRepository(currentProject.getRootProject(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                handleError(caught, console);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
            }

            @Override
            public void onSuccess(Void result) {
                console.print(constant.initSuccess());
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                notificationManager.notify(constant.initSuccess(), currentProject.getRootProject());
                getRootProject(currentProject.getRootProject());
            }
        });
    }

    /**
     * Handler some action whether some exception happened.
     *
     * @param e
     *         exception what happened
     */
    private void handleError(@NotNull Throwable e, GitOutputConsole console) {
        String errorMessage = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : constant.initFailed();
        console.printError(errorMessage);
        notificationManager.notify(constant.initFailed(), FAIL, true, appContext.getCurrentProject().getRootProject());
    }

    private void getRootProject(final ProjectConfigDto projectConfig) {
        projectService.getProject(appContext.getWorkspace().getId(),
                                  projectConfig.getPath(),
                                  new AsyncRequestCallback<ProjectConfigDto>(dtoUnmarshaller.newUnmarshaller(ProjectConfigDto.class)) {
                                      @Override
                                      protected void onSuccess(ProjectConfigDto result) {
                                          eventBus.fireEvent(new ProjectUpdatedEvent(projectConfig.getPath(), result));
                                      }

                                      @Override
                                      protected void onFailure(Throwable exception) {

                                      }
                                  });
    }
}

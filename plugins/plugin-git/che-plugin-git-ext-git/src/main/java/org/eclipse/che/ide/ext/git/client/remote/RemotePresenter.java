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
package org.eclipse.che.ide.ext.git.client.remote;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.ext.git.client.remote.add.AddRemoteRepositoryPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for working with remote repository list (view, add and delete).
 *
 * @author Ann Zhuleva
 */
@Singleton
public class RemotePresenter implements RemoteView.ActionDelegate {
    public static final String REMOTE_REPO_COMMAND_NAME = "Git list of remotes";

    private final EventBus                eventBus;
    private final ProjectServiceClient    projectService;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ConsolesPanelPresenter  consolesPanelPresenter;

    private final RemoteView                   view;
    private final GitServiceClient             service;
    private final AppContext                   appContext;
    private final GitLocalizationConstant      constant;
    private final AddRemoteRepositoryPresenter addRemoteRepositoryPresenter;
    private final NotificationManager          notificationManager;

    private Remote           selectedRemote;
    private ProjectConfigDto project;
    private String           workspaceId;

    @Inject
    public RemotePresenter(RemoteView view,
                           GitServiceClient service,
                           AppContext appContext,
                           EventBus eventBus,
                           GitLocalizationConstant constant,
                           ProjectServiceClient projectService,
                           AddRemoteRepositoryPresenter addRemoteRepositoryPresenter,
                           NotificationManager notificationManager,
                           DtoUnmarshallerFactory dtoUnmarshallerFactory,
                           GitOutputConsoleFactory gitOutputConsoleFactory,
                           ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.eventBus = eventBus;
        this.projectService = projectService;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.addRemoteRepositoryPresenter = addRemoteRepositoryPresenter;
        this.notificationManager = notificationManager;
        this.workspaceId = appContext.getWorkspaceId();
    }

    /**
     * Show dialog.
     */
    public void showDialog() {
        project = appContext.getCurrentProject().getRootProject();
        getRemotes();
    }

    /**
     * Get the list of remote repositories for local one. If remote repositories are found,
     * then get the list of branches (remote and local).
     */
    private void getRemotes() {
        service.remoteList(workspaceId, project, null, true,
                           new AsyncRequestCallback<List<Remote>>(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class)) {
                               @Override
                               protected void onSuccess(List<Remote> result) {
                                   view.setEnableDeleteButton(selectedRemote != null);
                                   view.setRemotes(result);
                                   if (!view.isShown()) {
                                       view.showDialog();
                                   }
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   String errorMessage =
                                           exception.getMessage() != null ? exception.getMessage() : constant.remoteListFailed();
                                   handleError(errorMessage);
                               }
                           });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCloseClicked() {
        view.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAddClicked() {
        addRemoteRepositoryPresenter.showDialog(new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                getRemotes();
                refreshProject();
            }

            @Override
            public void onFailure(Throwable caught) {
                String errorMessage = caught.getMessage() != null ? caught.getMessage() : constant.remoteAddFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(REMOTE_REPO_COMMAND_NAME);
                console.printError(errorMessage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                notificationManager.notify(constant.remoteAddFailed(), FAIL, true, project);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeleteClicked() {
        if (selectedRemote == null) {
            handleError(constant.selectRemoteRepositoryFail());
            return;
        }

        final String name = selectedRemote.getName();
        service.remoteDelete(workspaceId, project, name, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(String result) {
                getRemotes();
                refreshProject();
            }

            @Override
            protected void onFailure(Throwable exception) {
                String errorMessage = exception.getMessage() != null ? exception.getMessage() : constant.remoteDeleteFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(REMOTE_REPO_COMMAND_NAME);
                console.printError(errorMessage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                notificationManager.notify(constant.remoteDeleteFailed(), FAIL, true, project);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRemoteSelected(@NotNull Remote remote) {
        selectedRemote = remote;
        view.setEnableDeleteButton(selectedRemote != null);
    }

    private void handleError(@NotNull String errorMessage) {
        GitOutputConsole console = gitOutputConsoleFactory.create(REMOTE_REPO_COMMAND_NAME);
        console.printError(errorMessage);
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
        notificationManager.notify(errorMessage, project);
    }

    private void refreshProject() {
        projectService.getProject(workspaceId, project.getName(), new AsyncRequestCallback<ProjectConfigDto>(
                dtoUnmarshallerFactory.newUnmarshaller(ProjectConfigDto.class)) {
            @Override
            protected void onSuccess(ProjectConfigDto result) {
                eventBus.fireEvent(new ProjectUpdatedEvent(project.getPath(), result));
            }

            @Override
            protected void onFailure(Throwable exception) {
                notificationManager.notify(exception.getLocalizedMessage(), FAIL, true, project);
            }
        });
    }
}

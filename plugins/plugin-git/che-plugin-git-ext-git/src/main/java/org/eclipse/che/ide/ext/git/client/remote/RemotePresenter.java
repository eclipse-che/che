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

import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.ext.git.client.remote.add.AddRemoteRepositoryPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for working with remote repository list (view, add and delete).
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class RemotePresenter implements RemoteView.ActionDelegate {
    public static final String REMOTE_REPO_COMMAND_NAME = "Git list of remotes";

    private final GitOutputConsoleFactory      gitOutputConsoleFactory;
    private final ProcessesPanelPresenter      consolesPanelPresenter;
    private final RemoteView                   view;
    private final GitServiceClient             service;
    private final AppContext                   appContext;
    private final GitLocalizationConstant      constant;
    private final AddRemoteRepositoryPresenter addRemoteRepositoryPresenter;
    private final NotificationManager          notificationManager;

    private Remote  selectedRemote;
    private Project project;

    @Inject
    public RemotePresenter(RemoteView view,
                           GitServiceClient service,
                           AppContext appContext,
                           GitLocalizationConstant constant,
                           AddRemoteRepositoryPresenter addRemoteRepositoryPresenter,
                           NotificationManager notificationManager,
                           GitOutputConsoleFactory gitOutputConsoleFactory,
                           ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.addRemoteRepositoryPresenter = addRemoteRepositoryPresenter;
        this.notificationManager = notificationManager;
    }

    /**
     * Show dialog.
     */
    public void showDialog(Project project) {
        this.project = project;
        getRemotes();
    }

    /**
     * Get the list of remote repositories for local one. If remote repositories are found,
     * then get the list of branches (remote and local).
     */
    private void getRemotes() {
        service.remoteList(appContext.getDevMachine(), project.getLocation(), null, true).then(new Operation<List<Remote>>() {
            @Override
            public void apply(List<Remote> remotes) throws OperationException {
                view.setEnableDeleteButton(selectedRemote != null);
                view.setRemotes(remotes);
                if (!view.isShown()) {
                    view.showDialog();
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                String errorMessage = error.getMessage() != null ? error.getMessage() : constant.remoteListFailed();
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

                project.synchronize();
            }

            @Override
            public void onFailure(Throwable caught) {
                String errorMessage = caught.getMessage() != null ? caught.getMessage() : constant.remoteAddFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(REMOTE_REPO_COMMAND_NAME);
                console.printError(errorMessage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.remoteAddFailed(), FAIL, FLOAT_MODE);
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

        service.remoteDelete(appContext.getDevMachine(), project.getLocation(), selectedRemote.getName()).then(new Operation<Void>() {
            @Override
            public void apply(Void ignored) throws OperationException {
                getRemotes();

                project.synchronize();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                String errorMessage = error.getMessage() != null ? error.getMessage() : constant.remoteDeleteFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(REMOTE_REPO_COMMAND_NAME);
                console.printError(errorMessage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.remoteDeleteFailed(), FAIL, FLOAT_MODE);
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
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
        notificationManager.notify(errorMessage);
    }
}

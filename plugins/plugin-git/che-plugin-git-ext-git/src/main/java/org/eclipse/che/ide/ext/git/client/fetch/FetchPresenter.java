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
package org.eclipse.che.ide.ext.git.client.fetch;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_REMOTE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Presenter for fetching changes from remote repository.
 *
 * @author Ann Zhuleva
 */
@Singleton
public class FetchPresenter implements FetchView.ActionDelegate {
    public static final String FETCH_COMMAND_NAME = "Git fetch";

    private final DtoFactory              dtoFactory;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final NotificationManager     notificationManager;
    private final BranchSearcher          branchSearcher;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ConsolesPanelPresenter  consolesPanelPresenter;
    private final FetchView               view;
    private final GitServiceClient        service;
    private final AppContext              appContext;
    private final GitLocalizationConstant constant;
    private final String                  workspaceId;

    private CurrentProject project;

    @Inject
    public FetchPresenter(DtoFactory dtoFactory,
                          FetchView view,
                          GitServiceClient service,
                          AppContext appContext,
                          GitLocalizationConstant constant,
                          NotificationManager notificationManager,
                          DtoUnmarshallerFactory dtoUnmarshallerFactory,
                          BranchSearcher branchSearcher,
                          GitOutputConsoleFactory gitOutputConsoleFactory,
                          ConsolesPanelPresenter consolesPanelPresenter) {
        this.dtoFactory = dtoFactory;
        this.view = view;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.branchSearcher = branchSearcher;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.workspaceId = appContext.getWorkspaceId();
    }

    /** Show dialog. */
    public void showDialog() {
        project = appContext.getCurrentProject();
        view.setRemoveDeleteRefs(false);
        view.setFetchAllBranches(true);
        updateRemotes();
    }

    /**
     * Update the list of remote repositories for local one. If remote repositories are found, then update the list of branches (remote and
     * local).
     */
    private void updateRemotes() {
        service.remoteList(workspaceId, project.getRootProject(), null, true,
                           new AsyncRequestCallback<List<Remote>>(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class)) {
                               @Override
                               protected void onSuccess(List<Remote> result) {
                                   view.setRepositories(result);
                                   updateBranches(LIST_REMOTE);
                                   view.setEnableFetchButton(!result.isEmpty());
                                   view.showDialog();
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   GitOutputConsole console = gitOutputConsoleFactory.create(FETCH_COMMAND_NAME);
                                   console.printError(constant.remoteListFailed());
                                   consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                   notificationManager.notify(constant.remoteListFailed(), FAIL, true, project.getRootProject());
                                   view.setEnableFetchButton(false);
                               }
                           });
    }

    /**
     * Update the list of branches.
     *
     * @param remoteMode
     *         is a remote mode
     */
    private void updateBranches(@NotNull final String remoteMode) {
        service.branchList(workspaceId, project.getRootProject(), remoteMode,
                           new AsyncRequestCallback<List<Branch>>(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class)) {
                               @Override
                               protected void onSuccess(List<Branch> result) {
                                   if (LIST_REMOTE.equals(remoteMode)) {
                                       view.setRemoteBranches(branchSearcher.getRemoteBranchesToDisplay(view.getRepositoryName(), result));
                                       updateBranches(LIST_LOCAL);
                                   } else {
                                       view.setLocalBranches(branchSearcher.getLocalBranchesToDisplay(result));
                                       for (Branch branch : result) {
                                           if (branch.isActive()) {
                                               view.selectRemoteBranch(branch.getDisplayName());
                                               break;
                                           }
                                       }
                                   }
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   String errorMessage =
                                           exception.getMessage() != null ? exception.getMessage() : constant.branchesListFailed();
                                   GitOutputConsole console = gitOutputConsoleFactory.create(FETCH_COMMAND_NAME);
                                   console.printError(errorMessage);
                                   consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                   notificationManager.notify(constant.branchesListFailed(), FAIL, true, project.getRootProject());
                                   view.setEnableFetchButton(false);
                               }
                           });
    }

    /** {@inheritDoc} */
    @Override
    public void onFetchClicked() {
        final String remoteUrl = view.getRepositoryUrl();
        String remoteName = view.getRepositoryName();
        boolean removeDeletedRefs = view.isRemoveDeletedRefs();

        final StatusNotification notification =
                notificationManager.notify(constant.fetchProcess(), PROGRESS, true, project.getRootProject());
        final GitOutputConsole console = gitOutputConsoleFactory.create(FETCH_COMMAND_NAME);
        try {
            service.fetch(workspaceId, project.getRootProject(), remoteName, getRefs(), removeDeletedRefs,
                          new RequestCallback<String>() {
                              @Override
                              protected void onSuccess(String result) {
                                  console.print(constant.fetchSuccess(remoteUrl));
                                  consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                  notification.setStatus(SUCCESS);
                                  notification.setTitle(constant.fetchSuccess(remoteUrl));
                              }

                              @Override
                              protected void onFailure(Throwable exception) {
                                  handleError(exception, remoteUrl, notification, console);
                                  consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                              }
                          }
                         );
        } catch (WebSocketException e) {
            handleError(e, remoteUrl, notification, console);
            consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
        }
        view.close();
    }

    /** @return list of refs to fetch */
    @NotNull
    private List<String> getRefs() {
        if (view.isFetchAllBranches()) {
            return new ArrayList<>();
        }

        String localBranch = view.getLocalBranch();
        String remoteBranch = view.getRemoteBranch();
        String remoteName = view.getRepositoryName();
        String refs = localBranch.isEmpty() ? remoteBranch
                                            : "refs/heads/" + localBranch + ":" + "refs/remotes/" + remoteName + "/" + remoteBranch;
        return new ArrayList<>(Arrays.asList(refs));
    }

    /**
     * Handler some action whether some exception happened.
     *
     * @param throwable
     *         exception what happened
     */
    private void handleError(@NotNull Throwable throwable, @NotNull String remoteUrl, StatusNotification notification,
                             GitOutputConsole console) {
        String errorMessage = throwable.getMessage();
        notification.setStatus(FAIL);
        if (errorMessage == null) {
            console.printError(constant.fetchFail(remoteUrl));
            notification.setTitle(constant.fetchFail(remoteUrl));
            return;
        }

        try {
            errorMessage = dtoFactory.createDtoFromJson(errorMessage, ServiceError.class).getMessage();
            if (errorMessage.equals("Unable get private ssh key")) {
                console.printError(constant.messagesUnableGetSshKey());
                notification.setTitle(constant.messagesUnableGetSshKey());
                return;
            }
            console.printError(errorMessage);
            notification.setTitle(errorMessage);
        } catch (Exception e) {
            console.printError(errorMessage);
            notification.setTitle(errorMessage);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        boolean isFetchAll = view.isFetchAllBranches();
        view.setEnableLocalBranchField(!isFetchAll);
        view.setEnableRemoteBranchField(!isFetchAll);
    }

    /** {@inheritDoc} */
    @Override
    public void onRemoteBranchChanged() {
        view.selectLocalBranch(view.getRemoteBranch());
    }

    /** {@inheritDoc} */
    @Override
    public void onRemoteRepositoryChanged() {
        updateBranches(LIST_REMOTE);
    }
}

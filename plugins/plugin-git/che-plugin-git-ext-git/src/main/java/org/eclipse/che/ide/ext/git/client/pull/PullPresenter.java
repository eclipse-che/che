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
package org.eclipse.che.ide.ext.git.client.pull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_REMOTE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.ext.git.client.compare.branchList.BranchListPresenter.BRANCH_LIST_COMMAND_NAME;
import static org.eclipse.che.ide.ext.git.client.remote.RemotePresenter.REMOTE_REPO_COMMAND_NAME;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter pulling changes from remote repository.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class PullPresenter implements PullView.ActionDelegate {
    public static final String PULL_COMMAND_NAME = "Git pull";

    private static final String GREEN_COLOR = "lightgreen";

    private final PullView                view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;
    private final DialogFactory           dialogFactory;
    private final BranchSearcher          branchSearcher;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;

    private Project project;

    @Inject
    public PullPresenter(PullView view,
                         GitServiceClient service,
                         AppContext appContext,
                         GitLocalizationConstant constant,
                         NotificationManager notificationManager,
                         DialogFactory dialogFactory,
                         BranchSearcher branchSearcher,
                         GitOutputConsoleFactory gitOutputConsoleFactory,
                         ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.dialogFactory = dialogFactory;
        this.branchSearcher = branchSearcher;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
    }

    public void showDialog(Project project) {
        this.project = project;

        view.setEnablePullButton(false);

        service.remoteList(appContext.getDevMachine(), project.getLocation(), null, true)
               .then(new Operation<List<Remote>>() {
                   @Override
                   public void apply(List<Remote> remotes) throws OperationException {
                       updateBranches(LIST_REMOTE);
                       view.setRepositories(remotes);
                       view.setEnablePullButton(!remotes.isEmpty());
                       view.showDialog();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       handleError(error.getCause(), REMOTE_REPO_COMMAND_NAME);
                       view.setEnablePullButton(false);
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

        service.branchList(appContext.getDevMachine(), project.getLocation(), remoteMode).then(new Operation<List<Branch>>() {
            @Override
            public void apply(List<Branch> branches) throws OperationException {
                if (LIST_REMOTE.equals(remoteMode)) {
                    view.setRemoteBranches(branchSearcher.getRemoteBranchesToDisplay(view.getRepositoryName(), branches));
                    updateBranches(LIST_LOCAL);
                } else {
                    view.setLocalBranches(branchSearcher.getLocalBranchesToDisplay(branches));
                    for (Branch branch : branches) {
                        if (branch.isActive()) {
                            view.selectRemoteBranch(branch.getDisplayName());
                            break;
                        }
                    }
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                handleError(error.getCause(), BRANCH_LIST_COMMAND_NAME);
                view.setEnablePullButton(false);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onPullClicked() {
        view.close();

        final StatusNotification notification =
                notificationManager.notify(constant.pullProcess(), PROGRESS, FLOAT_MODE);

        service.pull(appContext.getDevMachine(), project.getLocation(), getRefs(), view.getRepositoryName()).then(new Operation<PullResponse>() {
            @Override
            public void apply(PullResponse response) throws OperationException {
                GitOutputConsole console = gitOutputConsoleFactory.create(PULL_COMMAND_NAME);
                console.print(response.getCommandOutput(), GREEN_COLOR);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notification.setStatus(SUCCESS);
                if (response.getCommandOutput().contains("Already up-to-date")) {
                    notification.setTitle(constant.pullUpToDate());
                } else {
                    project.synchronize();
                    notification.setTitle(constant.pullSuccess(view.getRepositoryUrl()));
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notification.setStatus(FAIL);
                if (getErrorCode(error.getCause()) == ErrorCodes.MERGE_CONFLICT) {
                    project.synchronize();
                }
                handleError(error.getCause(), PULL_COMMAND_NAME);
            }
        });
    }

    /** @return list of refs to fetch */
    @NotNull
    private String getRefs() {
        String remoteName = view.getRepositoryName();
        String localBranch = view.getLocalBranch();
        String remoteBranch = view.getRemoteBranch();

        return localBranch.isEmpty() ? remoteBranch
                                     : "refs/heads/" + localBranch + ":" + "refs/remotes/" + remoteName + "/" + remoteBranch;
    }

    /**
     * Handler some action whether some exception happened.
     *
     * @param exception
     *         exception that happened
     * @param commandName
     *         name of the command
     */
    private void handleError(@NotNull Throwable exception, @NotNull String commandName) {
        int errorCode = getErrorCode(exception);
        if (errorCode == ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED) {
            dialogFactory.createMessageDialog(constant.pullTitle(), constant.committerIdentityInfoEmpty(), null).show();
            return;
        } else if (errorCode == ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY) {
            dialogFactory.createMessageDialog(constant.pullTitle(), constant.messagesUnableGetSshKey(), null).show();
            return;
        }

        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            switch (commandName) {
                case REMOTE_REPO_COMMAND_NAME:
                    errorMessage = constant.remoteListFailed();
                    break;
                case BRANCH_LIST_COMMAND_NAME:
                    errorMessage = constant.branchesListFailed();
                    break;
                case PULL_COMMAND_NAME:
                    errorMessage = constant.pullFail(view.getRepositoryUrl());
                    break;
            }
        }

        GitOutputConsole console = gitOutputConsoleFactory.create(commandName);
        console.printError(errorMessage);
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
        notificationManager.notify(errorMessage, FAIL, FLOAT_MODE);
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
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

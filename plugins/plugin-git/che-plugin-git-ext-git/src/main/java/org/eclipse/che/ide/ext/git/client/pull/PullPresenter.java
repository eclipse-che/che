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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

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
 */
@Singleton
public class PullPresenter implements PullView.ActionDelegate {
    public static final String PULL_COMMAND_NAME = "Git pull";

    private static final String GREEN_COLOR = "lightgreen";

    private final PullView                 view;
    private final GitServiceClient         gitServiceClient;
    private final EventBus                 eventBus;
    private final GitLocalizationConstant  constant;
    private final EditorAgent              editorAgent;
    private final AppContext               appContext;
    private final NotificationManager      notificationManager;
    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private final DialogFactory            dialogFactory;
    private final BranchSearcher           branchSearcher;
    private final ProjectExplorerPresenter projectExplorer;
    private final GitOutputConsoleFactory  gitOutputConsoleFactory;
    private final ConsolesPanelPresenter   consolesPanelPresenter;
    private       CurrentProject           project;


    @Inject
    public PullPresenter(PullView view,
                         EditorAgent editorAgent,
                         GitServiceClient gitServiceClient,
                         EventBus eventBus,
                         AppContext appContext,
                         GitLocalizationConstant constant,
                         NotificationManager notificationManager,
                         DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         DialogFactory dialogFactory,
                         BranchSearcher branchSearcher,
                         ProjectExplorerPresenter projectExplorer,
                         GitOutputConsoleFactory gitOutputConsoleFactory,
                         ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.dialogFactory = dialogFactory;
        this.branchSearcher = branchSearcher;
        this.projectExplorer = projectExplorer;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.gitServiceClient = gitServiceClient;
        this.eventBus = eventBus;
        this.constant = constant;
        this.editorAgent = editorAgent;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    /** Show dialog. */
    public void showDialog() {
        project = appContext.getCurrentProject();
        updateRemotes();
    }

    /**
     * Update the list of remote repositories for local one. If remote repositories are found, then update the list of branches (remote and
     * local).
     */
    private void updateRemotes() {
        view.setEnablePullButton(true);

        gitServiceClient.remoteList(appContext.getDevMachine(), project.getRootProject(), null, true,
                                    new AsyncRequestCallback<List<Remote>>(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class)) {
                                        @Override
                                        protected void onSuccess(List<Remote> result) {
                                            updateBranches(LIST_REMOTE);
                                            view.setRepositories(result);
                                            view.setEnablePullButton(!result.isEmpty());
                                            view.showDialog();
                                        }

                                        @Override
                                        protected void onFailure(Throwable exception) {
                                            handleError(exception, REMOTE_REPO_COMMAND_NAME);
                                            view.setEnablePullButton(false);
                                        }
                                    }
                                   );
    }

    /**
     * Update the list of branches.
     *
     * @param remoteMode
     *         is a remote mode
     */
    private void updateBranches(@NotNull final String remoteMode) {
        gitServiceClient.branchList(appContext.getDevMachine(), project.getRootProject(), remoteMode,
                                    new AsyncRequestCallback<List<Branch>>(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class)) {
                                        @Override
                                        protected void onSuccess(List<Branch> result) {
                                            if (LIST_REMOTE.equals(remoteMode)) {
                                                view.setRemoteBranches(branchSearcher.getRemoteBranchesToDisplay(view.getRepositoryName(),
                                                                                                                 result));
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
                                            handleError(exception, BRANCH_LIST_COMMAND_NAME);
                                            view.setEnablePullButton(false);
                                        }
                                    }
                                   );
    }

    /** {@inheritDoc} */
    @Override
    public void onPullClicked() {
        String remoteName = view.getRepositoryName();
        final String remoteUrl = view.getRepositoryUrl();
        view.close();

        final List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();

        final StatusNotification notification =
                notificationManager.notify(constant.pullProcess(), PROGRESS, FLOAT_MODE, project.getRootProject());

        gitServiceClient.pull(appContext.getDevMachine(), project.getRootProject(), getRefs(), remoteName,
                              new AsyncRequestCallback<PullResponse>(dtoUnmarshallerFactory.newUnmarshaller(PullResponse.class)) {
                                  @Override
                                  protected void onSuccess(PullResponse result) {
                                      GitOutputConsole console = gitOutputConsoleFactory.create(PULL_COMMAND_NAME);
                                      console.print(result.getCommandOutput(), GREEN_COLOR);
                                      consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                                      notification.setStatus(SUCCESS);
                                      if (result.getCommandOutput().contains("Already up-to-date")) {
                                          notification.setTitle(constant.pullUpToDate());
                                      } else {
                                          refreshProjectNodesAndEditors(openedEditors);
                                          notification.setTitle(constant.pullSuccess(remoteUrl));
                                      }
                                  }

                                  @Override
                                  protected void onFailure(Throwable exception) {
                                      notification.setStatus(FAIL);
                                      if (getErrorCode(exception) == ErrorCodes.MERGE_CONFLICT) {
                                          refreshProjectNodesAndEditors(openedEditors);
                                      }
                                      handleError(exception, PULL_COMMAND_NAME);
                                  }
                              });
    }

    /**
     * Refresh project.
     *
     * @param openedEditors
     *         editors that corresponds to open files
     */
    private void refreshProjectNodesAndEditors(final List<EditorPartPresenter> openedEditors) {
        projectExplorer.reloadChildren();
        for (EditorPartPresenter partPresenter : openedEditors) {
            final VirtualFile file = partPresenter.getEditorInput().getFile();
            eventBus.fireEvent(new FileContentUpdateEvent(file.getPath()));
        }
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
        notificationManager.notify(errorMessage, FAIL, FLOAT_MODE, project.getRootProject());
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

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

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
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
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_REMOTE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.ext.git.client.remote.RemotePresenter.REMOTE_REPO_COMMAND_NAME;
import static org.eclipse.che.ide.ext.git.client.compare.branchList.BranchListPresenter.BRANCH_LIST_COMMAND_NAME;

/**
 * Presenter pulling changes from remote repository.
 *
 * @author Ann Zhuleva
 */
@Singleton
public class PullPresenter implements PullView.ActionDelegate {
    public static final String PULL_COMMAND_NAME = "Git pull";

    private final PullView                 view;
    private final GitServiceClient         gitServiceClient;
    private final EventBus                 eventBus;
    private final GitLocalizationConstant  constant;
    private final EditorAgent              editorAgent;
    private final AppContext               appContext;
    private final NotificationManager      notificationManager;
    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private final DtoFactory               dtoFactory;
    private final BranchSearcher           branchSearcher;
    private final ProjectExplorerPresenter projectExplorer;
    private final GitOutputConsoleFactory  gitOutputConsoleFactory;
    private final ConsolesPanelPresenter   consolesPanelPresenter;
    private       CurrentProject           project;
    private final String                   workspaceId;


    @Inject
    public PullPresenter(PullView view,
                         EditorAgent editorAgent,
                         GitServiceClient gitServiceClient,
                         EventBus eventBus,
                         AppContext appContext,
                         GitLocalizationConstant constant,
                         NotificationManager notificationManager,
                         DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         DtoFactory dtoFactory,
                         BranchSearcher branchSearcher,
                         ProjectExplorerPresenter projectExplorer,
                         GitOutputConsoleFactory gitOutputConsoleFactory,
                         ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.dtoFactory = dtoFactory;
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
        this.workspaceId = appContext.getWorkspaceId();
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

        gitServiceClient.remoteList(workspaceId, project.getRootProject(), null, true,
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
                                            String errorMessage =
                                                    exception.getMessage() != null ? exception.getMessage()
                                                                                   : constant.remoteListFailed();
                                            GitOutputConsole console = gitOutputConsoleFactory.create(REMOTE_REPO_COMMAND_NAME);
                                            console.printError(errorMessage);
                                            consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                            notificationManager.notify(constant.remoteListFailed(), FAIL, true, project.getRootProject());
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
        gitServiceClient.branchList(workspaceId, project.getRootProject(), remoteMode,
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
                                            String errorMessage =
                                                    exception.getMessage() != null ? exception.getMessage()
                                                                                   : constant.branchesListFailed();
                                            GitOutputConsole console = gitOutputConsoleFactory.create(BRANCH_LIST_COMMAND_NAME);
                                            console.printError(errorMessage);
                                            consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                            notificationManager.notify(constant.branchesListFailed(), FAIL, true, project.getRootProject());
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

        final List<EditorPartPresenter> openedEditors = new ArrayList<>();
        for (EditorPartPresenter partPresenter : editorAgent.getOpenedEditors().values()) {
            openedEditors.add(partPresenter);
        }

        final StatusNotification notification =
                notificationManager.notify(constant.pullProcess(), PROGRESS, true, project.getRootProject());
        final GitOutputConsole console = gitOutputConsoleFactory.create(PULL_COMMAND_NAME);

        gitServiceClient.pull(workspaceId, project.getRootProject(), getRefs(), remoteName,
                              new AsyncRequestCallback<PullResponse>(dtoUnmarshallerFactory.newUnmarshaller(PullResponse.class)) {
                                  @Override
                                  protected void onSuccess(PullResponse result) {
                                      console.printInfo(result.getCommandOutput());
                                      consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                      notification.setStatus(SUCCESS);
                                      if (result.getCommandOutput().contains("Already up-to-date")) {
                                          notification.setTitle(constant.pullUpToDate());
                                      } else {
                                          refreshProject(openedEditors);
                                          notification.setTitle(constant.pullSuccess(remoteUrl));
                                      }
                                  }

                                  @Override
                                  protected void onFailure(Throwable throwable) {
                                      if (throwable.getMessage().contains("Merge conflict")) {
                                          refreshProject(openedEditors);
                                      }
                                      handleError(throwable, remoteUrl, notification, console);
                                      consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                  }
                              });
    }

    /**
     * Refresh project.
     *
     * @param openedEditors
     *         editors that corresponds to open files
     */
    private void refreshProject(final List<EditorPartPresenter> openedEditors) {
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
     * @param throwable
     *         exception what happened
     */
    private void handleError(@NotNull Throwable throwable, @NotNull String remoteUrl, StatusNotification notification,
                             GitOutputConsole console) {
        String errorMessage = throwable.getMessage();
        notification.setStatus(FAIL);
        if (errorMessage == null) {
            console.printError(constant.pullFail(remoteUrl));
            notification.setTitle(constant.pullFail(remoteUrl));
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
    public void onRemoteBranchChanged() {
        view.selectLocalBranch(view.getRemoteBranch());
    }

    /** {@inheritDoc} */
    @Override
    public void onRemoteRepositoryChanged() {
        updateBranches(LIST_REMOTE);
    }
}

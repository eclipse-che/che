/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.pull;

import static org.eclipse.che.api.git.shared.BranchListMode.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_REMOTE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.ext.git.client.compare.branchlist.BranchListPresenter.BRANCH_LIST_COMMAND_NAME;
import static org.eclipse.che.ide.ext.git.client.remote.RemotePresenter.REMOTE_REPO_COMMAND_NAME;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.auth.Credentials;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.ext.git.client.GitAuthActionPresenter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter pulling changes from remote repository.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class PullPresenter extends GitAuthActionPresenter implements PullView.ActionDelegate {
  public static final String PULL_COMMAND_NAME = "Git pull";

  private static final String GREEN_COLOR = "lightgreen";

  private final PullView view;
  private final GitServiceClient service;
  private final DialogFactory dialogFactory;
  private final BranchSearcher branchSearcher;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;

  private Project project;

  @Inject
  public PullPresenter(
      PullView view,
      GitServiceClient service,
      GitLocalizationConstant constant,
      NotificationManager notificationManager,
      DialogFactory dialogFactory,
      BranchSearcher branchSearcher,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter,
      OAuthServiceClient oauthServiceClient) {
    super(notificationManager, constant, oauthServiceClient);
    this.view = view;
    this.dialogFactory = dialogFactory;
    this.branchSearcher = branchSearcher;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = processesPanelPresenter;
    this.view.setDelegate(this);
    this.service = service;
    this.notificationManager = notificationManager;
  }

  public void showDialog(Project project) {
    this.project = project;

    view.setEnablePullButton(false);

    service
        .remoteList(project.getLocation(), null, true)
        .then(
            remotes -> {
              updateBranches(LIST_REMOTE);
              view.setRepositories(remotes);
              view.setEnablePullButton(!remotes.isEmpty());
              view.showDialog();
            })
        .catchError(
            error -> {
              handleError(error.getCause(), REMOTE_REPO_COMMAND_NAME);
              view.setEnablePullButton(false);
            });
  }

  /**
   * Update the list of branches.
   *
   * @param remoteMode is a remote mode
   */
  private void updateBranches(@NotNull final BranchListMode remoteMode) {

    service
        .branchList(project.getLocation(), remoteMode)
        .then(
            branches -> {
              if (LIST_REMOTE.equals(remoteMode)) {
                view.setRemoteBranches(
                    branchSearcher.getRemoteBranchesToDisplay(view.getRepositoryName(), branches));
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
            })
        .catchError(
            error -> {
              handleError(error.getCause(), BRANCH_LIST_COMMAND_NAME);
              view.setEnablePullButton(false);
            });
  }

  @Override
  public void onPullClicked() {
    view.close();

    final StatusNotification notification =
        notificationManager.notify(locale.pullProcess(), PROGRESS, FLOAT_MODE);
    GitOutputConsole console = gitOutputConsoleFactory.create(PULL_COMMAND_NAME);

    performOperationWithTokenRequestIfNeeded(
            new RemoteGitOperation<PullResponse>() {
              @Override
              public Promise<PullResponse> perform(Credentials credentials) {
                return service.pull(
                    project.getLocation(),
                    getRefs(),
                    view.getRepositoryName(),
                    view.getRebase(),
                    credentials);
              }
            })
        .then(
            response -> {
              console.print(response.getCommandOutput(), GREEN_COLOR);
              consolesPanelPresenter.addCommandOutput(console);
              notification.setStatus(SUCCESS);
              if (response.getCommandOutput().contains("Already up-to-date")) {
                notification.setTitle(locale.pullUpToDate());
              } else {
                notification.setTitle(locale.pullSuccess(view.getRepositoryUrl()));
              }
            })
        .catchError(
            error -> {
              notification.setStatus(FAIL);
              handleError(error.getCause(), PULL_COMMAND_NAME, notification);
            });
  }

  /** @return list of refs to fetch */
  private String getRefs() {
    String remoteName = view.getRepositoryName();
    String localBranch = view.getLocalBranch();
    String remoteBranch = view.getRemoteBranch();

    return localBranch.isEmpty()
        ? remoteBranch
        : "refs/heads/" + localBranch + ":" + "refs/remotes/" + remoteName + "/" + remoteBranch;
  }

  /**
   * Handler some action whether some exception happened.
   *
   * @param exception exception that happened
   * @param commandName name of the command
   */
  private void handleError(Throwable exception, String commandName) {
    handleError(exception, commandName, null);
  }

  /**
   * Handler some action whether some exception happened.
   *
   * @param exception exception that happened
   * @param commandName name of the command
   * @param notification notification to set Error message, if not null
   */
  private void handleError(
      Throwable exception, String commandName, @Nullable Notification notification) {
    int errorCode = getErrorCode(exception);
    if (errorCode == ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED) {
      dialogFactory
          .createMessageDialog(locale.pullTitle(), locale.committerIdentityInfoEmpty(), null)
          .show();
      return;
    } else if (errorCode == ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY) {
      dialogFactory
          .createMessageDialog(locale.pullTitle(), locale.messagesUnableGetSshKey(), null)
          .show();
      return;
    }

    String errorMessage = exception.getMessage();
    if (errorMessage == null) {
      switch (commandName) {
        case REMOTE_REPO_COMMAND_NAME:
          errorMessage = locale.remoteListFailed();
          break;
        case BRANCH_LIST_COMMAND_NAME:
          errorMessage = locale.branchesListFailed();
          break;
        case PULL_COMMAND_NAME:
          errorMessage = locale.pullFail(view.getRepositoryUrl());
          break;
      }
    }

    GitOutputConsole console = gitOutputConsoleFactory.create(commandName);
    console.printError(errorMessage);
    consolesPanelPresenter.addCommandOutput(console);
    if (notification != null) {
      notification.setTitle(errorMessage);
    } else {
      notificationManager.notify(errorMessage, FAIL, FLOAT_MODE);
    }
  }

  @Override
  public void onCancelClicked() {
    view.close();
  }

  @Override
  public void onRemoteBranchChanged() {
    view.selectLocalBranch(view.getRemoteBranch());
  }

  @Override
  public void onRemoteRepositoryChanged() {
    updateBranches(LIST_REMOTE);
  }
}

/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.fetch;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_REMOTE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Presenter for fetching changes from remote repository.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class FetchPresenter implements FetchView.ActionDelegate {
  public static final String FETCH_COMMAND_NAME = "Git fetch";

  private final DtoFactory dtoFactory;
  private final NotificationManager notificationManager;
  private final BranchSearcher branchSearcher;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter processesPanelPresenter;
  private final FetchView view;
  private final GitServiceClient service;
  private final AppContext appContext;
  private final GitLocalizationConstant constant;

  private Project project;

  @Inject
  public FetchPresenter(
      DtoFactory dtoFactory,
      FetchView view,
      GitServiceClient service,
      AppContext appContext,
      GitLocalizationConstant constant,
      NotificationManager notificationManager,
      BranchSearcher branchSearcher,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter) {
    this.dtoFactory = dtoFactory;
    this.view = view;
    this.branchSearcher = branchSearcher;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.processesPanelPresenter = processesPanelPresenter;
    this.view.setDelegate(this);
    this.service = service;
    this.appContext = appContext;
    this.constant = constant;
    this.notificationManager = notificationManager;
  }

  /** Show dialog. */
  public void showDialog(Project project) {
    this.project = project;
    view.setRemoveDeleteRefs(false);
    view.setFetchAllBranches(true);
    updateRemotes();
  }

  /**
   * Update the list of remote repositories for local one. If remote repositories are found, then
   * update the list of branches (remote and local).
   */
  private void updateRemotes() {
    service
        .remoteList(project.getLocation(), null, true)
        .then(
            remotes -> {
              view.setRepositories(remotes);
              updateBranches(LIST_REMOTE);
              view.setEnableFetchButton(!remotes.isEmpty());
              view.showDialog();
            })
        .catchError(
            error -> {
              GitOutputConsole console = gitOutputConsoleFactory.create(FETCH_COMMAND_NAME);
              console.printError(constant.remoteListFailed());
              processesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.remoteListFailed(), FAIL, FLOAT_MODE);
              view.setEnableFetchButton(false);
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
              final String errorMessage =
                  error.getMessage() != null ? error.getMessage() : constant.branchesListFailed();
              GitOutputConsole console = gitOutputConsoleFactory.create(FETCH_COMMAND_NAME);
              console.printError(errorMessage);
              processesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.branchesListFailed(), FAIL, FLOAT_MODE);
              view.setEnableFetchButton(false);
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onFetchClicked() {
    final String remoteUrl = view.getRepositoryUrl();

    final StatusNotification notification =
        notificationManager.notify(constant.fetchProcess(), PROGRESS, FLOAT_MODE);
    final GitOutputConsole console = gitOutputConsoleFactory.create(FETCH_COMMAND_NAME);

    service
        .fetch(
            project.getLocation(), view.getRepositoryName(), getRefs(), view.isRemoveDeletedRefs())
        .then(
            ignored -> {
              console.print(constant.fetchSuccess(remoteUrl));
              processesPanelPresenter.addCommandOutput(console);
              notification.setStatus(SUCCESS);
              notification.setTitle(constant.fetchSuccess(remoteUrl));
            })
        .catchError(
            error -> {
              handleError(error.getCause(), remoteUrl, notification, console);
              processesPanelPresenter.addCommandOutput(console);
            });
    view.close();
  }

  /** @return list of refs to fetch */
  @NotNull
  private List<String> getRefs() {
    if (view.isFetchAllBranches()) {
      return emptyList();
    }

    String localBranch = view.getLocalBranch();
    String remoteBranch = view.getRemoteBranch();
    String remoteName = view.getRepositoryName();
    String refs =
        localBranch.isEmpty()
            ? remoteBranch
            : "refs/heads/" + localBranch + ":" + "refs/remotes/" + remoteName + "/" + remoteBranch;
    return singletonList(refs);
  }

  /**
   * Handler some action whether some exception happened.
   *
   * @param throwable exception what happened
   */
  private void handleError(
      @NotNull Throwable throwable,
      @NotNull String remoteUrl,
      StatusNotification notification,
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

  @Override
  public void onCancelClicked() {
    view.close();
  }

  @Override
  public void onValueChanged() {
    boolean isFetchAll = view.isFetchAllBranches();
    view.setEnableLocalBranchField(!isFetchAll);
    view.setEnableRemoteBranchField(!isFetchAll);
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

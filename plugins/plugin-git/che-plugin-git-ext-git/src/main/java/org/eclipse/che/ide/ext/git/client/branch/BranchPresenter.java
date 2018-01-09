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
package org.eclipse.che.ide.ext.git.client.branch;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter for displaying and work with branches.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class BranchPresenter implements BranchView.ActionDelegate {

  private static final String BRANCH_RENAME_COMMAND_NAME = "Git rename branch";
  private static final String BRANCH_DELETE_COMMAND_NAME = "Git delete branch";
  private static final String BRANCH_CHECKOUT_COMMAND_NAME = "Git checkout branch";
  private static final String BRANCH_CREATE_COMMAND_NAME = "Git create branch";
  private static final String BRANCH_LIST_COMMAND_NAME = "Git list of branches";

  private final DtoFactory dtoFactory;
  private final BranchView view;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;
  private final DialogFactory dialogFactory;
  private final GitServiceClient service;
  private final GitLocalizationConstant constant;
  private final NotificationManager notificationManager;

  private Branch selectedBranch;
  private Project project;

  /** Create presenter. */
  @Inject
  BranchPresenter(
      BranchView view,
      DtoFactory dtoFactory,
      GitServiceClient service,
      GitLocalizationConstant constant,
      NotificationManager notificationManager,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter,
      DialogFactory dialogFactory) {
    this.view = view;
    this.dtoFactory = dtoFactory;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = processesPanelPresenter;
    this.dialogFactory = dialogFactory;
    this.view.setDelegate(this);
    this.service = service;
    this.constant = constant;
    this.notificationManager = notificationManager;
  }

  /** Open dialog if closed and shows branches. */
  public void showBranches(Project project) {
    this.project = project;
    getBranches();
  }

  @Override
  public void onClose() {
    view.closeDialogIfShowing();
    view.setTextToSearchFilterLabel("");
    view.clearSearchFilter();
  }

  @Override
  public void onRenameClicked() {
    if (selectedBranch.isRemote()) {
      dialogFactory
          .createConfirmDialog(
              constant.branchConfirmRenameTitle(),
              constant.branchConfirmRenameMessage(),
              this::renameBranch,
              null)
          .show();
    } else {
      renameBranch();
    }
  }

  private void renameBranch() {
    final String selectedBranchName = getSelectedBranchName();
    dialogFactory
        .createInputDialog(
            constant.branchTitleRename(),
            constant.branchTypeRename(),
            selectedBranchName,
            0,
            selectedBranchName.length(),
            this::renameBranch,
            null)
        .show();
  }

  private void renameBranch(String newName) {
    service
        .branchRename(project.getLocation(), selectedBranch.getDisplayName(), newName)
        .then(
            ignored -> {
              getBranches();
              view.setFocus();
            })
        .catchError(
            error -> {
              handleError(error.getCause(), BRANCH_RENAME_COMMAND_NAME);
              getBranches(); // rename of remote branch occurs in three stages, so needs update list
              // of branches on view
            });
  }

  /** @return name of branch, e.g. 'origin/master' -> 'master' */
  private String getSelectedBranchName() {
    String selectedBranchName = selectedBranch.getDisplayName();
    String[] tokens = selectedBranchName.split("/");
    return tokens.length > 0 ? tokens[tokens.length - 1] : selectedBranchName;
  }

  @Override
  public void onDeleteClicked() {
    dialogFactory
        .createConfirmDialog(
            constant.branchDelete(),
            constant.branchDeleteAsk(getSelectedBranchName()),
            () ->
                service
                    .branchDelete(project.getLocation(), selectedBranch.getName(), true)
                    .then(
                        ignored -> {
                          getBranches();
                          view.setFocus();
                        })
                    .catchError(
                        error -> {
                          handleError(error.getCause(), BRANCH_DELETE_COMMAND_NAME);
                        }),
            null)
        .show();
  }

  @Override
  public void onCheckoutClicked() {
    final CheckoutRequest checkoutRequest = dtoFactory.createDto(CheckoutRequest.class);
    if (selectedBranch.isRemote()) {
      checkoutRequest.setTrackBranch(selectedBranch.getDisplayName());
    } else {
      checkoutRequest.setName(selectedBranch.getDisplayName());
    }

    service
        .checkout(project.getLocation(), checkoutRequest)
        .then(
            ignored -> {
              view.closeDialogIfShowing();
            })
        .catchError(
            error -> {
              handleError(error.getCause(), BRANCH_CHECKOUT_COMMAND_NAME);
            });
  }

  /** Get the list of branches. */
  private void getBranches() {
    service
        .branchList(project.getLocation(), BranchListMode.from(view.getFilterValue()))
        .then(
            branches -> {
              if (branches.isEmpty()) {
                dialogFactory
                    .createMessageDialog(
                        constant.branchTitle(), constant.initCommitWasNotPerformed(), null)
                    .show();
              } else {
                view.setBranches(branches);
                view.showDialogIfClosed();
              }
            })
        .catchError(
            error -> {
              handleError(error.getCause(), BRANCH_LIST_COMMAND_NAME);
            });
  }

  @Override
  public void onCreateClicked() {
    dialogFactory
        .createInputDialog(
            constant.branchCreateNew(),
            constant.branchTypeNew(),
            value -> {
              if (value.isEmpty()) {
                return;
              }

              service
                  .branchCreate(project.getLocation(), value, null)
                  .then(
                      branch -> {
                        getBranches();
                        view.setFocus();
                      })
                  .catchError(
                      error -> {
                        handleError(error.getCause(), BRANCH_CREATE_COMMAND_NAME);
                      });
            },
            null)
        .show();
  }

  @Override
  public void onBranchUnselected() {
    selectedBranch = null;

    view.setEnableCheckoutButton(false);
    view.setEnableRenameButton(false);
    view.setEnableDeleteButton(false);
  }

  @Override
  public void onLocalRemoteFilterChanged() {
    getBranches();
  }

  @Override
  public void onSearchFilterChanged(String filter) {
    view.setTextToSearchFilterLabel(filter);
  }

  @Override
  public void onBranchSelected(@NotNull Branch branch) {
    selectedBranch = branch;
    boolean isActive = selectedBranch.isActive();

    view.setEnableCheckoutButton(!isActive);
    view.setEnableDeleteButton(!isActive);
    view.setEnableRenameButton(true);
  }

  /**
   * Handler some action whether some exception happened.
   *
   * @param exception exception what happened
   * @param commandName name of the executed command
   */
  void handleError(@NotNull Throwable exception, String commandName) {
    if (getErrorCode(exception) == ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY) {
      dialogFactory
          .createMessageDialog(commandName, constant.messagesUnableGetSshKey(), null)
          .show();
      return;
    }

    String errorMessage = exception.getMessage();
    if (errorMessage == null) {
      switch (commandName) {
        case BRANCH_CREATE_COMMAND_NAME:
          errorMessage = constant.branchCreateFailed();
          break;
        case BRANCH_DELETE_COMMAND_NAME:
          errorMessage = constant.branchDeleteFailed();
          break;
        case BRANCH_LIST_COMMAND_NAME:
          errorMessage = constant.branchesListFailed();
          break;
        case BRANCH_RENAME_COMMAND_NAME:
          errorMessage = constant.branchRenameFailed();
          break;
        case BRANCH_CHECKOUT_COMMAND_NAME:
          errorMessage = constant.branchCheckoutFailed();
          break;
      }
    }

    GitOutputConsole console = gitOutputConsoleFactory.create(commandName);
    printGitMessage(errorMessage, console);
    consolesPanelPresenter.addCommandOutput(console);
    notificationManager.notify(errorMessage, FAIL, FLOAT_MODE);
  }

  private void printGitMessage(String messageText, GitOutputConsole console) {
    console.print("");
    String[] lines = messageText.split("\n");
    for (String line : lines) {
      console.printError(line);
    }
  }
}

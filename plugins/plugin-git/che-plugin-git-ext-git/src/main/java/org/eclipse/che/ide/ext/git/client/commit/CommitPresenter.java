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
package org.eclipse.che.ide.ext.git.client.commit;

import static com.google.common.collect.Iterables.getFirst;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_REMOTE;
import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.resource.Path.valueOf;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel.SelectableChangesPanelPresenter;
import org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel.SelectionCallBack;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter for commit changes on git.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 * @author Igor Vinokur
 */
@Singleton
public class CommitPresenter implements CommitView.ActionDelegate, SelectionCallBack {
  private static final String COMMIT_COMMAND_NAME = "Git commit";

  private final SelectableChangesPanelPresenter selectableChangesPanelPresenter;
  private final DialogFactory dialogFactory;
  private final AppContext appContext;
  private final CommitView view;
  private final GitServiceClient service;
  private final GitLocalizationConstant constant;
  private final NotificationManager notificationManager;
  private final DateTimeFormatter dateTimeFormatter;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;

  private Project project;

  @Inject
  public CommitPresenter(
      CommitView view,
      GitServiceClient service,
      SelectableChangesPanelPresenter selectableChangesPanelPresenter,
      GitLocalizationConstant constant,
      NotificationManager notificationManager,
      DialogFactory dialogFactory,
      AppContext appContext,
      DateTimeFormatter dateTimeFormatter,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter) {
    this.view = view;
    this.selectableChangesPanelPresenter = selectableChangesPanelPresenter;
    this.dialogFactory = dialogFactory;
    this.appContext = appContext;
    this.dateTimeFormatter = dateTimeFormatter;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = processesPanelPresenter;
    this.view.setDelegate(this);
    this.service = service;
    this.constant = constant;
    this.notificationManager = notificationManager;

    this.view.setChangesPanelView(selectableChangesPanelPresenter.getView());
  }

  public void showDialog(Project project) {
    this.project = project;

    view.setValueToAmendCheckBox(false);
    view.setValueToPushAfterCommitCheckBox(false);
    view.setEnableAmendCheckBox(true);
    view.setEnablePushAfterCommitCheckBox(true);
    view.setEnableRemoteBranchesDropDownLis(false);
    service
        .diff(project.getLocation(), null, NAME_STATUS, true, 0, "HEAD", false)
        .then(
            diff -> {
              service
                  .log(project.getLocation(), null, -1, 1, false)
                  .then(
                      arg -> {
                        if (diff.isEmpty()) {
                          showAskForAmendDialog();
                        } else {
                          show(diff);
                        }
                      })
                  .catchError(
                      error -> {
                        if (getErrorCode(error.getCause())
                            == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                          service
                              .getStatus(project.getLocation(), emptyList())
                              .then(
                                  status -> {
                                    view.setEnableAmendCheckBox(false);
                                    view.setEnablePushAfterCommitCheckBox(false);
                                    List<String> newFiles = new ArrayList<>();
                                    newFiles.addAll(status.getAdded());
                                    newFiles.addAll(status.getUntracked());
                                    show(newFiles.stream().collect(joining("\nA\t", "A\t", "")));
                                  });
                        }
                      });
            })
        .catchError(
            arg -> {
              notificationManager.notify(constant.diffFailed(), FAIL, FLOAT_MODE);
            });

    service
        .branchList(project.getLocation(), LIST_REMOTE)
        .then(view::setRemoteBranchesList)
        .catchError(
            error -> {
              notificationManager.notify(constant.branchesListFailed(), FAIL, FLOAT_MODE);
            });
  }

  private void showAskForAmendDialog() {
    dialogFactory
        .createConfirmDialog(
            constant.commitTitle(),
            constant.commitNothingToCommitMessageText(),
            constant.buttonYes(),
            constant.buttonNo(),
            () -> {
              view.setValueToAmendCheckBox(true);
              view.setEnablePushAfterCommitCheckBox(false);
              setAmendCommitMessage();
              show(null);
            },
            null)
        .show();
  }

  private void show(@Nullable String diff) {
    AlteredFiles alteredFiles = new AlteredFiles(project, diff);

    view.setEnableCommitButton(!view.getCommitMessage().isEmpty());
    view.focusInMessageField();
    view.showDialog();
    selectableChangesPanelPresenter.show(alteredFiles, this);
    selectableChangesPanelPresenter.setMarkedCheckBoxes(
        stream(appContext.getResources())
            .map(resource -> resource.getLocation().removeFirstSegments(1))
            .collect(Collectors.toSet()));
  }

  @Override
  public void onCommitClicked() {
    Path location = project.getLocation();
    Path[] filesToCommitArray = getFilesToCommit();

    service
        .add(location, false, filesToCommitArray)
        .then(
            arg -> {
              service
                  .commit(location, view.getCommitMessage(), view.isAmend(), filesToCommitArray)
                  .then(
                      revision -> {
                        onCommitSuccess(revision);
                        if (view.isPushAfterCommit()) {
                          push(location);
                        }
                        view.close();
                      })
                  .catchError(
                      error -> {
                        handleError(error.getCause());
                      });
            })
        .catchError(
            error -> {
              notificationManager.notify(constant.addFailed(), FAIL, FLOAT_MODE);
            });
  }

  private Path[] getFilesToCommit() {
    List<String> selectedFiles = selectableChangesPanelPresenter.getSelectedFiles();
    int selectedFilesSize = selectedFiles.size();
    Path[] filesToCommit = new Path[selectedFilesSize];
    for (int i = 0; i < selectedFilesSize; i++) {
      filesToCommit[i] = valueOf(selectedFiles.get(i));
    }
    return filesToCommit;
  }

  private void push(Path location) {
    String remoteBranch = view.getRemoteBranch();
    String remote = remoteBranch.split("/")[0];
    String branch = remoteBranch.split("/")[1];
    service
        .push(location, singletonList(branch), remote, false)
        .then(
            result -> {
              notificationManager.notify(constant.pushSuccess(remote), SUCCESS, FLOAT_MODE);
            })
        .catchError(
            error -> {
              notificationManager.notify(constant.pushFail(), FAIL, FLOAT_MODE);
            });
  }

  @Override
  public void onCancelClicked() {
    view.close();
  }

  @Override
  public void onValueChanged() {
    view.setEnableCommitButton(
        !view.getCommitMessage().isEmpty()
            && (!selectableChangesPanelPresenter.getSelectedFiles().isEmpty() || view.isAmend()));
  }

  @Override
  public void onSelectionChanged(Path path, boolean isChecked) {
    onValueChanged();
  }

  @Override
  public void setAmendCommitMessage() {
    service
        .log(project.getLocation(), null, -1, -1, false)
        .then(
            log -> {
              String message = "";
              final Revision revision = getFirst(log.getCommits(), null);
              if (revision != null) {
                message = revision.getMessage();
              }
              CommitPresenter.this.view.setMessage(message);
              CommitPresenter.this.view.setEnableCommitButton(!message.isEmpty());
            })
        .catchError(
            error -> {
              if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                dialogFactory
                    .createMessageDialog(
                        constant.commitTitle(), constant.initCommitWasNotPerformed(), null)
                    .show();
              } else {
                CommitPresenter.this.view.setMessage("");
                notificationManager.notify(constant.logFailed(), FAIL, NOT_EMERGE_MODE);
              }
            });
  }

  private void onCommitSuccess(@NotNull final Revision revision) {
    String date = dateTimeFormatter.getFormattedDate(revision.getCommitTime());
    String message = constant.commitMessage(revision.getId(), date);

    if ((revision.getCommitter() != null
        && revision.getCommitter().getName() != null
        && !revision.getCommitter().getName().isEmpty())) {
      message += " " + constant.commitUser(revision.getCommitter().getName());
    }
    GitOutputConsole console = gitOutputConsoleFactory.create(COMMIT_COMMAND_NAME);
    console.print(message);
    consolesPanelPresenter.addCommandOutput(console);
    notificationManager.notify(message);
    view.setMessage("");
  }

  private void handleError(@NotNull Throwable exception) {
    if (exception instanceof ServerException
        && ((ServerException) exception).getErrorCode()
            == ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED) {
      dialogFactory
          .createMessageDialog(constant.commitTitle(), constant.committerIdentityInfoEmpty(), null)
          .show();
      return;
    }
    String exceptionMessage = exception.getMessage();
    String errorMessage =
        (exceptionMessage != null && !exceptionMessage.isEmpty())
            ? exceptionMessage
            : constant.commitFailed();
    GitOutputConsole console = gitOutputConsoleFactory.create(COMMIT_COMMAND_NAME);
    console.printError(errorMessage);
    consolesPanelPresenter.addCommandOutput(console);
    notificationManager.notify(constant.commitFailed(), errorMessage, FAIL, FLOAT_MODE);
  }
}

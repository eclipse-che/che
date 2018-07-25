/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.revert;

import static org.eclipse.che.api.git.shared.Constants.DEFAULT_PAGE_SIZE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.RevertResult;
import org.eclipse.che.api.git.shared.RevertResult.RevertStatus;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.ext.git.client.revert.RevertCommitView.ActionDelegate;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter for reverting commits
 *
 * @author Dmitrii Bocharov (bdshadow)
 */
@Singleton
public class RevertCommitPresenter implements ActionDelegate {
  private final RevertCommitView view;
  private final GitServiceClient service;
  private final DialogFactory dialogFactory;
  private final GitLocalizationConstant constant;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;
  private final NotificationManager notificationManager;

  private Revision selectedRevision;
  private List<Revision> revisions;
  private Project project;
  private int skip;

  @Inject
  public RevertCommitPresenter(
      RevertCommitView view,
      GitServiceClient service,
      DialogFactory dialogFactory,
      GitLocalizationConstant constant,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter consolesPanelPresenter,
      NotificationManager notificationManager) {
    this.view = view;
    this.service = service;
    this.dialogFactory = dialogFactory;
    this.constant = constant;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = consolesPanelPresenter;
    this.notificationManager = notificationManager;

    this.view.setDelegate(this);
  }

  public void show(final Project project) {
    this.project = project;
    this.skip = 0;
    this.revisions = new ArrayList<>();

    fetchRevisions();
  }

  @Override
  public void onRevertClicked() {
    this.view.close();
    revert();
  }

  @Override
  public void onCancelClicked() {
    this.view.close();
  }

  @Override
  public void onRevisionSelected(Revision revision) {
    this.selectedRevision = revision;
    view.setEnableRevertButton(true);
  }

  @Override
  public void onScrolledToBottom() {
    fetchRevisions();
  }

  private void fetchRevisions() {
    service
        .log(project.getLocation(), null, skip, DEFAULT_PAGE_SIZE, false)
        .then(
            log -> {
              List<Revision> commits = log.getCommits();
              if (!commits.isEmpty()) {
                skip += commits.size();
                revisions.addAll(commits);
                view.setEnableRevertButton(selectedRevision != null);
                view.setRevisions(revisions);
                view.showDialog();
              }
            })
        .catchError(
            error -> {
              if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                dialogFactory
                    .createMessageDialog(
                        constant.revertCommitViewTitle(),
                        constant.initCommitWasNotPerformed(),
                        null)
                    .show();
                return;
              }
              String errorMessage =
                  (error.getMessage() != null) ? error.getMessage() : constant.logFailed();
              GitOutputConsole console =
                  gitOutputConsoleFactory.create(constant.consoleLogCommandName());
              console.printError(errorMessage);
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.logFailed(), FAIL, FLOAT_MODE);
            });
  }

  private void revert() {
    final GitOutputConsole console =
        gitOutputConsoleFactory.create(constant.consoleRevertCommandName());
    service
        .revert(project.getLocation(), selectedRevision.getId())
        .then(
            result -> {
              console.print(formRevertMessage(result));

              String conflictsMessage = formConflictsMessage(result.getConflicts());
              if (!conflictsMessage.isEmpty()) {
                console.printError(conflictsMessage);
              }

              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.revertCommitSuccessfully());
            })
        .catchError(
            error -> {
              String errorMessage =
                  (error.getMessage() != null) ? error.getMessage() : constant.revertCommitFailed();
              console.printError(errorMessage);
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.revertCommitFailed(), FAIL, FLOAT_MODE);
            });
  }

  private String formRevertMessage(RevertResult revertResult) {
    StringBuilder message = new StringBuilder();
    if (revertResult.getNewHead() != null) {
      message.append(constant.revertedNewHead(revertResult.getNewHead()));
    }
    List<String> commits = revertResult.getRevertedCommits();
    if (commits != null && commits.size() > 0) {
      StringBuilder revertedCommits = new StringBuilder();
      for (String commit : commits) {
        revertedCommits.append("- " + commit);
      }
      message.append(
          revertedCommits.length() > 0
              ? " " + constant.revertedCommits(revertedCommits.toString()) + "\n"
              : "\n");
    }

    return message.toString();
  }

  private String formConflictsMessage(Map<String, RevertStatus> conflicts) {
    if (conflicts != null && conflicts.size() > 0) {
      StringBuilder conflictsMessage = new StringBuilder(constant.revertedConflicts() + "\n");
      for (Map.Entry<String, RevertStatus> conflictEntry : conflicts.entrySet()) {
        conflictsMessage
            .append("    ")
            .append(conflictEntry.getKey())
            .append(" - ")
            .append(conflictEntry.getValue().getValue())
            .append("\n");
      }
      return conflictsMessage.toString();
    }
    return "";
  }
}

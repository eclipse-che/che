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
package org.eclipse.che.ide.ext.git.client.reset.commit;

import static org.eclipse.che.api.git.shared.Constants.DEFAULT_PAGE_SIZE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter for resetting head to commit.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ResetToCommitPresenter implements ResetToCommitView.ActionDelegate {
  public static final String RESET_COMMAND_NAME = "Git reset to commit";
  public static final String LOG_COMMAND_NAME = "Git log";

  private final ResetToCommitView view;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final DialogFactory dialogFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;
  private final GitServiceClient service;
  private final AppContext appContext;
  private final GitLocalizationConstant constant;
  private final NotificationManager notificationManager;

  private Revision selectedRevision;
  private List<Revision> revisions;
  private Project project;
  private int skip;

  @Inject
  public ResetToCommitPresenter(
      ResetToCommitView view,
      GitServiceClient service,
      GitLocalizationConstant constant,
      DialogFactory dialogFactory,
      AppContext appContext,
      NotificationManager notificationManager,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter) {
    this.view = view;
    this.dialogFactory = dialogFactory;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = processesPanelPresenter;
    this.view.setDelegate(this);
    this.service = service;
    this.constant = constant;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
  }

  public void showDialog(final Project project) {
    this.project = project;
    this.skip = 0;
    this.revisions = new ArrayList<>();
    this.selectedRevision = null;
    this.view.resetRevisionSelection();

    fetchAndAddNextRevisions();
  }

  /** {@inheritDoc} */
  @Override
  public void onResetClicked() {
    view.close();
    reset();
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelClicked() {
    view.close();
  }

  /** {@inheritDoc} */
  @Override
  public void onRevisionSelected(@NotNull Revision revision) {
    selectedRevision = revision;
    view.setEnableResetButton(selectedRevision != null);
  }

  @Override
  public void onScrolledToBottom() {
    fetchAndAddNextRevisions();
  }

  private void fetchAndAddNextRevisions() {
    service
        .log(project.getLocation(), null, skip, DEFAULT_PAGE_SIZE, false)
        .then(
            log -> {
              List<Revision> commits = log.getCommits();
              if (!commits.isEmpty()) {
                skip += commits.size();
                revisions.addAll(commits);
                view.setEnableResetButton(selectedRevision != null);
                view.setRevisions(revisions);
                view.setMixMode(true);
                view.showDialog();

                project.synchronize();
              }
            })
        .catchError(
            error -> {
              if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                dialogFactory
                    .createMessageDialog(
                        constant.resetCommitViewTitle(), constant.initCommitWasNotPerformed(), null)
                    .show();
                return;
              }
              String errorMessage =
                  (error.getMessage() != null) ? error.getMessage() : constant.logFailed();
              GitOutputConsole console = gitOutputConsoleFactory.create(LOG_COMMAND_NAME);
              console.printError(errorMessage);
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.logFailed(), FAIL, FLOAT_MODE);
            });
  }

  /** Reset current HEAD to the specified state and refresh project in the success case. */
  private void reset() {
    ResetRequest.ResetType type = view.isMixMode() ? ResetRequest.ResetType.MIXED : null;
    type = (type == null && view.isSoftMode()) ? ResetRequest.ResetType.SOFT : type;
    type = (type == null && view.isHardMode()) ? ResetRequest.ResetType.HARD : type;

    final GitOutputConsole console = gitOutputConsoleFactory.create(RESET_COMMAND_NAME);

    service
        .reset(project.getLocation(), selectedRevision.getId(), type, null)
        .then(
            ignored -> {
              console.print(constant.resetSuccessfully());
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.resetSuccessfully());

              project.synchronize();
            })
        .catchError(
            error -> {
              String errorMessage =
                  (error.getMessage() != null) ? error.getMessage() : constant.resetFail();
              console.printError(errorMessage);
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.resetFail(), FAIL, FLOAT_MODE);
            });
  }
}

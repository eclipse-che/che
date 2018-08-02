/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client.steps;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.plugin.pullrequest.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CANCEL;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;
import javax.inject.Inject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.dialogs.commit.CommitPresenter;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import org.eclipse.che.plugin.pullrequest.shared.dto.Configuration;

/**
 * This step allow the user to commit the current working tree if the git repository status is not
 * clean.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class CommitWorkingTreeStep implements Step {
  private final CommitPresenter commitPresenter;
  private final ContributeMessages messages;
  private final NotificationManager notificationManager;

  @Inject
  public CommitWorkingTreeStep(
      final CommitPresenter commitPresenter,
      final ContributeMessages messages,
      final NotificationManager notificationManager) {
    this.commitPresenter = commitPresenter;
    this.messages = messages;
    this.notificationManager = notificationManager;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    final Configuration configuration = context.getConfiguration();

    commitPresenter.setCommitActionHandler(
        new CommitPresenter.CommitActionHandler() {
          @Override
          public void onCommitAction(final CommitAction action) {
            if (action == CANCEL) {
              executor.fail(CommitWorkingTreeStep.this, context, messages.stepCommitCanceled());
            } else {
              executor.done(CommitWorkingTreeStep.this, context);
            }
          }
        });
    commitPresenter.hasUncommittedChanges(
        new AsyncCallback<Boolean>() {
          @Override
          public void onFailure(final Throwable exception) {
            notificationManager.notify(exception.getLocalizedMessage(), FAIL, FLOAT_MODE);
            executor.fail(CommitWorkingTreeStep.this, context, exception.getLocalizedMessage());
          }

          @Override
          public void onSuccess(final Boolean hasUncommittedChanges) {
            if (hasUncommittedChanges) {
              commitPresenter.showView(
                  messages.contributorExtensionDefaultCommitDescription(
                      configuration.getContributionBranchName(),
                      configuration.getContributionTitle()));
            } else {
              executor.done(CommitWorkingTreeStep.this, context);
            }
          }
        });
  }
}

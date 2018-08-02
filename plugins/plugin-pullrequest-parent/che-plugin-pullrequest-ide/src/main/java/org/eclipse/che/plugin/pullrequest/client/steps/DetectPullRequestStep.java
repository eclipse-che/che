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
import static org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowStatus.READY_TO_UPDATE_PR;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import org.eclipse.che.plugin.pullrequest.shared.dto.PullRequest;

/**
 * Detects if pull request exists for current working branch, stops creation workflow if so and
 * toggles update pull request mode.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class DetectPullRequestStep implements Step {

  private final ContributeMessages messages;
  private final NotificationManager notificationManager;

  @Inject
  public DetectPullRequestStep(ContributeMessages messages, NotificationManager manager) {
    this.messages = messages;
    this.notificationManager = manager;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    context
        .getVcsHostingService()
        .getPullRequest(
            context.getOriginRepositoryOwner(),
            context.getOriginRepositoryName(),
            context.getHostUserLogin(),
            context.getWorkBranchName())
        .then(
            new Operation<PullRequest>() {
              @Override
              public void apply(final PullRequest pr) throws OperationException {
                notificationManager.notify(
                    messages.stepDetectPrExistsTitle(),
                    messages.stepDetectPrExistsTitle(context.getWorkBranchName()),
                    StatusNotification.Status.FAIL,
                    FLOAT_MODE);
                context.setPullRequest(pr);
                context.setPullRequestIssueNumber(pr.getNumber());
                context.setForkedRepositoryName(context.getOriginRepositoryName());
                context.setStatus(READY_TO_UPDATE_PR);
                executor.fail(
                    DetectPullRequestStep.this, context, messages.stepDetectPrExistsTitle());
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(final PromiseError error) throws OperationException {
                // keep going if pr already exists
                executor.done(DetectPullRequestStep.this, context);
              }
            });
  }
}

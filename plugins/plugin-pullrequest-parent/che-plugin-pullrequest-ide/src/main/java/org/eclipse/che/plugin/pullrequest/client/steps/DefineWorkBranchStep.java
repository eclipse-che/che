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

import com.google.inject.Singleton;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsService;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsServiceProvider;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/**
 * This step defines the working branch for the user contribution.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class DefineWorkBranchStep implements Step {

  private final NotificationManager notificationManager;
  private final VcsServiceProvider vcsServiceProvider;

  @Inject
  public DefineWorkBranchStep(
      final NotificationManager notificationManager, final VcsServiceProvider vcsServiceProvider) {
    this.notificationManager = notificationManager;
    this.vcsServiceProvider = vcsServiceProvider;
  }

  @Override
  public void execute(@NotNull final WorkflowExecutor executor, final Context context) {
    final VcsService vcsService = vcsServiceProvider.getVcsService(context.getProject());

    vcsService
        .getBranchName(context.getProject())
        .then(
            new Operation<String>() {
              @Override
              public void apply(String branchName) throws OperationException {
                if (context.getContributeToBranchName() == null) {
                  context.setContributeToBranchName(branchName);
                }
                context.setWorkBranchName(branchName);
                executor.done(DefineWorkBranchStep.this, context);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError err) throws OperationException {
                notificationManager.notify(err.getCause().getLocalizedMessage(), FAIL, FLOAT_MODE);
                executor.fail(
                    DefineWorkBranchStep.this, context, err.getCause().getLocalizedMessage());
              }
            });
  }
}

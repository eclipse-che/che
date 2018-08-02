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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingServiceProvider;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import org.eclipse.che.plugin.pullrequest.shared.dto.Repository;

public class WaitForkOnRemoteStep implements Step {
  private static final int POLL_FREQUENCY_MS = 1000;

  private final VcsHostingServiceProvider vcsHostingServiceProvider;
  private final Step nextStep;
  private Timer timer;

  @AssistedInject
  public WaitForkOnRemoteStep(
      @NotNull final VcsHostingServiceProvider vcsHostingServiceProvider,
      @NotNull final @Assisted Step nextStep) {
    this.vcsHostingServiceProvider = vcsHostingServiceProvider;
    this.nextStep = nextStep;
  }

  @Override
  public void execute(@NotNull final WorkflowExecutor executor, final Context context) {
    if (timer == null) {
      timer =
          new Timer() {
            @Override
            public void run() {
              checkRepository(
                  context,
                  new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(final Throwable caught) {
                      timer.schedule(POLL_FREQUENCY_MS);
                    }

                    @Override
                    public void onSuccess(final Void result) {
                      executor.done(WaitForkOnRemoteStep.this, context);
                    }
                  });
            }
          };
    }

    timer.schedule(POLL_FREQUENCY_MS);
  }

  private void checkRepository(final Context context, final AsyncCallback<Void> callback) {
    context
        .getVcsHostingService()
        .getRepository(context.getHostUserLogin(), context.getForkedRepositoryName())
        .then(
            new Operation<Repository>() {
              @Override
              public void apply(Repository arg) throws OperationException {
                callback.onSuccess(null);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(arg.getCause());
              }
            });
  }
}

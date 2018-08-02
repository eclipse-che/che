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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoUserForkException;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import org.eclipse.che.plugin.pullrequest.shared.dto.Repository;

/** Create a fork of the contributed project (upstream) to push the user's contribution. */
@Singleton
public class CreateForkStep implements Step {
  private final ContributeMessages messages;

  @Inject
  public CreateForkStep(final ContributeMessages messages) {
    this.messages = messages;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    final String originRepositoryOwner = context.getOriginRepositoryOwner();
    final String originRepositoryName = context.getOriginRepositoryName();
    final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
    final String upstreamRepositoryName = context.getUpstreamRepositoryName();

    // the upstream repository has been cloned a fork must be created
    if (originRepositoryOwner.equalsIgnoreCase(upstreamRepositoryOwner)
        && originRepositoryName.equalsIgnoreCase(upstreamRepositoryName)) {

      context
          .getVcsHostingService()
          .getUserFork(context.getHostUserLogin(), upstreamRepositoryOwner, upstreamRepositoryName)
          .then(
              new Operation<Repository>() {
                @Override
                public void apply(Repository fork) throws OperationException {
                  proceed(fork.getName(), executor, context);
                }
              })
          .catchError(
              new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError error) throws OperationException {
                  if (error.getCause() instanceof NoUserForkException) {
                    createFork(executor, context, upstreamRepositoryOwner, upstreamRepositoryName);
                    return;
                  }

                  executor.fail(CreateForkStep.this, context, error.getCause().getMessage());
                }
              });
    } else {
      // user fork has been cloned
      proceed(originRepositoryName, executor, context);
    }
  }

  private void createFork(
      final WorkflowExecutor executor,
      final Context context,
      final String upstreamRepositoryOwner,
      final String upstreamRepositoryName) {
    context
        .getVcsHostingService()
        .fork(upstreamRepositoryOwner, upstreamRepositoryName)
        .then(
            new Operation<Repository>() {
              @Override
              public void apply(Repository result) throws OperationException {
                proceed(result.getName(), executor, context);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError err) throws OperationException {
                final String errorMessage =
                    messages.stepCreateForkErrorCreatingFork(
                        upstreamRepositoryOwner,
                        upstreamRepositoryName,
                        err.getCause().getMessage());
                executor.fail(CreateForkStep.this, context, errorMessage);
              }
            });
  }

  private void proceed(
      final String forkName, final WorkflowExecutor executor, final Context context) {
    context.setForkedRepositoryName(forkName);
    executor.done(this, context);
  }
}

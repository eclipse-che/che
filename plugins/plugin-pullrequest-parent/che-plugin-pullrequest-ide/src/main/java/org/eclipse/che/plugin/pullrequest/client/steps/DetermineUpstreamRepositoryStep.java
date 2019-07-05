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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import org.eclipse.che.plugin.pullrequest.shared.dto.Repository;

/**
 * Determines what is the upstream repository and updates the context with {@link
 * Context#getUpstreamRepositoryOwner()} () repository owner} and {@link
 * Context#getUpstreamRepositoryName()} repository name}.
 *
 * <p>The algorithm is simple: if origin repository is user's fork then get its parent and set as
 * upstream repo, otherwise use {@link Context#getOriginRepositoryOwner() origin owner} and {@link
 * Context#getOriginRepositoryName() origin name}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class DetermineUpstreamRepositoryStep implements Step {

  private final NotificationManager notificationManager;

  @Inject
  public DetermineUpstreamRepositoryStep(NotificationManager notificationManager) {
    this.notificationManager = notificationManager;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    final VcsHostingService hostingService = context.getVcsHostingService();
    hostingService
        .getRepository(context.getOriginRepositoryOwner(), context.getOriginRepositoryName())
        .then(
            new Operation<Repository>() {
              @Override
              public void apply(Repository repo) throws OperationException {
                if (repo.isFork()
                    && context
                        .getOriginRepositoryOwner()
                        .equalsIgnoreCase(context.getHostUserLogin())) {
                  final String upstreamUrl = repo.getParent().getCloneUrl();
                  context.setUpstreamRepositoryName(
                      hostingService.getRepositoryNameFromUrl(upstreamUrl));
                  context.setUpstreamRepositoryOwner(
                      hostingService.getRepositoryOwnerFromUrl(upstreamUrl));
                } else {
                  context.setUpstreamRepositoryName(context.getOriginRepositoryName());
                  context.setUpstreamRepositoryOwner(context.getOriginRepositoryOwner());
                }
                executor.done(DetermineUpstreamRepositoryStep.this, context);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                executor.fail(DetermineUpstreamRepositoryStep.this, context, error.getMessage());
              }
            });
  }
}

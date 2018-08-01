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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsServiceProvider;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/**
 * This step initialize the contribution workflow context.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class InitializeWorkflowContextStep implements Step {

  private static final Predicate<Remote> ORIGIN_REMOTE_FILTER =
      new Predicate<Remote>() {
        @Override
        public boolean apply(Remote remote) {
          return remote.getName().equals("origin");
        }
      };

  private final VcsServiceProvider vcsServiceProvider;
  private final NotificationManager notificationManager;
  private final ContributeMessages messages;

  @Inject
  public InitializeWorkflowContextStep(
      final VcsServiceProvider vcsServiceProvider,
      final NotificationManager notificationManager,
      final ContributeMessages messages) {
    this.vcsServiceProvider = vcsServiceProvider;
    this.notificationManager = notificationManager;
    this.messages = messages;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    vcsServiceProvider
        .getVcsService(context.getProject())
        .listRemotes(context.getProject())
        .then(setUpOriginRepoOp(executor, context))
        .catchError(errorSettingUpOriginRepoOp(executor, context));
  }

  private Operation<List<Remote>> setUpOriginRepoOp(
      final WorkflowExecutor executor, final Context context) {
    return new Operation<List<Remote>>() {
      @Override
      public void apply(final List<Remote> remotes) throws OperationException {
        final Optional<Remote> remoteOpt =
            FluentIterable.from(remotes).filter(ORIGIN_REMOTE_FILTER).first();
        if (remoteOpt.isPresent()) {
          final Remote remote = remoteOpt.get();
          final String originUrl = remote.getUrl();
          final VcsHostingService vcsHostingService = context.getVcsHostingService();

          context.setOriginRepositoryOwner(vcsHostingService.getRepositoryOwnerFromUrl(originUrl));
          context.setOriginRepositoryName(vcsHostingService.getRepositoryNameFromUrl(originUrl));

          setContributeToBranchName(context);

          executor.done(InitializeWorkflowContextStep.this, context);
        } else {
          notificationManager.notify(
              messages.stepInitWorkflowOriginRemoteNotFound(), FAIL, FLOAT_MODE);
          executor.fail(
              InitializeWorkflowContextStep.this,
              context,
              messages.stepInitWorkflowOriginRemoteNotFound());
        }
      }
    };
  }

  protected void setContributeToBranchName(Context context) {
    String contributeToBranchName = getBranchFromProjectMetadata(context.getProject());

    if (contributeToBranchName != null) {
      context.setContributeToBranchName(contributeToBranchName);
      return;
    }

    vcsServiceProvider
        .getVcsService(context.getProject())
        .getBranchName(context.getProject())
        .then(
            (String branchName) -> {
              context.setContributeToBranchName(branchName);
              setBranchToProjectMetadata(context.getProject(), branchName);
            });
  }

  private String getBranchFromProjectMetadata(final ProjectConfig project) {
    final Map<String, List<String>> attrs = project.getAttributes();
    if (attrs.containsKey(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME)
        && !attrs.get(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME).isEmpty()) {
      return attrs.get(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME).get(0);
    }
    if (project.getSource() != null) {
      final String branchName = project.getSource().getParameters().get("branch");
      if (!isNullOrEmpty(branchName)) {
        return branchName;
      }
    }
    return null;
  }

  private void setBranchToProjectMetadata(final ProjectConfig project, String branchName) {
    project.getSource().getParameters().put("branch", branchName);
  }

  private Operation<PromiseError> errorSettingUpOriginRepoOp(
      final WorkflowExecutor executor, final Context context) {
    return new Operation<PromiseError>() {
      @Override
      public void apply(final PromiseError error) throws OperationException {
        notificationManager.notify(
            messages.contributorExtensionErrorSetupOriginRepository(error.getMessage()),
            FAIL,
            FLOAT_MODE);
        executor.fail(InitializeWorkflowContextStep.this, context, error.getMessage());
      }
    };
  }
}

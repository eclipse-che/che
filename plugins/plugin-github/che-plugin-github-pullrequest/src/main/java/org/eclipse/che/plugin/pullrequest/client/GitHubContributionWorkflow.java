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
package org.eclipse.che.plugin.pullrequest.client;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.plugin.pullrequest.client.steps.AddHttpForkRemoteStep;
import org.eclipse.che.plugin.pullrequest.client.steps.AddReviewFactoryLinkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.AuthorizeCodenvyOnVCSHostStep;
import org.eclipse.che.plugin.pullrequest.client.steps.CommitWorkingTreeStep;
import org.eclipse.che.plugin.pullrequest.client.steps.CreateForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DefineExecutionConfiguration;
import org.eclipse.che.plugin.pullrequest.client.steps.DefineWorkBranchStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetectPullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetermineUpstreamRepositoryStep;
import org.eclipse.che.plugin.pullrequest.client.steps.GenerateReviewFactoryStep;
import org.eclipse.che.plugin.pullrequest.client.steps.InitializeWorkflowContextStep;
import org.eclipse.che.plugin.pullrequest.client.steps.IssuePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnOriginStep;
import org.eclipse.che.plugin.pullrequest.client.steps.UpdatePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.ContributionWorkflow;
import org.eclipse.che.plugin.pullrequest.client.workflow.StepsChain;

/**
 * Declares steps of contribution workflow for GitHub repositories.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class GitHubContributionWorkflow implements ContributionWorkflow {

  private final InitializeWorkflowContextStep initializeWorkflowContextStep;
  private final DefineWorkBranchStep defineWorkBranchStep;
  private final CommitWorkingTreeStep commitWorkingTreeStep;
  private final AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep;
  private final DefineExecutionConfiguration defineExecutionConfiguration;
  private final DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep;
  private final CreateForkStep createForkStep;
  private final AddHttpForkRemoteStep addHttpForkRemoteStep;
  private final PushBranchOnForkStep pushBranchOnForkStep;
  private final PushBranchOnOriginStep pushBranchOnOriginStep;
  private final GenerateReviewFactoryStep generateReviewFactoryStep;
  private final AddReviewFactoryLinkStep addReviewFactoryLinkStep;
  private final IssuePullRequestStep issuePullRequestStep;
  private final UpdatePullRequestStep updatePullRequestStep;
  private final DetectPullRequestStep detectPullRequestStep;

  @Inject
  public GitHubContributionWorkflow(
      InitializeWorkflowContextStep initializeWorkflowContextStep,
      DefineWorkBranchStep defineWorkBranchStep,
      CommitWorkingTreeStep commitWorkingTreeStep,
      AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep,
      DefineExecutionConfiguration defineExecutionConfiguration,
      DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep,
      CreateForkStep createForkStep,
      AddHttpForkRemoteStep addHttpForkRemoteStep,
      PushBranchOnForkStep pushBranchOnForkStep,
      PushBranchOnOriginStep pushBranchOnOriginStep,
      GenerateReviewFactoryStep generateReviewFactoryStep,
      AddReviewFactoryLinkStep addReviewFactoryLinkStep,
      IssuePullRequestStep issuePullRequestStep,
      UpdatePullRequestStep updatePullRequestStep,
      DetectPullRequestStep detectPullRequestStep) {
    this.initializeWorkflowContextStep = initializeWorkflowContextStep;
    this.defineWorkBranchStep = defineWorkBranchStep;
    this.commitWorkingTreeStep = commitWorkingTreeStep;
    this.authorizeCodenvyOnVCSHostStep = authorizeCodenvyOnVCSHostStep;
    this.defineExecutionConfiguration = defineExecutionConfiguration;
    this.determineUpstreamRepositoryStep = determineUpstreamRepositoryStep;
    this.createForkStep = createForkStep;
    this.addHttpForkRemoteStep = addHttpForkRemoteStep;
    this.pushBranchOnForkStep = pushBranchOnForkStep;
    this.pushBranchOnOriginStep = pushBranchOnOriginStep;
    this.generateReviewFactoryStep = generateReviewFactoryStep;
    this.addReviewFactoryLinkStep = addReviewFactoryLinkStep;
    this.issuePullRequestStep = issuePullRequestStep;
    this.updatePullRequestStep = updatePullRequestStep;
    this.detectPullRequestStep = detectPullRequestStep;
  }

  @Override
  public StepsChain initChain(Context context) {
    return StepsChain.first(initializeWorkflowContextStep).then(defineWorkBranchStep);
  }

  @Override
  public StepsChain creationChain(final Context context) {
    return StepsChain.first(commitWorkingTreeStep)
        .then(authorizeCodenvyOnVCSHostStep)
        .then(defineExecutionConfiguration)
        .then(determineUpstreamRepositoryStep)
        .then(detectPullRequestStep)
        .thenChainIf(
            new Supplier<Boolean>() {
              @Override
              public Boolean get() {
                return context.isForkAvailable();
              }
            },
            StepsChain.first(createForkStep).then(addHttpForkRemoteStep).then(pushBranchOnForkStep),
            StepsChain.first(pushBranchOnOriginStep))
        .then(generateReviewFactoryStep)
        .thenIf(
            new Supplier<Boolean>() {
              @Override
              public Boolean get() {
                return context.getReviewFactoryUrl() != null;
              }
            },
            addReviewFactoryLinkStep)
        .then(issuePullRequestStep);
  }

  @Override
  public StepsChain updateChain(final Context context) {
    final StepsChain forkChain =
        StepsChain.firstIf(
                new Supplier<Boolean>() {
                  @Override
                  public Boolean get() {
                    return context.getForkedRemoteName() == null;
                  }
                },
                addHttpForkRemoteStep)
            .then(pushBranchOnForkStep);

    final StepsChain originChain = StepsChain.first(pushBranchOnOriginStep);

    return StepsChain.first(commitWorkingTreeStep)
        .then(authorizeCodenvyOnVCSHostStep)
        .thenChainIf(
            new Supplier<Boolean>() {
              @Override
              public Boolean get() {
                return context.isForkAvailable();
              }
            },
            forkChain,
            originChain)
        .then(updatePullRequestStep);
  }
}

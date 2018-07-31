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
package org.eclipse.che.plugin.pullrequest.client;

import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.StagesProvider;
import org.eclipse.che.plugin.pullrequest.client.steps.CommitWorkingTreeStep;
import org.eclipse.che.plugin.pullrequest.client.steps.CreateForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetectPullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.IssuePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnOriginStep;
import org.eclipse.che.plugin.pullrequest.client.steps.UpdatePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;

/**
 * Provides displayed stages for GitHub contribution workflow.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class GithubStagesProvider implements StagesProvider {

  private static final Set<Class<? extends Step>> UPDATE_ORIGIN_STEP_DONE_TYPES;
  private static final Set<Class<? extends Step>> UPDATE_FORK_STEP_DONE_TYPES;
  private static final Set<Class<? extends Step>> CREATION_ORIGIN_STEP_DONE_TYPES;
  private static final Set<Class<? extends Step>> CREATION_FORK_STEP_DONE_TYPES;

  static {
    UPDATE_FORK_STEP_DONE_TYPES =
        ImmutableSet.of(PushBranchOnForkStep.class, UpdatePullRequestStep.class);
    UPDATE_ORIGIN_STEP_DONE_TYPES =
        ImmutableSet.of(PushBranchOnOriginStep.class, UpdatePullRequestStep.class);
    CREATION_FORK_STEP_DONE_TYPES =
        ImmutableSet.of(
            CreateForkStep.class, PushBranchOnForkStep.class, IssuePullRequestStep.class);
    CREATION_ORIGIN_STEP_DONE_TYPES =
        ImmutableSet.of(PushBranchOnOriginStep.class, IssuePullRequestStep.class);
  }

  private final ContributeMessages messages;

  @Inject
  public GithubStagesProvider(final ContributeMessages messages) {
    this.messages = messages;
  }

  @Override
  public List<String> getStages(final Context context) {
    if (context.isUpdateMode()) {
      return asList(
          messages.contributePartStatusSectionNewCommitsPushedStepLabel(),
          messages.contributePartStatusSectionPullRequestUpdatedStepLabel());
    }
    if (context.isForkAvailable()) {
      return asList(
          messages.contributePartStatusSectionForkCreatedStepLabel(),
          messages.contributePartStatusSectionBranchPushedForkStepLabel(),
          messages.contributePartStatusSectionPullRequestIssuedStepLabel());
    } else {
      return asList(
          messages.contributePartStatusSectionBranchPushedOriginStepLabel(),
          messages.contributePartStatusSectionPullRequestIssuedStepLabel());
    }
  }

  @Override
  public Set<Class<? extends Step>> getStepDoneTypes(Context context) {
    if (context.isUpdateMode()) {
      return context.isForkAvailable()
          ? UPDATE_FORK_STEP_DONE_TYPES
          : UPDATE_ORIGIN_STEP_DONE_TYPES;
    }
    return context.isForkAvailable()
        ? CREATION_FORK_STEP_DONE_TYPES
        : CREATION_ORIGIN_STEP_DONE_TYPES;
  }

  @Override
  public Set<Class<? extends Step>> getStepErrorTypes(Context context) {
    return getStepDoneTypes(context);
  }

  @Override
  public Class<? extends Step> getDisplayStagesType(Context context) {
    return context.isUpdateMode() ? CommitWorkingTreeStep.class : DetectPullRequestStep.class;
  }
}

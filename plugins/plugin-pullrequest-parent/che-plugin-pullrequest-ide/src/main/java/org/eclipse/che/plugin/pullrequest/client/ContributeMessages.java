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
package org.eclipse.che.plugin.pullrequest.client;

import com.google.gwt.i18n.client.Messages;

/** Internationalizable messages for the contributor plugin. */
public interface ContributeMessages extends Messages {
  /*
   * Contribute part
   */
  @Key("contribute.part.title")
  String contributePartTitle();

  @Key("contribute.part.repository.section.title")
  String contributePartRepositorySectionTitle();

  @Key("contribute.part.configure.contribution.section.title")
  String contributePartConfigureContributionSectionTitle();

  @Key("contribute.part.configure.contribution.section.contribution.branch.name.label")
  String contributePartConfigureContributionSectionContributionBranchNameLabel();

  @Key(
      "contribute.part.configure.contribution.section.contribution.branch.name.create.new.item.text")
  String contributePartConfigureContributionSectionContributionBranchNameCreateNewItemText();

  @Key("contribute.part.configure.contribution.section.contribution.title.label")
  String contributePartConfigureContributionSectionContributionTitleLabel();

  @Key("contribute.part.configure.contribution.section.contribution.title.placeholder")
  String contributePartConfigureContributionSectionContributionTitlePlaceholder();

  @Key("contribute.part.configure.contribution.section.contribution.comment.label")
  String contributePartConfigureContributionSectionContributionCommentLabel();

  @Key("contribute.part.configure.contribution.section.contribution.comment.placeholder")
  String contributePartConfigureContributionSectionContributionCommentPlaceholder();

  @Key("contribute.part.configure.contribution.section.button.contribute.text")
  String contributePartConfigureContributionSectionButtonContributeText();

  @Key("contribute.part.configure.contribution.section.button.contribute.update.text")
  String contributePartConfigureContributionSectionButtonContributeUpdateText();

  @Key("contribute.part.status.section.title")
  String contributePartStatusSectionTitle();

  @Key("contribute.part.status.section.fork.created.step.label")
  String contributePartStatusSectionForkCreatedStepLabel();

  @Key("contribute.part.status.section.branch.pushed.fork.step.label")
  String contributePartStatusSectionBranchPushedForkStepLabel();

  @Key("contribute.part.status.section.branch.pushed.origin.step.label")
  String contributePartStatusSectionBranchPushedOriginStepLabel();

  @Key("contribute.part.status.section.new.commits.pushed.step.label")
  String contributePartStatusSectionNewCommitsPushedStepLabel();

  @Key("contribute.part.status.section.pull.request.issued.step.label")
  String contributePartStatusSectionPullRequestIssuedStepLabel();

  @Key("contribute.part.status.section.pull.request.updated.step.label")
  String contributePartStatusSectionPullRequestUpdatedStepLabel();

  @Key("contribute.part.status.section.contribution.created.message")
  String contributePartStatusSectionContributionCreatedMessage();

  @Key("contribute.part.status.section.contribution.updated.message")
  String contributePartStatusSectionContributionUpdatedMessage();

  @Key("contribute.part.new.contribution.section.button.open.pull.request.on.vcs.host.text")
  String contributePartNewContributionSectionButtonOpenPullRequestOnVcsHostText(String vcsHostName);

  @Key("contribute.part.new.contribution.section.button.new.text")
  String contributePartNewContributionSectionButtonNewText();

  @Key("contribute.part.new.contribution.contribute.branch.checked.out")
  String contributePartNewContributionContributeBranchCheckedOut(String contributeToBranchName);

  @Key("contribute.part.configure.contribution.dialog.update.title")
  String contributePartConfigureContributionDialogUpdateTitle();

  @Key("contribute.part.configure.contribution.dialog.update.text")
  String contributePartConfigureContributionDialogUpdateText(String branchName);

  @Key("contribute.part.configure.contribution.dialog.new.branch.title")
  String contributePartConfigureContributionDialogNewBranchTitle();

  @Key("contribute.part.configure.contribution.dialog.new.branch.label")
  String contributePartConfigureContributionDialogNewBranchLabel();

  @Key("contribute.part.configure.contribution.dialog.new.branch.error.branch.exists")
  String contributePartConfigureContributionDialogNewBranchErrorBranchExists(String branchName);

  @Key("contribute.part.configure.contribution.dialog.ssh.not.found.title")
  String contributePartConfigureContributionDialogSshNotFoundTitle();

  @Key("contribute.part.configure.contribution.dialog.ssh.not.found.text")
  String contributePartConfigureContributionDialogSshNotFoundText();

  /*
   * Commit dialog
   */
  @Key("commit.dialog.title")
  String commitDialogTitle();

  @Key("commit.dialog.message")
  String commitDialogMessage();

  @Key("commit.dialog.checkbox.include.untracked.text")
  String commitDialogCheckBoxIncludeUntracked();

  @Key("commit.dialog.description.title")
  String commitDialogDescriptionTitle();

  @Key("commit.dialog.button.ok.text")
  String commitDialogButtonOkText();

  @Key("commit.dialog.button.continue.text")
  String commitDialogButtonContinueText();

  @Key("commit.dialog.button.cancel.text")
  String commitDialogButtonCancelText();

  /*
   * Notification message prefix.
   */
  @Key("notification.message.prefix")
  String notificationMessagePrefix(String notificationMessage);

  @Key("step.check_branch_to_push.cloned_branch_is_equal_to_work_branch")
  String stepCheckBranchClonedBranchIsEqualToWorkBranch();

  @Key("step.detect_pr.pr_exists_title")
  String stepDetectPrExistsTitle();

  @Key("step.detect_pr.pr_exists_body")
  String stepDetectPrExistsTitle(String branch);
  /*
   * Init workflow step
   */
  @Key("step.init_workflow.remote_not_found")
  String stepInitWorkflowOriginRemoteNotFound();

  /*
   * Commit working tree step
   */
  @Key("step.commit.canceled")
  String stepCommitCanceled();

  /*
   * Define work branch step
   */
  @Key("step.define.work.branch.creating.work.branch")
  String stepDefineWorkBranchCreatingWorkBranch(String branchName);

  @Key("step.define.work.branch.work.branch.created")
  String stepDefineWorkBranchWorkBranchCreated(String branchName);

  /*
   * Checkout work branch to push step
   */
  @Key("step.checkout.branch.to.push.local.branch.checked.out")
  String stepCheckoutBranchToPushLocalBranchCheckedOut(String branchName);

  @Key("step.checkout.branch.to.push.error.list.local.branches")
  String stepCheckoutBranchToPushErrorListLocalBranches();

  @Key("step.checkout.branch.to.push.error.checkout.local.branch")
  String stepCheckoutBranchToPushErrorCheckoutLocalBranch();

  /*
   * Add fork remote step
   */
  @Key("step.add.fork.remote.error.add.fork")
  String stepAddForkRemoteErrorAddFork();

  @Key("step.add.fork.remote.error.set.forked.repository.remote")
  String stepAddForkRemoteErrorSetForkedRepositoryRemote();

  @Key("step.add.fork.remote.error.check.remotes")
  String stepAddForkRemoteErrorCheckRemote();

  /*
   * Create fork step
   */
  @Key("step.create.fork.error.creating.fork")
  String stepCreateForkErrorCreatingFork(String owner, String repository, String message);

  /*
   * Push branch fork step
   */
  @Key("step.push.branch.error.pushing.branch")
  String stepPushBranchErrorPushingBranch(String cause);

  @Key("step.push.branch.error.branch.up.to.date")
  String stepPushBranchErrorBranchUpToDate();

  @Key("step.push.branch.canceling")
  String stepPushBranchCanceling();

  /*
   * Add review factory link step
   */
  @Key("step.add.review.factory.link.error.adding.review.factory.link")
  String stepAddReviewFactoryLinkErrorAddingReviewFactoryLink();

  /*
   * Generate review factory step
   */
  @Key("step.generate.review.factory.error.create.factory")
  String stepGenerateReviewFactoryErrorCreateFactory();

  /*
   * Issue pull request step
   */
  @Key("step.issue.pull.request.error.create.pull.request")
  String stepIssuePullRequestErrorCreatePullRequest();

  @Key("step.issue.pull.request.error.create.pull.request.without.commits")
  String stepIssuePullRequestErrorCreatePullRequestWithoutCommits();

  /*
   * Authorize Codenvy on VCS Host step
   */
  @Key("step.authorize.codenvy.on.vcs.host.error.cannot.access.vcs.host.title")
  String stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHostTitle();

  @Key("step.authorize.codenvy.on.vcs.host.error.cannot.access.vcs.host.content")
  String stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHostContent();

  /*
   * Contributor extension
   */
  @Key("contributor.extension.error.updating.contribution.attributes")
  String contributorExtensionErrorUpdatingContributionAttributes(String exceptionMessage);

  @Key("contributor.extension.error.setOriginRepository")
  String contributorExtensionErrorSetupOriginRepository(String message);

  @Key("contributor.extension.default.commit.description")
  String contributorExtensionDefaultCommitDescription(String branchName, String contributionTitle);

  @Key("stub.text.project.not.provide.supported.vcs")
  String stubTextProjectNotProvideSupportedVCS();

  @Key("stub.text.project.is.not.selected")
  String stubTextProjectIsNotSelected();

  @Key("stub.text.loading")
  String stubTextLoading();

  @Key("stub.text.nothing.to.show")
  String stubTextNothingToShow();

  @Key("failed.to.get.vcs.service")
  String failedToGetVCSService(String project, String cause);

  @Key("failed.to.apply.vcs.mixin")
  String failedToApplyVCSMixin(String project, String cause);

  @Key("stub.text.should.be.selected.only.one.project")
  String stubTextShouldBeSelectedOnlyOneProject();

  // Switch contribute part action
  @Key("action.switch.contribute.part.displaying.title")
  String switchContributePartDisplayingTitle();

  @Key("action.switch.contribute.part.displaying.description")
  String switchContributePartDisplayingDescription();

  /*
   * Contribute part preferences
   */
  @Key("contribute.preferences.title")
  String contributePreferencesTitle();

  @Key("contribute.preferences.category")
  String contributePreferencesCategory();

  @Key("contribute.preferences.activateByProjectSelectionLabel")
  String contributePreferencesActivateByProjectSelectionLabel();

  @Key("contribute.preferences.notification.activatePart.text")
  String contributePreferencesNotificationActivatePartText();
}

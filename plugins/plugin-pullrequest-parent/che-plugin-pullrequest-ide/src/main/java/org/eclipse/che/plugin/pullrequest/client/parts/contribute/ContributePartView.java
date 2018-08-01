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
package org.eclipse.che.plugin.pullrequest.client.parts.contribute;

import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * Interface for the contribution configuration shown when the user decides to send their
 * contribution.
 */
public interface ContributePartView extends View<ContributePartView.ActionDelegate> {
  /** Set factory's repository URL. */
  void setRepositoryUrl(String url);

  /** Set factory's contribute to branch name. */
  void setContributeToBranch(String branch);

  /** Set project name. */
  void setProjectName(String projectName);

  /**
   * Returns the contribution branch name.
   *
   * @return the contribution branch name
   */
  String getContributionBranchName();

  /**
   * Sets the contribution branch name.
   *
   * @param branchName the contribution branch name.
   */
  void setContributionBranchName(String branchName);

  /**
   * Set the contribution branch name list.
   *
   * @param branchNames the branch name list.
   */
  void setContributionBranchNameList(List<String> branchNames);

  /** Sets the enabled/disabled state of the contribution branch name field. */
  void setContributionBranchNameEnabled(boolean enabled);

  /**
   * Returns the current content of the contribution comment.
   *
   * @return the comment.
   */
  String getContributionComment();

  /**
   * Sets the contribution comment.
   *
   * @param comment the contribution comment.
   */
  void setContributionComment(String comment);

  void addContributionCommentChangedHandler(TextChangedHandler handler);

  /** Sets the enabled/disabled state of the contribution comment field. */
  void setContributionCommentEnabled(boolean enabled);

  /**
   * Returns the contribution title.
   *
   * @return the title.
   */
  String getContributionTitle();

  /**
   * Sets the contribution title.
   *
   * @param title the contribution title.
   */
  void setContributionTitle(String title);

  void addContributionTitleChangedHandler(TextChangedHandler handler);

  void addBranchChangedHandler(TextChangedHandler changeHandler);

  /** Sets the enabled/disabled state of the contribution title field. */
  void setContributionTitleEnabled(boolean enabled);

  /**
   * Sets the contribution title input error state.
   *
   * @param showError {@code true} if the contribution title is in error, {@code false} otherwise.
   */
  void showContributionTitleError(boolean showError);

  /**
   * Sets the enabled/disabled state of the "Contribute" button.
   *
   * @param enabled true to enable, false to disable
   */
  void setContributeButtonEnabled(boolean enabled);

  /**
   * Sets the text displayed into the "Contribute" button.
   *
   * @param text the text to display
   */
  void setContributeButtonText(String text);

  /** Shows the status section. */
  void showStatusSection(String... statusSteps);

  /**
   * Sets the current status step state.
   *
   * @param success {@code true} if success, {@code false} otherwise.
   */
  void setCurrentStatusStepStatus(boolean success);

  /**
   * Shows the status section message.
   *
   * @param error {@code true} if the message displayed is an error, {@code false} otherwise.
   */
  void showStatusSectionMessage(String message, boolean error);

  /** Hides the status section message. */
  void hideStatusSectionMessage();

  /** Hides the status section. */
  void hideStatusSection();

  /**
   * Show the new contribution section.
   *
   * @param vcsHostName the VCS host name.
   */
  void showNewContributionSection(String vcsHostName);

  /**
   * Display stub with {@code content}
   *
   * @param content to display in the stub
   */
  void showStub(String content);

  /** Hide stub and show content panel. */
  void showContent();

  /** Hide the new contribution section. */
  void hideNewContributionSection();

  /**
   * Defines if the contribution is in progress.
   *
   * @param progress {@code true} if the contribution is in progress, {@code false} otherwise.
   */
  void setContributionProgressState(boolean progress);

  String getCurrentStatusStepName();

  /** Action delegate interface for the contribution configuration dialog. */
  interface ActionDelegate extends BaseActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the Contribute
     * button.
     */
    void onContribute();

    /**
     * Performs any action appropriate in response to the user having pressed the open pull request
     * on vcs host button.
     */
    void onOpenPullRequestOnVcsHost();

    /**
     * Performs any action appropriate in response to the user having pressed the start new
     * contribution button.
     */
    void onNewContribution();

    /**
     * Performs any action appropriate in response to the user having pressed the refresh
     * contribution branch names list button.
     */
    void onRefreshContributionBranchNameList();

    /**
     * Performs any action appropriate in response to the user having selected the create new branch
     * item.
     */
    void onCreateNewBranch();

    /** Performs any action when view state is modified. */
    void updateControls();
  }
}

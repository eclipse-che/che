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
package org.eclipse.che.plugin.pullrequest.client.workflow;

import static org.eclipse.che.plugin.pullrequest.client.events.ContextPropertyChangeEvent.ContextProperty.CONTRIBUTE_TO_BRANCH_NAME;
import static org.eclipse.che.plugin.pullrequest.client.events.ContextPropertyChangeEvent.ContextProperty.PROJECT;
import static org.eclipse.che.plugin.pullrequest.client.events.ContextPropertyChangeEvent.ContextProperty.WORK_BRANCH_NAME;

import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.pullrequest.client.events.ContextPropertyChangeEvent;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsService;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.shared.dto.Configuration;
import org.eclipse.che.plugin.pullrequest.shared.dto.PullRequest;

/**
 * Context used to share information between the steps in the contribution workflow.
 *
 * @author Kevin Pollet
 */
public class Context {
  /** The event bus. */
  private final EventBus eventBus;

  /** The project. */
  private ProjectConfig project;

  /** The name of the branch to contribute to. */
  private String contributeToBranchName;

  /** The name of the working branch. */
  private String workBranchName;

  /** The name of the user on host VCS. */
  private String hostUserLogin;

  /** The name of the owner of the repository forked on VCS. */
  private String upstreamRepositoryOwner;

  /** The name of the repository forked on VCS. */
  private String upstreamRepositoryName;

  /** The name of the owner of the repository cloned on VCS. */
  private String originRepositoryOwner;

  /** The name of the repository cloned on VCS. */
  private String originRepositoryName;

  /** The identifier of the pull request on the hosting service. */
  private PullRequest pullRequest;

  /** The issue number of the pull request issued for the contribution. */
  private String pullRequestIssueNumber;

  /** The generated review factory URL. */
  private String reviewFactoryUrl;

  /** The name of the forked remote. */
  private String forkedRemoteName;

  /** The name of the forked repository. */
  private String forkedRepositoryName;

  /** Defines availability of fork creation. */
  private boolean forkAvailable;

  /** Defines ability to use ssh URLs. */
  private boolean sshAvailable;

  private VcsHostingService vcsHostingService;

  /** The name of the origin remote. */
  private String originRemoteName;

  private WorkflowStatus status;
  private WorkflowStatus previousStatus;
  private Configuration configuration;
  public ViewState viewState;
  private VcsService vcsService;

  public Context(final EventBus eventBus) {
    this.eventBus = eventBus;
    viewState = new ViewState();
  }

  public ProjectConfig getProject() {
    return project;
  }

  public void setProject(final ProjectConfig project) {
    final ProjectConfig oldValue = this.project;
    this.project = project;

    fireContextPropertyChange(PROJECT, oldValue, project);
  }

  public String getContributeToBranchName() {
    return contributeToBranchName;
  }

  public void setContributeToBranchName(final String contributeToBranchName) {
    final String oldValue = this.contributeToBranchName;
    this.contributeToBranchName = contributeToBranchName;

    fireContextPropertyChange(CONTRIBUTE_TO_BRANCH_NAME, oldValue, contributeToBranchName);
  }

  public String getWorkBranchName() {
    return workBranchName;
  }

  public void setWorkBranchName(final String workBranchName) {
    final String oldValue = this.workBranchName;
    this.workBranchName = workBranchName;

    fireContextPropertyChange(WORK_BRANCH_NAME, oldValue, workBranchName);
  }

  public String getHostUserLogin() {
    return hostUserLogin;
  }

  public void setHostUserLogin(final String hostUserLogin) {
    this.hostUserLogin = hostUserLogin;
  }

  public String getUpstreamRepositoryOwner() {
    return upstreamRepositoryOwner;
  }

  public void setUpstreamRepositoryOwner(String upstreamRepositoryOwner) {
    this.upstreamRepositoryOwner = upstreamRepositoryOwner;
  }

  public String getUpstreamRepositoryName() {
    return upstreamRepositoryName;
  }

  public void setUpstreamRepositoryName(String upstreamRepositoryName) {
    this.upstreamRepositoryName = upstreamRepositoryName;
  }

  public String getOriginRepositoryOwner() {
    return originRepositoryOwner;
  }

  public void setOriginRepositoryOwner(final String originRepositoryOwner) {
    final String oldValue = this.originRepositoryOwner;
    this.originRepositoryOwner = originRepositoryOwner;

    fireContextPropertyChange(
        ContextPropertyChangeEvent.ContextProperty.ORIGIN_REPOSITORY_OWNER,
        oldValue,
        originRepositoryOwner);
  }

  public String getOriginRepositoryName() {
    return originRepositoryName;
  }

  public void setOriginRepositoryName(final String originRepositoryName) {
    final String oldValue = this.originRepositoryName;
    this.originRepositoryName = originRepositoryName;

    fireContextPropertyChange(
        ContextPropertyChangeEvent.ContextProperty.ORIGIN_REPOSITORY_NAME,
        oldValue,
        originRepositoryName);
  }

  public PullRequest getPullRequest() {
    return pullRequest;
  }

  public void setPullRequest(PullRequest pullRequest) {
    this.pullRequest = pullRequest;
  }

  /**
   * Return the issue number of the pull request issued for this contribution.
   *
   * @return the pull request issue id
   */
  public String getPullRequestIssueNumber() {
    return pullRequestIssueNumber;
  }

  /**
   * Sets the issue number of the pull request issued for this contribution.
   *
   * @param pullRequestIssueNumber the new value
   */
  public void setPullRequestIssueNumber(final String pullRequestIssueNumber) {
    this.pullRequestIssueNumber = pullRequestIssueNumber;
  }

  /**
   * Returns the generated review factory URL (if available).
   *
   * @return factory URL
   */
  public String getReviewFactoryUrl() {
    return this.reviewFactoryUrl;
  }

  /**
   * Sets the generated review factory URL (if available).
   *
   * @param reviewFactoryUrl new value
   */
  public void setReviewFactoryUrl(final String reviewFactoryUrl) {
    this.reviewFactoryUrl = reviewFactoryUrl;
  }

  public String getOriginRemoteName() {
    return originRemoteName;
  }

  public void setOriginRemoteName(String originRemoteName) {
    this.originRemoteName = originRemoteName;
  }

  public String getForkedRemoteName() {
    return forkedRemoteName;
  }

  public void setForkedRemoteName(String forkedRemoteName) {
    this.forkedRemoteName = forkedRemoteName;
  }

  public String getForkedRepositoryName() {
    return forkedRepositoryName;
  }

  public void setForkedRepositoryName(String forkedRepositoryName) {
    this.forkedRepositoryName = forkedRepositoryName;
  }

  private void fireContextPropertyChange(
      final ContextPropertyChangeEvent.ContextProperty contextProperty,
      final Object oldValue,
      final Object newValue) {
    if (!Objects.equals(oldValue, newValue)) {
      eventBus.fireEvent(new ContextPropertyChangeEvent(this, contextProperty));
    }
  }

  public boolean isUpdateMode() {
    return status == WorkflowStatus.UPDATING_PR || status == WorkflowStatus.READY_TO_UPDATE_PR;
  }

  public VcsHostingService getVcsHostingService() {
    return vcsHostingService;
  }

  public void setVcsHostingService(VcsHostingService service) {
    this.vcsHostingService = service;
  }

  @Nullable
  public WorkflowStatus getStatus() {
    return status;
  }

  @Nullable
  public WorkflowStatus getPreviousStatus() {
    return previousStatus;
  }

  public void setStatus(@Nullable WorkflowStatus status) {
    this.previousStatus = this.status;
    this.status = status;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public ViewState getViewState() {
    return viewState;
  }

  public void setVcsService(VcsService vcsService) {
    this.vcsService = vcsService;
  }

  public VcsService getVcsService() {
    return vcsService;
  }

  public boolean isForkAvailable() {
    return forkAvailable;
  }

  public void setForkAvailable(boolean forkAvailable) {
    this.forkAvailable = forkAvailable;
  }

  public boolean isSshAvailable() {
    return sshAvailable;
  }

  public void setSshAvailable(boolean sshAvailable) {
    this.sshAvailable = sshAvailable;
  }

  public static final class ViewState {

    private String contributionTitle;
    private String contributionComment;
    private StatusMessage statusMessage;
    private List<Stage> stages;
    private int currentStage;

    private ViewState() {
      currentStage = 0;
    }

    public void setStatusMessage(String message, boolean error) {
      this.statusMessage = new StatusMessage(message, error);
    }

    public void setStatusMessage(StatusMessage message) {
      this.statusMessage = message;
    }

    public void setContributionTitle(String title) {
      this.contributionTitle = title;
    }

    public String getContributionTitle() {
      return contributionTitle;
    }

    public void setContributionComment(String contributionComment) {
      this.contributionComment = contributionComment;
    }

    public String getContributionComment() {
      return contributionComment;
    }

    public List<Stage> getStages() {
      if (stages == null) {
        stages = new ArrayList<>(3);
      }
      return stages;
    }

    public List<String> getStageNames() {
      final List<String> statusNames = new ArrayList<>(getStages().size());
      for (Stage stepStatus : getStages()) {
        statusNames.add(stepStatus.getName());
      }
      return statusNames;
    }

    public List<Boolean> getStageValues() {
      final List<Boolean> statusNames = new ArrayList<>(getStages().size());
      for (Stage stepStatus : getStages()) {
        statusNames.add(stepStatus.getStatus());
      }
      return statusNames;
    }

    public void resetStages() {
      getStages().clear();
      currentStage = 0;
    }

    public void setStep(String name, Boolean value) {
      getStages().add(new Stage(name, value));
    }

    public void setStages(List<String> stages) {
      resetStages();
      for (String newStage : stages) {
        setStep(newStage, null);
      }
    }

    public StatusMessage getStatusMessage() {
      return statusMessage;
    }

    public void setStageDone(boolean stepDone) {
      getStages().get(currentStage++).setStatus(stepDone);
    }

    public static class StatusMessage {
      private final boolean error;
      private final String message;

      private StatusMessage(String message, boolean error) {
        this.message = message;
        this.error = error;
      }

      public boolean isError() {
        return error;
      }

      public String getMessage() {
        return message;
      }
    }

    public static class Stage {
      private final String name;

      private Boolean status;

      public Stage(String name, Boolean status) {
        this.status = status;
        this.name = name;
      }

      public String getName() {
        return name;
      }

      public Boolean getStatus() {
        return status;
      }

      public void setStatus(Boolean status) {
        this.status = status;
      }
    }
  }
}

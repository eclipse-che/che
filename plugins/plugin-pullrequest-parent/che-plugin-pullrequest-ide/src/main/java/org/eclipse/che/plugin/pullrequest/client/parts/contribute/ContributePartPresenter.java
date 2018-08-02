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

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.asList;
import static org.eclipse.che.ide.api.constraints.Constraints.LAST;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;
import org.eclipse.che.ide.util.browser.BrowserUtils;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.ContributeResources;
import org.eclipse.che.plugin.pullrequest.client.events.ContextInvalidatedEvent;
import org.eclipse.che.plugin.pullrequest.client.events.ContextInvalidatedHandler;
import org.eclipse.che.plugin.pullrequest.client.events.ContextPropertyChangeEvent;
import org.eclipse.che.plugin.pullrequest.client.events.ContextPropertyChangeHandler;
import org.eclipse.che.plugin.pullrequest.client.events.CurrentContextChangedEvent;
import org.eclipse.che.plugin.pullrequest.client.events.CurrentContextChangedHandler;
import org.eclipse.che.plugin.pullrequest.client.events.StepEvent;
import org.eclipse.che.plugin.pullrequest.client.events.StepHandler;
import org.eclipse.che.plugin.pullrequest.client.steps.CommitWorkingTreeStep;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowStatus;
import org.eclipse.che.providers.DynaObject;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Part for the contribution configuration.
 *
 * @author Kevin Pollet
 */
@Singleton
@DynaObject
public class ContributePartPresenter extends BasePresenter
    implements ContributePartView.ActionDelegate,
        StepHandler,
        ContextPropertyChangeHandler,
        CurrentContextChangedHandler,
        ContextInvalidatedHandler {
  private final ContributePartView view;
  private final ContributeResources resources;
  private final WorkspaceAgent workspaceAgent;
  private final ContributeMessages messages;
  private final WorkflowExecutor workflowExecutor;
  private final AppContext appContext;
  private final NotificationManager notificationManager;
  private final DialogFactory dialogFactory;
  private final Map<String, StagesProvider> stagesProviders;

  @Inject
  public ContributePartPresenter(
      final ContributePartView view,
      final ContributeMessages messages,
      final ContributeResources resources,
      final WorkspaceAgent workspaceAgent,
      final EventBus eventBus,
      final WorkflowExecutor workflow,
      final AppContext appContext,
      final NotificationManager notificationManager,
      final DialogFactory dialogFactory,
      final Map<String, StagesProvider> stagesProviders) {
    this.view = view;
    this.resources = resources;
    this.workspaceAgent = workspaceAgent;
    this.workflowExecutor = workflow;
    this.messages = messages;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
    this.dialogFactory = dialogFactory;
    this.stagesProviders = stagesProviders;

    this.view.setDelegate(this);

    view.addContributionTitleChangedHandler(
        new TextChangedHandler() {
          @Override
          public void onTextChanged(String newText) {
            final Context curContext = workflowExecutor.getCurrentContext();
            curContext.getViewState().setContributionTitle(newText);
          }
        });

    view.addContributionCommentChangedHandler(
        new TextChangedHandler() {
          @Override
          public void onTextChanged(String newText) {
            final Context curContext = workflowExecutor.getCurrentContext();
            curContext.getViewState().setContributionComment(newText);
          }
        });

    view.addBranchChangedHandler(
        new TextChangedHandler() {
          @Override
          public void onTextChanged(String branchName) {
            final Context curContext = workflowExecutor.getCurrentContext();
            if (!branchName.equals(
                    messages
                        .contributePartConfigureContributionSectionContributionBranchNameCreateNewItemText())
                && !branchName.equals(curContext.getWorkBranchName())) {
              checkoutBranch(curContext, branchName, false);
            }
          }
        });

    eventBus.addHandler(StepEvent.TYPE, this);
    eventBus.addHandler(ContextPropertyChangeEvent.TYPE, this);
    eventBus.addHandler(CurrentContextChangedEvent.TYPE, this);
    eventBus.addHandler(ContextInvalidatedEvent.TYPE, this);
  }

  public void open() {
    resetView();
    workspaceAgent.openPart(ContributePartPresenter.this, TOOLING, LAST);
  }

  public void remove() {
    workspaceAgent.removePart(ContributePartPresenter.this);
  }

  @Override
  public void onContribute() {
    final Context context = workflowExecutor.getCurrentContext();
    context.getViewState().setStatusMessage(null);
    context.getViewState().resetStages();

    updateView(
        context,
        new NewContributionPanelUpdate(),
        new StatusMessageUpdate(),
        new StatusSectionUpdate());

    // Extract configuration values and perform contribution
    if (isCurrentContext(context)) {
      context
          .getConfiguration()
          .withContributionBranchName(view.getContributionBranchName())
          .withContributionComment(view.getContributionComment())
          .withContributionTitle(view.getContributionTitle());
    }

    if (context.isUpdateMode()) {
      workflowExecutor.updatePullRequest(context);
    } else {
      workflowExecutor.createPullRequest(context);
    }

    updateView(context, new ContributionButtonUpdate(messages));
  }

  private void restore(final Context context) {
    final List<ViewUpdate> updates = new ArrayList<>();
    // Repository panel updates
    updates.add(new RepositoryUrlUpdate());
    updates.add(new ClonedBranchUpdate());
    updates.add(new ProjectNameUpdate());
    // All the other panels are available only if the mode is different from INITIALIZING
    // All the other panels are hidden by the #open method, which is called before initialization
    if (context.getStatus() != WorkflowStatus.INITIALIZING) {
      // Configuration panel updates
      updates.add(new WorkBranchUpdate());
      updates.add(new ContributionTitleUpdate());
      updates.add(new ContributionCommentUpdate());
      // Contribution button update
      updates.add(new ContributionButtonUpdate(messages));
      // Status panel updates
      updates.add(new StatusSectionUpdate());
      updates.add(new StatusMessageUpdate());
      // New contribution panel updates
      updates.add(new NewContributionPanelUpdate());
    }
    updateView(context, updates);
  }

  /** Continuously resets the view state as long as current project is not changed. */
  private void resetView() {
    final Project project = appContext.getRootProject();
    final String projectName = project != null ? project.getName() : null;
    if (!isCurrentProject(projectName)) return;
    view.setRepositoryUrl("");

    if (!isCurrentProject(projectName)) return;
    view.setContributeToBranch("");

    if (!isCurrentProject(projectName)) return;
    view.setContributionBranchName("");

    if (!isCurrentProject(projectName)) return;
    view.setContributionBranchNameEnabled(true);

    if (!isCurrentProject(projectName)) return;
    view.setContributionBranchNameList(Collections.<String>emptyList());

    if (!isCurrentProject(projectName)) return;
    view.setContributionTitle("");

    if (!isCurrentProject(projectName)) return;
    view.setProjectName("");

    if (!isCurrentProject(projectName)) return;
    view.setContributionTitleEnabled(true);

    if (!isCurrentProject(projectName)) return;
    view.setContributionComment("");

    if (!isCurrentProject(projectName)) return;
    view.setContributionCommentEnabled(true);

    if (!isCurrentProject(projectName)) return;
    view.setContributeButtonText(
        messages.contributePartConfigureContributionSectionButtonContributeText());

    if (!isCurrentProject(projectName)) return;
    view.hideStatusSection();

    if (!isCurrentProject(projectName)) return;
    view.hideNewContributionSection();

    if (!isCurrentProject(projectName)) return;
    updateControls();
  }

  @Override
  public SVGResource getTitleImage() {
    return resources.titleIcon();
  }

  @Override
  public void onOpenPullRequestOnVcsHost() {
    final Context context = workflowExecutor.getCurrentContext();

    BrowserUtils.openInNewTab(
        context
            .getVcsHostingService()
            .makePullRequestUrl(
                context.getOriginRepositoryOwner(),
                context.getOriginRepositoryName(),
                context.getPullRequestIssueNumber()));
  }

  @Override
  public void onNewContribution() {
    final Context context = workflowExecutor.getCurrentContext();
    context
        .getVcsService()
        .checkoutBranch(
            context.getProject(),
            context.getContributeToBranchName(),
            false,
            new AsyncCallback<String>() {
              @Override
              public void onFailure(final Throwable exception) {
                notificationManager.notify(exception.getMessage(), FAIL, FLOAT_MODE);
              }

              @Override
              public void onSuccess(final String branchName) {
                resetView();
                workflowExecutor.invalidateContext(context.getProject());
                workflowExecutor.init(context.getVcsHostingService(), context.getProject());
              }
            });
  }

  @Override
  public void onRefreshContributionBranchNameList() {
    updateView(workflowExecutor.getCurrentContext(), new WorkBranchUpdate());
  }

  @Override
  public void onCreateNewBranch() {
    final Context context = workflowExecutor.getCurrentContext();
    dialogFactory
        .createInputDialog(
            messages.contributePartConfigureContributionDialogNewBranchTitle(),
            messages.contributePartConfigureContributionDialogNewBranchLabel(),
            new CreateNewBranchCallback(context),
            new CancelNewBranchCallback(context))
        .withValidator(new BranchNameValidator())
        .show();
  }

  @Override
  public void updateControls() {
    final String contributionTitle = view.getContributionTitle();

    boolean isValid = true;
    view.showContributionTitleError(false);

    if (contributionTitle == null || contributionTitle.trim().isEmpty()) {
      view.showContributionTitleError(true);
      isValid = false;
    }

    view.setContributeButtonEnabled(isValid);
  }

  @Override
  public void go(final AcceptsOneWidget container) {
    container.setWidget(view.asWidget());
  }

  @NotNull
  @Override
  public String getTitle() {
    return messages.contributePartTitle();
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Nullable
  @Override
  public String getTitleToolTip() {
    return messages.contributePartTitle();
  }

  @Override
  public int getSize() {
    return 350;
  }

  @Override
  public void onStepDone(final StepEvent event) {
    final Class<? extends Step> stepClass = event.getStep().getClass();
    final Context context = event.getContext();

    // if it is necessarily to display stages on this step
    if (getProvider(context).getDisplayStagesType(context) == stepClass) {
      context.getViewState().setStages(getProvidedStages(context));
      updateView(context, new StatusSectionUpdate());
    }

    // if current step is in list of provided stages types
    // then this step is done and view should be affected
    if (!context.getViewState().getStages().isEmpty()
        && getProvidedStepDoneTypes(context).contains(stepClass)) {
      context.getViewState().setStageDone(true);
      updateView(context, new DisplayCurrentStepResultUpdate(true));
    } else if (stepClass == WorkflowExecutor.ChangeContextStatusStep.class) {
      if (context.getStatus() == WorkflowStatus.READY_TO_UPDATE_PR) {
        final List<ViewUpdate> updates = new ArrayList<>();
        // Display status message
        final String message;
        if (context.getPreviousStatus() == WorkflowStatus.CREATING_PR) {
          message = messages.contributePartStatusSectionContributionCreatedMessage();
        } else {
          message = messages.contributePartStatusSectionContributionUpdatedMessage();
        }
        context.getViewState().setStatusMessage(message, false);
        updates.add(new StatusMessageUpdate());

        // Contribution button
        updates.add(new ContributionButtonUpdate(messages));

        // Config panel
        updates.add(new ContributionTitleUpdate());
        updates.add(new ContributionCommentUpdate());

        // New contribution panel
        updates.add(new NewContributionPanelUpdate());

        updateView(context, updates);
      }
    }
  }

  @Override
  public void onStepError(final StepEvent event) {
    final Step step = event.getStep();
    final Class<? extends Step> stepClass = step.getClass();
    final Context context = event.getContext();
    if (stepClass == CommitWorkingTreeStep.class) {
      if (!context.isUpdateMode()) {
        context.getViewState().resetStages();
        context.getViewState().setStatusMessage(null);
        updateView(
            context,
            new StatusSectionUpdate(),
            new StatusMessageUpdate(),
            new NewContributionPanelUpdate(),
            new ContributionButtonUpdate(messages));
      } else {
        context.getViewState().resetStages();
        context.getViewState().setStatusMessage(event.getMessage(), true);
        updateView(
            context,
            new StatusSectionUpdate(),
            new StatusMessageUpdate(),
            new ContributionButtonUpdate(messages));
      }
    } else if (getProvidedStepErrorTypes(context).contains(stepClass)) {
      context.getViewState().setStageDone(false);
      context.getViewState().setStatusMessage(event.getMessage(), true);
      updateView(
          context,
          new DisplayCurrentStepResultUpdate(false),
          new StatusMessageUpdate(),
          new ContributionButtonUpdate(messages));
    } else {
      context.getViewState().resetStages();
      restore(context);
      Log.error(ContributePartPresenter.class, "Step error: ", event.getMessage());
    }
  }

  @Override
  public void onContextPropertyChange(final ContextPropertyChangeEvent event) {
    final Context context = event.getContext();

    switch (event.getContextProperty()) {
      case CONTRIBUTE_TO_BRANCH_NAME:
        updateView(context, new ClonedBranchUpdate());
        break;

      case WORK_BRANCH_NAME:
        updateView(context, new WorkBranchUpdate());
        break;

      case ORIGIN_REPOSITORY_NAME:
      case ORIGIN_REPOSITORY_OWNER:
        updateView(context, new RepositoryUrlUpdate());
        break;

      case PROJECT:
        updateView(context, new ProjectNameUpdate());
        break;

      default:
        // nothing to do
        break;
    }
  }

  @Override
  public void onContextChanged(final Context context) {
    restore(context);
  }

  private void updateView(final Context context, final ViewUpdate... updates) {
    updateView(context, asList(updates));
  }

  private void updateView(final Context context, final List<ViewUpdate> updates) {
    for (Iterator<ViewUpdate> it = updates.iterator();
        it.hasNext() && isCurrentContext(context); ) {
      it.next().update(view, context);
    }
  }

  private void updateView(final Context context, final ViewUpdate update) {
    if (isCurrentContext(context)) {
      update.update(view, context);
    }
  }

  @Override
  public void onContextInvalidated(Context context) {
    resetView();
  }

  public void showStub(String stubContent) {
    view.showStub(stubContent);
  }

  public void showContent() {
    view.showContent();
  }

  /**
   * Defines a single update operation. Single update operations are required as view is shared
   * between multiple projects. If context is switched view should not be updated with the updates
   * related to the previous context.
   */
  private interface ViewUpdate {
    void update(final ContributePartView view, final Context context);
  }

  private static class DisplayCurrentStepResultUpdate implements ViewUpdate {
    private final boolean result;

    public DisplayCurrentStepResultUpdate(boolean result) {
      this.result = result;
    }

    @Override
    public void update(final ContributePartView view, final Context context) {
      view.setCurrentStatusStepStatus(result);
    }
  }

  private static class NewContributionPanelUpdate implements ViewUpdate {
    @Override
    public void update(final ContributePartView view, final Context context) {
      view.hideNewContributionSection();
      if (context.isUpdateMode()) {
        view.showNewContributionSection(context.getVcsHostingService().getName());
      }
    }
  }

  private static class StatusMessageUpdate implements ViewUpdate {
    @Override
    public void update(final ContributePartView view, final Context context) {
      view.hideStatusSectionMessage();
      final Context.ViewState.StatusMessage statusMessage =
          context.getViewState().getStatusMessage();
      if (statusMessage != null) {
        view.showStatusSectionMessage(statusMessage.getMessage(), statusMessage.isError());
      }
    }
  }

  private static class StatusSectionUpdate implements ViewUpdate {
    @Override
    public void update(ContributePartView view, Context context) {
      view.hideStatusSection();
      final List<Context.ViewState.Stage> stepStatuses = context.getViewState().getStages();
      if (stepStatuses.size() > 0) {
        final String[] names =
            context.getViewState().getStageNames().toArray(new String[stepStatuses.size()]);
        view.showStatusSection(names);
        for (Boolean stepStatus : context.getViewState().getStageValues()) {
          if (stepStatus == null) {
            break;
          }
          view.setCurrentStatusStepStatus(stepStatus);
        }
      } else if (context.getViewState().getStatusMessage() != null) {
        view.showStatusSection();
      }
    }
  }

  private static class ClonedBranchUpdate implements ViewUpdate {
    @Override
    public void update(final ContributePartView view, final Context context) {
      view.setContributeToBranch(nullToEmpty(context.getContributeToBranchName()));
    }
  }

  private static class ContributionButtonUpdate implements ViewUpdate {
    private final ContributeMessages messages;

    private ContributionButtonUpdate(final ContributeMessages messages) {
      this.messages = messages;
    }

    @Override
    public void update(final ContributePartView view, final Context context) {
      final boolean isEnabled =
          !nullToEmpty(context.getViewState().getContributionTitle()).isEmpty();
      final boolean isInProgress;
      final String buttonText;
      switch (context.getStatus()) {
        case UPDATING_PR:
          buttonText =
              messages.contributePartConfigureContributionSectionButtonContributeUpdateText();
          isInProgress = true;
          break;
        case READY_TO_UPDATE_PR:
          buttonText =
              messages.contributePartConfigureContributionSectionButtonContributeUpdateText();
          isInProgress = false;
          break;
        case CREATING_PR:
          buttonText = messages.contributePartConfigureContributionSectionButtonContributeText();
          isInProgress = true;
          break;
        case READY_TO_CREATE_PR:
          buttonText = messages.contributePartConfigureContributionSectionButtonContributeText();
          isInProgress = false;
          break;
        default:
          throw new IllegalStateException("Illegal workflow status " + context.getStatus());
      }
      view.setContributeButtonText(buttonText);
      view.setContributionProgressState(isInProgress);
      view.setContributeButtonEnabled(isEnabled);
    }
  }

  private class ContributionCommentUpdate implements ViewUpdate {
    @Override
    public void update(final ContributePartView view, final Context context) {
      view.setContributionComment(nullToEmpty(context.getViewState().getContributionComment()));
      view.setContributionCommentEnabled(context.getStatus() == WorkflowStatus.READY_TO_CREATE_PR);
    }
  }

  private static class ContributionTitleUpdate implements ViewUpdate {
    @Override
    public void update(final ContributePartView view, final Context context) {
      view.setContributionTitle(nullToEmpty(context.getViewState().getContributionTitle()));
      view.setContributionTitleEnabled(context.getStatus() == WorkflowStatus.READY_TO_CREATE_PR);
    }
  }

  private static class RepositoryUrlUpdate implements ViewUpdate {
    @Override
    public void update(final ContributePartView view, final Context context) {
      final String originRepositoryName = context.getOriginRepositoryName();
      final String originRepositoryOwner = context.getOriginRepositoryOwner();
      if (originRepositoryName != null && originRepositoryOwner != null) {
        view.setRepositoryUrl(
            context
                .getVcsHostingService()
                .makeHttpRemoteUrl(originRepositoryOwner, originRepositoryName));
      }
    }
  }

  private static class WorkBranchUpdate implements ViewUpdate {
    @Override
    public void update(final ContributePartView view, final Context context) {
      context
          .getVcsService()
          .listLocalBranches(
              context.getProject(),
              new AsyncCallback<List<Branch>>() {
                @Override
                public void onFailure(final Throwable notUsed) {}

                @Override
                public void onSuccess(final List<Branch> branches) {
                  final List<String> branchNames = new ArrayList<>();
                  for (final Branch oneBranch : branches) {
                    branchNames.add(oneBranch.getDisplayName());
                  }
                  view.setContributionBranchNameList(branchNames);
                  view.setContributionBranchName(context.getWorkBranchName());
                }
              });
    }
  }

  private static class ProjectNameUpdate implements ViewUpdate {
    @Override
    public void update(final ContributePartView view, final Context context) {
      view.setProjectName(context.getProject().getName());
    }
  }

  private boolean isCurrentContext(final Context context) {
    final Project project = appContext.getRootProject();

    return project != null && Objects.equals(context.getProject().getName(), project.getName());
  }

  private boolean isCurrentProject(final String projectName) {
    final Project project = appContext.getRootProject();

    return project != null && Objects.equals(projectName, project.getName());
  }

  private StagesProvider getProvider(final Context context) {
    for (Map.Entry<String, StagesProvider> entry : stagesProviders.entrySet()) {
      if (entry.getKey().equals(context.getVcsHostingService().getName())) {
        return entry.getValue();
      }
    }
    throw new IllegalStateException(
        "StagesProvider for VCS hosting service "
            + context.getVcsHostingService().getName()
            + " isn't registered");
  }

  private List<String> getProvidedStages(final Context context) {
    return getProvider(context).getStages(context);
  }

  private Set<Class<? extends Step>> getProvidedStepDoneTypes(final Context context) {
    return getProvider(context).getStepDoneTypes(context);
  }

  private Set<Class<? extends Step>> getProvidedStepErrorTypes(final Context context) {
    return getProvider(context).getStepErrorTypes(context);
  }

  private static class BranchNameValidator implements InputValidator {
    private static final Violation ERROR_WITH_NO_MESSAGE =
        new InputValidator.Violation() {
          @Nullable
          @Override
          public String getMessage() {
            return "";
          }

          @Nullable
          @Override
          public String getCorrectedValue() {
            return null;
          }
        };

    @Nullable
    @Override
    public Violation validate(final String branchName) {
      return branchName.matches("[0-9A-Za-z-]+") ? null : ERROR_WITH_NO_MESSAGE;
    }
  }

  private class CreateNewBranchCallback implements InputCallback {
    private final Context context;

    public CreateNewBranchCallback(final Context context) {
      this.context = context;
    }

    @Override
    public void accepted(final String branchName) {
      context
          .getVcsService()
          .isLocalBranchWithName(
              context.getProject(),
              branchName,
              new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(final Throwable exception) {
                  notificationManager.notify(exception.getMessage(), FAIL, FLOAT_MODE);
                }

                @Override
                public void onSuccess(final Boolean branchExists) {
                  if (branchExists) {
                    notificationManager.notify(
                        messages
                            .contributePartConfigureContributionDialogNewBranchErrorBranchExists(
                                branchName),
                        FAIL,
                        FLOAT_MODE);

                  } else {
                    checkoutBranch(context, branchName, true);
                  }
                }
              });
    }
  }

  private void checkoutBranch(
      final Context context, final String branchName, final boolean createNew) {
    context
        .getVcsService()
        .checkoutBranch(
            context.getProject(),
            branchName,
            createNew,
            new AsyncCallback<String>() {
              @Override
              public void onFailure(final Throwable exception) {
                notificationManager.notify(exception.getLocalizedMessage(), FAIL, FLOAT_MODE);
              }

              @Override
              public void onSuccess(final String notUsed) {
                workflowExecutor.invalidateContext(context.getProject());
                workflowExecutor.init(context.getVcsHostingService(), context.getProject());
              }
            });
  }

  private class CancelNewBranchCallback implements CancelCallback {
    private final Context context;

    private CancelNewBranchCallback(final Context context) {
      this.context = context;
    }

    @Override
    public void cancelled() {
      updateView(context, new WorkBranchUpdate());
    }
  }
}

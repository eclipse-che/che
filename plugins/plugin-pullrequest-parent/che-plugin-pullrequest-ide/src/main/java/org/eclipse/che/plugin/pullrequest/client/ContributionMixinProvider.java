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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;
import static org.eclipse.che.ide.api.parts.PartStack.State.HIDDEN;
import static org.eclipse.che.ide.api.parts.PartStack.State.MINIMIZED;
import static org.eclipse.che.ide.api.parts.PartStack.State.NORMAL;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;
import static org.eclipse.che.plugin.pullrequest.client.preference.ContributePreferencePresenter.ACTIVATE_BY_PROJECT_SELECTION;
import static org.eclipse.che.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME;
import static org.eclipse.che.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.factory.FactoryAcceptedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStack.State;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionChangedEvent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.ui.smartTree.data.HasDataObject;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.ContributePartPresenter;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsService;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsServiceProvider;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingServiceProvider;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/**
 * Responsible for setting up contribution mixin for the currently selected project in application
 * context.
 *
 * @author Vlad Zhukovskyi
 * @since 5.0.0
 */
@Singleton
public class ContributionMixinProvider {

  private final EventBus eventBus;
  private final AppContext appContext;
  private final WorkspaceAgent workspaceAgent;
  private final PreferencesManager preferencesManager;
  private final ContributePartPresenter contributePart;
  private final WorkflowExecutor workflowExecutor;
  private final VcsServiceProvider vcsServiceProvider;
  private final VcsHostingServiceProvider vcsHostingServiceProvider;
  private final PromiseProvider promiseProvider;
  private final ContributeMessages messages;

  private HandlerRegistration selectionHandlerReg;

  private Project lastSelected;

  @Inject
  public ContributionMixinProvider(
      EventBus eventBus,
      AppContext appContext,
      WorkspaceAgent workspaceAgent,
      PreferencesManager preferencesManager,
      ContributePartPresenter contributePart,
      WorkflowExecutor workflowExecutor,
      VcsServiceProvider vcsServiceProvider,
      VcsHostingServiceProvider vcsHostingServiceProvider,
      PromiseProvider promiseProvider,
      ContributeMessages messages) {
    this.eventBus = eventBus;
    this.appContext = appContext;
    this.workspaceAgent = workspaceAgent;
    this.preferencesManager = preferencesManager;
    this.contributePart = contributePart;
    this.workflowExecutor = workflowExecutor;
    this.vcsServiceProvider = vcsServiceProvider;
    this.vcsHostingServiceProvider = vcsHostingServiceProvider;
    this.promiseProvider = promiseProvider;
    this.messages = messages;

    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this::onWorkspaceStopped);
    eventBus.addHandler(PartStackStateChangedEvent.TYPE, this::onPartStackStateChanged);

    if (appContext.getFactory() != null) {
      eventBus.addHandler(FactoryAcceptedEvent.TYPE, event -> subscribeToSelectionChangedEvent());
    } else {
      eventBus.addHandler(
          WorkspaceReadyEvent.getType(), event -> subscribeToSelectionChangedEvent());
    }
  }

  private void onPartStackStateChanged(PartStackStateChangedEvent event) {
    PartStack toolingPartStack = workspaceAgent.getPartStack(TOOLING);
    if (event.getPartStack() != toolingPartStack) {
      return;
    }

    if (isPartStackHidden(toolingPartStack)) {
      invalidateContext(lastSelected);
      return;
    }

    PartPresenter activePart = toolingPartStack.getActivePart();
    if (activePart == null || activePart == contributePart) {
      handleCurrentProject();
    }
  }

  private void onSelectionChanged(Selection<?> selection) {
    WorkspaceStatus workspaceStatus = appContext.getWorkspace().getStatus();
    if (RUNNING != workspaceStatus || !isSupportedSelection(selection) || !isActivationAllowed()) {
      return;
    }

    if (appContext.getRootProject() != null) {
      handleCurrentProject();
      return;
    }

    String message =
        selection.isMultiSelection()
            ? messages.stubTextShouldBeSelectedOnlyOneProject()
            : messages.stubTextProjectIsNotSelected();

    contributePart.showStub(message);
    invalidateContext(lastSelected);
  }

  private void handleCurrentProject() {
    Project project = appContext.getRootProject();
    if (project == null) {
      invalidateContext(lastSelected);
      contributePart.showStub(messages.stubTextProjectIsNotSelected());
      return;
    }

    if (hasVcsService(project)) {
      initializeContributionWorkflow(project);
    } else {
      invalidateContext(project);
      contributePart.showStub(messages.stubTextProjectNotProvideSupportedVCS());
    }
  }

  private void initializeContributionWorkflow(Project project) {
    if (lastSelected != null && lastSelected.equals(project) && hasVcsService(lastSelected)) {
      return;
    }

    lastSelected = project;
    vcsHostingServiceProvider
        .getVcsHostingService(project)
        .then(
            vcsHostingService -> {
              provideMixin(project)
                  .then(
                      updatedProject -> {
                        workflowExecutor.init(vcsHostingService, updatedProject);
                        lastSelected = updatedProject;
                        setContributePart();
                      })
                  .catchError(
                      error -> {
                        handleVCSError(
                            project,
                            messages.failedToApplyVCSMixin(project.getName(), error.getMessage()));
                      });
            })
        .catchError(
            error -> {
              handleVCSError(
                  project, messages.failedToGetVCSService(project.getName(), error.getMessage()));
            });
  }

  private void setContributePart() {
    PartStack partStack = workspaceAgent.getPartStack(TOOLING);
    if (!partStack.containsPart(contributePart)) {
      partStack.addPart(contributePart, FIRST);
    }

    PartPresenter activePart = partStack.getActivePart();
    if (activePart != contributePart) {
      partStack.setActivePart(contributePart);
    }

    contributePart.showContent();
  }

  private Promise<Project> provideMixin(Project project) {
    VcsService vcsService = vcsServiceProvider.getVcsService(project);
    if (!hasVcsService(project) || hasContributionMixin(project)) {
      return promiseProvider.resolve(project);
    }

    return vcsService
        .getBranchName(project)
        .thenPromise(
            branchName -> {
              MutableProjectConfig mutableConfig = new MutableProjectConfig(project);
              mutableConfig.getMixins().add(CONTRIBUTION_PROJECT_TYPE_ID);
              mutableConfig
                  .getAttributes()
                  .put(CONTRIBUTE_TO_BRANCH_VARIABLE_NAME, singletonList(branchName));

              return project.update().withBody(mutableConfig).send();
            });
  }

  private void handleVCSError(Project project, String logError) {
    Log.error(getClass(), logError);
    invalidateContext(project);
    contributePart.showStub(messages.stubTextNothingToShow());
  }

  private void invalidateContext(Project project) {
    if (project != null) {
      final Optional<Context> context = workflowExecutor.getContext(project.getName());
      if (context.isPresent()) {
        workflowExecutor.invalidateContext(context.get().getProject());
      }
    }
    lastSelected = null;
  }

  private boolean hasVcsService(Project project) {
    return vcsServiceProvider.getVcsService(project) != null;
  }

  private boolean hasContributionMixin(Project project) {
    return project.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID);
  }

  private boolean isPartStackHidden(PartStack partStack) {
    State partStackState = partStack.getPartStackState();
    return partStackState == HIDDEN || partStackState == MINIMIZED;
  }

  private boolean isSupportedSelection(Selection<?> selection) {
    if (selection instanceof Selection.NoSelectionProvided) {
      return false;
    }

    Object headElem = selection.getHeadElement();

    boolean isResourceSelection = headElem instanceof Resource;
    boolean isHasDataWithResource =
        headElem instanceof HasDataObject
            && ((HasDataObject) headElem).getData() instanceof Resource;

    return headElem == null || isResourceSelection || isHasDataWithResource;
  }

  private boolean isActivationAllowed() {
    State toolingPartStackState = workspaceAgent.getPartStack(TOOLING).getPartStackState();
    if (MINIMIZED == toolingPartStackState) {
      return false;
    }

    if (NORMAL == toolingPartStackState) {
      return true;
    }

    String preference = preferencesManager.getValue(ACTIVATE_BY_PROJECT_SELECTION);
    return isNullOrEmpty(preference) || parseBoolean(preference);
  }

  private void subscribeToSelectionChangedEvent() {
    selectionHandlerReg =
        eventBus.addHandler(
            SelectionChangedEvent.TYPE, event -> onSelectionChanged(event.getSelection()));
  }

  private void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    lastSelected = null;
    contributePart.showStub("");
    if (selectionHandlerReg != null) {
      selectionHandlerReg.removeHandler();
    }
  }
}

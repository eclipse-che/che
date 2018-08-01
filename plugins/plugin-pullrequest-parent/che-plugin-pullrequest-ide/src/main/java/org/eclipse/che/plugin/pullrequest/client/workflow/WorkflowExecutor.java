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

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowStatus.CREATING_PR;
import static org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowStatus.INITIALIZING;
import static org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowStatus.READY_TO_CREATE_PR;
import static org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowStatus.READY_TO_UPDATE_PR;
import static org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowStatus.UPDATING_PR;

import com.google.common.base.Optional;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.pullrequest.client.events.ContextInvalidatedEvent;
import org.eclipse.che.plugin.pullrequest.client.events.CurrentContextChangedEvent;
import org.eclipse.che.plugin.pullrequest.client.events.StepEvent;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsServiceProvider;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.shared.dto.Configuration;

/**
 * This class is responsible for maintaining the context between the different steps and to maintain
 * the state of the contribution workflow.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkflowExecutor {

  private final EventBus eventBus;
  private final AppContext appContext;
  private final VcsServiceProvider vcsServiceProvider;
  private final DtoFactory dtoFactory;
  private final Map<String, Context> projectNameToContextMap;
  private final Map<String, ChainExecutor> projectNameToChainExecutorMap;
  private final Map<String, ContributionWorkflow> hostingServiceToWorkflowMap;

  @Inject
  public WorkflowExecutor(
      final EventBus eventBus,
      final DtoFactory dtoFactory,
      final AppContext appContext,
      final VcsServiceProvider vcsServiceProvider,
      final Map<String, ContributionWorkflow> workflowMap) {
    this.eventBus = eventBus;
    this.dtoFactory = dtoFactory;
    this.appContext = appContext;
    this.vcsServiceProvider = vcsServiceProvider;
    this.projectNameToContextMap = new HashMap<>();
    this.projectNameToChainExecutorMap = new HashMap<>();
    this.hostingServiceToWorkflowMap = workflowMap;
  }

  /**
   * Should be invoked when step execution is successful.
   *
   * @param step step which execution is done
   * @param context execution context
   */
  public void done(final Step step, final Context context) {
    if (!(step instanceof SyntheticStep)) {
      eventBus.fireEvent(new StepEvent(context, step, true));
    }
    executeNextStep(context);
  }

  /**
   * Should be invoked when step execution is failed.
   *
   * @param step step which execution is failed
   * @param context execution context
   * @param message error message
   */
  public void fail(final Step step, final Context context, final String message) {
    // restore context status from the processing to stable
    // the simple implementation of the status spec
    Log.error(getClass(), "Exec error " + step.getClass() + ", msg: " + message);
    switch (context.getStatus()) {
      case INITIALIZING:
        invalidateContext(context.getProject());
        break;
      case CREATING_PR:
        context.setStatus(READY_TO_CREATE_PR);
        break;
      case UPDATING_PR:
        context.setStatus(READY_TO_UPDATE_PR);
        break;
      default:
        break;
    }
    if (!(step instanceof SyntheticStep)) {
      eventBus.fireEvent(new StepEvent(context, step, false, message));
    }
  }

  /**
   * Initializes {@link ContributionWorkflow} provided by {@code vcsHistingService}. If context for
   * such project already initialized then it either invalidates context when vcs changes are
   * detected, or fires {@link CurrentContextChangedEvent} otherwise.
   *
   * @param vcsHostingService VCS hosting service based on project origin remote
   * @param project project for which initialization should be performed
   */
  public void init(final VcsHostingService vcsHostingService, final ProjectConfig project) {
    final Optional<Context> contextOpt = getContext(project.getName());
    if (!contextOpt.isPresent()) {
      doInit(vcsHostingService, project);
    } else {
      checkVcsState(contextOpt.get())
          .then(
              new Operation<Boolean>() {
                @Override
                public void apply(Boolean stateChanged) throws OperationException {
                  if (stateChanged) {
                    invalidateContext(contextOpt.get().getProject());
                    doInit(vcsHostingService, project);
                  } else {
                    eventBus.fireEvent(new CurrentContextChangedEvent(contextOpt.get()));
                  }
                }
              });
    }
  }

  /**
   * Returns an {@link Optional} describing the context for project with name {@code projectName},
   * or an empty {@code Optional} if context doesn't exist.
   */
  public Optional<Context> getContext(final String projectName) {
    return fromNullable(projectNameToContextMap.get(projectName));
  }

  /** Returns the context based on current project in {@link AppContext}. */
  public Context getCurrentContext() {
    return getContext(getCurrentProject().getName()).get();
  }

  /**
   * Executes {@link ContributionWorkflow#creationChain(Context)} based on given {@code context}.
   *
   * @param context project for which pull request should be created
   */
  public void createPullRequest(final Context context) {
    context.setStatus(CREATING_PR);
    final StepsChain contributeChain =
        getWorkflow(context)
            .creationChain(context)
            .then(new ChangeContextStatusStep(CREATING_PR, READY_TO_UPDATE_PR));
    projectNameToChainExecutorMap.put(
        context.getProject().getName(), new ChainExecutor(contributeChain));
    executeNextStep(context);
  }

  /**
   * Executes {@link ContributionWorkflow#updateChain(Context)} based on given {@code context}.
   *
   * @param context project for which pull request should be updated
   */
  public void updatePullRequest(final Context context) {
    context.setStatus(UPDATING_PR);
    final StepsChain updateChain =
        getWorkflow(context)
            .updateChain(context)
            .then(new ChangeContextStatusStep(UPDATING_PR, READY_TO_UPDATE_PR));
    projectNameToChainExecutorMap.put(
        context.getProject().getName(), new ChainExecutor(updateChain));
    executeNextStep(context);
  }

  /**
   * Invalidates context for given {@code project}. If any {@link ChainExecutor} exists for context
   * which is invalidated then executor will be removed and the next chain step won't be performed.
   *
   * <p>Fires {@link ContextInvalidatedEvent} if context for given project exists.
   *
   * @param project project for which context should be invalidated
   */
  public void invalidateContext(final ProjectConfig project) {
    final Optional<Context> contextOpt = getContext(project.getName());
    if (contextOpt.isPresent()) {
      projectNameToContextMap.remove(project.getName());
      projectNameToChainExecutorMap.remove(project.getName());
      eventBus.fireEvent(new ContextInvalidatedEvent(contextOpt.get()));
    }
  }

  private void doInit(final VcsHostingService vcsHostingService, final ProjectConfig project) {
    final Context context = new Context(eventBus);
    context.setVcsHostingService(vcsHostingService);
    context.setVcsService(vcsServiceProvider.getVcsService(project));
    context.setProject(project);
    context.setConfiguration(dtoFactory.createDto(Configuration.class));
    context.setStatus(INITIALIZING);
    projectNameToContextMap.put(project.getName(), context);

    // executes init steps chain for vcs hosting service workflow
    final StepsChain initChain =
        getWorkflow(context)
            .initChain(context)
            .then(new ChangeContextStatusStep(INITIALIZING, READY_TO_CREATE_PR));
    projectNameToChainExecutorMap.put(project.getName(), new ChainExecutor(initChain));
    executeNextStep(context);
  }

  private ProjectConfig getCurrentProject() {
    return appContext.getRootProject();
  }

  private Promise<Boolean> checkVcsState(final Context context) {
    return context
        .getVcsService()
        .getBranchName(context.getProject())
        .then(
            new Function<String, Boolean>() {
              @Override
              public Boolean apply(String branchName) throws FunctionException {
                return !branchName.equals(context.getWorkBranchName());
              }
            });
  }

  private ContributionWorkflow getWorkflow(Context context) {
    final String serviceName = context.getVcsHostingService().getName();
    final ContributionWorkflow strategy = hostingServiceToWorkflowMap.get(serviceName);
    if (strategy == null) {
      throw new IllegalArgumentException(
          "There is no contribution strategy for the '" + serviceName + "' service");
    }
    return strategy;
  }

  private void executeNextStep(final Context context) {
    final ChainExecutor executor = getExecutor(context);
    if (executor != null) {
      executor.execute(this, context);
    }
  }

  private ChainExecutor getExecutor(final Context context) {
    return projectNameToChainExecutorMap.get(context.getProject().getName());
  }

  public static class ChangeContextStatusStep implements Step {

    private final WorkflowStatus from;
    private final WorkflowStatus to;

    public ChangeContextStatusStep(final WorkflowStatus status, final WorkflowStatus to) {
      this.from = status;
      this.to = to;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
      if (context.getStatus() == from) {
        context.setStatus(to);
      }
      executor.done(this, context);
    }
  }
}

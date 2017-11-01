/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOPPED_BY;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an internal API for managing {@link RuntimeImpl} instances.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceRuntimes {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRuntimes.class);

  private final ImmutableMap<String, RuntimeInfrastructure> infraByRecipe;

  private final ConcurrentMap<String, RuntimeState> runtimes;
  private final EventService eventService;
  private final WorkspaceSharedPool sharedPool;
  private final WorkspaceDao workspaceDao;
  private final AtomicBoolean isStartRefused;
  private final Map<String, InternalEnvironmentFactory> environmentFactories;

  @Inject
  public WorkspaceRuntimes(
      EventService eventService,
      Map<String, InternalEnvironmentFactory> environmentFactories,
      Set<RuntimeInfrastructure> infrastructures,
      WorkspaceSharedPool sharedPool,
      WorkspaceDao workspaceDao,
      @SuppressWarnings("unused") DBInitializer ignored) {
    this.runtimes = new ConcurrentHashMap<>();
    this.eventService = eventService;
    this.sharedPool = sharedPool;
    this.workspaceDao = workspaceDao;
    this.isStartRefused = new AtomicBoolean(false);

    // TODO: consider extracting to a strategy interface(1. pick the last, 2. fail with conflict)
    Map<String, RuntimeInfrastructure> tmp = new HashMap<>();
    for (RuntimeInfrastructure infra : infrastructures) {
      for (String type : infra.getRecipeTypes()) {
        LOG.info("Register infrastructure '{}' recipe type '{}'", infra.getName(), type);
        RuntimeInfrastructure existingInfra = tmp.put(type, infra);
        if (existingInfra != null) {
          LOG.warn(
              "Both '{}' and '{}' infrastructures support recipe of type '{}', infrastructure '{}' will be used",
              infra.getName(),
              existingInfra.getName(),
              type,
              infra.getName());
        }
      }
    }
    infraByRecipe = ImmutableMap.copyOf(tmp);
    this.environmentFactories = ImmutableMap.copyOf(environmentFactories);
  }

  @PostConstruct
  private void init() {
    subscribeCleanupOnAbnormalRuntimeStopEvent();
    recover();
  }

  public void validate(Environment environment)
      throws NotFoundException, InfrastructureException, ValidationException {

    String type = environment.getRecipe().getType();
    if (!infraByRecipe.containsKey(type)) {
      throw new NotFoundException("Infrastructure not found for type: " + type);
    }
    environmentFactory(type).create(environment);

  }

  /**
   * Injects runtime information such as status and {@link
   * org.eclipse.che.api.core.model.workspace.Runtime} into the workspace object, if the workspace
   * doesn't have runtime sets the status to {@link WorkspaceStatus#STOPPED}.
   *
   * @param workspace the workspace to inject runtime into
   */
  public void injectRuntime(WorkspaceImpl workspace) {
    RuntimeState runtimeState = runtimes.get(workspace.getId());
    if (runtimeState != null) {
      workspace.setRuntime(new RuntimeImpl(runtimeState.runtime));
      workspace.setStatus(runtimeState.status);
    } else {
      workspace.setStatus(STOPPED);
    }
  }

  /**
   * Gets workspace status by its identifier.
   *
   * @param workspaceId workspace identifier
   */
  public WorkspaceStatus getStatus(String workspaceId) {
    RuntimeState state = runtimes.get(workspaceId);
    if (state != null) {
      return state.status;
    }
    return STOPPED;
  }

  /**
   * Starts all machines from specified workspace environment, creates workspace runtime instance
   * based on that environment.
   *
   * <p>During the start of the workspace its runtime is visible with {@link
   * WorkspaceStatus#STARTING} status.
   *
   * @param workspace workspace which environment should be started
   * @param envName the name of the environment to start
   * @param options whether machines should be recovered(true) or not(false)
   * @return completable future of start execution.
   * @throws ConflictException when workspace is already running
   * @throws ConflictException when start is interrupted
   * @throws NotFoundException when any not found exception occurs during environment start
   * @throws ServerException other error occurs during environment start
   * @see WorkspaceStatus#STARTING
   * @see WorkspaceStatus#RUNNING
   */
  public CompletableFuture<Void> startAsync(
      Workspace workspace, String envName, Map<String, String> options)
      throws ConflictException, NotFoundException, ServerException {

    final EnvironmentImpl environment = copyEnv(workspace, envName);
    final String workspaceId = workspace.getId();

    requireNonNull(environment, "Environment should not be null " + workspaceId);
    requireNonNull(environment.getRecipe(), "Recipe should not be null " + workspaceId);
    requireNonNull(
        environment.getRecipe().getType(), "Recipe type should not be null " + workspaceId);

    RuntimeInfrastructure infra = infraByRecipe.get(environment.getRecipe().getType());
    if (infra == null) {
      throw new NotFoundException(
          "No infrastructure found of type: "
              + environment.getRecipe().getType()
              + " for workspace: "
              + workspaceId);
    }

    RuntimeState existingState = runtimes.get(workspaceId);
    if (existingState != null) {
      throw new ConflictException(
          format(
              "Could not start workspace '%s' because its state is '%s'",
              workspaceId, existingState.status));
    }

    Subject subject = EnvironmentContext.getCurrent().getSubject();
    RuntimeIdentity runtimeId =
        new RuntimeIdentityImpl(workspaceId, envName, subject.getUserName());
    try {
      InternalEnvironment internalEnvironment = environmentFactory(environment.getRecipe().getType())
          .create(environment);
      RuntimeContext runtimeContext = infra.prepare(runtimeId, internalEnvironment);

      InternalRuntime runtime = runtimeContext.getRuntime();
      if (runtime == null) {
        throw new IllegalStateException(
            "SPI contract violated. RuntimeInfrastructure.start(...) must not return null: "
                + RuntimeInfrastructure.class);
      }
      RuntimeState state = new RuntimeState(runtime, STARTING);
      if (isStartRefused.get()) {
        throw new ConflictException(
            format(
                "Start of the workspace '%s' is rejected by the system, "
                    + "no more workspaces are allowed to start",
                workspace.getConfig().getName()));
      }
      if (runtimes.putIfAbsent(workspaceId, state) != null) {
        throw new ConflictException(
            "Could not start workspace '" + workspaceId + "' because it is not in 'STOPPED' state");
      }
      LOG.info(
          "Starting workspace '{}/{}' with id '{}' by user '{}'",
          workspace.getNamespace(),
          workspace.getConfig().getName(),
          workspace.getId(),
          sessionUserNameOr("undefined"));

      publishWorkspaceStatusEvent(workspaceId, STARTING, STOPPED, null);

      return CompletableFuture.runAsync(
          ThreadLocalPropagateContext.wrap(new StartRuntimeTask(workspace, options, runtime)),
          sharedPool.getExecutor());
    } catch (ValidationException e) {
      LOG.error(e.getLocalizedMessage(), e);
      throw new ConflictException(e.getLocalizedMessage());
    } catch (InfrastructureException e) {
      LOG.error(e.getLocalizedMessage(), e);
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  private class StartRuntimeTask implements Runnable {
    private final Workspace workspace;
    private final Map<String, String> options;
    private final InternalRuntime runtime;

    public StartRuntimeTask(
        Workspace workspace, Map<String, String> options, InternalRuntime runtime) {
      this.workspace = workspace;
      this.options = options;
      this.runtime = runtime;
    }

    @Override
    public void run() {
      String workspaceId = workspace.getId();
      try {
        runtime.start(options);
        runtimes.replace(workspaceId, new RuntimeState(runtime, RUNNING));

        LOG.info(
            "Workspace '{}:{}' with id '{}' started by user '{}'",
            workspace.getNamespace(),
            workspace.getConfig().getName(),
            workspaceId,
            sessionUserNameOr("undefined"));
        publishWorkspaceStatusEvent(workspaceId, RUNNING, STARTING, null);
      } catch (InfrastructureException e) {
        runtimes.remove(workspaceId);
        String failureCause = "failed";
        if (e instanceof RuntimeStartInterruptedException) {
          failureCause = "interrupted";
        }
        LOG.info(
            "Workspace '{}:{}' with id '{}' start {}",
            workspace.getNamespace(),
            workspace.getConfig().getName(),
            workspaceId,
            failureCause);
        // InfrastructureException is supposed to be an exception that can't be solved
        // by Che admin, so should not be logged (but not InternalInfrastructureException).
        // It will prevent bothering the admin when user made a mistake in WS configuration.
        if (e instanceof InternalInfrastructureException) {
          LOG.error(e.getLocalizedMessage(), e);
        }
        publishWorkspaceStatusEvent(workspaceId, STOPPED, STARTING, e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Stops running workspace runtime asynchronously.
   *
   * <p>Stops environment in an implementation specific way. During the stop of the workspace its
   * runtime is accessible with {@link WorkspaceStatus#STOPPING stopping} status. Workspace may be
   * stopped only if its status is {@link WorkspaceStatus#RUNNING} or {@link
   * WorkspaceStatus#STARTING}.
   *
   * @param workspace workspace which runtime should be stopped
   * @throws NotFoundException when workspace with specified identifier does not have runtime
   * @throws ConflictException when running workspace status is different from {@link
   *     WorkspaceStatus#RUNNING} or {@link WorkspaceStatus#STARTING}
   * @see WorkspaceStatus#STOPPING
   */
  public CompletableFuture<Void> stopAsync(Workspace workspace, Map<String, String> options)
      throws NotFoundException, ConflictException {
    String workspaceId = workspace.getId();
    RuntimeState state = runtimes.get(workspaceId);
    if (state == null) {
      throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
    }
    if (state.status != RUNNING && state.status != STARTING) {
      throw new ConflictException(
          format(
              "Could not stop workspace '%s' because its state is '%s'",
              workspaceId, state.status));
    }
    if (!runtimes.replace(workspaceId, state, new RuntimeState(state.runtime, STOPPING))) {
      RuntimeState newState = runtimes.get(workspaceId);
      WorkspaceStatus status = newState != null ? newState.status : STOPPED;
      throw new ConflictException(
          format("Could not stop workspace '%s' because its state is '%s'", workspaceId, status));
    }
    String stoppedBy =
        firstNonNull(
            sessionUserNameOr(workspace.getAttributes().get(WORKSPACE_STOPPED_BY)), "undefined");
    LOG.info(
        "Workspace '{}/{}' with id '{}' is being stopped by user '{}'",
        workspace.getNamespace(),
        workspace.getConfig().getName(),
        workspace.getId(),
        stoppedBy);
    publishWorkspaceStatusEvent(workspaceId, STOPPING, state.status, null);

    return CompletableFuture.runAsync(
        ThreadLocalPropagateContext.wrap(new StopRuntimeTask(workspace, options, stoppedBy, state)),
        sharedPool.getExecutor());
  }

  private class StopRuntimeTask implements Runnable {
    private final Workspace workspace;
    private final Map<String, String> options;
    private final String stoppedBy;
    private final RuntimeState state;

    public StopRuntimeTask(
        Workspace workspace, Map<String, String> options, String stoppedBy, RuntimeState state) {
      this.workspace = workspace;
      this.options = options;
      this.stoppedBy = stoppedBy;
      this.state = state;
    }

    @Override
    public void run() {
      String workspaceId = workspace.getId();
      try {
        state.runtime.stop(options);

        // remove before firing an event to have consistency between state and the event
        runtimes.remove(workspaceId);
        LOG.info(
            "Workspace '{}/{}' with id '{}' stopped by user '{}'",
            workspace.getNamespace(),
            workspace.getConfig().getName(),
            workspaceId,
            stoppedBy);
        publishWorkspaceStatusEvent(workspaceId, STOPPED, STOPPING, null);
      } catch (InfrastructureException e) {
        // remove before firing an event to have consistency between state and the event
        runtimes.remove(workspaceId);
        LOG.info(
            "Error occurs on workspace '{}/{}' with id '{}' stopped by user '{}'. Error: {}",
            workspace.getNamespace(),
            workspace.getConfig().getName(),
            workspaceId,
            stoppedBy,
            e);
        publishWorkspaceStatusEvent(
            workspaceId,
            STOPPED,
            STOPPING,
            "Error occurs on workspace runtime stop. Error: " + e.getMessage());
        // InfrastructureException is supposed to be an exception that can't be solved
        // by Che admin, so should not be logged (but not InternalInfrastructureException).
        // It will prevent bothering the admin when user made a mistake in WS configuration.
        if (e instanceof InternalInfrastructureException) {
          LOG.error(e.getLocalizedMessage(), e);
        }
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Returns true if workspace was started and its status is {@link WorkspaceStatus#RUNNING
   * running}, {@link WorkspaceStatus#STARTING starting} or {@link WorkspaceStatus#STOPPING
   * stopping} - otherwise returns false.
   *
   * @param workspaceId workspace identifier to perform check
   * @return true if workspace is running, otherwise false
   */
  public boolean hasRuntime(String workspaceId) {
    return runtimes.containsKey(workspaceId);
  }

  @VisibleForTesting
  void recover() {
    for (RuntimeInfrastructure infra : new HashSet<>(infraByRecipe.values())) {
      try {
        for (RuntimeIdentity identity : infra.getIdentities()) {
          recoverOne(infra, identity);
        }
      } catch (UnsupportedOperationException x) {
        LOG.warn("Not recoverable infrastructure: '{}'", infra.getName());
      } catch (InternalInfrastructureException x) {
        LOG.error(
            format(
                "An error occurred while attempted to recover runtimes using infrastructure '%s'",
                infra.getName()),
            x);
      } catch (ServerException | InfrastructureException x) {
        LOG.error(
            "An error occurred while attempted to recover runtimes using infrastructure '{}'. Reason: '{}'",
            infra.getName(),
            x.getMessage());
      }
    }
  }

  @VisibleForTesting
  void recoverOne(RuntimeInfrastructure infra, RuntimeIdentity identity) throws ServerException {
    Workspace workspace;
    try {
      workspace = workspaceDao.get(identity.getWorkspaceId());
    } catch (NotFoundException x) {
      LOG.error(
          "Workspace configuration is missing for the runtime '{}:{}'. Runtime won't be recovered",
          identity.getWorkspaceId(),
          identity.getEnvName());
      return;
    }

    Environment environment = workspace.getConfig().getEnvironments().get(identity.getEnvName());
    if (environment == null) {
      LOG.error(
          "Environment configuration is missing for the runtime '{}:{}'. Runtime won't be recovered",
          identity.getWorkspaceId(),
          identity.getEnvName());
      return;
    }

    InternalRuntime runtime;
    try {
      InternalEnvironment internalEnvironment = environmentFactory(environment.getRecipe().getType())
          .create(environment);
      runtime = infra.prepare(identity, internalEnvironment).getRuntime();
    } catch (InfrastructureException | ValidationException | NotFoundException x) {
      LOG.error(
          "Couldn't recover runtime '{}:{}'. Error: {}",
          identity.getWorkspaceId(),
          identity.getEnvName(),
          x.getMessage());
      return;
    }

    RuntimeState prev =
        runtimes.putIfAbsent(identity.getWorkspaceId(), new RuntimeState(runtime, RUNNING));
    if (prev == null) {
      LOG.info(
          "Successfully recovered workspace runtime '{}'",
          identity.getWorkspaceId(),
          identity.getEnvName());
    } else {
      LOG.error(
          "More than 1 runtime with id '{}:{}' found. "
              + "Duplicate provided by infrastructure '{}' will be skipped",
          identity.getWorkspaceId(),
          identity.getEnvName(),
          prev.runtime.getContext().getInfrastructure().getName(),
          infra.getName());
    }
  }

  private void subscribeCleanupOnAbnormalRuntimeStopEvent() {
    eventService.subscribe(new CleanupRuntimeOnAbnormalRuntimeStop());
  }

  private void publishWorkspaceStatusEvent(
      String workspaceId, WorkspaceStatus status, WorkspaceStatus previous, String errorMsg) {
    eventService.publish(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withWorkspaceId(workspaceId)
            .withPrevStatus(previous)
            .withError(errorMsg)
            .withStatus(status));
  }

  private static EnvironmentImpl copyEnv(Workspace workspace, String envName) {

    requireNonNull(workspace, "Workspace should not be null.");
    requireNonNull(envName, "Environment name should not be null.");
    final Environment environment = workspace.getConfig().getEnvironments().get(envName);
    if (environment == null) {
      throw new IllegalArgumentException(
          format("Workspace '%s' doesn't contain environment '%s'", workspace.getId(), envName));
    }
    return new EnvironmentImpl(environment);
  }

  /**
   * Once called no more workspaces are allowed to start, {@link #startAsync} will always throw an
   * appropriate exception. All the running workspaces will continue running, unless stopped
   * directly.
   *
   * @return true if this is the caller is the one who refused start, otherwise if start is being
   *     already refused returns false
   */
  public boolean refuseStart() {
    return isStartRefused.compareAndSet(false, true);
  }

  /**
   * Gets the workspaces identifiers managed by this component. If an identifier is present in set
   * then that workspace wasn't stopped at the moment of method execution.
   *
   * @return workspaces identifiers for those workspaces that are running(not stopped), or an empty
   *     set if there is no a single running workspace
   */
  public Set<String> getRuntimesIds() {
    return ImmutableSet.copyOf(runtimes.keySet());
  }

  /**
   * Returns true if there is at least one workspace running(it's status is different from {@link
   * WorkspaceStatus#STOPPED}), otherwise returns false.
   */
  public boolean isAnyRunning() {
    return !runtimes.isEmpty();
  }

  /**
   * Returns an optional wrapping the runtime context of the workspace with the given identifier, an
   * empty optional is returned in case the workspace doesn't have the runtime.
   */
  public Optional<RuntimeContext> getRuntimeContext(String id) {
    RuntimeState state = runtimes.get(id);
    if (state == null) {
      return Optional.empty();
    }
    return Optional.of(state.runtime.getContext());
  }

  private InternalEnvironmentFactory environmentFactory(String recipeType) throws NotFoundException {
    InternalEnvironmentFactory factory = environmentFactories.get(recipeType);
    if(factory == null)
      throw new NotFoundException(format("InternalEnvironmentFactory not configured for recipe type: '%s'", recipeType));
    return factory;
  }

  private String sessionUserNameOr(String nameIfNoUser) {
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (!subject.isAnonymous()) {
      return subject.getUserName();
    }
    return nameIfNoUser;
  }

  private class CleanupRuntimeOnAbnormalRuntimeStop implements EventSubscriber<RuntimeStatusEvent> {
    @Override
    public void onEvent(RuntimeStatusEvent event) {
      if (event.isFailed()) {
        RuntimeState state = runtimes.remove(event.getIdentity().getWorkspaceId());
        if (state != null) {
          publishWorkspaceStatusEvent(
              state.runtime.getContext().getIdentity().getWorkspaceId(),
              STOPPED,
              RUNNING,
              "Error occurs on workspace runtime stop. Error: " + event.getError());
        }
      }
    }
  }

  private static class RuntimeState {
    final InternalRuntime runtime;
    final WorkspaceStatus status;

    RuntimeState(InternalRuntime runtime, WorkspaceStatus status) {
      this.runtime = runtime;
      this.status = status;
    }
  }
}

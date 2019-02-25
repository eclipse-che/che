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
package org.eclipse.che.api.workspace.server;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.api.workspace.shared.Constants.ERROR_MESSAGE_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.STOPPED_ABNORMALLY_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.STOPPED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_RUNTIMES_ID_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOPPED_BY;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOP_REASON;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.event.RuntimeAbnormalStoppedEvent;
import org.eclipse.che.api.workspace.server.event.RuntimeAbnormalStoppingEvent;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.commons.lang.concurrent.Unlocker;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.tracing.TracingTags;
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

  private ConcurrentMap<String, InternalRuntime<?>> runtimes;
  private final WorkspaceStatusCache statuses;
  private final WorkspaceLockService lockService;
  private final EventService eventService;
  private final WorkspaceSharedPool sharedPool;
  private final WorkspaceDao workspaceDao;
  private final AtomicBoolean isStartRefused;
  private final Map<String, InternalEnvironmentFactory> environmentFactories;
  private final RuntimeInfrastructure infrastructure;
  private final ProbeScheduler probeScheduler;
  // Unique identifier for this workspace runtimes
  private final String workspaceRuntimesId;

  @VisibleForTesting
  WorkspaceRuntimes(
      ConcurrentMap<String, InternalRuntime<?>> runtimes,
      EventService eventService,
      Map<String, InternalEnvironmentFactory> envFactories,
      RuntimeInfrastructure infra,
      WorkspaceSharedPool sharedPool,
      WorkspaceDao workspaceDao,
      @SuppressWarnings("unused") DBInitializer ignored,
      ProbeScheduler probeScheduler,
      WorkspaceStatusCache statuses,
      WorkspaceLockService lockService) {
    this(
        eventService,
        envFactories,
        infra,
        sharedPool,
        workspaceDao,
        ignored,
        probeScheduler,
        statuses,
        lockService);
    this.runtimes = runtimes;
  }

  @Inject
  public WorkspaceRuntimes(
      EventService eventService,
      Map<String, InternalEnvironmentFactory> envFactories,
      RuntimeInfrastructure infra,
      WorkspaceSharedPool sharedPool,
      WorkspaceDao workspaceDao,
      @SuppressWarnings("unused") DBInitializer ignored,
      ProbeScheduler probeScheduler,
      WorkspaceStatusCache statuses,
      WorkspaceLockService lockService) {
    this.probeScheduler = probeScheduler;
    this.runtimes = new ConcurrentHashMap<>();
    this.statuses = statuses;
    this.eventService = eventService;
    this.sharedPool = sharedPool;
    this.workspaceDao = workspaceDao;
    this.isStartRefused = new AtomicBoolean(false);
    this.infrastructure = infra;
    this.environmentFactories = ImmutableMap.copyOf(envFactories);
    this.lockService = lockService;
    LOG.info("Configured factories for environments: '{}'", envFactories.keySet());
    LOG.info("Registered infrastructure '{}'", infra.getName());
    SetView<String> notSupportedByInfra =
        Sets.difference(envFactories.keySet(), infra.getRecipeTypes());
    if (!notSupportedByInfra.isEmpty()) {
      LOG.warn(
          "Configured environment(s) are not supported by infrastructure: '{}'",
          notSupportedByInfra);
    }
    workspaceRuntimesId = NameGenerator.generate("runtimes", 16);
  }

  @PostConstruct
  void init() {
    subscribeAbnormalRuntimeStopListener();
    recover();
  }

  public void validate(Environment environment)
      throws NotFoundException, InfrastructureException, ValidationException {
    String type = environment.getRecipe().getType();
    if (!infrastructure.getRecipeTypes().contains(type)) {
      throw new NotFoundException("Infrastructure not found for type: " + type);
    }
    // try to create internal environment to check if the specified environment is valid
    createInternalEnvironment(environment, emptyMap(), emptyList());
  }

  /**
   * Injects runtime information such as status and {@link
   * org.eclipse.che.api.core.model.workspace.Runtime} into the workspace object, if the workspace
   * doesn't have runtime sets the status to {@link WorkspaceStatus#STOPPED}.
   *
   * @param workspace the workspace to inject runtime into
   */
  public void injectRuntime(WorkspaceImpl workspace) throws ServerException {
    try (Unlocker ignored = lockService.writeLock(workspace.getId())) {
      WorkspaceStatus workspaceStatus = statuses.get(workspace.getId());

      if (workspaceStatus == null) {
        workspace.setStatus(STOPPED);
        return;
      }

      InternalRuntime<?> internalRuntime;
      try {
        internalRuntime = getInternalRuntime(workspace.getId());
      } catch (ServerException | InfrastructureException e) {
        workspace.setStatus(STOPPED);
        return;
      }

      workspace.setRuntime(asRuntime(internalRuntime));
      workspace.setStatus(workspaceStatus);
    }
  }

  /**
   * Returns {@link InternalRuntime} implementation for workspace with the specified id.
   *
   * <p>If memory-storage does not contain internal runtime, then runtime will be recovered if it is
   * active. Otherwise, an exception will be thrown.
   *
   * @param workspaceId identifier of workspace to fetch runtime
   * @return {@link InternalRuntime} implementation for workspace with the specified id.
   * @throws InfrastructureException if any infrastructure exception occurs
   * @throws ServerException if there is no active runtime for the specified workspace
   * @throws ServerException if any other exception occurs
   */
  public InternalRuntime<?> getInternalRuntime(String workspaceId)
      throws InfrastructureException, ServerException {
    try (Unlocker ignored = lockService.writeLock(workspaceId)) {
      InternalRuntime<?> runtime = runtimes.get(workspaceId);
      if (runtime == null) {
        try {
          final Optional<RuntimeIdentity> runtimeIdentity =
              infrastructure
                  .getIdentities()
                  .stream()
                  .filter(id -> id.getWorkspaceId().equals(workspaceId))
                  .findAny();

          if (runtimeIdentity.isPresent()) {
            LOG.info(
                "Runtime for workspace '{}' is requested but there is no cached one. Recovering it.",
                workspaceId);
            runtime = recoverOne(infrastructure, runtimeIdentity.get());
          } else {
            // runtime is not considered by Infrastructure as active
            throw new ServerException("No active runtime is found");
          }
        } catch (ServerException e) {
          statuses.remove(workspaceId);
          throw e;
        } catch (UnsupportedOperationException | ConflictException e) {
          statuses.remove(workspaceId);
          throw new ServerException(e.getMessage(), e);
        }
      }
      return runtime;
    }
  }

  private static RuntimeImpl asRuntime(InternalRuntime<?> runtime) throws ServerException {
    try {
      return new RuntimeImpl(
          runtime.getActiveEnv(),
          runtime.getMachines(),
          runtime.getOwner(),
          runtime.getCommands(),
          runtime.getWarnings());
    } catch (InfrastructureException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  /**
   * Gets workspace status by its identifier.
   *
   * @param workspaceId workspace identifier
   */
  public WorkspaceStatus getStatus(String workspaceId) {
    try (Unlocker ignored = lockService.readLock(workspaceId)) {
      final WorkspaceStatus status = statuses.get(workspaceId);
      return status != null ? status : STOPPED;
    }
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
  @Traced
  public CompletableFuture<Void> startAsync(
      Workspace workspace, @Nullable String envName, Map<String, String> options)
      throws ConflictException, NotFoundException, ServerException {
    TracingTags.WORKSPACE_ID.set(workspace.getId());
    TracingTags.STACK_ID.set(() -> workspace.getAttributes().getOrDefault("stackId", "no stack"));

    final String workspaceId = workspace.getId();
    // Sidecar-based workspaces allowed not to have environments
    EnvironmentImpl environment = null;
    if (envName != null) {
      environment = copyEnv(workspace, envName);
      requireNonNull(environment, "Environment should not be null " + workspaceId);
      requireNonNull(environment.getRecipe(), "Recipe should not be null " + workspaceId);
      requireNonNull(
          environment.getRecipe().getType(), "Recipe type should not be null " + workspaceId);
    }

    WorkspaceConfig workspaceConfig = workspace.getConfig();
    if (isStartRefused.get()) {
      throw new ConflictException(
          format(
              "Start of the workspace '%s' is rejected by the system, "
                  + "no more workspaces are allowed to start",
              workspaceConfig.getName()));
    }

    final String ownerId = EnvironmentContext.getCurrent().getSubject().getUserId();
    final RuntimeIdentity runtimeId = new RuntimeIdentityImpl(workspaceId, envName, ownerId);
    try {
      InternalEnvironment internalEnv =
          createInternalEnvironment(
              environment, workspaceConfig.getAttributes(), workspaceConfig.getCommands());
      RuntimeContext runtimeContext = infrastructure.prepare(runtimeId, internalEnv);
      InternalRuntime runtime = runtimeContext.getRuntime();

      try (Unlocker ignored = lockService.writeLock(workspaceId)) {
        final WorkspaceStatus existingStatus = statuses.putIfAbsent(workspaceId, STARTING);
        if (existingStatus != null) {
          throw new ConflictException(
              format(
                  "Could not start workspace '%s' because its state is '%s'",
                  workspaceId, existingStatus));
        }
        setRuntimesId(workspaceId);
        runtimes.put(workspaceId, runtime);
      }
      LOG.info(
          "Starting workspace '{}/{}' with id '{}' by user '{}'",
          workspace.getNamespace(),
          workspaceConfig.getName(),
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
        try (Unlocker ignored = lockService.writeLock(workspaceId)) {
          statuses.replace(workspaceId, RUNNING);
        }

        LOG.info(
            "Workspace '{}:{}' with id '{}' started by user '{}'",
            workspace.getNamespace(),
            workspace.getConfig().getName(),
            workspaceId,
            sessionUserNameOr("undefined"));
        publishWorkspaceStatusEvent(workspaceId, RUNNING, STARTING, null);
      } catch (InfrastructureException e) {
        try (Unlocker ignored = lockService.writeLock(workspaceId)) {
          runtimes.remove(workspaceId);
          statuses.remove(workspaceId);
        }
        // Cancels workspace servers probes if any
        probeScheduler.cancel(workspaceId);

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
  @Traced
  public CompletableFuture<Void> stopAsync(Workspace workspace, Map<String, String> options)
      throws NotFoundException, ConflictException {
    TracingTags.WORKSPACE_ID.set(workspace.getId());
    TracingTags.STOPPED_BY.set(getStoppedBy(workspace));
    TracingTags.STACK_ID.set(() -> workspace.getAttributes().getOrDefault("stackId", "no stack"));

    String workspaceId = workspace.getId();
    WorkspaceStatus status = statuses.get(workspaceId);
    if (status == null) {
      throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
    }
    if (status != RUNNING && status != STARTING) {
      throw new ConflictException(
          format("Could not stop workspace '%s' because its state is '%s'", workspaceId, status));
    }
    if (!statuses.replace(workspaceId, status, STOPPING)) {
      WorkspaceStatus newStatus = statuses.get(workspaceId);
      throw new ConflictException(
          format(
              "Could not stop workspace '%s' because its state is '%s'",
              workspaceId, newStatus == null ? STOPPED : newStatus));
    }
    setRuntimesId(workspaceId);

    String stoppedBy =
        firstNonNull(
            sessionUserNameOr(workspace.getAttributes().get(WORKSPACE_STOPPED_BY)), "undefined");
    LOG.info(
        "Workspace '{}/{}' with id '{}' is stopping by user '{}'",
        workspace.getNamespace(),
        workspace.getConfig().getName(),
        workspace.getId(),
        stoppedBy);
    publishWorkspaceStatusEvent(workspaceId, STOPPING, status, options.get(WORKSPACE_STOP_REASON));
    return CompletableFuture.runAsync(
        ThreadLocalPropagateContext.wrap(new StopRuntimeTask(workspace, options, stoppedBy)),
        sharedPool.getExecutor());
  }

  private String getStoppedBy(Workspace workspace) {
    return firstNonNull(
        sessionUserIdOr(workspace.getAttributes().get(WORKSPACE_STOPPED_BY)), "undefined");
  }

  private class StopRuntimeTask implements Runnable {

    private final Workspace workspace;
    private final Map<String, String> options;
    private final String stoppedBy;

    public StopRuntimeTask(Workspace workspace, Map<String, String> options, String stoppedBy) {
      this.workspace = workspace;
      this.options = options;
      this.stoppedBy = stoppedBy;
    }

    @Override
    public void run() {
      String workspaceId = workspace.getId();
      // Cancels workspace servers probes if any
      probeScheduler.cancel(workspaceId);
      InternalRuntime<?> runtime = null;
      try {
        runtime = getInternalRuntime(workspaceId);

        runtime.stop(options);

        // remove before firing an event to have consistency between state and the event
        try (Unlocker ignored = lockService.writeLock(workspaceId)) {
          runtimes.remove(workspaceId);
          statuses.remove(workspaceId);
        }
        LOG.info(
            "Workspace '{}/{}' with id '{}' is stopped by user '{}'",
            workspace.getNamespace(),
            workspace.getConfig().getName(),
            workspaceId,
            stoppedBy);
        publishWorkspaceStatusEvent(workspaceId, STOPPED, STOPPING, null);
      } catch (ServerException | InfrastructureException e) {
        // remove before firing an event to have consistency between state and the event
        try (Unlocker ignored = lockService.writeLock(workspaceId)) {
          runtimes.remove(workspaceId);
          statuses.remove(workspaceId);
        }

        if (runtime == null) {
          LOG.error(
              "Error occurred during fetching of runtime for stopping workspace with id '{}' by user '{}'. Error: {}",
              workspaceId,
              stoppedBy,
              e.getMessage(),
              e);
        } else {
          RuntimeIdentity runtimeId = runtime.getContext().getIdentity();
          LOG.error(
              "Error occurred during stopping of runtime '{}:{}:{}' by user '{}'. Error: {}",
              runtimeId.getWorkspaceId(),
              runtimeId.getEnvName(),
              runtimeId.getOwnerId(),
              stoppedBy,
              e.getMessage(),
              e);
        }

        publishWorkspaceStatusEvent(
            workspaceId,
            STOPPED,
            STOPPING,
            "Error occurs on workspace runtime stop. Error: " + e.getMessage());
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
    return statuses.get(workspaceId) != null;
  }

  @VisibleForTesting
  void recover() {
    if (isStartRefused.get()) {
      LOG.warn("Recovery of the workspaces is rejected.");
      return;
    }
    Set<RuntimeIdentity> identities;
    try {
      identities = infrastructure.getIdentities();
    } catch (UnsupportedOperationException e) {
      LOG.warn("Not recoverable infrastructure: '{}'", infrastructure.getName());
      return;
    } catch (InfrastructureException e) {
      LOG.error(
          "An error occurred while attempting to get runtime identities for infrastructure '{}'. Reason: '{}'",
          infrastructure.getName(),
          e.getMessage());
      return;
    }

    LOG.info("Infrastructure is tracking {} active runtimes", identities.size());

    if (identities.isEmpty()) {
      return;
    }

    for (RuntimeIdentity identity : identities) {
      String workspaceId = identity.getWorkspaceId();

      try (Unlocker ignored = lockService.writeLock(workspaceId)) {
        statuses.putIfAbsent(workspaceId, STARTING);
      }
    }

    sharedPool.execute(new RecoverRuntimesTask(identities));
  }

  @VisibleForTesting
  class RecoverRuntimesTask implements Runnable {

    private final Set<RuntimeIdentity> identities;

    RecoverRuntimesTask(Set<RuntimeIdentity> identities) {
      this.identities = identities;
    }

    @Override
    public void run() {
      long startTime = System.currentTimeMillis();
      LOG.info("Recovering of runtimes is started.");

      for (RuntimeIdentity identity : identities) {
        try {
          recoverOne(infrastructure, identity);
        } catch (ServerException | ConflictException e) {
          LOG.error(
              "An error occurred while attempting to recover runtime '{}' using infrastructure '{}'. Reason: '{}'",
              identity.getWorkspaceId(),
              infrastructure.getName(),
              e.getMessage());
        }
      }

      long finishTime = System.currentTimeMillis();
      LOG.info(
          "All runtimes have been recovered in {} seconds.",
          TimeUnit.MILLISECONDS.toSeconds(finishTime - startTime));
    }
  }

  @VisibleForTesting
  InternalRuntime<?> recoverOne(RuntimeInfrastructure infra, RuntimeIdentity identity)
      throws ServerException, ConflictException {
    if (isStartRefused.get()) {
      throw new ConflictException(
          format(
              "Recovery of the workspace '%s' is rejected by the system, "
                  + "no more workspaces are allowed to start",
              identity.getWorkspaceId()));
    }
    Workspace workspace;
    try {
      workspace = workspaceDao.get(identity.getWorkspaceId());
    } catch (NotFoundException x) {
      throw new ServerException(
          format(
              "Workspace configuration is missing for the runtime '%s:%s'. Runtime won't be recovered",
              identity.getWorkspaceId(), identity.getEnvName()));
    }

    Environment environment = null;
    WorkspaceConfig workspaceConfig = workspace.getConfig();
    if (identity.getEnvName() != null) {
      environment = workspaceConfig.getEnvironments().get(identity.getEnvName());
      if (environment == null) {
        throw new ServerException(
            format(
                "Environment configuration is missing for the runtime '%s:%s'. Runtime won't be recovered",
                identity.getWorkspaceId(), identity.getEnvName()));
      }
    }

    InternalRuntime runtime;
    try {
      InternalEnvironment internalEnv =
          createInternalEnvironment(
              environment, workspaceConfig.getAttributes(), workspaceConfig.getCommands());
      runtime = infra.prepare(identity, internalEnv).getRuntime();
      WorkspaceStatus runtimeStatus = runtime.getStatus();
      try (Unlocker ignored = lockService.writeLock(workspace.getId())) {
        statuses.replace(identity.getWorkspaceId(), runtimeStatus);
        runtimes.putIfAbsent(identity.getWorkspaceId(), runtime);
      }
      LOG.info(
          "Successfully recovered workspace runtime '{}'. Its status is '{}'",
          identity.getWorkspaceId(),
          runtimeStatus);
      return runtime;
    } catch (InfrastructureException | ValidationException | NotFoundException x) {
      throw new ServerException(
          format(
              "Couldn't recover runtime '%s:%s'. Error: %s",
              identity.getWorkspaceId(), identity.getEnvName(), x.getMessage()));
    }
  }

  private void subscribeAbnormalRuntimeStopListener() {
    eventService.subscribe(new AbnormalRuntimeStoppingListener());
    eventService.subscribe(new AbnormalRuntimeStoppedListener());
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

  /** Returns workspace ids which has {@link WorkspaceStatus#RUNNING} runtimes. */
  public Set<String> getRunning() {
    return statuses
        .asMap()
        .entrySet()
        .stream()
        .filter(e -> RUNNING == e.getValue())
        .map(Entry::getKey)
        .collect(toSet());
  }

  /**
   * Gets the workspaces identifiers managed by this component. If an identifier is present in set
   * then that workspace wasn't stopped at the moment of method execution.
   *
   * @return workspaces identifiers for those workspaces that are active(not stopped), or an empty
   *     set if there is no a single active workspace
   */
  public Set<String> getActive() {
    return ImmutableSet.copyOf(statuses.asMap().keySet());
  }

  /**
   * Returns true if there is at least one workspace active(it's status is different from {@link
   * WorkspaceStatus#STOPPED}), otherwise returns false.
   */
  public boolean isAnyActive() {
    return !statuses.asMap().isEmpty();
  }

  /**
   * Gets the list of workspace id's which are currently starting or stopping on given node. (it's
   * status is {@link WorkspaceStatus#STARTING} or {@link WorkspaceStatus#STOPPING})
   */
  public Set<String> getInProgress() {
    return statuses
        .asMap()
        .entrySet()
        .stream()
        .filter(e -> STARTING == e.getValue() || STOPPING == e.getValue())
        .map(Entry::getKey)
        .filter(this::containsThisRuntimesId)
        .collect(toSet());
  }

  /**
   * Returns true if there is at least one local workspace starting or stopping (it's status is
   * {@link WorkspaceStatus#STARTING} or {@link WorkspaceStatus#STOPPING}), otherwise returns false.
   */
  public boolean isAnyInProgress() {
    return statuses
        .asMap()
        .entrySet()
        .stream()
        .filter(e -> STARTING == e.getValue() || STOPPING == e.getValue())
        .map(Entry::getKey)
        .anyMatch(this::containsThisRuntimesId);
  }

  private void setRuntimesId(String workspaceId) {
    try {
      final WorkspaceImpl workspace = workspaceDao.get(workspaceId);
      workspace.getAttributes().put(WORKSPACE_RUNTIMES_ID_ATTRIBUTE, workspaceRuntimesId);
      workspaceDao.update(workspace);
    } catch (NotFoundException | ServerException | ConflictException ex) {
      LOG.warn(
          String.format(
              "Cannot set runtimes id attribute for the workspace %s. Cause: %s",
              workspaceId, ex.getMessage()));
    }
  }

  /** Checks whether workspace with given id, related to this workspace runtimes. */
  private boolean containsThisRuntimesId(String workspaceId) {
    try {
      final WorkspaceImpl workspace = workspaceDao.get(workspaceId);
      if (workspaceRuntimesId.equals(
          workspace.getAttributes().get(WORKSPACE_RUNTIMES_ID_ATTRIBUTE))) {
        return true;
      }
    } catch (NotFoundException | ServerException ex) {
      LOG.warn(
          String.format(
              "Failed to get processing workspace %s. Cause: %s", workspaceId, ex.getMessage()));
    }
    return false;
  }

  /**
   * Returns an optional wrapping the runtime context of the workspace with the given identifier, an
   * empty optional is returned in case the workspace doesn't have the runtime.
   */
  public Optional<RuntimeContext> getRuntimeContext(String workspaceId) {
    try (Unlocker ignored = lockService.readLock(workspaceId)) {
      InternalRuntime<?> runtime = runtimes.get(workspaceId);
      if (runtime == null) {
        return Optional.empty();
      }
      return Optional.of(runtime.getContext());
    }
  }

  public Set<String> getSupportedRecipes() {
    return environmentFactories.keySet();
  }

  @VisibleForTesting
  InternalEnvironment createInternalEnvironment(
      @Nullable Environment environment,
      Map<String, String> workspaceConfigAttributes,
      List<? extends Command> commands)
      throws InfrastructureException, ValidationException, NotFoundException {
    String recipeType;
    if (environment == null) {
      recipeType = Constants.NO_ENVIRONMENT_RECIPE_TYPE;
    } else {
      recipeType = environment.getRecipe().getType();
    }
    InternalEnvironmentFactory factory = environmentFactories.get(recipeType);
    if (factory == null) {
      throw new NotFoundException(
          format("InternalEnvironmentFactory is not configured for recipe type: '%s'", recipeType));
    }
    InternalEnvironment internalEnvironment = factory.create(environment);
    internalEnvironment.setAttributes(new HashMap<>(workspaceConfigAttributes));
    internalEnvironment.setCommands(
        commands.stream().map(CommandImpl::new).collect(Collectors.toList()));

    return internalEnvironment;
  }

  private String sessionUserNameOr(String nameIfNoUser) {
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (!subject.isAnonymous()) {
      return subject.getUserName();
    }
    return nameIfNoUser;
  }

  private String sessionUserIdOr(String nameIfNoUser) {
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (!subject.isAnonymous()) {
      return subject.getUserId();
    }
    return nameIfNoUser;
  }

  private class AbnormalRuntimeStoppingListener
      implements EventSubscriber<RuntimeAbnormalStoppingEvent> {

    @Override
    public void onEvent(RuntimeAbnormalStoppingEvent event) {
      RuntimeIdentity identity = event.getIdentity();
      String workspaceId = identity.getWorkspaceId();
      WorkspaceStatus previousStatus;
      try (Unlocker ignored = lockService.writeLock(workspaceId)) {
        previousStatus = statuses.replace(workspaceId, STOPPING);
      }

      if (previousStatus == null) {
        LOG.error(
            "Runtime '{}:{}:{}' became abnormally stopping but it was not considered as active before",
            workspaceId,
            identity.getEnvName(),
            identity.getOwnerId());
      }

      LOG.info(
          "Runtime '{}:{}:{}' is stopping abnormally. Reason: {}",
          workspaceId,
          identity.getEnvName(),
          identity.getOwnerId(),
          event.getReason());

      publishWorkspaceStatusEvent(
          workspaceId,
          STOPPING,
          previousStatus,
          "Workspace is going to be STOPPED. Reason: " + event.getReason());
    }
  }

  private class AbnormalRuntimeStoppedListener
      implements EventSubscriber<RuntimeAbnormalStoppedEvent> {

    @Override
    public void onEvent(RuntimeAbnormalStoppedEvent event) {
      RuntimeIdentity identity = event.getIdentity();
      String workspaceId = identity.getWorkspaceId();
      // Cancels workspace servers probes if any
      probeScheduler.cancel(workspaceId);
      final WorkspaceStatus previousStatus;
      try (Unlocker ignored = lockService.writeLock(workspaceId)) {
        runtimes.remove(workspaceId);
        previousStatus = statuses.remove(workspaceId);
      }

      if (previousStatus == null) {
        LOG.error(
            "Runtime '{}:{}:{}' is abnormally stopped but it was not considered as active before",
            workspaceId,
            identity.getEnvName(),
            identity.getOwnerId());
      }

      LOG.info(
          "Runtime '{}:{}:{}' is stopped abnormally. Reason: {}",
          workspaceId,
          identity.getEnvName(),
          identity.getOwnerId(),
          event.getReason());

      publishWorkspaceStatusEvent(
          workspaceId,
          STOPPED,
          previousStatus,
          "Workspace is stopped. Reason: " + event.getReason());
      setAbnormalStopAttributes(workspaceId, event.getReason());
    }

    private void setAbnormalStopAttributes(String workspaceId, String error) {
      try {
        WorkspaceImpl workspace = workspaceDao.get(workspaceId);
        workspace.getAttributes().put(ERROR_MESSAGE_ATTRIBUTE_NAME, error);
        workspace.getAttributes().put(STOPPED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
        workspace.getAttributes().put(STOPPED_ABNORMALLY_ATTRIBUTE_NAME, Boolean.toString(true));
        workspaceDao.update(workspace);
      } catch (NotFoundException | ServerException | ConflictException e) {
        LOG.warn(
            format(
                "Cannot set error status of the workspace %s. Error status is: %s. Cause: %s",
                workspaceId, error, e.getMessage()));
      }
    }
  }
}

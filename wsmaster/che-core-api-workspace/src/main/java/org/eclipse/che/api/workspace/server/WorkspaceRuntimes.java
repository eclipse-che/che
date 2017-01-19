/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.server.launcher.AgentLauncher;
import org.eclipse.che.api.agent.server.launcher.AgentLauncherFactory;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.WebsocketMessageConsumer;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.api.environment.server.MachineStartedHandler;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.exception.EnvironmentNotRunningException;
import org.eclipse.che.api.environment.server.exception.EnvironmentStartInterruptedException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.lang.concurrent.StripedLocks;
import org.eclipse.che.commons.lang.concurrent.Unlocker;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.SNAPSHOTTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Defines an internal API for managing {@link WorkspaceRuntimeImpl} instances.
 *
 * <p>This component implements {@link WorkspaceStatus} contract.
 *
 * <p>The implementation is thread-safe and guarded by
 * eagerly initialized readwrite locks produced by {@link StripedLocks}.
 * The component doesn't expose any api for client-side locking.
 * All the instances produced by this component are copies of the real data.
 *
 * <p>The component doesn't check if the incoming objects are in application-valid state.
 * Which means that it is expected that if {@link #startAsync(Workspace, String, boolean)} method is called
 * then {@code Workspace} argument is a application-valid object which contains
 * all the required data for performing start.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceRuntimes {

    private static final Logger LOG = getLogger(WorkspaceRuntimes.class);

    private final ConcurrentMap<String, RuntimeState> states;
    private final EventService                        eventsService;
    private final StripedLocks                        locks;
    private final CheEnvironmentEngine                envEngine;
    private final AgentSorter                         agentSorter;
    private final AgentLauncherFactory                launcherFactory;
    private final AgentRegistry                       agentRegistry;
    private final SnapshotDao                         snapshotDao;
    private final WorkspaceSharedPool                 sharedPool;

    private volatile boolean isPreDestroyInvoked;

    @Inject
    public WorkspaceRuntimes(EventService eventsService,
                             CheEnvironmentEngine envEngine,
                             AgentSorter agentSorter,
                             AgentLauncherFactory launcherFactory,
                             AgentRegistry agentRegistry,
                             SnapshotDao snapshotDao,
                             WorkspaceSharedPool sharedPool) {
        this(eventsService,
             envEngine,
             agentSorter,
             launcherFactory,
             agentRegistry,
             snapshotDao,
             sharedPool,
             new ConcurrentHashMap<>());
    }

    public WorkspaceRuntimes(EventService eventsService,
                             CheEnvironmentEngine envEngine,
                             AgentSorter agentSorter,
                             AgentLauncherFactory launcherFactory,
                             AgentRegistry agentRegistry,
                             SnapshotDao snapshotDao,
                             WorkspaceSharedPool sharedPool,
                             ConcurrentMap<String, RuntimeState> states) {
        this.eventsService = eventsService;
        this.envEngine = envEngine;
        this.agentSorter = agentSorter;
        this.launcherFactory = launcherFactory;
        this.agentRegistry = agentRegistry;
        this.snapshotDao = snapshotDao;
        // 16 - experimental value for stripes count, it comes from default hash map size
        this.locks = new StripedLocks(16);
        this.sharedPool = sharedPool;
        this.states = states;
    }

    /**
     * Asynchronously starts the environment of the workspace.
     * Before executing start task checks whether all conditions
     * are met and throws appropriate exceptions if not, so
     * there is no way to start the same workspace twice.
     *
     * <p>Note that cancellation of resulting future won't
     * interrupt workspace start, call {@link #stop(String)} directly instead.
     *
     * <p>If starting process is interrupted let's say within call
     * to {@link #stop(String)} method, resulting future will
     * be exceptionally completed(eventually) with an instance of
     * {@link EnvironmentStartInterruptedException}. Note that clients
     * don't have to cleanup runtime resources, the component
     * will do necessary cleanup when interrupted.
     *
     * <p>Implementation notes:
     * if thread which executes the task is interrupted, then the
     * task is also eventually(depends on the environment engine implementation)
     * interrupted as if {@link #stop(String)} is called directly.
     * That helps to shutdown gracefully when thread pool is asked
     * to {@link ExecutorService#shutdownNow()} and also reduces
     * shutdown time when there are starting workspaces.
     *
     * @param workspace
     *         workspace containing target environment
     * @param envName
     *         the name of the environment to start
     * @param recover
     *         whether to recover from the snapshot
     * @return completable future describing the instance of running environment
     * @throws ConflictException
     *         when the workspace is already started
     * @throws IllegalArgumentException
     *         when the workspace doesn't contain the environment
     * @throws NullPointerException
     *         when either {@code workspace} or {@code envName} is null
     */
    public CompletableFuture<WorkspaceRuntimeImpl> startAsync(Workspace workspace,
                                                              String envName,
                                                              boolean recover) throws ConflictException {
        requireNonNull(workspace, "Non-null workspace required");
        requireNonNull(envName, "Non-null environment name required");
        EnvironmentImpl environment = copyEnv(workspace, envName);
        String workspaceId = workspace.getId();
        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture;
        StartTask startTask;
        try (@SuppressWarnings("unused") Unlocker u = locks.writeLock(workspaceId)) {
            ensurePreDestroyIsNotExecuted();
            RuntimeState state = states.get(workspaceId);
            if (state != null) {
                throw new ConflictException(format("Could not start workspace '%s' because its status is '%s'",
                                                   workspace.getConfig().getName(),
                                                   state.status));
            }
            startTask = new StartTask(workspaceId,
                                      envName,
                                      environment,
                                      recover,
                                      cmpFuture = new CompletableFuture<>());
            states.put(workspaceId, new RuntimeState(WorkspaceStatus.STARTING,
                                                     envName,
                                                     startTask,
                                                     sharedPool.submit(startTask)));
        }

        // publish event synchronously as the task may not be executed by
        // executors service(due to legal cancellation), clients still have
        // to receive STOPPED -> STARTING event
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withStatus(WorkspaceStatus.STARTING)
                                        .withEventType(EventType.STARTING)
                                        .withPrevStatus(WorkspaceStatus.STOPPED));

        // so the start thread is free to go and start the environment
        startTask.unlockStart();

        return cmpFuture;
    }

    /**
     * Gets workspace runtime descriptor.
     *
     * @param workspaceId
     *         the id of the workspace to get its runtime
     * @return descriptor which describes current state of the workspace runtime
     * @throws NotFoundException
     *         when workspace with given {@code workspaceId} is not found
     * @throws ServerException
     *         if any error occurs while getting machines runtime information
     */
    public WorkspaceRuntimeImpl getRuntime(String workspaceId) throws NotFoundException, ServerException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        RuntimeState state;
        try (@SuppressWarnings("unused") Unlocker u = locks.readLock(workspaceId)) {
            state = new RuntimeState(getExistingState(workspaceId));
        }
        return new WorkspaceRuntimeImpl(state.envName, envEngine.getMachines(workspaceId));
    }

    /**
     * Return status of the workspace.
     *
     * @param workspaceId
     *         ID of requested workspace
     * @return {@link WorkspaceStatus#STOPPED} if workspace is not running or,
     * the status of workspace runtime otherwise
     */
    public WorkspaceStatus getStatus(String workspaceId) {
        requireNonNull(workspaceId, "Required non-null workspace id");
        try (@SuppressWarnings("unused") Unlocker u = locks.readLock(workspaceId)) {
            RuntimeState state = states.get(workspaceId);
            if (state == null) {
                return WorkspaceStatus.STOPPED;
            }
            return state.status;
        }
    }

    /**
     * Injects runtime information such as status and {@link WorkspaceRuntimeImpl}
     * into the workspace object, if the workspace doesn't have runtime sets the
     * status to {@link WorkspaceStatus#STOPPED}.
     *
     * @param workspace
     *         the workspace to inject runtime into
     */
    public void injectRuntime(WorkspaceImpl workspace) {
        requireNonNull(workspace, "Required non-null workspace");
        RuntimeState state = null;
        try (@SuppressWarnings("unused") Unlocker u = locks.readLock(workspace.getId())) {
            if (states.containsKey(workspace.getId())) {
                state = new RuntimeState(states.get(workspace.getId()));
            }
        }
        if (state == null) {
            workspace.setStatus(WorkspaceStatus.STOPPED);
        } else {
            workspace.setStatus(state.status);
            try {
                workspace.setRuntime(new WorkspaceRuntimeImpl(state.envName, envEngine.getMachines(workspace.getId())));
            } catch (Exception x) {
                workspace.setRuntime(new WorkspaceRuntimeImpl(state.envName, Collections.emptyList()));
            }
        }
    }

    /**
     * Returns true if the status of the workspace is different
     * from {@link WorkspaceStatus#STOPPED}.
     *
     * @param workspaceId
     *         workspace identifier to perform check
     * @return true if workspace status is different from {@link WorkspaceStatus#STOPPED}
     */
    public boolean hasRuntime(String workspaceId) {
        return states.containsKey(workspaceId);
    }

    /**
     * Stops running workspace runtime.
     *
     * <p>Stops environment in an implementation specific way.
     * During the stop of the workspace its runtime is accessible with {@link WorkspaceStatus#STOPPING stopping} status.
     * Workspace may be stopped only if its status is {@link WorkspaceStatus#RUNNING}.
     *
     * @param workspaceId
     *         identifier of workspace which should be stopped
     * @throws NotFoundException
     *         when workspace with specified identifier is not running
     * @throws ServerException
     *         when any error occurs during workspace stopping
     * @throws ConflictException
     *         when running workspace status is different from {@link WorkspaceStatus#RUNNING}
     * @see CheEnvironmentEngine#stop(String)
     * @see WorkspaceStatus#STOPPING
     */
    public void stop(String workspaceId) throws NotFoundException,
                                                ServerException,
                                                ConflictException,
                                                EnvironmentException {
        requireNonNull(workspaceId, "Required not-null workspace id");
        RuntimeState prevState;
        try (@SuppressWarnings("unused") Unlocker u = locks.writeLock(workspaceId)) {
            ensurePreDestroyIsNotExecuted();
            RuntimeState state = getExistingState(workspaceId);
            if (state.status != WorkspaceStatus.RUNNING && state.status != WorkspaceStatus.STARTING) {
                throw new ConflictException(format("Couldn't stop the workspace '%s' because its status is '%s'. " +
                                                   "Workspace can be stopped only if it is 'RUNNING' or 'STARTING'",
                                                   workspaceId,
                                                   state.status));
            }
            prevState = new RuntimeState(state);
            state.status = WorkspaceStatus.STOPPING;
        }

        // workspace is running, stop normally
        if (prevState.status == WorkspaceStatus.RUNNING) {
            stopEnvironmentAndPublishEvents(workspaceId, WorkspaceStatus.RUNNING);
            return;
        }

        // interrupt workspace start thread
        prevState.startFuture.cancel(true);

        // if task wasn't called by executor service, then
        // no real machines were started but, the clients still
        // have to be notified about the workspace shut down
        StartTask startTask = prevState.startTask;
        if (startTask.markAsUsed()) {
            removeStateAndPublishStopEvents(workspaceId);
            prevState.startTask.earlyComplete();
            return;
        }

        // otherwise stop will be triggered by the start task, wait for it to finish
        try {
            startTask.await();
        } catch (EnvironmentStartInterruptedException ignored) {
            // environment start successfully interrupted
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
            throw new ServerException("Interrupted while waiting for start task cancellation", x);
        }
    }

    /**
     * Starts machine in running workspace.
     *
     * @param workspaceId
     *         ID of workspace that owns machine
     * @param machineConfig
     *         config of machine that should be started
     * @return running machine
     * @throws ConflictException
     *         if environment is not running or conflicting machine already exists in the environment
     * @throws ConflictException
     *         if environment was stopped during start of machine
     * @throws ServerException
     *         if any other error occurs
     */
    public Instance startMachine(String workspaceId,
                                 MachineConfig machineConfig) throws ServerException,
                                                                     ConflictException,
                                                                     NotFoundException,
                                                                     EnvironmentException {

        try (@SuppressWarnings("unused") Unlocker u = locks.readLock(workspaceId)) {
            getRunningState(workspaceId);
        }

        // Copy constructor makes deep copy of objects graph
        // which means that original values won't affect the values in used further in this class
        MachineConfigImpl machineConfigCopy = new MachineConfigImpl(machineConfig);

        List<String> agents = Collections.singletonList("org.eclipse.che.terminal");

        Instance instance = envEngine.startMachine(workspaceId, machineConfigCopy, agents);
        launchAgents(instance, agents);

        try (@SuppressWarnings("unused") Unlocker u = locks.writeLock(workspaceId)) {
            ensurePreDestroyIsNotExecuted();
            RuntimeState workspaceState = states.get(workspaceId);
            if (workspaceState == null || workspaceState.status != RUNNING) {
                try {
                    envEngine.stopMachine(workspaceId, instance.getId());
                } catch (NotFoundException | ServerException | ConflictException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
                throw new ConflictException(format("Environment of workspace '%s' was stopped during start of  machine",
                                                   workspaceId));
            }
        }
        return instance;
    }

    /**
     * Synchronously creates a snapshot of a given workspace,
     * the workspace must be {@link WorkspaceStatus#RUNNING}.
     *
     * <p>Publishes {@link EventType#SNAPSHOT_CREATING}, {@link EventType#SNAPSHOT_CREATED},
     * {@link EventType#SNAPSHOT_CREATION_ERROR} like defined by {@link EventType}.
     *
     * @param workspaceId
     *         the id of workspace to create snapshot
     * @throws NotFoundException
     *         when workspace doesn't have a runtime
     * @throws ConflictException
     *         when workspace status is different from {@link WorkspaceStatus#RUNNING}
     * @throws ServerException
     *         when any other error occurs
     */
    public void snapshot(String workspaceId) throws NotFoundException,
                                                    ConflictException,
                                                    ServerException {
        try (@SuppressWarnings("unused") Unlocker u = locks.writeLock(workspaceId)) {
            getRunningState(workspaceId).status = SNAPSHOTTING;
        }
        snapshotAndUpdateStatus(workspaceId);
    }

    /**
     * Asynchronously creates a snapshot of a given workspace,
     * but synchronously toggles workspace status to {@link WorkspaceStatus#SNAPSHOTTING}
     * or throws an error if it is impossible to do so.
     *
     * @see #snapshot(String)
     */
    public Future<Void> snapshotAsync(String workspaceId) throws NotFoundException, ConflictException {
        try (@SuppressWarnings("unused") Unlocker u = locks.writeLock(workspaceId)) {
            getRunningState(workspaceId).status = SNAPSHOTTING;
        }
        return sharedPool.submit(() -> {
            try {
                snapshotAndUpdateStatus(workspaceId);
            } catch (Exception x) {
                LOG.error(format("Couldn't create a snapshot of workspace '%s'", workspaceId), x);
                throw x;
            }
            return null;
        });
    }

    /**
     * Removes snapshot binaries in implementation specific way.
     *
     * @param snapshot
     *         snapshot that will be removed
     * @return true if binaries are successfully removed,
     * otherwise if binaries not found returns false
     * @throws ServerException
     *         if any error occurs during binaries removal
     * @see CheEnvironmentEngine#removeSnapshot(SnapshotImpl)
     */
    public boolean removeBinaries(SnapshotImpl snapshot) throws ServerException {
        try {
            envEngine.removeSnapshot(snapshot);
        } catch (NotFoundException x) {
            return false;
        }
        return true;
    }

    /**
     * Removes binaries of all the snapshots, continues to remove
     * snapshots if removal of binaries for a single snapshot fails.
     *
     * @param snapshots
     *         the list of snapshots to remove binaries
     */
    public void removeBinaries(Collection<? extends SnapshotImpl> snapshots) {
        for (SnapshotImpl snapshot : snapshots) {
            try {
                if (!removeBinaries(snapshot)) {
                    LOG.warn("An attempt to remove binaries of the snapshot '{}' while there are no binaries", snapshot.getId());
                }
            } catch (ServerException x) {
                LOG.error(format("Couldn't remove snapshot '%s', workspace id '%s'", snapshot.getId(), snapshot.getWorkspaceId()), x);
            }
        }
    }

    /**
     * Stops machine in a running environment.
     *
     * @param workspaceId
     *         ID of workspace that owns environment
     * @param machineId
     *         ID of machine that should be stopped
     * @throws NotFoundException
     *         if machine is not found in the environment
     *         or workspace doesn't have a runtime
     * @throws ConflictException
     *         if environment is not running
     * @throws ConflictException
     *         if machine is dev and its stop is forbidden
     * @throws ServerException
     *         if any other error occurs
     */
    public void stopMachine(String workspaceId, String machineId) throws NotFoundException,
                                                                         ServerException,
                                                                         ConflictException {
        try (@SuppressWarnings("unused") Unlocker u = locks.readLock(workspaceId)) {
            getRunningState(workspaceId);
        }
        envEngine.stopMachine(workspaceId, machineId);
    }

    /**
     * Finds machine {@link Instance} by specified workspace and machine IDs.
     *
     * @param workspaceId
     *         ID of workspace that owns machine
     * @param machineId
     *         ID of requested machine
     * @return requested machine
     * @throws NotFoundException
     *         if environment or machine is not running
     */
    public Instance getMachine(String workspaceId, String machineId) throws NotFoundException {
        return envEngine.getMachine(workspaceId, machineId);
    }

    /**
     * Gets the workspaces identifiers managed by this component.
     * If an identifier is present in set then that workspace wasn't
     * stopped at the moment of method execution.
     *
     * @return workspaces identifiers for those workspaces that are running(not stopped),
     * or an empty set if there is no a single running workspace
     */
    public Set<String> getRuntimesIds() {
        return new HashSet<>(states.keySet());
    }

    /**
     * Removes all states from the in-memory storage, while
     * {@link CheEnvironmentEngine} is responsible for environment destroying.
     */
    @PreDestroy
    @VisibleForTesting
    void cleanup() {
        isPreDestroyInvoked = true;

        // wait existing tasks to complete
        sharedPool.terminateAndWait();

        List<String> idsToStop;
        try (@SuppressWarnings("unused") Unlocker u = locks.writeAllLock()) {
            idsToStop = states.entrySet()
                              .stream()
                              .filter(e -> e.getValue().status != STOPPING)
                              .map(Map.Entry::getKey)
                              .collect(Collectors.toList());
            states.clear();
        }

        // nothing to stop
        if (idsToStop.isEmpty()) {
            return;
        }

        LOG.info("Shutdown running states, states to shutdown '{}'", idsToStop.size());
        ExecutorService executor =
                Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors(),
                                             new ThreadFactoryBuilder().setNameFormat("StopEnvironmentsPool-%d")
                                                                       .setDaemon(false)
                                                                       .build());
        for (String id : idsToStop) {
            executor.execute(() -> {
                try {
                    envEngine.stop(id);
                } catch (EnvironmentNotRunningException ignored) {
                    // could be stopped during workspace pool shutdown
                } catch (Exception x) {
                    LOG.error(x.getMessage(), x);
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOG.error("Unable terminate machines pool");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void ensurePreDestroyIsNotExecuted() {
        if (isPreDestroyInvoked) {
            throw new IllegalStateException("Could not perform operation because application server is stopping");
        }
    }

    private RuntimeState getExistingState(String workspaceId) throws NotFoundException {
        RuntimeState state = states.get(workspaceId);
        if (state == null) {
            throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running");
        }
        return state;
    }

    private RuntimeState getRunningState(String workspaceId) throws NotFoundException, ConflictException {
        RuntimeState state = getExistingState(workspaceId);
        if (state.status != RUNNING) {
            throw new ConflictException(format("Workspace with id '%s' is not 'RUNNING', it's status is '%s'",
                                               workspaceId,
                                               state.status));
        }
        return state;
    }

    protected void launchAgents(Instance instance, List<String> agents) throws ServerException {
        try {
            for (AgentKey agentKey : agentSorter.sort(agents)) {
                if (!Thread.currentThread().isInterrupted()) {
                    LOG.info("Launching '{}' agent at workspace {}", agentKey.getId(), instance.getWorkspaceId());
                    Agent agent = agentRegistry.getAgent(agentKey);
                    AgentLauncher launcher = launcherFactory.find(agentKey.getId(), instance.getConfig().getType());
                    launcher.launch(instance, agent);
                }
            }
        } catch (AgentException e) {
            throw new MachineException(e.getMessage(), e);
        }
    }

    /**
     * Starts the environment publishing all the necessary events.
     * Respects task interruption & stops the workspace if starting task is cancelled.
     */
    private void startEnvironmentAndPublishEvents(EnvironmentImpl environment,
                                                  String workspaceId,
                                                  String envName,
                                                  boolean recover) throws ServerException,
                                                                          EnvironmentException,
                                                                          ConflictException {
        try {
            envEngine.start(workspaceId,
                            envName,
                            environment,
                            recover,
                            new WebsocketMessageConsumer<>(format(ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE, workspaceId)),
                            new MachineAgentsLauncher(environment.getMachines()));
        } catch (EnvironmentStartInterruptedException x) {
            // environment start was interrupted, it's either shutdown or direct stop
            // in the case of shutdown make sure the status is correct,
            // otherwise workspace is already stopping
            compareAndSetStatus(workspaceId, WorkspaceStatus.STARTING, WorkspaceStatus.STOPPING);
            removeStateAndPublishStopEvents(workspaceId);
            throw x;
        } catch (EnvironmentException | ServerException | ConflictException x) {
            // environment can't be started for some reason, STARTING -> STOPPED
            removeState(workspaceId);
            eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                            .withWorkspaceId(workspaceId)
                                            .withEventType(EventType.ERROR)
                                            .withPrevStatus(WorkspaceStatus.STARTING)
                                            .withStatus(WorkspaceStatus.STOPPED)
                                            .withError("Start of environment '" + envName + "' failed. Error: " + x.getMessage()));
            throw x;
        }

        // disallow direct start cancellation, STARTING -> RUNNING
        WorkspaceStatus prevStatus;
        try (@SuppressWarnings("unused") Unlocker u = locks.writeLock(workspaceId)) {
            ensurePreDestroyIsNotExecuted();
            RuntimeState state = states.get(workspaceId);
            prevStatus = state.status;
            if (state.status == WorkspaceStatus.STARTING) {
                state.status = WorkspaceStatus.RUNNING;
                state.startTask = null;
                state.startFuture = null;
            }
        }

        // either current thread is interrupted right after status update,
        // or stop is called directly, anyway stop the environment
        if (Thread.interrupted() || prevStatus != WorkspaceStatus.STARTING) {
            try {
                stopEnvironmentAndPublishEvents(workspaceId, WorkspaceStatus.STARTING);
            } catch (Exception x) {
                LOG.error("Couldn't stop the environment '{}' of the workspace '{}'. Error: {}",
                          envName,
                          workspaceId,
                          x.getMessage());
            }
            throw new EnvironmentStartInterruptedException(workspaceId, envName);
        }

        // normally started, notify clients
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withStatus(WorkspaceStatus.RUNNING)
                                        .withEventType(EventType.RUNNING)
                                        .withPrevStatus(WorkspaceStatus.STARTING));
    }

    /** STOPPING -> remove runtime -> STOPPED. */
    private void removeStateAndPublishStopEvents(String workspaceId) {
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withPrevStatus(STARTING)
                                        .withStatus(WorkspaceStatus.STOPPING)
                                        .withEventType(EventType.STOPPING));
        removeState(workspaceId);
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withPrevStatus(WorkspaceStatus.STOPPING)
                                        .withEventType(EventType.STOPPED)
                                        .withStatus(WorkspaceStatus.STOPPED));
    }

    /**
     * Stops the workspace publishing all the necessary events.
     */
    private void stopEnvironmentAndPublishEvents(String workspaceId,
                                                 WorkspaceStatus prevStatus) throws ServerException,
                                                                                    EnvironmentException {
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withPrevStatus(prevStatus)
                                        .withStatus(WorkspaceStatus.STOPPING)
                                        .withEventType(EventType.STOPPING));
        removeState(workspaceId);
        try {
            envEngine.stop(workspaceId);
        } catch (Exception x) {
            eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                            .withWorkspaceId(workspaceId)
                                            .withPrevStatus(WorkspaceStatus.STOPPING)
                                            .withEventType(EventType.ERROR)
                                            .withError(x.getMessage())
                                            .withStatus(WorkspaceStatus.STOPPED));
            try {
                throw x;
            } catch (ServerException rethrow) {
                throw rethrow;
            } catch (Exception wrap) {
                throw new ServerException(wrap.getMessage(), wrap);
            }
        }
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withPrevStatus(WorkspaceStatus.STOPPING)
                                        .withEventType(EventType.STOPPED)
                                        .withStatus(WorkspaceStatus.STOPPED));
    }

    /**
     * Safely compares current status of given workspace
     * with {@code from} and if they are equal sets the status to {@code to}.
     * Returns true if the status of workspace was updated with {@code to} value.
     */
    private boolean compareAndSetStatus(String id, WorkspaceStatus from, WorkspaceStatus to) {
        try (@SuppressWarnings("unused") Unlocker u = locks.writeLock(id)) {
            ensurePreDestroyIsNotExecuted();
            RuntimeState state = states.get(id);
            if (state != null && state.status == from) {
                state.status = to;
                return true;
            }
        }
        return false;
    }

    /** Removes state from in-memory storage in write lock. */
    private void removeState(String workspaceId) {
        try (@SuppressWarnings("unused") Unlocker u = locks.writeLock(workspaceId)) {
            ensurePreDestroyIsNotExecuted();
            states.remove(workspaceId);
        }
    }

    /** Creates a snapshot and changes status SNAPSHOTTING -> RUNNING. */
    private void snapshotAndUpdateStatus(String workspaceId) throws NotFoundException,
                                                                    ConflictException,
                                                                    ServerException {
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withStatus(WorkspaceStatus.SNAPSHOTTING)
                                        .withEventType(EventType.SNAPSHOT_CREATING)
                                        .withPrevStatus(WorkspaceStatus.RUNNING));

        WorkspaceRuntimeImpl runtime = getRuntime(workspaceId);
        List<MachineImpl> machines = runtime.getMachines();
        machines.sort(comparing(m -> !m.getConfig().isDev(), Boolean::compare));

        LOG.info("Creating snapshot of workspace '{}', machines to snapshot: '{}'", workspaceId, machines.size());
        List<SnapshotImpl> newSnapshots = new ArrayList<>(machines.size());
        for (MachineImpl machine : machines) {
            try {
                newSnapshots.add(envEngine.saveSnapshot(workspaceId, machine.getId()));
            } catch (ServerException | NotFoundException x) {
                if (machine.getConfig().isDev()) {
                    compareAndSetStatus(workspaceId, WorkspaceStatus.SNAPSHOTTING, WorkspaceStatus.RUNNING);
                    eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                    .withWorkspaceId(workspaceId)
                                                    .withStatus(WorkspaceStatus.RUNNING)
                                                    .withEventType(EventType.SNAPSHOT_CREATION_ERROR)
                                                    .withPrevStatus(WorkspaceStatus.SNAPSHOTTING)
                                                    .withError(x.getMessage()));
                    throw x;
                }
                LOG.warn(format("Couldn't create snapshot of machine '%s:%s' in workspace '%s'",
                                machine.getEnvName(),
                                machine.getConfig().getName(),
                                workspaceId));
            }
        }

        LOG.info("Saving new snapshots metadata, workspace id '{}'", workspaceId);
        try {
            List<SnapshotImpl> removed = snapshotDao.replaceSnapshots(workspaceId,
                                                                      runtime.getActiveEnv(),
                                                                      newSnapshots);
            if (!removed.isEmpty()) {
                LOG.info("Removing old snapshots binaries, workspace id '{}', snapshots to remove '{}'", workspaceId, removed.size());
                removeBinaries(removed);
            }
        } catch (SnapshotException x) {
            LOG.error(format("Couldn't remove existing snapshots metadata for workspace '%s'", workspaceId), x);
            LOG.info("Removing newly created snapshots, workspace id '{}', snapshots to remove '{}'", workspaceId, newSnapshots.size());
            removeBinaries(newSnapshots);
            compareAndSetStatus(workspaceId, WorkspaceStatus.SNAPSHOTTING, WorkspaceStatus.RUNNING);
            eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                            .withWorkspaceId(workspaceId)
                                            .withStatus(WorkspaceStatus.RUNNING)
                                            .withEventType(EventType.SNAPSHOT_CREATION_ERROR)
                                            .withPrevStatus(WorkspaceStatus.SNAPSHOTTING)
                                            .withError(x.getMessage()));
            throw x;
        }
        compareAndSetStatus(workspaceId, WorkspaceStatus.SNAPSHOTTING, WorkspaceStatus.RUNNING);
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withStatus(WorkspaceStatus.RUNNING)
                                        .withWorkspaceId(workspaceId)
                                        .withEventType(EventType.SNAPSHOT_CREATED)
                                        .withPrevStatus(WorkspaceStatus.SNAPSHOTTING));
    }

    /** Holds runtime information while workspace is running. */
    @VisibleForTesting
    static class RuntimeState {

        WorkspaceStatus              status;
        String                       envName;
        StartTask                    startTask;
        Future<WorkspaceRuntimeImpl> startFuture;

        RuntimeState(RuntimeState state) {
            this.status = state.status;
            this.envName = state.envName;
            this.startFuture = state.startFuture;
            this.startTask = state.startTask;
        }

        RuntimeState(WorkspaceStatus status,
                     String envName,
                     StartTask startTask,
                     Future<WorkspaceRuntimeImpl> startFuture) {
            this.status = status;
            this.envName = envName;
            this.startTask = startTask;
            this.startFuture = startFuture;
        }
    }

    @VisibleForTesting
    class StartTask implements Callable<WorkspaceRuntimeImpl> {

        final String                                  workspaceId;
        final String                                  envName;
        final EnvironmentImpl                         environment;
        final boolean                                 recover;
        final CompletableFuture<WorkspaceRuntimeImpl> cmpFuture;
        final AtomicBoolean                           used;
        final CountDownLatch                          allowStartLatch;
        final CountDownLatch                          completionLatch;

        volatile Exception exception;

        StartTask(String workspaceId,
                  String envName,
                  EnvironmentImpl environment,
                  boolean recover,
                  CompletableFuture<WorkspaceRuntimeImpl> cmpFuture) {
            this.workspaceId = workspaceId;
            this.envName = envName;
            this.environment = environment;
            this.recover = recover;
            this.cmpFuture = cmpFuture;
            this.used = new AtomicBoolean(false);
            this.completionLatch = new CountDownLatch(1);
            this.allowStartLatch = new CountDownLatch(1);
        }

        @Override
        public WorkspaceRuntimeImpl call() throws Exception {
            if (!markAsUsed()) {
                throw new CancellationException(format("Start of the workspace '%s' was cancelled", workspaceId));
            }
            allowStartLatch.await();
            try {
                startEnvironmentAndPublishEvents(environment, workspaceId, envName, recover);
                WorkspaceRuntimeImpl runtime = getRuntime(workspaceId);
                cmpFuture.complete(runtime);
                return runtime;
            } catch (IllegalStateException illegalStateEx) {
                if (isPreDestroyInvoked) {
                    exception = new EnvironmentStartInterruptedException(workspaceId, envName);
                } else {
                    exception = new ServerException(illegalStateEx.getMessage(), illegalStateEx);
                }
                cmpFuture.completeExceptionally(exception);
                throw exception;
            } catch (Exception occurred) {
                cmpFuture.completeExceptionally(exception = occurred);
                throw occurred;
            } finally {
                completionLatch.countDown();
            }
        }

        /**
         * Awaits this task to complete, rethrows exceptions occurred during the invocation.
         */
        void await() throws InterruptedException,
                            ServerException,
                            ConflictException,
                            EnvironmentException {
            completionLatch.await();
            if (exception != null) {
                try {
                    throw exception;
                } catch (ServerException | EnvironmentException | ConflictException rethrow) {
                    throw rethrow;
                } catch (Exception x) {
                    throw new ServerException(x.getMessage(), x);
                }
            }
        }

        /**
         * Completes corresponding completable future exceptionally
         * with {@link EnvironmentStartInterruptedException}.
         */
        void earlyComplete() {
            exception = new EnvironmentStartInterruptedException(workspaceId, envName);
            cmpFuture.completeExceptionally(exception);
            completionLatch.countDown();
        }

        /**
         * Marks this task as used, returns true only if it was unused before.
         */
        boolean markAsUsed() {
            return used.compareAndSet(false, true);
        }

        /**
         * Allows start of this task.
         * The task caller will wait until this method is called.
         */
        void unlockStart() {
            allowStartLatch.countDown();
        }
    }

    private class MachineAgentsLauncher implements MachineStartedHandler {

        private final Map<String, ? extends ExtendedMachine> nameToMachine;

        private MachineAgentsLauncher(Map<String, ? extends ExtendedMachine> nameToMachine) {
            this.nameToMachine = nameToMachine;
        }

        @Override
        public void started(Instance machine) throws ServerException {
            ExtendedMachine extMachine = nameToMachine.get(machine.getConfig().getName());
            if (extMachine != null) {
                launchAgents(machine, extMachine.getAgents());
            }
        }
    }

    private static EnvironmentImpl copyEnv(Workspace workspace, String envName) {
        Environment environment = workspace.getConfig().getEnvironments().get(envName);
        if (environment == null) {
            throw new IllegalArgumentException(format("Workspace '%s' doesn't contain environment '%s'",
                                                      workspace.getId(),
                                                      envName));
        }
        return new EnvironmentImpl(environment);
    }
}

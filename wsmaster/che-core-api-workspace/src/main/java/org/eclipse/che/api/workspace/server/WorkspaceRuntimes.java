/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineLogMessage;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.AbstractMessageConsumer;
import org.eclipse.che.api.core.util.MessageConsumer;
import org.eclipse.che.api.core.util.WebsocketMessageConsumer;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.exception.EnvironmentNotRunningException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.SNAPSHOTTING;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Defines an internal API for managing {@link WorkspaceRuntimeImpl} instances.
 *
 * <p>This component implements {@link WorkspaceStatus} contract.
 *
 * <p>All the operations performed by this component are synchronous.
 *
 * <p>The implementation is thread-safe and guarded by
 * eagerly initialized readwrite locks produced by {@link StripedLocks}.
 * The component doesn't expose any api for client-side locking.
 * All the instances produced by this component are copies of the real data.
 *
 * <p>The component doesn't check if the incoming objects are in application-valid state.
 * Which means that it is expected that if {@link #start(Workspace, String, boolean)} method is called
 * then {@code WorkspaceImpl} argument is a application-valid object which contains
 * all the required data for performing start.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceRuntimes {

    private static final Logger LOG = getLogger(WorkspaceRuntimes.class);

    @VisibleForTesting
    final         Map<String, WorkspaceState> workspaces;
    private final EventService                eventService;
    private final StripedLocks                stripedLocks;
    private final CheEnvironmentEngine        environmentEngine;
    private final AgentSorter                 agentSorter;
    private final AgentLauncherFactory        launcherFactory;
    private final AgentRegistry               agentRegistry;
    private final ExecutorService             executor;

    private volatile boolean isPreDestroyInvoked;

    @Inject
    public WorkspaceRuntimes(EventService eventService,
                             CheEnvironmentEngine environmentEngine,
                             AgentSorter agentSorter,
                             AgentLauncherFactory launcherFactory,
                             AgentRegistry agentRegistry) {
        this.eventService = eventService;
        this.environmentEngine = environmentEngine;
        this.agentSorter = agentSorter;
        this.launcherFactory = launcherFactory;
        this.agentRegistry = agentRegistry;
        this.workspaces = new HashMap<>();
        // 16 - experimental value for stripes count, it comes from default hash map size
        this.stripedLocks = new StripedLocks(16);
        executor = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors(),
                                                new ThreadFactoryBuilder().setNameFormat("WorkspaceRuntimes-%d")
                                                                          .setDaemon(false)
                                                                          .build());
    }

    /**
     * Returns the runtime descriptor describing currently starting/running/stopping
     * workspace runtime.
     *
     * <p>Note that the {@link RuntimeDescriptor#getRuntime()} method
     * returns a copy of a real {@code WorkspaceRuntime} object,
     * which means that any runtime copy modifications won't affect the
     * real object and also it means that copy won't be affected with modifications applied
     * to the real runtime workspace object state.
     *
     * @param workspaceId
     *         the id of the workspace to get its runtime
     * @return descriptor which describes current state of the workspace runtime
     * @throws NotFoundException
     *         when workspace with given {@code workspaceId} is not found
     * @throws ServerException
     *         if environment is in illegal state
     */
    public RuntimeDescriptor get(String workspaceId) throws NotFoundException,
                                                            ServerException {
        WorkspaceState workspaceState;
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            workspaceState = workspaces.get(workspaceId);
        }
        if (workspaceState == null) {
            throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
        }

        RuntimeDescriptor runtimeDescriptor = new RuntimeDescriptor(workspaceState.status,
                                                                    new WorkspaceRuntimeImpl(workspaceState.activeEnv,
                                                                                             null,
                                                                                             Collections.emptyList(),
                                                                                             null));
        List<Instance> machines = environmentEngine.getMachines(workspaceId);
        Optional<Instance> devMachineOptional = machines.stream()
                                                        .filter(machine -> machine.getConfig().isDev())
                                                        .findAny();
        if (devMachineOptional.isPresent()) {
            String projectsRoot = devMachineOptional.get().getStatus() == MachineStatus.RUNNING ?
                                  devMachineOptional.get().getRuntime().projectsRoot() :
                                  null;
            runtimeDescriptor.setRuntime(new WorkspaceRuntimeImpl(workspaceState.activeEnv,
                                                                  projectsRoot,
                                                                  machines,
                                                                  devMachineOptional.get()));
        } else if (workspaceState.status == WorkspaceStatus.RUNNING) {
            // invalid state of environment is detected
            String error = format("Dev machine is not found in active environment of workspace '%s'",
                                  workspaceId);
            throw new ServerException(error);
        }

        return runtimeDescriptor;
    }

    /**
     * Starts all machines from specified workspace environment,
     * creates workspace runtime instance based on that environment.
     *
     * <p>During the start of the workspace its
     * runtime is visible with {@link WorkspaceStatus#STARTING} status.
     *
     * @param workspace
     *         workspace which environment should be started
     * @param envName
     *         the name of the environment to start
     * @param recover
     *         whether machines should be recovered(true) or not(false)
     * @return the workspace runtime instance with machines set.
     * @throws ConflictException
     *         when workspace is already running
     * @throws ConflictException
     *         when start is interrupted
     * @throws NotFoundException
     *         when any not found exception occurs during environment start
     * @throws ServerException
     *         when component {@link #isPreDestroyInvoked is stopped}
     * @throws ServerException
     *         other error occurs during environment start
     * @see CheEnvironmentEngine#start(String, String, Environment, boolean, MessageConsumer)
     * @see WorkspaceStatus#STARTING
     * @see WorkspaceStatus#RUNNING
     */
    public RuntimeDescriptor start(Workspace workspace,
                                   String envName,
                                   boolean recover) throws ServerException,
                                                           ConflictException,
                                                           NotFoundException {
        final EnvironmentImpl environment = copyEnv(workspace, envName);
        final String workspaceId = workspace.getId();
        initState(workspaceId, workspace.getConfig().getName(), envName);
        doStart(environment, workspaceId, envName, recover);
        return get(workspaceId);
    }

    /**
     * Starts the workspace like {@link #start(Workspace, String, boolean)}
     * method does, but asynchronously. Nonetheless synchronously checks that workspace
     * doesn't have runtime and makes it {@link WorkspaceStatus#STARTING}.
     */
    public Future<RuntimeDescriptor> startAsync(Workspace workspace,
                                                String envName,
                                                boolean recover) throws ConflictException, ServerException {
        final EnvironmentImpl environment = copyEnv(workspace, envName);
        final String workspaceId = workspace.getId();
        initState(workspaceId, workspace.getConfig().getName(), envName);
        return executor.submit(ThreadLocalPropagateContext.wrap(() -> {
            doStart(environment, workspaceId, envName, recover);
            return get(workspaceId);
        }));
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
    public void stop(String workspaceId) throws NotFoundException, ServerException, ConflictException {
        // This check allows to exit with an appropriate exception before blocking on lock.
        // The double check is required as it is still possible to get unlucky timing
        // between locking and stopping workspace.
        ensurePreDestroyIsNotExecuted();
        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
            ensurePreDestroyIsNotExecuted();
            WorkspaceState workspaceState = workspaces.get(workspaceId);
            if (workspaceState == null) {
                throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
            }
            if (workspaceState.status != WorkspaceStatus.RUNNING) {
                throw new ConflictException(
                        format("Couldn't stop '%s' workspace because its status is '%s'. Workspace can be stopped only if it is 'RUNNING'",
                               workspaceId,
                               workspaceState.status));
            }

            workspaceState.status = WorkspaceStatus.STOPPING;
        }

        publishWorkspaceEvent(EventType.STOPPING, workspaceId, null);
        String error = null;
        try {
            environmentEngine.stop(workspaceId);
        } catch (ServerException | RuntimeException e) {
            error = e.getLocalizedMessage();
        } finally {
            try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
                workspaces.remove(workspaceId);
            }
        }
        if (error == null) {
            publishWorkspaceEvent(EventType.STOPPED, workspaceId, null);
        } else {
            publishWorkspaceEvent(EventType.ERROR, workspaceId, error);
        }
    }

    /**
     * Returns true if workspace was started and its status is
     * {@link WorkspaceStatus#RUNNING running}, {@link WorkspaceStatus#STARTING starting}
     * or {@link WorkspaceStatus#STOPPING stopping} - otherwise returns false.
     *
     * <p> This method is less expensive alternative to {@link #get(String)} + {@code try catch}, see example:
     * <pre>{@code
     *
     *     if (!runtimes.hasRuntime("workspace123")) {
     *         doStuff("workspace123");
     *     }
     *
     *     //vs
     *
     *     try {
     *         runtimes.get("workspace123");
     *     } catch (NotFoundException ex) {
     *         doStuff("workspace123");
     *     }
     *
     * }</pre>
     *
     * @param workspaceId
     *         workspace identifier to perform check
     * @return true if workspace is running, otherwise false
     */
    public boolean hasRuntime(String workspaceId) {
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            return workspaces.containsKey(workspaceId);
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

        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            getRunningState(workspaceId);
        }

        // Copy constructor makes deep copy of objects graph
        // which means that original values won't affect the values in used further in this class
        MachineConfigImpl machineConfigCopy = new MachineConfigImpl(machineConfig);

        List<String> agents = Collections.singletonList("org.eclipse.che.terminal");

        Instance instance = environmentEngine.startMachine(workspaceId, machineConfigCopy, agents);
        launchAgents(instance, agents);

        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
            WorkspaceState workspaceState = workspaces.get(workspaceId);
            if (workspaceState == null || workspaceState.status != RUNNING) {
                try {
                    environmentEngine.stopMachine(workspaceId, instance.getId());
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
     * Changes workspace runtimes status from RUNNING to SNAPSHOTTING.
     *
     * @param workspaceId
     *         the id of the workspace to begin snapshotting for
     * @throws NotFoundException
     *         when workspace with such id doesn't have runtime
     * @throws ConflictException
     *         when workspace status is different from SNAPSHOTTING
     * @see WorkspaceStatus#SNAPSHOTTING
     */
    public void beginSnapshotting(String workspaceId) throws NotFoundException, ConflictException {
        try (StripedLocks.WriteLock ignored = stripedLocks.acquireWriteLock(workspaceId)) {
            getRunningState(workspaceId).status = SNAPSHOTTING;
        }
    }

    /**
     * Changes workspace runtimes status from SNAPSHOTTING back to RUNNING.
     * This method won't affect workspace runtime or throw any exception
     * if workspace is not in running status or doesn't have runtime.
     *
     * @param workspaceId
     *         the id of the workspace to finish snapshotting for
     * @see WorkspaceStatus#SNAPSHOTTING
     */
    public void finishSnapshotting(String workspaceId) {
        try (StripedLocks.WriteLock ignored = stripedLocks.acquireWriteLock(workspaceId)) {
            final WorkspaceState state = workspaces.get(workspaceId);
            if (state != null && state.status == SNAPSHOTTING) {
                state.status = RUNNING;
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
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            WorkspaceState workspaceState = workspaces.get(workspaceId);
            if (workspaceState == null || workspaceState.status != RUNNING) {
                throw new ConflictException(format("Environment of workspace '%s' is not running", workspaceId));
            }
        }
        environmentEngine.stopMachine(workspaceId, machineId);
    }

    /**
     * Save snapshot of running machine.
     *
     * @param namespace
     *         namespace of workspace
     * @param workspaceId
     *         ID of workspace that owns machine
     * @param machineId
     *         ID of machine that should be saved
     * @return snapshot description
     * @throws NotFoundException
     *         if machine is not running
     * @throws ConflictException
     *         if environment of machine is not running
     * @throws ServerException
     *         if another error occurs
     */
    public SnapshotImpl saveMachine(String namespace,
                                    String workspaceId,
                                    String machineId) throws NotFoundException,
                                                             ServerException,
                                                             ConflictException {

        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            WorkspaceState workspaceState = workspaces.get(workspaceId);
            if (workspaceState == null || !(workspaceState.status == SNAPSHOTTING || workspaceState.status == RUNNING)) {
                throw new ConflictException(format("Environment of workspace '%s' is not running or snapshotting", workspaceId));
            }
        }
        return environmentEngine.saveSnapshot(namespace, workspaceId, machineId);
    }

    /**
     * Removes implementation specific representation of snapshot.
     *
     * @param snapshot
     *         description of snapshot that should be removed
     * @throws NotFoundException
     *         if snapshot is not found
     * @throws ServerException
     *         if error occurs
     */
    public void removeSnapshot(SnapshotImpl snapshot) throws ServerException, NotFoundException {
        environmentEngine.removeSnapshot(snapshot);
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
        return environmentEngine.getMachine(workspaceId, machineId);
    }

    /**
     * Returns all workspaces with statuses of its active environment.
     */
    public Map<String, WorkspaceState> getWorkspaces() {
        return new HashMap<>(workspaces);
    }

    private MessageConsumer<MachineLogMessage> getEnvironmentLogger(String workspaceId) throws ServerException {
        WebsocketMessageConsumer<MachineLogMessage> envMessageConsumer =
                new WebsocketMessageConsumer<>(format(ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE, workspaceId));
        return new AbstractMessageConsumer<MachineLogMessage>() {
            @Override
            public void consume(MachineLogMessage message) throws IOException {
                envMessageConsumer.consume(message);
            }
        };
    }

    /**
     * Removes all workspaces from the in-memory storage, while
     * {@link CheEnvironmentEngine} is responsible for environment destroying.
     */
    @PreDestroy
    @VisibleForTesting
    void cleanup() {
        isPreDestroyInvoked = true;

        try (StripedLocks.WriteAllLock lock = stripedLocks.acquireWriteAllLock()) {
            for (Map.Entry<String, WorkspaceState> workspace : workspaces.entrySet()) {
                if (workspace.getValue().status.equals(RUNNING) ||
                    workspace.getValue().status.equals(WorkspaceStatus.STARTING)) {
                    executor.execute(() -> {
                        try {
                            environmentEngine.stop(workspace.getKey());
                        } catch (ServerException | NotFoundException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    });
                }
            }

            workspaces.clear();

            executor.shutdown();
        }
        try {
            if (!executor.awaitTermination(50, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.warn("Unable terminate destroy machines pool");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @VisibleForTesting
    void publishWorkspaceEvent(EventType type, String workspaceId, String error) {
        eventService.publish(newDto(WorkspaceStatusEvent.class)
                                     .withEventType(type)
                                     .withWorkspaceId(workspaceId)
                                     .withError(error));
    }

    private void ensurePreDestroyIsNotExecuted() throws ServerException {
        if (isPreDestroyInvoked) {
            throw new ServerException("Could not perform operation because application server is stopping");
        }
    }

    private WorkspaceState getRunningState(String workspaceId) throws NotFoundException, ConflictException {
        final WorkspaceState state = workspaces.get(workspaceId);
        if (state == null) {
            throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running");
        }
        if (state.getStatus() != RUNNING) {
            throw new ConflictException(format("Workspace with id '%s' is not in 'RUNNING', it's status is '%s'",
                                               workspaceId,
                                               state.getStatus()));
        }
        return state;
    }

    protected void launchAgents(Instance instance, List<String> agents) throws ServerException {
        try {
            for (AgentKey agentKey : agentSorter.sort(agents)) {
                LOG.info("Launching '{}' agent at workspace {}", agentKey.getId(), instance.getWorkspaceId());

                Agent agent = agentRegistry.getAgent(agentKey);
                AgentLauncher launcher = launcherFactory.find(agentKey.getId(), instance.getConfig().getType());
                launcher.launch(instance, agent);
            }
        } catch (AgentException e) {
            throw new MachineException(e.getMessage(), e);
        }
    }

    /**
     * Initializes workspace in {@link WorkspaceStatus#STARTING} status,
     * saves the state or throws an appropriate exception if the workspace is already initialized.
     */
    private void initState(String workspaceId, String workspaceName, String envName) throws ConflictException, ServerException {
        try (StripedLocks.WriteLock ignored = stripedLocks.acquireWriteLock(workspaceId)) {
            ensurePreDestroyIsNotExecuted();
            final WorkspaceState state = workspaces.get(workspaceId);
            if (state != null) {
                throw new ConflictException(format("Could not start workspace '%s' because its status is '%s'",
                                                   workspaceName,
                                                   state.status));
            }
            workspaces.put(workspaceId, new WorkspaceState(WorkspaceStatus.STARTING, envName));
        }
    }

    /** Starts the machine instances. */
    private void doStart(EnvironmentImpl environment,
                         String workspaceId,
                         String envName,
                         boolean recover) throws ServerException {
        publishWorkspaceEvent(EventType.STARTING, workspaceId, null);
        try {
            List<Instance> machines = environmentEngine.start(workspaceId,
                                                              envName,
                                                              environment,
                                                              recover,
                                                              getEnvironmentLogger(workspaceId));
            launchAgents(environment, machines);

            try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
                WorkspaceState workspaceState = workspaces.get(workspaceId);
                workspaceState.status = WorkspaceStatus.RUNNING;
            }
            // Event publication should be performed outside of the lock
            // as it may take some time to notify subscribers
            publishWorkspaceEvent(EventType.RUNNING, workspaceId, null);
        } catch (ApiException | EnvironmentException | RuntimeException e) {
            try {
                environmentEngine.stop(workspaceId);
            } catch (EnvironmentNotRunningException ignore) {
            } catch (Exception ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            String environmentStartError = "Start of environment " + envName +
                                           " failed. Error: " + e.getLocalizedMessage();
            try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
                workspaces.remove(workspaceId);
            }
            publishWorkspaceEvent(EventType.ERROR,
                                  workspaceId,
                                  environmentStartError);

            throw new ServerException(environmentStartError, e);
        }
    }

    private void launchAgents(EnvironmentImpl environment, List<Instance> machines) throws ServerException {
        for (Instance instance : machines) {
            Map<String, ExtendedMachineImpl> envMachines = environment.getMachines();
            if (envMachines != null) {
                ExtendedMachine extendedMachine = envMachines.get(instance.getConfig().getName());
                if (extendedMachine != null) {
                    List<String> agents = extendedMachine.getAgents();
                    launchAgents(instance, agents);
                }
            }
        }
    }

    private static EnvironmentImpl copyEnv(Workspace workspace, String envName) {
        final Environment environment = workspace.getConfig().getEnvironments().get(envName);
        if (environment == null) {
            throw new IllegalArgumentException(format("Workspace '%s' doesn't contain environment '%s'",
                                                      workspace.getId(),
                                                      envName));
        }
        return new EnvironmentImpl(environment);
    }

    public static class WorkspaceState {
        private WorkspaceStatus status;
        private String          activeEnv;

        public WorkspaceState(WorkspaceStatus status, String activeEnv) {
            this.status = status;
            this.activeEnv = activeEnv;
        }

        public String getActiveEnv() {
            return activeEnv;
        }

        public WorkspaceStatus getStatus() {
            return status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WorkspaceState)) return false;
            WorkspaceState that = (WorkspaceState)o;
            return status == that.status &&
                   Objects.equals(activeEnv, that.activeEnv);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, activeEnv);
        }
    }

    /**
     * Wrapper for the {@link WorkspaceRuntime} instance.
     * Knows the state of the started workspace runtime,
     * helps to postpone {@code WorkspaceRuntime} instance creation to
     * the time when all the machines from the workspace are created.
     */
    public static class RuntimeDescriptor {

        private WorkspaceRuntimeImpl runtime;
        private WorkspaceStatus      status;

        public RuntimeDescriptor(WorkspaceStatus workspaceStatus,
                                 WorkspaceRuntimeImpl runtime) {
            this.status = workspaceStatus;
            this.runtime = runtime;
        }

        /** Returns the instance of {@code WorkspaceRuntime} described by this descriptor. */
        public WorkspaceRuntimeImpl getRuntime() {
            return runtime;
        }

        public void setRuntime(WorkspaceRuntimeImpl runtime) {
            this.runtime = runtime;
        }

        /**
         * Returns the status of the {@code WorkspaceRuntime} described by this descriptor.
         * Never returns {@link WorkspaceStatus#STOPPED} status, you'll rather get {@link NotFoundException}
         * from {@link #get(String)} method.
         */
        public WorkspaceStatus getRuntimeStatus() {
            return status;
        }

        private void setRuntimeStatus(WorkspaceStatus status) {
            this.status = status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RuntimeDescriptor)) return false;
            RuntimeDescriptor that = (RuntimeDescriptor)o;
            return Objects.equals(runtime, that.runtime) &&
                   status == that.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(runtime, status);
        }

        @Override
        public String toString() {
            return "RuntimeDescriptor{" +
                   "runtime=" + runtime +
                   ", status=" + status +
                   '}';
        }
    }
}

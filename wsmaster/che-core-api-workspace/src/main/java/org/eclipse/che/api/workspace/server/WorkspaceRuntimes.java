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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineLogMessage;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.AbstractMessageConsumer;
import org.eclipse.che.api.core.util.MessageConsumer;
import org.eclipse.che.api.core.util.WebsocketMessageConsumer;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.NotSupportedException;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.ValidationException;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.StripedLocks;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE;
import static org.slf4j.LoggerFactory.getLogger;

//import org.eclipse.che.api.machine.server.spi.Runtime;

/**
 * Defines an internal API for managing {@link RuntimeImpl} instances.
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
 * Which means that it is expected that if {start(Workspace, String, boolean)} method is called
 * then {@code WorkspaceImpl} argument is a application-valid object which contains
 * all the required data for performing start.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceRuntimes {

    private static final Logger LOG = getLogger(WorkspaceRuntimes.class);

    private final Map<String, RuntimeInfrastructure> infraByRecipe;

    private final ConcurrentMap<String, InternalRuntime> runtimes;
    private final EventService                           eventsService;
    private final StripedLocks                           locks;
    private final WorkspaceSharedPool                    sharedPool;

    @Inject
    public WorkspaceRuntimes(EventService eventsService,
                             Set<RuntimeInfrastructure> infrastructures,
                             WorkspaceSharedPool sharedPool) {


        this.eventsService = eventsService;
        this.runtimes = new ConcurrentHashMap<>();

        this.infraByRecipe = new ConcurrentHashMap<>();

        //TODO move it to post-create?
        for (RuntimeInfrastructure infra : infrastructures) {

            // If several entries - last wins (TODO should we throw conflict exception?)
            for (String type : infra.getRecipeTypes())
                infraByRecipe.put(type, infra);

            // try to recover from infrastructures
            try {
                for (RuntimeIdentity id : infra.getIdentities()) {
                    runtimes.put(id.getWorkspaceId(), validate(infra.getRuntime(id)));
                }
            } catch (NotSupportedException e) {
                LOG.warn("Not recoverable infrastructure: " + infra.getName() + " Reason: " + e.getMessage());
            }

        }
        //

        // 16 - experimental value for stripes count, it comes from default hash map size
        this.locks = new StripedLocks(16);
        this.sharedPool = sharedPool;

    }


    //TODO do we need some validation on start
    private InternalRuntime validate(InternalRuntime runtime) {
        return runtime;
    }


    public Environment estimate(Environment environment) throws NotFoundException, ServerException, ValidationException {

        String type = environment.getRecipe().getType();
        if (!infraByRecipe.containsKey(type))
            throw new NotFoundException("Ifrastructure not found for type: " + type);

        return infraByRecipe.get(type).estimate(environment);
    }


    /**
     * Returns the runtime descriptor describing currently starting/running/stopping
     * workspace runtime.
     *
     * returns a copy of a real {@code Runtime} object,
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
    public Runtime get(String workspaceId) throws NotFoundException, ServerException {

        InternalRuntime runtime = runtimes.get(workspaceId);
        if (runtime != null)
            return runtime;
        else
            throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");

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
     * @param options
     *         whether machines should be recovered(true) or not(false)
     * @return the workspace runtime instance with machines set.
     * @throws ConflictException
     *         when workspace is already running
     * @throws ConflictException
     *         when start is interrupted
     * @throws NotFoundException
     *         when any not found exception occurs during environment start
     * @throws ServerException
     *         other error occurs during environment start
     * @see WorkspaceStatus#STARTING
     * @see WorkspaceStatus#RUNNING
     */
    public Runtime start(Workspace workspace,
                         String envName,
                         Map<String, String> options) throws ApiException, ValidationException, IOException {

        final EnvironmentImpl environment = copyEnv(workspace, envName);
        final String workspaceId = workspace.getId();
        doStart(environment, workspaceId, envName, options);
        return get(workspaceId);
    }

    /**
     * Starts the workspace like
     * method does, but asynchronously. Nonetheless synchronously checks that workspace
     * doesn't have runtime and makes it {@link WorkspaceStatus#STARTING}.
     */
    public Future<Runtime> startAsync(Workspace workspace,
                                      String envName,
                                      Map<String, String> options) throws ConflictException, ServerException {

        final EnvironmentImpl environment = copyEnv(workspace, envName);
        final String workspaceId = workspace.getId();
        return sharedPool.submit(() -> {
            doStart(environment, workspaceId, envName, options);
            return get(workspaceId);
        });
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
     * @see WorkspaceStatus#STOPPING
     */
    public void stop(String workspaceId, Map<String, String> options) throws NotFoundException, ServerException, ConflictException {

        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withPrevStatus(WorkspaceStatus.RUNNING)
                                        .withStatus(WorkspaceStatus.STOPPING)
                                        .withEventType(EventType.STOPPING));

        InternalRuntime runtime = runtimes.get(workspaceId);
        if (runtime == null)
            throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");

        runtime.getContext().stop(options);

        runtimes.remove(workspaceId);

        final WorkspaceStatusEvent event = DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                     .withWorkspaceId(workspaceId)
                                                     .withPrevStatus(WorkspaceStatus.STOPPING);
        event.setStatus(WorkspaceStatus.STOPPED);
        event.setEventType(EventType.STOPPED);
        eventsService.publish(event);
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
        return runtimes.containsKey(workspaceId);
    }


    private void doStart(EnvironmentImpl environment,
                         String workspaceId, String envName,
                         Map<String, String> options) throws ApiException, ValidationException, IOException {

        requireNonNull(environment, "Environment should not be null " + workspaceId);
        requireNonNull(environment.getRecipe(), "OldRecipe should not be null " + workspaceId);
        requireNonNull(environment.getRecipe().getType(), "OldRecipe type should not be null " + workspaceId);

        RuntimeInfrastructure infra = infraByRecipe.get(environment.getRecipe().getType());
        if (infra == null)
            throw new NotFoundException("No infrastructure found of type: " + environment.getRecipe().getType() +
                                        " for workspace: " + workspaceId);

        if (runtimes.containsKey(workspaceId))
            throw new ConflictException("Could not start workspace '" + workspaceId + "' because its status is 'RUNNING'");

        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withStatus(WorkspaceStatus.STARTING)
                                        .withEventType(EventType.STARTING)
                                        .withPrevStatus(WorkspaceStatus.STOPPED));


        // Start environment
        //MessageConsumer<MachineLogMessage> logger = getEnvironmentLogger(workspaceId);
        if (options == null)
            options = new HashMap<>();

        Subject subject = EnvironmentContext.getCurrent().getSubject();
        RuntimeIdentity runtimeId = new RuntimeIdentity(workspaceId, envName, subject.getUserName());

        InternalRuntime runtime = infra.prepare(runtimeId, environment).start(options);

        if (runtime == null)
            throw new ServerException("SPI contract violated. RuntimeInfrastructure.start(...) must not return null: "
                                      + RuntimeInfrastructure.class);


//        // Phase 2: start agents if any
//        for (Map.Entry<String, MachineConfigImpl> machineEntry : environment.getMachines().entrySet()) {
//            if (!machineEntry.getValue().getAgents().isEmpty()) {
//                Machine machine = runtime.getMachines().get(machineEntry.getKey());
//                // TODO
//                // installAgents(machine);
//            }
//        }

        runtimes.put(workspaceId, runtime);
        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withStatus(WorkspaceStatus.RUNNING)
                                        .withEventType(EventType.RUNNING)
                                        .withPrevStatus(WorkspaceStatus.STARTING));

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

    private static EnvironmentImpl copyEnv(Workspace workspace, String envName) {

        requireNonNull(workspace, "Workspace should not be null.");
        requireNonNull(envName, "Environment name should not be null.");
        final Environment environment = workspace.getConfig().getEnvironments().get(envName);
        if (environment == null) {
            throw new IllegalArgumentException(format("Workspace '%s' doesn't contain environment '%s'",
                                                      workspace.getId(),
                                                      envName));
        }
        return new EnvironmentImpl(environment);
    }

//    private LineConsumer getMachineLogger(MessageConsumer<MachineLogMessage> environmentLogger,
//                                          String machineId,
//                                          String machineName) throws ServerException {
//        createMachineLogsDir(machineId);
//
//        LineConsumer lineConsumer = new AbstractLineConsumer() {
//            @Override
//            public void writeLine(String line) throws IOException {
//                environmentLogger.consume(new MachineLogMessageImpl(machineName, line));
//            }
//        };
//        try {
//            return new ConcurrentCompositeLineConsumer(new ConcurrentFileLineConsumer(getMachineLogsFile(machineId)),
//                                                       lineConsumer);
//        } catch (IOException e) {
//            throw new MachineException(format("Unable create log file '%s' for machine '%s'.",
//                                              e.getLocalizedMessage(),
//                                              machineId));
//        }
//    }
//
//    private void createMachineLogsDir(String machineId) throws MachineException {
//        File dir = new File(machineLogsDir, machineId);
//        if (!dir.exists() && !dir.mkdirs()) {
//            throw new MachineException("Can't create folder for the logs of machine");
//        }
//    }
//
//    private File getMachineLogsFile(String machineId) {
//        return new File(new File(machineLogsDir, machineId), "machineId.logs");
//    }
//

//    /**
//     * Removes all workspaces from the in-memory storage, while
//     * {@link CheEnvironmentEngine} is responsible for environment destroying.
//     */
//    @PreDestroy
//    @VisibleForTesting
//    void cleanup() {
//        isPreDestroyInvoked = true;
//
//        // wait existing tasks to complete
//        sharedPool.terminateAndWait();
//
//        List<String> idsToStop;
//        try (@SuppressWarnings("unused") CloseableLock l = locks.acquireWriteAllLock()) {
//            idsToStop = workspaces.entrySet()
//                                  .stream()
//                                  .filter(e -> e.getValue().status != STOPPING)
//                                  .map(Map.Entry::getKey)
//                                  .collect(Collectors.toList());
//            workspaces.clear();
//        }
//
//        // nothing to stop
//        if (idsToStop.isEmpty()) {
//            return;
//        }
//
//        LOG.info("Shutdown running workspaces, workspaces to shutdown '{}'", idsToStop.size());
//        ExecutorService executor =
//                Executors.newFixedThreadPool(2 * java.lang.Runtime.getRuntime().availableProcessors(),
//                                             new ThreadFactoryBuilder().setNameFormat("StopEnvironmentsPool-%d")
//                                                                       .setDaemon(false)
//                                                                       .build());
//        for (String id : idsToStop) {
//            executor.execute(() -> {
//                try {
//                    networks.stop(id);
//                } catch (Exception x) {
//                    LOG.error(x.getMessage(), x);
//                }
//            });
//        }
//
//        executor.shutdown();
//        try {
//            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
//                executor.shutdownNow();
//                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                    LOG.error("Unable terminate machines pool");
//                }
//            }
//        } catch (InterruptedException e) {
//            executor.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    private void ensurePreDestroyIsNotExecuted() throws ServerException {
//        if (isPreDestroyInvoked) {
//            throw new ServerException("Could not perform operation because application server is stopping");
//        }
//    }
//
//    protected void launchAgents(Runtime instance, List<String> agents) throws ServerException {
//        try {
//            for (AgentKey agentKey : agentSorter.sort(agents)) {
//                LOG.info("Launching '{}' agent at workspace {}", agentKey.getId(), instance.getMachine().getWorkspaceId());
//
//                Agent agent = agentRegistry.getAgent(agentKey);
//                AgentLauncher launcher = launcherFactory.find(agentKey.getId(), instance.getConfig().getType());
//                launcher.launch(instance, agent);
//            }
//        } catch (AgentException e) {
//            throw new MachineException(e.getMessage(), e);
//        }
//    }
//

    /** Starts the machine instances. */
//    private void doStart(EnvironmentImpl environment,
//                         String workspaceId,
//                         String envName,
//                         Map<String, String> options) throws ServerException {
//
//
//
//        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
//                                        .withWorkspaceId(workspaceId)
//                                        .withStatus(WorkspaceStatus.STARTING)
//                                        .withEventType(EventType.STARTING)
//                                        .withPrevStatus(WorkspaceStatus.STOPPED));
//
//
//        RuntimeInfrastructure infra = infraByRecipe.get(environment.getRecipe().getType());
//
//        infra.start(workspaceId, environment, getEnvironmentLogger(workspaceId), options);
//
//        // agents
//        for(Map.Entry<String, MachineConfigImpl> machineEntry : environment.getMachines().entrySet()) {
//            if(!machineEntry.getValue().getAgents().isEmpty()) {
//                RuntimeMachine machine = runtimes.get(workspaceId).getMachines().get(machineEntry.getKey());
//                // installAgents(machine);
//            }
//        }
//
//        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
//                                        .withWorkspaceId(workspaceId)
//                                        .withStatus(WorkspaceStatus.RUNNING)
//                                        .withEventType(EventType.RUNNING)
//                                        .withPrevStatus(WorkspaceStatus.STARTING));


//        try {
//
//
//            LOG.info("DO START workspace: " + workspaceId + " environment: " + envName);
//
//            List<Runtime> machines = this.networks.start(workspaceId, envName, environment, recover, getEnvironmentLogger(workspaceId));
//
////            List<Instance> machines = envEngine.start(workspaceId,
////                                                      envName,
////                                                      environment,
////                                                      recover,
////                                                      getEnvironmentLogger(workspaceId));
//
//
//            LOG.info("DO LAUNCH AGENT workspace: " + workspaceId + " environment: " + envName);
//
//            launchAgents(environment, machines);
//
//            try (@SuppressWarnings("unused") CloseableLock lock = locks.acquireWriteLock(workspaceId)) {
//                ensurePreDestroyIsNotExecuted();
//                WorkspaceState workspaceState = workspaces.get(workspaceId);
//                workspaceState.status = WorkspaceStatus.RUNNING;
//            }
//
//            eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
//                                            .withWorkspaceId(workspaceId)
//                                            .withStatus(WorkspaceStatus.RUNNING)
//                                            .withEventType(EventType.RUNNING)
//                                            .withPrevStatus(WorkspaceStatus.STARTING));
//        } catch (ApiException | EnvironmentException | RuntimeException e) {
//            try {
//                envEngine.stop(workspaceId);
//            } catch (EnvironmentNotRunningException ignore) {
//            } catch (Exception ex) {
//                LOG.error(ex.getLocalizedMessage(), ex);
//            }
//            String environmentStartError = "Start of environment " + envName +
//                                           " failed. Error: " + e.getLocalizedMessage();
//            try (@SuppressWarnings("unused") CloseableLock lock = locks.acquireWriteLock(workspaceId)) {
//                workspaces.remove(workspaceId);
//            }
//            eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
//                                            .withWorkspaceId(workspaceId)
//                                            .withEventType(EventType.ERROR)
//                                            .withPrevStatus(WorkspaceStatus.STARTING)
//                                            .withError(environmentStartError));
//
//            throw new ServerException(environmentStartError, e);
//        }
//    }

//    private void launchAgents(EnvironmentImpl environment, List<Runtime> machines) throws ServerException {
//        for (Runtime instance : machines) {
//            Map<String, MachineConfigImpl> envMachines = environment.getMachines();
//            if (envMachines != null) {
//                MachineConfig extendedMachine = envMachines.get(instance.getConfig().getName());
//                if (extendedMachine != null) {
//                    List<String> agents = extendedMachine.getAgents();
//                    launchAgents(instance, agents);
//                }
//            }
//        }
//    }
//
//
//    /**
//     * Safely compares current status of given workspace
//     * with {@code from} and if they are equal sets the status to {@code to}.
//     * Returns true if the status of workspace was updated with {@code to} value.
//     */
//    private boolean compareAndSetStatus(String id, WorkspaceStatus from, WorkspaceStatus to) {
//        try (@SuppressWarnings("unused") CloseableLock l = locks.acquireWriteLock(id)) {
//            WorkspaceState state = workspaces.get(id);
//            if (state != null && state.getStatus() == from) {
//                state.status = to;
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /** Creates a snapshot and changes status SNAPSHOTTING -> RUNNING . */
//    private void snapshotAndUpdateStatus(String workspaceId) throws NotFoundException,
//                                                                    ConflictException,
//                                                                    ServerException {
//        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
//                                        .withWorkspaceId(workspaceId)
//                                        .withStatus(WorkspaceStatus.SNAPSHOTTING)
//                                        .withEventType(EventType.SNAPSHOT_CREATING)
//                                        .withPrevStatus(WorkspaceStatus.RUNNING));
//
//        RuntimeImpl runtime = get(workspaceId).getRuntime();
//        List<OldMachineImpl> machines = runtime.getMachines();
//        machines.sort(comparing(m -> !m.getConfig().isDev(), Boolean::compare));
//
//        LOG.info("Creating snapshot of workspace '{}', machines to snapshot: '{}'", workspaceId, machines.size());
//        List<SnapshotImpl> newSnapshots = new ArrayList<>(machines.size());
//        for (OldMachineImpl machine : machines) {
//            try {
//                newSnapshots.add(envEngine.saveSnapshot(workspaceId, machine.getId()));
//            } catch (ServerException | NotFoundException x) {
//                if (machine.getConfig().isDev()) {
//                    compareAndSetStatus(workspaceId, WorkspaceStatus.SNAPSHOTTING, WorkspaceStatus.RUNNING);
//                    eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
//                                                    .withWorkspaceId(workspaceId)
//                                                    .withStatus(WorkspaceStatus.RUNNING)
//                                                    .withEventType(EventType.SNAPSHOT_CREATION_ERROR)
//                                                    .withPrevStatus(WorkspaceStatus.SNAPSHOTTING)
//                                                    .withError(x.getMessage()));
//                    throw x;
//                }
//                LOG.warn(format("Couldn't create snapshot of machine '%s:%s' in workspace '%s'",
//                                machine.getEnvName(),
//                                machine.getConfig().getName(),
//                                workspaceId));
//            }
//        }
//
//        LOG.info("Saving new snapshots metadata, workspace id '{}'", workspaceId);
//        try {
//            List<SnapshotImpl> removed = snapshotDao.replaceSnapshots(workspaceId,
//                                                                      runtime.getActiveEnv(),
//                                                                      newSnapshots);
//            if (!removed.isEmpty()) {
//                LOG.info("Removing old snapshots binaries, workspace id '{}', snapshots to remove '{}'", workspaceId, removed.size());
//                removeBinaries(removed);
//            }
//        } catch (SnapshotException x) {
//            LOG.error(format("Couldn't remove existing snapshots metadata for workspace '%s'", workspaceId), x);
//            LOG.info("Removing newly created snapshots, workspace id '{}', snapshots to remove '{}'", workspaceId, newSnapshots.size());
//            removeBinaries(newSnapshots);
//            compareAndSetStatus(workspaceId, WorkspaceStatus.SNAPSHOTTING, WorkspaceStatus.RUNNING);
//            eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
//                                            .withWorkspaceId(workspaceId)
//                                            .withStatus(WorkspaceStatus.RUNNING)
//                                            .withEventType(EventType.SNAPSHOT_CREATION_ERROR)
//                                            .withPrevStatus(WorkspaceStatus.SNAPSHOTTING)
//                                            .withError(x.getMessage()));
//            throw x;
//        }
//        compareAndSetStatus(workspaceId, WorkspaceStatus.SNAPSHOTTING, WorkspaceStatus.RUNNING);
//        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
//                                        .withStatus(WorkspaceStatus.RUNNING)
//                                        .withWorkspaceId(workspaceId)
//                                        .withEventType(EventType.SNAPSHOT_CREATED)
//                                        .withPrevStatus(WorkspaceStatus.SNAPSHOTTING));
//    }
//


}

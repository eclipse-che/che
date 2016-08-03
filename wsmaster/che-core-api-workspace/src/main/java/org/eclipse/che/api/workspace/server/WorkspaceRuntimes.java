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
import com.google.common.util.concurrent.Striped;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineLogMessage;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.WebsocketMessageConsumer;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLogMessageImpl;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Defines an internal API for managing {@link WorkspaceRuntimeImpl} instances.
 *
 * <p>This component implements {@link WorkspaceStatus} contract.
 *
 * <p>All the operations performed by this component are synchronous.
 *
 * <p>The implementation is thread-safe and guarded by
 * eagerly initialized readwrite locks produced by {@link WorkspaceRuntimes#STRIPED}.
 * The component doesn't expose any api for client-side locking.
 * All the instances produced by this component are copies of the real data.
 *
 * <p>The component doesn't check if the incoming objects are in application-valid state.
 * Which means that it is expected that if {@link #start(WorkspaceImpl, String)} method is called
 * then {@code WorkspaceImpl} argument is a application-valid object which contains
 * all the required data for performing start.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceRuntimes {

    private static final Logger                 LOG     = LoggerFactory.getLogger(WorkspaceRuntimes.class);
    // 16 - experimental value for stripes count, it comes from default hash map size
    private static final Striped<ReadWriteLock> STRIPED = Striped.readWriteLock(16);

    @VisibleForTesting
    final Map<String, RuntimeDescriptor>        descriptors;
    @VisibleForTesting
    final Map<String, Queue<MachineConfigImpl>> startQueues;

    private final MachineManager                      machineManager;
    private final EventService                        eventService;
    private final EventSubscriber<MachineStatusEvent> addMachineEventSubscriber;
    private final EventSubscriber<MachineStatusEvent> removeMachineEventSubscriber;

    private volatile boolean isPreDestroyInvoked;

    @Inject
    public WorkspaceRuntimes(MachineManager machineManager, EventService eventService) {
        this.machineManager = machineManager;
        this.eventService = eventService;
        this.descriptors = new HashMap<>();
        this.startQueues = new HashMap<>();
        this.addMachineEventSubscriber = new AddMachineEventSubscriber();
        this.removeMachineEventSubscriber = new RemoveMachineEventSubscriber();
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
     *         when workspace with given {@code workspaceId} is not running
     */
    public RuntimeDescriptor get(String workspaceId) throws NotFoundException {
        acquireReadLock(workspaceId);
        try {
            final RuntimeDescriptor descriptor = descriptors.get(workspaceId);
            if (descriptor == null) {
                throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
            }
            return new RuntimeDescriptor(descriptor);
        } finally {
            releaseReadLock(workspaceId);
        }
    }

    /**
     * Starts all machines from specified workspace environment,
     * creates workspace runtime instance based on that environment.
     *
     * <p>Dev-machine always starts before the other machines.
     * If dev-machine start failed then method will throw appropriate
     * {@link ServerException}. During the start of the workspace its
     * runtime is visible with {@link WorkspaceStatus#STARTING} status.
     *
     * <p>If {@link #stop} method executed after dev machine is started but
     * another machines haven't been started yet then {@link ConflictException}
     * will be thrown and start process will be interrupted.
     *
     * <p>Note that it doesn't provide any events for
     * machines start, Machine API is responsible for it.
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
     *         when component {@link #isPreDestroyInvoked is stopped} or any
     *         other error occurs during environment start
     * @see MachineManager#createMachineSync(MachineConfig, String, String, org.eclipse.che.api.core.util.LineConsumer)
     * @see WorkspaceStatus#STARTING
     * @see WorkspaceStatus#RUNNING
     */
    public RuntimeDescriptor start(WorkspaceImpl workspace,
                                   String envName,
                                   boolean recover) throws ServerException,
                                                           ConflictException,
                                                           NotFoundException {
        final Optional<EnvironmentImpl> environmentOpt = workspace.getConfig().getEnvironment(envName);
        if (!environmentOpt.isPresent()) {
            throw new IllegalArgumentException(format("Workspace '%s' doesn't contain environment '%s'",
                                                      workspace.getId(),
                                                      envName));
        }

        // Environment copy constructor makes deep copy of objects graph
        // in this way machine configs also copied from incoming values
        // which means that original values won't affect the values in starting queue
        final EnvironmentImpl environmentCopy = new EnvironmentImpl(environmentOpt.get());

        // This check allows to exit with an appropriate exception before blocking on lock.
        // The double check is required as it is still possible to get unlucky timing
        // between locking and starting workspace.
        ensurePreDestroyIsNotExecuted();
        acquireWriteLock(workspace.getId());
        try {
            ensurePreDestroyIsNotExecuted();
            final RuntimeDescriptor existingDescriptor = descriptors.get(workspace.getId());
            if (existingDescriptor != null) {
                throw new ConflictException(format("Could not start workspace '%s' because its status is '%s'",
                                                   workspace.getConfig().getName(),
                                                   existingDescriptor.getRuntimeStatus()));
            }

            // Create a new runtime descriptor and save it with 'STARTING' status
            final RuntimeDescriptor descriptor = new RuntimeDescriptor(new WorkspaceRuntimeImpl(envName));
            descriptor.setRuntimeStatus(WorkspaceStatus.STARTING);
            descriptors.put(workspace.getId(), descriptor);

            // Create a new start queue with a dev machine in the queue head
            final List<MachineConfigImpl> startConfigs = environmentCopy.getMachineConfigs();
            final MachineConfigImpl devCfg = removeFirstMatching(startConfigs, MachineConfig::isDev);
            startConfigs.add(0, devCfg);
            startQueues.put(workspace.getId(), new ArrayDeque<>(startConfigs));
        } finally {
            releaseWriteLock(workspace.getId());
        }

        startQueue(workspace.getId(), environmentCopy.getName(), recover);

        return get(workspace.getId());
    }

    /**
     * This method is similar to the {@link #start(WorkspaceImpl, String, boolean)} method
     * except that it doesn't recover workspace and always starts a new one.
     */
    public RuntimeDescriptor start(WorkspaceImpl workspace, String envName) throws ServerException,
                                                                                   ConflictException,
                                                                                   NotFoundException {
        return start(workspace, envName, false);
    }

    /**
     * Stops running workspace runtime.
     *
     * <p>Stops all running machines one by one,
     * non-dev machines first. During the stop of the workspace
     * its runtime is accessible with {@link WorkspaceStatus#STOPPING stopping} status.
     * Workspace may be stopped only if its status is {@link WorkspaceStatus#RUNNING}.
     *
     * <p>If workspace has runtime with dev-machine running
     * and other machines starting then the runtime can still
     * be stopped which will also interrupt starting process.
     *
     * <p>Note that it doesn't provide any events for machines stop,
     * Machine API is responsible for it.
     *
     * @param workspaceId
     *         identifier of workspace which should be stopped
     * @throws NotFoundException
     *         when workspace with specified identifier is not running
     * @throws ServerException
     *         when any error occurs during workspace stopping
     * @throws ConflictException
     *         when running workspace status is different from {@link WorkspaceStatus#RUNNING}
     * @see MachineManager#destroy(String, boolean)
     * @see WorkspaceStatus#STOPPING
     */
    public void stop(String workspaceId) throws NotFoundException, ServerException, ConflictException {
        // This check allows to exit with an appropriate exception before blocking on lock.
        // The double check is required as it is still possible to get unlucky timing
        // between locking and stopping workspace.
        ensurePreDestroyIsNotExecuted();
        acquireWriteLock(workspaceId);
        final WorkspaceRuntimeImpl runtime;
        try {
            ensurePreDestroyIsNotExecuted();
            final RuntimeDescriptor descriptor = descriptors.get(workspaceId);
            if (descriptor == null) {
                throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
            }
            if (descriptor.getRuntimeStatus() != WorkspaceStatus.RUNNING) {
                throw new ConflictException(
                        format("Couldn't stop '%s' workspace because its status is '%s'. Workspace can be stopped only if it is 'RUNNING'",
                               workspaceId,
                               descriptor.getRuntimeStatus()));
            }

            // According to the WorkspaceStatus specification workspace runtime
            // must visible with STOPPING status until dev-machine is not stopped
            descriptor.setRuntimeStatus(WorkspaceStatus.STOPPING);

            // At this point of time starting queue must be removed
            // to prevent start of another machines which are not started yet.
            // In this case workspace start will be interrupted and
            // interruption will be reported, machine which is currently starting(if such exists)
            // will be destroyed by workspace starting thread.
            startQueues.remove(workspaceId);

            // Create deep  copy of the currently running workspace to prevent
            // out of the lock instance modifications and stale data effects
            runtime = new WorkspaceRuntimeImpl(descriptor.getRuntime());
        } finally {
            releaseWriteLock(workspaceId);
        }
        destroyRuntime(workspaceId, runtime);
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
        acquireReadLock(workspaceId);
        try {
            return descriptors.containsKey(workspaceId);
        } finally {
            releaseReadLock(workspaceId);
        }
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(addMachineEventSubscriber);
        eventService.subscribe(removeMachineEventSubscriber);
    }

    /**
     * Removes all descriptors from the in-memory storage, while
     * {@link MachineManager#cleanup()} is responsible for machines destroying.
     */
    @PreDestroy
    @VisibleForTesting
    void cleanup() {
        isPreDestroyInvoked = true;

        // Unsubscribe from events
        eventService.unsubscribe(addMachineEventSubscriber);
        eventService.unsubscribe(removeMachineEventSubscriber);

        // Acquire all the locks
        for (int i = 0; i < STRIPED.size(); i++) {
            STRIPED.getAt(i).writeLock().lock();
        }

        // clean up
        descriptors.clear();
        startQueues.clear();

        // Release all the locks
        for (int i = 0; i < STRIPED.size(); i++) {
            STRIPED.getAt(i).writeLock().unlock();
        }
    }

    @VisibleForTesting
    void publishEvent(EventType type, String workspaceId, String error) {
        eventService.publish(newDto(WorkspaceStatusEvent.class)
                                     .withEventType(type)
                                     .withWorkspaceId(workspaceId)
                                     .withError(error));
    }

    @VisibleForTesting
    void cleanupStartResources(String workspaceId) {
        acquireWriteLock(workspaceId);
        try {
            descriptors.remove(workspaceId);
            startQueues.remove(workspaceId);
        } finally {
            releaseWriteLock(workspaceId);
        }
    }

    private void removeRuntime(String workspaceId) {
        acquireWriteLock(workspaceId);
        try {
            descriptors.remove(workspaceId);
        } finally {
            releaseWriteLock(workspaceId);
        }
    }

    /**
     * Stops workspace by destroying all its machines and removing it from the in memory storage.
     */
    private void destroyRuntime(String workspaceId,
                                WorkspaceRuntimeImpl workspace) throws NotFoundException, ServerException {
        publishEvent(EventType.STOPPING, workspaceId, null);

        // Preparing the list of machines to be destroyed, dev machine goes last
        final List<MachineImpl> machines = workspace.getMachines();
        final MachineImpl devMachine = removeFirstMatching(machines, m -> m.getConfig().isDev());

        // Synchronously destroying all non-dev machines
        for (MachineImpl machine : machines) {
            try {
                machineManager.destroy(machine.getId(), false);
            } catch (NotFoundException ignore) {
                // This may happen, if machine is stopped by direct call to the Machine API
                // MachineManager cleanups all the machines due to application server shutdown
                // As non-dev machines don't affect runtime status, this exception is ignored
            } catch (RuntimeException | MachineException ex) {
                LOG.error(format("Could not destroy machine '%s' of workspace '%s'",
                                 machine.getId(),
                                 machine.getWorkspaceId()),
                          ex);
            }
        }

        // Synchronously destroying dev-machine
        try {
            machineManager.destroy(devMachine.getId(), false);
            publishEvent(EventType.STOPPED, workspaceId, null);
        } catch (NotFoundException ignore) {
            // This may happen, if machine is stopped by direct call to the Machine API
            // MachineManager cleanups all the machines due to application server shutdown
            // In this case workspace is considered as successfully stopped
            publishEvent(EventType.STOPPED, workspaceId, null);
        } catch (RuntimeException | ServerException ex) {
            publishEvent(EventType.ERROR, workspaceId, ex.getLocalizedMessage());
            throw ex;
        } finally {
            removeRuntime(workspaceId);
        }
    }

    private void startQueue(String workspaceId,
                            String envName,
                            boolean recover) throws ServerException,
                                                    NotFoundException,
                                                    ConflictException {
        publishEvent(EventType.STARTING, workspaceId, null);

        // Starting all the machines one by one by getting configs
        // from the corresponding starting queue.
        // Config will be null only if there are no machines left in the queue
        MachineConfigImpl config = queuePeekOrFail(workspaceId);
        while (config != null) {

            // According to WorkspaceStatus specification the workspace start
            // is failed when dev-machine start is failed, so if any error
            // occurs during machine creation and the machine is dev-machine
            // then start fail is reported and start resources such as queue
            // and descriptor must be cleaned up
            MachineImpl machine = null;
            try {
                machine = startMachine(config, workspaceId, envName, recover);
            } catch (RuntimeException | ServerException | ConflictException | NotFoundException x) {
                if (config.isDev()) {
                    publishEvent(EventType.ERROR, workspaceId, x.getLocalizedMessage());
                    cleanupStartResources(workspaceId);
                    throw x;
                }
                LOG.error(format("Error while creating non-dev machine '%s' in workspace '%s', environment '%s'",
                                 config.getName(),
                                 workspaceId,
                                 envName),
                          x);
            }

            // Machine destroying is an expensive operation which must be
            // performed outside of the lock, this section checks if
            // the workspace wasn't stopped while it is starting and sets
            // polled flag to true if the workspace wasn't stopped plus
            // polls the proceeded machine configuration from the queue
            boolean queuePolled = false;
            acquireWriteLock(workspaceId);
            try {
                ensurePreDestroyIsNotExecuted();
                final Queue<MachineConfigImpl> queue = startQueues.get(workspaceId);
                if (queue != null) {
                    queue.poll();
                    queuePolled = true;
                    if (machine != null) {
                        final RuntimeDescriptor descriptor = descriptors.get(workspaceId);
                        if (config.isDev()) {
                            descriptor.getRuntime().setDevMachine(machine);
                            descriptor.setRuntimeStatus(WorkspaceStatus.RUNNING);
                        }
                        descriptor.getRuntime().getMachines().add(machine);
                    }
                }
            } finally {
                releaseWriteLock(workspaceId);
            }

            // Event publication should be performed outside of the lock
            // as it may take some time to notify subscribers
            if (machine != null && config.isDev()) {
                publishEvent(EventType.RUNNING, workspaceId, null);
            }

            // If machine config is not polled from the queue
            // then workspace was stopped and newly created machine
            // must be destroyed(if such exists)
            if (!queuePolled) {
                if (machine != null) {
                    machineManager.destroy(machine.getId(), false);
                }
                throw new ConflictException(format("Workspace '%s' start interrupted. Workspace stopped before all its machines started",
                                                   workspaceId));
            }

            config = queuePeekOrFail(workspaceId);
        }

        // All the machines tried to start which means that queue
        // should be empty and can be normally removed, but in the case of
        // some unlucky timing, the workspace may be stopped and started again
        // so the queue, which is guarded by the same lock as workspace descriptor
        // may be initialized again with a new batch of machines to start,
        // that's why queue should be removed only if it is not empty.
        // On the other hand queue may not exist because workspace has been stopped
        // just before queue utilization, which considered as a normal behaviour
        acquireWriteLock(workspaceId);
        try {
            final Queue<MachineConfigImpl> queue = startQueues.get(workspaceId);
            if (queue != null && queue.isEmpty()) {
                startQueues.remove(workspaceId);
            }
        } finally {
            releaseWriteLock(workspaceId);
        }
    }

    /**
     * Gets head config from the queue associated with the given {@code workspaceId}.
     *
     * <p>Note that this method won't actually poll the queue.
     *
     * <p>Fails if workspace start was interrupted by stop(queue doesn't exist).
     *
     * @return machine config which is in the queue head, or null
     * if there are no machine configs left
     * @throws ConflictException
     *         when queue doesn't exist which means that {@link #stop(String)} executed
     *         before all the machines started
     * @throws ServerException
     *         only if pre destroy has been invoked before peek config retrieved
     */
    private MachineConfigImpl queuePeekOrFail(String workspaceId) throws ConflictException, ServerException {
        acquireReadLock(workspaceId);
        try {
            ensurePreDestroyIsNotExecuted();
            final Queue<MachineConfigImpl> queue = startQueues.get(workspaceId);
            if (queue == null) {
                throw new ConflictException(
                        format("Workspace '%s' start interrupted. Workspace was stopped before all its machines were started",
                               workspaceId));
            }
            return queue.peek();
        } finally {
            releaseReadLock(workspaceId);
        }
    }

    /**
     * Starts the machine from the configuration, returns null if machine start failed.
     */
    private MachineImpl startMachine(MachineConfigImpl config,
                                     String workspaceId,
                                     String envName,
                                     boolean recover) throws ServerException,
                                                             NotFoundException,
                                                             ConflictException {

        LineConsumer machineLogger = getMachineLogger(workspaceId, config.getName());

        MachineImpl machine;
        try {
            if (recover) {
                machine = machineManager.recoverMachine(config, workspaceId, envName, machineLogger);
            } else {
                machine = machineManager.createMachineSync(config, workspaceId, envName, machineLogger);
            }
        } catch (ConflictException x) {
            // The conflict is because of the already running machine
            // which may be running by several reasons:
            // 1. It has been running before the workspace started
            // 2. It was started immediately after the workspace
            // Consider the next example:
            // If workspace starts machines from configurations [m1, m2, m3]
            // and currently starting machine is 'm2' then it is still possible
            // to use direct Machine API call to start the machine 'm3'
            // which will result as a conflict for the workspace API during 'm3' start.
            // This is not usual/normal behaviour but it should be handled.
            // The handling logic gets the running machine instance by the 'm3' config
            // and considers that the machine is started correctly, if it is impossible
            // to find the corresponding machine then the fail will be reported
            // and workspace runtime state will be changed according to the machine config context.
            final Optional<MachineImpl> machineOpt = machineManager.getMachines()
                                                                   .stream()
                                                                   .filter(m -> m.getWorkspaceId().equals(workspaceId)
                                                                                && m.getEnvName().equals(envName)
                                                                                && m.getConfig().equals(config))
                                                                   .findAny();
            if (machineOpt.isPresent() && machineOpt.get().getStatus() == MachineStatus.RUNNING) {
                machine = machineOpt.get();
            } else {
                throw x;
            }
        } catch (BadRequestException x) {
            // TODO don't throw bad request exception from machine manager
            throw new IllegalArgumentException(x.getLocalizedMessage(), x);
        }
        return machine;
    }

    protected LineConsumer getMachineLogger(String workspaceId, String machineName) throws ServerException {
        WebsocketMessageConsumer<MachineLogMessage> envMessageConsumer =
                new WebsocketMessageConsumer<>(format(ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE, workspaceId));
        return new AbstractLineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
                envMessageConsumer.consume(new MachineLogMessageImpl(machineName, line));
            }
        };
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

        private RuntimeDescriptor(WorkspaceRuntimeImpl runtime) {
            this.runtime = runtime;
        }

        private RuntimeDescriptor(RuntimeDescriptor descriptor) {
            this(new WorkspaceRuntimeImpl(descriptor.runtime));
            this.status = descriptor.status;
        }

        /** Returns the instance of {@code WorkspaceRuntime} described by this descriptor. */
        public WorkspaceRuntimeImpl getRuntime() {
            return runtime;
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
    }

    @VisibleForTesting
    class AddMachineEventSubscriber implements EventSubscriber<MachineStatusEvent> {
        @Override
        public void onEvent(MachineStatusEvent event) {
            if (event.getEventType() == MachineStatusEvent.EventType.RUNNING) {
                try {
                    final MachineImpl machine = machineManager.getMachine(event.getMachineId());
                    if (!addMachine(machine)) {
                        machineManager.destroy(machine.getId(), true);
                    }
                } catch (NotFoundException | MachineException x) {
                    LOG.warn(format("An error occurred during an attempt to add the machine '%s' to the workspace '%s'",
                                    event.getMachineId(),
                                    event.getWorkspaceId()),
                             x);
                }
            }
        }
    }

    /**
     * Tries to add the machine to the WorkspaceRuntime.
     *
     * @return true if machine is added or will be added to the corresponding WorkspaceRuntime,
     * returns false when incoming machine can't be added and should be destroyed.
     */
    @VisibleForTesting
    boolean addMachine(Machine machine) throws NotFoundException, MachineException {
        final String workspaceId = machine.getWorkspaceId();

        acquireWriteLock(workspaceId);
        try {
            final RuntimeDescriptor descriptor = descriptors.get(workspaceId);

            // Ensure that workspace runtime exists for such machine
            // if it is not, then the machine should be immediately destroyed
            if (descriptor == null) {
                LOG.warn("Could not add machine '{}' to the workspace '{}' because workspace is in not running",
                         machine.getId(),
                         workspaceId);
                return false;
            }

            // This may happen when either dev-machine started by WorkspaceRuntimes,
            // or dev-machine started by direct call to MachineManager before WorkspaceRuntimes
            // started it. In this case WorkspaceRuntimes#startMachine will fail & then if such
            // machine exists it will be added to the corresponding WorkspaceRuntime.
            if (machine.getConfig().isDev()) {
                if (descriptor.getRuntimeStatus() == WorkspaceStatus.STARTING) {
                    return true;
                }
                LOG.warn("Could not add another dev-machine '{}' to the workspace '{}'",
                         machine.getId(),
                         workspaceId);
                return false;
            }

            // When workspace is not running then started machine must be immediately destroyed
            // Example: status == STARTING & machine is non-dev
            if (descriptor.getRuntimeStatus() != WorkspaceStatus.RUNNING) {
                LOG.warn("Could not add machine '{}' to the workspace '{}' because workspace status is '{}'",
                         machine.getId(),
                         workspaceId,
                         descriptor.getRuntimeStatus());
                return false;
            }

            // Workspace is RUNNING and there is no start queue
            // which means that machine can be added to the workspace
            if (!startQueues.containsKey(workspaceId)) {
                descriptor.getRuntime().getMachines().add(new MachineImpl(machine));
                return true;
            }

            // These are configs of machines which are not started yet
            final Queue<MachineConfigImpl> machineConfigs = startQueues.get(workspaceId);

            // If there is no config equal to the machine config
            // then the machine can be added to the workspace runtime
            // otherwise it will be added later, after WorkspaceRuntimes starts it
            if (!machineConfigs.stream().anyMatch(m -> m.equals(machine.getConfig()))) {
                descriptor.getRuntime().getMachines().add(new MachineImpl(machine));
            }

            // All the cases are covered, in this case machine will be added
            // directly by WorkspaceRuntimes, after it fails on #startMachine
            return true;
        } finally {
            releaseWriteLock(workspaceId);
        }
    }

    @VisibleForTesting
    class RemoveMachineEventSubscriber implements EventSubscriber<MachineStatusEvent> {

        @Override
        public void onEvent(MachineStatusEvent event) {
            // This event subscriber doesn't handle dev-machine destroyed events
            // as in that case workspace should be stopped, and stop should be asynchronous
            // but WorkspaceRuntimes provides only synchronous operations.
            if (event.getEventType() == MachineStatusEvent.EventType.DESTROYED && !event.isDev()) {
                removeMachine(event.getMachineId(),
                              event.getMachineName(),
                              event.getWorkspaceId());
            }
        }
    }

    /** Removes machine from the workspace runtime. */
    @VisibleForTesting
    void removeMachine(String machineId, String machineName, String workspaceId) {
        acquireWriteLock(workspaceId);
        try {
            final RuntimeDescriptor descriptor = descriptors.get(workspaceId);

            // Machine can be removed only from existing runtime with 'RUNNING' status
            if (descriptor == null || descriptor.getRuntimeStatus() != WorkspaceStatus.RUNNING) {
                return;
            }

            // Try to remove non-dev machine from the runtime machines list
            // It is unusual but still possible to get the state when such machine
            // doesn't exist, in this case an appropriate warning will be logged
            if (!descriptor.getRuntime()
                           .getMachines()
                           .removeIf(m -> m.getConfig().getName().equals(machineName))) {
                LOG.warn("An attempt to remove the machine '{}' from the workspace runtime '{}' failed. " +
                         "Workspace doesn't contain machine with name '{}'",
                         machineId,
                         workspaceId,
                         machineName);
            }
        } finally {
            releaseWriteLock(workspaceId);
        }
    }

    private static <T> T removeFirstMatching(List<? extends T> elements, Predicate<T> predicate) {
        T element = null;
        for (final Iterator<? extends T> it = elements.iterator(); it.hasNext() && element == null; ) {
            final T next = it.next();
            if (predicate.test(next)) {
                element = next;
                it.remove();
            }
        }
        return element;
    }

    private void ensurePreDestroyIsNotExecuted() throws ServerException {
        if (isPreDestroyInvoked) {
            throw new ServerException("Could not perform operation because application server is stopping");
        }
    }

    /** Short alias for acquiring read lock for the given workspace. */
    private static void acquireReadLock(String workspaceId) {
        STRIPED.get(workspaceId).readLock().lock();
    }

    /** Short alias for releasing read lock for the given workspace. */
    private static void releaseReadLock(String workspaceId) {
        STRIPED.get(workspaceId).readLock().unlock();
    }

    /** Short alias for acquiring write lock for the given workspace. */
    private static void acquireWriteLock(String workspaceId) {
        STRIPED.get(workspaceId).writeLock().lock();
    }

    /** Short alias for releasing write lock for the given workspace. */
    private static void releaseWriteLock(String workspaceId) {
        STRIPED.get(workspaceId).writeLock().unlock();
    }
}

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
import com.google.common.base.Predicate;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Defines an internal API for managing {@link WorkspaceRuntimeImpl} instances.
 *
 * <p>This component implements {@link WorkspaceStatus} spec.
 *
 * <p>All the operations performed by this component are synchronous.
 *
 * <p>The implementation is thread-safe and guarded by {@link ReentrantReadWriteLock rwLock}.
 *
 * <p>The implementation doesn't validate parameters.
 * Parameters should be validated by caller of methods of this class.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 * @author Mykola Morhun
 */
@Singleton
public class WorkspaceRuntimes implements EventSubscriber<MachineStatusEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRuntimes.class);

    private final ReadWriteLock                         rwLock;
    private final Map<String, RuntimeDescriptor>        descriptors;
    private final Map<String, Queue<MachineConfigImpl>> startQueues;
    private final MachineManager                        machineManager;
    private final EventService                          eventService;

    private volatile boolean isPreDestroyInvoked;

    @Inject
    public WorkspaceRuntimes(MachineManager machineManager, EventService eventService) {
        this.machineManager = machineManager;
        this.eventService = eventService;
        this.descriptors = new HashMap<>();
        this.startQueues = new HashMap<>();
        this.rwLock = new ReentrantReadWriteLock();
    }

    @PostConstruct
    private void subscribeOnMachineEvents() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribeOnMachineEvents() {
        eventService.unsubscribe(this);
    }

    /**
     * Returns the runtime descriptor describing currently starting/running/stopping
     * workspace runtime.
     *
     * <p>Note that the {@link RuntimeDescriptor#getRuntime()} method
     * returns {@link Optional} which describes just a snapshot copy of
     * a real {@code WorkspaceRuntime} object, which means that any
     * runtime copy modifications won't affect the real object and also
     * it means that copy won't be affected with modifications applied
     * to the real runtime workspace object state.
     *
     * @param workspaceId
     *         the id of the workspace to get its runtime
     * @return descriptor which describes current state of the workspace runtime
     * @throws NotFoundException
     *         when workspace with given {@code workspaceId} doesn't have runtime
     */
    public RuntimeDescriptor get(String workspaceId) throws NotFoundException {
        rwLock.readLock().lock();
        try {
            final RuntimeDescriptor descriptor = descriptors.get(workspaceId);
            if (descriptor == null) {
                throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
            }
            return new RuntimeDescriptor(descriptor);
        } finally {
            rwLock.readLock().unlock();
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
     * @see MachineManager#createMachineSync(MachineConfig, String, String)
     * @see WorkspaceStatus#STARTING
     * @see WorkspaceStatus#RUNNING
     */
    public RuntimeDescriptor start(WorkspaceImpl workspace, String envName, boolean recover) throws ServerException,
                                                                                                    ConflictException,
                                                                                                    NotFoundException {
        final EnvironmentImpl activeEnv = new EnvironmentImpl(workspace.getConfig().getEnvironment(envName).get());
        ensurePreDestroyIsNotExecuted();
        rwLock.writeLock().lock();
        try {
            ensurePreDestroyIsNotExecuted();
            final RuntimeDescriptor descriptor = descriptors.get(workspace.getId());
            if (descriptor != null) {
                throw new ConflictException(format("Could not start workspace '%s' because its status is '%s'",
                                                   workspace.getConfig().getName(),
                                                   descriptor.getRuntimeStatus()));
            }
            descriptors.put(workspace.getId(), new RuntimeDescriptor(new WorkspaceRuntimeImpl(envName)));
            // Dev machine goes first in the start queue
            final List<MachineConfigImpl> machineConfigs = activeEnv.getMachineConfigs();
            final MachineConfigImpl devCfg = rmFirst(machineConfigs, MachineConfig::isDev);
            machineConfigs.add(0, devCfg);
            startQueues.put(workspace.getId(), new ArrayDeque<>(machineConfigs));
        } finally {
            rwLock.writeLock().unlock();
        }
        startQueue(workspace.getId(), activeEnv.getName(), recover);
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
        ensurePreDestroyIsNotExecuted();
        rwLock.writeLock().lock();
        final WorkspaceRuntimeImpl runtime;
        try {
            ensurePreDestroyIsNotExecuted();
            final RuntimeDescriptor descriptor = descriptors.get(workspaceId);
            if (descriptor == null) {
                throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
            }
            if (descriptor.getRuntimeStatus() != WorkspaceStatus.RUNNING) {
                throw new ConflictException(format("Couldn't stop '%s' workspace because its status is '%s'",
                                                   workspaceId,
                                                   descriptor.getRuntimeStatus()));
            }
            descriptor.setStopping();
            // remove the workspace from the queue to prevent start
            // of another not started machines(if such exist)
            startQueues.remove(workspaceId);
            runtime = descriptor.getRuntime();
        } finally {
            rwLock.writeLock().unlock();
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
        rwLock.readLock().lock();
        try {
            return descriptors.containsKey(workspaceId);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void onEvent(MachineStatusEvent event) {
        if (hasRuntime(event.getWorkspaceId())) {
            switch (event.getEventType()) {
                case RUNNING:
                    if (startQueues.get(event.getWorkspaceId()) == null) {
                        try {
                            addMachine(event.getMachineId());
                        } catch (ServerException | ConflictException | NotFoundException exception) {
                            try {
                                machineManager.destroy(event.getMachineId(), true);
                            } catch (NotFoundException | MachineException e) {
                                LOG.error(exception.getLocalizedMessage(), exception);
                            }
                        }
                    } else {
                        LOG.warn("Cannot add machine into starting workspace");
                        try {
                            removeMachine(event.getMachineId());
                        } catch (NotFoundException | MachineException exception) {
                            LOG.error(exception.getLocalizedMessage(), exception);
                        }
                    }
                    break;
                case DESTROYING:
                    try {
                        removeMachine(event.getMachineId());
                    } catch (NotFoundException | MachineException exception) {
                        LOG.error(exception.getLocalizedMessage(), exception);
                    }
                    break;
                case ERROR:
                    try {
                        machineManager.destroy(event.getMachineId(), true);
                    } catch (NotFoundException ignore) {
                    } catch (MachineException exception) {
                        LOG.error(exception.getLocalizedMessage(), exception);
                    }
                    break;
            }
        }
    }

    /**
     * Removes all descriptors from the in-memory storage, while
     * {@link MachineManager#cleanup()} is responsible for machines destroying.
     */
    @PreDestroy
    @VisibleForTesting
    void cleanup() {
        isPreDestroyInvoked = true;
        rwLock.writeLock().lock();
        try {
            descriptors.clear();
            startQueues.clear();
        } finally {
            rwLock.writeLock().unlock();
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
        rwLock.writeLock().lock();
        try {
            descriptors.remove(workspaceId);
            startQueues.remove(workspaceId);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @VisibleForTesting
    void removeRuntime(String wsId) {
        rwLock.writeLock().lock();
        try {
            descriptors.remove(wsId);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Adds machine into running workspace.
     * This method do not touch workspace configuration.
     * Just adds machine to workspace runtime and destroy it on workspace stop.
     * Does nothing if add already existing machine.
     *
     * @param machineId
     *         id of machine to add to specified runtime
     * @throws NotFoundException
     *         when workspace with specified id not running or not exists or
     *         when machine with specified id doesn't exist
     * @throws ServerException
     *         when application server is stopping
     * @throws ConflictException
     *         when workspace is not running
     */
    void addMachine(String machineId) throws NotFoundException, ServerException, ConflictException {
        ensurePreDestroyIsNotExecuted();

        MachineImpl machine = machineManager.getMachine(machineId);
        String workspaceId = machine.getWorkspaceId();

        rwLock.writeLock().lock();
        try {
            ensurePreDestroyIsNotExecuted();
            final RuntimeDescriptor descriptor = descriptors.get(workspaceId);
            if (descriptor == null) {
                throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
            }
            if (descriptor.getRuntimeStatus() != WorkspaceStatus.RUNNING) {
                throw new ConflictException("Cannot add machine " + machine.getId() + " to not running workspace.");
            }

            List<MachineImpl> machines = descriptor.getRuntime().getMachines();
            if (!machines.stream().anyMatch(m -> machine.getId().equals(m.getId()))) {
                machines.add(machine);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Removes machine from running workspace.
     * This method do not touch workspace configuration and is opposite to {@link #addMachine(String)}
     *
     * @param machineId
     *         id of machine to remove from specified runtime
     * @throws NotFoundException
     *         when workspace with specified id not running or not exists
     * @throws MachineException
     */
    void removeMachine(String machineId) throws NotFoundException, MachineException {
        MachineImpl machine = machineManager.getMachine(machineId);
        String workspaceId = machine.getWorkspaceId();

        rwLock.writeLock().lock();
        try {
            final RuntimeDescriptor descriptor = descriptors.get(workspaceId);
            if (descriptor == null) {
                throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
            }

            rmFirst(descriptor.getRuntime().getMachines(), m -> machine.getId().equals(m.getId()));
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Stops workspace by destroying all its machines and removing it from in memory storage.
     */
    private void destroyRuntime(String wsId, WorkspaceRuntimeImpl workspace) throws NotFoundException, ServerException {
        publishEvent(EventType.STOPPING, wsId, null);
        final List<MachineImpl> machines = new ArrayList<>(workspace.getMachines());
        final MachineImpl devMachine = rmFirst(machines, m -> m.getConfig().isDev());
        // destroying all non-dev machines
        for (MachineImpl machine : machines) {
            try {
                machineManager.destroy(machine.getId(), false);
            } catch (NotFoundException ignore) {
                // it is ok, machine has been already destroyed
            } catch (RuntimeException | MachineException ex) {
                LOG.error(format("Could not destroy machine '%s' of workspace '%s'",
                                 machine.getId(),
                                 machine.getWorkspaceId()),
                          ex);
            }
        }
        // destroying dev-machine
        try {
            machineManager.destroy(devMachine.getId(), false);
            publishEvent(EventType.STOPPED, wsId, null);
        } catch (NotFoundException ignore) {
            // it is ok, machine has been already destroyed
        } catch (RuntimeException | ServerException ex) {
            publishEvent(EventType.ERROR, wsId, ex.getLocalizedMessage());
            throw ex;
        } finally {
            removeRuntime(wsId);
        }
    }

    private void startQueue(String wsId, String envName, boolean recover) throws ServerException,
                                                                                 NotFoundException,
                                                                                 ConflictException {
        publishEvent(EventType.STARTING, wsId, null);
        MachineConfigImpl config = getPeekConfig(wsId);
        while (config != null) {
            startMachine(config, wsId, envName, recover);
            config = getPeekConfig(wsId);
        }

        // Clean up the start queue when all the machines successfully started
        rwLock.writeLock().lock();
        try {
            final Queue<MachineConfigImpl> queue = startQueues.get(wsId);
            if (queue != null && queue.isEmpty()) {
                startQueues.remove(wsId);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private MachineConfigImpl getPeekConfig(String wsId) throws ConflictException, ServerException {
        // Trying to get machine to start. If queue doesn't exist then workspace
        // start was interrupted either by the stop method, or by the cleanup
        rwLock.readLock().lock();
        try {
            ensurePreDestroyIsNotExecuted();
            final Queue<MachineConfigImpl> queue = startQueues.get(wsId);
            if (queue == null) {
                throw new ConflictException(format("Workspace '%s' start interrupted. " +
                                                   "Workspace was stopped before all its machines were started",
                                                   wsId));
            }
            return queue.peek();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void startMachine(MachineConfigImpl config,
                              String wsId,
                              String envName,
                              boolean recover) throws ServerException,
                                                      NotFoundException,
                                                      ConflictException {
        // Trying to start machine from the given configuration
        MachineImpl machine = null;
        try {
            machine = createMachine(config, wsId, envName, recover);
        } catch (RuntimeException | MachineException | NotFoundException | SnapshotException | ConflictException ex) {
            if (config.isDev()) {
                publishEvent(EventType.ERROR, wsId, ex.getLocalizedMessage());
                cleanupStartResources(wsId);
                throw ex;
            }
            LOG.error(format("Error while creating non-dev machine '%s' in workspace '%s', environment '%s'",
                             config.getName(),
                             wsId,
                             envName),
                      ex);
        }

        // Machine destroying is an expensive operation which must be
        // performed outside of the lock, this section checks if
        // the workspace wasn't stopped while it is starting and sets
        // polled flag to true if the workspace wasn't stopped plus
        // polls the proceeded machine configuration from the queue
        boolean queuePolled = false;
        rwLock.readLock().lock();
        try {
            ensurePreDestroyIsNotExecuted();
            final Queue<MachineConfigImpl> queue = startQueues.get(wsId);
            if (queue != null) {
                queue.poll();
                queuePolled = true;
                if (machine != null) {
                    final WorkspaceRuntimeImpl runtime = descriptors.get(wsId).getRuntime();
                    if (config.isDev()) {
                        runtime.setDevMachine(machine);
                        publishEvent(EventType.RUNNING, wsId, null);
                    }
                    runtime.getMachines().add(machine);
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }

        // If machine config is not polled from the queue
        // then stop method was executed and the machine which
        // has been just created must be destroyed
        if (!queuePolled) {
            if (machine != null) {
                machineManager.destroy(machine.getId(), false);
            }
            throw new ConflictException(format("Workspace '%s' start interrupted. " +
                                               "Workspace was stopped before all its machines were started",
                                               wsId));
        }
    }

    private <T> T rmFirst(List<? extends T> elements, Predicate<T> predicate) {
        T element = null;
        for (final Iterator<? extends T> it = elements.iterator(); it.hasNext() && element == null; ) {
            final T next = it.next();
            if (predicate.apply(next)) {
                element = next;
                it.remove();
            }
        }
        return element;
    }

    /**
     * Creates or recovers machine based on machine config.
     */
    private MachineImpl createMachine(MachineConfig machine,
                                      String workspaceId,
                                      String envName,
                                      boolean recover) throws MachineException,
                                                              SnapshotException,
                                                              NotFoundException,
                                                              ConflictException {
        try {
            if (recover) {
                return machineManager.recoverMachine(machine, workspaceId, envName);
            } else {
                return machineManager.createMachineSync(machine, workspaceId, envName);
            }
        } catch (BadRequestException brEx) {
            // TODO fix this in machineManager
            throw new IllegalArgumentException(brEx.getLocalizedMessage(), brEx);
        }
    }

    private void ensurePreDestroyIsNotExecuted() throws ServerException {
        if (isPreDestroyInvoked) {
            throw new ServerException("Could not perform operation because application server is stopping");
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
        private boolean              isStopping;

        private RuntimeDescriptor(WorkspaceRuntimeImpl runtime) {
            this.runtime = runtime;
        }

        private RuntimeDescriptor(RuntimeDescriptor descriptor) {
            this(new WorkspaceRuntimeImpl(descriptor.runtime));
            this.isStopping = descriptor.isStopping;
        }

        /**
         * Returns an {@link Optional} describing a started {@link WorkspaceRuntime},
         * if the runtime is in starting state then an empty {@code Optional} will be returned.
         */
        public WorkspaceRuntimeImpl getRuntime() {
            return runtime;
        }

        /**
         * Returns the status of the started workspace runtime.
         * The relation between {@link #getRuntime()} and this method
         * is pretty clear, whether workspace is in starting state, then
         * {@code getRuntime()} will return an empty optional, otherwise
         * the optional describing a running or stopping workspace runtime.
         */
        public WorkspaceStatus getRuntimeStatus() {
            if (isStopping) {
                return WorkspaceStatus.STOPPING;
            }
            if (runtime.getDevMachine() == null) {
                return WorkspaceStatus.STARTING;
            }
            return WorkspaceStatus.RUNNING;
        }

        private void setStopping() {
            isStopping = true;
        }
    }
}

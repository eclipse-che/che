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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.RuntimeWorkspace;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeWorkspaceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;

/**
 * Defines {@link RuntimeWorkspace} internal API.
 *
 * <p>Registry implements {@link WorkspaceStatus} contract for runtime workspaces,
 * the only implementation remark here is that in the case of {@link WorkspaceStatus#STOPPED}
 * status registry removes workspace from in-memory storage instead of updating its status to STOPPED.
 *
 * <p>All methods which return instance of {@link RuntimeWorkspace} return the
 * copy of real object, which means that instance modification won't affect real object.
 * This is done for keeping data in actual state and for avoiding race-conditions.
 * All operations performed by registry are synchronous.
 *
 * <p>The implementation is thread-safe.
 * Workspaces are stored in memory - in 2 Maps. First for <i>identifier -> workspace</i> mapping,
 * second for <i>owner -> list of workspaces</i> mapping(which speeds up fetching workspaces by owner).
 * Maps are guarded by {@link ReentrantReadWriteLock}.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class RuntimeWorkspaceRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeWorkspaceRegistry.class);

    private final Map<String, RuntimeWorkspaceImpl>          idToWorkspaces;
    private final ListMultimap<String, RuntimeWorkspaceImpl> ownerToWorkspaces;
    private final ReadWriteLock                              lock;
    private final MachineManager                             machineManager;

    private volatile boolean isStopped;

    @Inject
    public RuntimeWorkspaceRegistry(MachineManager machineManager) {
        this.machineManager = machineManager;
        this.idToWorkspaces = new HashMap<>();
        this.ownerToWorkspaces = ArrayListMultimap.create();
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Starts all machines from specified workspace environment, creates runtime workspace for it.
     *
     * <p>Dev-machine always starts before the other machines. If dev-machine start failed
     * method throws appropriate {@link ServerException} and removes runtime workspace instance from the registry.
     * During the start the runtime workspace is visible with {@link WorkspaceStatus#STARTING} status.
     *
     * <p>Note that it doesn't provide any events for machines start, Machine API is responsible for it.
     *
     * @param usersWorkspace
     *         workspace which should be started
     * @param envName
     *         name of environment
     * @param recover
     *         if true - registry tries to recover workspace from snapshot, otherwise starts workspace from recipes
     * @return runtime view of {@code usersWorkspace} with status {@link WorkspaceStatus#RUNNING}
     * @throws ConflictException
     *         when workspace is already running or any other conflict error occurs during environment start
     * @throws BadRequestException
     *         when active environment is in inconsistent state or {@code envName/usersWorkspace} is null
     * @throws NotFoundException
     *         whe any not found exception occurs during environment start
     * @throws ServerException
     *         when registry {@link #isStopped is stopped} other error occurs during environment start
     * @see MachineManager#createMachineSync(MachineConfig, String, String)
     * @see WorkspaceStatus#STARTING
     */
    public RuntimeWorkspaceImpl start(UsersWorkspace usersWorkspace, String envName, boolean recover) throws ConflictException,
                                                                                                             ServerException,
                                                                                                             BadRequestException,
                                                                                                             NotFoundException {
        checkRegistryIsNotStopped();
        checkWorkspaceIsValidForStart(usersWorkspace, envName);
        // Prepare runtime workspace for start
        final RuntimeWorkspaceImpl newRuntime = RuntimeWorkspaceImpl.builder()
                                                                    .fromWorkspace(usersWorkspace)
                                                                    .setActiveEnv(envName)
                                                                    .setStatus(STARTING)
                                                                    .build();
        // Save workspace with 'STARTING' status
        lock.writeLock().lock();
        try {
            checkRegistryIsNotStopped();
            final RuntimeWorkspace running = idToWorkspaces.get(newRuntime.getId());
            if (running != null) {
                throw new ConflictException(format("Could not start workspace '%s' because its status is '%s'",
                                                   running.getConfig().getName(),
                                                   running.getStatus()));
            }
            idToWorkspaces.put(newRuntime.getId(), newRuntime);
            ownerToWorkspaces.get(newRuntime.getOwner()).add(newRuntime);
        } finally {
            lock.writeLock().unlock();
        }
        startEnvironment(newRuntime.getActiveEnvironment(), newRuntime.getId(), recover);
        return get(newRuntime.getId());
    }

    /**
     * Starts all machines from specified workspace environment, creates runtime workspace for it.
     *
     * <p>Dev-machine always starts before the other machines. If dev-machine start failed
     * method throws appropriate {@link ServerException} and removes runtime workspace instance from the registry.
     * During the start the runtime workspace is visible with {@link WorkspaceStatus#STARTING} status.
     *
     * <p>Note that it doesn't provide any events for machines start, Machine API is responsible for it.
     *
     * @param usersWorkspace
     *         workspace which should be started
     * @param envName
     *         name of environment
     * @return runtime view of {@code usersWorkspace} with status {@link WorkspaceStatus#RUNNING}
     * @throws ConflictException
     *         when workspace is already running or any other conflict error occurs during environment start
     * @throws BadRequestException
     *         when active environment is in inconsistent state or {@code envName} is null
     * @throws NotFoundException
     *         whe any not found exception occurs during environment start
     * @throws ServerException
     *         when registry {@link #isStopped is stopped} other error occurs during environment start
     * @see MachineManager#createMachineSync(MachineConfig, String, String)
     * @see WorkspaceStatus#STARTING
     */
    public RuntimeWorkspaceImpl start(UsersWorkspace usersWorkspace, String envName) throws ConflictException,
                                                                                            ServerException,
                                                                                            BadRequestException,
                                                                                            NotFoundException {
        return start(usersWorkspace, envName, false);
    }

    /**
     * Stops running workspace.
     *
     * <p>Stops all {@link RuntimeWorkspace#getMachines() running machines} one by one,
     * non-dev machines first. During the workspace stopping the workspace
     * will still be accessible with {@link WorkspaceStatus#STOPPING stopping} status.
     * Workspace may be stopped only if its status is {@link WorkspaceStatus#RUNNING}.
     *
     * <p>Note that it doesn't provide any events for machines stop, Machine API is responsible for it.
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
        checkRegistryIsNotStopped();
        lock.writeLock().lock();
        final RuntimeWorkspaceImpl workspace;
        try {
            checkRegistryIsNotStopped();
            workspace = idToWorkspaces.get(workspaceId);
            if (workspace == null) {
                throw new NotFoundException("Workspace with id " + workspaceId + " is not running.");
            }
            if (workspace.getStatus() != RUNNING) {
                throw new ConflictException(format("Couldn't stop '%s' workspace because its status is '%s'",
                                                   workspace.getConfig().getName(),
                                                   workspace.getStatus()));
            }
            workspace.setStatus(STOPPING);
        } finally {
            lock.writeLock().unlock();
        }
        stopMachines(workspace);
    }

    /**
     * Returns true if workspace was started and its status is {@link WorkspaceStatus#RUNNING running},
     * {@link WorkspaceStatus#STARTING starting} or {@link WorkspaceStatus#STOPPING stopping} - otherwise returns false.
     *
     * <p> Using of this method is equivalent to {@link #get(String)} + {@code try catch}, see example:
     * <pre>
     *
     *     if (!registry.hasRuntime("workspace123")) {
     *         doStuff("workspace123");
     *     }
     *
     *     //vs
     *
     *     try {
     *         registry.get("workspace123");
     *     } catch (NotFoundException ex) {
     *         doStuff("workspace123");
     *     }
     *
     * </pre>
     *
     * @param workspaceId
     *         workspace identifier to perform check
     * @return true if workspace is running, otherwise false
     */
    public boolean hasRuntime(String workspaceId) {
        lock.readLock().lock();
        try {
            return idToWorkspaces.containsKey(workspaceId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns runtime view of {@link UsersWorkspace} if exists, throws {@link NotFoundException} otherwise.
     *
     * <p>Note that returned {@link RuntimeWorkspaceImpl instance} is a copy of real runtime workspace object
     * and its modification will not affect the real object.
     *
     * @param workspaceId
     *         workspace identifier to get runtime workspace
     * @return runtime view of {@link UsersWorkspace}
     * @throws NotFoundException
     *         when workspace with specified {@code workspaceId} was not found
     */
    public RuntimeWorkspaceImpl get(String workspaceId) throws NotFoundException {
        lock.readLock().lock();
        final RuntimeWorkspaceImpl runtimeWorkspace;
        try {
            runtimeWorkspace = idToWorkspaces.get(workspaceId);
            if (runtimeWorkspace == null) {
                throw new NotFoundException("Workspace with id " + workspaceId + " is not running.");
            }
        } finally {
            lock.readLock().unlock();
        }
        return new RuntimeWorkspaceImpl(runtimeWorkspace);
    }

    /**
     * Gets runtime workspaces owned by certain user.
     *
     * <p>Note that returned {@link RuntimeWorkspaceImpl instances} are copies of real runtime workspace objects
     * and it modification will not affect the real objects.
     *
     * @param ownerId
     *         owner identifier
     * @return list of workspace owned by {@code ownerId} or empty list when user doesn't have any workspaces running
     */
    public List<RuntimeWorkspaceImpl> getByOwner(String ownerId) {
        lock.readLock().lock();
        try {
            return ownerToWorkspaces.get(ownerId)
                                    .stream()
                                    .map(RuntimeWorkspaceImpl::new)
                                    .collect(toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Starts all environment machines, starting from dev machine.
     */
    private void startEnvironment(Environment environment, String workspaceId, boolean recover) throws BadRequestException,
                                                                                                       ServerException,
                                                                                                       NotFoundException,
                                                                                                       ConflictException {
        final List<? extends MachineConfig> machineConfigs = new ArrayList<>(environment.getMachineConfigs());
        final MachineConfig devCfg = findDev(machineConfigs);

        // Starting workspace dev machine
        final MachineImpl devMachine;
        try {
            devMachine = createMachine(devCfg, workspaceId, environment.getName(), recover);
        } catch (MachineException | BadRequestException | NotFoundException | SnapshotException | ConflictException ex) {
            doRemoveWorkspace(workspaceId);
            throw ex;
        }

        // Try to add dev-machine to the list of runtime workspace machines.
        // If runtime workspace doesn't exist it means only that
        // 'stopRegistry' was performed and workspace was removed by 'stopRegistry',
        // in that case dev-machine must not be destroyed(MachineManager is responsible for it)
        // and another machines must not be started.
        lock.writeLock().lock();
        try {
            if (!addRunningMachine(devMachine)) {
                // Dev machine was started but workspace doesn't exist
                // it means that registry was stopped, dev-machine must not be
                // destroyed in this place as MachineManager#cleanup() does it
                throw new ServerException("Workspace '" + workspaceId + "' had been stopped before its dev-machine was started");
            }
            idToWorkspaces.get(workspaceId).setStatus(RUNNING);
        } finally {
            lock.writeLock().unlock();
        }

        // Try to start all the other machines different from the dev one.
        machineConfigs.remove(devCfg);
        for (MachineConfig nonDevCfg : machineConfigs) {
            try {
                final MachineImpl nonDevMachine = createMachine(nonDevCfg, workspaceId, environment.getName(), recover);
                if (!addRunningMachine(nonDevMachine)) {
                    // Non dev machine was started but workspace doesn't exist
                    // it means that either registry was stopped or runtime workspace
                    // was stopped by client. In the case when it was stopped by
                    // client we should destroy newly started non-dev machine
                    // as it wasn't destroyed by 'stop' method. When 'stopRegistry' was performed we
                    // must not destroy machine as MachineManager is responsible for it.
                    if (!isStopped) {
                        machineManager.destroy(nonDevMachine.getId(), false);
                    }
                    throw new ServerException("Workspace '" + workspaceId + "' had been stopped before all its machines were started");
                }
            } catch (MachineException | BadRequestException | NotFoundException | SnapshotException | ConflictException ex) {
                LOG.error(format("Error while creating machine '%s' in workspace '%s', environment '%s'",
                                 nonDevCfg.getName(),
                                 workspaceId,
                                 environment.getName()),
                          ex);
            }
        }
    }

    /**
     * Checks if workspace is valid for start.
     */
    private void checkWorkspaceIsValidForStart(UsersWorkspace workspace, String envName) throws BadRequestException {
        if (workspace == null) {
            throw new BadRequestException("Required non-null workspace");
        }
        if (envName == null) {
            throw new BadRequestException(format("Couldn't start workspace '%s', environment name is null",
                                                 workspace.getConfig().getName()));
        }
        final Optional<? extends Environment> environmentOptional = workspace.getConfig()
                                                                             .getEnvironments()
                                                                             .stream()
                                                                             .filter(env -> env.getName().equals(envName))
                                                                             .findFirst();
        if (!environmentOptional.isPresent()) {
            throw new BadRequestException(format("Couldn't start workspace '%s', workspace doesn't have environment '%s'",
                                                 workspace.getConfig().getName(),
                                                 envName));
        }
        Environment environment = environmentOptional.get();
        if (environment.getRecipe() != null && !"docker".equals(environment.getRecipe().getType())) {
            throw new BadRequestException(format("Couldn't start workspace '%s' from environment '%s', " +
                                                 "environment recipe has unsupported type '%s'",
                                                 workspace.getConfig().getName(),
                                                 envName,
                                                 environment.getRecipe().getType()));
        }
        if (findDev(environment.getMachineConfigs()) == null) {
            throw new BadRequestException(format("Couldn't start workspace '%s' from environment '%s', " +
                                                 "environment doesn't contain dev-machine",
                                                 workspace.getConfig().getName(),
                                                 envName));
        }
    }

    /**
     * Adds given machine to the running workspace, if the workspace exists.
     * Sets up this machine as dev-machine if it is dev.
     *
     * @return true if machine was added to the workspace(workspace exists) and false otherwise
     */
    @VisibleForTesting
    boolean addRunningMachine(MachineImpl machine) throws ServerException {
        lock.writeLock().lock();
        try {
            final RuntimeWorkspaceImpl workspace = idToWorkspaces.get(machine.getWorkspaceId());
            if (workspace != null) {
                if (machine.getConfig().isDev()) {
                    workspace.setDevMachine(machine);
                }
                workspace.getMachines().add(machine);
            }
            return workspace != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void doRemoveWorkspace(String workspaceId) {
        lock.writeLock().lock();
        try {
            final RuntimeWorkspaceImpl workspace = idToWorkspaces.get(workspaceId);
            if (workspace != null) {
                ownerToWorkspaces.get(workspace.getOwner()).removeIf(ws -> ws.getId().equals(workspaceId));
            }
            idToWorkspaces.remove(workspaceId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Creates or recovers machine based on machine config.
     */
    private MachineImpl createMachine(MachineConfig machine,
                                      String workspaceId,
                                      String envName,
                                      boolean recover) throws MachineException,
                                                              BadRequestException,
                                                              SnapshotException,
                                                              NotFoundException,
                                                              ConflictException {
        if (recover) {
            return machineManager.recoverMachine(machine, workspaceId, envName);
        } else {
            return machineManager.createMachineSync(machine, workspaceId, envName);
        }
    }

    /**
     * Checks that registry is not stopped(stopWorkspaces was performed) if it is - throws {@link ServerException}.
     */
    private void checkRegistryIsNotStopped() throws ServerException {
        if (isStopped) {
            throw new ServerException("Could not perform operation while registry is stopping workspaces");
        }
    }

    private <T extends MachineConfig> T findDev(List<T> machines) {
        for (T machine : machines) {
            if (machine.isDev()) {
                return machine;
            }
        }
        return null;
    }

    /**
     * Stops workspace destroying all its machines and removing it from in memory storage.
     */
    private void stopMachines(RuntimeWorkspaceImpl workspace) throws NotFoundException, ServerException {
        final List<MachineImpl> machines = new ArrayList<>(workspace.getMachines());
        // destroying all non-dev machines
        for (MachineImpl machine : machines) {
            if (machine.getConfig().isDev()) {
                continue;
            }
            try {
                machineManager.destroy(machine.getId(), false);
            } catch (NotFoundException | MachineException ex) {
                LOG.error(format("Could not destroy machine '%s' of workspace '%s'",
                                 machine.getId(),
                                 machine.getWorkspaceId()),
                          ex);
            }
        }
        // destroying dev-machine
        try {
            machineManager.destroy(workspace.getDevMachine().getId(), false);
        } finally {
            doRemoveWorkspace(workspace.getId());
        }
    }

    /**
     * Removes all workspaces from the in-memory storage, while
     * {@link MachineManager#cleanup()} is responsible for machines destroying.
     */
    @PreDestroy
    @VisibleForTesting
    void stopRegistry() {
        isStopped = true;
        lock.writeLock().lock();
        try {
            new ArrayList<>(idToWorkspaces.values()).stream()
                                                    .map(UsersWorkspace::getId)
                                                    .forEach(this::doRemoveWorkspace);
        } finally {
            lock.writeLock().unlock();
        }
    }
}

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

import com.google.inject.Inject;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.machine.server.exception.SourceNotFoundException;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.event.WorkspaceCreatedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Throwables.getCausalChain;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.workspace.shared.Constants.AUTO_CREATE_SNAPSHOT;
import static org.eclipse.che.api.workspace.shared.Constants.AUTO_RESTORE_FROM_SNAPSHOT;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOPPED_BY;

/**
 * Facade for Workspace related operations.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 */
@Singleton
public class WorkspaceManager {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceManager.class);

    /** This attribute describes time when workspace was created. */
    public static final String CREATED_ATTRIBUTE_NAME = "created";
    /** This attribute describes time when workspace was last updated or started/stopped/recovered. */
    public static final String UPDATED_ATTRIBUTE_NAME = "updated";

    private final WorkspaceDao        workspaceDao;
    private final SnapshotDao         snapshotDao;
    private final WorkspaceRuntimes   runtimes;
    private final AccountManager      accountManager;
    private final WorkspaceSharedPool sharedPool;
    private final EventService        eventService;
    private final boolean             defaultAutoSnapshot;
    private final boolean             defaultAutoRestore;

    @Inject
    public WorkspaceManager(WorkspaceDao workspaceDao,
                            WorkspaceRuntimes workspaceRegistry,
                            EventService eventService,
                            AccountManager accountManager,
                            @Named("che.workspace.auto_snapshot") boolean defaultAutoSnapshot,
                            @Named("che.workspace.auto_restore") boolean defaultAutoRestore,
                            SnapshotDao snapshotDao,
                            WorkspaceSharedPool sharedPool) {
        this.workspaceDao = workspaceDao;
        this.snapshotDao = snapshotDao;
        this.runtimes = workspaceRegistry;
        this.accountManager = accountManager;
        this.eventService = eventService;
        this.defaultAutoSnapshot = defaultAutoSnapshot;
        this.defaultAutoRestore = defaultAutoRestore;
        this.sharedPool = sharedPool;
    }

    /**
     * Creates a new {@link WorkspaceImpl} instance based on the given configuration.
     *
     * @param config
     *         the workspace config to create the new workspace instance
     * @param namespace
     *         workspace name is unique in this namespace
     * @return new workspace instance
     * @throws NullPointerException
     *         when either {@code config} or {@code owner} is null
     * @throws NotFoundException
     *         when account with given id was not found
     * @throws ConflictException
     *         when any conflict occurs (e.g Workspace with such name already exists for {@code owner})
     * @throws ServerException
     *         when any other error occurs
     */
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace) throws ServerException,
                                                                  ConflictException,
                                                                  NotFoundException {
        requireNonNull(config, "Required non-null config");
        requireNonNull(namespace, "Required non-null namespace");
        WorkspaceImpl workspace = doCreateWorkspace(config,
                                                    accountManager.getByName(namespace),
                                                    emptyMap(),
                                                    false);
        workspace.setStatus(WorkspaceStatus.STOPPED);
        return workspace;
    }

    /**
     * Creates a new {@link Workspace} instance based on
     * the given configuration and the instance attributes.
     *
     * @param config
     *         the workspace config to create the new workspace instance
     * @param namespace
     *         workspace name is unique in this namespace
     * @param attributes
     *         workspace instance attributes
     * @return new workspace instance
     * @throws NullPointerException
     *         when either {@code config} or {@code owner} is null
     * @throws NotFoundException
     *         when account with given id was not found
     * @throws ConflictException
     *         when any conflict occurs (e.g Workspace with such name already exists for {@code owner})
     * @throws ServerException
     *         when any other error occurs
     */
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace,
                                         Map<String, String> attributes) throws ServerException,
                                                                                NotFoundException,
                                                                                ConflictException {
        requireNonNull(config, "Required non-null config");
        requireNonNull(namespace, "Required non-null namespace");
        requireNonNull(attributes, "Required non-null attributes");
        WorkspaceImpl workspace = doCreateWorkspace(config,
                                                    accountManager.getByName(namespace),
                                                    emptyMap(),
                                                    false);
        workspace.setStatus(WorkspaceStatus.STOPPED);
        return workspace;
    }

    /**
     * Gets workspace by composite key.
     *
     * <p> Key rules:
     * <ul>
     * <li>If it doesn't contain <b>:</b> character then that key is id(e.g. workspace123456)
     * <li>If it contains <b>:</b> character then that key is combination of user name and workspace name
     * <li><b></>:workspace_name</b> is valid abstract key and user will be detected from Environment.
     * <li><b>user_name:</b> is not valid abstract key
     * </ul>
     *
     * @param key
     *         composite key(e.g. workspace 'id' or 'namespace:name')
     * @return the workspace instance
     * @throws NullPointerException
     *         when {@code key} is null
     * @throws NotFoundException
     *         when workspace doesn't exist
     * @throws ServerException
     *         when any server error occurs
     */
    public WorkspaceImpl getWorkspace(String key) throws NotFoundException, ServerException {
        requireNonNull(key, "Required non-null workspace key");
        WorkspaceImpl workspace = getByKey(key);
        runtimes.injectRuntime(workspace);
        return workspace;
    }

    /**
     * Gets workspace by name and owner.
     *
     * <p>Returned instance status is either {@link WorkspaceStatus#STOPPED}
     * or  defined by its runtime(if exists).
     *
     * @param name
     *         the name of the workspace
     * @param namespace
     *         the owner of the workspace
     * @return the workspace instance
     * @throws NotFoundException
     *         when workspace with such id doesn't exist
     * @throws ServerException
     *         when any server error occurs
     */
    public WorkspaceImpl getWorkspace(String name, String namespace) throws NotFoundException, ServerException {
        requireNonNull(name, "Required non-null workspace name");
        requireNonNull(namespace, "Required non-null workspace owner");
        WorkspaceImpl workspace = workspaceDao.get(name, namespace);
        runtimes.injectRuntime(workspace);
        return workspace;
    }

    /**
     * Gets list of workspaces which user can read. Runtimes are included
     *
     * @param user
     *         the id of the user
     * @return the list of workspaces or empty list if user can't read any workspace
     * @throws NullPointerException
     *         when {@code user} is null
     * @throws ServerException
     *         when any server error occurs while getting workspaces with {@link WorkspaceDao#getWorkspaces(String)}
     * @deprecated use #getWorkspaces(String user, boolean includeRuntimes) instead
     */
    @Deprecated
    public List<WorkspaceImpl> getWorkspaces(String user) throws ServerException {
        return getWorkspaces(user, true);
    }

    /**
     * Gets list of workspaces which user can read
     *
     * <p>Returned workspaces have either {@link WorkspaceStatus#STOPPED} status
     * or status defined by their runtime instances(if those exist).
     *
     * @param user
     *         the id of the user
     * @param includeRuntimes
     *         if <code>true</code>, will fetch runtime info for workspaces.
     *         If <code>false</code>, will not fetch runtime info.
     * @return the list of workspaces or empty list if user can't read any workspace
     * @throws NullPointerException
     *         when {@code user} is null
     * @throws ServerException
     *         when any server error occurs while getting workspaces with {@link WorkspaceDao#getWorkspaces(String)}
     */
    public List<WorkspaceImpl> getWorkspaces(String user, boolean includeRuntimes) throws ServerException {
        requireNonNull(user, "Required non-null user id");
        final List<WorkspaceImpl> workspaces = workspaceDao.getWorkspaces(user);
        if (includeRuntimes) {
            injectRuntimes(workspaces);
        } else {
            injectStatuses(workspaces);
        }
        return workspaces;
    }

    /**
     * Gets list of workspaces which has given namespace. Runtimes are included
     *
     * @param namespace
     *         the namespace to find workspaces
     * @return the list of workspaces or empty list if no matches
     * @throws NullPointerException
     *         when {@code namespace} is null
     * @throws ServerException
     *         when any server error occurs while getting workspaces with {@link WorkspaceDao#getByNamespace(String)}
     * @deprecated use #getByNamespace(String user, boolean includeRuntimes) instead
     */
    @Deprecated
    public List<WorkspaceImpl> getByNamespace(String namespace) throws ServerException {
        return getByNamespace(namespace, true);
    }

    /**
     * Gets list of workspaces which has given namespace
     *
     * <p>Returned workspaces have either {@link WorkspaceStatus#STOPPED} status
     * or status defined by their runtime instances(if those exist).
     *
     * @param namespace
     *         the namespace to find workspaces
     * @param includeRuntimes
     *         if <code>true</code>, will fetch runtime info for workspaces.
     *         If <code>false</code>, will not fetch runtime info.
     * @return the list of workspaces or empty list if no matches
     * @throws NullPointerException
     *         when {@code namespace} is null
     * @throws ServerException
     *         when any server error occurs while getting workspaces with {@link WorkspaceDao#getByNamespace(String)}
     */
    public List<WorkspaceImpl> getByNamespace(String namespace, boolean includeRuntimes) throws ServerException {
        requireNonNull(namespace, "Required non-null namespace");
        final List<WorkspaceImpl> workspaces = workspaceDao.getByNamespace(namespace);
        if (includeRuntimes) {
            injectRuntimes(workspaces);
        } else {
            injectStatuses(workspaces);
        }
        return workspaces;
    }

    /**
     * Updates an existing workspace with a new configuration.
     *
     * <p>Replace strategy is used for workspace update, it means
     * that existing workspace data will be replaced with given {@code update}.
     *
     * @param update
     *         workspace update
     * @return updated instance of the workspace
     * @throws NullPointerException
     *         when either {@code workspaceId} or {@code update} is null
     * @throws NotFoundException
     *         when workspace with given id doesn't exist
     * @throws ConflictException
     *         when any conflict occurs (e.g Workspace with such name already exists in {@code namespace})
     * @throws ServerException
     *         when any other error occurs
     */
    public WorkspaceImpl updateWorkspace(String id, Workspace update) throws ConflictException,
                                                                             ServerException,
                                                                             NotFoundException {
        requireNonNull(id, "Required non-null workspace id");
        requireNonNull(update, "Required non-null workspace update");
        WorkspaceImpl workspace = workspaceDao.get(id);
        workspace.setConfig(new WorkspaceConfigImpl(update.getConfig()));
        update.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
        workspace.setAttributes(update.getAttributes());
        workspace.setTemporary(update.isTemporary());
        WorkspaceImpl updated = workspaceDao.update(workspace);
        runtimes.injectRuntime(updated);
        return updated;
    }

    /**
     * Removes workspace with specified identifier.
     *
     * <p>Does not remove the workspace if it has the runtime,
     * throws {@link ConflictException} in this case.
     * Won't throw any exception if workspace doesn't exist.
     *
     * @param workspaceId
     *         workspace id to remove workspace
     * @throws ConflictException
     *         when workspace has runtime
     * @throws ServerException
     *         when any server error occurs
     * @throws NullPointerException
     *         when {@code workspaceId} is null
     */
    public void removeWorkspace(String workspaceId) throws ConflictException, ServerException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        if (runtimes.hasRuntime(workspaceId)) {
            throw new ConflictException(format("The workspace '%s' is currently running and cannot be removed.",
                                               workspaceId));
        }

        workspaceDao.remove(workspaceId);
        LOG.info("Workspace '{}' removed by user '{}'", workspaceId, sessionUserNameOr("undefined"));
    }

    /**
     * Asynchronously starts certain workspace with specified environment and account.
     *
     * @param workspaceId
     *         identifier of workspace which should be started
     * @param envName
     *         name of environment or null, when default environment should be used
     * @param restore
     *         if <code>true</code> workspace will be restored from snapshot if snapshot exists,
     *         otherwise (if snapshot does not exist) workspace will be started from default source.
     *         If <code>false</code> workspace will be started from default source,
     *         even if auto-restore is enabled and snapshot exists.
     *         If <code>null</code> workspace will be restored from snapshot
     *         only if workspace has `auto-restore` attribute set to <code>true</code>,
     *         or system wide parameter `auto-restore` is enabled and snapshot exists.
     *         <p>
     *         This parameter has the highest priority to define if it is needed to restore from snapshot or not.
     *         If it is not defined workspace `auto-restore` attribute will be checked, then if last is not defined
     *         system wide `auto-restore` parameter will be checked.
     * @return starting workspace
     * @throws NullPointerException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace with given {@code workspaceId} doesn't exist
     * @throws ServerException
     *         when any other error occurs during workspace start
     */
    public WorkspaceImpl startWorkspace(String workspaceId,
                                        @Nullable String envName,
                                        @Nullable Boolean restore) throws NotFoundException,
                                                                          ServerException,
                                                                          ConflictException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        final WorkspaceImpl workspace = workspaceDao.get(workspaceId);
        final String restoreAttr = workspace.getAttributes().get(AUTO_RESTORE_FROM_SNAPSHOT);
        final boolean autoRestore = restoreAttr == null ? defaultAutoRestore : parseBoolean(restoreAttr);
        startAsync(workspace, envName, firstNonNull(restore, autoRestore) && !getSnapshot(workspaceId).isEmpty());
        runtimes.injectRuntime(workspace);
        return workspace;
    }

    /**
     * Asynchronously starts workspace from the given configuration.
     *
     * @param config
     *         workspace configuration from which workspace is created and started
     * @param namespace
     *         workspace name is unique in this namespace
     * @return starting workspace
     * @throws NullPointerException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace with given {@code workspaceId} doesn't exist
     * @throws ServerException
     *         when any other error occurs during workspace start
     */
    public WorkspaceImpl startWorkspace(WorkspaceConfig config,
                                        String namespace,
                                        boolean isTemporary) throws ServerException,
                                                                    NotFoundException,
                                                                    ConflictException {
        requireNonNull(config, "Required non-null configuration");
        requireNonNull(namespace, "Required non-null namespace");
        final WorkspaceImpl workspace = doCreateWorkspace(config,
                                                          accountManager.getByName(namespace),
                                                          emptyMap(),
                                                          isTemporary);
        startAsync(workspace, workspace.getConfig().getDefaultEnv(), false);
        runtimes.injectRuntime(workspace);
        return workspace;
    }

    /**
     * Starts machine in running workspace
     *
     * @param machineConfig
     *         configuration of machine to start
     * @param workspaceId
     *         id of workspace in which machine should be started
     * @throws NotFoundException
     *         if machine type from recipe is unsupported
     * @throws NotFoundException
     *         if no instance provider implementation found for provided machine type
     * @throws ConflictException
     *         if machine with given name already exists
     * @throws ConflictException
     *         if workspace is not in RUNNING state
     * @throws BadRequestException
     *         if machine name is invalid
     * @throws ServerException
     *         if any other exception occurs during starting
     */
    public void startMachine(MachineConfig machineConfig,
                             String workspaceId) throws ServerException,
                                                        ConflictException,
                                                        BadRequestException,
                                                        NotFoundException {

        final WorkspaceImpl workspace = getWorkspace(workspaceId);
        if (RUNNING != workspace.getStatus()) {
            throw new ConflictException(format("Workspace '%s' is not running, new machine can't be started", workspaceId));
        }

        startAsync(machineConfig, workspaceId);
    }

    /**
     * Asynchronously stops the workspace.
     *
     * @param workspaceId
     *         the id of the workspace to stop
     * @throws ServerException
     *         when any server error occurs
     * @throws NullPointerException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace {@code workspaceId} doesn't have runtime
     */
    public void stopWorkspace(String workspaceId) throws ServerException,
                                                         NotFoundException,
                                                         ConflictException {
        stopWorkspace(workspaceId, null);
    }

    /**
     * Asynchronously stops the workspace,
     * creates a snapshot of it if {@code createSnapshot} is set to true.
     *
     * @param workspaceId
     *         the id of the workspace to stop
     * @param createSnapshot
     *         true if create snapshot, false if don't,
     *         null if default behaviour should be used
     * @throws ServerException
     *         when any server error occurs
     * @throws NullPointerException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace {@code workspaceId} doesn't have runtime
     */
    public void stopWorkspace(String workspaceId, @Nullable Boolean createSnapshot) throws ConflictException,
                                                                                           NotFoundException,
                                                                                           ServerException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        final WorkspaceImpl workspace = workspaceDao.get(workspaceId);
        workspace.setStatus(runtimes.getStatus(workspaceId));
        if (workspace.getStatus() != WorkspaceStatus.RUNNING && workspace.getStatus() != WorkspaceStatus.STARTING) {
            throw new ConflictException(format("Could not stop the workspace '%s:%s' because its status is '%s'. " +
                                               "Workspace must be either 'STARTING' or 'RUNNING'",
                                               workspace.getNamespace(),
                                               workspace.getConfig().getName(),
                                               workspace.getStatus()));
        }
        stopAsync(workspace, createSnapshot);
    }

    /**
     * Creates snapshot of runtime workspace.
     *
     * <p>Basically creates {@link SnapshotImpl snapshot} instance for each machine from
     * runtime workspace's active environment.
     *
     * <p> If snapshot of workspace's dev machine was created successfully
     * publishes {@link EventType#SNAPSHOT_CREATED} event, otherwise publishes {@link EventType#SNAPSHOT_CREATION_ERROR}
     * with appropriate error message.
     *
     * <p> Note that:
     * <br>Snapshots are created asynchronously
     * <br>If snapshot creation for one machine failed, it wouldn't affect another snapshot creations
     *
     * @param workspaceId
     *         runtime workspace id
     * @throws NullPointerException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when runtime workspace with given id does not exist
     * @throws ServerException
     *         when any other error occurs
     * @throws ConflictException
     *         when workspace is not running
     */
    public void createSnapshot(String workspaceId) throws NotFoundException,
                                                          ServerException,
                                                          ConflictException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        runtimes.snapshotAsync(workspaceId);
    }

    /**
     * Returns list of machine snapshots which are related to workspace with given id.
     *
     * @param workspaceId
     *         workspace id to get snapshot
     * @return list of machine snapshots related to given workspace
     * @throws NullPointerException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace with given id doesn't exists
     * @throws ServerException
     *         when any other error occurs
     */
    public List<SnapshotImpl> getSnapshot(String workspaceId) throws ServerException, NotFoundException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        // check if workspace exists
        workspaceDao.get(workspaceId);
        return snapshotDao.findSnapshots(workspaceId);
    }

    /**
     * Removes all snapshots of workspace machines,
     * continues to remove snapshots even when removal of some of them fails.
     *
     * <p>Note that snapshots binaries are removed asynchronously
     * while metadata removal is synchronous operation.
     *
     * @param workspaceId
     *         workspace id to remove machine snapshots
     * @throws NotFoundException
     *         when workspace with given id doesn't exists
     * @throws ServerException
     *         when any other error occurs
     */
    public void removeSnapshots(String workspaceId) throws NotFoundException, ServerException {
        List<SnapshotImpl> snapshots = getSnapshot(workspaceId);
        List<SnapshotImpl> removed = new ArrayList<>(snapshots.size());
        for (SnapshotImpl snapshot : snapshots) {
            try {
                snapshotDao.removeSnapshot(snapshot.getId());
                removed.add(snapshot);
            } catch (Exception x) {
                LOG.error(format("Couldn't remove snapshot '%s' meta data, " +
                                 "binaries won't be removed either", snapshot.getId()), x);
            }
        }
        // binaries removal may take some time, do it asynchronously
        sharedPool.execute(() -> runtimes.removeBinaries(removed));
    }

    /**
     * Stops machine in running workspace.
     *
     * @param workspaceId
     *         ID of workspace that owns machine
     * @param machineId
     *         ID of machine that should be stopped
     * @throws NotFoundException
     *         if machine is not found in running workspace
     * @throws ConflictException
     *         if workspace is not running
     * @throws ConflictException
     *         if machine stop is forbidden (e.g. machine is dev-machine)
     * @throws ServerException
     *         if other error occurs
     */
    public void stopMachine(String workspaceId,
                            String machineId) throws NotFoundException,
                                                     ServerException,
                                                     ConflictException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        requireNonNull(machineId, "Required non-null machine id");
        final WorkspaceImpl workspace = workspaceDao.get(workspaceId);
        workspace.setStatus(runtimes.getStatus(workspaceId));
        checkWorkspaceIsRunning(workspace, format("stop machine with ID '%s' of", machineId));
        runtimes.stopMachine(workspaceId, machineId);
    }

    /**
     * Retrieves machine instance that allows to execute commands in a machine.
     *
     * @param workspaceId
     *         ID of workspace that owns machine
     * @param machineId
     *         ID of requested machine
     * @return instance of requested machine
     * @throws NotFoundException
     *         if workspace is not running
     * @throws NotFoundException
     *         if machine is not found in the workspace
     */
    public Instance getMachineInstance(String workspaceId,
                                       String machineId) throws NotFoundException,
                                                                ServerException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        requireNonNull(machineId, "Required non-null machine id");
        workspaceDao.get(workspaceId);
        return runtimes.getMachine(workspaceId, machineId);
    }

    /**
     * Shuts down workspace service and waits for it to finish, so currently
     * starting and running workspaces are stopped and it becomes unavailable to start new workspaces.
     *
     * @throws InterruptedException
     *         if it's interrupted while waiting for running workspaces to stop
     * @throws IllegalStateException
     *         if component shutdown is already called
     */
    public void shutdown() throws InterruptedException {
        if (!runtimes.refuseWorkspacesStart()) {
            throw new IllegalStateException("Workspace service shutdown has been already called");
        }
        stopRunningWorkspacesNormally();
        runtimes.shutdown();
        sharedPool.shutdown();
    }

    /**
     * Returns set of workspace ids that are not {@link WorkspaceStatus#STOPPED}.
     */
    public Set<String> getRunningWorkspacesIds() {
        return runtimes.getRuntimesIds();
    }

    /**
     * Stops all the running and starting workspaces - snapshotting them before if needed.
     * Workspace stop operations executed asynchronously while the method waits
     * for async task to finish.
     */
    private void stopRunningWorkspacesNormally() throws InterruptedException {
        if (runtimes.isAnyRunning()) {

            // getting all the running or starting workspaces
            ArrayList<WorkspaceImpl> runningOrStarting = new ArrayList<>();
            for (String workspaceId : runtimes.getRuntimesIds()) {
                try {
                    WorkspaceImpl workspace = workspaceDao.get(workspaceId);
                    workspace.setStatus(runtimes.getStatus(workspaceId));
                    if (workspace.getStatus() == WorkspaceStatus.RUNNING || workspace.getStatus() == WorkspaceStatus.STARTING) {
                        runningOrStarting.add(workspace);
                    }
                } catch (NotFoundException | ServerException x) {
                    if (runtimes.hasRuntime(workspaceId)) {
                        LOG.error("Couldn't get the workspace '{}' while it's running, the occurred error: '{}'",
                                  workspaceId,
                                  x.getMessage());
                    }
                }
            }

            // stopping them asynchronously
            CountDownLatch stopLatch = new CountDownLatch(runningOrStarting.size());
            for (WorkspaceImpl workspace : runningOrStarting) {
                try {
                    stopAsync(workspace, null).whenComplete((res, ex) -> stopLatch.countDown());
                } catch (Exception x) {
                    stopLatch.countDown();
                    if (runtimes.hasRuntime(workspace.getId())) {
                        LOG.warn("Couldn't stop the workspace '{}' normally, due to error: {}", workspace.getId(), x.getMessage());
                    }
                }
            }

            // wait for stopping workspaces to complete
            stopLatch.await();
        }
    }

    /** Asynchronously starts given workspace. */
    private void startAsync(WorkspaceImpl workspace,
                            String envName,
                            boolean recover) throws ConflictException,
                                                    NotFoundException,
                                                    ServerException {
        if (envName != null && !workspace.getConfig().getEnvironments().containsKey(envName)) {
            throw new NotFoundException(format("Workspace '%s:%s' doesn't contain environment '%s'",
                                               workspace.getNamespace(),
                                               workspace.getConfig().getName(),
                                               envName));
        }
        workspace.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
        workspaceDao.update(workspace);
        final String env = firstNonNull(envName, workspace.getConfig().getDefaultEnv());

        runtimes.startAsync(workspace, env, recover)
                .whenComplete((runtime, ex) -> {
                    if (ex == null) {
                        LOG.info("Workspace '{}:{}' with id '{}' started by user '{}'",
                                 workspace.getNamespace(),
                                 workspace.getConfig().getName(),
                                 workspace.getId(),
                                 sessionUserNameOr("undefined"));
                    } else {
                        if (workspace.isTemporary()) {
                            removeWorkspaceQuietly(workspace);
                        }
                        for (Throwable cause : getCausalChain(ex)) {
                            if (cause instanceof SourceNotFoundException) {
                                return;
                            }
                        }
                        try {
                            throw ex;
                        } catch (EnvironmentException envEx) {
                            // it's okay, e.g. recipe is invalid | start interrupted
                            LOG.info("Workspace '{}:{}' can't be started because: {}",
                                     workspace.getNamespace(),
                                     workspace.getConfig().getName(),
                                     envEx.getMessage());
                        } catch (Throwable thr) {
                            LOG.error(thr.getMessage(), thr);
                        }
                    }
                });
    }

    private CompletableFuture<Void> stopAsync(WorkspaceImpl workspace, @Nullable Boolean createSnapshot) throws ConflictException {
        return sharedPool.runAsync(() -> {
            final String stoppedBy = sessionUserNameOr(workspace.getAttributes().get(WORKSPACE_STOPPED_BY));
            LOG.info("Workspace '{}:{}' with id '{}' is being stopped by user '{}'",
                     workspace.getNamespace(),
                     workspace.getConfig().getName(),
                     workspace.getId(),
                     firstNonNull(stoppedBy, "undefined"));

            final boolean snapshotBeforeStop;
            if (workspace.isTemporary() || workspace.getStatus() == WorkspaceStatus.STARTING) {
                snapshotBeforeStop = false;
            } else if (createSnapshot != null) {
                snapshotBeforeStop = createSnapshot;
            } else if (workspace.getAttributes().containsKey(AUTO_CREATE_SNAPSHOT)) {
                snapshotBeforeStop = parseBoolean(workspace.getAttributes().get(AUTO_CREATE_SNAPSHOT));
            } else {
                snapshotBeforeStop = defaultAutoSnapshot;
            }

            if (snapshotBeforeStop) {
                try {
                    runtimes.snapshot(workspace.getId());
                } catch (ConflictException | NotFoundException | ServerException x) {
                    LOG.warn("Could not create a snapshot of the workspace '{}:{}' " +
                             "with workspace id '{}'. The workspace will be stopped",
                             workspace.getNamespace(),
                             workspace.getConfig().getName(),
                             workspace.getId());
                }
            }

            try {
                runtimes.stop(workspace.getId());
                if (!workspace.isTemporary()) {
                    workspace.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
                    workspaceDao.update(workspace);
                }
                LOG.info("Workspace '{}:{}' with id '{}' stopped by user '{}'",
                         workspace.getNamespace(),
                         workspace.getConfig().getName(),
                         workspace.getId(),
                         firstNonNull(stoppedBy, "undefined"));
            } catch (Exception ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            } finally {
                if (workspace.isTemporary()) {
                    removeWorkspaceQuietly(workspace);
                }
            }
        });
    }

    private void startAsync(MachineConfig machineConfig, String workspaceId) {
        sharedPool.execute(() -> {
            try {
                runtimes.startMachine(workspaceId, machineConfig);
            } catch (ApiException | EnvironmentException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        });
    }

    private void checkWorkspaceIsRunning(WorkspaceImpl workspace, String operation) throws ConflictException {
        if (workspace.getStatus() != RUNNING) {
            throw new ConflictException(format("Could not %s the workspace '%s:%s' because its status is '%s'.",
                                               operation,
                                               workspace.getNamespace(),
                                               workspace.getConfig().getName(),
                                               workspace.getStatus()));
        }
    }

    private void removeWorkspaceQuietly(Workspace workspace) {
        try {
            workspaceDao.remove(workspace.getId());
        } catch (ServerException x) {
            LOG.error("Unable to remove temporary workspace '{}'", workspace.getId());
        }
    }

    private Subject sessionUser() {
        return EnvironmentContext.getCurrent().getSubject();
    }

    private String sessionUserNameOr(String nameIfNoUser) {
        final Subject subject = EnvironmentContext.getCurrent().getSubject();
        if (!subject.isAnonymous()) {
            return subject.getUserName();
        }
        return nameIfNoUser;
    }

    private WorkspaceImpl doCreateWorkspace(WorkspaceConfig config,
                                            Account account,
                                            Map<String, String> attributes,
                                            boolean isTemporary) throws NotFoundException,
                                                                        ServerException,
                                                                        ConflictException {
        final WorkspaceImpl workspace = WorkspaceImpl.builder()
                                                     .generateId()
                                                     .setConfig(config)
                                                     .setAccount(account)
                                                     .setAttributes(attributes)
                                                     .setTemporary(isTemporary)
                                                     .build();
        workspace.getAttributes().put(CREATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
        workspaceDao.create(workspace);
        LOG.info("Workspace '{}:{}' with id '{}' created by user '{}'",
                 account.getName(),
                 workspace.getConfig().getName(),
                 workspace.getId(),
                 sessionUserNameOr("undefined"));
        eventService.publish(new WorkspaceCreatedEvent(workspace));
        return workspace;
    }

    /*
    * Get workspace using composite key.
    *
    */
    private WorkspaceImpl getByKey(String key) throws NotFoundException, ServerException {
        String[] parts = key.split(":", -1); // -1 is to prevent skipping trailing part
        if (parts.length == 1) {
            return workspaceDao.get(key);
        }
        final String nsPart = parts[0];
        final String wsName = parts[1];
        final String namespace = nsPart.isEmpty() ? sessionUser().getUserName() : nsPart;
        return workspaceDao.get(wsName, namespace);
    }


    private void injectRuntimes(List<? extends WorkspaceImpl> workspaces) {
        for (WorkspaceImpl workspace : workspaces) {
            runtimes.injectRuntime(workspace);
        }
    }

    private void injectStatuses(List<? extends WorkspaceImpl> workspaces) {
        for (WorkspaceImpl workspace : workspaces) {
            workspace.setStatus(runtimes.getStatus(workspace.getId()));
        }
    }
}

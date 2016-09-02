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
import com.google.inject.Inject;

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
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes.RuntimeDescriptor;
import org.eclipse.che.api.workspace.server.event.WorkspaceCreatedEvent;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.workspace.shared.Constants.AUTO_CREATE_SNAPSHOT;
import static org.eclipse.che.api.workspace.shared.Constants.AUTO_RESTORE_FROM_SNAPSHOT;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOPPED_BY;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.SNAPSHOT_CREATED;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.SNAPSHOT_CREATING;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.SNAPSHOT_CREATION_ERROR;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

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
    /** This attribute describes time when workspace was last update or started/stopped/recovered. */
    public static final String UPDATED_ATTRIBUTE_NAME = "updated";

    private final WorkspaceDao      workspaceDao;
    private final WorkspaceRuntimes runtimes;
    private final EventService      eventService;
    private final ExecutorService   executor;
    private final boolean           defaultAutoSnapshot;
    private final boolean           defaultAutoRestore;
    private final SnapshotDao       snapshotDao;

    private WorkspaceHooks hooks = new NoopWorkspaceHooks();

    @Inject
    public WorkspaceManager(WorkspaceDao workspaceDao,
                            WorkspaceRuntimes workspaceRegistry,
                            EventService eventService,
                            @Named("workspace.runtime.auto_snapshot") boolean defaultAutoSnapshot,
                            @Named("workspace.runtime.auto_restore") boolean defaultAutoRestore,
                            SnapshotDao snapshotDao) {
        this.workspaceDao = workspaceDao;
        this.runtimes = workspaceRegistry;
        this.eventService = eventService;
        this.defaultAutoSnapshot = defaultAutoSnapshot;
        this.defaultAutoRestore = defaultAutoRestore;
        this.snapshotDao = snapshotDao;

        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("WorkspaceManager-%d")
                                                                           .setDaemon(true)
                                                                           .build());
    }

    @Inject(optional = true)
    public void setHooks(WorkspaceHooks hooks) {
        this.hooks = hooks;
    }

    /**
     * Creates a new {@link WorkspaceImpl} instance based on the given configuration.
     *
     * @param config
     *         the workspace config to create the new workspace instance
     * @param namespace
     *         workspace name is unique in this namespace
     * @param accountId
     *         the account id, which is used to verify if the user has required
     *         permissions to create the new workspace
     * @return new workspace instance
     * @throws NullPointerException
     *         when either {@code config} or {@code owner} is null
     * @throws NotFoundException
     *         when account with given id was not found
     * @throws ConflictException
     *         when any conflict occurs (e.g Workspace with such name already exists for {@code owner})
     * @throws ServerException
     *         when any other error occurs
     * @see WorkspaceHooks#beforeCreate(Workspace, String)
     * @see WorkspaceHooks#afterCreate(Workspace, String)
     */
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace,
                                         @Nullable String accountId) throws ServerException,
                                                                            ConflictException,
                                                                            NotFoundException {
        requireNonNull(config, "Required non-null config");
        requireNonNull(namespace, "Required non-null namespace");
        return normalizeState(doCreateWorkspace(config,
                                                namespace,
                                                emptyMap(),
                                                false,
                                                accountId));
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
     * @param accountId
     *         the account id, which is used to verify if the user has required
     *         permissions to create the new workspace
     * @return new workspace instance
     * @throws NullPointerException
     *         when either {@code config} or {@code owner} is null
     * @throws NotFoundException
     *         when account with given id was not found
     * @throws ConflictException
     *         when any conflict occurs (e.g Workspace with such name already exists for {@code owner})
     * @throws ServerException
     *         when any other error occurs
     * @see WorkspaceHooks#beforeCreate(Workspace, String)
     * @see WorkspaceHooks#afterCreate(Workspace, String)
     */
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace,
                                         Map<String, String> attributes,
                                         @Nullable String accountId) throws ServerException,
                                                                            NotFoundException,
                                                                            ConflictException {
        requireNonNull(config, "Required non-null config");
        requireNonNull(namespace, "Required non-null namespace");
        requireNonNull(attributes, "Required non-null attributes");
        return normalizeState(doCreateWorkspace(config,
                                                namespace,
                                                attributes,
                                                false,
                                                accountId));
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
        return normalizeState(getByKey(key));
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
        return normalizeState(workspaceDao.get(name, namespace));
    }

    /**
     * Gets list of workspaces which user can read
     *
     * <p>Returned workspaces have either {@link WorkspaceStatus#STOPPED} status
     * or status defined by their runtime instances(if those exist).
     *
     * @param user
     *         the id of the user
     * @return the list of workspaces or empty list if user can't read any workspace
     * @throws NullPointerException
     *         when {@code user} is null
     * @throws ServerException
     *         when any server error occurs while getting workspaces with {@link WorkspaceDao#getWorkspaces(String)}
     */
    public List<WorkspaceImpl> getWorkspaces(String user) throws ServerException {
        requireNonNull(user, "Required non-null user id");
        final List<WorkspaceImpl> workspaces = workspaceDao.getWorkspaces(user);
        for (WorkspaceImpl workspace : workspaces) {
            normalizeState(workspace);
        }
        return workspaces;
    }

    /**
     * Gets list of workspaces which has given namespace
     *
     * <p>Returned workspaces have either {@link WorkspaceStatus#STOPPED} status
     * or status defined by their runtime instances(if those exist).
     *
     * @param namespace
     *         the namespace to find workspaces
     * @return the list of workspaces or empty list if no matches
     * @throws NullPointerException
     *         when {@code namespace} is null
     * @throws ServerException
     *         when any server error occurs while getting workspaces with {@link WorkspaceDao#getByNamespace(String)}
     */
    public List<WorkspaceImpl> getByNamespace(String namespace) throws ServerException {
        requireNonNull(namespace, "Required non-null namespace");
        final List<WorkspaceImpl> workspaces = workspaceDao.getByNamespace(namespace);
        for (WorkspaceImpl workspace : workspaces) {
            normalizeState(workspace);
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
        final WorkspaceImpl workspace = workspaceDao.get(id);
        workspace.setConfig(new WorkspaceConfigImpl(update.getConfig()));
        update.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
        workspace.setAttributes(update.getAttributes());
        return normalizeState(workspaceDao.update(workspace));
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
     * @see WorkspaceHooks#afterRemove(String)
     */
    public void removeWorkspace(String workspaceId) throws ConflictException, ServerException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        if (runtimes.hasRuntime(workspaceId)) {
            throw new ConflictException("The workspace '" + workspaceId + "' is currently running and cannot be removed.");
        }
        workspaceDao.remove(workspaceId);
        hooks.afterRemove(workspaceId);
        eventService.publish(new WorkspaceRemovedEvent(workspaceId));
        LOG.info("Workspace '{}' removed by user '{}'", workspaceId, sessionUserNameOr("undefined"));
    }

    /**
     * Asynchronously starts certain workspace with specified environment and account.
     *
     * @param workspaceId
     *         identifier of workspace which should be started
     * @param envName
     *         name of environment or null, when default environment should be used
     * @param accountId
     *         account which should be used for this runtime workspace or null when
     *         it should be automatically detected
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
     *         when workspace with given {@code workspaceId} doesn't exist, or
     *         {@link WorkspaceHooks#beforeStart(Workspace, String, String)} throws this exception
     * @throws ServerException
     *         when any other error occurs during workspace start
     */
    public WorkspaceImpl startWorkspace(String workspaceId,
                                        @Nullable String envName,
                                        @Nullable String accountId,
                                        @Nullable Boolean restore) throws NotFoundException,
                                                                          ServerException,
                                                                          ConflictException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        final WorkspaceImpl workspace = workspaceDao.get(workspaceId);
        final String restoreAttr = workspace.getAttributes().get(AUTO_RESTORE_FROM_SNAPSHOT);
        final boolean autoRestore = restoreAttr == null ? defaultAutoRestore : parseBoolean(restoreAttr);
        final boolean snapshotExists = !getSnapshot(workspaceId).isEmpty();
        return performAsyncStart(workspace, envName, firstNonNull(restore, autoRestore) && snapshotExists, accountId);
    }

    /**
     * Asynchronously starts workspace from the given configuration.
     *
     * @param config
     *         workspace configuration from which workspace is created and started
     * @param namespace
     *         workspace name is unique in this namespace
     * @param accountId
     *         account which should be used for this runtime workspace or null when
     *         it should be automatically detected
     * @return starting workspace
     * @throws NullPointerException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace with given {@code workspaceId} doesn't exist, or
     *         {@link WorkspaceHooks#beforeStart(Workspace, String, String)} throws this exception
     * @throws ServerException
     *         when any other error occurs during workspace start
     */
    public WorkspaceImpl startWorkspace(WorkspaceConfig config,
                                        String namespace,
                                        boolean isTemporary,
                                        @Nullable String accountId) throws ServerException,
                                                                           NotFoundException,
                                                                           ConflictException {
        requireNonNull(config, "Required non-null configuration");
        requireNonNull(namespace, "Required non-null namespace");
        final WorkspaceImpl workspace = doCreateWorkspace(config,
                                                          namespace,
                                                          emptyMap(),
                                                          isTemporary,
                                                          accountId);
        performAsyncStart(workspace, workspace.getConfig().getDefaultEnv(), false, accountId);
        return normalizeState(workspace);
    }

    /**
     * Starts machine in running workspace
     *
     * @param machineConfig configuration of machine to start
     * @param workspaceId id of workspace in which machine should be started
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

        performAsyncStart(machineConfig, workspaceId);
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
        requireNonNull(workspaceId, "Required non-null workspace id");
        final WorkspaceImpl workspace = normalizeState(workspaceDao.get(workspaceId));
        checkWorkspaceIsRunning(workspace, "stop");
        performAsyncStop(workspace);
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
        final WorkspaceImpl workspace = normalizeState(workspaceDao.get(workspaceId));
        checkWorkspaceIsRunning(workspace, "create a snapshot of");
        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            createSnapshotSync(workspace.getRuntime(),
                               workspace.getNamespace(),
                               workspaceId);
        }));
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
        final WorkspaceImpl workspace = workspaceDao.get(workspaceId);
        return snapshotDao.findSnapshots(workspace.getNamespace(), workspaceId);
    }

    /**
     * Removes all snapshots of workspace machines.
     * Continues to remove snapshots even when removal of some of them fails.
     *
     * @param workspaceId workspace id to remove machine snapshots
     * @throws NotFoundException
     *         when workspace with given id doesn't exists
     * @throws ServerException
     *         when any other error occurs
     */
    public void removeSnapshots(String workspaceId) throws NotFoundException, ServerException {
        List<SnapshotImpl> snapshots = getSnapshot(workspaceId);
        for (SnapshotImpl snapshot : snapshots) {
            try {
                runtimes.removeSnapshot(snapshot);
                snapshotDao.removeSnapshot(snapshot.getId());
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
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
        final WorkspaceImpl workspace = normalizeState(workspaceDao.get(workspaceId));
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
        normalizeState(workspaceDao.get(workspaceId));
        return runtimes.getMachine(workspaceId, machineId);
    }

    /** Asynchronously starts given workspace. */
    @VisibleForTesting
    WorkspaceImpl performAsyncStart(WorkspaceImpl workspace,
                                    String envName,
                                    boolean recover,
                                    @Nullable String accountId) throws ConflictException, NotFoundException, ServerException {
        if (envName != null && !workspace.getConfig()
                                         .getEnvironments()
                                         .containsKey(envName)) {
            throw new NotFoundException(format("Workspace '%s:%s' doesn't contain environment '%s'",
                                               workspace.getNamespace(),
                                               workspace.getConfig().getName(),
                                               envName));
        }
        // WorkspaceRuntimes performs this check as well
        // but this check needed here because permanent workspace start performed asynchronously
        // which means that even if workspace runtimes won't start workspace client receives workspace object
        // with starting status, this check prevents it and throws appropriate exception
        try {
            final RuntimeDescriptor descriptor = runtimes.get(workspace.getId());
            throw new ConflictException(format("Could not start workspace '%s' because its status is '%s'",
                                               workspace.getConfig().getName(),
                                               descriptor.getRuntimeStatus()));
        } catch (NotFoundException ignored) {
            // it is okay if workspace does not exist in runtimes
        }

        workspace.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
        workspaceDao.update(workspace);

        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            try {
                final String env = firstNonNull(envName, workspace.getConfig().getDefaultEnv());
                hooks.beforeStart(workspace, env, accountId);
                runtimes.start(workspace, env, recover);
                LOG.info("Workspace '{}:{}' with id '{}' started by user '{}'",
                         workspace.getNamespace(),
                         workspace.getConfig().getName(),
                         workspace.getId(),
                         sessionUserNameOr("undefined"));
            } catch (RuntimeException | ApiException ex) {
                if (workspace.isTemporary()) {
                    try {
                        removeWorkspace(workspace.getId());
                    } catch (ConflictException | ServerException rmEx) {
                        LOG.error("Couldn't remove temporary workspace {}, because : {}",
                                  workspace.getId(),
                                  rmEx.getLocalizedMessage());
                    }
                }
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }));
        return normalizeState(workspace);
    }

    /**
     * Asynchronously creates a snapshot(if workspace contains {@link Constants#AUTO_CREATE_SNAPSHOT}
     * attribute set to true) and then stops the workspace(even if snapshot creation failed).
     */
    @VisibleForTesting
    void performAsyncStop(WorkspaceImpl workspace) throws ConflictException {
        checkWorkspaceIsRunning(workspace, "stop");
        final String autoSnapshotAttr = workspace.getAttributes().get(AUTO_CREATE_SNAPSHOT);
        final boolean createSnapshot = autoSnapshotAttr == null ? defaultAutoSnapshot : parseBoolean(autoSnapshotAttr);
        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            final String stoppedBy = sessionUserNameOr(workspace.getAttributes().get(WORKSPACE_STOPPED_BY));
            LOG.info("Workspace '{}:{}' with id '{}' is being stopped by user '{}'",
                     workspace.getNamespace(),
                     workspace.getConfig().getName(),
                     workspace.getId(),
                     firstNonNull(stoppedBy, "undefined"));
            if (createSnapshot && !createSnapshotSync(workspace.getRuntime(), workspace.getNamespace(), workspace.getId())) {
                LOG.warn("Could not create a snapshot of the workspace '{}:{}' with workspace id '{}'. The workspace will be stopped",
                         workspace.getNamespace(),
                         workspace.getConfig().getName(),
                         workspace.getId());
            }
            try {
                runtimes.stop(workspace.getId());
                if (workspace.isTemporary()) {
                    workspaceDao.remove(workspace.getId());
                } else {
                    workspace.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
                    workspaceDao.update(workspace);
                }
                LOG.info("Workspace '{}:{}' with id '{}' stopped by user '{}'",
                         workspace.getNamespace(),
                         workspace.getConfig().getName(),
                         workspace.getId(),
                         firstNonNull(stoppedBy, "undefined"));
            } catch (RuntimeException | ConflictException | NotFoundException | ServerException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }));
    }

    private void performAsyncStart(MachineConfig machineConfig, String workspaceId) {
        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            try {
                runtimes.startMachine(workspaceId, machineConfig);
            } catch (ApiException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }));
    }

    /**
     * Synchronously creates snapshot of the workspace.
     *
     * @return true if snapshot of dev-machine was successfully created
     * otherwise returns false.
     */
    @VisibleForTesting
    boolean createSnapshotSync(WorkspaceRuntimeImpl runtime, String namespace, String workspaceId) {
        eventService.publish(newDto(WorkspaceStatusEvent.class)
                                     .withEventType(SNAPSHOT_CREATING)
                                     .withWorkspaceId(workspaceId));
        String devMachineSnapshotFailMessage = null;
        for (MachineImpl machine : runtime.getMachines()) {
            try {
                SnapshotImpl snapshot = runtimes.saveMachine(namespace,
                                                             workspaceId,
                                                             machine.getId());
                snapshotDao.saveSnapshot(snapshot);
            } catch (ApiException apiEx) {
                if (machine.getConfig().isDev()) {
                    devMachineSnapshotFailMessage = apiEx.getLocalizedMessage();
                }
                LOG.error(apiEx.getLocalizedMessage(), apiEx);
            }
        }
        if (devMachineSnapshotFailMessage != null) {
            eventService.publish(newDto(WorkspaceStatusEvent.class)
                                         .withEventType(SNAPSHOT_CREATION_ERROR)
                                         .withWorkspaceId(workspaceId)
                                         .withError(devMachineSnapshotFailMessage));
        } else {
            eventService.publish(newDto(WorkspaceStatusEvent.class)
                                         .withEventType(SNAPSHOT_CREATED)
                                         .withWorkspaceId(workspaceId));
        }
        return devMachineSnapshotFailMessage == null;
    }

    @VisibleForTesting
    void checkWorkspaceIsRunning(WorkspaceImpl workspace, String operation) throws ConflictException {
        if (workspace.getStatus() != RUNNING) {
            throw new ConflictException(format("Could not %s the workspace '%s:%s' because its status is '%s'.",
                                               operation,
                                               workspace.getNamespace(),
                                               workspace.getConfig().getName(),
                                               workspace.getStatus()));
        }
    }

    private Subject sessionUser() {
        return EnvironmentContext.getCurrent().getSubject();
    }

    private String sessionUserNameOr(String nameIfNoUser) {
        final Subject subject;
        if (EnvironmentContext.getCurrent() != null && (subject = EnvironmentContext.getCurrent().getSubject()) != null) {
            return subject.getUserName();
        }
        return nameIfNoUser;
    }

    private WorkspaceImpl normalizeState(WorkspaceImpl workspace) throws ServerException {
        try {
            return normalizeState(workspace, runtimes.get(workspace.getId()));
        } catch (NotFoundException e) {
            return normalizeState(workspace, null);
        }
    }

    private WorkspaceImpl normalizeState(WorkspaceImpl workspace, RuntimeDescriptor descriptor) {
        if (descriptor != null) {
            workspace.setStatus(descriptor.getRuntimeStatus());
            workspace.setRuntime(new WorkspaceRuntimeImpl(descriptor.getRuntime()));
        } else {
            workspace.setStatus(STOPPED);
        }
        return workspace;
    }

    private WorkspaceImpl doCreateWorkspace(WorkspaceConfig config,
                                            String namespace,
                                            Map<String, String> attributes,
                                            boolean isTemporary,
                                            @Nullable String accountId) throws NotFoundException,
                                                                               ServerException,
                                                                               ConflictException {
        final WorkspaceImpl workspace = WorkspaceImpl.builder()
                                                     .generateId()
                                                     .setConfig(config)
                                                     .setNamespace(namespace)
                                                     .setAttributes(attributes)
                                                     .setTemporary(isTemporary)
                                                     .build();
        workspace.getAttributes().put(CREATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
        hooks.beforeCreate(workspace, accountId);
        workspaceDao.create(workspace);
        hooks.afterCreate(workspace, accountId);
        LOG.info("Workspace '{}:{}' with id '{}' created by user '{}'",
                 namespace,
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

    /** No-operations workspace hooks. Each method does nothing */
    private static class NoopWorkspaceHooks implements WorkspaceHooks {
        @Override
        public void beforeStart(Workspace workspace, String evnName, String accountId) throws NotFoundException, ServerException {}

        @Override
        public void beforeCreate(Workspace workspace, String accountId) throws NotFoundException, ServerException {}

        @Override
        public void afterCreate(Workspace workspace, String accountId) throws ServerException {}

        @Override
        public void afterRemove(String workspaceId) {}
    }
}

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
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeWorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.ERROR;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.RUNNING;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.SNAPSHOT_CREATED;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.SNAPSHOT_CREATION_ERROR;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.STARTING;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.STOPPING;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Facade for Workspace related operations.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceManager {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceManager.class);

    private final WorkspaceDao             workspaceDao;
    private final RuntimeWorkspaceRegistry workspaceRegistry;
    private final WorkspaceConfigValidator configValidator;
    private final EventService             eventService;
    private final ExecutorService          executor;
    private final MachineManager           machineManager;

    private WorkspaceHooks hooks = new NoopWorkspaceHooks();

    @Inject
    public WorkspaceManager(WorkspaceDao workspaceDao,
                            RuntimeWorkspaceRegistry workspaceRegistry,
                            WorkspaceConfigValidator workspaceConfigValidator,
                            EventService eventService,
                            MachineManager machineManager) {
        this.workspaceDao = workspaceDao;
        this.workspaceRegistry = workspaceRegistry;
        this.configValidator = workspaceConfigValidator;
        this.eventService = eventService;
        this.machineManager = machineManager;

        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("WorkspaceManager-%d")
                                                                           .setDaemon(true)
                                                                           .build());
    }

    @Inject(optional = true)
    public void setHooks(WorkspaceHooks hooks) {
        this.hooks = hooks;
    }

    /**
     * Creates a new {@link UsersWorkspace} instance based on the given configuration.
     *
     * @param config
     *         the workspace config to create the new workspace instance
     * @param owner
     *         the id of the user who will be the workspace owner
     * @param accountId
     *         the account id, which is used to verify if the user has required
     *         permissions to create the new workspace
     * @return new workspace instance
     * @throws NotFoundException
     *         when account with given id was not found
     * @throws ForbiddenException
     *         when user doesn't have access to create workspace
     * @throws BadRequestException
     *         when either {@code config} or {@code owner} is null
     * @throws BadRequestException
     *         when {@code config} is not valid
     * @throws ConflictException
     *         when any conflict occurs (e.g Workspace with such name already exists for {@code owner})
     * @throws ServerException
     *         when any other error occurs
     * @see WorkspaceHooks#beforeCreate(UsersWorkspace, String)
     * @see WorkspaceHooks#afterCreate(UsersWorkspace, String)
     */
    public UsersWorkspaceImpl createWorkspace(WorkspaceConfig config, String owner, @Nullable String accountId) throws ForbiddenException,
                                                                                                                       ServerException,
                                                                                                                       BadRequestException,
                                                                                                                       ConflictException,
                                                                                                                       NotFoundException {
        final UsersWorkspaceImpl newWorkspace = fromConfig(config, owner);

        hooks.beforeCreate(newWorkspace, accountId);
        workspaceDao.create(newWorkspace);
        hooks.afterCreate(newWorkspace, accountId);

        // TODO move 'analytics' logs to the appropriate interceptors
        LOG.info("EVENT#workspace-created# WS#{}# WS-ID#{}# USER#{}#",
                 newWorkspace.getConfig().getName(),
                 newWorkspace.getId(),
                 getCurrentUserId());
        return normalizeState(newWorkspace);
    }

    /**
     * Gets workspace by its id.
     *
     * <p>Returned instance always permanent(non-temporary), contains websocket channels
     * and with either {@link WorkspaceStatus#STOPPED} status or status defined by its runtime(if exists).
     *
     * @param workspaceId
     *         workspace id
     * @return the workspace instance
     * @throws BadRequestException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace doesn't exist
     * @throws ServerException
     *         when any server error occurs
     */
    public UsersWorkspaceImpl getWorkspace(String workspaceId) throws NotFoundException, ServerException, BadRequestException {
        requiredNotNull(workspaceId, "Required non-null workspace id");
        return normalizeState(workspaceDao.get(workspaceId));
    }

    /**
     * Gets workspace by name and owner.
     *
     * <p>Returned instance always permanent(non-temporary), contains websocket channels
     * and with either {@link WorkspaceStatus#STOPPED} status or status defined by its runtime(if exists).
     *
     * @param name
     *         the name of the workspace
     * @param owner
     *         the owner of the workspace
     * @return the workspace instance
     * @throws BadRequestException
     * @throws NotFoundException
     * @throws ServerException
     */
    public UsersWorkspaceImpl getWorkspace(String name, String owner) throws BadRequestException, NotFoundException, ServerException {
        requiredNotNull(name, "Required non-null workspace name");
        requiredNotNull(owner, "Required non-null workspace owner");
        return normalizeState(workspaceDao.get(name, owner));
    }

    /**
     * Gets all user's workspaces(workspaces where user is owner).
     *
     * <p>Returned workspaces have either {@link WorkspaceStatus#STOPPED} status
     * or status defined by their runtime instances(if those exist), all of these
     * workspaces are permanent(non-temporary)
     *
     * @param owner
     *         the id of the user whose workspaces should be fetched
     * @return the list of workspaces or empty list if user doesn't own any workspace
     * @throws BadRequestException
     *         when {@code owner} is null
     * @throws ServerException
     *         when any server error occurs while getting workspaces with {@link WorkspaceDao#getByOwner(String)}
     */
    public List<UsersWorkspaceImpl> getWorkspaces(String owner) throws ServerException, BadRequestException {
        requiredNotNull(owner, "Required non-null workspace owner");
        final Map<String, RuntimeWorkspaceImpl> runtimeWorkspaces = new HashMap<>();
        for (RuntimeWorkspaceImpl runtimeWorkspace : workspaceRegistry.getByOwner(owner)) {
            runtimeWorkspaces.put(runtimeWorkspace.getId(), runtimeWorkspace);
        }
        final List<UsersWorkspaceImpl> workspaces = workspaceDao.getByOwner(owner);
        for (UsersWorkspaceImpl workspace : workspaces) {
            normalizeState(workspace, runtimeWorkspaces.get(workspace.getId()));
        }
        return workspaces;
    }

    /**
     * Updates the existing workspace with the new configuration.
     *
     * <p>Replace strategy is used for workspace update, it means
     * that existing workspace data will be replaced with given {@code update}.
     *
     * @param workspaceId
     *         the id of the workspace which should be updated
     * @param updateConfig
     *         the workspace update
     * @return updated instance of the workspace
     * @throws BadRequestException
     *         when {@code update} is not valid
     * @throws BadRequestException
     *         when either {@code workspaceId} or {@code update} is null
     * @throws NotFoundException
     *         when workspace with given id doesn't exist
     * @throws ConflictException
     *         when any conflict occurs (e.g Workspace with such name already exists for {@code owner})
     * @throws ServerException
     *         when any other error occurs
     */
    public UsersWorkspaceImpl updateWorkspace(String workspaceId, WorkspaceConfig updateConfig) throws ConflictException,
                                                                                                       ServerException,
                                                                                                       BadRequestException,
                                                                                                       NotFoundException {
        configValidator.validate(updateConfig);
        final UsersWorkspaceImpl updated = workspaceDao.update(new UsersWorkspaceImpl(updateConfig, workspaceId, getCurrentUserId()));
        // TODO move 'analytics' logs to the appropriate interceptors
        LOG.info("EVENT#workspace-updated# WS#{}# WS-ID#{}#", updated.getConfig().getName(), updated.getId());
        return normalizeState(updated);
    }

    /**
     * Removes workspace with specified identifier.
     *
     * <p>Does not remove the workspace if it has the runtime, throws {@link ConflictException} in this case.
     * Doesn't throw any exception if workspace doesn't exist.
     *
     * @param workspaceId
     *         workspace id to remove workspace
     * @throws ConflictException
     *         when workspace has runtime
     * @throws ServerException
     *         when any server error occurs
     * @throws BadRequestException
     *         when {@code workspaceId} is null
     * @see WorkspaceHooks#afterRemove(String)
     */
    public void removeWorkspace(String workspaceId) throws ConflictException, ServerException, BadRequestException {
        requiredNotNull(workspaceId, "Required non-null workspace id");
        if (workspaceRegistry.hasRuntime(workspaceId)) {
            throw new ConflictException("The workspace " + workspaceId + " is currently running and cannot be removed.");
        }
        workspaceDao.remove(workspaceId);
        hooks.afterRemove(workspaceId);
        LOG.info("EVENT#workspace-remove# WS-ID#{}#", workspaceId);
    }

    /**
     * Gets runtime workspace by its id.
     *
     * @param workspaceId
     *         workspace id
     * @return runtime workspace instance
     * @throws BadRequestException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when runtime workspace with given id doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    public RuntimeWorkspaceImpl getRuntimeWorkspace(String workspaceId) throws BadRequestException, NotFoundException, ServerException {
        requiredNotNull(workspaceId, "Required non-null workspace id");
        return workspaceRegistry.get(workspaceId);
    }

    /**
     * Gets runtime workspaces owned by specified user.
     *
     * @param owner
     *         workspaces owner identifier
     * @return list of runtime workspaces owned by {@code owner} or empty list when user doesn't have workspaces running
     * @throws BadRequestException
     *         when {@code owner} is null
     */
    public List<RuntimeWorkspaceImpl> getRuntimeWorkspaces(String owner) throws BadRequestException {
        requiredNotNull(owner, "Required non-null workspace owner");
        return workspaceRegistry.getByOwner(owner);
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
     * @return starting workspace
     * @throws BadRequestException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace with given {@code workspaceId} doesn't exist, or
     *         {@link WorkspaceHooks#beforeStart(UsersWorkspace, String, String)} throws this exception
     * @throws ForbiddenException
     *         when user doesn't have access to start workspace in certain account
     * @throws ServerException
     *         when any other error occurs during workspace start
     * @see WorkspaceHooks#beforeStart(UsersWorkspace, String, String)
     * @see RuntimeWorkspaceRegistry#start(UsersWorkspace, String)
     */
    public UsersWorkspaceImpl startWorkspaceById(String workspaceId,
                                                 @Nullable String envName,
                                                 @Nullable String accountId) throws NotFoundException,
                                                                                    ServerException,
                                                                                    BadRequestException,
                                                                                    ForbiddenException,
                                                                                    ConflictException {
        requiredNotNull(workspaceId, "Required non-null workspace id");
        return performAsyncStart(workspaceDao.get(workspaceId), envName, false, accountId);
    }

    /**
     * Asynchronously starts certain workspace with specified environment and account.
     *
     * @param workspaceName
     *         name of workspace which should be started
     * @param envName
     *         name of environment or null, when default environment should be used
     * @param owner
     *         owner of the workspace which should be started
     * @param accountId
     *         account which should be used for this runtime workspace or null when
     *         it should be automatically detected
     * @return starting workspace
     * @throws BadRequestException
     *         when given {@code workspaceName} or {@code owner} is null
     * @throws NotFoundException
     *         when workspace with given {@code workspaceName & owner} doesn't exist, or
     *         {@link WorkspaceHooks#beforeStart(UsersWorkspace, String, String)} throws this exception
     * @throws ForbiddenException
     *         when user doesn't have access to start workspace in certain account
     * @throws ServerException
     *         when any other error occurs during workspace start
     * @see WorkspaceHooks#beforeStart(UsersWorkspace, String, String)
     * @see RuntimeWorkspaceRegistry#start(UsersWorkspace, String)
     */
    public UsersWorkspaceImpl startWorkspaceByName(String workspaceName,
                                                   String owner,
                                                   @Nullable String envName,
                                                   @Nullable String accountId) throws NotFoundException,
                                                                                      ServerException,
                                                                                      BadRequestException,
                                                                                      ForbiddenException,
                                                                                      ConflictException {
        requiredNotNull(workspaceName, "Required non-null workspace name");
        requiredNotNull(owner, "Required non-null workspace owner");
        return performAsyncStart(workspaceDao.get(workspaceName, owner), envName, false, accountId);
    }

    /**
     * Synchronously starts temporary workspace based on config and account.
     *
     * @param workspaceConfig
     *         workspace configuration
     * @param accountId
     *         account which should be used for this runtime workspace or null when
     *         it should be automatically detected
     * @return running workspace
     * @throws BadRequestException
     *         when {@code workspaceConfig} is null or not valid
     * @throws ForbiddenException
     *         when user doesn't have access to start workspace in certain account
     * @throws NotFoundException
     *         when {@link WorkspaceHooks#beforeCreate(UsersWorkspace, String)}
     *         or {@link WorkspaceHooks#beforeStart(UsersWorkspace, String, String)} throws this exception
     * @throws ServerException
     *         when any other error occurs during workspace start
     * @see WorkspaceHooks#beforeStart(UsersWorkspace, String, String)
     * @see WorkspaceHooks#beforeCreate(UsersWorkspace, String)
     * @see RuntimeWorkspaceRegistry#start(UsersWorkspace, String)
     */
    public RuntimeWorkspaceImpl startTemporaryWorkspace(WorkspaceConfig workspaceConfig, @Nullable String accountId) throws ServerException,
                                                                                                                            BadRequestException,
                                                                                                                            ForbiddenException,
                                                                                                                            NotFoundException,
                                                                                                                            ConflictException {
        final UsersWorkspaceImpl workspace = fromConfig(workspaceConfig, getCurrentUserId());
        workspace.setTemporary(true);
        // Temporary workspace is not persistent one, which means
        // that it is created when runtime workspace instance created(workspace started)
        hooks.beforeCreate(workspace, accountId);
        final RuntimeWorkspaceImpl runtime = performSyncStart(workspace, workspace.getConfig().getDefaultEnv(), false, accountId);
        hooks.afterCreate(runtime, accountId);

        // TODO move 'analytics' logs to the appropriate interceptors
        LOG.info("EVENT#workspace-created# WS#{}# WS-ID#{}# USER#{}# TEMP#true#",
                 runtime.getConfig().getName(),
                 runtime.getId(),
                 getCurrentUserId());
        return runtime;
    }

    /**
     * Asynchronously recovers the workspace from the snapshot.
     *
     * @param workspaceId
     *         workspace id
     * @param envName
     *         environment name or null if default one should be used
     * @param accountId
     *         account which should be used for this runtime workspace or null when
     *         it should be automatically detected
     * @return starting workspace instance
     * @throws BadRequestException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace with such id doesn't exist
     * @throws ServerException
     *         when any server error occurs
     * @throws ConflictException
     *         when workspace with such id is not stopped
     * @throws ForbiddenException
     *         when user doesn't have access to start the new workspace
     */
    public UsersWorkspaceImpl recoverWorkspace(String workspaceId,
                                               @Nullable String envName,
                                               @Nullable String accountId) throws BadRequestException,
                                                                                  NotFoundException,
                                                                                  ServerException,
                                                                                  ConflictException,
                                                                                  ForbiddenException {
        requiredNotNull(workspaceId, "Required non-null workspace id");
        return performAsyncStart(workspaceDao.get(workspaceId), envName, true, accountId);
    }

    /**
     * Asynchronously stops the workspace.
     *
     * @param workspaceId
     *         the id of the workspace to stop
     * @throws ServerException
     *         when any server error occurs
     * @throws BadRequestException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace {@code workspaceId} doesn't have runtime
     */
    public void stopWorkspace(String workspaceId) throws ServerException, BadRequestException, NotFoundException {
        requiredNotNull(workspaceId, "Required non-null workspace id");
        performAsyncStop(workspaceRegistry.get(workspaceId));
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
     * @throws BadRequestException
     *         when workspace id is null
     * @throws NotFoundException
     *         when runtime workspace with given id does not exist
     * @throws ServerException
     *         when any other error occurs
     */
    public void createSnapshot(String workspaceId) throws BadRequestException, NotFoundException, ServerException {
        requiredNotNull(workspaceId, "Required non-null workspace id");

        final RuntimeWorkspaceImpl workspace = workspaceRegistry.get(workspaceId);
        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            String devMachineSnapshotFailMessage = null;
            for (MachineImpl machine : workspace.getMachines()) {
                try {
                    machineManager.saveSync(machine.getId(), workspace.getOwner(), workspace.getActiveEnv());
                } catch (ApiException apiEx) {
                    if (machine.getConfig().isDev()) {
                        devMachineSnapshotFailMessage = apiEx.getLocalizedMessage();
                    }
                    LOG.error(apiEx.getLocalizedMessage(), apiEx);
                }
            }
            if (devMachineSnapshotFailMessage != null) {
                eventService.publish(newDto(WorkspaceStatusEvent.class).withEventType(SNAPSHOT_CREATION_ERROR)
                                                                       .withWorkspaceId(workspaceId)
                                                                       .withError(devMachineSnapshotFailMessage));
            } else {
                eventService.publish(newDto(WorkspaceStatusEvent.class).withEventType(SNAPSHOT_CREATED).withWorkspaceId(workspaceId));
            }
        }));
    }

    /**
     * Returns list of machine snapshots which are related to workspace with given id.
     *
     * @param workspaceId
     *         workspace id to get snapshot
     * @return list of machine snapshots related to given workspace
     * @throws BadRequestException
     *         when {@code workspaceId} is null
     * @throws NotFoundException
     *         when workspace with given id doesn't exists
     * @throws ServerException
     *         when any other error occurs
     */
    public List<SnapshotImpl> getSnapshot(String workspaceId) throws ServerException, BadRequestException, NotFoundException {
        requiredNotNull(workspaceId, "Required non-null workspace id");
        // check if workspace exists
        workspaceDao.get(workspaceId);
        return machineManager.getSnapshots(getCurrentUserId(), workspaceId);
    }

    private UsersWorkspaceImpl normalizeState(UsersWorkspaceImpl workspace) {
        try {
            return normalizeState(workspace, workspaceRegistry.get(workspace.getId()));
        } catch (NotFoundException e) {
            return normalizeState(workspace, null);
        }
    }

    private UsersWorkspaceImpl normalizeState(UsersWorkspaceImpl workspace, RuntimeWorkspaceImpl runtime) {
        workspace.setTemporary(false);
        workspace.setStatus(runtime == null ? STOPPED : runtime.getStatus());
        return workspace;
    }

    private UsersWorkspaceImpl fromConfig(WorkspaceConfig config, String owner) throws BadRequestException,
                                                                                       ForbiddenException,
                                                                                       ServerException {
        requiredNotNull(config, "Required non-null workspace configuration");
        requiredNotNull(owner, "Required non-null workspace owner");
        configValidator.validate(config);
        return UsersWorkspaceImpl.builder()
                                 .generateId()
                                 .fromConfig(config)
                                 .setOwner(owner)
                                 .build();
    }

    /**
     * Asynchronously starts permanent(non-temporary) workspace.
     */
    @VisibleForTesting
    UsersWorkspaceImpl performAsyncStart(UsersWorkspaceImpl workspace,
                                         String envName,
                                         boolean recover,
                                         @Nullable String accountId) throws ConflictException {
        // Runtime workspace registry performs this check as well
        // but this check needed here because permanent workspace start performed asynchronously
        // which means that even if registry won't start workspace client receives workspace object
        // with starting status, this check prevents it and throws appropriate exception
        try {
            final RuntimeWorkspaceImpl runtime = workspaceRegistry.get(workspace.getId());
            throw new ConflictException(format("Could not start workspace '%s' because its status is '%s'",
                                               runtime.getConfig().getName(),
                                               runtime.getStatus()));
        } catch (NotFoundException ignored) {
            // it is okay if workspace does not exist
        }
        workspace.setTemporary(false);
        workspace.setStatus(WorkspaceStatus.STARTING);
        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            try {
                performSyncStart(workspace, firstNonNull(envName, workspace.getConfig().getDefaultEnv()), recover, accountId);
            } catch (BadRequestException | ServerException | NotFoundException | ConflictException | ForbiddenException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }));
        return workspace;
    }

    /**
     * Synchronously starts the given workspace in the given environment, doesn't perform any
     * checks or validations. Publishes {@link EventType#STARTING} event before start is performed,
     * {@link EventType#RUNNING} after workspace was successfully started and {@link EventType#ERROR}
     * if any error occurs during workspace start.
     */
    @VisibleForTesting
    RuntimeWorkspaceImpl performSyncStart(UsersWorkspaceImpl workspace,
                                          String envName,
                                          boolean recover,
                                          @Nullable String accountId) throws ForbiddenException,
                                                                             BadRequestException,
                                                                             ConflictException,
                                                                             NotFoundException,
                                                                             ServerException {
        hooks.beforeStart(workspace, envName, accountId);
        eventService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                       .withEventType(STARTING)
                                       .withWorkspaceId(workspace.getId()));
        final RuntimeWorkspaceImpl runtime;
        try {
            runtime = workspaceRegistry.start(workspace, envName, recover);
        } catch (ServerException | ConflictException | BadRequestException | NotFoundException ex) {
            eventService.publish(newDto(WorkspaceStatusEvent.class).withEventType(ERROR)
                                                                   .withWorkspaceId(workspace.getId())
                                                                   .withError(ex.getLocalizedMessage()));
            throw ex;
        }
        eventService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                       .withEventType(RUNNING)
                                       .withWorkspaceId(runtime.getId()));
        return runtime;
    }

    /**
     * Asynchronously stops the workspace, publishes {@link EventType#STOPPING} event before stop
     * and {@link EventType#STOPPED} after workspace successfully stopped and {@link EventType#ERROR}
     * when error occurs during the workspace stopping.
     */
    @VisibleForTesting
    void performAsyncStop(RuntimeWorkspaceImpl runtime) {
        eventService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                       .withEventType(STOPPING)
                                       .withWorkspaceId(runtime.getId()));
        executor.execute(() -> {
            try {
                workspaceRegistry.stop(runtime.getId());
                if (runtime.isTemporary()) {
                    hooks.afterRemove(runtime.getId());
                }
            } catch (ConflictException | NotFoundException | ServerException ex) {
                eventService.publish(newDto(WorkspaceStatusEvent.class).withEventType(ERROR)
                                                                       .withWorkspaceId(runtime.getId())
                                                                       .withError(ex.getLocalizedMessage()));
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            eventService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                           .withEventType(EventType.STOPPED)
                                           .withWorkspaceId(runtime.getId()));
        });
    }

    private String getCurrentUserId() {
        return EnvironmentContext.getCurrent().getUser().getId();
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param message
     *         used as subject of exception message "{subject} required"
     * @throws org.eclipse.che.api.core.BadRequestException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String message) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(message);
        }
    }

    /**
     * No-operations workspace hooks. Each method does nothing
     */
    private static class NoopWorkspaceHooks implements WorkspaceHooks {
        @Override
        public void beforeStart(UsersWorkspace workspace, String evnName, String accountId) throws NotFoundException, ServerException {}

        @Override
        public void beforeCreate(UsersWorkspace workspace, String accountId) throws NotFoundException, ServerException {}

        @Override
        public void afterCreate(UsersWorkspace workspace, String accountId) throws ServerException {}

        @Override
        public void afterRemove(String workspaceId) {}
    }
}

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
package org.eclipse.che.api.machine.server;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.util.CompositeLineConsumer;
import org.eclipse.che.api.core.util.FileLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.WebsocketLineConsumer;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.event.InstanceStateEvent;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.exception.UnsupportedRecipeException;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.machine.server.wsagent.WsAgentLauncher;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.eclipse.che.api.machine.server.event.InstanceStateEvent.Type.DIE;
import static org.eclipse.che.api.machine.server.event.InstanceStateEvent.Type.OOM;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Facade for Machine level operations.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@Singleton
public class MachineManager {
    private static final Logger  LOG                          = LoggerFactory.getLogger(MachineManager.class);
    /* machine name must contain only {a-zA-Z0-9_-} characters and it's needed for validation machine names */
    private static final Pattern MACHINE_DISPLAY_NAME_PATTERN = Pattern.compile("^/?[a-zA-Z0-9_-]+$");

    private final SnapshotDao              snapshotDao;
    private final File                     machineLogsDir;
    private final MachineInstanceProviders machineInstanceProviders;
    private final ExecutorService          executor;
    private final MachineRegistry          machineRegistry;
    private final EventService             eventService;
    private final int                      defaultMachineMemorySizeMB;
    private final MachineCleaner           machineCleaner;
    private final WsAgentLauncher          wsAgentLauncher;
    private final RecipeDownloader         recipeDownloader;

    @Inject
    public MachineManager(SnapshotDao snapshotDao,
                          MachineRegistry machineRegistry,
                          MachineInstanceProviders machineInstanceProviders,
                          @Named("machine.logs.location") String machineLogsDir,
                          EventService eventService,
                          @Named("machine.default_mem_size_mb") int defaultMachineMemorySizeMB,
                          WsAgentLauncher wsAgentLauncher,
                          RecipeDownloader recipeDownloader) {
        this.snapshotDao = snapshotDao;
        this.machineInstanceProviders = machineInstanceProviders;
        this.eventService = eventService;
        this.wsAgentLauncher = wsAgentLauncher;
        this.recipeDownloader = recipeDownloader;
        this.machineLogsDir = new File(machineLogsDir);
        this.machineRegistry = machineRegistry;
        this.defaultMachineMemorySizeMB = defaultMachineMemorySizeMB;

        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MachineManager-%d")
                                                                           .setDaemon(false)
                                                                           .build());
        this.machineCleaner = new MachineCleaner();
    }

    /**
     * Synchronously creates and starts machine from scratch.
     *
     * @param machineConfig
     *         configuration that contains all information needed for machine creation
     * @param workspaceId
     *         id of the workspace the created machine will belong to
     * @param environmentName
     *         environment name the created machine will belongs to
     * @return new machine
     * @throws NotFoundException
     *         if machine type from recipe is unsupported
     * @throws NotFoundException
     *         if snapshot not found
     * @throws NotFoundException
     *         if no instance provider implementation found for provided machine type
     * @throws SnapshotException
     *         if error occurs on retrieving snapshot information
     * @throws ConflictException
     *         if machine with given name already exists
     * @throws BadRequestException
     *         if machine display name is invalid
     * @throws MachineException
     *         if any other exception occurs during starting
     */
    public MachineImpl createMachineSync(MachineConfig machineConfig,
                                         final String workspaceId,
                                         final String environmentName)
            throws NotFoundException,
                   SnapshotException,
                   ConflictException,
                   MachineException,
                   BadRequestException {
        LOG.info("Creating machine [ws = {}: env = {}: machine = {}]",
                 workspaceId,
                 environmentName,
                 machineConfig.getName());
        final MachineImpl machine = createMachine(normalizeMachineConfig(machineConfig),
                                                  workspaceId,
                                                  environmentName,
                                                  this::createInstance,
                                                  null);
        LOG.info("Machine [ws = {}: env = {}: machine = {}] was successfully created, its id is '{}'",
                 workspaceId,
                 environmentName,
                 machine.getConfig().getName(),
                 machine.getId());

        return machineRegistry.getMachine(machine.getId());
    }

    /**
     * Synchronously recovers machine from snapshot.
     *
     * @param machineConfig
     *         machine meta information which is needed for {@link Machine machine} instance creation
     * @param workspaceId
     *         workspace id
     * @param envName
     *         name of environment
     * @return machine instance
     * @throws NotFoundException
     *         when snapshot doesn't exist
     * @throws SnapshotException
     *         when any error occurs during snapshot fetching
     * @throws MachineException
     *         when any error occurs during machine start
     * @throws ConflictException
     *         when any conflict occurs during machine creation (e.g Machine with given name already registered for certain workspace)
     * @throws BadRequestException
     *         when either machineConfig or workspace id, or environment name is not valid
     */
    public MachineImpl recoverMachine(MachineConfig machineConfig, String workspaceId, String envName) throws NotFoundException,
                                                                                                              SnapshotException,
                                                                                                              MachineException,
                                                                                                              ConflictException,
                                                                                                              BadRequestException {
        final SnapshotImpl snapshot = snapshotDao.getSnapshot(workspaceId, envName, machineConfig.getName());

        LOG.info("Recovering machine [ws = {}: env = {}: machine = {}] from snapshot", workspaceId, envName, machineConfig.getName());
        final MachineImpl machine = createMachine(normalizeMachineConfig(machineConfig),
                                                  workspaceId,
                                                  envName,
                                                  this::createInstance,
                                                  snapshot);
        LOG.info("Machine [ws = {}: env = {}: machine = {}] was successfully recovered, its id '{}'",
                 workspaceId,
                 envName,
                 machine.getConfig().getName(),
                 machine.getId());

        return machineRegistry.getMachine(machine.getId());
    }

    /**
     * Asynchronously creates and starts machine from scratch.
     *
     * @param machineConfig
     *         configuration that contains all information needed for machine creation
     * @param workspaceId
     *         id of the workspace the created machine will belong to
     * @param environmentName
     *         environment name the created machine will belongs to
     * @return new machine
     * @throws NotFoundException
     *         if machine type from recipe is unsupported
     * @throws NotFoundException
     *         if snapshot not found
     * @throws NotFoundException
     *         if no instance provider implementation found for provided machine type
     * @throws SnapshotException
     *         if error occurs on retrieving snapshot information
     * @throws ConflictException
     *         if machine with given name already exists
     * @throws BadRequestException
     *         if machine display name is invalid
     * @throws MachineException
     *         if any other exception occurs during starting
     */
    public MachineImpl createMachineAsync(MachineConfig machineConfig,
                                          final String workspaceId,
                                          final String environmentName)
            throws NotFoundException,
                   SnapshotException,
                   ConflictException,
                   MachineException,
                   BadRequestException {
        return createMachine(normalizeMachineConfig(machineConfig),
                             workspaceId,
                             environmentName,
                             (instanceProvider, recipe, instanceKey, machine, machineLogger) ->
                                     executor.execute(ThreadLocalPropagateContext.wrap(() -> {
                                         try {
                                             createInstance(instanceProvider,
                                                            recipe,
                                                            instanceKey,
                                                            machine,
                                                            machineLogger);
                                         } catch (MachineException | NotFoundException e) {
                                             LOG.error(e.getLocalizedMessage(), e);
                                             // todo what should we do in that case?
                                         }
                                     })),
                             null);
    }

    private MachineImpl createMachine(MachineConfigImpl machineConfig,
                                      String workspaceId,
                                      String environmentName,
                                      MachineInstanceCreator instanceCreator,
                                      SnapshotImpl snapshot)
            throws NotFoundException,
                   SnapshotException,
                   ConflictException,
                   BadRequestException,
                   MachineException {
        final InstanceProvider instanceProvider = machineInstanceProviders.getProvider(machineConfig.getType());

        // Backward compatibility for source type 'Recipe'.
        // Only 'dockerfile' impl of source type existed when 'Recipe' was valid source type.
        // Changed in 4.2.0-RC1
        // todo remove that several versions later
        if ("recipe".equalsIgnoreCase(machineConfig.getSource().getType())) {
            machineConfig.getSource().setType("dockerfile");
        }
        if (!instanceProvider.getRecipeTypes().contains(machineConfig.getSource().getType().toLowerCase())) {
            throw new UnsupportedRecipeException(format("Recipe type %s of %s machine is unsupported",
                                                        machineConfig.getSource().getType(),
                                                        machineConfig.getName()));
        }

        Recipe recipe = null;
        InstanceKey instanceKey = null;
        if (snapshot != null) {
            instanceKey = snapshot.getInstanceKey();
        } else {
            recipe = recipeDownloader.getRecipe(machineConfig);
        }

        if (!MACHINE_DISPLAY_NAME_PATTERN.matcher(machineConfig.getName()).matches()) {
            throw new BadRequestException("Invalid machine name " + machineConfig.getName());
        }

        for (MachineImpl machine : machineRegistry.getMachines()) {
            if (machine.getWorkspaceId().equals(workspaceId) && machine.getConfig().getName().equals(machineConfig.getName())) {
                throw new ConflictException("Machine with name " + machineConfig.getName() + " already exists");
            }
        }

        final String machineId = generateMachineId();
        final String creator = EnvironmentContext.getCurrent().getSubject().getUserId();

        if (machineConfig.getLimits().getRam() == 0) {
            machineConfig.setLimits(new LimitsImpl(defaultMachineMemorySizeMB));
        }

        final MachineImpl machine = new MachineImpl(machineConfig,
                                                    machineId,
                                                    workspaceId,
                                                    environmentName,
                                                    creator,
                                                    MachineStatus.CREATING,
                                                    null);

        createMachineLogsDir(machineId);
        final LineConsumer machineLogger = getMachineLogger(machineId, getMachineChannels(machine.getConfig().getName(),
                                                                                          machine.getWorkspaceId(),
                                                                                          machine.getEnvName())
                .getOutput());

        try {
            machineRegistry.addMachine(machine);

            instanceCreator.createInstance(instanceProvider, recipe, instanceKey, machine, machineLogger);

            return machine;
        } catch (ConflictException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }

    private void createInstance(InstanceProvider instanceProvider,
                                Recipe recipe,
                                InstanceKey instanceKey,
                                Machine machine,
                                LineConsumer machineLogger) throws MachineException, NotFoundException {
        Instance instance = null;
        try {
            eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                           .withEventType(MachineStatusEvent.EventType.CREATING)
                                           .withMachineId(machine.getId())
                                           .withDev(machine.getConfig().isDev())
                                           .withWorkspaceId(machine.getWorkspaceId())
                                           .withMachineName(machine.getConfig().getName()));

            if (instanceKey == null) {
                instance = instanceProvider.createInstance(recipe, machine, machineLogger);
            } else {
                instance = instanceProvider.createInstance(instanceKey, machine, machineLogger);
            }

            instance.setStatus(MachineStatus.RUNNING);

            machineRegistry.update(instance);

            if (machine.getConfig().isDev()) {
                wsAgentLauncher.startWsAgent(machine.getWorkspaceId());
            }

            eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                           .withEventType(MachineStatusEvent.EventType.RUNNING)
                                           .withDev(machine.getConfig().isDev())
                                           .withMachineId(machine.getId())
                                           .withWorkspaceId(machine.getWorkspaceId())
                                           .withMachineName(machine.getConfig().getName()));

        } catch (ServerException | InterruptedException e) {
            if (instance != null) {
                instance.destroy();
            }

            eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                           .withEventType(MachineStatusEvent.EventType.ERROR)
                                           .withMachineId(machine.getId())
                                           .withDev(machine.getConfig().isDev())
                                           .withWorkspaceId(machine.getWorkspaceId())
                                           .withMachineName(machine.getConfig().getName())
                                           .withError(e.getLocalizedMessage()));

            try {
                machineRegistry.remove(machine.getId());
                machineLogger.writeLine(String.format("[ERROR] %s", e.getLocalizedMessage()));
                machineLogger.close();
            } catch (IOException | NotFoundException e1) {
                LOG.error(e1.getLocalizedMessage());
            }
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }

    private interface MachineInstanceCreator {
        void createInstance(InstanceProvider instanceProvider,
                            Recipe recipe, InstanceKey instanceKey,
                            Machine machineState,
                            LineConsumer machineLogger) throws MachineException, NotFoundException;
    }

    /**
     * Get machine by id
     *
     * @param machineId
     *         id of required machine
     * @return machine with specified id
     * @throws NotFoundException
     *         if machine with specified if not found
     */
    public MachineImpl getMachine(String machineId) throws NotFoundException, MachineException {
        return machineRegistry.getMachine(machineId);
    }

    /**
     * Get machine instance by id
     *
     * @param machineId
     *         id of required machine
     * @return machine with specified id
     * @throws NotFoundException
     *         if machine with specified if not found
     */
    public Instance getInstance(String machineId) throws NotFoundException, MachineException {
        return machineRegistry.getInstance(machineId);
    }

    /**
     * Find machines connected with specific workspace
     *
     * @param owner
     *         id of owner of machine
     * @param workspaceId
     *         workspace binding
     * @return list of machines or empty list
     */
    public List<MachineImpl> getMachines(String owner, String workspaceId) throws MachineException, BadRequestException {
        requiredNotNull(owner, "Owner");

        return machineRegistry.getMachines()
                              .stream()
                              .filter(machine -> owner.equals(machine.getOwner())
                                                 && machine.getWorkspaceId().equals(workspaceId))
                              .collect(Collectors.toList());
    }

    /**
     * Returns all active machines
     */
    public List<MachineImpl> getMachines() throws MachineException {
        return new ArrayList<>(machineRegistry.getMachines());
    }

    /**
     * Returns {@link MachineImpl} of dev machine of workspace
     */
    public MachineImpl getDevMachine(String workspaceId) throws NotFoundException, MachineException {
        return machineRegistry.getDevMachine(workspaceId);
    }

    /**
     * Asynchronously saves machine to snapshot.
     *
     * @param machineId
     *         id of machine for saving
     * @param namespace
     *         owner for new snapshot
     * @param description
     *         optional description that should help to understand purpose of new snapshot in future
     * @return {@link SnapshotImpl} that will be stored in background
     * @throws NotFoundException
     *         if machine with specified id doesn't exist
     * @throws MachineException
     *         if other error occur
     */
    public SnapshotImpl save(String machineId, String namespace, String description)
            throws NotFoundException, MachineException {
        final Instance machine = getInstance(machineId);
        final SnapshotImpl snapshot = SnapshotImpl.builder()
                                                  .generateId()
                                                  .setType(machine.getConfig().getType())
                                                  .setNamespace(namespace)
                                                  .setWorkspaceId(machine.getWorkspaceId())
                                                  .setDescription(description)
                                                  .setDev(machine.getConfig().isDev())
                                                  .setEnvName(machine.getEnvName())
                                                  .setMachineName(machine.getConfig().getName())
                                                  .useCurrentCreationDate()
                                                  .build();
        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            try {
                doSaveMachine(snapshot, machine);
            } catch (Exception ignored) {
                // exception is already logged in #doSaveMachine
            }
        }));
        return snapshot;
    }

    /**
     * Synchronously saves machine to snapshot.
     *
     * @param machineId
     *         id of machine for saving
     * @param namespace
     *         snapshot namespace (e.g. owner)
     * @param description
     *         optional description that should help to understand purpose of new snapshot in future
     * @return {@link SnapshotImpl} that will be stored in background
     * @throws NotFoundException
     *         if machine with specified id doesn't exist
     * @throws SnapshotException
     *         when any error occurs during snapshot storing
     * @throws MachineException
     *         if other error occur
     */
    public SnapshotImpl saveSync(String machineId, String namespace, String description) throws MachineException,
                                                                                            SnapshotException,
                                                                                            NotFoundException {
        final Instance machine = getInstance(machineId);
        final SnapshotImpl snapshot = SnapshotImpl.builder()
                                                  .generateId()
                                                  .setType(machine.getConfig().getType())
                                                  .setNamespace(namespace)
                                                  .setWorkspaceId(machine.getWorkspaceId())
                                                  .setDescription(description)
                                                  .setDev(machine.getConfig().isDev())
                                                  .setEnvName(machine.getEnvName())
                                                  .setMachineName(machine.getConfig().getName())
                                                  .useCurrentCreationDate()
                                                  .build();
        return doSaveMachine(snapshot, machine);
    }

    /**
     * Get snapshot by id
     *
     * @param snapshotId
     *         id of required snapshot
     * @return snapshot with specified id
     * @throws NotFoundException
     *         if snapshot with provided id not found
     * @throws SnapshotException
     *         if other error occur
     */
    public SnapshotImpl getSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        return snapshotDao.getSnapshot(snapshotId);
    }

    /**
     * Gets list of Snapshots by project.
     *
     * @param owner
     *         id of owner of machine
     * @param workspaceId
     *         workspace binding
     * @return list of Snapshots
     * @throws SnapshotException
     *         if error occur
     */
    public List<SnapshotImpl> getSnapshots(String owner, String workspaceId) throws SnapshotException {
        return snapshotDao.findSnapshots(owner, workspaceId);
    }

    /**
     * Remove snapshot by id
     *
     * @param snapshotId
     *         id of snapshot to remove
     * @throws NotFoundException
     *         if snapshot with specified id not found
     * @throws SnapshotException
     *         if other error occurs
     */
    public void removeSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        final SnapshotImpl snapshot = getSnapshot(snapshotId);
        final String instanceType = snapshot.getType();
        final InstanceProvider instanceProvider = machineInstanceProviders.getProvider(instanceType);
        instanceProvider.removeInstanceSnapshot(snapshot.getInstanceKey());

        snapshotDao.removeSnapshot(snapshotId);
    }

    /**
     * Removes Snapshots by owner, workspace and project.
     *
     * @param owner
     *         owner of required snapshots
     * @param workspaceId
     *         workspace binding
     * @throws SnapshotException
     *         error occur
     */
    public void removeSnapshots(String owner, String workspaceId) throws SnapshotException {
        for (SnapshotImpl snapshot : snapshotDao.findSnapshots(owner, workspaceId)) {
            try {
                removeSnapshot(snapshot.getId());
            } catch (NotFoundException ignored) {
                // This is not expected since we just get list of snapshots from DAO.
            } catch (SnapshotException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Execute a command in machine
     *
     * @param machineId
     *         id of the machine where command should be executed
     * @param command
     *         command that should be executed in the machine
     * @return {@link org.eclipse.che.api.machine.server.spi.InstanceProcess} that represents started process in machine
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws BadRequestException
     *         if value of required parameter is invalid
     * @throws MachineException
     *         if other error occur
     */
    public InstanceProcess exec(final String machineId, final Command command, @Nullable String outputChannel)
            throws NotFoundException, MachineException, BadRequestException {
        requiredNotNull(machineId, "Machine ID is required");
        requiredNotNull(command, "Command is required");
        requiredNotNull(command.getCommandLine(), "Command line is required");
        requiredNotNull(command.getName(), "Command name is required");
        requiredNotNull(command.getType(), "Command type is required");

        final Instance machine = getInstance(machineId);
        final InstanceProcess instanceProcess = machine.createProcess(command, outputChannel);
        final int pid = instanceProcess.getPid();

        final LineConsumer processLogger = getProcessLogger(machineId, pid, outputChannel);

        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            try {
                eventService.publish(newDto(MachineProcessEvent.class)
                                             .withEventType(MachineProcessEvent.EventType.STARTED)
                                             .withMachineId(machineId)
                                             .withProcessId(pid));

                instanceProcess.start(processLogger);

                eventService.publish(newDto(MachineProcessEvent.class)
                                             .withEventType(MachineProcessEvent.EventType.STOPPED)
                                             .withMachineId(machineId)
                                             .withProcessId(pid));
            } catch (ConflictException | MachineException error) {
                eventService.publish(newDto(MachineProcessEvent.class)
                                             .withEventType(MachineProcessEvent.EventType.ERROR)
                                             .withMachineId(machineId)
                                             .withProcessId(pid)
                                             .withError(error.getLocalizedMessage()));

                try {
                    processLogger.writeLine(String.format("[ERROR] %s", error.getMessage()));
                } catch (IOException ignored) {
                }
            }
        }));
        return instanceProcess;
    }

    /**
     * Get list of active processes from specific machine
     *
     * @param machineId
     *         id of machine to get processes information from
     * @return list of {@link org.eclipse.che.api.machine.server.spi.InstanceProcess}
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws MachineException
     *         if other error occur
     */
    public List<InstanceProcess> getProcesses(String machineId) throws NotFoundException, MachineException {
        return getInstance(machineId).getProcesses();
    }

    /**
     * Stop process in machine
     *
     * @param machineId
     *         if of the machine where process should be stopped
     * @param pid
     *         id of the process that should be stopped in machine
     * @throws NotFoundException
     *         if machine or process with specified id not found
     * @throws ForbiddenException
     *         if process is finished already
     * @throws MachineException
     *         if other error occur
     */
    public void stopProcess(String machineId, int pid) throws NotFoundException, MachineException, ForbiddenException {
        final InstanceProcess process = getInstance(machineId).getProcess(pid);
        if (!process.isAlive()) {
            throw new ForbiddenException("Process finished already");
        }

        process.kill();

        eventService.publish(newDto(MachineProcessEvent.class)
                                     .withEventType(MachineProcessEvent.EventType.STOPPED)
                                     .withMachineId(machineId)
                                     .withProcessId(pid));
    }

    /**
     * Destroy machine with specified id
     *
     * @param machineId
     *         id of machine that should be destroyed
     * @param async
     *         should destroying be asynchronous or not
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws MachineException
     *         if other error occur
     */
    public void destroy(final String machineId, boolean async) throws NotFoundException, MachineException {
        final Instance machine = getInstance(machineId);

        machine.setStatus(MachineStatus.DESTROYING);

        eventService.publish(newDto(MachineStatusEvent.class)
                                     .withEventType(MachineStatusEvent.EventType.DESTROYING)
                                     .withMachineId(machineId)
                                     .withDev(machine.getConfig().isDev())
                                     .withWorkspaceId(machine.getWorkspaceId())
                                     .withMachineName(machine.getConfig().getName()));

        if (async) {
            executor.execute(ThreadLocalPropagateContext.wrap(() -> {
                try {
                    doDestroy(machine);
                } catch (NotFoundException | MachineException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }));
        } else {
            doDestroy(machine);
        }
    }

    /**
     * Gets logs reader from machine by specified id
     *
     * @param machineId
     *         machine id whose process reader will be returned
     * @return reader for logs on specified machine
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws MachineException
     *         if other error occur
     */
    public Reader getMachineLogReader(String machineId) throws NotFoundException, MachineException {
        final File machineLogsFile = getMachineLogsFile(machineId);
        if (machineLogsFile.isFile()) {
            try {
                return Files.newBufferedReader(machineLogsFile.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                throw new MachineException(String.format("Unable read log file for machine '%s'. %s", machineId, e.getMessage()));
            }
        }
        throw new NotFoundException(String.format("Logs for machine '%s' are not available", machineId));
    }

    /**
     * Gets process reader from machine by specified id.
     *
     * @param machineId
     *         machine id whose process reader will be returned
     * @param pid
     *         process id
     * @return reader for specified process on machine
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws MachineException
     *         if other error occur
     */
    public Reader getProcessLogReader(String machineId, int pid) throws NotFoundException, MachineException {
        final File processLogsFile = getProcessLogsFile(machineId, pid);
        if (processLogsFile.isFile()) {
            try {
                return Files.newBufferedReader(processLogsFile.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                throw new MachineException(
                        String.format("Unable read log file for process '%s' of machine '%s'. %s", pid, machineId, e.getMessage()));
            }
        }
        throw new NotFoundException(String.format("Logs for process '%s' of machine '%s' are not available", pid, machineId));
    }

    private SnapshotImpl doSaveMachine(SnapshotImpl snapshot, Instance machine) throws SnapshotException, MachineException {
        final SnapshotImpl snapshotWithKey;
        try {
            LOG.info("Creating snapshot of machine [ws = {}: env = {}: machine name = {}: machine id = {}]",
                     snapshot.getWorkspaceId(),
                     snapshot.getEnvName(),
                     snapshot.getMachineName(),
                     machine.getId());

            snapshotWithKey = new SnapshotImpl(snapshot);
            snapshotWithKey.setInstanceKey(machine.saveToSnapshot(machine.getOwner()));

            try {
                SnapshotImpl oldSnapshot = snapshotDao.getSnapshot(snapshot.getWorkspaceId(),
                                                                   snapshot.getEnvName(),
                                                                   snapshot.getMachineName());
                snapshotDao.removeSnapshot(oldSnapshot.getId());
                machineInstanceProviders.getProvider(oldSnapshot.getType()).removeInstanceSnapshot(oldSnapshot.getInstanceKey());
            } catch (NotFoundException ignored) {
               //DO nothing if we has no snapshots or when provider not found
            } catch (SnapshotException se) {
                LOG.error("Failed to delete snapshot: {}, because {}",
                          snapshot,
                          se.getLocalizedMessage());
            }
            snapshotDao.saveSnapshot(snapshotWithKey);

            LOG.info("Snapshot of machine [ws = {}: env = {}: machine name = {}: machine id = {}] was successfully created, its id is '{}'",
                     snapshot.getWorkspaceId(),
                     snapshot.getEnvName(),
                     snapshot.getMachineName(),
                     machine.getId(),
                     snapshot.getId());
        } catch (MachineException | SnapshotException ex) {
            try {
                machine.getLogger().writeLine("Snapshot storing failed. " + ex.getLocalizedMessage());
            } catch (IOException ignore) {
            }
            LOG.error("Failed to create snapshot of machine [ws = {}: env = {}: machine = {}], because {}",
                      snapshot.getWorkspaceId(),
                      snapshot.getEnvName(),
                      snapshot.getMachineName(),
                      ex.getLocalizedMessage());
            throw ex;
        }
        return snapshotWithKey;
    }

    private void doDestroy(Instance machine) throws MachineException, NotFoundException {
        LOG.info("Destroying machine [ws = {}: env = {}: machine name = {}: machine id = {}]",
                 machine.getWorkspaceId(),
                 machine.getEnvName(),
                 machine.getConfig().getName(),
                 machine.getId());

        try {
            machineRegistry.remove(machine.getId());
        } finally {
            try {
                machine.destroy();

                LOG.info("Machine [ws = {}: env = {}: machine name = {}: machine id = {}] was successfully destroyed",
                         machine.getWorkspaceId(),
                         machine.getEnvName(),
                         machine.getConfig().getName(),
                         machine.getId());
            } catch (MachineException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        eventService.publish(newDto(MachineStatusEvent.class)
                                     .withEventType(MachineStatusEvent.EventType.DESTROYED)
                                     .withDev(machine.getConfig().isDev())
                                     .withMachineId(machine.getId())
                                     .withWorkspaceId(machine.getWorkspaceId())
                                     .withMachineName(machine.getConfig().getName()));
    }

    private void createMachineLogsDir(String machineId) throws MachineException {
        File dir = new File(machineLogsDir, machineId);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new MachineException("Can't create folder for the logs of machine");
        }
    }

    private FileLineConsumer getMachineFileLogger(String machineId) throws MachineException {
        try {
            return new FileLineConsumer(getMachineLogsFile(machineId));
        } catch (IOException e) {
            throw new MachineException(String.format("Unable create log file for machine '%s'. %s", machineId, e.getMessage()));
        }
    }

    private File getMachineLogsFile(String machineId) {
        return new File(new File(machineLogsDir, machineId), "machineId.logs");
    }

    private File getProcessLogsFile(String machineId, int pid) {
        return new File(new File(machineLogsDir, machineId), Integer.toString(pid));
    }

    private FileLineConsumer getProcessFileLogger(String machineId, int pid) throws MachineException {
        try {
            return new FileLineConsumer(getProcessLogsFile(machineId, pid));
        } catch (IOException e) {
            throw new MachineException(
                    String.format("Unable create log file for process '%s' of machine '%s'. %s", pid, machineId, e.getMessage()));
        }
    }

    String generateMachineId() {
        return NameGenerator.generate("machine", 16);
    }

    private LineConsumer getMachineLogger(String machineId, String outputChannel) throws MachineException {
        return getLogger(getMachineFileLogger(machineId), outputChannel);
    }

    private LineConsumer getProcessLogger(String machineId, int pid, String outputChannel) throws MachineException {
        return getLogger(getProcessFileLogger(machineId, pid), outputChannel);
    }

    private LineConsumer getLogger(LineConsumer fileLogger, String outputChannel) throws MachineException {
        if (outputChannel != null) {
            return new CompositeLineConsumer(fileLogger, new WebsocketLineConsumer(outputChannel));
        }
        return fileLogger;
    }

    static ChannelsImpl getMachineChannels(String machineName, String workspaceId, String envName) {
        return new ChannelsImpl(workspaceId + ':' + envName + ':' + machineName,
                                "machine:status:" + workspaceId + ':' + machineName);
    }

    // cleanup machine if event about instance failure comes
    private class MachineCleaner implements EventSubscriber<InstanceStateEvent> {
        @Override
        public void onEvent(InstanceStateEvent event) {
            if ((event.getType() == OOM) || (event.getType() == DIE)) {
                try {
                    final Instance machine = getInstance(event.getMachineId());

                    machineRegistry.remove(machine.getId());

                    String message = "Machine is destroyed. ";
                    if (event.getType() == OOM) {
                        message = message +
                                  "The processes in this machine need more RAM. This machine started with " +
                                  machine.getConfig().getLimits().getRam() +
                                  "MB. Create a new machine configuration that allocates additional RAM or increase " +
                                  "the workspace RAM limit in the user dashboard.";
                    }

                    try {
                        if (!Strings.isNullOrEmpty(message)) {
                            machine.getLogger().writeLine(message);
                        }
                        machine.getLogger().close();
                    } catch (IOException ignore) {
                    }

                    eventService.publish(newDto(MachineStatusEvent.class)
                                                 .withEventType(MachineStatusEvent.EventType.DESTROYED)
                                                 .withDev(machine.getConfig().isDev())
                                                 .withMachineId(machine.getId())
                                                 .withWorkspaceId(machine.getWorkspaceId())
                                                 .withMachineName(machine.getConfig().getName()));
                } catch (NotFoundException | MachineException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
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
            throw new BadRequestException(message + " required");
        }
    }

    @SuppressWarnings("unused")
    @PostConstruct
    private void createLogsDir() {
        eventService.subscribe(machineCleaner);

        if (!(machineLogsDir.exists() || machineLogsDir.mkdirs())) {
            throw new IllegalStateException(String.format("Unable create directory %s", machineLogsDir.getAbsolutePath()));
        }
    }

    @PreDestroy
    private void cleanup() {
        eventService.unsubscribe(machineCleaner);

        boolean interrupted = false;

        executor.shutdown();

        final ExecutorService destroyMachinesExecutor =
                Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors(),
                                             new ThreadFactoryBuilder().setNameFormat("DestroyMachine-%d")
                                                                       .setDaemon(false)
                                                                       .build());
        try {
            for (MachineImpl machine : machineRegistry.getMachines()) {
                destroyMachinesExecutor.execute(() -> {
                    try {
                        destroy(machine.getId(), false);
                    } catch (NotFoundException ignore) {
                        // it is ok, machine has been already destroyed
                    } catch (Exception e) {
                        LOG.warn(e.getMessage());
                    }
                });
            }
            destroyMachinesExecutor.shutdown();
            if (!destroyMachinesExecutor.awaitTermination(50, TimeUnit.SECONDS)) {
                destroyMachinesExecutor.shutdownNow();
                if (!destroyMachinesExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.warn("Unable terminate destroy machines pool");
                }
            }
        } catch (InterruptedException e) {
            interrupted = true;
            destroyMachinesExecutor.shutdownNow();
        } catch (MachineException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.warn("Unable terminate main pool");
                }
            }
        } catch (InterruptedException e) {
            interrupted = true;
            executor.shutdownNow();
        }

        final java.io.File[] files = machineLogsDir.listFiles();
        if (files != null && files.length > 0) {
            for (java.io.File f : files) {
                if (!IoUtil.deleteRecursive(f)) {
                    LOG.warn("Failed delete {}", f);
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private MachineConfigImpl normalizeMachineConfig(MachineConfig machineConfig) {
        return new MachineConfigImpl(machineConfig);
    }
}

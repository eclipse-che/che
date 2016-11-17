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
package org.eclipse.che.api.environment.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.model.impl.AgentImpl;
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineLogMessage;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ServerConf2;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.MessageConsumer;
import org.eclipse.che.api.core.util.lineconsumer.ConcurrentCompositeLineConsumer;
import org.eclipse.che.api.core.util.lineconsumer.ConcurrentFileLineConsumer;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.exception.EnvironmentNotRunningException;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.server.event.InstanceStateEvent;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SourceNotFoundException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLogMessageImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.server.StripedLocks;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Size;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.machine.server.event.InstanceStateEvent.Type.DIE;
import static org.eclipse.che.api.machine.server.event.InstanceStateEvent.Type.OOM;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Facade for implementation specific operations with environment runtimes.
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@Singleton
public class CheEnvironmentEngine {

    private static final Logger LOG = getLogger(CheEnvironmentEngine.class);

    private final Map<String, EnvironmentHolder> environments;
    private final StripedLocks                   stripedLocks;
    private final File                           machineLogsDir;
    private final MachineInstanceProviders       machineInstanceProviders;
    private final long                           defaultMachineMemorySizeBytes;
    private final SnapshotDao                    snapshotDao;
    private final EventService                   eventService;
    private final EnvironmentParser              environmentParser;
    private final DefaultServicesStartStrategy   startStrategy;
    private final MachineInstanceProvider        machineProvider;
    private final InfrastructureProvisioner      infrastructureProvisioner;
    private final RecipeDownloader               recipeDownloader;
    private final Pattern                        recipeApiPattern;
    private final ContainerNameGenerator         containerNameGenerator;
    private final AgentRegistry                  agentRegistry;

    private volatile boolean isPreDestroyInvoked;

    @Inject
    public CheEnvironmentEngine(SnapshotDao snapshotDao,
                                MachineInstanceProviders machineInstanceProviders,
                                @Named("che.workspace.logs") String machineLogsDir,
                                @Named("che.workspace.default_memory_mb") int defaultMachineMemorySizeMB,
                                EventService eventService,
                                EnvironmentParser environmentParser,
                                DefaultServicesStartStrategy startStrategy,
                                MachineInstanceProvider machineProvider,
                                InfrastructureProvisioner infrastructureProvisioner,
                                @Named("che.api") String apiEndpoint,
                                RecipeDownloader recipeDownloader,
                                ContainerNameGenerator containerNameGenerator,
                                AgentRegistry agentRegistry) {
        this.snapshotDao = snapshotDao;
        this.eventService = eventService;
        this.environmentParser = environmentParser;
        this.startStrategy = startStrategy;
        this.machineProvider = machineProvider;
        this.infrastructureProvisioner = infrastructureProvisioner;
        this.recipeDownloader = recipeDownloader;
        this.agentRegistry = agentRegistry;
        this.environments = new ConcurrentHashMap<>();
        this.machineInstanceProviders = machineInstanceProviders;
        this.machineLogsDir = new File(machineLogsDir);
        this.defaultMachineMemorySizeBytes = Size.parseSize(defaultMachineMemorySizeMB + "MB");
        // 16 - experimental value for stripes count, it comes from default hash map size
        this.stripedLocks = new StripedLocks(16);
        this.recipeApiPattern = Pattern.compile("(^https?" +
                                                apiEndpoint.substring(apiEndpoint.indexOf(":")) +
                                                "/recipe/.*$)|(^/recipe/.*$)");
        this.containerNameGenerator = containerNameGenerator;

        eventService.subscribe(new MachineCleaner());
    }

    /**
     * Returns all machines from environment of specific workspace.
     *
     * @param workspaceId
     *         ID of workspace that owns environment machines
     * @return list of machines
     * @throws EnvironmentNotRunningException
     *         if environment is not running
     */
    public List<Instance> getMachines(String workspaceId) throws EnvironmentNotRunningException {
        EnvironmentHolder environment;
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            environment = environments.get(workspaceId);
            if (environment == null) {
                throw new EnvironmentNotRunningException("Environment with ID '" + workspaceId + "' is not found");
            }
            return new ArrayList<>(environment.machines);
        }
    }

    /**
     * Returns specific machine from environment of specific workspace.
     *
     * @param workspaceId
     *         ID of workspace that owns environment machines
     * @param machineId
     *         ID of requested machine
     * @return requested machine
     * @throws EnvironmentNotRunningException
     *         if environment is not running
     * @throws NotFoundException
     *         if machine is not found in the environment
     */
    public Instance getMachine(String workspaceId, String machineId) throws NotFoundException {
        EnvironmentHolder environment;
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            environment = environments.get(workspaceId);
        }
        if (environment == null) {
            throw new EnvironmentNotRunningException("Environment with ID '" + workspaceId + "' is not found");
        }
        return environment.machines.stream()
                                   .filter(instance -> instance.getId().equals(machineId))
                                   .findAny()
                                   .orElseThrow(() -> new NotFoundException(
                                           format("Machine with ID '%s' is not found in the environment of workspace '%s'",
                                                  machineId, workspaceId)));
    }

    /**
     * Starts provided environment.
     * <p/>
     * Environment starts if and only all machines in environment definition start successfully.<br/>
     * Otherwise exception is thrown by this method.<br/>
     * It is not defined whether environment start fails right after first failure or in the end of the process.<br/>
     * Starting order of machines is not guarantied. Machines can start sequentially or in parallel.
     *
     * @param workspaceId
     *         ID of workspace that owns provided environment
     * @param envName
     *         name of environment
     * @param env
     *         environment to start
     * @param recover
     *         whether machines from environment should be recovered or not
     * @param messageConsumer
     *         consumer of log messages from machines in the environment
     * @return list of running machines of this environment
     * @throws ServerException
     *         if other error occurs
     */
    public List<Instance> start(String workspaceId,
                                String envName,
                                Environment env,
                                boolean recover,
                                MessageConsumer<MachineLogMessage> messageConsumer) throws ServerException,
                                                                                           ConflictException,
                                                                                           EnvironmentException {
        // TODO move to machines provider
        // add random chars to ensure that old environments that weren't removed by some reason won't prevent start
        String networkId = NameGenerator.generate(workspaceId + "_", 16);

        String namespace = EnvironmentContext.getCurrent().getSubject().getUserName();

        initializeEnvironment(namespace,
                              workspaceId,
                              envName,
                              env,
                              networkId,
                              messageConsumer);

        String devMachineName = findDevMachineName(env);

        startEnvironmentQueue(namespace,
                              workspaceId,
                              devMachineName,
                              networkId,
                              recover);

        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
            EnvironmentHolder environmentHolder = environments.get(workspaceId);
            // possible only if environment was stopped during its start
            if (environmentHolder == null) {
                throw new ServerException("Environment start was interrupted by environment stopping");
            }
            environmentHolder.status = EnvStatus.RUNNING;
            // prevent list modification
            return new ArrayList<>(environmentHolder.machines);
        }
    }

    /**
     * Stops running environment of specified workspace.
     *
     * @param workspaceId
     *         ID of workspace that owns environment
     * @throws EnvironmentNotRunningException
     *         when environment is not running
     * @throws ServerException
     *         if other error occurs
     */
    public void stop(String workspaceId) throws EnvironmentNotRunningException,
                                                ServerException {
        List<Instance> machinesCopy = null;
        EnvironmentHolder environmentHolder;
        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
            environmentHolder = environments.get(workspaceId);
            if (environmentHolder == null || environmentHolder.status != EnvStatus.RUNNING) {
                throw new EnvironmentNotRunningException(
                        format("Stop of not running environment of workspace with ID '%s' is not allowed.",
                               workspaceId));
            }
            environments.remove(workspaceId);
            List<Instance> machines = environmentHolder.machines;
            if (machines != null && !machines.isEmpty()) {
                machinesCopy = new ArrayList<>(machines);
            }
        }

        // long operation - perform out of lock
        if (machinesCopy != null) {
            destroyEnvironment(environmentHolder.networkId, machinesCopy);
        }
    }

    /**
     * Starts machine in running environment.
     *
     * @param workspaceId
     *         ID of workspace that owns environment in which machine should be started
     * @param machineConfig
     *         configuration of machine that should be started
     * @return running machine
     * @throws EnvironmentNotRunningException
     *         if environment is not running
     * @throws NotFoundException
     *         if provider of machine implementation is not found
     * @throws ConflictException
     *         if machine with the same name already exists in the environment
     * @throws ServerException
     *         if any other error occurs
     */
    public Instance startMachine(String workspaceId,
                                 MachineConfig machineConfig,
                                 List<String> agents) throws ServerException,
                                                             NotFoundException,
                                                             ConflictException,
                                                             EnvironmentException {

        MachineConfig machineConfigCopy = new MachineConfigImpl(machineConfig);
        EnvironmentHolder environmentHolder;
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            environmentHolder = environments.get(workspaceId);
            if (environmentHolder == null || environmentHolder.status != EnvStatus.RUNNING) {
                throw new EnvironmentNotRunningException(format("Environment '%s' is not running", workspaceId));
            }
            for (Instance machine : environmentHolder.machines) {
                if (machine.getConfig().getName().equals(machineConfigCopy.getName())) {
                    throw new ConflictException(
                            format("Machine with name '%s' already exists in environment of workspace '%s'",
                                   machineConfigCopy.getName(), workspaceId));
                }
            }
        }
        final String creator = EnvironmentContext.getCurrent().getSubject().getUserId();
        final String namespace = EnvironmentContext.getCurrent().getSubject().getUserName();

        MachineImpl machine = MachineImpl.builder()
                                         .setConfig(machineConfig)
                                         .setWorkspaceId(workspaceId)
                                         .setStatus(MachineStatus.CREATING)
                                         .setEnvName(environmentHolder.name)
                                         .setOwner(creator)
                                         .build();

        MachineStarter machineStarter;
        if ("docker".equals(machineConfig.getType())) {
            // needed to reuse startInstance method and
            // create machine instances by different implementation-specific providers
            CheServiceImpl service = machineConfigToService(machineConfig);
            normalize(namespace,
                      workspaceId,
                      machineConfig.getName(),
                      service);
            machine.setId(service.getId());

            machineStarter = (machineLogger, machineSource) -> {
                CheServiceImpl serviceWithNormalizedSource = normalizeServiceSource(service, machineSource);

                normalize(namespace,
                          workspaceId,
                          machineConfig.getName(),
                          serviceWithNormalizedSource);

                infrastructureProvisioner.provision(new ExtendedMachineImpl().withAgents(agents),
                                                    serviceWithNormalizedSource);

                return machineProvider.startService(namespace,
                                                    workspaceId,
                                                    environmentHolder.name,
                                                    machineConfig.getName(),
                                                    machineConfig.isDev(),
                                                    environmentHolder.networkId,
                                                    serviceWithNormalizedSource,
                                                    machineLogger);
            };
        } else {
            try {
                InstanceProvider provider = machineInstanceProviders.getProvider(machineConfig.getType());
                machine.setId(generateMachineId());
                addAgentsProvidedServers(machine, agents);

                machineStarter = (machineLogger, machineSource) -> {
                    Machine machineWithNormalizedSource = normalizeMachineSource(machine, machineSource);
                    return provider.createInstance(machineWithNormalizedSource, machineLogger);
                };
            } catch (NotFoundException e) {
                throw new NotFoundException(format("Provider of machine type '%s' not found", machineConfig.getType()));
            }
        }
        return startInstance(false,
                             environmentHolder.logger,
                             machine,
                             machineStarter);
    }

    /**
     * Stops machine in running environment.
     *
     * @param workspaceId
     *         ID of workspace of environment that owns machine
     * @param machineId
     *         ID of machine that should be stopped
     * @throws NotFoundException
     *         if machine in not found in environment
     * @throws EnvironmentNotRunningException
     *         if environment is not running
     * @throws ConflictException
     *         if stop of dev machine is requested
     * @throws ServerException
     *         if other error occurs
     */
    public void stopMachine(String workspaceId, String machineId) throws NotFoundException,
                                                                         ServerException,
                                                                         ConflictException {
        Instance targetMachine = null;
        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
            EnvironmentHolder environmentHolder = environments.get(workspaceId);
            if (environmentHolder == null || environmentHolder.status != EnvStatus.RUNNING) {
                throw new EnvironmentNotRunningException(format("Environment '%s' is not running", workspaceId));
            }
            for (Instance machine : environmentHolder.machines) {
                if (machine.getId().equals(machineId)) {
                    if (machine.getConfig().isDev()) {
                        throw new ConflictException(
                                "Stop of dev machine is not allowed. Please, stop whole environment");
                    }
                    targetMachine = machine;
                    break;
                }
            }
            environmentHolder.machines.remove(targetMachine);
        }
        if (targetMachine == null) {
            throw new NotFoundException(format("Machine with ID '%s' is not found in environment of workspace '%s'",
                                               machineId, workspaceId));
        }

        // out of lock to prevent blocking by potentially long-running method
        destroyMachine(targetMachine);
    }

    /**
     * Saves machine into snapshot.
     *
     * @param namespace
     *         namespace of the workspace
     * @param workspaceId
     *         ID of workspace that owns environment
     * @param machineId
     *         ID of machine to save
     * @return snapshot
     * @throws EnvironmentNotRunningException
     *         if environment of machine is not running
     * @throws NotFoundException
     *         if machine is not running
     * @throws ServerException
     *         if another error occurs
     */
    public SnapshotImpl saveSnapshot(String namespace,
                                     String workspaceId,
                                     String machineId) throws ServerException,
                                                              NotFoundException {
        EnvironmentHolder environmentHolder;
        SnapshotImpl snapshot = null;
        Instance instance = null;
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            environmentHolder = environments.get(workspaceId);
            if (environmentHolder == null || environmentHolder.status != EnvStatus.RUNNING) {
                throw new EnvironmentNotRunningException(format("Environment '%s' is not running", workspaceId));
            }
            for (Instance machine : environmentHolder.machines) {
                if (machine.getId().equals(machineId)) {
                    instance = machine;
                    snapshot = SnapshotImpl.builder()
                                           .generateId()
                                           .setType(machine.getConfig().getType())
                                           .setWorkspaceId(machine.getWorkspaceId())
                                           .setDescription(machine.getEnvName())
                                           .setDev(machine.getConfig().isDev())
                                           .setEnvName(machine.getEnvName())
                                           .setMachineName(machine.getConfig().getName())
                                           .useCurrentCreationDate()
                                           .build();
                }
            }
        }
        if (instance == null) {
            throw new NotFoundException(format("Machine with id '%s' is not found in environment of workspace '%s'",
                                               machineId, workspaceId));
        }
        try {
            MachineSource machineSource = instance.saveToSnapshot();
            snapshot.setMachineSource(new MachineSourceImpl(machineSource));
            return snapshot;
        } catch (ServerException e) {
            try {
                instance.getLogger().writeLine("Snapshot storing failed. " + e.getLocalizedMessage());
            } catch (IOException ignore) {
            }
            throw e;
        }
    }

    /**
     * Removes snapshot of machine.
     *
     * @param snapshot
     *         description of snapshot that should be removed
     * @throws NotFoundException
     *         if snapshot is not found
     * @throws ServerException
     *         if error occurs on snapshot removal
     */
    public void removeSnapshot(SnapshotImpl snapshot) throws ServerException, NotFoundException {
        final String instanceType = snapshot.getType();
        final InstanceProvider instanceProvider = machineInstanceProviders.getProvider(instanceType);
        instanceProvider.removeInstanceSnapshot(snapshot.getMachineSource());
    }

    private void initializeEnvironment(String namespace,
                                       String workspaceId,
                                       String envName,
                                       Environment envConfig,
                                       String networkId,
                                       MessageConsumer<MachineLogMessage> messageConsumer)
            throws ServerException,
                   ConflictException,
                   EnvironmentException {

        CheServicesEnvironmentImpl internalEnv = environmentParser.parse(envConfig);

        internalEnv.setWorkspaceId(workspaceId);

        infrastructureProvisioner.provision(envConfig, internalEnv);

        normalize(namespace,
                  workspaceId,
                  internalEnv);

        List<String> servicesOrder = startStrategy.order(internalEnv);

        normalizeNames(internalEnv);

        EnvironmentHolder environmentHolder = new EnvironmentHolder(servicesOrder,
                                                                    internalEnv,
                                                                    messageConsumer,
                                                                    EnvStatus.STARTING,
                                                                    envName,
                                                                    networkId);

        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
            if (environments.putIfAbsent(workspaceId, environmentHolder) != null) {
                throw new ConflictException(format("Environment of workspace '%s' already exists", workspaceId));
            }
        }
    }

    private void addAgentsProvidedServers(MachineImpl machine, List<String> agentKeys) throws ServerException {
        for (String agentKey : agentKeys) {
            try {
                AgentImpl agent = new AgentImpl(agentRegistry.getAgent(AgentKeyImpl.parse(agentKey)));
                for (Map.Entry<String, ? extends ServerConf2> entry : agent.getServers().entrySet()) {
                    String ref = entry.getKey();
                    ServerConf2 conf2 = entry.getValue();

                    ServerConfImpl conf = new ServerConfImpl(ref,
                                                             conf2.getPort(),
                                                             conf2.getProtocol(),
                                                             conf2.getProperties().get("path"));
                    machine.getConfig().getServers().add(conf);
                }
            } catch (AgentException e) {
                throw new ServerException(e);
            }
        }
    }

    private void normalize(String namespace,
                           String workspaceId,
                           CheServicesEnvironmentImpl environment) throws ServerException {

        Map<String, CheServiceImpl> services = environment.getServices();
        for (Map.Entry<String, CheServiceImpl> serviceEntry : services.entrySet()) {
            normalize(namespace,
                      workspaceId,
                      serviceEntry.getKey(),
                      serviceEntry.getValue());
        }
    }

    /**
     * Sets specific names for this environment instance where it is required.
     *
     * @param environment
     *         environment in which names will be normalized
     */
    private void normalizeNames(CheServicesEnvironmentImpl environment) {
        Map<String, CheServiceImpl> services = environment.getServices();
        for (Map.Entry<String, CheServiceImpl> serviceEntry : services.entrySet()) {
            CheServiceImpl service = serviceEntry.getValue();
            normalizeVolumesFrom(service, services);
            normalizeLinks(service, services);
        }
    }

    // replace machines names in volumes_from with containers IDs
    private void normalizeVolumesFrom(CheServiceImpl service, Map<String, CheServiceImpl> services) {
        if (service.getVolumesFrom() != null) {
            service.setVolumesFrom(service.getVolumesFrom()
                                          .stream()
                                          .map(serviceName -> services.get(serviceName).getContainerName())
                                          .collect(toList()));
        }
    }

    /**
     * Replaces linked to this service's name with container name which represents the service in links section.
     * The problem is that a user writes names of other services in links section in compose file.
     * But actually links are constraints and their values should be names of containers (not services) to be linked.
     * <br/>
     * For example: serviceDB:serviceDbAlias -> container_1234:serviceDbAlias <br/>
     * If alias is omitted then service name will be used.
     *
     * @param serviceToNormalizeLinks
     *         service which links will be normalized
     * @param services
     *         all services in environment
     */
    @VisibleForTesting
    void normalizeLinks(CheServiceImpl serviceToNormalizeLinks, Map<String, CheServiceImpl> services) {
        serviceToNormalizeLinks.setLinks(
                serviceToNormalizeLinks.getLinks()
                                       .stream()
                                       .map(link -> {
                                           // a link has format: 'name:alias' or 'name'
                                           String serviceNameAndAliasToLink[] = link.split(":", 2);
                                           String serviceName = serviceNameAndAliasToLink[0];
                                           String serviceAlias = (serviceNameAndAliasToLink.length > 1) ?
                                                                 serviceNameAndAliasToLink[1] : null;
                                           CheServiceImpl serviceLinkTo = services.get(serviceName);
                                           if (serviceLinkTo != null) {
                                               String containerNameLinkTo = serviceLinkTo.getContainerName();
                                               return (serviceAlias == null) ?
                                                      containerNameLinkTo :
                                                      containerNameLinkTo + ':' + serviceAlias;
                                           } else {
                                               // should never happens. Errors like this should be filtered by CheEnvironmentValidator
                                               throw new IllegalArgumentException("Attempt to link non existing service " + serviceName +
                                                                                  " to " + serviceToNormalizeLinks + " service.");
                                           }
                                       }).collect(toList()));
    }

    private void normalize(String namespace,
                           String workspaceId,
                           String machineName,
                           CheServiceImpl service) throws ServerException {
        // set default mem limit for service if it is not set
        if (service.getMemLimit() == null || service.getMemLimit() == 0) {
            service.setMemLimit(defaultMachineMemorySizeBytes);
        }
        // download dockerfile if it is hosted by API to avoid problems with unauthorized requests from docker daemon
        if (service.getBuild() != null &&
            service.getBuild().getContext() != null &&
            recipeApiPattern.matcher(service.getBuild().getContext()).matches()) {

            String recipeContent = recipeDownloader.getRecipe(service.getBuild().getContext());
            service.getBuild().setDockerfileContent(recipeContent);
            service.getBuild().setContext(null);
            service.getBuild().setDockerfilePath(null);
        }
        if (service.getId() == null) {
            service.setId(generateMachineId());
        }

        service.setContainerName(containerNameGenerator.generateContainerName(workspaceId,
                                                                              service.getId(),
                                                                              namespace,
                                                                              machineName));
    }

    private String findDevMachineName(Environment env) throws ServerException {
        return env.getMachines()
                  .entrySet()
                  .stream()
                  .filter(entry -> entry.getValue()
                                        .getAgents()
                                        .stream()
                                        .filter(agent -> agent.contains("org.eclipse.che.ws-agent"))
                                        .findAny()
                                        .isPresent())
                  .findAny()
                  .orElseThrow(() -> new ServerException("Agent 'org.eclipse.che.ws-agent' is not found in any of environment machines"))
                  .getKey();
    }

    /**
     * Starts all machine from machine queue of environment.
     */
    private void startEnvironmentQueue(String namespace,
                                       String workspaceId,
                                       String devMachineName,
                                       String networkId,
                                       boolean recover)
            throws ServerException,
                   EnvironmentException {
        // Starting all machines in environment one by one by getting configs
        // from the corresponding starting queue.
        // Config will be null only if there are no machines left in the queue
        String envName;
        MessageConsumer<MachineLogMessage> envLogger;
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            EnvironmentHolder environmentHolder = environments.get(workspaceId);
            if (environmentHolder == null) {
                throw new ServerException("Environment start is interrupted.");
            }
            envName = environmentHolder.name;
            envLogger = environmentHolder.logger;
        }

        try {
            machineProvider.createNetwork(networkId);

            String machineName = queuePeekOrFail(workspaceId);
            while (machineName != null) {
                boolean isDev = devMachineName.equals(machineName);
                // Environment start is failed when any machine start is failed, so if any error
                // occurs during machine creation then environment start fail is reported and
                // start resources such as queue and descriptor must be cleaned up
                String creator = EnvironmentContext.getCurrent().getSubject().getUserId();

                CheServiceImpl service;
                try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
                    EnvironmentHolder environmentHolder = environments.get(workspaceId);
                    if (environmentHolder == null) {
                        throw new ServerException("Environment start is interrupted.");
                    }
                    service = environmentHolder.environment.getServices().get(machineName);
                }
                // should not happen
                if (service == null) {
                    LOG.error("Start of machine with name {} in workspace {} failed. Machine not found in start queue",
                              machineName, workspaceId);
                    throw new ServerException(
                            format("Environment of workspace with ID '%s' failed due to internal error", workspaceId));
                }

                final String finalMachineName = machineName;
                // needed to reuse startInstance method and
                // create machine instances by different implementation-specific providers
                MachineStarter machineStarter = (machineLogger, machineSource) -> {
                    CheServiceImpl serviceWithNormalizedSource = normalizeServiceSource(service, machineSource);
                    return machineProvider.startService(namespace,
                                                        workspaceId,
                                                        envName,
                                                        finalMachineName,
                                                        isDev,
                                                        networkId,
                                                        serviceWithNormalizedSource,
                                                        machineLogger);
                };

                MachineImpl machine =
                        MachineImpl.builder()
                                   .setConfig(MachineConfigImpl.builder()
                                                               .setDev(isDev)
                                                               .setLimits(new MachineLimitsImpl(
                                                                       bytesToMB(service.getMemLimit())))
                                                               .setType("docker")
                                                               .setName(machineName)
                                                               .setEnvVariables(service.getEnvironment())
                                                               .build())
                                   .setId(service.getId())
                                   .setWorkspaceId(workspaceId)
                                   .setStatus(MachineStatus.CREATING)
                                   .setEnvName(envName)
                                   .setOwner(creator)
                                   .build();

                Instance instance = startInstance(recover,
                                                  envLogger,
                                                  machine,
                                                  machineStarter);

                // Machine destroying is an expensive operation which must be
                // performed outside of the lock, this section checks if
                // the environment wasn't stopped while it is starting and sets
                // polled flag to true if the environment wasn't stopped.
                // Also polls the proceeded machine configuration from the queue
                boolean queuePolled = false;
                try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
                    ensurePreDestroyIsNotExecuted();
                    EnvironmentHolder environmentHolder = environments.get(workspaceId);
                    if (environmentHolder != null) {
                        final Queue<String> queue = environmentHolder.startQueue;
                        if (queue != null) {
                            queue.poll();
                            queuePolled = true;
                        }
                    }
                }

                // If machine config is not polled from the queue
                // then environment was stopped and newly created machine
                // must be destroyed
                if (!queuePolled) {
                    try {
                        eventService.publish(newDto(MachineStatusEvent.class)
                                                     .withEventType(MachineStatusEvent.EventType.DESTROYING)
                                                     .withDev(isDev)
                                                     .withMachineName(machineName)
                                                     .withMachineId(instance.getId())
                                                     .withWorkspaceId(workspaceId));

                        instance.destroy();

                        removeMachine(workspaceId, instance.getId());

                        eventService.publish(newDto(MachineStatusEvent.class)
                                                     .withEventType(MachineStatusEvent.EventType.DESTROYED)
                                                     .withDev(isDev)
                                                     .withMachineName(machineName)
                                                     .withMachineId(instance.getId())
                                                     .withWorkspaceId(workspaceId));
                    } catch (MachineException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    throw new ServerException("Workspace '" + workspaceId +
                                              "' start interrupted. Workspace stopped before all its machines started");
                }

                machineName = queuePeekOrFail(workspaceId);
            }
        } catch (RuntimeException | ServerException e) {
            EnvironmentHolder env;
            try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
                env = environments.remove(workspaceId);
            }

            try {
                destroyEnvironment(env.networkId, env.machines);
            } catch (Exception remEx) {
                LOG.error(remEx.getLocalizedMessage(), remEx);
            }
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    private Instance startInstance(boolean recover,
                                   MessageConsumer<MachineLogMessage> environmentLogger,
                                   MachineImpl machine,
                                   MachineStarter machineStarter)
            throws ServerException,
                   EnvironmentException {

        LineConsumer machineLogger = null;
        Instance instance = null;
        try {
            addMachine(machine);

            eventService.publish(newDto(MachineStatusEvent.class)
                                         .withEventType(MachineStatusEvent.EventType.CREATING)
                                         .withDev(machine.getConfig().isDev())
                                         .withMachineName(machine.getConfig().getName())
                                         .withMachineId(machine.getId())
                                         .withWorkspaceId(machine.getWorkspaceId()));

            machineLogger = getMachineLogger(environmentLogger,
                                             machine.getId(),
                                             machine.getConfig().getName());

            MachineImpl originMachine = new MachineImpl(machine);
            try {
                MachineSource machineSource = null;
                if (recover) {
                    SnapshotImpl snapshot = snapshotDao.getSnapshot(machine.getWorkspaceId(),
                                                                    machine.getEnvName(),
                                                                    machine.getConfig().getName());

                    machineSource = snapshot.getMachineSource();
                }

                instance = machineStarter.startMachine(machineLogger, machineSource);
            } catch (SourceNotFoundException e) {
                if (recover) {
                    LOG.error("Image of snapshot for machine " + machine.getConfig().getName() +
                              " not found. " + "Machine will be created from origin source");
                    machine = originMachine;
                    instance = machineStarter.startMachine(machineLogger, null);
                } else {
                    throw e;
                }
            }

            replaceMachine(instance);

            eventService.publish(newDto(MachineStatusEvent.class)
                                         .withEventType(MachineStatusEvent.EventType.RUNNING)
                                         .withDev(machine.getConfig().isDev())
                                         .withMachineName(machine.getConfig().getName())
                                         .withMachineId(instance.getId())
                                         .withWorkspaceId(machine.getWorkspaceId()));

            return instance;
        } catch (ApiException | RuntimeException e) {
            removeMachine(machine.getWorkspaceId(), machine.getId());

            if (instance != null) {
                try {
                    instance.destroy();
                } catch (Exception destroyingExc) {
                    LOG.error(destroyingExc.getLocalizedMessage(), destroyingExc);
                }
            }

            if (machineLogger != null) {
                try {
                    machineLogger.writeLine("[ERROR] " + e.getLocalizedMessage());
                } catch (IOException ioEx) {
                    LOG.error(ioEx.getLocalizedMessage(), ioEx);
                }
                try {
                    machineLogger.close();
                } catch (IOException ioEx) {
                    LOG.error(ioEx.getLocalizedMessage(), ioEx);
                }
            }

            eventService.publish(newDto(MachineStatusEvent.class)
                                         .withEventType(MachineStatusEvent.EventType.ERROR)
                                         .withDev(machine.getConfig().isDev())
                                         .withMachineName(machine.getConfig().getName())
                                         .withMachineId(machine.getId())
                                         .withWorkspaceId(machine.getWorkspaceId()));

            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    private interface MachineStarter {
        Instance startMachine(LineConsumer machineLogger,
                              MachineSource machineSource) throws ServerException,
                                                                  NotFoundException,
                                                                  EnvironmentException;
    }

    private CheServiceImpl normalizeServiceSource(CheServiceImpl service,
                                                  MachineSource machineSource)
            throws ServerException {
        CheServiceImpl serviceWithNormalizedSource = service;
        if (machineSource != null) {
            serviceWithNormalizedSource = new CheServiceImpl(service);
            if ("image".equals(machineSource.getType())) {
                serviceWithNormalizedSource.setBuild(null);
                serviceWithNormalizedSource.setImage(machineSource.getLocation());
            } else {
                // dockerfile
                serviceWithNormalizedSource.setImage(null);
                if (machineSource.getContent() != null) {
                    serviceWithNormalizedSource.setBuild(new CheServiceBuildContextImpl(null,
                                                                                        null,
                                                                                        machineSource.getContent(),
                                                                                        null));
                } else {
                    serviceWithNormalizedSource.setBuild(new CheServiceBuildContextImpl(machineSource.getLocation(),
                                                                                        null,
                                                                                        null,
                                                                                        null));
                }
            }
        }
        return serviceWithNormalizedSource;
    }

    private Machine normalizeMachineSource(MachineImpl machine, MachineSource machineSource) {
        Machine machineWithNormalizedSource = machine;
        if (machineSource != null) {
            machineWithNormalizedSource = MachineImpl.builder()
                                                     .fromMachine(machine)
                                                     .setConfig(MachineConfigImpl.builder()
                                                                                 .fromConfig(machine.getConfig())
                                                                                 .setSource(machineSource)
                                                                                 .build())
                                                     .build();

        }
        return machineWithNormalizedSource;
    }

    private void addMachine(MachineImpl machine) throws ServerException {
        Instance instance = new NoOpMachineInstance(machine);
        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(machine.getWorkspaceId())) {
            ensurePreDestroyIsNotExecuted();
            EnvironmentHolder environmentHolder = environments.get(machine.getWorkspaceId());
            if (environmentHolder != null && environmentHolder.status != EnvStatus.STOPPING) {
                environmentHolder.machines.add(instance);
            } else {
                throw new ServerException(
                        format("Can't add machine into environment. Environment of workspace '%s' is missing",
                               machine.getWorkspaceId()));
            }
        }
    }

    private int bytesToMB(long bytes) {
        return (int)Size.parseSizeToMegabytes(Long.toString(bytes) + "b");
    }

    private void removeMachine(String workspaceId,
                               String machineId) {
        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(workspaceId)) {
            EnvironmentHolder environmentHolder = environments.get(workspaceId);
            if (environmentHolder != null) {
                for (Instance machine : environmentHolder.machines) {
                    if (machine.getId().equals(machineId)) {
                        environmentHolder.machines.remove(machine);
                        return;
                    }
                }
            }
        }
    }

    private void replaceMachine(Instance machine) throws ServerException {
        try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(machine.getWorkspaceId())) {
            ensurePreDestroyIsNotExecuted();
            EnvironmentHolder environmentHolder = environments.get(machine.getWorkspaceId());
            if (environmentHolder != null) {
                for (int i = 0; i < environmentHolder.machines.size(); i++) {
                    if (environmentHolder.machines.get(i).getId().equals(machine.getId())) {
                        environmentHolder.machines.set(i, machine);
                        return;
                    }
                }
            }
        }
        // if this area is reachable then environment/machine is not found and machine should be stopped
        try {
            machine.destroy();
        } catch (MachineException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        // should not happen
        throw new ServerException(format(
                "Machine with ID '%s' and name '%s' has been stopped because its configuration is not found in the environment of workspace '%s'",
                machine.getId(), machine.getConfig().getName(), machine.getWorkspaceId()));
    }

    /**
     * Gets head config from the queue associated with the given {@code workspaceId}.
     *
     * <p>Note that this method won't actually poll the queue.
     *
     * <p>Fails if environment start was interrupted by stop(queue doesn't exist).
     *
     * @return machine config which is in the queue head, or null
     * if there are no machine configs left
     * @throws ServerException
     *         if queue doesn't exist which means that {@link #stop(String)} executed
     *         before all the machines started
     * @throws ServerException
     *         if pre destroy has been invoked before peek config retrieved
     */
    private String queuePeekOrFail(String workspaceId) throws ServerException {
        try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(workspaceId)) {
            ensurePreDestroyIsNotExecuted();
            EnvironmentHolder environmentHolder = environments.get(workspaceId);
            if (environmentHolder == null || environmentHolder.startQueue == null) {
                throw new ServerException("Workspace " + workspaceId +
                                          " start interrupted. Workspace was stopped before all its machines were started");
            }
            return environmentHolder.startQueue.peek();
        }
    }

    /**
     * Destroys provided machines and associated network.
     */
    private void destroyEnvironment(String networkId,
                                    List<Instance> machines) {
        for (Instance machine : machines) {
            try {
                destroyMachine(machine);
            } catch (RuntimeException | MachineException ex) {
                LOG.error(format("Could not destroy machine '%s' of workspace '%s'",
                                 machine.getId(),
                                 machine.getWorkspaceId()),
                          ex);
            }
        }
        try {
            machineProvider.destroyNetwork(networkId);
        } catch (RuntimeException | ServerException netExc) {
            LOG.error(netExc.getLocalizedMessage(), netExc);
        }
    }

    private void destroyMachine(Instance machine) throws MachineException {
        eventService.publish(newDto(MachineStatusEvent.class)
                                     .withEventType(MachineStatusEvent.EventType.DESTROYING)
                                     .withDev(machine.getConfig().isDev())
                                     .withMachineName(machine.getConfig().getName())
                                     .withMachineId(machine.getId())
                                     .withWorkspaceId(machine.getWorkspaceId()));

        machine.destroy();

        eventService.publish(newDto(MachineStatusEvent.class)
                                     .withEventType(MachineStatusEvent.EventType.DESTROYED)
                                     .withDev(machine.getConfig().isDev())
                                     .withMachineName(machine.getConfig().getName())
                                     .withMachineId(machine.getId())
                                     .withWorkspaceId(machine.getWorkspaceId()));
    }

    @SuppressWarnings("unused")
    @PostConstruct
    private void createLogsDir() {
        if (!(machineLogsDir.exists() || machineLogsDir.mkdirs())) {
            throw new IllegalStateException("Unable create directory " + machineLogsDir.getAbsolutePath());
        }
    }

    /**
     * Removes all descriptors from the in-memory storage, while
     * {@link MachineProcessManager#cleanup()} is responsible for machines destroying.
     */
    @PreDestroy
    @VisibleForTesting
    @SuppressWarnings("unused")
    void cleanup() {
        isPreDestroyInvoked = true;
        final java.io.File[] files = machineLogsDir.listFiles();
        if (files != null && files.length > 0) {
            for (java.io.File f : files) {
                if (!IoUtil.deleteRecursive(f)) {
                    LOG.warn("Failed delete {}", f);
                }
            }
        }
    }

    private LineConsumer getMachineLogger(MessageConsumer<MachineLogMessage> environmentLogger,
                                          String machineId,
                                          String machineName) throws ServerException {
        createMachineLogsDir(machineId);

        LineConsumer lineConsumer = new AbstractLineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
                environmentLogger.consume(new MachineLogMessageImpl(machineName, line));
            }
        };
        try {
            return new ConcurrentCompositeLineConsumer(new ConcurrentFileLineConsumer(getMachineLogsFile(machineId)),
                                                       lineConsumer);
        } catch (IOException e) {
            throw new MachineException(format("Unable create log file '%s' for machine '%s'.",
                                              e.getLocalizedMessage(),
                                              machineId));
        }
    }

    private void createMachineLogsDir(String machineId) throws MachineException {
        File dir = new File(machineLogsDir, machineId);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new MachineException("Can't create folder for the logs of machine");
        }
    }

    private File getMachineLogsFile(String machineId) {
        return new File(new File(machineLogsDir, machineId), "machineId.logs");
    }

    @VisibleForTesting
    String generateMachineId() {
        return NameGenerator.generate("machine", 16);
    }

    private void ensurePreDestroyIsNotExecuted() throws ServerException {
        if (isPreDestroyInvoked) {
            throw new ServerException("Could not perform operation because application server is stopping");
        }
    }

    private CheServiceImpl machineConfigToService(MachineConfig machineConfig) throws ServerException {
        CheServiceImpl service = new CheServiceImpl();
        service.setMemLimit(machineConfig.getLimits().getRam() * 1024L * 1024L);
        service.setEnvironment(machineConfig.getEnvVariables());
        if ("image".equals(machineConfig.getSource().getType())) {
            service.setImage(machineConfig.getSource().getLocation());
        } else {
            if (machineConfig.getSource().getContent() != null) {
                throw new ServerException(
                        "Additional machine creation from dockerfile content is not supported anymore. " +
                        "Please use dockerfile location instead");
            } else {
                service.setBuild(new CheServiceBuildContextImpl(machineConfig.getSource().getLocation(),
                                                                null,
                                                                null,
                                                                null));
            }
        }
        List<? extends ServerConf> servers = machineConfig.getServers();
        if (servers != null) {
            List<String> expose = new ArrayList<>();
            for (ServerConf server : servers) {
                expose.add(server.getPort());
            }
            service.setExpose(expose);
        }

        return service;
    }

    private enum EnvStatus {
        STARTING,
        RUNNING,
        STOPPING
    }

    private static class EnvironmentHolder {
        final Queue<String>                      startQueue;
        final CheServicesEnvironmentImpl         environment;
        final MessageConsumer<MachineLogMessage> logger;
        final String                             name;
        final String                             networkId;

        List<Instance> machines;
        EnvStatus      status;

        EnvironmentHolder(List<String> startQueue,
                          CheServicesEnvironmentImpl environment,
                          MessageConsumer<MachineLogMessage> envLogger,
                          EnvStatus envStatus,
                          String name,
                          String networkId) {
            this.startQueue = new ArrayDeque<>(startQueue);
            this.machines = new CopyOnWriteArrayList<>();
            this.logger = envLogger;
            this.status = envStatus;
            this.name = name;
            this.environment = environment;
            this.networkId = networkId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EnvironmentHolder)) return false;
            EnvironmentHolder that = (EnvironmentHolder)o;
            return Objects.equals(startQueue, that.startQueue) &&
                   Objects.equals(machines, that.machines) &&
                   status == that.status &&
                   Objects.equals(logger, that.logger) &&
                   Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startQueue, machines, status, logger, name);
        }
    }

    // cleanup machine if event about instance failure comes
    private class MachineCleaner implements EventSubscriber<InstanceStateEvent> {
        @Override
        public void onEvent(InstanceStateEvent event) {
            if ((event.getType() == OOM) || (event.getType() == DIE)) {
                EnvironmentHolder environmentHolder;
                try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock("workspaceId")) {
                    environmentHolder = environments.get(event.getWorkspaceId());
                }
                if (environmentHolder != null) {
                    for (Instance instance : environmentHolder.machines) {
                        if (instance.getId().equals(event.getMachineId())) {
                            String message = "Machine is destroyed. ";
                            if (event.getType() == OOM) {
                                message = message +
                                          "The processes in this machine need more RAM. This machine started with " +
                                          instance.getConfig().getLimits().getRam() +
                                          "MB. Create a new machine configuration that allocates additional RAM or increase " +
                                          "the workspace RAM limit in the user dashboard.";
                            }

                            try {
                                if (!Strings.isNullOrEmpty(message)) {
                                    instance.getLogger().writeLine(message);
                                }
                            } catch (IOException ignore) {
                            }

                            eventService.publish(newDto(MachineStatusEvent.class)
                                                         .withEventType(MachineStatusEvent.EventType.DESTROYED)
                                                         .withDev(instance.getConfig().isDev())
                                                         .withMachineId(instance.getId())
                                                         .withWorkspaceId(instance.getWorkspaceId())
                                                         .withMachineName(instance.getConfig().getName()));
                        }
                    }
                }
            }
        }
    }
}

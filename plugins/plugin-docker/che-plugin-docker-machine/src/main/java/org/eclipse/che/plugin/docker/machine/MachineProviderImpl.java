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
package org.eclipse.che.plugin.docker.machine;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ObjectArrays;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.environment.server.MachineInstanceProvider;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SourceNotFoundException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.ProgressLineFormatterImpl;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.exception.ContainerNotFoundException;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.exception.NetworkNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.json.container.NetworkingConfig;
import org.eclipse.che.plugin.docker.client.json.network.ConnectContainer;
import org.eclipse.che.plugin.docker.client.json.network.EndpointConfig;
import org.eclipse.che.plugin.docker.client.json.network.NewNetwork;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.GetContainerLogsParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.plugin.docker.client.params.RemoveNetworkParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.network.ConnectContainerToNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.CreateNetworkParams;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.plugin.docker.machine.DockerInstance.LATEST_TAG;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Creates/destroys docker networks and creates docker compose based {@link Instance}.
 *
 * @author Alexander Garagatyi
 */
public class MachineProviderImpl implements MachineInstanceProvider {
    private static final Logger LOG = getLogger(MachineProviderImpl.class);

    /**
     * Prefix of image repository, used to identify that the image is a machine saved to snapshot.
     */
    public static final String MACHINE_SNAPSHOT_PREFIX = "machine_snapshot_";

    public static final Pattern SNAPSHOT_LOCATION_PATTERN = Pattern.compile("(.+/)?" + MACHINE_SNAPSHOT_PREFIX + ".+");

    private final DockerConnector                               docker;
    private final UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
    private final ExecutorService                               executor;
    private final DockerInstanceStopDetector                    dockerInstanceStopDetector;
    private final boolean                                       doForcePullOnBuild;
    private final boolean                                       privilegeMode;
    private final int                                           pidsLimit;
    private final DockerMachineFactory                          dockerMachineFactory;
    private final List<String>                                  devMachinePortsToExpose;
    private final List<String>                                  commonMachinePortsToExpose;
    private final List<String>                                  devMachineSystemVolumes;
    private final List<String>                                  commonMachineSystemVolumes;
    private final Map<String, String>                           devMachineEnvVariables;
    private final Map<String, String>                           commonMachineEnvVariables;
    private final String[]                                      allMachinesExtraHosts;
    private final boolean                                       snapshotUseRegistry;
    private final double                                        memorySwapMultiplier;
    private final String                                        networkDriver;
    private final Set<String>                                   additionalNetworks;
    private final WindowsPathEscaper                            windowsPathEscaper;

    @Inject
    public MachineProviderImpl(DockerConnector docker,
                               DockerConnectorConfiguration dockerConnectorConfiguration,
                               UserSpecificDockerRegistryCredentialsProvider dockerCredentials,
                               DockerMachineFactory dockerMachineFactory,
                               DockerInstanceStopDetector dockerInstanceStopDetector,
                               @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
                               @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers,
                               @Named("machine.docker.dev_machine.machine_volumes") Set<String> devMachineSystemVolumes,
                               @Named("machine.docker.machine_volumes") Set<String> allMachinesSystemVolumes,
                               @Nullable @Named("che.workspace.hosts") String allMachinesExtraHosts,
                               @Named("che.docker.always_pull_image") boolean doForcePullOnBuild,
                               @Named("che.docker.privilege") boolean privilegeMode,
                               @Named("che.docker.pids_limit") int pidsLimit,
                               @Named("machine.docker.dev_machine.machine_env") Set<String> devMachineEnvVariables,
                               @Named("machine.docker.machine_env") Set<String> allMachinesEnvVariables,
                               @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry,
                               @Named("che.docker.swap") double memorySwapMultiplier,
                               @Named("machine.docker.networks") Set<Set<String>> additionalNetworks,
                               @Nullable @Named("che.docker.network_driver") String networkDriver,
                               WindowsPathEscaper windowsPathEscaper)
            throws IOException {
        this.docker = docker;
        this.dockerCredentials = dockerCredentials;
        this.dockerMachineFactory = dockerMachineFactory;
        this.dockerInstanceStopDetector = dockerInstanceStopDetector;
        this.doForcePullOnBuild = doForcePullOnBuild;
        this.privilegeMode = privilegeMode;
        this.snapshotUseRegistry = snapshotUseRegistry;
        // use-cases:
        //  -1  enable unlimited swap
        //  0   disable swap
        //  0.5 enable swap with size equal to half of current memory size
        //  1   enable swap with size equal to current memory size
        //
        //  according to docker docs field  memorySwap should be equal to memory+swap
        //  we calculate this field as memorySwap=memory * (1 + multiplier) so we just add 1 to multiplier
        this.memorySwapMultiplier = memorySwapMultiplier == -1 ? -1 : memorySwapMultiplier + 1;
        this.networkDriver = networkDriver;
        this.windowsPathEscaper = windowsPathEscaper;
        this.pidsLimit = pidsLimit;

        allMachinesSystemVolumes = removeEmptyAndNullValues(allMachinesSystemVolumes);
        devMachineSystemVolumes = removeEmptyAndNullValues(devMachineSystemVolumes);

        allMachinesSystemVolumes = allMachinesSystemVolumes.stream()
                                                           .map(line -> line.split(";"))
                                                           .flatMap(Arrays::stream)
                                                           .distinct()
                                                           .collect(toSet());

        devMachineSystemVolumes = devMachineSystemVolumes.stream()
                                                         .map(line -> line.split(";"))
                                                         .flatMap(Arrays::stream)
                                                         .distinct()
                                                         .collect(toSet());

        if (SystemInfo.isWindows()) {
            allMachinesSystemVolumes = escapePaths(allMachinesSystemVolumes);
            devMachineSystemVolumes = escapePaths(devMachineSystemVolumes);
        }
        this.commonMachineSystemVolumes = new ArrayList<>(allMachinesSystemVolumes);
        List<String> devMachineVolumes = new ArrayList<>(allMachinesSystemVolumes.size()
                                                         + devMachineSystemVolumes.size());
        devMachineVolumes.addAll(allMachinesSystemVolumes);
        devMachineVolumes.addAll(devMachineSystemVolumes);
        this.devMachineSystemVolumes = devMachineVolumes;

        this.devMachinePortsToExpose = new ArrayList<>(allMachinesServers.size() + devMachineServers.size());
        this.commonMachinePortsToExpose = new ArrayList<>(allMachinesServers.size());
        for (ServerConf serverConf : devMachineServers) {
            devMachinePortsToExpose.add(serverConf.getPort());
        }
        for (ServerConf serverConf : allMachinesServers) {
            commonMachinePortsToExpose.add(serverConf.getPort());
            devMachinePortsToExpose.add(serverConf.getPort());
        }

        allMachinesEnvVariables = removeEmptyAndNullValues(allMachinesEnvVariables);
        devMachineEnvVariables = removeEmptyAndNullValues(devMachineEnvVariables);
        this.commonMachineEnvVariables = new HashMap<>();
        this.devMachineEnvVariables = new HashMap<>();
        allMachinesEnvVariables.forEach(envVar -> {
            String[] split = envVar.split("=", 2);
            this.commonMachineEnvVariables.put(split[0], split[1]);
            this.devMachineEnvVariables.put(split[0], split[1]);
        });
        devMachineEnvVariables.forEach(envVar -> {
            String[] split = envVar.split("=", 2);
            this.devMachineEnvVariables.put(split[0], split[1]);
        });

        // always add Che server to hosts list
        String cheHost = dockerConnectorConfiguration.getDockerHostIp();
        String cheHostAlias = DockerInstanceRuntimeInfo.CHE_HOST.concat(":").concat(cheHost);
        if (isNullOrEmpty(allMachinesExtraHosts)) {
            this.allMachinesExtraHosts = new String[] {cheHostAlias};
        } else {
            this.allMachinesExtraHosts = ObjectArrays.concat(allMachinesExtraHosts.split(","), cheHostAlias);
        }

        this.additionalNetworks = additionalNetworks.stream()
                                                    .flatMap(Set::stream)
                                                    .collect(toSet());

        // TODO single point of failure in case of highly loaded system
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MachineLogsStreamer-%d")
                                                                           .setDaemon(true)
                                                                           .build());
    }

    @Override
    public Instance startService(String namespace,
                                 String workspaceId,
                                 String envName,
                                 String machineName,
                                 boolean isDev,
                                 String networkName,
                                 CheServiceImpl service,
                                 LineConsumer machineLogger) throws ServerException {

        // copy to not affect/be affected by changes in origin
        service = new CheServiceImpl(service);

        ProgressLineFormatterImpl progressLineFormatter = new ProgressLineFormatterImpl();
        ProgressMonitor progressMonitor = currentProgressStatus -> {
            try {
                machineLogger.writeLine(progressLineFormatter.format(currentProgressStatus));
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        };

        String container = null;
        try {
            String image = prepareImage(machineName,
                                        service,
                                        progressMonitor);

            container = createContainer(workspaceId,
                                        machineName,
                                        isDev,
                                        image,
                                        networkName,
                                        service);

            connectContainerToAdditionalNetworks(container,
                                                 service);

            docker.startContainer(StartContainerParams.create(container));

            readContainerLogsInSeparateThread(container,
                                              workspaceId,
                                              service.getId(),
                                              machineLogger);

            DockerNode node = dockerMachineFactory.createNode(workspaceId, container);

            dockerInstanceStopDetector.startDetection(container,
                                                      service.getId(),
                                                      workspaceId);

            MachineImpl machine = new MachineImpl(MachineConfigImpl.builder()
                                                                   .setDev(isDev)
                                                                   .setName(machineName)
                                                                   .setType("docker")
                                                                   // casting considered as safe because more than int of megabytes is a lot!
                                                                   .setLimits(new MachineLimitsImpl((int)Size
                                                                           .parseSizeToMegabytes(
                                                                                   service.getMemLimit() + "b")))
                                                                   .setSource(new MachineSourceImpl(service.getBuild() != null ?
                                                                                                    "context" :
                                                                                                    "image")
                                                                                      .setLocation(service.getBuild() != null ?
                                                                                                   service.getBuild().getContext() :
                                                                                                   service.getImage()))
                                                                   .build(),
                                                  service.getId(),
                                                  workspaceId,
                                                  envName,
                                                  namespace,
                                                  MachineStatus.RUNNING,
                                                  null);

            return dockerMachineFactory.createInstance(machine,
                                                       container,
                                                       image,
                                                       node,
                                                       machineLogger);
        } catch (SourceNotFoundException e) {
            throw e;
        } catch (RuntimeException | ServerException | NotFoundException | IOException e) {
            cleanUpContainer(container);
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void createNetwork(String networkName) throws ServerException {
        try {
            docker.createNetwork(CreateNetworkParams.create(new NewNetwork().withName(networkName)
                                                                            .withDriver(networkDriver)
                                                                            .withCheckDuplicate(true)));
        } catch (IOException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void destroyNetwork(String networkName) throws ServerException {
        try {
            docker.removeNetwork(RemoveNetworkParams.create(networkName));
        } catch (NetworkNotFoundException ignore) {
        } catch (IOException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    private String prepareImage(String machineName,
                                CheServiceImpl service,
                                ProgressMonitor progressMonitor)
            throws ServerException,
                   NotFoundException {

        String imageName = "eclipse-che/" + service.getContainerName();
        if ((service.getBuild() == null || (service.getBuild().getContext() == null &&
                                            service.getBuild().getDockerfileContent() == null)) &&
            service.getImage() == null) {

            throw new ServerException(format("Che service '%s' doesn't have neither build nor image fields",
                                             machineName));
        }

        if (service.getBuild() != null && (service.getBuild().getContext() != null ||
                                           service.getBuild().getDockerfileContent() != null)) {
            buildImage(service, imageName, doForcePullOnBuild, progressMonitor);
        } else {
            pullImage(service, imageName, progressMonitor);
        }

        return imageName;
    }

    protected void buildImage(CheServiceImpl service,
                              String machineImageName,
                              boolean doForcePullOnBuild,
                              ProgressMonitor progressMonitor)
            throws MachineException {

        File workDir = null;
        try {
            BuildImageParams buildImageParams;
            if (service.getBuild() != null &&
                service.getBuild().getDockerfileContent() != null) {

                workDir = Files.createTempDirectory(null).toFile();
                final File dockerfileFile = new File(workDir, "Dockerfile");
                try (FileWriter output = new FileWriter(dockerfileFile)) {
                    output.append(service.getBuild().getDockerfileContent());
                }

                buildImageParams = BuildImageParams.create(dockerfileFile);
            } else {
                buildImageParams = BuildImageParams.create(service.getBuild().getContext())
                                                   .withDockerfile(service.getBuild().getDockerfilePath());
            }
            buildImageParams.withForceRemoveIntermediateContainers(true)
                            .withRepository(machineImageName)
                            .withAuthConfigs(dockerCredentials.getCredentials())
                            .withDoForcePull(doForcePullOnBuild)
                            .withMemoryLimit(service.getMemLimit())
                            .withMemorySwapLimit(-1)
                            .withBuildArgs(service.getBuild().getArgs());

            docker.buildImage(buildImageParams, progressMonitor);
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        } finally {
            if (workDir != null) {
                FileCleaner.addFile(workDir);
            }
        }
    }

    /**
     * Pulls docker image for container creation.
     *
     * @param service
     *         service that provides description of image that should be pulled
     * @param machineImageName
     *         name of the image that should be assigned on pull
     * @param progressMonitor
     *         consumer of output
     * @throws SourceNotFoundException
     *         if image for pulling not found
     * @throws MachineException
     *         if any other error occurs
     */
    protected void pullImage(CheServiceImpl service,
                             String machineImageName,
                             ProgressMonitor progressMonitor) throws MachineException {
        DockerMachineSource dockerMachineSource = new DockerMachineSource(
                new MachineSourceImpl("image").setLocation(service.getImage()));
        if (dockerMachineSource.getRepository() == null) {
            throw new MachineException(
                    format("Machine creation failed. Machine source is invalid. No repository is defined. Found '%s'.",
                           dockerMachineSource));
        }

        try {
            boolean isSnapshot = SNAPSHOT_LOCATION_PATTERN.matcher(dockerMachineSource.getLocation()).matches();
            if (!isSnapshot || snapshotUseRegistry) {
                PullParams pullParams = PullParams.create(dockerMachineSource.getRepository())
                                                  .withTag(MoreObjects.firstNonNull(dockerMachineSource.getTag(),
                                                                                    LATEST_TAG))
                                                  .withRegistry(dockerMachineSource.getRegistry())
                                                  .withAuthConfigs(dockerCredentials.getCredentials());
                docker.pull(pullParams, progressMonitor);
            }

            String fullNameOfPulledImage = dockerMachineSource.getLocation(false);
            try {
                // tag image with generated name to allow sysadmin recognize it
                docker.tag(TagParams.create(fullNameOfPulledImage, machineImageName));
            } catch (ImageNotFoundException nfEx) {
                throw new SourceNotFoundException(nfEx.getLocalizedMessage(), nfEx);
            }

            // remove unneeded tag if restoring snapshot from registry
            if (isSnapshot && snapshotUseRegistry) {
                docker.removeImage(RemoveImageParams.create(fullNameOfPulledImage).withForce(false));
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException("Can't create machine from image. Cause: " + e.getLocalizedMessage());
        }
    }

    private String createContainer(String workspaceId,
                                   String machineName,
                                   boolean isDev,
                                   String image,
                                   String networkName,
                                   CheServiceImpl service) throws IOException {

        long machineMemorySwap = memorySwapMultiplier == -1 ?
                                 -1 :
                                 (long)(service.getMemLimit() * memorySwapMultiplier);

        addSystemWideContainerSettings(workspaceId,
                                       isDev,
                                       service);

        EndpointConfig endpointConfig = new EndpointConfig().withAliases(machineName)
                                                            .withLinks(toArrayIfNotNull(service.getLinks()));
        NetworkingConfig networkingConfig = new NetworkingConfig().withEndpointsConfig(singletonMap(networkName,
                                                                                                    endpointConfig));

        HostConfig hostConfig = new HostConfig();
        hostConfig.withBinds(toArrayIfNotNull(service.getVolumes()))
                  .withExtraHosts(allMachinesExtraHosts)
                  .withPublishAllPorts(true)
                  .withMemorySwap(machineMemorySwap)
                  .withMemory(service.getMemLimit())
                  .withPrivileged(privilegeMode)
                  .withPidsLimit(pidsLimit)
                  .withNetworkMode(networkName)
                  .withLinks(toArrayIfNotNull(service.getLinks()))
                  .withPortBindings(service.getPorts()
                                           .stream()
                                           .collect(toMap(Function.identity(),
                                                          value -> new PortBinding[0])))
                  .withVolumesFrom(toArrayIfNotNull(service.getVolumesFrom()));

        ContainerConfig config = new ContainerConfig();
        config.withImage(image)
              .withExposedPorts(service.getExpose()
                                       .stream()
                                       .distinct()
                                       .collect(toMap(Function.identity(), value -> emptyMap())))
              .withHostConfig(hostConfig)
              .withCmd(toArrayIfNotNull(service.getCommand()))
              .withEntrypoint(toArrayIfNotNull(service.getEntrypoint()))
              .withLabels(service.getLabels())
              .withNetworkingConfig(networkingConfig)
              .withEnv(service.getEnvironment()
                              .entrySet()
                              .stream()
                              .map(entry -> entry.getKey() + "=" + entry.getValue())
                              .toArray(String[]::new));

        return docker.createContainer(CreateContainerParams.create(config)
                                                           .withContainerName(service.getContainerName()))
                     .getId();
    }

    private void addSystemWideContainerSettings(String workspaceId,
                                                boolean isDev,
                                                CheServiceImpl composeService) throws IOException {
        List<String> portsToExpose;
        List<String> volumes;
        Map<String, String> env;
        if (isDev) {
            portsToExpose = devMachinePortsToExpose;
            volumes = devMachineSystemVolumes;

            env = new HashMap<>(devMachineEnvVariables);
            env.put(DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID, workspaceId);
            env.put(DockerInstanceRuntimeInfo.USER_TOKEN, getUserToken(workspaceId));
        } else {
            portsToExpose = commonMachinePortsToExpose;
            env = commonMachineEnvVariables;
            volumes = commonMachineSystemVolumes;
        }
        composeService.getExpose().addAll(portsToExpose);
        composeService.getEnvironment().putAll(env);
        composeService.getVolumes().addAll(volumes);
        composeService.getNetworks().addAll(additionalNetworks);
    }

    private void connectContainerToAdditionalNetworks(String container,
                                                      CheServiceImpl service) throws IOException {

        for (String network : service.getNetworks()) {
            docker.connectContainerToNetwork(
                    ConnectContainerToNetworkParams.create(network, new ConnectContainer().withContainer(container)));
        }
    }

    private void readContainerLogsInSeparateThread(String container,
                                                   String workspaceId,
                                                   String machineId,
                                                   LineConsumer outputConsumer) {
        executor.execute(() -> {
            long lastProcessedLogDate = 0;
            boolean isContainerRunning = true;
            int errorsCounter = 0;
            long lastErrorTime = 0;
            while (isContainerRunning) {
                try {
                    docker.getContainerLogs(GetContainerLogsParams.create(container)
                                                                  .withFollow(true)
                                                                  .withSince(lastProcessedLogDate),
                                            new LogMessagePrinter(outputConsumer));
                    isContainerRunning = false;
                } catch (SocketTimeoutException ste) {
                    lastProcessedLogDate = System.currentTimeMillis() / 1000L;
                    // reconnect to container
                } catch (ContainerNotFoundException e) {
                    isContainerRunning = false;
                } catch (IOException e) {
                    long errorTime = System.currentTimeMillis();
                    lastProcessedLogDate = errorTime / 1000L;
                    LOG.warn("Failed to get logs from machine {} of workspace {} backed by container {}, because: {}.",
                             machineId,
                             workspaceId,
                             container,
                             e.getMessage(),
                             e);
                    if (errorTime - lastErrorTime < 20_000L) { // if new error occurs less than 20 seconds after previous
                        if (++errorsCounter == 5) {
                            LOG.error("Too many errors while streaming logs from machine {} of workspace {} backed by container {}. " +
                                      "Logs streaming is closed. Last error: {}.",
                                      machineId,
                                      workspaceId,
                                      container,
                                      e.getMessage(),
                                      e);
                            break;
                        }
                    } else {
                        errorsCounter = 1;
                    }
                    lastErrorTime = errorTime;

                    try {
                        sleep(1_000);
                    } catch (InterruptedException ie) {
                        return;
                    }
                }
            }
        });
    }

    private void cleanUpContainer(String containerId) {
        try {
            if (containerId != null) {
                docker.removeContainer(RemoveContainerParams.create(containerId)
                                                            .withRemoveVolumes(true)
                                                            .withForce(true));
            }
        } catch (Exception ex) {
            LOG.error("Failed to remove docker container {}", containerId, ex);
        }
    }

    // workspaceId parameter is required, because in case of separate storage for tokens
    // you need to know exactly which workspace and which user to apply the token.
    protected String getUserToken(String wsId) {
        return EnvironmentContext.getCurrent().getSubject().getToken();
    }

    /**
     * Returns set that contains all non empty and non nullable values from specified set
     */
    protected Set<String> removeEmptyAndNullValues(Set<String> paths) {
        return paths.stream()
                    .filter(path -> !Strings.isNullOrEmpty(path))
                    .collect(toSet());
    }

    /**
     * Escape paths for Windows system with boot@docker according to rules given here :
     * https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
     *
     * @param paths
     *         set of paths to escape
     * @return set of escaped path
     */
    private Set<String> escapePaths(Set<String> paths) {
        return paths.stream()
                    .map(path -> windowsPathEscaper.escapePathStatic(path))
                    .collect(toSet());
    }

    /** Converts list to array if it is not null, otherwise returns null */
    private String[] toArrayIfNotNull(List<String> list) {
        if (list == null) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }
}

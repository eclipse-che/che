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
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.ProgressLineFormatterImpl;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.exception.ContainerNotFoundException;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.json.Volume;
import org.eclipse.che.plugin.docker.client.json.container.NetworkingConfig;
import org.eclipse.che.plugin.docker.client.json.network.ConnectContainer;
import org.eclipse.che.plugin.docker.client.json.network.EndpointConfig;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.GetContainerLogsParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.network.ConnectContainerToNetworkParams;
import org.eclipse.che.workspace.infrastructure.docker.exception.SourceNotFoundException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;
import org.eclipse.che.workspace.infrastructure.docker.old.DockerMachineSource;
import org.eclipse.che.workspace.infrastructure.docker.strategy.ServerEvaluationStrategyProvider;
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

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.CHE_WORKSPACE_ID;
import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.LATEST_TAG;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Alexander Garagatyi
 */
public class ServiceStarter {
    private static final Logger LOG                     = getLogger(ServiceStarter.class);
    /**
     * Prefix of image repository, used to identify that the image is a machine saved to snapshot.
     */
    public static final  String MACHINE_SNAPSHOT_PREFIX = "machine_snapshot_";

    public static final Pattern SNAPSHOT_LOCATION_PATTERN = Pattern.compile("(.+/)?" + MACHINE_SNAPSHOT_PREFIX + ".+");

    private final DockerConnector                               docker;
    private final UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
    private final ExecutorService                               executor;
    private final DockerInstanceStopDetector                    dockerInstanceStopDetector;
    private final boolean                                       doForcePullOnBuild;
    private final boolean                                       privilegedMode;
    private final int                                           pidsLimit;
//    private final List<String>                                  devMachinePortsToExpose;
//    private final List<String>                                  commonMachinePortsToExpose;
    private final List<String>                                  devMachineSystemVolumes;
    private final List<String>                                  commonMachineSystemVolumes;
    private final Map<String, String>                           devMachineEnvVariables;
    private final Map<String, String>                           commonMachineEnvVariables;
    private final String[]                                      allMachinesExtraHosts;
    private final boolean                                       snapshotUseRegistry;
    private final double                                        memorySwapMultiplier;
    private final Set<String>                                   additionalNetworks;
    private final String                                        parentCgroup;
    private final String                                        cpusetCpus;
    private final long                                          cpuPeriod;
    private final long                                          cpuQuota;
    private final WindowsPathEscaper                            windowsPathEscaper;
    private final String[]                                      dnsResolvers;
    private       ServerEvaluationStrategyProvider              serverEvaluationStrategyProvider;

    @Inject
    public ServiceStarter(DockerConnector docker,
                          UserSpecificDockerRegistryCredentialsProvider dockerCredentials,
                          DockerInstanceStopDetector dockerInstanceStopDetector,
//                          @Named("machine.docker.dev_machine.machine_servers") Set<ServerConfig> devMachineServers,
//                          @Named("machine.docker.machine_servers") Set<ServerConfig> allMachinesServers,
                          @Named("machine.docker.dev_machine.machine_volumes") Set<String> devMachineSystemVolumes,
                          @Named("machine.docker.machine_volumes") Set<String> allMachinesSystemVolumes,
                          @Named("che.docker.always_pull_image") boolean doForcePullOnBuild,
                          @Named("che.docker.privileged") boolean privilegedMode,
                          @Named("che.docker.pids_limit") int pidsLimit,
                          @Named("machine.docker.dev_machine.machine_env") Set<String> devMachineEnvVariables,
                          @Named("machine.docker.machine_env") Set<String> allMachinesEnvVariables,
                          @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry,
                          @Named("che.docker.swap") double memorySwapMultiplier,
                          @Named("machine.docker.networks") Set<Set<String>> additionalNetworks,
                          @Nullable @Named("che.docker.parent_cgroup") String parentCgroup,
                          @Nullable @Named("che.docker.cpuset_cpus") String cpusetCpus,
                          @Named("che.docker.cpu_period") long cpuPeriod,
                          @Named("che.docker.cpu_quota") long cpuQuota,
                          WindowsPathEscaper windowsPathEscaper,
                          @Named("che.docker.extra_hosts") Set<Set<String>> additionalHosts,
                          @Nullable @Named("che.docker.dns_resolvers") String[] dnsResolvers,
                          ServerEvaluationStrategyProvider serverEvaluationStrategyProvider) {
        this.docker = docker;
        this.dockerCredentials = dockerCredentials;
        this.dockerInstanceStopDetector = dockerInstanceStopDetector;
        this.doForcePullOnBuild = doForcePullOnBuild;
        this.privilegedMode = privilegedMode;
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
        this.parentCgroup = parentCgroup;
        this.cpusetCpus = cpusetCpus;
        this.cpuPeriod = cpuPeriod;
        this.cpuQuota = cpuQuota;
        this.windowsPathEscaper = windowsPathEscaper;
        this.pidsLimit = pidsLimit;
        this.dnsResolvers = dnsResolvers;
        this.serverEvaluationStrategyProvider = serverEvaluationStrategyProvider;

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

//        this.devMachinePortsToExpose = new ArrayList<>(allMachinesServers.size() + devMachineServers.size());
//        this.commonMachinePortsToExpose = new ArrayList<>(allMachinesServers.size());
//        for (ServerConfig serverConf : devMachineServers) {
//            devMachinePortsToExpose.add(serverConf.getPort());
//        }
//        for (ServerConfig serverConf : allMachinesServers) {
//            commonMachinePortsToExpose.add(serverConf.getPort());
//            devMachinePortsToExpose.add(serverConf.getPort());
//        }

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

        this.allMachinesExtraHosts = additionalHosts.stream()
                                                    .flatMap(Set::stream)
                                                    .toArray(String[]::new);

        this.additionalNetworks = additionalNetworks.stream()
                                                    .flatMap(Set::stream)
                                                    .collect(toSet());

        // TODO single point of failure in case of highly loaded system
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MachineLogsStreamer-%d")
                                                                           .setUncaughtExceptionHandler(
                                                                                   LoggingUncaughtExceptionHandler
                                                                                           .getInstance())
                                                                           .setDaemon(true)
                                                                           .build());
    }

    public DockerMachine startService(String networkName,
                                      String machineName,
                                      DockerService service,
                                      RuntimeIdentity identity,
                                      Map<String, String> startOptions) throws InfrastructureException {
        String workspaceId = identity.getWorkspaceId();
        LineConsumer machineLogger = new ListLineConsumer();

        // copy to not affect/be affected by changes in origin
        service = new DockerService(service);

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
//                                        isDev,
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

//            DockerNode node = dockerMachineFactory.createNode(workspaceId, container);

            dockerInstanceStopDetector.startDetection(container,
                                                      service.getId(),
                                                      workspaceId);

            return new DockerMachine(docker,
                                     container,
                                     image,
                                     serverEvaluationStrategyProvider);
        } catch (RuntimeException | ServerException | NotFoundException | IOException e) {
            cleanUpContainer(container);
            throw new InfrastructureException(e.getLocalizedMessage(), e);
        }
    }

    private String prepareImage(String machineName,
                                DockerService service,
                                ProgressMonitor progressMonitor)
            throws ServerException,
                   NotFoundException, SourceNotFoundException {

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

    protected void buildImage(DockerService service,
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
                            .withCpusetCpus(cpusetCpus)
                            .withCpuPeriod(cpuPeriod)
                            .withCpuQuota(cpuQuota)
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
    protected void pullImage(DockerService service,
                             String machineImageName,
                             ProgressMonitor progressMonitor) throws MachineException, SourceNotFoundException {
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
            throw new MachineException("Can't create machine from image. Cause: " + e.getLocalizedMessage(), e);
        }
    }

    private String createContainer(String workspaceId,
                                   String machineName,
//                                   boolean isDev,
                                   String image,
                                   String networkName,
                                   DockerService service) throws IOException {

        long machineMemorySwap = memorySwapMultiplier == -1 ?
                                 -1 :
                                 (long)(service.getMemLimit() * memorySwapMultiplier);

        addSystemWideContainerSettings(workspaceId,
//                                       isDev,
                                       service);

        EndpointConfig endpointConfig = new EndpointConfig().withAliases(machineName)
                                                            .withLinks(toArrayIfNotNull(service.getLinks()));
        NetworkingConfig networkingConfig = new NetworkingConfig().withEndpointsConfig(singletonMap(networkName,
                                                                                                    endpointConfig));

        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemorySwap(machineMemorySwap)
                  .withMemory(service.getMemLimit())
                  .withNetworkMode(networkName)
                  .withLinks(toArrayIfNotNull(service.getLinks()))
                  .withPortBindings(service.getPorts()
                                           .stream()
                                           .collect(toMap(Function.identity(), value -> new PortBinding[0])))
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

        List<String> bindMountVolumes = new ArrayList<>();
        Map<String, Volume> nonBindMountVolumes = new HashMap<>();
        for (String volume : service.getVolumes()) {
            // If volume contains colon then it is bind volume, otherwise - non bind-mount volume.
            if (volume.contains(":")) {
                bindMountVolumes.add(volume);
            } else {
                nonBindMountVolumes.put(volume, new Volume());
            }
        }
        hostConfig.setBinds(bindMountVolumes.toArray(new String[bindMountVolumes.size()]));
        config.setVolumes(nonBindMountVolumes);

        addStaticDockerConfiguration(config);

        return docker.createContainer(CreateContainerParams.create(config)
                                                           .withContainerName(service.getContainerName()))
                     .getId();
    }

    private void addStaticDockerConfiguration(ContainerConfig config) {
        config.getHostConfig()
              .withPidsLimit(pidsLimit)
              .withExtraHosts(allMachinesExtraHosts)
              .withPrivileged(privilegedMode)
              .withPublishAllPorts(true)
              .withDns(dnsResolvers);
        // CPU limits
        config.getHostConfig()
              .withCpusetCpus(cpusetCpus)
              .withCpuQuota(cpuQuota)
              .withCpuPeriod(cpuPeriod);
        // Cgroup parent for custom limits
        config.getHostConfig().setCgroupParent(parentCgroup);
    }

    private void addSystemWideContainerSettings(String workspaceId,
//                                                boolean isDev,
                                                DockerService composeService) throws IOException {
//        List<String> portsToExpose;
        List<String> volumes;
        Map<String, String> env;
//        if (isDev) {
//        portsToExpose = devMachinePortsToExpose;
        volumes = devMachineSystemVolumes;
//
        env = new HashMap<>(devMachineEnvVariables);
        env.put(CHE_WORKSPACE_ID, workspaceId);
//            env.put(USER_TOKEN, getUserToken(workspaceId));
//        } else {
//            portsToExpose = commonMachinePortsToExpose;
//            env = commonMachineEnvVariables;
//            volumes = commonMachineSystemVolumes;
//        }
//        composeService.getExpose().addAll(portsToExpose);
        composeService.getEnvironment().putAll(env);
        composeService.getVolumes().addAll(volumes);
        composeService.getNetworks().addAll(additionalNetworks);
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
                    if (errorTime - lastErrorTime <
                        20_000L) { // if new error occurs less than 20 seconds after previous
                        if (++errorsCounter == 5) {
                            LOG.error(
                                    "Too many errors while streaming logs from machine {} of workspace {} backed by container {}. " +
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

    /** Converts list to array if it is not null, otherwise returns null */
    private String[] toArrayIfNotNull(List<String> list) {
        if (list == null) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns set that contains all non empty and non nullable values from specified set
     */
    protected Set<String> removeEmptyAndNullValues(Set<String> paths) {
        return paths.stream()
                    .filter(path -> !Strings.isNullOrEmpty(path))
                    .collect(toSet());
    }

    // workspaceId parameter is required, because in case of separate storage for tokens
    // you need to know exactly which workspace and which user to apply the token.
//    protected String getUserToken(String wsId) {
//        return EnvironmentContext.getCurrent().getSubject().getToken();
//    }

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
                    .map(windowsPathEscaper::escapePath)
                    .collect(toSet());
    }

    private void connectContainerToAdditionalNetworks(String container,
                                                      DockerService service) throws IOException {

        for (String network : service.getNetworks()) {
            docker.connectContainerToNetwork(
                    ConnectContainerToNetworkParams.create(network, new ConnectContainer().withContainer(container)));
        }
    }
}

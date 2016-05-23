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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.machine.server.exception.InvalidRecipeException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.exception.UnsupportedRecipeException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerFileException;
import org.eclipse.che.plugin.docker.client.Dockerfile;
import org.eclipse.che.plugin.docker.client.DockerfileParser;
import org.eclipse.che.plugin.docker.client.ProgressLineFormatterImpl;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Docker implementation of {@link InstanceProvider}
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public class DockerInstanceProvider implements InstanceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DockerInstanceProvider.class);

    private final DockerConnector                  docker;
    private final DockerInstanceStopDetector       dockerInstanceStopDetector;
    private final DockerContainerNameGenerator     containerNameGenerator;
    private final WorkspaceFolderPathProvider      workspaceFolderPathProvider;
    private final boolean                          doForcePullOnBuild;
    private final boolean                          privilegeMode;
    private final Set<String>                      supportedRecipeTypes;
    private final DockerMachineFactory             dockerMachineFactory;
    private final Map<String, Map<String, String>> devMachinePortsToExpose;
    private final Map<String, Map<String, String>> commonMachinePortsToExpose;
    private final String[]                         devMachineSystemVolumes;
    private final String[]                         commonMachineSystemVolumes;
    private final Set<String>                      devMachineEnvVariables;
    private final Set<String>                      commonMachineEnvVariables;
    private final String[]                         allMachinesExtraHosts;
    private final String                           projectFolderPath;

    @Inject
    public DockerInstanceProvider(DockerConnector docker,
                                  DockerConnectorConfiguration dockerConnectorConfiguration,
                                  DockerMachineFactory dockerMachineFactory,
                                  DockerInstanceStopDetector dockerInstanceStopDetector,
                                  DockerContainerNameGenerator containerNameGenerator,
                                  @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
                                  @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers,
                                  @Named("machine.docker.dev_machine.machine_volumes") Set<String> devMachineSystemVolumes,
                                  @Named("machine.docker.machine_volumes") Set<String> allMachinesSystemVolumes,
                                  @Nullable @Named("machine.docker.machine_extra_hosts") String allMachinesExtraHosts,
                                  WorkspaceFolderPathProvider workspaceFolderPathProvider,
                                  @Named("che.machine.projects.internal.storage") String projectFolderPath,
                                  @Named("machine.docker.pull_image") boolean doForcePullOnBuild,
                                  @Named("machine.docker.privilege_mode") boolean privilegeMode,
                                  @Named("machine.docker.dev_machine.machine_env") Set<String> devMachineEnvVariables,
                                  @Named("machine.docker.machine_env") Set<String> allMachinesEnvVariables)
            throws IOException {

        this.docker = docker;
        this.dockerMachineFactory = dockerMachineFactory;
        this.dockerInstanceStopDetector = dockerInstanceStopDetector;
        this.containerNameGenerator = containerNameGenerator;
        this.workspaceFolderPathProvider = workspaceFolderPathProvider;
        this.doForcePullOnBuild = doForcePullOnBuild;
        this.privilegeMode = privilegeMode;
        this.supportedRecipeTypes = Collections.singleton("dockerfile");
        this.projectFolderPath = projectFolderPath;

        allMachinesSystemVolumes = removeEmptyAndNullValues(allMachinesSystemVolumes);
        devMachineSystemVolumes = removeEmptyAndNullValues(devMachineSystemVolumes);
        if (SystemInfo.isWindows()) {
            allMachinesSystemVolumes = escapePaths(allMachinesSystemVolumes);
            devMachineSystemVolumes = escapePaths(devMachineSystemVolumes);
        }
        this.commonMachineSystemVolumes = allMachinesSystemVolumes.toArray(new String[allMachinesSystemVolumes.size()]);
        final Set<String> devMachineVolumes = Sets.newHashSetWithExpectedSize(allMachinesSystemVolumes.size()
                                                                              + devMachineSystemVolumes.size());
        devMachineVolumes.addAll(allMachinesSystemVolumes);
        devMachineVolumes.addAll(devMachineSystemVolumes);
        this.devMachineSystemVolumes = devMachineVolumes.toArray(new String[devMachineVolumes.size()]);

        this.devMachinePortsToExpose = Maps.newHashMapWithExpectedSize(allMachinesServers.size() + devMachineServers.size());
        this.commonMachinePortsToExpose = Maps.newHashMapWithExpectedSize(allMachinesServers.size());
        for (ServerConf serverConf : devMachineServers) {
            devMachinePortsToExpose.put(serverConf.getPort(), Collections.emptyMap());
        }
        for (ServerConf serverConf : allMachinesServers) {
            commonMachinePortsToExpose.put(serverConf.getPort(), Collections.emptyMap());
            devMachinePortsToExpose.put(serverConf.getPort(), Collections.emptyMap());
        }

        allMachinesEnvVariables = removeEmptyAndNullValues(allMachinesEnvVariables);
        devMachineEnvVariables = removeEmptyAndNullValues(devMachineEnvVariables);
        this.commonMachineEnvVariables = allMachinesEnvVariables;
        final HashSet<String> envVariablesForDevMachine = Sets.newHashSetWithExpectedSize(allMachinesEnvVariables.size() +
                                                                                          devMachineEnvVariables.size());
        envVariablesForDevMachine.addAll(allMachinesEnvVariables);
        envVariablesForDevMachine.addAll(devMachineEnvVariables);
        this.devMachineEnvVariables = envVariablesForDevMachine;

        // always add the docker host
        String dockerHost = DockerInstanceRuntimeInfo.CHE_HOST.concat(":").concat(dockerConnectorConfiguration.getDockerHostIp());
        if (isNullOrEmpty(allMachinesExtraHosts)) {
            this.allMachinesExtraHosts = new String[] {dockerHost};
        } else {
            this.allMachinesExtraHosts = ObjectArrays.concat(allMachinesExtraHosts.split(","), dockerHost);
        }
    }

    /**
     * Escape paths for Windows system with boot@docker according to rules given here :
     * https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
     *
     * @param paths
     *         set of paths to escape
     * @return set of escaped path
     */
    protected Set<String> escapePaths(Set<String> paths) {
        return paths.stream().map(this::escapePath).collect(Collectors.toSet());
    }

    /**
     * Escape path for Windows system with boot@docker according to rules given here :
     * https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
     *
     * @param path
     *         path to escape
     * @return escaped path
     */
    protected String escapePath(String path) {
        String esc;
        if (path.indexOf(":") == 1) {
            //check and replace only occurrence of ":" after disk label on Windows host (e.g. C:/)
            // but keep other occurrences it can be marker for docker mount volumes
            // (e.g. /path/dir/from/host:/name/of/dir/in/container                                               )
            esc = path.replaceFirst(":", "").replace('\\', '/');
            esc = Character.toLowerCase(esc.charAt(0)) + esc.substring(1); //letter of disk mark must be lower case
        } else {
            esc = path.replace('\\', '/');
        }
        if (!esc.startsWith("/")) {
            esc = "/" + esc;
        }
        return esc;
    }


    @Override
    public String getType() {
        return "docker";
    }

    @Override
    public Set<String> getRecipeTypes() {
        return supportedRecipeTypes;
    }

    @Override
    public Instance createInstance(Recipe recipe,
                                   Machine machine,
                                   LineConsumer creationLogsOutput) throws MachineException, UnsupportedRecipeException {
        final Dockerfile dockerfile = parseRecipe(recipe);

        final String userName = EnvironmentContext.getCurrent().getSubject().getUserName();
        final String machineContainerName = containerNameGenerator.generateContainerName(machine.getWorkspaceId(),
                                                                                         machine.getId(),
                                                                                         userName,
                                                                                         machine.getConfig().getName());
        final String machineImageName = "eclipse-che/" + machineContainerName;
        final long memoryLimit = (long)machine.getConfig().getLimits().getRam() * 1024 * 1024;

        buildImage(dockerfile, creationLogsOutput, machineImageName, doForcePullOnBuild, memoryLimit, -1);

        return createInstance(machineContainerName,
                              machine,
                              machineImageName,
                              creationLogsOutput);
    }

    @Override
    public Instance createInstance(InstanceKey instanceKey,
                                   Machine machine,
                                   LineConsumer creationLogsOutput) throws NotFoundException, MachineException {
        final DockerInstanceKey dockerInstanceKey = new DockerInstanceKey(instanceKey);

        pullImage(dockerInstanceKey, creationLogsOutput);

        final String userName = EnvironmentContext.getCurrent().getSubject().getUserName();
        final String machineContainerName = containerNameGenerator.generateContainerName(machine.getWorkspaceId(),
                                                                                         machine.getId(),
                                                                                         userName,
                                                                                         machine.getConfig().getName());
        final String machineImageName = "eclipse-che/" + machineContainerName;
        final String fullNameOfPulledImage = dockerInstanceKey.getFullName();
        try {
            // tag image with generated name to allow sysadmin recognize it
            docker.tag(fullNameOfPulledImage, machineImageName, null);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException("Can't create machine from snapshot.");
        }
        try {
            // remove unneeded tag
            docker.removeImage(fullNameOfPulledImage, false);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return createInstance(machineContainerName,
                              machine,
                              machineImageName,
                              creationLogsOutput);
    }

    private Dockerfile parseRecipe(Recipe recipe) throws InvalidRecipeException {
        final Dockerfile dockerfile = getDockerFile(recipe);
        if (dockerfile.getImages().isEmpty()) {
            throw new InvalidRecipeException("Unable build docker based machine, Dockerfile found but it doesn't contain base image.");
        }
        if (dockerfile.getImages().size() > 1) {
            throw new InvalidRecipeException(
                    "Unable build docker based machine, Dockerfile found but it contains more than one instruction 'FROM'.");
        }
        return dockerfile;
    }

    private Dockerfile getDockerFile(Recipe recipe) throws InvalidRecipeException {
        if (recipe.getScript() == null) {
            throw new InvalidRecipeException("Unable build docker based machine, recipe isn't set or doesn't provide Dockerfile and " +
                                             "no Dockerfile found in the list of files attached to this builder.");
        }
        try {
            return DockerfileParser.parse(recipe.getScript());
        } catch (DockerFileException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            throw new InvalidRecipeException("Unable build docker based machine. " + e.getMessage());
        }
    }

    protected void buildImage(Dockerfile dockerfile,
                              final LineConsumer creationLogsOutput,
                              String imageName,
                              boolean doForcePullOnBuild,
                              long memoryLimit,
                              long memorySwapLimit)
            throws MachineException {

        File workDir = null;
        try {
            // build docker image
            workDir = Files.createTempDirectory(null).toFile();
            final File dockerfileFile = new File(workDir, "Dockerfile");
            dockerfile.writeDockerfile(dockerfileFile);
            final List<File> files = new LinkedList<>();
            //noinspection ConstantConditions
            Collections.addAll(files, workDir.listFiles());
            final ProgressLineFormatterImpl progressLineFormatter = new ProgressLineFormatterImpl();
            final ProgressMonitor progressMonitor = currentProgressStatus -> {
                try {
                    creationLogsOutput.writeLine(progressLineFormatter.format(currentProgressStatus));
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            };
            docker.buildImage(imageName,
                              progressMonitor,
                              null,
                              doForcePullOnBuild,
                              memoryLimit,
                              memorySwapLimit,
                              files.toArray(new File[files.size()]));
        } catch (IOException | InterruptedException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            if (workDir != null) {
                FileCleaner.addFile(workDir);
            }
        }
    }

    private void pullImage(DockerInstanceKey dockerInstanceKey, final LineConsumer creationLogsOutput) throws MachineException {
        if (dockerInstanceKey.getRepository() == null) {
            throw new MachineException("Machine creation failed. Snapshot state is invalid. Please, contact support.");
        }
        try {
            final ProgressLineFormatterImpl progressLineFormatter = new ProgressLineFormatterImpl();
            docker.pull(dockerInstanceKey.getRepository(),
                        dockerInstanceKey.getTag(),
                        dockerInstanceKey.getRegistry(),
                        currentProgressStatus -> {
                            try {
                                creationLogsOutput.writeLine(progressLineFormatter.format(currentProgressStatus));
                            } catch (IOException e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        });
        } catch (IOException | InterruptedException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void removeInstanceSnapshot(InstanceKey instanceKey) throws SnapshotException {
        // use registry API directly because docker doesn't have such API yet
        // https://github.com/docker/docker-registry/issues/45
        final DockerInstanceKey dockerInstanceKey = new DockerInstanceKey(instanceKey);
        String registry = dockerInstanceKey.getRegistry();
        String repository = dockerInstanceKey.getRepository();
        if (registry == null || repository == null) {
            LOG.error("Failed to remove instance snapshot: invalid instance key: {}", instanceKey);
            throw new SnapshotException("Snapshot removing failed. Snapshot attributes are not valid");
        }

        try {
            URL url = UriBuilder.fromUri("http://" + registry) // TODO make possible to use https here
                                .path("/v2/{repository}/manifests/{digest}")
                                .build(repository, dockerInstanceKey.getDigest())
                                .toURL();
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            try {
                conn.setConnectTimeout(30 * 1000);
                conn.setRequestMethod("DELETE");
                // TODO add auth header for secured registry
                // conn.setRequestProperty("Authorization", authHeader);
                final int responseCode = conn.getResponseCode();
                if ((responseCode / 100) != 2) {
                    InputStream in = conn.getErrorStream();
                    if (in == null) {
                        in = conn.getInputStream();
                    }
                    LOG.error("An error occurred while deleting snapshot with url: {}\nError stream: {}",
                              url,
                              IoUtil.readAndCloseQuietly(in));
                    throw new SnapshotException("Internal server error occurs. Can't remove snapshot");
                }
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private Instance createInstance(String containerName,
                                    Machine machine,
                                    String imageName,
                                    LineConsumer outputConsumer)
            throws MachineException {
        try {
            final Map<String, Map<String, String>> portsToExpose;
            final String[] volumes;
            final List<String> env;
            if (machine.getConfig().isDev()) {
                portsToExpose = new HashMap<>(devMachinePortsToExpose);

                final String projectFolderVolume = format("%s:%s:Z",
                                                          workspaceFolderPathProvider.getPath(machine.getWorkspaceId()),
                                                          projectFolderPath);
                volumes = ObjectArrays.concat(devMachineSystemVolumes,
                                              SystemInfo.isWindows() ? escapePath(projectFolderVolume) : projectFolderVolume);

                env = new ArrayList<>(devMachineEnvVariables);
                env.add(DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID + '=' + machine.getWorkspaceId());
                env.add(DockerInstanceRuntimeInfo.USER_TOKEN + '=' + getUserToken(machine.getWorkspaceId()));
            } else {
                portsToExpose = new HashMap<>(commonMachinePortsToExpose);
                volumes = commonMachineSystemVolumes;
                env = new ArrayList<>(commonMachineEnvVariables);
            }
            machine.getConfig()
                   .getServers()
                   .stream()
                   .forEach(serverConf -> portsToExpose.put(serverConf.getPort(), Collections.emptyMap()));

            machine.getConfig()
                   .getEnvVariables()
                   .entrySet()
                   .stream()
                   .map(entry -> entry.getKey() + "=" + entry.getValue())
                   .forEach(env::add);

            final HostConfig hostConfig = new HostConfig().withBinds(volumes)
                                                          .withExtraHosts(allMachinesExtraHosts)
                                                          .withPublishAllPorts(true)
                                                          .withMemorySwap(-1)
                                                          .withMemory((long)machine.getConfig().getLimits().getRam() * 1024 * 1024)
                                                          .withPrivileged(privilegeMode);
            final ContainerConfig config = new ContainerConfig().withImage(imageName)
                                                                .withExposedPorts(portsToExpose)
                                                                .withHostConfig(hostConfig)
                                                                .withEnv(env.toArray(new String[env.size()]));

            final String containerId = docker.createContainer(config, containerName).getId();

            docker.startContainer(containerId, null);

            final DockerNode node = dockerMachineFactory.createNode(machine.getWorkspaceId(), containerId);
            if (machine.getConfig().isDev()) {
                node.bindWorkspace();
                LOG.info("Machine with id '{}' backed by container '{}' has been deployed on node '{}'",
                         machine.getId(), containerId, node.getHost());
            }

            dockerInstanceStopDetector.startDetection(containerId, machine.getId());

            return dockerMachineFactory.createInstance(machine,
                                                       containerId,
                                                       imageName,
                                                       node,
                                                       outputConsumer);
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
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
                    .collect(Collectors.toSet());
    }
}

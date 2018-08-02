/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.LogMessage;
import org.eclipse.che.infrastructure.docker.client.MessageProcessor;
import org.eclipse.che.infrastructure.docker.client.exception.ContainerNotFoundException;
import org.eclipse.che.infrastructure.docker.client.json.ContainerConfig;
import org.eclipse.che.infrastructure.docker.client.json.ContainerInfo;
import org.eclipse.che.infrastructure.docker.client.json.HostConfig;
import org.eclipse.che.infrastructure.docker.client.json.ImageConfig;
import org.eclipse.che.infrastructure.docker.client.json.PortBinding;
import org.eclipse.che.infrastructure.docker.client.json.Volume;
import org.eclipse.che.infrastructure.docker.client.json.container.NetworkingConfig;
import org.eclipse.che.infrastructure.docker.client.json.network.ConnectContainer;
import org.eclipse.che.infrastructure.docker.client.json.network.EndpointConfig;
import org.eclipse.che.infrastructure.docker.client.params.CreateContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.GetContainerLogsParams;
import org.eclipse.che.infrastructure.docker.client.params.RemoveContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.StartContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.network.ConnectContainerToNetworkParams;
import org.eclipse.che.workspace.infrastructure.docker.exception.SourceNotFoundException;
import org.eclipse.che.workspace.infrastructure.docker.logs.MachineLoggersFactory;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.monit.AbnormalMachineStopHandler;
import org.eclipse.che.workspace.infrastructure.docker.monit.DockerMachineStopDetector;
import org.slf4j.Logger;

/*
 TODO:

 1. Decompose this component on:
  - DockerNetworks(NetworkLifecycle <-) - deals with docker networks.
  - DockerImages - deals with docker images.
  - DockerContainers - deals with docker containers.
 Then DockerRuntimeContext may use these components to run consistent
 and clear flow of calls to achieve its needs, e.g.:

 DockerNetworks networks;
 DockerImages images;
 DockerContainer containers;

 start runtime {
   networks.create(environment.getNetwork())
   for (config: environment.getConfigs()) {
     String image
     if (isBuildable(config)) {
       image = images.build(config)
     } else {
       image = images.pull(config)
     }
     String id = containers.create(image)
     Container container = containers.start(id);
     DockerMachine machine = machineCreator.create(container);
     ...
   }
 }

 2. Move the logic related to containers configuration modification to
 DockerEnvironmentProvisioner implementation.
*/

/**
 * Starts container described in {@link DockerContainerConfig}. Includes building/pulling images,
 * networks creation, etc.
 *
 * @author Alexander Garagatyi
 */
public class DockerMachineStarter {
  private static final Logger LOG = getLogger(DockerMachineStarter.class);

  private static final String CONTAINER_EXITED_ERROR =
      "We detected that a machine exited unexpectedly. "
          + "This may be caused by a container in interactive mode "
          + "or a container that requires additional arguments to start. "
          + "Please check the container recipe.";

  // CMDs and entrypoints that lead to exiting of container right after start
  private static final Set<List<String>> badCMDs =
      ImmutableSet.of(
          singletonList("/bin/bash"),
          singletonList("/bin/sh"),
          singletonList("bash"),
          singletonList("sh"),
          Arrays.asList("/bin/sh", "-c", "/bin/sh"),
          Arrays.asList("/bin/sh", "-c", "/bin/bash"),
          Arrays.asList("/bin/sh", "-c", "bash"),
          Arrays.asList("/bin/sh", "-c", "sh"));

  private static final Set<List<String>> badEntrypoints =
      ImmutableSet.<List<String>>builder()
          .addAll(badCMDs)
          .add(Arrays.asList("/bin/sh", "-c"))
          .add(Arrays.asList("/bin/bash", "-c"))
          .add(Arrays.asList("sh", "-c"))
          .add(Arrays.asList("bash", "-c"))
          .build();

  private final DockerConnector docker;

  private final ExecutorService executor;
  private final DockerMachineStopDetector dockerInstanceStopDetector;

  private final MachineLoggersFactory machineLoggerFactory;
  private final DockerMachineCreator machineCreator;

  @Inject
  public DockerMachineStarter(
      DockerConnector docker,
      DockerMachineStopDetector dockerMachineStopDetector,
      MachineLoggersFactory machineLogger,
      DockerMachineCreator machineCreator) {
    this.machineCreator = machineCreator;
    // TODO spi should we move all configuration stuff into infrastructure provisioner and left
    // logic of container start here only
    this.docker = docker;
    this.dockerInstanceStopDetector = dockerMachineStopDetector;
    this.machineLoggerFactory = machineLogger;
    // single point of failure in case of highly loaded system
    executor =
        Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("MachineLogsStreamer-%d")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setDaemon(true)
                .build());
  }

  /**
   * Start Docker machine by performing all needed operations such as pull, build, create container,
   * etc.
   *
   * @param networkName name of a network Docker container should use
   * @param machineName name of Docker machine
   * @param containerConfig configuration of container to start
   * @param identity identity of user that starts machine
   * @return {@link DockerMachine} instance that represents started container
   * @throws InternalInfrastructureException if internal error occurs
   * @throws SourceNotFoundException if image for container creation is missing
   * @throws InfrastructureException if any other error occurs
   */
  public DockerMachine startContainer(
      String networkName,
      String machineName,
      String image,
      DockerContainerConfig containerConfig,
      RuntimeIdentity identity,
      AbnormalMachineStopHandler abnormalMachineStopHandler)
      throws InfrastructureException {
    String workspaceId = identity.getWorkspaceId();

    // copy to not affect/be affected by changes in origin
    containerConfig = new DockerContainerConfig(containerConfig);
    String container = null;
    try {
      container = createContainer(machineName, image, networkName, containerConfig);

      connectContainerToAdditionalNetworks(container, containerConfig);

      docker.startContainer(StartContainerParams.create(container));

      ContainerInfo runningContainer = getRunningContainer(container);

      readContainerLogsInSeparateThread(
          container,
          workspaceId,
          containerConfig.getId(),
          machineLoggerFactory.newLogsProcessor(machineName, identity));

      dockerInstanceStopDetector.startDetection(container, machineName, abnormalMachineStopHandler);

      return machineCreator.create(runningContainer);
    } catch (RuntimeException | IOException | InfrastructureException e) {
      cleanUpContainer(container);
      if (e instanceof InfrastructureException) {
        throw (InfrastructureException) e;
      } else {
        throw new InternalInfrastructureException(e.getLocalizedMessage(), e);
      }
    }
  }

  private String createContainer(
      String machineName, String image, String networkName, DockerContainerConfig containerConfig)
      throws IOException, InternalInfrastructureException {

    EndpointConfig endpointConfig =
        new EndpointConfig()
            .withAliases(machineName)
            .withLinks(toArrayIfNotNull(containerConfig.getLinks()));
    NetworkingConfig networkingConfig =
        new NetworkingConfig().withEndpointsConfig(singletonMap(networkName, endpointConfig));

    HostConfig hostConfig = new HostConfig();
    hostConfig
        .withMemory(containerConfig.getMemLimit())
        .withMemorySwap(containerConfig.getMemSwapLimit())
        .withPidsLimit(containerConfig.getPidsLimit())
        .withPidMode(containerConfig.getPidMode())
        .withExtraHosts(
            containerConfig
                .getExtraHosts()
                .toArray(new String[containerConfig.getExtraHosts().size()]))
        .withPrivileged(containerConfig.getPrivileged())
        .withDns(containerConfig.getDns().toArray(new String[containerConfig.getDns().size()]))
        .withSecurityOpt(
            containerConfig
                .getSecurityOpt()
                .toArray(new String[containerConfig.getSecurityOpt().size()]))
        .withCpusetCpus(containerConfig.getCpuSet())
        .withCpuQuota(containerConfig.getCpuQuota())
        .withCpuPeriod(containerConfig.getCpuPeriod())
        .withCgroupParent(containerConfig.getCgroupParent())
        .withNetworkMode(networkName)
        .withLinks(toArrayIfNotNull(containerConfig.getLinks()))
        .withPortBindings(convertPortBindings(containerConfig.getPorts(), machineName))
        .withVolumesFrom(toArrayIfNotNull(containerConfig.getVolumesFrom()));

    ContainerConfig config = new ContainerConfig();
    config
        .withImage(image)
        .withExposedPorts(
            containerConfig
                .getExpose()
                .stream()
                .distinct()
                .collect(toMap(Function.identity(), value -> emptyMap())))
        .withHostConfig(hostConfig)
        .withCmd(toArrayIfNotNull(containerConfig.getCommand()))
        .withEntrypoint(toArrayIfNotNull(containerConfig.getEntrypoint()))
        .withLabels(containerConfig.getLabels())
        .withNetworkingConfig(networkingConfig)
        .withEnv(
            containerConfig
                .getEnvironment()
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new));

    List<String> bindMountVolumes = new ArrayList<>();
    Map<String, Volume> nonBindMountVolumes = new HashMap<>();
    for (String volume : containerConfig.getVolumes()) {
      // If volume contains colon then it is bind volume, otherwise - non bind-mount volume.
      if (volume.contains(":")) {
        bindMountVolumes.add(volume);
      } else {
        nonBindMountVolumes.put(volume, new Volume());
      }
    }
    hostConfig.setBinds(bindMountVolumes.toArray(new String[bindMountVolumes.size()]));
    config.setVolumes(nonBindMountVolumes);

    setNonExitingContainerCommandIfNeeded(config);

    return docker
        .createContainer(
            CreateContainerParams.create(config)
                .withContainerName(containerConfig.getContainerName()))
        .getId();
  }

  private Map<String, PortBinding[]> convertPortBindings(
      List<String> portsSpecs, String machineName) throws InternalInfrastructureException {
    Map<String, PortBinding[]> portsBindings = Maps.newHashMapWithExpectedSize(portsSpecs.size());
    for (String portSpec : portsSpecs) {
      String[] portMapping = portSpec.split(":");
      if (portMapping.length == 1) {
        portsBindings.put(portMapping[0], new PortBinding[] {new PortBinding()});
      } else if (portMapping.length == 2) {
        portsBindings.put(
            portMapping[0], new PortBinding[] {new PortBinding().withHostPort(portMapping[1])});
      } else {
        throw new InternalInfrastructureException(
            format("Invalid port specification '%s' found machine '%s'", portsSpecs, machineName));
      }
    }
    return portsBindings;
  }

  // We can detect certain situation when container exited right after start.
  // We can detect
  //  - when no command/entrypoint is set
  //  - when most common shell interpreters are used and require additional arguments
  //  - when most common shell interpreters are used and they require interactive mode which we
  // don't support
  // When we identify such situation we change CMD/entrypoint in such a way that it runs "tail -f
  // /dev/null".
  // This command does nothing and lasts until workspace is stopped.
  // Images such as "ubuntu" or "openjdk" fits this situation.
  protected void setNonExitingContainerCommandIfNeeded(ContainerConfig containerConfig)
      throws IOException {
    ImageConfig imageConfig = docker.inspectImage(containerConfig.getImage()).getConfig();
    List<String> cmd = imageConfig.getCmd() == null ? null : Arrays.asList(imageConfig.getCmd());
    List<String> entrypoint =
        imageConfig.getEntrypoint() == null ? null : Arrays.asList(imageConfig.getEntrypoint());

    if ((entrypoint == null || badEntrypoints.contains(entrypoint))
        && (cmd == null || badCMDs.contains(cmd))) {
      containerConfig.setCmd("tail", "-f", "/dev/null");
      containerConfig.setEntrypoint((String[]) null);
    }
  }

  // Inspect container right after start to check if it is running,
  // otherwise throw error that command should not exit right after container start
  protected ContainerInfo getRunningContainer(String container)
      throws IOException, InfrastructureException {
    ContainerInfo containerInfo = docker.inspectContainer(container);
    if ("exited".equals(containerInfo.getState().getStatus())) {
      throw new InfrastructureException(CONTAINER_EXITED_ERROR);
    }
    return containerInfo;
  }

  @VisibleForTesting
  void readContainerLogsInSeparateThread(
      String container,
      String workspaceId,
      String machineId,
      MessageProcessor<LogMessage> logsProcessor) {
    executor.execute(
        () -> {
          long lastProcessedLogDate = 0;
          boolean isContainerRunning = true;
          int errorsCounter = 0;
          long lastErrorTime = 0;
          while (isContainerRunning) {
            try {
              docker.getContainerLogs(
                  GetContainerLogsParams.create(container)
                      .withFollow(true)
                      .withSince(lastProcessedLogDate),
                  logsProcessor);
              isContainerRunning = false;
            } catch (SocketTimeoutException ste) {
              lastProcessedLogDate = System.currentTimeMillis() / 1000L;
              // reconnect to container
            } catch (ContainerNotFoundException e) {
              isContainerRunning = false;
            } catch (IOException e) {
              long errorTime = System.currentTimeMillis();
              lastProcessedLogDate = errorTime / 1000L;
              LOG.warn(
                  "Failed to get logs from machine {} of workspace {} backed by container {}, because: {}.",
                  machineId,
                  workspaceId,
                  container,
                  e.getMessage(),
                  e);
              if (errorTime - lastErrorTime
                  < 20_000L) { // if new error occurs less than 20 seconds after previous
                if (++errorsCounter == 5) {
                  LOG.error(
                      "Too many errors while streaming logs from machine {} of workspace {} backed by container {}. "
                          + "Logs streaming is closed. Last error: {}.",
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
        docker.removeContainer(
            RemoveContainerParams.create(containerId).withRemoveVolumes(true).withForce(true));
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

  // TODO spi should we move it into network lifecycle?
  private void connectContainerToAdditionalNetworks(
      String container, DockerContainerConfig containerConfig) throws IOException {

    for (String network : containerConfig.getNetworks()) {
      docker.connectContainerToNetwork(
          ConnectContainerToNetworkParams.create(
              network, new ConnectContainer().withContainer(container)));
    }
  }
}

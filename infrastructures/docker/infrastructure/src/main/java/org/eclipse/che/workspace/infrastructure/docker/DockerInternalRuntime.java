/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult.ProbeStatus;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.infrastructure.docker.client.json.ContainerListEntry;
import org.eclipse.che.workspace.infrastructure.docker.bootstrap.DockerBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.docker.logs.MachineLoggersFactory;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.monit.AbnormalMachineStopHandler;
import org.eclipse.che.workspace.infrastructure.docker.monit.DockerMachineStopDetector;
import org.eclipse.che.workspace.infrastructure.docker.network.NetworkLifecycle;
import org.slf4j.Logger;

/**
 * Represents {@link InternalRuntime} for Docker infrastructure.
 *
 * @author Alexander Garagatyi
 */
public class DockerInternalRuntime extends InternalRuntime<DockerRuntimeContext> {

  private static final Logger LOG = getLogger(DockerInternalRuntime.class);

  private final RuntimeMachines runtimeMachines;
  private final StartSynchronizer startSynchronizer;
  private final Map<String, String> properties;
  private final NetworkLifecycle networks;
  private final DockerMachineStarter containerStarter;
  private final EventService eventService;
  private final DockerBootstrapperFactory bootstrapperFactory;
  private final ServersCheckerFactory serverCheckerFactory;
  private final MachineLoggersFactory loggers;
  private final ProbeScheduler probeScheduler;
  private final WorkspaceProbesFactory probesFactory;
  private final ParallelDockerImagesBuilderFactory imagesBuilderFactory;
  private final int bootstrappingTimeoutMinutes;

  /**
   * Creates non running runtime. Normally created by {@link
   * DockerRuntimeFactory#create(DockerRuntimeContext, List)}.
   */
  @AssistedInject
  public DockerInternalRuntime(
      @Assisted DockerRuntimeContext context,
      @Assisted List<Warning> warnings,
      URLRewriter urlRewriter,
      NetworkLifecycle networks,
      DockerMachineStarter machineStarter,
      EventService eventService,
      DockerBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      MachineLoggersFactory loggers,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      ParallelDockerImagesBuilderFactory imagesBuilderFactory,
      @Named("che.infra.docker.bootstrapper.timeout_min") int bootstrappingTimeoutMinutes) {
    this(
        context,
        urlRewriter,
        warnings,
        false, // <- non running
        networks,
        machineStarter,
        eventService,
        bootstrapperFactory,
        serverCheckerFactory,
        loggers,
        probeScheduler,
        probesFactory,
        imagesBuilderFactory,
        bootstrappingTimeoutMinutes);
  }

  /**
   * Creates a running runtime from the list of given containers. Normally created by {@link
   * DockerRuntimeFactory#create(DockerRuntimeContext, List, List)}.
   */
  @AssistedInject
  public DockerInternalRuntime(
      @Assisted DockerRuntimeContext context,
      @Assisted List<ContainerListEntry> containers,
      @Assisted List<Warning> warnings,
      URLRewriter urlRewriter,
      NetworkLifecycle networks,
      DockerMachineStarter machineStarter,
      EventService eventService,
      DockerBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      MachineLoggersFactory loggers,
      DockerMachineCreator machineCreator,
      DockerMachineStopDetector stopDetector,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      ParallelDockerImagesBuilderFactory imagesBuilderFactory,
      @Named("che.infra.docker.bootstrapper.timeout_min") int bootstrappingTimeoutMinutes)
      throws InfrastructureException {
    this(
        context,
        urlRewriter,
        warnings,
        true, // <- running
        networks,
        machineStarter,
        eventService,
        bootstrapperFactory,
        serverCheckerFactory,
        loggers,
        probeScheduler,
        probesFactory,
        imagesBuilderFactory,
        bootstrappingTimeoutMinutes);

    for (ContainerListEntry container : containers) {
      DockerMachine machine = machineCreator.create(container);
      String name = Labels.newDeserializer(container.getLabels()).machineName();

      runtimeMachines.putMachine(name, machine);
      stopDetector.startDetection(container.getId(), name, new AbnormalMachineStopHandlerImpl());
      streamLogsAsync(name, container.getId());
    }
  }

  private DockerInternalRuntime(
      DockerRuntimeContext context,
      URLRewriter urlRewriter,
      List<Warning> warnings,
      boolean running,
      NetworkLifecycle networks,
      DockerMachineStarter machineStarter,
      EventService eventService,
      DockerBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      MachineLoggersFactory loggers,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      ParallelDockerImagesBuilderFactory imagesBuilderFactory,
      int bootstrappingTimeoutMinutes) {
    super(context, urlRewriter, warnings, running);
    this.networks = networks;
    this.containerStarter = machineStarter;
    this.eventService = eventService;
    this.bootstrapperFactory = bootstrapperFactory;
    this.serverCheckerFactory = serverCheckerFactory;
    this.probesFactory = probesFactory;
    this.bootstrappingTimeoutMinutes = bootstrappingTimeoutMinutes;
    this.properties = new HashMap<>();
    this.startSynchronizer = new StartSynchronizer();
    this.runtimeMachines = new RuntimeMachines();
    this.loggers = loggers;
    this.probeScheduler = probeScheduler;
    this.imagesBuilderFactory = imagesBuilderFactory;
  }

  @Override
  protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
    startSynchronizer.setStartThread();
    try {
      networks.createNetwork(getContext().getEnvironment().getNetwork());
      Map<String, String> images =
          imagesBuilderFactory
              .create(getContext().getIdentity())
              .prepareImages(getContext().getEnvironment().getContainers());

      for (Map.Entry<String, DockerContainerConfig> containerEntry :
          getContext().getEnvironment().getContainers().entrySet()) {
        checkInterruption();
        String machineName = containerEntry.getKey();

        runtimeMachines.putMachine(machineName, new DockerMachine.StartingDockerMachine());
        sendStartingEvent(machineName);

        try {
          DockerMachine machine =
              startMachine(machineName, images.get(machineName), containerEntry.getValue());
          sendRunningEvent(machineName);

          bootstrapInstallers(machineName, machine);

          checkServers(machineName, machine);
        } catch (InfrastructureException e) {
          sendFailedEvent(machineName, e.getMessage());
          throw e;
        }
      }
      startSynchronizer.complete();
    } catch (InfrastructureException | InterruptedException | RuntimeException e) {
      boolean interrupted = Thread.interrupted() || e instanceof InterruptedException;

      // Cancels workspace servers probes if any
      probeScheduler.cancel(getContext().getIdentity().getWorkspaceId());

      try {
        destroyRuntime(emptyMap());
      } catch (InternalInfrastructureException destExc) {
        LOG.error(destExc.getMessage(), destExc);
      } catch (InfrastructureException ignore) {
      }

      if (interrupted) {
        final RuntimeStartInterruptedException ex =
            new RuntimeStartInterruptedException(getContext().getIdentity());
        startSynchronizer.completeExceptionally(ex);
        throw ex;
      }
      startSynchronizer.completeExceptionally(e);
      try {
        throw e;
      } catch (InfrastructureException rethrow) {
        throw rethrow;
      } catch (Exception wrap) {
        throw new InternalInfrastructureException(e.getMessage(), wrap);
      }
    }
  }

  @Override
  protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
    // Cancels workspace servers probes if any
    probeScheduler.cancel(getContext().getIdentity().getWorkspaceId());

    if (startSynchronizer.interrupt()) {
      try {
        startSynchronizer.await();
      } catch (RuntimeStartInterruptedException ex) {
        // normal stop
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException(
            "Interrupted while waiting for start task cancellation", ex);
      }
    } else {
      destroyRuntime(stopOptions);
    }
  }

  @Override
  public Map<String, ? extends Machine> getInternalMachines() {
    return runtimeMachines
        .getMachines()
        .entrySet()
        .stream()
        .collect(toMap(Map.Entry::getKey, e -> new MachineImpl(e.getValue())));
  }

  @Override
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(properties);
  }

  /** Checks servers availability on all the machines. */
  void checkServers() throws InfrastructureException {
    for (Map.Entry<String, ? extends DockerMachine> entry :
        runtimeMachines.getMachines().entrySet()) {
      String name = entry.getKey();
      DockerMachine machine = entry.getValue();
      RuntimeIdentity runtimeId = getContext().getIdentity();
      ServersChecker checker = serverCheckerFactory.create(runtimeId, name, machine.getServers());
      checker.checkOnce(new ServerReadinessHandler(name));

      probeScheduler.schedule(
          probesFactory.getProbes(runtimeId, name, machine.getServers()),
          new ServerLivenessHandler());
    }
  }

  private DockerMachine startMachine(
      String name, String image, DockerContainerConfig containerConfig)
      throws InfrastructureException, InterruptedException {
    RuntimeIdentity identity = getContext().getIdentity();

    DockerMachine machine =
        containerStarter.startContainer(
            getContext().getEnvironment().getNetwork(),
            name,
            image,
            containerConfig,
            identity,
            new AbnormalMachineStopHandlerImpl());
    try {
      runtimeMachines.putMachine(name, machine);

      return machine;
    } catch (InfrastructureException e) {
      // destroy machine only in case its addition fails
      // in other cases cleanup of whole runtime will be performed
      destroyMachineQuietly(name, machine);
      throw e;
    }
  }

  private void checkServers(String name, DockerMachine machine)
      throws InterruptedException, InfrastructureException {
    RuntimeIdentity identity = getContext().getIdentity();

    checkInterruption();
    // one-time check
    ServersChecker readinessChecker =
        serverCheckerFactory.create(identity, name, machine.getServers());
    readinessChecker.startAsync(new ServerReadinessHandler(name));
    readinessChecker.await();

    checkInterruption();
    // continuous checking
    probeScheduler.schedule(
        probesFactory.getProbes(identity, name, machine.getServers()), new ServerLivenessHandler());
  }

  private void bootstrapInstallers(String name, DockerMachine machine)
      throws InfrastructureException, InterruptedException {
    InternalMachineConfig machineCfg = getContext().getEnvironment().getMachines().get(name);
    RuntimeIdentity identity = getContext().getIdentity();

    if (!machineCfg.getInstallers().isEmpty()) {
      checkInterruption();
      bootstrapperFactory
          .create(name, identity, machineCfg.getInstallers(), machine)
          .bootstrap(bootstrappingTimeoutMinutes);
    }
  }

  private void checkInterruption() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
  }

  // TODO stream bootstrapper logs as well
  private void streamLogsAsync(String name, String containerId) {
    containerStarter.readContainerLogsInSeparateThread(
        containerId,
        getContext().getIdentity().getWorkspaceId(),
        name,
        loggers.newLogsProcessor(name, getContext().getIdentity()));
  }

  private class ServerReadinessHandler implements Consumer<String> {
    private String machineName;

    public ServerReadinessHandler(String machineName) {
      this.machineName = machineName;
    }

    @Override
    public void accept(String serverRef) {
      DockerMachine machine = runtimeMachines.getMachine(machineName);
      if (machine == null) {
        // Probably machine was removed from the list during server check start due to some reason
        return;
      }
      machine.setServerStatus(serverRef, ServerStatus.RUNNING);

      eventService.publish(
          DtoFactory.newDto(ServerStatusEvent.class)
              .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
              .withMachineName(machineName)
              .withServerName(serverRef)
              .withStatus(ServerStatus.RUNNING)
              .withServerUrl(machine.getServers().get(serverRef).getUrl()));
    }
  }

  private class ServerLivenessHandler implements Consumer<ProbeResult> {
    @Override
    public void accept(ProbeResult probeResult) {
      String machineName = probeResult.getMachineName();
      DockerMachine machine = runtimeMachines.getMachine(machineName);
      if (machine == null) {
        // Probably machine was removed from the list during server check start due to some reason
        return;
      }
      String serverName = probeResult.getServerName();
      ProbeStatus probeStatus = probeResult.getStatus();
      Server server = machine.getServers().get(serverName);
      ServerStatus oldServerStatus = server.getStatus();
      ServerStatus serverStatus;

      if (probeStatus == ProbeStatus.FAILED && oldServerStatus == ServerStatus.RUNNING) {
        serverStatus = ServerStatus.STOPPED;
      } else if (probeStatus == ProbeStatus.PASSED && (oldServerStatus != ServerStatus.RUNNING)) {
        serverStatus = ServerStatus.RUNNING;
      } else {
        return;
      }

      machine.setServerStatus(serverName, serverStatus);
      sendServerStatusEvent(machineName, serverName, machine.getServers().get(serverName));
    }
  }

  private void destroyRuntime(Map<String, String> stopOptions) throws InfrastructureException {
    Map<String, DockerMachine> machines = runtimeMachines.removeMachines();
    for (Map.Entry<String, DockerMachine> entry : machines.entrySet()) {
      destroyMachineQuietly(entry.getKey(), entry.getValue());
      sendStoppedEvent(entry.getKey());
    }
    // TODO what happens when context throws exception here
    networks.destroyNetwork(getContext().getEnvironment().getNetwork());
  }

  /** Destroys specified machine with suppressing exception that occurs while destroying. */
  private void destroyMachineQuietly(String machineName, DockerMachine machine) {
    try {
      machine.destroy();
    } catch (InfrastructureException e) {
      LOG.error(
          format(
              "Error occurs on destroying of docker machine '%s' in workspace '%s'. Error: %s",
              machineName, getContext().getIdentity().getWorkspaceId(), e.getMessage()),
          e);
    }
  }

  private class AbnormalMachineStopHandlerImpl implements AbnormalMachineStopHandler {
    @Override
    public void handle(String error) {
      try {
        internalStop(emptyMap());
      } catch (InfrastructureException e) {
        LOG.error(e.getLocalizedMessage(), e);
      } finally {
        eventService.publish(
            DtoFactory.newDto(RuntimeStatusEvent.class)
                .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
                .withStatus("STOPPED")
                .withPrevStatus("RUNNING")
                .withFailed(true)
                .withError(error));
      }
    }
  }

  private void sendStartingEvent(String machineName) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withEventType(MachineStatus.STARTING)
            .withMachineName(machineName));
  }

  private void sendRunningEvent(String machineName) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withEventType(MachineStatus.RUNNING)
            .withMachineName(machineName));
  }

  private void sendFailedEvent(String machineName, String message) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withEventType(MachineStatus.FAILED)
            .withMachineName(machineName)
            .withError(message));
  }

  private void sendStoppedEvent(String machineName) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withEventType(MachineStatus.STOPPED)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withMachineName(machineName));
  }

  private void sendServerStatusEvent(String machineName, String serverName, Server server) {
    eventService.publish(
        DtoFactory.newDto(ServerStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withMachineName(machineName)
            .withServerName(serverName)
            .withStatus(server.getStatus())
            .withServerUrl(server.getUrl()));
  }
}

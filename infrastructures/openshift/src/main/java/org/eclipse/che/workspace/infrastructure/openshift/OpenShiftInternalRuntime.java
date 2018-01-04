/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Watcher.Action;
import io.fabric8.openshift.api.model.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter.NoOpURLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult.ProbeStatus;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.dto.event.MachineLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.openshift.bootstrapper.OpenShiftBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.project.event.ContainerEvent;
import org.eclipse.che.workspace.infrastructure.openshift.project.event.ContainerEventHandler;
import org.eclipse.che.workspace.infrastructure.openshift.project.event.PodActionHandler;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.WorkspaceVolumesStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class OpenShiftInternalRuntime extends InternalRuntime<OpenShiftRuntimeContext> {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftInternalRuntime.class);

  private static final String RUNTIME_STOPPED_STATE = "STOPPED";
  private static final String RUNTIME_RUNNING_STATE = "RUNNING";
  private static final String POD_FAILED_STATUS = "Failed";

  private final EventService eventService;
  private final ServersCheckerFactory serverCheckerFactory;
  private final OpenShiftBootstrapperFactory bootstrapperFactory;
  private final Map<String, OpenShiftMachine> machines;
  private final int machineStartTimeoutMin;
  private final ProbeScheduler probeScheduler;
  private final WorkspaceProbesFactory probesFactory;
  private final OpenShiftProject project;
  private final WorkspaceVolumesStrategy volumesStrategy;

  @Inject
  public OpenShiftInternalRuntime(
      @Named("che.infra.openshift.machine_start_timeout_min") int machineStartTimeoutMin,
      NoOpURLRewriter urlRewriter,
      EventService eventService,
      OpenShiftBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      WorkspaceVolumesStrategy volumesStrategy,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      @Assisted OpenShiftRuntimeContext context,
      @Assisted OpenShiftProject project,
      @Assisted List<Warning> warnings) {
    super(context, urlRewriter, warnings, false);
    this.eventService = eventService;
    this.bootstrapperFactory = bootstrapperFactory;
    this.serverCheckerFactory = serverCheckerFactory;
    this.volumesStrategy = volumesStrategy;
    this.machineStartTimeoutMin = machineStartTimeoutMin;
    this.probeScheduler = probeScheduler;
    this.probesFactory = probesFactory;
    this.project = project;
    this.machines = new ConcurrentHashMap<>();
  }

  @Override
  protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
    OpenShiftRuntimeContext context = getContext();
    String workspaceId = context.getIdentity().getWorkspaceId();
    try {
      final OpenShiftEnvironment osEnv = context.getEnvironment();
      volumesStrategy.prepare(osEnv, workspaceId);

      List<Service> createdServices = new ArrayList<>();
      for (Service service : osEnv.getServices().values()) {
        createdServices.add(project.services().create(service));
      }

      List<Route> createdRoutes = new ArrayList<>();
      for (Route route : osEnv.getRoutes().values()) {
        createdRoutes.add(project.routes().create(route));
      }
      // TODO https://github.com/eclipse/che/issues/7653
      // project.pods().watch(new AbnormalStopHandler());
      // project.pods().watchContainers(new MachineLogsPublisher());

      createPods(createdServices, createdRoutes);

      // TODO Rework it to parallel waiting https://github.com/eclipse/che/issues/7067
      for (OpenShiftMachine machine : machines.values()) {
        try {
          machine.waitRunning(machineStartTimeoutMin);
          bootstrapMachine(machine);
          checkMachineServers(machine);
          machine.setStatus(MachineStatus.RUNNING);
          sendRunningEvent(machine.getName());
        } catch (InfrastructureException rethrow) {
          sendFailedEvent(machine.getName(), rethrow.getMessage());
          throw rethrow;
        }
      }
    } catch (InfrastructureException | RuntimeException | InterruptedException e) {
      LOG.warn(
          "Failed to start OpenShift runtime of workspace {}. Cause: {}",
          workspaceId,
          e.getMessage());
      boolean interrupted = Thread.interrupted() || e instanceof InterruptedException;
      // Cancels workspace servers probes if any
      probeScheduler.cancel(workspaceId);
      try {
        project.cleanUp();
      } catch (InfrastructureException ignored) {
      }
      if (interrupted) {
        throw new InfrastructureException("OpenShift environment start was interrupted");
      }
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
  public Map<String, ? extends Machine> getInternalMachines() {
    return ImmutableMap.copyOf(machines);
  }

  @Override
  protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
    // Cancels workspace servers probes if any
    probeScheduler.cancel(getContext().getIdentity().getWorkspaceId());
    project.cleanUp();
  }

  @Override
  public Map<String, String> getProperties() {
    return emptyMap();
  }

  /**
   * Bootstraps machine.
   *
   * @param machine the OpenShift machine instance to bootstrap
   * @throws InfrastructureException when any error occurs while bootstrapping machine
   * @throws InterruptedException when machine bootstrapping was interrupted
   */
  private void bootstrapMachine(OpenShiftMachine machine)
      throws InfrastructureException, InterruptedException {
    InternalMachineConfig machineConfig =
        getContext().getEnvironment().getMachines().get(machine.getName());
    if (machineConfig != null && !machineConfig.getInstallers().isEmpty())
      bootstrapperFactory
          .create(getContext().getIdentity(), machineConfig.getInstallers(), machine)
          .bootstrap();
  }

  /**
   * Checks whether machine servers are ready.
   *
   * @param machine the OpenShift machine instance
   * @throws InfrastructureException when any error while server checks occur
   * @throws InterruptedException when process of server check was interrupted
   */
  private void checkMachineServers(OpenShiftMachine machine)
      throws InfrastructureException, InterruptedException {
    final ServersChecker check =
        serverCheckerFactory.create(
            getContext().getIdentity(), machine.getName(), machine.getServers());
    check.startAsync(new ServerReadinessHandler(machine.getName()));
    check.await();

    probeScheduler.schedule(
        probesFactory.getProbes(
            getContext().getIdentity().getWorkspaceId(), machine.getName(), machine.getServers()),
        new ServerLivenessHandler());
  }

  /**
   * Creates OpenShift pods and resolves machine servers based on routes and services.
   *
   * @param services created OpenShift services
   * @param routes created OpenShift routes
   * @throws InfrastructureException when any error occurs while creating OpenShift pods
   */
  @VisibleForTesting
  void createPods(List<Service> services, List<Route> routes) throws InfrastructureException {
    final ServerResolver serverResolver = ServerResolver.of(services, routes);
    for (Pod toCreate : getContext().getEnvironment().getPods().values()) {
      final Pod createdPod = project.pods().create(toCreate);
      final ObjectMeta podMetadata = createdPod.getMetadata();
      for (Container container : createdPod.getSpec().getContainers()) {
        String machineName = Names.machineName(toCreate, container);
        OpenShiftMachine machine =
            new OpenShiftMachine(
                machineName,
                podMetadata.getName(),
                container.getName(),
                serverResolver.resolve(machineName),
                project);
        machines.put(machine.getName(), machine);
        sendStartingEvent(machine.getName());
      }
    }
  }

  private class ServerReadinessHandler implements Consumer<String> {
    private String machineName;

    ServerReadinessHandler(String machineName) {
      this.machineName = machineName;
    }

    @Override
    public void accept(String serverRef) {
      final OpenShiftMachine machine = machines.get(machineName);
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
      OpenShiftMachine machine = machines.get(machineName);
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

  private void sendRuntimeStoppedEvent(String errorMsg) {
    eventService.publish(
        DtoFactory.newDto(RuntimeStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withStatus(RUNTIME_STOPPED_STATE)
            .withPrevStatus(RUNTIME_RUNNING_STATE)
            .withFailed(true)
            .withError(errorMsg));
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

  /** Listens container's events and publish them as machine logs. */
  class MachineLogsPublisher implements ContainerEventHandler {

    @Override
    public void handle(ContainerEvent event) {
      final String podName = event.getPodName();
      final String containerName = event.getContainerName();
      for (Entry<String, OpenShiftMachine> entry : machines.entrySet()) {
        final OpenShiftMachine machine = entry.getValue();
        if (machine.getPodName().equals(podName)
            && machine.getContainerName().equals(containerName)) {
          eventService.publish(
              DtoFactory.newDto(MachineLogEvent.class)
                  .withMachineName(entry.getKey())
                  .withRuntimeId(DtoConverter.asDto(getContext().getIdentity()))
                  .withText(event.getMessage())
                  .withTime(event.getTime()));
          return;
        }
      }
    }
  }

  /** Stops runtime if one of the pods was abnormally stopped. */
  class AbnormalStopHandler implements PodActionHandler {

    @Override
    public void handle(Action action, Pod pod) {
      // Cancels workspace servers probes if any
      probeScheduler.cancel(getContext().getIdentity().getWorkspaceId());
      if (pod.getStatus() != null && POD_FAILED_STATUS.equals(pod.getStatus().getPhase())) {
        try {
          internalStop(emptyMap());
        } catch (InfrastructureException ex) {
          LOG.error("OpenShift environment stop failed cause '{}'", ex.getMessage());
        } finally {
          sendRuntimeStoppedEvent(
              format("Pod '%s' was abnormally stopped", pod.getMetadata().getName()));
        }
      }
    }
  }
}

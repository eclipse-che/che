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
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
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
import org.eclipse.che.workspace.infrastructure.docker.server.mapping.ExternalIpURLRewriter;
import org.slf4j.Logger;

/** @author Alexander Garagatyi */
public class DockerInternalRuntime extends InternalRuntime<DockerRuntimeContext> {

  private static final Logger LOG = getLogger(DockerInternalRuntime.class);

  private final StartSynchronizer startSynchronizer;
  private final Map<String, String> properties;
  private final NetworkLifecycle networks;
  private final DockerMachineStarter containerStarter;
  private final EventService eventService;
  private final DockerBootstrapperFactory bootstrapperFactory;
  private final ServersCheckerFactory serverCheckerFactory;
  private final MachineLoggersFactory loggers;

  /**
   * Creates non running runtime. Normally created by {@link
   * DockerRuntimeFactory#create(DockerRuntimeContext)}.
   */
  @AssistedInject
  public DockerInternalRuntime(
      @Assisted DockerRuntimeContext context,
      ExternalIpURLRewriter urlRewriter,
      NetworkLifecycle networks,
      DockerMachineStarter machineStarter,
      EventService eventService,
      DockerBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      MachineLoggersFactory loggers) {
    this(
        context,
        urlRewriter,
        false, // <- non running
        networks,
        machineStarter,
        eventService,
        bootstrapperFactory,
        serverCheckerFactory,
        loggers);
  }

  /**
   * Creates a running runtime from the list of given containers. Normally created by {@link
   * DockerRuntimeFactory#create(DockerRuntimeContext, List)}.
   */
  @AssistedInject
  public DockerInternalRuntime(
      @Assisted DockerRuntimeContext context,
      @Assisted List<ContainerListEntry> containers,
      ExternalIpURLRewriter urlRewriter,
      NetworkLifecycle networks,
      DockerMachineStarter machineStarter,
      EventService eventService,
      DockerBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      MachineLoggersFactory loggers,
      DockerMachineCreator machineCreator,
      DockerMachineStopDetector stopDetector)
      throws InfrastructureException {
    this(
        context,
        urlRewriter,
        true, // <- running
        networks,
        machineStarter,
        eventService,
        bootstrapperFactory,
        serverCheckerFactory,
        loggers);

    for (ContainerListEntry container : containers) {
      DockerMachine machine = machineCreator.create(container);
      String name = Labels.newDeserializer(container.getLabels()).machineName();

      startSynchronizer.addMachine(name, machine);
      stopDetector.startDetection(container.getId(), name, new AbnormalMachineStopHandlerImpl());
      streamLogsAsync(name, container.getId());
    }
  }

  private DockerInternalRuntime(
      DockerRuntimeContext context,
      URLRewriter urlRewriter,
      boolean running,
      NetworkLifecycle networks,
      DockerMachineStarter machineStarter,
      EventService eventService,
      DockerBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      MachineLoggersFactory loggers) {
    super(context, urlRewriter, running);
    this.networks = networks;
    this.containerStarter = machineStarter;
    this.eventService = eventService;
    this.bootstrapperFactory = bootstrapperFactory;
    this.serverCheckerFactory = serverCheckerFactory;
    this.properties = new HashMap<>();
    this.startSynchronizer = new StartSynchronizer();
    this.loggers = loggers;
  }

  @Override
  protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
    startSynchronizer.setStartThread();
    Map<String, DockerContainerConfig> machineName2config =
        getContext().getDockerEnvironment().getContainers();
    try {
      networks.createNetwork(getContext().getDockerEnvironment().getNetwork());

      for (String machineName : getContext().getOrderedContainers()) {
        checkInterruption();
        final DockerContainerConfig config = machineName2config.get(machineName);
        sendStartingEvent(machineName);
        try {
          startMachine(machineName, config);
          sendRunningEvent(machineName);
        } catch (InfrastructureException e) {
          sendFailedEvent(machineName, e.getMessage());
          throw e;
        }
      }
      startSynchronizer.complete();
    } catch (InfrastructureException | InterruptedException | RuntimeException e) {
      boolean interrupted = Thread.interrupted() || e instanceof InterruptedException;
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
    return startSynchronizer
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
        startSynchronizer.getMachines().entrySet()) {
      String name = entry.getKey();
      DockerMachine machine = entry.getValue();
      ServersChecker checker =
          serverCheckerFactory.create(getContext().getIdentity(), name, machine.getServers());
      checker.checkOnce(new ServerReadinessHandler(name));
    }
  }

  private void startMachine(String name, DockerContainerConfig containerConfig)
      throws InfrastructureException, InterruptedException {
    InternalMachineConfig machineCfg = getContext().getEnvironment().getMachines().get(name);

    DockerMachine machine =
        containerStarter.startContainer(
            getContext().getDockerEnvironment().getNetwork(),
            name,
            containerConfig,
            getContext().getIdentity(),
            new AbnormalMachineStopHandlerImpl());
    try {
      startSynchronizer.addMachine(name, machine);
    } catch (InfrastructureException e) {
      // destroy machine only in case its addition fails
      // in other cases cleanup of whole runtime will be performed
      destroyMachineQuietly(name, machine);
      throw e;
    }
    if (!machineCfg.getInstallers().isEmpty()) {
      bootstrapperFactory
          .create(name, getContext().getIdentity(), machineCfg.getInstallers(), machine)
          .bootstrap();
    }

    checkInterruption();
    ServersChecker readinessChecker =
        serverCheckerFactory.create(getContext().getIdentity(), name, machine.getServers());
    readinessChecker.startAsync(new ServerReadinessHandler(name));
    readinessChecker.await();
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
      DockerMachine machine = startSynchronizer.getMachines().get(machineName);
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

  private void destroyRuntime(Map<String, String> stopOptions) throws InfrastructureException {
    Map<String, DockerMachine> machines = startSynchronizer.removeMachines();
    for (Map.Entry<String, DockerMachine> entry : machines.entrySet()) {
      destroyMachineQuietly(entry.getKey(), entry.getValue());
      sendStoppedEvent(entry.getKey());
    }
    // TODO what happens when context throws exception here
    networks.destroyNetwork(getContext().getDockerEnvironment().getNetwork());
  }

  /** Destroys specified machine with suppressing exception that occurs while destroying. */
  private void destroyMachineQuietly(String machineName, DockerMachine machine) {
    try {
      machine.destroy();
    } catch (InfrastructureException e) {
      LOG.error(
          format(
              "Error occurs on destroying of docker machine '%s' in workspace '%s'. Container '%s'",
              machineName, getContext().getIdentity().getWorkspaceId(), machine.getContainer()),
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

  /**
   * Controls the runtime start flow and helps to cancel it.
   *
   * <p>The runtime start with cancellation using the Start Synchronizer might look like:
   *
   * <pre>
   * ...
   *     public void startRuntime() {
   *          startSynchronizer.setStartThread();
   *          try {
   *               // .....
   *               startSynchronizer.complete();
   *          } catch (Exception ex) {
   *               startSynchronizer.completeExceptionally(ex);
   *               throw ex;
   *          }
   *     }
   * ...
   * </pre>
   *
   * <p>At the same time stopping might look like:
   *
   * <pre>
   * ...
   *     public void stopRuntime() {
   *          if (startSynchronizer.interrupt()) {
   *               try {
   *                  startSynchronizer.await();
   *               } catch (RuntimeStartInterruptedException ex) {
   *                  // normal stop
   *               } catch (InterruptedException ex) {
   *                  Thread.currentThread().interrupt();
   *                  ...
   *               }
   *          }
   *     }
   * ...
   * </pre>
   */
  static class StartSynchronizer {

    private Exception exception;
    private Thread startThread;
    private Map<String, DockerMachine> machines;
    private CountDownLatch completionLatch;

    public StartSynchronizer() {
      this.machines = new HashMap<>();
      this.completionLatch = new CountDownLatch(1);
    }

    public synchronized Map<String, ? extends DockerMachine> getMachines() {
      return machines != null ? machines : emptyMap();
    }

    public synchronized void addMachine(String name, DockerMachine machine)
        throws InternalInfrastructureException {
      if (machines != null) {
        machines.put(name, machine);
      } else {
        throw new InternalInfrastructureException("Start of runtime is canceled.");
      }
    }

    public synchronized Map<String, DockerMachine> removeMachines() throws InfrastructureException {
      if (machines != null) {
        Map<String, DockerMachine> machines = this.machines;
        // unset to identify error if method called second time
        this.machines = null;
        return machines;
      }
      throw new InfrastructureException("Runtime doesn't have machines to remove");
    }

    /**
     * Sets {@link Thread#currentThread()} as a {@link #startThread}.
     *
     * @throws InternalInfrastructureException when {@link #startThread} already setted.
     */
    public synchronized void setStartThread() throws InternalInfrastructureException {
      if (startThread != null) {
        throw new InternalInfrastructureException(
            "Docker infrastructure context of workspace already started");
      }
      startThread = Thread.currentThread();
    }

    /**
     * Releases waiting task and reset the starting thread.
     *
     * @throws InterruptedException when execution thread was interrupted just before this method
     *     call
     */
    public synchronized void complete() throws InterruptedException {
      if (Thread.currentThread().isInterrupted()) {
        throw new InterruptedException();
      }
      startThread = null;
      completionLatch.countDown();
    }

    /**
     * Releases waiting task, reset the starting thread and sets an exception if it is not null.
     *
     * @param ex completion exception might be null
     */
    public synchronized void completeExceptionally(Exception ex) {
      exception = ex;
      startThread = null;
      completionLatch.countDown();
    }

    /**
     * Interrupts the {@link #startThread} if its value different to null.
     *
     * @return true if {@link #startThread} interruption flag setted, otherwise false will be
     *     returned
     */
    public synchronized boolean interrupt() {
      if (startThread != null) {
        startThread.interrupt();
        return true;
      }
      return false;
    }

    /**
     * Waits until {@link #complete} is called and rethrow the {@link #exception} if it present.
     * This call is blocking and it should be used with {@link #interrupt} method.
     *
     * @throws InterruptedException when this thread is interrupted while waiting for {@link
     *     #complete}
     * @throws RuntimeStartInterruptedException when {@link #startThread} successfully interrupted
     * @throws InfrastructureException when any error occurs while waiting for {@link #complete}
     */
    public void await() throws InterruptedException, InfrastructureException {
      completionLatch.await();
      synchronized (this) {
        if (exception != null) {
          try {
            throw exception;
          } catch (InfrastructureException rethrow) {
            throw rethrow;
          } catch (Exception ex) {
            throw new InternalInfrastructureException(ex);
          }
        }
      }
    }
  }
}

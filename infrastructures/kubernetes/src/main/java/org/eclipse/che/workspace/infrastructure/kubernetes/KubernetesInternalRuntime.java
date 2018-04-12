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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.Watcher.Action;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
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
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.ContainerEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.ContainerEventHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodActionHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerResolver;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class KubernetesInternalRuntime<
        T extends KubernetesRuntimeContext<? extends KubernetesEnvironment>>
    extends InternalRuntime<T> {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesInternalRuntime.class);

  private static final String POD_FAILED_STATUS = "Failed";

  private final int workspaceStartTimeout;
  private final int ingressStartTimeout;
  private final ServersCheckerFactory serverCheckerFactory;
  private final KubernetesBootstrapperFactory bootstrapperFactory;
  private final ProbeScheduler probeScheduler;
  private final WorkspaceProbesFactory probesFactory;
  private final KubernetesNamespace namespace;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final RuntimeEventsPublisher eventPublisher;
  private final Executor executor;

  protected final Map<String, KubernetesMachine> machines;

  @Inject
  public KubernetesInternalRuntime(
      @Named("che.infra.kubernetes.workspace_start_timeout_min") int workspaceStartTimeout,
      @Named("che.infra.kubernetes.ingress_start_timeout_min") int ingressStartTimeout,
      NoOpURLRewriter urlRewriter,
      KubernetesBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      WorkspaceVolumesStrategy volumesStrategy,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      RuntimeEventsPublisher eventPublisher,
      KubernetesSharedPool sharedPool,
      @Assisted T context,
      @Assisted KubernetesNamespace namespace,
      @Assisted List<Warning> warnings) {
    super(context, urlRewriter, warnings);
    this.bootstrapperFactory = bootstrapperFactory;
    this.serverCheckerFactory = serverCheckerFactory;
    this.volumesStrategy = volumesStrategy;
    this.workspaceStartTimeout = workspaceStartTimeout;
    this.ingressStartTimeout = ingressStartTimeout;
    this.probeScheduler = probeScheduler;
    this.probesFactory = probesFactory;
    this.namespace = namespace;
    this.eventPublisher = eventPublisher;
    this.executor = sharedPool.getExecutor();
    this.machines = new ConcurrentHashMap<>();
  }

  @Override
  protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
    KubernetesRuntimeContext<? extends KubernetesEnvironment> context = getContext();
    String workspaceId = context.getIdentity().getWorkspaceId();
    try {
      final KubernetesEnvironment k8sEnv = context.getEnvironment();
      volumesStrategy.prepare(k8sEnv, workspaceId);
      startMachines();

      final CompletableFuture<Void> failure = new CompletableFuture<>();
      final List<CompletableFuture<Void>> machinesFutures = new ArrayList<>();
      // futures that must be cancelled explicitly
      final List<CompletableFuture<?>> toCancelFutures = new CopyOnWriteArrayList<>();
      final EnvironmentContext currentContext = EnvironmentContext.getCurrent();

      for (KubernetesMachine machine : machines.values()) {
        final CompletableFuture<Void> machineRunningFuture = machine.waitRunningAsync();
        toCancelFutures.add(machineRunningFuture);
        final CompletableFuture<Void> machineBootChain =
            machineRunningFuture
                // since machine running future will be completed from the thread that is not from
                // kubernetes pool it's needed to explicitly put the executor to not to delay
                // processing in the external pool.
                .thenComposeAsync(checkFailure(failure), executor)
                .thenRun(publishRunningStatus(machine))
                .thenCompose(checkFailure(failure))
                .thenCompose(setContext(currentContext, bootstrap(toCancelFutures, machine)))
                // see comments above why executor is explicitly put into arguments
                .thenComposeAsync(checkFailure(failure), executor)
                .thenCompose(setContext(currentContext, checkServers(toCancelFutures, machine)))
                .exceptionally(publishFailedStatus(failure, machine));
        machinesFutures.add(machineBootChain);
      }

      waitMachines(machinesFutures, toCancelFutures, failure);
    } catch (InfrastructureException | RuntimeException | InterruptedException e) {
      LOG.warn(
          "Failed to start Kubernetes runtime of workspace {}. Cause: {}",
          workspaceId,
          e.getMessage());
      boolean interrupted = Thread.interrupted() || e instanceof InterruptedException;
      // Cancels workspace servers probes if any
      probeScheduler.cancel(workspaceId);
      try {
        namespace.cleanUp();
      } catch (InfrastructureException ignored) {
      }
      if (interrupted) {
        throw new InfrastructureException("Kubernetes environment start was interrupted");
      }
      wrapAndRethrow(e);
    }
  }

  /** Returns new function that wraps given with set/unset context logic */
  private <T, R> Function<T, R> setContext(EnvironmentContext context, Function<T, R> func) {
    return funcArgument -> {
      try {
        EnvironmentContext.setCurrent(context);
        return func.apply(funcArgument);
      } finally {
        EnvironmentContext.reset();
      }
    };
  }

  /**
   * Waits for readiness of given machines.
   *
   * @param machinesFutures machines futures to wait
   * @param toCancelFutures futures that must be explicitly closed when any error occurs
   * @param failure failure callback that is used to prevent subsequent steps when any error occurs
   * @throws InfrastructureException when waiting for machines exceeds the timeout
   * @throws InfrastructureException when any problem occurred while waiting
   * @throws InterruptedException when the thread is interrupted while waiting machines
   */
  private void waitMachines(
      List<CompletableFuture<Void>> machinesFutures,
      List<CompletableFuture<?>> toCancelFutures,
      CompletableFuture<Void> failure)
      throws InfrastructureException, InterruptedException {
    try {
      final CompletableFuture<Void> allDone =
          CompletableFuture.allOf(
              machinesFutures.toArray(new CompletableFuture[machinesFutures.size()]));
      CompletableFuture.anyOf(allDone, failure).get(workspaceStartTimeout, TimeUnit.MINUTES);

      if (failure.isCompletedExceptionally()) {
        // rethrow the failure cause
        failure.get();
      }
    } catch (TimeoutException ex) {
      failure.completeExceptionally(ex);
      cancelAll(toCancelFutures);
      throw new InfrastructureException(
          "Waiting for Kubernetes environment '"
              + getContext().getIdentity().getEnvName()
              + "' of the workspace'"
              + getContext().getIdentity().getWorkspaceId()
              + "' reached timeout");
    } catch (InterruptedException ex) {
      failure.completeExceptionally(ex);
      cancelAll(toCancelFutures);
      throw ex;
    } catch (ExecutionException ex) {
      failure.completeExceptionally(ex);
      cancelAll(toCancelFutures);
      wrapAndRethrow(ex.getCause());
    }
  }

  /**
   * Returns a function, the result of which the completable stage that performs servers checks and
   * start of servers probes.
   */
  private Function<Void, CompletionStage<Void>> checkServers(
      List<CompletableFuture<?>> toCancelFutures, KubernetesMachine machine) {
    return ignored -> {
      // This completable future is used to unity the servers checks and start of probes
      final CompletableFuture<Void> serversAndProbesFuture = new CompletableFuture<>();
      final String machineName = machine.getName();
      final RuntimeIdentity runtimeId = getContext().getIdentity();
      final ServersChecker serverCheck =
          serverCheckerFactory.create(runtimeId, machineName, machine.getServers());
      final CompletableFuture<?> serversReadyFuture;
      try {
        serversReadyFuture = serverCheck.startAsync(new ServerReadinessHandler(machineName));
        toCancelFutures.add(serversReadyFuture);
        serversAndProbesFuture.whenComplete((ok, ex) -> serversReadyFuture.cancel(true));
      } catch (InfrastructureException ex) {
        serversAndProbesFuture.completeExceptionally(ex);
        return serversAndProbesFuture;
      }
      serversReadyFuture.whenComplete(
          (BiConsumer<Object, Throwable>)
              (ok, ex) -> {
                if (ex != null) {
                  serversAndProbesFuture.completeExceptionally(ex);
                  return;
                }
                try {
                  probeScheduler.schedule(
                      probesFactory.getProbes(runtimeId, machineName, machine.getServers()),
                      new ServerLivenessHandler());
                } catch (InfrastructureException iex) {
                  serversAndProbesFuture.completeExceptionally(iex);
                }
                serversAndProbesFuture.complete(null);
              });
      return serversAndProbesFuture;
    };
  }

  /**
   * Returns the function the result of which the completable stage that informs about bootstrapping
   * of the machine. Note that when the given machine does not contain installers then the result of
   * this function will be completed stage.
   */
  private Function<Void, CompletionStage<Void>> bootstrap(
      List<CompletableFuture<?>> toCancelFutures, KubernetesMachine machine) {
    return ignored -> {
      // think about to return copy of machines in environment
      final InternalMachineConfig machineConfig =
          getContext().getEnvironment().getMachines().get(machine.getName());
      final CompletableFuture<Void> bootstrapperFuture;
      if (!machineConfig.getInstallers().isEmpty()) {
        bootstrapperFuture =
            bootstrapperFactory
                .create(getContext().getIdentity(), machineConfig.getInstallers(), machine)
                .bootstrapAsync();
        toCancelFutures.add(bootstrapperFuture);
      } else {
        bootstrapperFuture = CompletableFuture.completedFuture(null);
      }
      return bootstrapperFuture;
    };
  }

  /**
   * Note that if this invocation caused a transition of failure to a completed state then
   * notification about machine start failed will be published.
   */
  private Function<Throwable, Void> publishFailedStatus(
      CompletableFuture<Void> failure, KubernetesMachine machine) {
    return ex -> {
      if (failure.completeExceptionally(ex)) {
        eventPublisher.sendFailedEvent(
            machine.getName(), ex.getMessage(), getContext().getIdentity());
      }
      return null;
    };
  }

  /** Returns instance of {@link Runnable} that propagate machine state. */
  private Runnable publishRunningStatus(KubernetesMachine machine) {
    return () -> {
      machine.setStatus(MachineStatus.RUNNING);
      eventPublisher.sendRunningEvent(machine.getName(), getContext().getIdentity());
    };
  }

  /** Returns the function that indicates whether a failure occurred or not. */
  private static <T> Function<T, CompletionStage<Void>> checkFailure(
      CompletableFuture<Void> failure) {
    return ignored -> {
      if (failure.isCompletedExceptionally()) {
        return failure;
      }
      return CompletableFuture.completedFuture(null);
    };
  }

  /** Cancels all the given futures */
  private static void cancelAll(Collection<CompletableFuture<?>> toClose) {
    toClose.forEach(cancelled -> cancelled.cancel(true));
  }

  @Override
  public Map<String, ? extends KubernetesMachine> getInternalMachines() {
    return ImmutableMap.copyOf(machines);
  }

  @Override
  protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
    // Cancels workspace servers probes if any
    probeScheduler.cancel(getContext().getIdentity().getWorkspaceId());
    namespace.cleanUp();
  }

  @Override
  public Map<String, String> getProperties() {
    return emptyMap();
  }

  /**
   * Create all machine related objects and start machines.
   *
   * @throws InfrastructureException when any error occurs while creating Kubernetes objects
   */
  protected void startMachines() throws InfrastructureException {
    KubernetesEnvironment k8sEnv = getContext().getEnvironment();
    List<Service> createdServices = new ArrayList<>();
    for (Service service : k8sEnv.getServices().values()) {
      createdServices.add(namespace.services().create(service));
    }

    // needed for resolution later on, even though n routes are actually created by ingress
    // /workspace{wsid}/server-{port} => service({wsid}):server-port => pod({wsid}):{port}
    List<Ingress> readyIngresses = createAndWaitReady(k8sEnv.getIngresses().values());

    // TODO https://github.com/eclipse/che/issues/7653
    // namespace.pods().watch(new AbnormalStopHandler());
    // namespace.pods().watchContainers(new MachineLogsPublisher());

    final KubernetesServerResolver serverResolver =
        new KubernetesServerResolver(createdServices, readyIngresses);

    doStartMachine(serverResolver);
  }

  /**
   * Creates Kubernetes pods and resolves servers using the specified serverResolver.
   *
   * @param serverResolver server resolver that provide servers by container
   * @throws InfrastructureException when any error occurs while creating Kubernetes pods
   */
  protected void doStartMachine(KubernetesServerResolver serverResolver)
      throws InfrastructureException {
    final KubernetesEnvironment environment = getContext().getEnvironment();
    final Map<String, InternalMachineConfig> machineConfigs = environment.getMachines();
    for (Pod toCreate : environment.getPods().values()) {
      final Pod createdPod = namespace.pods().create(toCreate);
      final ObjectMeta podMetadata = createdPod.getMetadata();
      for (Container container : createdPod.getSpec().getContainers()) {
        String machineName = Names.machineName(toCreate, container);
        KubernetesMachine machine =
            new KubernetesMachine(
                machineName,
                podMetadata.getName(),
                container.getName(),
                serverResolver.resolve(machineName),
                namespace,
                MachineStatus.STARTING,
                machineConfigs.get(machineName).getAttributes());
        machines.put(machine.getName(), machine);
        eventPublisher.sendStartingEvent(machine.getName(), getContext().getIdentity());
      }
    }
  }

  private List<Ingress> createAndWaitReady(Collection<Ingress> ingresses)
      throws InfrastructureException {
    List<Ingress> createdIngresses = new ArrayList<>();
    for (Ingress ingress : ingresses) {
      createdIngresses.add(namespace.ingresses().create(ingress));
    }

    // wait for LB ip
    List<Ingress> readyIngresses = new ArrayList<>();
    for (Ingress ingress : createdIngresses) {
      Ingress actualIngress =
          namespace
              .ingresses()
              .wait(
                  ingress.getMetadata().getName(),
                  ingressStartTimeout,
                  p -> (!p.getStatus().getLoadBalancer().getIngress().isEmpty()));
      readyIngresses.add(actualIngress);
    }

    return readyIngresses;
  }

  /**
   * When origin exception is not instance of infrastructure exception then it would be wrapped and
   * rethrown.
   */
  private static void wrapAndRethrow(Throwable origin) throws InfrastructureException {
    try {
      throw origin;
    } catch (InfrastructureException rethrow) {
      throw rethrow;
    } catch (Throwable cause) {
      throw new InternalInfrastructureException(cause.getMessage(), cause);
    }
  }

  private class ServerReadinessHandler implements Consumer<String> {

    private String machineName;

    ServerReadinessHandler(String machineName) {
      this.machineName = machineName;
    }

    @Override
    public void accept(String serverRef) {
      final KubernetesMachine machine = machines.get(machineName);
      if (machine == null) {
        // Probably machine was removed from the list during server check start due to some reason
        return;
      }

      machine.setServerStatus(serverRef, ServerStatus.RUNNING);
      eventPublisher.sendServerRunningEvent(
          machineName,
          serverRef,
          machine.getServers().get(serverRef).getUrl(),
          getContext().getIdentity());
    }
  }

  private class ServerLivenessHandler implements Consumer<ProbeResult> {

    @Override
    public void accept(ProbeResult probeResult) {
      String machineName = probeResult.getMachineName();
      KubernetesMachine machine = machines.get(machineName);
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
      eventPublisher.sendServerStatusEvent(
          machineName,
          serverName,
          machine.getServers().get(serverName),
          getContext().getIdentity());
    }
  }

  /** Listens container's events and publish them as machine logs. */
  class MachineLogsPublisher implements ContainerEventHandler {

    @Override
    public void handle(ContainerEvent event) {
      final String podName = event.getPodName();
      final String containerName = event.getContainerName();
      for (Entry<String, KubernetesMachine> entry : machines.entrySet()) {
        final KubernetesMachine machine = entry.getValue();
        if (machine.getPodName().equals(podName)
            && machine.getContainerName().equals(containerName)) {
          eventPublisher.sendMachineLogEnvent(
              entry.getKey(), event.getMessage(), event.getTime(), getContext().getIdentity());
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
          LOG.error("Kubernetes environment stop failed cause '{}'", ex.getMessage());
        } finally {
          eventPublisher.sendRuntimeStoppedEvent(
              format("Pod '%s' was abnormally stopped", pod.getMetadata().getName()),
              getContext().getIdentity());
        }
      }
    }
  }
}

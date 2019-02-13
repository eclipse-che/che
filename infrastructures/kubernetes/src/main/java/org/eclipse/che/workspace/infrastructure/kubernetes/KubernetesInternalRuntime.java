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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_FAILED;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.Watcher.Action;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
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
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.URLRewriter.NoOpURLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult.ProbeStatus;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbes;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.api.workspace.server.spi.StateException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.tracing.OptionalTracer;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodActionHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerResolver;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class KubernetesInternalRuntime<E extends KubernetesEnvironment>
    extends InternalRuntime<KubernetesRuntimeContext<E>> {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesInternalRuntime.class);

  private final int workspaceStartTimeoutMin;
  private final long ingressStartTimeoutMillis;
  private final UnrecoverablePodEventListenerFactory unrecoverableEventListenerFactory;
  private final ServersCheckerFactory serverCheckerFactory;
  private final KubernetesBootstrapperFactory bootstrapperFactory;
  private final ProbeScheduler probeScheduler;
  private final WorkspaceProbesFactory probesFactory;
  private final KubernetesNamespace namespace;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final RuntimeEventsPublisher eventPublisher;
  private final Executor executor;
  private final KubernetesRuntimeStateCache runtimeStates;
  private final KubernetesMachineCache machines;
  private final StartSynchronizer startSynchronizer;
  private final Set<InternalEnvironmentProvisioner> internalEnvironmentProvisioners;
  private final KubernetesEnvironmentProvisioner<E> kubernetesEnvironmentProvisioner;
  private final SidecarToolingProvisioner<E> toolingProvisioner;
  private final RuntimeHangingDetector runtimeHangingDetector;
  @Nullable protected final Tracer tracer;
  private Map<String, Span> machineStartupTraces;

  @Inject
  public KubernetesInternalRuntime(
      @Named("che.infra.kubernetes.workspace_start_timeout_min") int workspaceStartTimeoutMin,
      @Named("che.infra.kubernetes.ingress_start_timeout_min") int ingressStartTimeoutMin,
      NoOpURLRewriter urlRewriter,
      UnrecoverablePodEventListenerFactory unrecoverableEventListenerFactory,
      KubernetesBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      WorkspaceVolumesStrategy volumesStrategy,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      RuntimeEventsPublisher eventPublisher,
      KubernetesSharedPool sharedPool,
      KubernetesRuntimeStateCache runtimeStates,
      KubernetesMachineCache machines,
      StartSynchronizerFactory startSynchronizerFactory,
      Set<InternalEnvironmentProvisioner> internalEnvironmentProvisioners,
      KubernetesEnvironmentProvisioner<E> kubernetesEnvironmentProvisioner,
      SidecarToolingProvisioner<E> toolingProvisioner,
      RuntimeHangingDetector runtimeHangingDetector,
      @Nullable OptionalTracer tracer,
      @Assisted KubernetesRuntimeContext<E> context,
      @Assisted KubernetesNamespace namespace) {
    super(context, urlRewriter);
    this.unrecoverableEventListenerFactory = unrecoverableEventListenerFactory;
    this.bootstrapperFactory = bootstrapperFactory;
    this.serverCheckerFactory = serverCheckerFactory;
    this.volumesStrategy = volumesStrategy;
    this.workspaceStartTimeoutMin = workspaceStartTimeoutMin;
    this.ingressStartTimeoutMillis = TimeUnit.MINUTES.toMillis(ingressStartTimeoutMin);
    this.probeScheduler = probeScheduler;
    this.probesFactory = probesFactory;
    this.namespace = namespace;
    this.eventPublisher = eventPublisher;
    this.executor = sharedPool.getExecutor();
    this.runtimeStates = runtimeStates;
    this.machines = machines;
    this.toolingProvisioner = toolingProvisioner;
    this.kubernetesEnvironmentProvisioner = kubernetesEnvironmentProvisioner;
    this.internalEnvironmentProvisioners = internalEnvironmentProvisioners;
    this.runtimeHangingDetector = runtimeHangingDetector;
    this.startSynchronizer = startSynchronizerFactory.create(context.getIdentity());
    this.tracer = OptionalTracer.fromNullable(tracer);
  }

  @Override
  protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
    KubernetesRuntimeContext<E> context = getContext();
    String workspaceId = context.getIdentity().getWorkspaceId();
    try {
      startSynchronizer.setStartThread();
      startSynchronizer.start();

      // Tooling side car provisioner should be applied before other provisioners
      // because new machines may be provisioned there
      toolingProvisioner.provision(
          context.getIdentity(), startSynchronizer, context.getEnvironment());

      startSynchronizer.checkFailure();

      // Workspace API provisioners should be reapplied here to bring needed
      // changed into new machines that came during tooling provisioning
      for (InternalEnvironmentProvisioner envProvisioner : internalEnvironmentProvisioners) {
        envProvisioner.provision(context.getIdentity(), context.getEnvironment());
      }

      // commands might be updated during provisioning
      runtimeStates.updateCommands(context.getIdentity(), context.getEnvironment().getCommands());

      // Infrastructure specific provisioner should be applied last
      // because it converts all Workspace API model objects that comes
      // from previous provisioners into infrastructure specific objects
      kubernetesEnvironmentProvisioner.provision(context.getEnvironment(), context.getIdentity());

      LOG.debug("Provisioning of workspace '{}' completed.", workspaceId);

      volumesStrategy.prepare(
          context.getEnvironment(), workspaceId, startSynchronizer.getStartTimeoutMillis());

      startSynchronizer.checkFailure();

      startMachines();

      startSynchronizer.checkFailure();

      final Map<String, CompletableFuture<Void>> machinesFutures = new LinkedHashMap<>();
      // futures that must be cancelled explicitly
      final List<CompletableFuture<?>> toCancelFutures = new CopyOnWriteArrayList<>();
      final EnvironmentContext currentContext = EnvironmentContext.getCurrent();
      CompletableFuture<Void> startFailure = startSynchronizer.getStartFailure();

      for (KubernetesMachineImpl machine : machines.getMachines(context.getIdentity()).values()) {
        String machineName = machine.getName();
        final CompletableFuture<Void> machineBootChain =
            waitRunningAsync(toCancelFutures, machine)
                // since machine running future will be completed from the thread that is not from
                // kubernetes pool it's needed to explicitly put the executor to not to delay
                // processing in the external pool.
                .thenComposeAsync(checkFailure(startFailure), executor)
                .thenRun(publishRunningStatus(machineName))
                .thenCompose(checkFailure(startFailure))
                .thenCompose(setContext(currentContext, bootstrap(toCancelFutures, machine)))
                // see comments above why executor is explicitly put into arguments
                .thenComposeAsync(checkFailure(startFailure), executor)
                .thenCompose(setContext(currentContext, checkServers(toCancelFutures, machine)))
                .thenRun(finishStartupTracingSpan(machineName))
                .exceptionally(publishFailedStatusAndRecordFailureTrace(startFailure, machineName));
        machinesFutures.put(machineName, machineBootChain);
      }

      waitMachines(machinesFutures, toCancelFutures, startFailure);
      startSynchronizer.complete();
    } catch (InfrastructureException | RuntimeException e) {
      Exception startFailureCause = startSynchronizer.getStartFailureNow();
      if (startFailureCause == null) {
        startFailureCause = e;
      }

      startSynchronizer.completeExceptionally(startFailureCause);
      LOG.warn(
          "Failed to start Kubernetes runtime of workspace {}. Cause: {}",
          workspaceId,
          startFailureCause.getMessage());
      boolean interrupted =
          Thread.interrupted() || startFailureCause instanceof RuntimeStartInterruptedException;
      // Cancels workspace servers probes if any
      probeScheduler.cancel(workspaceId);
      // stop watching before namespace cleaning up
      namespace.deployments().stopWatch();
      try {
        namespace.cleanUp();
      } catch (InfrastructureException cleanUppingEx) {
        LOG.warn(
            "Failed to clean up namespace after workspace '{}' start failing. Cause: {}",
            context.getIdentity().getWorkspaceId(),
            cleanUppingEx.getMessage());
      }

      if (interrupted) {
        throw new RuntimeStartInterruptedException(getContext().getIdentity());
      }
      wrapAndRethrow(startFailureCause);
    } finally {
      namespace.deployments().stopWatch();
    }
  }

  /**
   * Schedules runtime state checks that are needed after recovering of runtime.
   *
   * <p>Different checks will be scheduled according to current runtime status:
   *
   * <ul>
   *   <li>STARTING - schedules servers checkers and starts tracking of starting runtime
   *   <li>RUNNING - schedules servers checkers
   *   <li>STOPPING - starts tracking of stopping runtime
   *   <li>STOPPED - do nothing. Should not happen since only active runtimes are recovered
   * </ul>
   */
  public void scheduleRuntimeStateChecks() throws InfrastructureException {
    switch (getStatus()) {
      case RUNNING:
        scheduleServersCheckers();
        break;

      case STOPPING:
        runtimeHangingDetector.trackStopping(this, workspaceStartTimeoutMin);
        break;

      case STARTING:
        runtimeHangingDetector.trackStarting(this, workspaceStartTimeoutMin);
        scheduleServersCheckers();
        break;
      case STOPPED:
      default:
        // do nothing
    }
  }

  private Runnable finishStartupTracingSpan(String machineName) {
    return () -> {
      Span span = machineStartupTraces.remove(machineName);
      if (span != null) {
        span.finish();
      }
    };
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
   * @throws RuntimeStartInterruptedException when the thread is interrupted while waiting machines
   */
  private void waitMachines(
      Map<String, CompletableFuture<Void>> machinesFutures,
      List<CompletableFuture<?>> toCancelFutures,
      CompletableFuture<Void> failure)
      throws InfrastructureException {
    try {
      LOG.debug(
          "Waiting to start machines of workspace '{}'",
          getContext().getIdentity().getWorkspaceId());
      final CompletableFuture<Void> allDone =
          CompletableFuture.allOf(
              machinesFutures.values().toArray(new CompletableFuture[machinesFutures.size()]));

      CompletableFuture.anyOf(allDone, failure)
          .get(startSynchronizer.getStartTimeoutMillis(), TimeUnit.MILLISECONDS);

      if (failure.isCompletedExceptionally()) {
        cancelAll(toCancelFutures);
        // rethrow the failure cause
        failure.get();
      }
      LOG.debug("Machines of workspace '{}' started", getContext().getIdentity().getWorkspaceId());
    } catch (TimeoutException ex) {
      InfrastructureException ie =
          new InfrastructureException(
              "Waiting for Kubernetes environment '"
                  + getContext().getIdentity().getEnvName()
                  + "' of the workspace'"
                  + getContext().getIdentity().getWorkspaceId()
                  + "' reached timeout");
      failure.completeExceptionally(ie);
      cancelAll(toCancelFutures);
      finishAllStartupTracesAsFailures(ie);
      throw ie;
    } catch (InterruptedException ex) {
      RuntimeStartInterruptedException runtimeInterruptedEx =
          new RuntimeStartInterruptedException(getContext().getIdentity());
      failure.completeExceptionally(runtimeInterruptedEx);
      cancelAll(toCancelFutures);
      finishAllStartupTracesAsFailures(ex);
      throw runtimeInterruptedEx;
    } catch (ExecutionException ex) {
      failure.completeExceptionally(ex.getCause());
      cancelAll(toCancelFutures);
      wrapAndRethrow(ex.getCause());
      // note that we do NOT finish the startup traces here, because execution exception is
      // handled by the "exceptional" parts of the completable chain.
    }
  }

  private void finishAllStartupTracesAsFailures(Throwable reason) {
    machineStartupTraces
        .entrySet()
        .removeIf(
            e -> {
              finishSpanAsFailure(e.getValue(), reason.getMessage());
              return true;
            });
  }

  /**
   * Returns a function, the result of which the completable stage that performs servers checks and
   * start of servers probes.
   */
  private Function<Void, CompletionStage<Void>> checkServers(
      List<CompletableFuture<?>> toCancelFutures, KubernetesMachineImpl machine) {
    return ignored -> {
      // This completable future is used to unity the servers checks and start of probes
      final CompletableFuture<Void> serversAndProbesFuture = new CompletableFuture<>();
      final String machineName = machine.getName();
      final RuntimeIdentity runtimeId = getContext().getIdentity();
      final ServersChecker serverCheck =
          serverCheckerFactory.create(runtimeId, machineName, machine.getServers());
      final CompletableFuture<?> serversReadyFuture;
      LOG.debug(
          "Performing servers check for machine '{}' in workspace '{}'",
          machineName,
          runtimeId.getWorkspaceId());
      try {
        serversReadyFuture = serverCheck.startAsync(new ServerReadinessHandler(machineName));
        toCancelFutures.add(serversReadyFuture);
        serversAndProbesFuture.whenComplete(
            (ok, ex) -> {
              LOG.debug(
                  "Servers checks done for machine '{}' in workspace '{}'",
                  machineName,
                  runtimeId.getWorkspaceId());
              serversReadyFuture.cancel(true);
            });
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
      List<CompletableFuture<?>> toCancelFutures, KubernetesMachineImpl machine) {
    return ignored -> {
      // think about to return copy of machines in environment
      final InternalMachineConfig machineConfig =
          getContext().getEnvironment().getMachines().get(machine.getName());
      LOG.debug(
          "Bootstrapping machine '{}' in workspace '{}'",
          machine.getName(),
          getContext().getIdentity().getWorkspaceId());
      final CompletableFuture<Void> bootstrapperFuture;
      if (!machineConfig.getInstallers().isEmpty()) {
        bootstrapperFuture =
            bootstrapperFactory
                .create(
                    getContext().getIdentity(),
                    machineConfig.getInstallers(),
                    machine,
                    namespace,
                    startSynchronizer)
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
  private Function<Throwable, Void> publishFailedStatusAndRecordFailureTrace(
      CompletableFuture<Void> failure, String machineName) {
    return ex -> {
      if (failure.completeExceptionally(ex)) {
        finishSpanAsFailure(machineStartupTraces.remove(machineName), ex.getMessage());

        try {
          machines.updateMachineStatus(
              getContext().getIdentity(), machineName, MachineStatus.FAILED);
        } catch (InfrastructureException e) {
          LOG.error(
              "Unable to update status of the machine '{}:{}'. Cause: {}",
              getContext().getIdentity().getWorkspaceId(),
              machineName,
              e.getMessage());
        }
        eventPublisher.sendFailedEvent(machineName, ex.getMessage(), getContext().getIdentity());
      } else {
        String message = ex.getMessage() + " (happened elsewhere)";
        finishSpanAsCancelled(machineStartupTraces.remove(machineName), message);
      }
      return null;
    };
  }

  private void finishSpanAsFailure(Span span, String reason) {
    if (span != null) {
      // record the startup as a failure and set the priority so that this span is not throttled
      TracingTags.ERROR.set(span, true);
      TracingTags.SAMPLING_PRIORITY.set(span, 1);
      TracingTags.ERROR_REASON.set(span, reason);
      span.finish();
    }
  }

  private void finishSpanAsCancelled(Span span, String reason) {
    if (span != null) {
      TracingTags.CANCELLED.set(span, true);
      TracingTags.CANCELLED_REASON.set(span, reason);
      span.finish();
    }
  }

  /**
   * Returns the future, which ends when machine is considered as running.
   *
   * <p>Note that the resulting future must be explicitly cancelled when its completion no longer
   * important because of finalization allocated resources.
   */
  public CompletableFuture<Void> waitRunningAsync(
      List<CompletableFuture<?>> toCancelFutures, KubernetesMachineImpl machine) {
    CompletableFuture<Void> waitFuture =
        namespace.deployments().waitRunningAsync(machine.getPodName());

    toCancelFutures.add(waitFuture);
    return waitFuture;
  }

  /** Returns instance of {@link Runnable} that propagate machine state. */
  private Runnable publishRunningStatus(String machineName) {
    return () -> {
      try {
        machines.updateMachineStatus(
            getContext().getIdentity(), machineName, MachineStatus.RUNNING);
      } catch (InfrastructureException e) {
        LOG.error(
            "Unable to update status of the machine '{}:{}'. Cause: {}",
            getContext().getIdentity().getWorkspaceId(),
            machineName,
            e.getMessage());
      }
      eventPublisher.sendRunningEvent(machineName, getContext().getIdentity());
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
  public Map<String, ? extends KubernetesMachineImpl> getInternalMachines()
      throws InfrastructureException {
    return ImmutableMap.copyOf(machines.getMachines(getContext().getIdentity()));
  }

  @Override
  public List<? extends Command> getCommands() throws InfrastructureException {
    return runtimeStates.getCommands(getContext().getIdentity());
  }

  @Traced
  @Override
  protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
    RuntimeIdentity identity = getContext().getIdentity();

    TracingTags.WORKSPACE_ID.set(identity.getWorkspaceId());

    runtimeHangingDetector.stopTracking(getContext().getIdentity());
    if (startSynchronizer.interrupt()) {
      // runtime is STARTING. Need to wait until start will be interrupted properly
      try {
        if (!startSynchronizer.awaitInterruption(workspaceStartTimeoutMin, TimeUnit.MINUTES)) {
          // Runtime is not interrupted yet. It may occur when start was performing by another
          // Che Server that is crashed so start is hung up in STOPPING phase.
          // Need to clean up runtime resources
          probeScheduler.cancel(identity.getWorkspaceId());
          namespace.cleanUp();
        }
      } catch (InterruptedException e) {
        throw new InfrastructureException(
            "Interrupted while waiting for start task cancellation", e);
      }
    } else {
      // runtime is RUNNING. Clean up used resources
      // Cancels workspace servers probes if any
      probeScheduler.cancel(identity.getWorkspaceId());
      namespace.cleanUp();
    }
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
    String workspaceId = getContext().getIdentity().getWorkspaceId();

    createSecrets(k8sEnv, workspaceId);
    createConfigMaps(k8sEnv, workspaceId);
    List<Service> createdServices = createServices(k8sEnv, workspaceId);

    // needed for resolution later on, even though n routes are actually created by ingress
    // /workspace{wsid}/server-{port} => service({wsid}):server-port => pod({wsid}):{port}
    List<Ingress> readyIngresses = createIngresses(k8sEnv, workspaceId);

    // TODO https://github.com/eclipse/che/issues/7653
    // namespace.pods().watch(new AbnormalStopHandler());
    namespace.deployments().watchEvents(new MachineLogsPublisher());
    if (unrecoverableEventListenerFactory.isConfigured()) {
      KubernetesEnvironment environment = getContext().getEnvironment();
      Set<String> toWatch =
          environment
              .getPodsData()
              .values()
              .stream()
              .map(podData -> podData.getMetadata().getName())
              .collect(Collectors.toSet());
      namespace
          .deployments()
          .watchEvents(
              unrecoverableEventListenerFactory.create(toWatch, this::handleUnrecoverableEvent));
    }

    final KubernetesServerResolver serverResolver =
        new KubernetesServerResolver(createdServices, readyIngresses);

    doStartMachine(serverResolver);
  }

  @Traced
  @SuppressWarnings("WeakerAccess") // package-private so that interception is possible
  void createSecrets(KubernetesEnvironment env, String workspaceId) throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);
    for (Secret secret : env.getSecrets().values()) {
      namespace.secrets().create(secret);
    }
  }

  @Traced
  @SuppressWarnings("WeakerAccess") // package-private so that interception is possible
  void createConfigMaps(KubernetesEnvironment env, String workspaceId)
      throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);
    for (ConfigMap configMap : env.getConfigMaps().values()) {
      namespace.configMaps().create(configMap);
    }
  }

  @Traced
  @SuppressWarnings("WeakerAccess") // package-private so that interception is possible
  List<Service> createServices(KubernetesEnvironment env, String workspaceId)
      throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);

    Collection<Service> servicesToCreate = env.getServices().values();
    List<Service> createdServices = new ArrayList<>(servicesToCreate.size());
    for (Service service : servicesToCreate) {
      createdServices.add(namespace.services().create(service));
    }

    return createdServices;
  }

  @Traced
  @SuppressWarnings("WeakerAccess") // package-private so that interception is possible
  List<Ingress> createIngresses(KubernetesEnvironment env, String workspaceId)
      throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);
    return createAndWaitReady(env.getIngresses().values());
  }

  /**
   * Creates Kubernetes pods and resolves servers using the specified serverResolver.
   *
   * @param serverResolver server resolver that provide servers by container
   * @throws InfrastructureException when any error occurs while creating Kubernetes pods
   */
  protected void doStartMachine(KubernetesServerResolver serverResolver)
      throws InfrastructureException {
    machineStartupTraces =
        new ConcurrentHashMap<>(getContext().getEnvironment().getMachines().size());

    final KubernetesEnvironment environment = getContext().getEnvironment();
    final Map<String, InternalMachineConfig> machineConfigs = environment.getMachines();
    final String workspaceId = getContext().getIdentity().getWorkspaceId();
    LOG.debug("Begin pods creation for workspace '{}'", workspaceId);
    for (Pod toCreate : environment.getPodsCopy().values()) {
      startTracingContainersStartup(toCreate.getMetadata(), toCreate.getSpec());
      ObjectMeta toCreateMeta = toCreate.getMetadata();
      final Pod createdPod = namespace.deployments().deploy(toCreate);
      LOG.debug("Creating pod '{}' in workspace '{}'", toCreateMeta.getName(), workspaceId);
      storeStartingMachine(createdPod, toCreateMeta, machineConfigs, serverResolver);
    }
    for (Deployment toCreate : environment.getDeploymentsCopy().values()) {
      PodTemplateSpec template = toCreate.getSpec().getTemplate();
      startTracingContainersStartup(template.getMetadata(), template.getSpec());
      ObjectMeta toCreateMeta = toCreate.getMetadata();
      final Pod createdPod = namespace.deployments().deploy(toCreate);
      LOG.debug("Creating deployment '{}' in workspace '{}'", toCreateMeta.getName(), workspaceId);
      // We need to pass the meta from the pod in the deployment as that is what matches
      // machine name
      final ObjectMeta templateMeta = toCreate.getSpec().getTemplate().getMetadata();
      storeStartingMachine(createdPod, templateMeta, machineConfigs, serverResolver);
    }
    LOG.debug("Pods creation finished in workspace '{}'", workspaceId);
  }

  /** Puts createdPod in the {@code machines} map and sends the starting event for this machine */
  private void storeStartingMachine(
      Pod createdPod,
      ObjectMeta toCreateMeta,
      Map<String, InternalMachineConfig> machineConfigs,
      KubernetesServerResolver serverResolver)
      throws InfrastructureException {
    final String workspaceId = getContext().getIdentity().getWorkspaceId();
    for (Container container : createdPod.getSpec().getContainers()) {
      String machineName = Names.machineName(toCreateMeta, container);

      LOG.debug("Creating machine '{}' in workspace '{}'", machineName, workspaceId);
      machines.put(
          getContext().getIdentity(),
          new KubernetesMachineImpl(
              workspaceId,
              machineName,
              createdPod.getMetadata().getName(),
              container.getName(),
              MachineStatus.STARTING,
              machineConfigs.get(machineName).getAttributes(),
              serverResolver.resolve(machineName)));
      eventPublisher.sendStartingEvent(machineName, getContext().getIdentity());
    }
  }

  private void startTracingContainersStartup(ObjectMeta podMeta, PodSpec podSpec) {
    if (tracer == null) {
      return;
    }

    for (Container container : podSpec.getContainers()) {
      String machineName = Names.machineName(podMeta, container);

      machineStartupTraces.put(
          machineName,
          tracer
              .buildSpan("machine.create")
              .asChildOf(tracer.activeSpan())
              .withTag(TracingTags.MACHINE_NAME.getKey(), machineName)
              .start());
    }
  }

  @Override
  public WorkspaceStatus getStatus() throws InfrastructureException {
    Optional<WorkspaceStatus> runtimeStatusOpt =
        runtimeStates.getStatus(getContext().getIdentity());
    return runtimeStatusOpt.isPresent() ? runtimeStatusOpt.get() : WorkspaceStatus.STOPPED;
  }

  @Override
  protected void markStarting() throws InfrastructureException {
    if (!runtimeStates.putIfAbsent(
        new KubernetesRuntimeState(
            getContext().getIdentity(),
            namespace.getName(),
            WorkspaceStatus.STARTING,
            getContext().getEnvironment().getCommands()))) {
      throw new StateException("Runtime is already started");
    }
  }

  @Override
  protected void markRunning() throws InfrastructureException {
    runtimeStates.updateStatus(getContext().getIdentity(), WorkspaceStatus.RUNNING);
  }

  @Override
  protected void markStopping() throws InfrastructureException {
    RuntimeIdentity runtimeId = getContext().getIdentity();

    // Check if runtime is in STARTING phase to actualize state of startSynchronizer.
    Optional<WorkspaceStatus> statusOpt = runtimeStates.getStatus(runtimeId);
    if (statusOpt.isPresent() && statusOpt.get() == WorkspaceStatus.STARTING) {
      startSynchronizer.start();
    }

    if (!runtimeStates.updateStatus(
        runtimeId,
        s -> s == WorkspaceStatus.RUNNING || s == WorkspaceStatus.STARTING,
        WorkspaceStatus.STOPPING)) {
      throw new StateException("The environment must be running or starting");
    }
  }

  @Override
  protected void markStopped() throws InfrastructureException {
    machines.remove(getContext().getIdentity());
    runtimeStates.remove(getContext().getIdentity());
  }

  private List<Ingress> createAndWaitReady(Collection<Ingress> ingresses)
      throws InfrastructureException {
    List<Ingress> createdIngresses = new ArrayList<>();
    for (Ingress ingress : ingresses) {
      createdIngresses.add(namespace.ingresses().create(ingress));
    }
    LOG.debug(
        "Ingresses created for workspace '{}'. Wait them to be ready.",
        getContext().getIdentity().getWorkspaceId());

    // wait for LB ip
    List<Ingress> readyIngresses = new ArrayList<>();
    for (Ingress ingress : createdIngresses) {
      Ingress actualIngress =
          namespace
              .ingresses()
              .wait(
                  ingress.getMetadata().getName(),
                  // Smaller value of ingress and start timeout should be used
                  Math.min(ingressStartTimeoutMillis, startSynchronizer.getStartTimeoutMillis()),
                  TimeUnit.MILLISECONDS,
                  p -> (!p.getStatus().getLoadBalancer().getIngress().isEmpty()));
      readyIngresses.add(actualIngress);
    }
    LOG.debug(
        "Ingresses creation for workspace '{}' done.", getContext().getIdentity().getWorkspaceId());
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

  /**
   * Schedules server checkers.
   *
   * <p>Note that if the runtime is {@link WorkspaceStatus#RUNNING} then checkers will be scheduled
   * immediately. If the runtime is {@link WorkspaceStatus#STARTING} then checkers will be scheduled
   * when it becomes {@link WorkspaceStatus#RUNNING}. If runtime has any another status then
   * checkers won't be scheduled at all.
   *
   * @throws InfrastructureException when any exception occurred
   */
  public void scheduleServersCheckers() throws InfrastructureException {
    WorkspaceStatus status = getStatus();

    if (status != WorkspaceStatus.RUNNING && status != WorkspaceStatus.STARTING) {
      return;
    }

    ServerLivenessHandler consumer = new ServerLivenessHandler();
    WorkspaceProbes probes =
        probesFactory.getProbes(getContext().getIdentity(), getInternalMachines());

    if (status == WorkspaceStatus.RUNNING) {
      probeScheduler.schedule(probes, consumer);
    } else {
      // Workspace is starting it is needed to start servers checkers when it becomes RUNNING
      probeScheduler.schedule(
          probes,
          consumer,
          () -> {
            try {
              return getStatus();
            } catch (InfrastructureException e) {
              throw new RuntimeException(e.getMessage());
            }
          });
    }
  }

  protected void handleUnrecoverableEvent(PodEvent podEvent) {
    String reason = podEvent.getReason();
    String message = podEvent.getMessage();
    LOG.error(
        "Unrecoverable event occurred during workspace '{}' startup: {}, {}, {}",
        getContext().getIdentity().getWorkspaceId(),
        reason,
        message,
        podEvent.getPodName());

    startSynchronizer.completeExceptionally(
        new InfrastructureException(
            format(
                "Unrecoverable event occurred: '%s', '%s', '%s'",
                reason, message, podEvent.getPodName())));
  }

  /**
   * Returns true if the runtime is working normally, or returns false if the runtime has
   * inconsistent state and should be stopped immediately
   *
   * <p>Runtime is considered as inconsistent if all its pods don't exist anymore. In this case, all
   * workspace servers are not available and it doesn't make sense to keep it RUNNING.
   *
   * <p>Runtime is considered as consistent if there is at least one existing pod because existing
   * pod may provide user needed services and there is no need to stop such workspaces.
   *
   * @throws InfrastructureException if any exception occurs during check performing
   */
  public boolean isConsistent() throws InfrastructureException {
    Set<String> podNames =
        getInternalMachines()
            .values()
            .stream()
            .map(KubernetesMachineImpl::getPodName)
            .collect(Collectors.toSet());
    for (String podName : podNames) {
      if (namespace.deployments().get(podName).isPresent()) {
        return true;
      }
    }
    // runtime has only non-existing pods
    return false;
  }

  private class ServerReadinessHandler implements Consumer<String> {

    private String machineName;

    ServerReadinessHandler(String machineName) {
      this.machineName = machineName;
    }

    @Override
    public void accept(String serverRef) {
      RuntimeIdentity identity = getContext().getIdentity();
      try {
        machines.updateServerStatus(identity, machineName, serverRef, ServerStatus.RUNNING);

        String url = machines.getServer(identity, machineName, serverRef).getUrl();

        eventPublisher.sendServerRunningEvent(machineName, serverRef, url, identity);
      } catch (InfrastructureException e) {
        LOG.error(
            "Unable to update status of the server '{}:{}:{}'. Cause: {}",
            identity.getWorkspaceId(),
            machineName,
            serverRef,
            e.getMessage());
      }
    }
  }

  private class ServerLivenessHandler implements Consumer<ProbeResult> {

    @Override
    public void accept(ProbeResult probeResult) {
      String machineName = probeResult.getMachineName();
      String serverName = probeResult.getServerName();
      ProbeStatus probeStatus = probeResult.getStatus();

      ServerStatus serverStatus;
      if (probeStatus == ProbeStatus.FAILED) {
        serverStatus = ServerStatus.STOPPED;
      } else if (probeStatus == ProbeStatus.PASSED) {
        serverStatus = ServerStatus.RUNNING;
      } else {
        return;
      }

      RuntimeIdentity identity = getContext().getIdentity();
      try {
        if (machines.updateServerStatus(identity, machineName, serverName, serverStatus)) {
          eventPublisher.sendServerStatusEvent(
              machineName,
              serverName,
              machines.getServer(identity, machineName, serverName),
              identity);
        }
      } catch (InfrastructureException e) {
        LOG.error(
            "Unable to update status of the server '{}:{}:{}'. Cause: {}",
            identity.getWorkspaceId(),
            machineName,
            serverName,
            e.getMessage());
      }
    }
  }

  /** Listens pod events and publish them as machine logs. */
  public class MachineLogsPublisher implements PodEventHandler {

    @Override
    public void handle(PodEvent event) {
      final String podName = event.getPodName();
      try {
        for (Entry<String, KubernetesMachineImpl> entry :
            machines.getMachines(getContext().getIdentity()).entrySet()) {
          final KubernetesMachineImpl machine = entry.getValue();
          if (machine.getPodName().equals(podName)) {
            eventPublisher.sendMachineLogEvent(
                entry.getKey(),
                event.getMessage(),
                event.getCreationTimeStamp(),
                getContext().getIdentity());
            return;
          }
        }
      } catch (InfrastructureException e) {
        LOG.error("Error while machine fetching for logs publishing. Cause: {}", e.getMessage());
      }
    }
  }

  /** Stops runtime if one of the pods was abnormally stopped. */
  class AbnormalStopHandler implements PodActionHandler {

    @Override
    public void handle(Action action, Pod pod) {
      eventPublisher.sendAbnormalStoppingEvent(
          getContext().getIdentity(),
          format("Pod '%s' was abnormally stopped", pod.getMetadata().getName()));
      // Cancels workspace servers probes if any
      probeScheduler.cancel(getContext().getIdentity().getWorkspaceId());
      if (pod.getStatus() != null && POD_STATUS_PHASE_FAILED.equals(pod.getStatus().getPhase())) {
        try {
          internalStop(emptyMap());
        } catch (InfrastructureException ex) {
          LOG.error("Kubernetes environment stop failed cause '{}'", ex.getMessage());
        } finally {
          eventPublisher.sendAbnormalStoppedEvent(
              getContext().getIdentity(),
              format("Pod '%s' was abnormally stopped", pod.getMetadata().getName()));
        }
      }
    }
  }
}

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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.STARTING;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.IntOrStringBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbes;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.api.workspace.server.spi.StateException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInternalRuntime.MachineLogsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapper;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl.MachineId;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState.RuntimeId;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesIngresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerResolver;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.PodEvents;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesInternalRuntime}.
 *
 * @author Anton Korneta
 */
public class KubernetesInternalRuntimeTest {

  private static final int EXPOSED_PORT_1 = 4401;
  private static final int EXPOSED_PORT_2 = 8081;
  private static final int INTERNAL_PORT = 4411;

  private static final String WORKSPACE_ID = "workspace123";
  private static final String WORKSPACE_POD_NAME = "app";
  private static final String INGRESS_NAME = "test-ingress";
  private static final String SERVICE_NAME = "test-service";
  private static final String POD_SELECTOR = "che.pod.name";
  private static final String CONTAINER_NAME_1 = "test1";
  private static final String CONTAINER_NAME_2 = "test2";
  private static final String EVENT_CREATION_TIMESTAMP = "2018-05-15T16:17:54Z";

  /* Pods created by a deployment are created with a random suffix, so Pod names won't match
  exactly. */
  private static final String POD_NAME_RANDOM_SUFFIX = "-12345";

  private static final String INGRESS_HOST = "localhost";

  private static final String M1_NAME = WORKSPACE_POD_NAME + '/' + CONTAINER_NAME_1;
  private static final String M2_NAME = WORKSPACE_POD_NAME + '/' + CONTAINER_NAME_2;

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1");

  @Mock private EventService eventService;
  @Mock private StartSynchronizerFactory startSynchronizerFactory;
  private StartSynchronizer startSynchronizer;
  @Mock private KubernetesRuntimeContext<KubernetesEnvironment> context;
  @Mock private ServersCheckerFactory serverCheckerFactory;
  @Mock private ServersChecker serversChecker;
  @Mock private UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory;
  @Mock private KubernetesBootstrapperFactory bootstrapperFactory;
  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private KubernetesNamespace namespace;
  @Mock private KubernetesServices services;
  @Mock private KubernetesIngresses ingresses;
  @Mock private KubernetesSecrets secrets;
  @Mock private KubernetesConfigsMaps configMaps;
  @Mock private KubernetesDeployments deployments;
  @Mock private KubernetesBootstrapper bootstrapper;
  @Mock private WorkspaceVolumesStrategy volumesStrategy;
  @Mock private WorkspaceProbesFactory workspaceProbesFactory;
  @Mock private ProbeScheduler probesScheduler;
  @Mock private WorkspaceProbes workspaceProbes;
  @Mock private KubernetesServerResolver kubernetesServerResolver;
  @Mock private InternalEnvironmentProvisioner internalEnvironmentProvisioner;

  @Mock
  private KubernetesEnvironmentProvisioner<KubernetesEnvironment> kubernetesEnvironmentProvisioner;

  @Mock private SidecarToolingProvisioner<KubernetesEnvironment> toolingProvisioner;
  private KubernetesRuntimeStateCache runtimeStatesCache;
  private KubernetesMachineCache machinesCache;

  @Captor private ArgumentCaptor<MachineStatusEvent> machineStatusEventCaptor;

  private KubernetesInternalRuntime<KubernetesEnvironment> internalRuntime;

  private KubernetesInternalRuntime<KubernetesEnvironment>
      internalRuntimeWithoutUnrecoverableHandler;

  private final ImmutableMap<String, Pod> podsMap =
      ImmutableMap.of(
          WORKSPACE_POD_NAME,
          mockPod(
              ImmutableList.of(
                  mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1),
                  mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT))));

  @BeforeMethod
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    runtimeStatesCache = new MapBasedRuntimeStateCache();
    machinesCache = new MapBasedMachinesCache();

    startSynchronizer = spy(new StartSynchronizer(eventService, IDENTITY));
    when(startSynchronizerFactory.create(any())).thenReturn(startSynchronizer);

    internalRuntime =
        new KubernetesInternalRuntime<>(
            13,
            5,
            new URLRewriter.NoOpURLRewriter(),
            unrecoverablePodEventListenerFactory,
            bootstrapperFactory,
            serverCheckerFactory,
            volumesStrategy,
            probesScheduler,
            workspaceProbesFactory,
            new RuntimeEventsPublisher(eventService),
            new KubernetesSharedPool(),
            runtimeStatesCache,
            machinesCache,
            startSynchronizerFactory,
            ImmutableSet.of(internalEnvironmentProvisioner),
            kubernetesEnvironmentProvisioner,
            toolingProvisioner,
            context,
            namespace,
            emptyList());

    internalRuntimeWithoutUnrecoverableHandler =
        new KubernetesInternalRuntime<>(
            13,
            5,
            new URLRewriter.NoOpURLRewriter(),
            unrecoverablePodEventListenerFactory,
            bootstrapperFactory,
            serverCheckerFactory,
            volumesStrategy,
            probesScheduler,
            workspaceProbesFactory,
            new RuntimeEventsPublisher(eventService),
            new KubernetesSharedPool(),
            runtimeStatesCache,
            machinesCache,
            startSynchronizerFactory,
            ImmutableSet.of(internalEnvironmentProvisioner),
            kubernetesEnvironmentProvisioner,
            toolingProvisioner,
            context,
            namespace,
            emptyList());

    when(context.getEnvironment()).thenReturn(k8sEnv);
    when(serverCheckerFactory.create(any(), anyString(), any())).thenReturn(serversChecker);
    when(context.getIdentity()).thenReturn(IDENTITY);
    doNothing().when(namespace).cleanUp();
    when(namespace.services()).thenReturn(services);
    when(namespace.ingresses()).thenReturn(ingresses);
    when(namespace.deployments()).thenReturn(deployments);
    when(namespace.secrets()).thenReturn(secrets);
    when(namespace.configMaps()).thenReturn(configMaps);
    when(bootstrapperFactory.create(any(), anyList(), any(), any(), any()))
        .thenReturn(bootstrapper);
    doReturn(
            ImmutableMap.of(
                M1_NAME,
                mockMachine(mockInstaller("ws-agent")),
                M2_NAME,
                mockMachine(mockInstaller("terminal"))))
        .when(k8sEnv)
        .getMachines();
    final Map<String, Service> allServices = ImmutableMap.of(SERVICE_NAME, mockService());
    final Ingress ingress = mockIngress();
    final Map<String, Ingress> allIngresses = ImmutableMap.of(INGRESS_NAME, ingress);
    when(services.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(ingresses.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(ingresses.wait(any(), anyInt(), any())).thenReturn(ingress);
    when(deployments.deploy(any())).thenAnswer(a -> a.getArguments()[0]);
    when(k8sEnv.getServices()).thenReturn(allServices);
    when(k8sEnv.getIngresses()).thenReturn(allIngresses);
    when(k8sEnv.getPods()).thenReturn(podsMap);

    when(deployments.waitRunningAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
    when(bootstrapper.bootstrapAsync()).thenReturn(CompletableFuture.completedFuture(null));
    when(serversChecker.startAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
  }

  @Test
  public void startsKubernetesEnvironment() throws Exception {
    when(k8sEnv.getSecrets()).thenReturn(ImmutableMap.of("secret", new Secret()));
    when(k8sEnv.getConfigMaps()).thenReturn(ImmutableMap.of("configMap", new ConfigMap()));

    internalRuntime.internalStart(emptyMap());

    verify(toolingProvisioner).provision(IDENTITY, k8sEnv);
    verify(internalEnvironmentProvisioner).provision(IDENTITY, k8sEnv);
    verify(kubernetesEnvironmentProvisioner).provision(k8sEnv, IDENTITY);
    verify(deployments).deploy(any());
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(secrets).create(any());
    verify(configMaps).create(any());
    verify(namespace.deployments(), times(1)).watchEvents(any());
    verify(bootstrapper, times(2)).bootstrapAsync();
    verify(eventService, times(4)).publish(any());
    verifyOrderedEventsChains(
        new MachineStatusEvent[] {newEvent(M1_NAME, STARTING), newEvent(M1_NAME, RUNNING)},
        new MachineStatusEvent[] {newEvent(M2_NAME, STARTING), newEvent(M2_NAME, RUNNING)});
    verify(serverCheckerFactory).create(IDENTITY, M1_NAME, emptyMap());
    verify(serverCheckerFactory).create(IDENTITY, M2_NAME, emptyMap());
    verify(serversChecker, times(2)).startAsync(any());
    verify(namespace.deployments(), times(1)).stopWatch();
  }

  @Test
  public void startsKubernetesEnvironmentWithUnrecoverableHandler() throws Exception {
    when(unrecoverablePodEventListenerFactory.isConfigured()).thenReturn(true);

    internalRuntimeWithoutUnrecoverableHandler.internalStart(emptyMap());

    verify(deployments).deploy(any());
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(namespace.deployments(), times(2)).watchEvents(any());
    verify(bootstrapper, times(2)).bootstrapAsync();
    verify(eventService, times(4)).publish(any());
    verifyOrderedEventsChains(
        new MachineStatusEvent[] {newEvent(M1_NAME, STARTING), newEvent(M1_NAME, RUNNING)},
        new MachineStatusEvent[] {newEvent(M2_NAME, STARTING), newEvent(M2_NAME, RUNNING)});
    verify(serverCheckerFactory).create(IDENTITY, M1_NAME, emptyMap());
    verify(serverCheckerFactory).create(IDENTITY, M2_NAME, emptyMap());
    verify(serversChecker, times(2)).startAsync(any());
    verify(namespace.deployments(), times(1)).stopWatch();
  }

  @Test
  public void startsKubernetesEnvironmentWithoutUnrecoverableHandler() throws Exception {
    when(unrecoverablePodEventListenerFactory.isConfigured()).thenReturn(false);

    internalRuntimeWithoutUnrecoverableHandler.internalStart(emptyMap());

    verify(deployments).deploy(any());
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(namespace.deployments(), times(1)).watchEvents(any());
    verify(bootstrapper, times(2)).bootstrapAsync();
    verify(eventService, times(4)).publish(any());
    verifyOrderedEventsChains(
        new MachineStatusEvent[] {newEvent(M1_NAME, STARTING), newEvent(M1_NAME, RUNNING)},
        new MachineStatusEvent[] {newEvent(M2_NAME, STARTING), newEvent(M2_NAME, RUNNING)});
    verify(serverCheckerFactory).create(IDENTITY, M1_NAME, emptyMap());
    verify(serverCheckerFactory).create(IDENTITY, M2_NAME, emptyMap());
    verify(serversChecker, times(2)).startAsync(any());
    verify(namespace.deployments(), times(1)).stopWatch();
  }

  @Test(expectedExceptions = InternalInfrastructureException.class)
  public void throwsInternalInfrastructureExceptionWhenRuntimeErrorOccurs() throws Exception {
    doNothing().when(namespace).cleanUp();
    when(k8sEnv.getServices()).thenThrow(new RuntimeException());

    try {
      internalRuntime.internalStart(emptyMap());
    } catch (Exception rethrow) {
      verify(namespace).cleanUp();
      verify(namespace, never()).services();
      verify(namespace, never()).ingresses();
      throw rethrow;
    } finally {
      verify(namespace.deployments(), times(2)).stopWatch();
    }
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void stopsWaitingAllMachineStartWhenOneMachineStartFailed() throws Exception {
    final Container container1 = mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1);
    final Container container2 = mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT);
    final ImmutableMap<String, Pod> allPods =
        ImmutableMap.of(WORKSPACE_POD_NAME, mockPod(ImmutableList.of(container1, container2)));
    when(k8sEnv.getPods()).thenReturn(allPods);
    doThrow(IllegalStateException.class).when(bootstrapper).bootstrapAsync();

    try {
      internalRuntime.internalStart(emptyMap());
    } catch (Exception rethrow) {
      verify(deployments).deploy(any());
      verify(ingresses).create(any());
      verify(services).create(any());
      verify(bootstrapper, atLeastOnce()).bootstrapAsync();
      verify(eventService, atLeastOnce()).publish(any());
      final List<MachineStatusEvent> events = captureEvents();
      assertTrue(events.contains(newEvent(M1_NAME, STARTING)));
      throw rethrow;
    }
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenErrorOccursAndCleanupFailed() throws Exception {
    doNothing().doThrow(InfrastructureException.class).when(namespace).cleanUp();
    when(k8sEnv.getServices()).thenReturn(singletonMap("testService", mock(Service.class)));
    when(services.create(any())).thenThrow(new InfrastructureException("service creation failed"));
    doThrow(IllegalStateException.class).when(namespace).services();

    try {
      internalRuntime.internalStart(emptyMap());
    } catch (Exception rethrow) {
      verify(namespace).cleanUp();
      verify(namespace).services();
      verify(namespace, never()).ingresses();
      throw rethrow;
    } finally {
      verify(namespace.deployments(), times(2)).stopWatch();
    }
  }

  @Test(
      expectedExceptions = RuntimeStartInterruptedException.class,
      expectedExceptionsMessageRegExp =
          "Runtime start for identity 'workspace: workspace123, "
              + "environment: env1, ownerId: id1' is interrupted")
  public void throwsInfrastructureExceptionWhenMachinesWaitingIsInterrupted() throws Exception {
    final Thread thread = Thread.currentThread();
    when(bootstrapper.bootstrapAsync()).thenReturn(new CompletableFuture<>());

    Executors.newSingleThreadScheduledExecutor()
        .schedule(thread::interrupt, 300, TimeUnit.MILLISECONDS);

    internalRuntime.internalStart(emptyMap());
  }

  @Test
  public void stopsKubernetesEnvironment() throws Exception {
    doNothing().when(namespace).cleanUp();

    internalRuntime.internalStop(emptyMap());

    verify(namespace).cleanUp();
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenKubernetesNamespaceCleanupFailed() throws Exception {
    doThrow(InfrastructureException.class).when(namespace).cleanUp();

    internalRuntime.internalStop(emptyMap());
  }

  @Test
  public void testRepublishContainerOutputAsMachineLogEvents() throws Exception {
    final MachineLogsPublisher logsPublisher = internalRuntime.new MachineLogsPublisher();
    final PodEvent out1 =
        mockContainerEventWithoutRandomName(
            WORKSPACE_POD_NAME,
            "Pulling",
            "pulling image",
            EVENT_CREATION_TIMESTAMP,
            getCurrentTimestampWithOneHourShiftAhead());
    final PodEvent out2 =
        mockContainerEventWithoutRandomName(
            WORKSPACE_POD_NAME,
            "Pulled",
            "image pulled",
            EVENT_CREATION_TIMESTAMP,
            getCurrentTimestampWithOneHourShiftAhead());
    final ArgumentCaptor<RuntimeLogEvent> captor = ArgumentCaptor.forClass(RuntimeLogEvent.class);

    internalRuntime.doStartMachine(kubernetesServerResolver);
    logsPublisher.handle(out1);
    logsPublisher.handle(out2);

    verify(eventService, atLeastOnce()).publish(captor.capture());
    final ImmutableList<RuntimeLogEvent> machineLogs =
        ImmutableList.of(asRuntimeLogEvent(out1), asRuntimeLogEvent(out2));

    assertTrue(captor.getAllValues().containsAll(machineLogs));
  }

  @Test
  public void testDoNotPublishForeignMachineOutput() throws ParseException {
    final MachineLogsPublisher logsPublisher = internalRuntime.new MachineLogsPublisher();
    final PodEvent out1 =
        mockContainerEvent(
            WORKSPACE_POD_NAME,
            "Created",
            "folder created",
            EVENT_CREATION_TIMESTAMP,
            getCurrentTimestampWithOneHourShiftAhead());

    logsPublisher.handle(out1);

    verify(eventService, never()).publish(any());
  }

  @Test
  public void cancelsWsProbesOnRuntimeStop() throws Exception {
    doNothing().when(namespace).cleanUp();

    internalRuntime.internalStop(emptyMap());

    verify(probesScheduler).cancel(WORKSPACE_ID);
  }

  @Test
  public void cancelsWsProbesWhenErrorOnRuntimeStartOccurs() throws Exception {
    doNothing().when(namespace).cleanUp();
    when(k8sEnv.getServices()).thenThrow(new RuntimeException());

    try {
      internalRuntime.internalStart(emptyMap());
    } catch (Exception e) {
      verify(probesScheduler).cancel(WORKSPACE_ID);
      return;
    }
    fail();
  }

  @Test
  public void schedulesProbesOnRuntimeStart() throws Exception {
    doNothing().when(namespace).cleanUp();
    when(workspaceProbesFactory.getProbes(eq(IDENTITY), anyString(), any()))
        .thenReturn(workspaceProbes);

    internalRuntime.internalStart(emptyMap());

    verify(probesScheduler, times(2)).schedule(eq(workspaceProbes), any());
  }

  @Test
  public void shouldMarkRuntimeStarting() throws Exception {
    // when
    internalRuntime.markStarting();

    assertEquals(internalRuntime.getStatus(), WorkspaceStatus.STARTING);
  }

  @Test(
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = "Runtime is already started")
  public void shouldThrowExceptionIfRuntimeIsAlreadyStarting() throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), "test", WorkspaceStatus.STARTING));

    // when
    internalRuntime.markStarting();
  }

  @Test
  public void shouldMarkRuntimeRunning() throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), "test", WorkspaceStatus.STARTING));

    // when
    internalRuntime.markRunning();

    // then
    assertEquals(internalRuntime.getStatus(), WorkspaceStatus.RUNNING);
  }

  @Test
  public void shouldMarkRuntimeStopping() throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), "test", WorkspaceStatus.RUNNING));

    // when
    internalRuntime.markStopping();

    // then
    assertEquals(internalRuntime.getStatus(), WorkspaceStatus.STOPPING);
  }

  @Test(
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = "The environment must be running or starting",
      dataProvider = "nonRunningStatuses")
  public void shouldThrowExceptionWhenTryToMakeNonRunningNorStartingRuntimeAsStopping(
      WorkspaceStatus status) throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(internalRuntime.getContext().getIdentity(), "test", status));

    // when
    internalRuntime.markStopping();
  }

  @DataProvider
  Object[][] nonRunningStatuses() {
    return new Object[][] {{WorkspaceStatus.STOPPING}, {WorkspaceStatus.STOPPED}};
  }

  @Test
  public void shouldRemoveRuntimeStateOnMarkingRuntimeStopped() throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), "test", WorkspaceStatus.STOPPING));

    // when
    internalRuntime.markStopped();

    // then
    assertFalse(runtimeStatesCache.get(internalRuntime.getContext().getIdentity()).isPresent());
  }

  @Test
  public void shouldScheduleServerCheckersForRunningRuntime() throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), "test", WorkspaceStatus.RUNNING));

    // when
    internalRuntime.scheduleServersCheckers();

    // then
    verify(probesScheduler).schedule(any(), any());
  }

  @Test
  public void shouldScheduleServerCheckersForStartingRuntime() throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), "test", WorkspaceStatus.STARTING));

    // when
    internalRuntime.scheduleServersCheckers();

    // then
    verify(probesScheduler).schedule(any(), any(), any());
  }

  @Test(dataProvider = "nonStartingRunningStatuses")
  public void shouldNotScheduleServerCheckersIfRuntimeIsNotStartingOrRunning(WorkspaceStatus status)
      throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(internalRuntime.getContext().getIdentity(), "test", status));

    // when
    internalRuntime.scheduleServersCheckers();

    // then
    verifyZeroInteractions(probesScheduler);
  }

  @DataProvider(name = "nonStartingRunningStatuses")
  public Object[][] nonStartingRunningStatuses() {
    return new Object[][] {{WorkspaceStatus.STOPPED}, {WorkspaceStatus.STOPPING}};
  }

  private static MachineStatusEvent newEvent(String machineName, MachineStatus status) {
    return newDto(MachineStatusEvent.class)
        .withIdentity(DtoConverter.asDto(IDENTITY))
        .withMachineName(machineName)
        .withEventType(status);
  }

  private void verifyOrderedEventsChains(MachineStatusEvent[]... eventsArrays) {
    Map<String, LinkedList<MachineStatusEvent>> machine2Events = new HashMap<>();
    List<MachineStatusEvent> machineStatusEvents = captureEvents();
    for (MachineStatusEvent event : machineStatusEvents) {
      final String machineName = event.getMachineName();
      machine2Events.computeIfPresent(
          machineName,
          (mName, events) -> {
            events.add(event);
            return events;
          });
      machine2Events.computeIfAbsent(
          machineName,
          mName -> {
            final LinkedList<MachineStatusEvent> events = new LinkedList<>();
            events.add(event);
            return events;
          });
    }

    for (MachineStatusEvent[] expected : eventsArrays) {
      final MachineStatusEvent machineStatusEvent = expected[0];
      final MachineStatusEvent[] actual =
          machine2Events
              .remove(machineStatusEvent.getMachineName())
              .toArray(new MachineStatusEvent[expected.length]);
      assertEquals(actual, expected);
    }
    assertTrue(machine2Events.isEmpty(), "No more events expected");
  }

  private List<MachineStatusEvent> captureEvents() {
    verify(eventService, atLeastOnce()).publish(machineStatusEventCaptor.capture());
    return machineStatusEventCaptor.getAllValues();
  }

  private static Container mockContainer(String name, int... ports) {
    final Container container = mock(Container.class);
    when(container.getName()).thenReturn(name);
    final List<ContainerPort> containerPorts = new ArrayList<>(ports.length);
    for (int port : ports) {
      containerPorts.add(new ContainerPortBuilder().withContainerPort(port).build());
    }
    when(container.getPorts()).thenReturn(containerPorts);
    return container;
  }

  private static Pod mockPod(List<Container> containers) {
    final Pod pod = mock(Pod.class);
    final PodSpec spec = mock(PodSpec.class);
    mockName(WORKSPACE_POD_NAME, pod);
    when(spec.getContainers()).thenReturn(containers);
    when(pod.getSpec()).thenReturn(spec);
    when(pod.getMetadata().getLabels())
        .thenReturn(
            ImmutableMap.of(
                POD_SELECTOR, WORKSPACE_POD_NAME, CHE_ORIGINAL_NAME_LABEL, WORKSPACE_POD_NAME));
    return pod;
  }

  private static Service mockService() {
    final Service service = mock(Service.class);
    final ServiceSpec spec = mock(ServiceSpec.class);
    mockName(SERVICE_NAME, service);
    when(service.getSpec()).thenReturn(spec);
    when(spec.getSelector()).thenReturn(ImmutableMap.of(POD_SELECTOR, WORKSPACE_POD_NAME));
    final ServicePort sp1 =
        new ServicePortBuilder().withTargetPort(intOrString(EXPOSED_PORT_1)).build();
    final ServicePort sp2 =
        new ServicePortBuilder().withTargetPort(intOrString(EXPOSED_PORT_2)).build();
    when(spec.getPorts()).thenReturn(ImmutableList.of(sp1, sp2));
    return service;
  }

  private static Ingress mockIngress() {
    final Ingress ingress = mock(Ingress.class);
    mockName(INGRESS_NAME, ingress);
    final IngressSpec spec = mock(IngressSpec.class);

    final IngressBackend backend = mock(IngressBackend.class);
    when(backend.getServiceName()).thenReturn(SERVICE_NAME);
    when(backend.getServicePort()).thenReturn(new IntOrString(EXPOSED_PORT_1));
    when(spec.getBackend()).thenReturn(backend);

    final IngressRule rule = mock(IngressRule.class);
    when(rule.getHost()).thenReturn(INGRESS_HOST);
    when(spec.getRules()).thenReturn(singletonList(rule));

    when(ingress.getSpec()).thenReturn(spec);
    when(ingress.getMetadata().getLabels())
        .thenReturn(ImmutableMap.of(CHE_ORIGINAL_NAME_LABEL, INGRESS_NAME));
    return ingress;
  }

  private static InstallerImpl mockInstaller(String name) {
    InstallerImpl installer = mock(InstallerImpl.class);
    when(installer.getName()).thenReturn(name);
    return installer;
  }

  private static InternalMachineConfig mockMachine(InstallerImpl... installers) {
    final InternalMachineConfig machine1 = mock(InternalMachineConfig.class);
    when(machine1.getInstallers()).thenReturn(Arrays.asList(installers));
    return machine1;
  }

  private static ObjectMeta mockName(String name, HasMetadata mock) {
    final ObjectMeta metadata = mock(ObjectMeta.class);
    when(mock.getMetadata()).thenReturn(metadata);
    when(metadata.getName()).thenReturn(name);
    return metadata;
  }

  /**
   * Mock a container event, as though it was triggered by the OpenShift API. As workspace Pods are
   * created indirectly through deployments, they are given generated names with the provided name
   * as a root. <br>
   * Use this method in a test to ensure that tested code manages this fact correctly. For example,
   * code such as unrecoverable events handling cannot directly look at an event's pod name and
   * compare it to the internal representation, and so must confirm the event is relevant in some
   * other way.
   */
  private static PodEvent mockContainerEvent(
      String podName,
      String reason,
      String message,
      String creationTimestamp,
      String lastTimestamp) {
    final PodEvent event = mock(PodEvent.class);
    when(event.getPodName()).thenReturn(podName + POD_NAME_RANDOM_SUFFIX);
    when(event.getContainerName()).thenReturn(CONTAINER_NAME_1);
    when(event.getReason()).thenReturn(reason);
    when(event.getMessage()).thenReturn(message);
    when(event.getCreationTimeStamp()).thenReturn(creationTimestamp);
    when(event.getLastTimestamp()).thenReturn(lastTimestamp);
    return event;
  }

  /**
   * Mock a container event, without modifying the involved Pod's name. Avoid using this method
   * unless it is necessary to check that a specific event (in terms of fields) is emitted.
   *
   * @see KubernetesInternalRuntimeTest#mockContainerEvent(String, String, String, String, String)
   */
  private static PodEvent mockContainerEventWithoutRandomName(
      String podName,
      String reason,
      String message,
      String creationTimestamp,
      String lastTimestamp) {
    final PodEvent event = mock(PodEvent.class);
    when(event.getPodName()).thenReturn(podName);
    when(event.getContainerName()).thenReturn(CONTAINER_NAME_1);
    when(event.getReason()).thenReturn(reason);
    when(event.getMessage()).thenReturn(message);
    when(event.getCreationTimeStamp()).thenReturn(creationTimestamp);
    when(event.getLastTimestamp()).thenReturn(lastTimestamp);
    return event;
  }

  private static RuntimeLogEvent asRuntimeLogEvent(PodEvent event) {
    return newDto(RuntimeLogEvent.class)
        .withRuntimeId(DtoConverter.asDto(IDENTITY))
        .withText(event.getMessage())
        .withTime(event.getCreationTimeStamp())
        .withMachineName(event.getPodName() + '/' + event.getContainerName());
  }

  private String getCurrentTimestampWithOneHourShiftAhead() throws ParseException {
    Date currentTimestampWithOneHourShiftAhead = new Date(new Date().getTime() + 3600 * 1000);
    return PodEvents.convertDateToEventTimestamp(currentTimestampWithOneHourShiftAhead);
  }

  private static IntOrString intOrString(int port) {
    return new IntOrStringBuilder().withIntVal(port).withStrVal(String.valueOf(port)).build();
  }

  private static class MapBasedRuntimeStateCache implements KubernetesRuntimeStateCache {
    private Map<RuntimeId, KubernetesRuntimeState> runtimesStates = new HashMap<>();

    @Override
    public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
      return new HashSet<>(runtimesStates.keySet());
    }

    @Override
    public boolean putIfAbsent(KubernetesRuntimeState state) throws InfrastructureException {
      return runtimesStates.putIfAbsent(state.getRuntimeId(), state) == null;
    }

    @Override
    public void updateStatus(RuntimeIdentity runtimeId, WorkspaceStatus newStatus)
        throws InfrastructureException {
      runtimesStates.get(new RuntimeId(runtimeId)).setStatus(newStatus);
    }

    @Override
    public boolean updateStatus(
        RuntimeIdentity identity, Predicate<WorkspaceStatus> predicate, WorkspaceStatus newStatus)
        throws InfrastructureException {
      KubernetesRuntimeState state = runtimesStates.get(new RuntimeId(identity));
      if (predicate.test(state.getStatus())) {
        state.setStatus(newStatus);
        return true;
      }
      return false;
    }

    @Override
    public WorkspaceStatus getStatus(RuntimeIdentity runtimeId) throws InfrastructureException {
      return runtimesStates.get(new RuntimeId(runtimeId)).getStatus();
    }

    @Override
    public Optional<KubernetesRuntimeState> get(RuntimeIdentity runtimeId)
        throws InfrastructureException {
      return Optional.ofNullable(runtimesStates.get(new RuntimeId(runtimeId)));
    }

    @Override
    public void remove(RuntimeIdentity runtimeId) throws InfrastructureException {
      runtimesStates.remove(new RuntimeId(runtimeId));
    }
  }

  private static class MapBasedMachinesCache implements KubernetesMachineCache {
    private Map<MachineId, KubernetesMachineImpl> machines = new HashMap<>();

    private MachineId machineIdOf(RuntimeIdentity runtimeId, KubernetesMachineImpl machine) {
      return new MachineId(runtimeId.getWorkspaceId(), machine.getName());
    }

    private MachineId machineIdOf(RuntimeIdentity runtimeId, String machineName) {
      return new MachineId(runtimeId.getWorkspaceId(), machineName);
    }

    @Override
    public void put(RuntimeIdentity runtimeIdentity, KubernetesMachineImpl machine)
        throws InfrastructureException {
      machines.put(machineIdOf(runtimeIdentity, machine), machine);
    }

    @Override
    public boolean updateServerStatus(
        RuntimeIdentity runtimeIdentity,
        String machineName,
        String serverName,
        ServerStatus newStatus)
        throws InfrastructureException {
      KubernetesServerImpl server =
          machines.get(machineIdOf(runtimeIdentity, machineName)).getServers().get(serverName);

      if (server.getStatus().equals(newStatus)) {
        return false;
      } else {
        server.setStatus(newStatus);
        return true;
      }
    }

    @Override
    public KubernetesServerImpl getServer(
        RuntimeIdentity runtimeIdentity, String machineName, String serverName)
        throws InfrastructureException {
      return machines.get(machineIdOf(runtimeIdentity, machineName)).getServers().get(serverName);
    }

    @Override
    public void updateMachineStatus(
        RuntimeIdentity runtimeIdentity, String machineName, MachineStatus newStatus)
        throws InfrastructureException {
      machines.get(machineIdOf(runtimeIdentity, machineName)).setStatus(newStatus);
    }

    @Override
    public Map<String, KubernetesMachineImpl> getMachines(RuntimeIdentity runtimeIdentity)
        throws InfrastructureException {
      return machines
          .entrySet()
          .stream()
          .filter(e -> e.getKey().getWorkspaceId().equals(runtimeIdentity.getWorkspaceId()))
          .collect(Collectors.toMap(e -> e.getValue().getName(), Entry::getValue));
    }

    @Override
    public void remove(RuntimeIdentity identity) throws InfrastructureException {
      machines.keySet().removeIf(id -> id.getWorkspaceId().equals(identity.getWorkspaceId()));
    }
  }
}

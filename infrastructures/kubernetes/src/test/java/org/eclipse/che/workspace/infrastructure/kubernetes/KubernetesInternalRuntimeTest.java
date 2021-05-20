/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.STARTING;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.CREATE_IN_CHE_INSTALLATION_NAMESPACE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putAnnotations;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabels;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
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
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.IntOrStringBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import io.opentracing.Tracer;
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
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbes;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.api.workspace.server.spi.StateException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;
import org.eclipse.che.commons.observability.NoopExecutorServiceWrapper;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl.MachineId;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeCommandImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.CheNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesIngresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.LogWatchTimeouts;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.LogWatcher;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.PodLogHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesPreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.SecretAsContainerResourceProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressPathTransformInverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.KubernetesServerResolverFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.ServerResolver;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.PodEvents;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
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
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1", "infraNamespace");

  @Mock private EventService eventService;
  @Mock private StartSynchronizerFactory startSynchronizerFactory;
  private StartSynchronizer startSynchronizer;
  @Mock private KubernetesRuntimeContext<KubernetesEnvironment> context;
  @Mock private ServersCheckerFactory serverCheckerFactory;
  @Mock private ServersChecker serversChecker;
  @Mock private UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory;
  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private KubernetesNamespace namespace;
  @Mock private CheNamespace cheNamespace;
  @Mock private KubernetesServices services;
  @Mock private KubernetesIngresses ingresses;
  @Mock private KubernetesSecrets secrets;
  @Mock private KubernetesConfigsMaps configMaps;
  @Mock private KubernetesDeployments deployments;
  @Mock private WorkspaceVolumesStrategy volumesStrategy;
  @Mock private WorkspaceProbesFactory workspaceProbesFactory;
  @Mock private ProbeScheduler probesScheduler;
  @Mock private WorkspaceProbes workspaceProbes;
  @Mock private ServerResolver serverResolver;
  @Mock private InternalEnvironmentProvisioner internalEnvironmentProvisioner;
  @Mock private IngressPathTransformInverter pathTransformInverter;
  @Mock private RuntimeHangingDetector runtimeHangingDetector;
  @Mock private KubernetesPreviewUrlCommandProvisioner previewUrlCommandProvisioner;
  @Mock private SecretAsContainerResourceProvisioner secretAsContainerResourceProvisioner;
  @Mock private ServiceExposureStrategyProvider serviceExposureStrategyProvider;
  @Mock private RuntimeCleaner runtimeCleaner;
  private KubernetesServerResolverFactory serverResolverFactory;

  @Mock
  private KubernetesEnvironmentProvisioner<KubernetesEnvironment> kubernetesEnvironmentProvisioner;

  @Mock private SidecarToolingProvisioner<KubernetesEnvironment> toolingProvisioner;
  private KubernetesRuntimeStateCache runtimeStatesCache;
  private KubernetesMachineCache machinesCache;
  private RuntimeEventsPublisher eventPublisher;

  @Captor private ArgumentCaptor<MachineStatusEvent> machineStatusEventCaptor;

  private KubernetesInternalRuntime<KubernetesEnvironment> internalRuntime;

  private final ImmutableMap<String, Pod> podsMap =
      ImmutableMap.of(
          WORKSPACE_POD_NAME,
          mockPod(
              ImmutableList.of(
                  mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1),
                  mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT))));

  private final ImmutableMap<String, Deployment> deploymentsMap =
      ImmutableMap.of(
          WORKSPACE_POD_NAME,
          mockDeployment(
              ImmutableList.of(
                  mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1),
                  mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT))));

  @Mock(answer = Answers.RETURNS_MOCKS)
  private Tracer tracer;

  private final CommandImpl envCommand = new CommandImpl("envCommand", "echo hello", "env");

  @BeforeMethod
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    runtimeStatesCache = new MapBasedRuntimeStateCache();
    machinesCache = new MapBasedMachinesCache();
    eventPublisher = new RuntimeEventsPublisher(eventService);
    serverResolverFactory =
        new KubernetesServerResolverFactory(
            pathTransformInverter,
            "che-host",
            MULTI_HOST_STRATEGY,
            WorkspaceExposureType.NATIVE.getConfigValue());

    startSynchronizer = spy(new StartSynchronizer(eventService, 5, IDENTITY));
    when(startSynchronizerFactory.create(any())).thenReturn(startSynchronizer);

    internalRuntime =
        new KubernetesInternalRuntime<>(
            13,
            5,
            new URLRewriter.NoOpURLRewriter(),
            unrecoverablePodEventListenerFactory,
            serverCheckerFactory,
            volumesStrategy,
            probesScheduler,
            workspaceProbesFactory,
            eventPublisher,
            new KubernetesSharedPool(new NoopExecutorServiceWrapper()),
            runtimeStatesCache,
            machinesCache,
            startSynchronizerFactory,
            ImmutableSet.of(internalEnvironmentProvisioner),
            kubernetesEnvironmentProvisioner,
            toolingProvisioner,
            runtimeHangingDetector,
            previewUrlCommandProvisioner,
            secretAsContainerResourceProvisioner,
            serverResolverFactory,
            runtimeCleaner,
            cheNamespace,
            tracer,
            context,
            namespace);

    when(context.getEnvironment()).thenReturn(k8sEnv);
    when(context.getRuntime()).thenReturn(internalRuntime);
    when(serverCheckerFactory.create(any(), anyString(), any())).thenReturn(serversChecker);
    when(context.getIdentity()).thenReturn(IDENTITY);
    doNothing().when(namespace).cleanUp();
    when(namespace.services()).thenReturn(services);
    when(namespace.ingresses()).thenReturn(ingresses);
    when(namespace.deployments()).thenReturn(deployments);
    when(namespace.secrets()).thenReturn(secrets);
    when(namespace.configMaps()).thenReturn(configMaps);
    doReturn(
            ImmutableMap.of(
                M1_NAME,
                mock(InternalMachineConfig.class),
                M2_NAME,
                mock(InternalMachineConfig.class)))
        .when(k8sEnv)
        .getMachines();

    final Map<String, Service> allServices = ImmutableMap.of(SERVICE_NAME, mockService());
    final Ingress ingress = mockIngress();
    final Map<String, Ingress> allIngresses = ImmutableMap.of(INGRESS_NAME, ingress);
    when(services.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(ingresses.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(ingresses.wait(anyString(), anyLong(), any(), any())).thenReturn(ingress);

    when(deployments.deploy(any(Pod.class))).thenAnswer(inv -> inv.getArgument(0));
    when(deployments.deploy(any(Deployment.class)))
        .thenAnswer(
            inv -> {
              Deployment d = inv.getArgument(0);
              Pod pod = new Pod();
              pod.setSpec(d.getSpec().getTemplate().getSpec());
              pod.setMetadata(d.getSpec().getTemplate().getMetadata());
              return pod;
            });

    when(k8sEnv.getServices()).thenReturn(allServices);
    when(k8sEnv.getIngresses()).thenReturn(allIngresses);
    when(k8sEnv.getPodsCopy()).thenReturn(podsMap);
    when(k8sEnv.getCommands()).thenReturn(new ArrayList<>(singletonList(envCommand)));

    when(deployments.waitRunningAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
    when(serversChecker.startAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
  }

  @Test
  public void shouldReturnTrueIfAllPodsExistOnRuntimeConsistencyChecking() throws Exception {
    // given
    KubernetesMachineImpl machine1 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine1", "pod1", "container 1", null, emptyMap(), emptyMap());
    KubernetesMachineImpl machine2 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine2", "pod1", "container 2", null, emptyMap(), emptyMap());
    KubernetesMachineImpl machine3 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine3", "pod2", "container 1", null, emptyMap(), emptyMap());
    machinesCache.put(IDENTITY, machine1);
    machinesCache.put(IDENTITY, machine2);
    machinesCache.put(IDENTITY, machine3);

    doReturn(Optional.of(mock(Pod.class))).when(deployments).get(anyString());

    // when
    boolean isConsistent = internalRuntime.isConsistent();

    // then
    assertTrue(isConsistent);
  }

  @Test
  public void shouldReturnTrueIfOnlyOnePodExistsOnRuntimeConsistencyChecking() throws Exception {
    // given
    KubernetesMachineImpl machine1 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine1", "pod1", "container 1", null, emptyMap(), emptyMap());
    KubernetesMachineImpl machine2 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine2", "pod1", "container 2", null, emptyMap(), emptyMap());
    KubernetesMachineImpl machine3 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine3", "pod2", "container 1", null, emptyMap(), emptyMap());
    machinesCache.put(IDENTITY, machine1);
    machinesCache.put(IDENTITY, machine2);
    machinesCache.put(IDENTITY, machine3);

    doReturn(Optional.of(mock(Pod.class))).when(deployments).get("pod1");
    doReturn(Optional.empty()).when(deployments).get("pod2");

    // when
    boolean isConsistent = internalRuntime.isConsistent();

    // then
    assertTrue(isConsistent);
  }

  @Test
  public void shouldReturnFalseIfAllPodsExistOnRuntimeConsistencyChecking() throws Exception {
    // given
    KubernetesMachineImpl machine1 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine1", "pod1", "container 1", null, emptyMap(), emptyMap());
    KubernetesMachineImpl machine2 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine2", "pod1", "container 2", null, emptyMap(), emptyMap());
    KubernetesMachineImpl machine3 =
        new KubernetesMachineImpl(
            WORKSPACE_ID, "machine3", "pod2", "container 1", null, emptyMap(), emptyMap());
    machinesCache.put(IDENTITY, machine1);
    machinesCache.put(IDENTITY, machine2);
    machinesCache.put(IDENTITY, machine3);

    doReturn(Optional.empty()).when(deployments).get(anyString());

    // when
    boolean isConsistent = internalRuntime.isConsistent();

    // then
    assertFalse(isConsistent);
  }

  @Test
  public void startsKubernetesEnvironment() throws Exception {
    when(k8sEnv.getSecrets()).thenReturn(ImmutableMap.of("secret", new Secret()));
    when(k8sEnv.getConfigMaps()).thenReturn(ImmutableMap.of("configMap", new ConfigMap()));

    internalRuntime.start(emptyMap());

    verify(toolingProvisioner).provision(IDENTITY, startSynchronizer, k8sEnv, emptyMap());
    verify(internalEnvironmentProvisioner).provision(IDENTITY, k8sEnv);
    verify(kubernetesEnvironmentProvisioner).provision(k8sEnv, IDENTITY);
    verify(deployments).deploy(any(Pod.class));
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(secrets).create(any());
    verify(configMaps).create(any());
    verify(runtimeCleaner).cleanUp(namespace, WORKSPACE_ID);
    verify(namespace.deployments(), times(1)).watchEvents(any());
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
  public void testCleanupHappensFirst() throws InfrastructureException {
    internalRuntime.start(emptyMap());

    InOrder cleanupInOrderExecutionVerification =
        Mockito.inOrder(runtimeCleaner, deployments, toolingProvisioner);
    cleanupInOrderExecutionVerification.verify(runtimeCleaner).cleanUp(namespace, WORKSPACE_ID);
    cleanupInOrderExecutionVerification
        .verify(toolingProvisioner)
        .provision(any(), any(), any(), any());
    cleanupInOrderExecutionVerification.verify(deployments).deploy(any(Pod.class));
  }

  @Test
  public void startKubernetesEnvironmentWithDeploymentsInsteadOfPods() throws Exception {
    when(k8sEnv.getPodsCopy()).thenReturn(emptyMap());
    when(k8sEnv.getDeploymentsCopy()).thenReturn(deploymentsMap);
    when(k8sEnv.getSecrets()).thenReturn(ImmutableMap.of("secret", new Secret()));
    when(k8sEnv.getConfigMaps()).thenReturn(ImmutableMap.of("configMap", new ConfigMap()));

    internalRuntime.start(emptyMap());

    verify(toolingProvisioner).provision(IDENTITY, startSynchronizer, k8sEnv, emptyMap());
    verify(internalEnvironmentProvisioner).provision(IDENTITY, k8sEnv);
    verify(kubernetesEnvironmentProvisioner).provision(k8sEnv, IDENTITY);
    verify(deployments).deploy(any(Deployment.class));
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(secrets).create(any());
    verify(configMaps).create(any());
    verify(namespace.deployments(), times(1)).watchEvents(any());
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
  public void startKubernetesEnvironmentWithDeploymentsAndPods() throws Exception {
    when(k8sEnv.getDeploymentsCopy()).thenReturn(deploymentsMap);
    when(k8sEnv.getSecrets()).thenReturn(ImmutableMap.of("secret", new Secret()));
    when(k8sEnv.getConfigMaps()).thenReturn(ImmutableMap.of("configMap", new ConfigMap()));

    internalRuntime.start(emptyMap());

    verify(toolingProvisioner).provision(IDENTITY, startSynchronizer, k8sEnv, emptyMap());
    verify(internalEnvironmentProvisioner).provision(IDENTITY, k8sEnv);
    verify(kubernetesEnvironmentProvisioner).provision(k8sEnv, IDENTITY);
    verify(deployments).deploy(any(Deployment.class));
    verify(deployments).deploy(any(Pod.class));
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(secrets).create(any());
    verify(configMaps).create(any());
    verify(namespace.deployments(), times(1)).watchEvents(any());
    verify(eventService, times(6)).publish(any());
    verifyOrderedEventsChains(
        new MachineStatusEvent[] {
          newEvent(M1_NAME, STARTING), newEvent(M1_NAME, STARTING), newEvent(M1_NAME, RUNNING)
        },
        new MachineStatusEvent[] {
          newEvent(M2_NAME, STARTING), newEvent(M2_NAME, STARTING), newEvent(M2_NAME, RUNNING)
        });
    verify(serverCheckerFactory).create(IDENTITY, M1_NAME, emptyMap());
    verify(serverCheckerFactory).create(IDENTITY, M2_NAME, emptyMap());
    verify(serversChecker, times(2)).startAsync(any());
    verify(namespace.deployments(), times(1)).stopWatch();
  }

  @Test
  public void shouldWatchLogsWithLogLimitBytesSetInStartOptions() throws InfrastructureException {
    internalRuntime.start(
        ImmutableMap.of(
            DEBUG_WORKSPACE_START, "true", DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES, "123"));

    verify(namespace.deployments(), times(1))
        .watchLogs(
            any(PodLogHandler.class),
            any(RuntimeEventsPublisher.class),
            any(LogWatchTimeouts.class),
            any(),
            eq(123L));
  }

  @Test
  public void shouldWatchLogsWhenSetInOptions() throws InfrastructureException {
    internalRuntime.start(singletonMap(DEBUG_WORKSPACE_START, "true"));

    verify(namespace.deployments(), times(1))
        .watchLogs(
            any(PodLogHandler.class),
            any(RuntimeEventsPublisher.class),
            any(LogWatchTimeouts.class),
            any(),
            eq(LogWatcher.DEFAULT_LOG_LIMIT_BYTES));
  }

  @Test
  public void shouldNotWatchLogsWhenSetFalseInOptions() throws InfrastructureException {
    internalRuntime.start(singletonMap(DEBUG_WORKSPACE_START, "false"));

    verify(namespace.deployments(), times(0))
        .watchLogs(
            any(PodLogHandler.class),
            any(RuntimeEventsPublisher.class),
            any(LogWatchTimeouts.class),
            any(),
            eq(LogWatcher.DEFAULT_LOG_LIMIT_BYTES));
  }

  @Test
  public void shouldNotWatchLogsWhenNotSetInOptions() throws InfrastructureException {
    internalRuntime.start(emptyMap());

    verify(namespace.deployments(), times(0))
        .watchLogs(
            any(PodLogHandler.class),
            any(RuntimeEventsPublisher.class),
            any(LogWatchTimeouts.class),
            any(),
            eq(LogWatcher.DEFAULT_LOG_LIMIT_BYTES));
  }

  @Test
  public void shouldReturnCommandsAfterRuntimeStart() throws Exception {
    // given
    CommandImpl commandToProvision = new CommandImpl("provisioned-command", "build", "env");
    doAnswer(
            (Answer<Void>)
                invocationOnMock -> {
                  k8sEnv.getCommands().add(commandToProvision);
                  return null;
                })
        .when(internalEnvironmentProvisioner)
        .provision(any(), any());
    internalRuntime.start(emptyMap());

    // when
    List<? extends Command> commands = internalRuntime.getCommands();

    // then
    assertEquals(commands.size(), 2);
    Optional<? extends Command> envCommandOpt =
        commands.stream().filter(c -> "envCommand".equals(c.getName())).findAny();
    assertTrue(envCommandOpt.isPresent());
    Command envCommand = envCommandOpt.get();
    assertEquals(envCommand.getCommandLine(), envCommand.getCommandLine());
    assertEquals(envCommand.getType(), envCommand.getType());

    Optional<? extends Command> provisionedCommandOpt =
        commands.stream().filter(c -> "provisioned-command".equals(c.getName())).findAny();
    assertTrue(provisionedCommandOpt.isPresent());
    Command provisionedCommand = provisionedCommandOpt.get();
    assertEquals(provisionedCommand.getCommandLine(), provisionedCommand.getCommandLine());
    assertEquals(provisionedCommand.getType(), provisionedCommand.getType());
  }

  @Test
  public void startsKubernetesEnvironmentWithUnrecoverableHandler() throws Exception {
    when(unrecoverablePodEventListenerFactory.isConfigured()).thenReturn(true);

    internalRuntime.start(emptyMap());

    verify(deployments).deploy(any(Pod.class));
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(namespace.deployments(), times(2)).watchEvents(any());
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

    internalRuntime.start(emptyMap());

    verify(deployments).deploy(any(Pod.class));
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(namespace.deployments(), times(1)).watchEvents(any());
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
      internalRuntime.start(emptyMap());
    } catch (Exception rethrow) {
      verify(runtimeCleaner, times(2)).cleanUp(namespace, WORKSPACE_ID);
      verify(namespace, never()).services();
      verify(namespace, never()).ingresses();
      throw rethrow;
    } finally {
      verify(namespace.deployments(), times(1)).stopWatch();
      verify(namespace.deployments(), times(1)).stopWatch(true);
    }
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void stopsWaitingAllMachineStartWhenOneMachineStartFailed() throws Exception {
    final Container container1 = mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1);
    final Container container2 = mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT);
    final ImmutableMap<String, Pod> allPods =
        ImmutableMap.of(WORKSPACE_POD_NAME, mockPod(ImmutableList.of(container1, container2)));
    when(k8sEnv.getPodsCopy()).thenReturn(allPods);

    internalRuntime = spy(internalRuntime);
    doThrow(IllegalStateException.class)
        .when(internalRuntime)
        .waitRunningAsync(any(), argThat(m -> m.getName().equals(M1_NAME)));

    try {
      internalRuntime.start(emptyMap());
    } catch (Exception rethrow) {
      verify(deployments).deploy(any(Pod.class));
      verify(ingresses).create(any());
      verify(services).create(any());
      verify(eventService, atLeastOnce()).publish(any());
      final List<MachineStatusEvent> events = captureEvents();
      assertTrue(events.contains(newEvent(M1_NAME, STARTING)));
      throw rethrow;
    }
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenErrorOccursAndCleanupFailed() throws Exception {
    doNothing()
        .doThrow(InfrastructureException.class)
        .when(runtimeCleaner)
        .cleanUp(namespace, WORKSPACE_ID);
    when(k8sEnv.getServices()).thenReturn(singletonMap("testService", mock(Service.class)));
    when(services.create(any())).thenThrow(new InfrastructureException("service creation failed"));
    doThrow(IllegalStateException.class).when(namespace).services();

    try {
      internalRuntime.start(emptyMap());
    } catch (Exception rethrow) {
      verify(runtimeCleaner, times(2)).cleanUp(namespace, WORKSPACE_ID);
      verify(namespace).services();
      verify(namespace, never()).ingresses();
      throw rethrow;
    } finally {
      verify(namespace.deployments(), times(1)).stopWatch();
      verify(namespace.deployments(), times(1)).stopWatch(true);
    }
  }

  @Test(
      expectedExceptions = RuntimeStartInterruptedException.class,
      expectedExceptionsMessageRegExp =
          "Runtime start for identity 'workspace: workspace123, "
              + "environment: env1, ownerId: id1' is interrupted")
  public void throwsInfrastructureExceptionWhenMachinesWaitingIsInterrupted() throws Exception {
    final Thread thread = Thread.currentThread();
    internalRuntime = spy(internalRuntime);
    doReturn(new CompletableFuture<>()).when(internalRuntime).waitRunningAsync(any(), any());

    Executors.newSingleThreadScheduledExecutor()
        .schedule(thread::interrupt, 300, TimeUnit.MILLISECONDS);

    internalRuntime.start(emptyMap());
  }

  @Test
  public void stopsKubernetesEnvironment() throws Exception {
    doNothing().when(namespace).cleanUp();

    internalRuntime.internalStop(emptyMap());

    verify(runtimeHangingDetector).stopTracking(IDENTITY);
    verify(runtimeCleaner).cleanUp(namespace, WORKSPACE_ID);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenKubernetesNamespaceCleanupFailed() throws Exception {
    doThrow(InfrastructureException.class).when(runtimeCleaner).cleanUp(namespace, WORKSPACE_ID);

    internalRuntime.internalStop(emptyMap());
  }

  @Test
  public void testRepublishContainerOutputAsMachineLogEvents() throws Exception {
    final MachineLogsPublisher logsPublisher =
        new MachineLogsPublisher(eventPublisher, machinesCache, IDENTITY);
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

    internalRuntime.doStartMachine(serverResolver);
    logsPublisher.handle(out1);
    logsPublisher.handle(out2);

    verify(eventService, atLeastOnce()).publish(captor.capture());
    final ImmutableList<RuntimeLogEvent> machineLogs =
        ImmutableList.of(asRuntimeLogEvent(out1), asRuntimeLogEvent(out2));

    assertTrue(captor.getAllValues().containsAll(machineLogs));
  }

  @Test
  public void testDoNotPublishForeignMachineOutput() throws ParseException {
    final MachineLogsPublisher logsPublisher =
        new MachineLogsPublisher(eventPublisher, machinesCache, IDENTITY);
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
      internalRuntime.start(emptyMap());
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

    internalRuntime.start(emptyMap());

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
            internalRuntime.getContext().getIdentity(), WorkspaceStatus.STARTING, emptyList()));

    // when
    internalRuntime.markStarting();
  }

  @Test
  public void shouldMarkRuntimeRunning() throws Exception {
    // given
    runtimeStatesCache.putIfAbsent(
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), WorkspaceStatus.STARTING, emptyList()));

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
            internalRuntime.getContext().getIdentity(), WorkspaceStatus.RUNNING, emptyList()));

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
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), status, emptyList()));

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
            internalRuntime.getContext().getIdentity(), WorkspaceStatus.STOPPING, emptyList()));

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
            internalRuntime.getContext().getIdentity(), WorkspaceStatus.RUNNING, emptyList()));

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
            internalRuntime.getContext().getIdentity(), WorkspaceStatus.STARTING, emptyList()));

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
        new KubernetesRuntimeState(
            internalRuntime.getContext().getIdentity(), status, emptyList()));

    // when
    internalRuntime.scheduleServersCheckers();

    // then
    verifyZeroInteractions(probesScheduler);
  }

  @DataProvider(name = "nonStartingRunningStatuses")
  public Object[][] nonStartingRunningStatuses() {
    return new Object[][] {{WorkspaceStatus.STOPPED}, {WorkspaceStatus.STOPPING}};
  }

  @Test
  public void testInjectablePodsMergedIntoRuntimePods() throws Exception {
    // given
    Map<String, Pod> injectedPods =
        ImmutableMap.of("injected", mockPod(singletonList(mockContainer("injectedContainer"))));

    doReturn(ImmutableMap.of(M1_NAME, injectedPods)).when(k8sEnv).getInjectablePodsCopy();

    doReturn(
            concat(podsMap.entrySet().stream(), injectedPods.entrySet().stream())
                .collect(toMap(Map.Entry::getKey, e -> new PodData(e.getValue()))))
        .when(k8sEnv)
        .getPodsData();

    doReturn(
            ImmutableMap.of(
                M1_NAME,
                mock(InternalMachineConfig.class),
                M2_NAME,
                mock(InternalMachineConfig.class),
                WORKSPACE_POD_NAME + "/injectedContainer",
                mock(InternalMachineConfig.class)))
        .when(k8sEnv)
        .getMachines();

    // when
    internalRuntime.start(emptyMap());

    // then
    ArgumentCaptor<Deployment> podCaptor = ArgumentCaptor.forClass(Deployment.class);

    verify(deployments).deploy(podCaptor.capture());

    List<Deployment> depls = podCaptor.getAllValues();
    assertEquals(depls.size(), 1);
    Deployment deployedPod = depls.get(0);
    List<String> containerNames =
        deployedPod
            .getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .stream()
            .map(Container::getName)
            .collect(toList());

    assertEquals(containerNames.size(), 3);
    assertEquals(
        new HashSet<>(containerNames),
        new HashSet<>(asList(CONTAINER_NAME_1, CONTAINER_NAME_2, "injectedContainer")));
  }

  @Test
  public void testMultipleMachinesRequiringSamePodInjectionResultInOnePodInjected()
      throws Exception {
    // given
    Map<String, Pod> injectedPods =
        ImmutableMap.of("injected", mockPod(singletonList(mockContainer("injectedContainer"))));
    Map<String, Pod> injectedPods2 =
        ImmutableMap.of("injected", mockPod(singletonList(mockContainer("injectedContainer"))));

    doReturn(ImmutableMap.of(M1_NAME, injectedPods, M2_NAME, injectedPods2))
        .when(k8sEnv)
        .getInjectablePodsCopy();

    doReturn(
            concat(podsMap.entrySet().stream(), injectedPods.entrySet().stream())
                .collect(toMap(Map.Entry::getKey, e -> new PodData(e.getValue()))))
        .when(k8sEnv)
        .getPodsData();

    doReturn(
            ImmutableMap.of(
                M1_NAME,
                mock(InternalMachineConfig.class),
                M2_NAME,
                mock(InternalMachineConfig.class),
                WORKSPACE_POD_NAME + "/injectedContainer",
                mock(InternalMachineConfig.class)))
        .when(k8sEnv)
        .getMachines();

    // when
    internalRuntime.start(emptyMap());

    // then
    ArgumentCaptor<Deployment> podCaptor = ArgumentCaptor.forClass(Deployment.class);

    verify(deployments).deploy(podCaptor.capture());

    List<Deployment> depls = podCaptor.getAllValues();
    assertEquals(depls.size(), 1);
    Deployment deployedPod = depls.get(0);
    List<String> containerNames =
        deployedPod
            .getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .stream()
            .map(Container::getName)
            .collect(toList());

    assertEquals(containerNames.size(), 3);
    assertEquals(
        new HashSet<>(containerNames),
        new HashSet<>(asList(CONTAINER_NAME_1, CONTAINER_NAME_2, "injectedContainer")));
  }

  @Test
  public void testDeploymentLabelsAndAnnotations() throws Exception {
    // given
    Map<String, Pod> injectedPods =
        ImmutableMap.of("injected", mockPod(singletonList(mockContainer("injectedContainer"))));

    doReturn(ImmutableMap.of(M1_NAME, injectedPods)).when(k8sEnv).getInjectablePodsCopy();

    doReturn(emptyMap()).when(k8sEnv).getPodsCopy();
    Deployment deployment = mockDeployment(singletonList(mockContainer(CONTAINER_NAME_1)));

    putLabels(deployment.getMetadata(), ImmutableMap.of("k1", "v2", "k3", "v4"));
    putAnnotations(deployment.getMetadata(), ImmutableMap.of("ak1", "av2", "ak3", "av4"));
    doReturn(ImmutableMap.of(WORKSPACE_POD_NAME, deployment)).when(k8sEnv).getDeploymentsCopy();

    doReturn(
            ImmutableMap.of(
                M1_NAME,
                mock(InternalMachineConfig.class),
                WORKSPACE_POD_NAME + "/injectedContainer",
                mock(InternalMachineConfig.class)))
        .when(k8sEnv)
        .getMachines();

    // when
    internalRuntime.start(emptyMap());

    // then
    ArgumentCaptor<Deployment> deploymentCaptor = ArgumentCaptor.forClass(Deployment.class);

    verify(deployments).deploy(deploymentCaptor.capture());
    assertTrue(deployment.getMetadata().getLabels().containsKey("k1"));
    assertTrue(deployment.getMetadata().getLabels().containsKey("k3"));
    assertEquals(
        deployment.getMetadata().getLabels(),
        deploymentCaptor.getValue().getMetadata().getLabels());
    assertTrue(deployment.getMetadata().getAnnotations().containsKey("ak1"));
    assertTrue(deployment.getMetadata().getAnnotations().containsKey("ak3"));
    assertEquals(
        deployment.getMetadata().getAnnotations(),
        deploymentCaptor.getValue().getMetadata().getAnnotations());
  }

  @Test
  public void testInjectablePodsMergedIntoRuntimeDeployments() throws Exception {
    // given
    Map<String, Pod> injectedPods =
        ImmutableMap.of("injected", mockPod(singletonList(mockContainer("injectedContainer"))));

    doReturn(ImmutableMap.of(M1_NAME, injectedPods)).when(k8sEnv).getInjectablePodsCopy();

    doReturn(emptyMap()).when(k8sEnv).getPodsCopy();

    doReturn(
            ImmutableMap.of(
                WORKSPACE_POD_NAME, mockDeployment(singletonList(mockContainer(CONTAINER_NAME_1)))))
        .when(k8sEnv)
        .getDeploymentsCopy();

    doReturn(
            ImmutableMap.of(
                M1_NAME,
                mock(InternalMachineConfig.class),
                WORKSPACE_POD_NAME + "/injectedContainer",
                mock(InternalMachineConfig.class)))
        .when(k8sEnv)
        .getMachines();

    // when
    internalRuntime.start(emptyMap());

    // then
    ArgumentCaptor<Deployment> podCaptor = ArgumentCaptor.forClass(Deployment.class);

    verify(deployments).deploy(podCaptor.capture());

    List<Deployment> depls = podCaptor.getAllValues();
    assertEquals(depls.size(), 1);
    Deployment deployedPod = depls.get(0);
    List<String> containerNames =
        deployedPod
            .getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .stream()
            .map(Container::getName)
            .collect(toList());

    assertEquals(containerNames.size(), 2);
    assertEquals(
        new HashSet<>(containerNames),
        new HashSet<>(asList(CONTAINER_NAME_1, "injectedContainer")));
  }

  @Test
  public void testGatewayRouteConfigsAreCreatedAsConfigmapsInCheNamespace()
      throws InfrastructureException {
    // given
    ConfigMap cmRoute1 =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route1")
            .withAnnotations(ImmutableMap.of(CREATE_IN_CHE_INSTALLATION_NAMESPACE, "true"))
            .endMetadata()
            .build();
    ConfigMap cmRoute2 =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route2")
            .withAnnotations(ImmutableMap.of(CREATE_IN_CHE_INSTALLATION_NAMESPACE, "true"))
            .endMetadata()
            .build();
    ConfigMap cm3 =
        new ConfigMapBuilder().withNewMetadata().withName("route2").endMetadata().build();

    Map<String, ConfigMap> configMaps =
        ImmutableMap.of("route1", cmRoute1, "route2", cmRoute2, "cm3", cm3);
    when(k8sEnv.getConfigMaps()).thenReturn(configMaps);

    List<ConfigMap> expectedCreatedCheNamespaceConfigmaps = Arrays.asList(cmRoute1, cmRoute2);

    // when
    internalRuntime.start(emptyMap());

    // then
    verify(cheNamespace)
        .createConfigMaps(
            expectedCreatedCheNamespaceConfigmaps, internalRuntime.getContext().getIdentity());
    verify(runtimeCleaner).cleanUp(namespace, WORKSPACE_ID);
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
    final Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(WORKSPACE_POD_NAME)
            .withLabels(
                ImmutableMap.of(
                    POD_SELECTOR, WORKSPACE_POD_NAME, CHE_ORIGINAL_NAME_LABEL, WORKSPACE_POD_NAME))
            .endMetadata()
            .withNewSpec()
            .withContainers(containers)
            .endSpec()
            .build();
    return pod;
  }

  private static Deployment mockDeployment(List<Container> containers) {
    final Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName(WORKSPACE_POD_NAME)
            .withLabels(
                ImmutableMap.of(
                    POD_SELECTOR, WORKSPACE_POD_NAME, CHE_ORIGINAL_NAME_LABEL, WORKSPACE_POD_NAME))
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(containers)
            .endSpec()
            .withNewMetadata()
            .withName(WORKSPACE_POD_NAME)
            .withLabels(
                ImmutableMap.of(
                    POD_SELECTOR, WORKSPACE_POD_NAME, CHE_ORIGINAL_NAME_LABEL, WORKSPACE_POD_NAME))
            .endMetadata()
            .endTemplate()
            .endSpec()
            .build();
    return deployment;
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

  private String getCurrentTimestampWithOneHourShiftAhead() {
    Date currentTimestampWithOneHourShiftAhead = new Date(new Date().getTime() + 3600 * 1000);
    return PodEvents.convertDateToEventTimestamp(currentTimestampWithOneHourShiftAhead);
  }

  private static IntOrString intOrString(int port) {
    return new IntOrStringBuilder().withIntVal(port).withStrVal(String.valueOf(port)).build();
  }

  private static class MapBasedRuntimeStateCache implements KubernetesRuntimeStateCache {
    private Map<RuntimeIdentity, KubernetesRuntimeState> runtimesStates = new HashMap<>();

    @Override
    public Set<RuntimeIdentity> getIdentities() {
      return new HashSet<>(runtimesStates.keySet());
    }

    @Override
    public boolean putIfAbsent(KubernetesRuntimeState state) {
      return runtimesStates.putIfAbsent(state.getRuntimeId(), state) == null;
    }

    @Override
    public void updateStatus(RuntimeIdentity runtimeId, WorkspaceStatus newStatus) {
      runtimesStates.get(new RuntimeIdentityImpl(runtimeId)).setStatus(newStatus);
    }

    @Override
    public boolean updateStatus(
        RuntimeIdentity identity, Predicate<WorkspaceStatus> predicate, WorkspaceStatus newStatus) {
      KubernetesRuntimeState state = runtimesStates.get(new RuntimeIdentityImpl(identity));
      if (predicate.test(state.getStatus())) {
        state.setStatus(newStatus);
        return true;
      }
      return false;
    }

    @Override
    public void updateCommands(RuntimeIdentity identity, List<? extends Command> commands)
        throws InfrastructureException {
      KubernetesRuntimeState runtimeState = runtimesStates.get(identity);
      if (runtimeState == null) {
        throw new InfrastructureException("Runtime state is not stored");
      }
      runtimeState.setCommands(
          commands.stream().map(KubernetesRuntimeCommandImpl::new).collect(toList()));
    }

    @Override
    public Optional<WorkspaceStatus> getStatus(RuntimeIdentity runtimeId) {
      KubernetesRuntimeState runtimeState = runtimesStates.get(runtimeId);
      if (runtimeState == null) {
        return Optional.empty();
      }
      return Optional.of(runtimeState.getStatus());
    }

    @Override
    public List<? extends Command> getCommands(RuntimeIdentity runtimeId) {
      KubernetesRuntimeState runtimeState = runtimesStates.get(runtimeId);
      if (runtimeState == null) {
        return emptyList();
      }
      return runtimeState.getCommands();
    }

    @Override
    public Optional<KubernetesRuntimeState> get(RuntimeIdentity runtimeId) {
      return Optional.ofNullable(runtimesStates.get(new RuntimeIdentityImpl(runtimeId)));
    }

    @Override
    public void remove(RuntimeIdentity runtimeId) {
      runtimesStates.remove(new RuntimeIdentityImpl(runtimeId));
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
    public void put(RuntimeIdentity runtimeIdentity, KubernetesMachineImpl machine) {
      machines.put(machineIdOf(runtimeIdentity, machine), machine);
    }

    @Override
    public boolean updateServerStatus(
        RuntimeIdentity runtimeIdentity,
        String machineName,
        String serverName,
        ServerStatus newStatus) {
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
        RuntimeIdentity runtimeIdentity, String machineName, String serverName) {
      return machines.get(machineIdOf(runtimeIdentity, machineName)).getServers().get(serverName);
    }

    @Override
    public void updateMachineStatus(
        RuntimeIdentity runtimeIdentity, String machineName, MachineStatus newStatus) {
      machines.get(machineIdOf(runtimeIdentity, machineName)).setStatus(newStatus);
    }

    @Override
    public Map<String, KubernetesMachineImpl> getMachines(RuntimeIdentity runtimeIdentity) {
      return machines
          .entrySet()
          .stream()
          .filter(e -> e.getKey().getWorkspaceId().equals(runtimeIdentity.getWorkspaceId()))
          .collect(toMap(e -> e.getValue().getName(), Entry::getValue));
    }

    @Override
    public void remove(RuntimeIdentity identity) {
      machines.keySet().removeIf(id -> id.getWorkspaceId().equals(identity.getWorkspaceId()));
    }
  }
}

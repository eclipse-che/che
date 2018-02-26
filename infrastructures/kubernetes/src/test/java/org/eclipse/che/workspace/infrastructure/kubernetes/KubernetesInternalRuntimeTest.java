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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.IntOrStringBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
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
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.dto.event.MachineLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInternalRuntime.MachineLogsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapper;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesIngresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPods;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.ContainerEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerResolver;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
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
  private static final String POD_NAME = "app";
  private static final String INGRESS_NAME = "test-ingress";
  private static final String SERVICE_NAME = "test-service";
  private static final String POD_SELECTOR = "che.pod.name";
  private static final String CONTAINER_NAME_1 = "test1";
  private static final String CONTAINER_NAME_2 = "test2";

  private static final String INGRESS_HOST = "localhost";

  private static final String M1_NAME = POD_NAME + '/' + CONTAINER_NAME_1;
  private static final String M2_NAME = POD_NAME + '/' + CONTAINER_NAME_2;

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "usr1", "id1");

  @Mock private KubernetesRuntimeContext<KubernetesEnvironment> context;
  @Mock private EventService eventService;
  @Mock private ServersCheckerFactory serverCheckerFactory;
  @Mock private ServersChecker serversChecker;
  @Mock private KubernetesBootstrapperFactory bootstrapperFactory;
  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private KubernetesNamespace namespace;
  @Mock private KubernetesServices services;
  @Mock private KubernetesIngresses ingresses;
  @Mock private KubernetesPods pods;
  @Mock private KubernetesBootstrapper bootstrapper;
  @Mock private WorkspaceVolumesStrategy volumesStrategy;
  @Mock private WorkspaceProbesFactory workspaceProbesFactory;
  @Mock private ProbeScheduler probesScheduler;
  @Mock private WorkspaceProbes workspaceProbes;
  @Mock private KubernetesServerResolver kubernetesServerResolver;

  @Captor private ArgumentCaptor<MachineStatusEvent> machineStatusEventCaptor;

  private KubernetesInternalRuntime<KubernetesRuntimeContext<KubernetesEnvironment>>
      internalRuntime;

  @BeforeMethod
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    internalRuntime =
        new KubernetesInternalRuntime<>(
            13,
            5,
            new URLRewriter.NoOpURLRewriter(),
            bootstrapperFactory,
            serverCheckerFactory,
            volumesStrategy,
            probesScheduler,
            workspaceProbesFactory,
            new RuntimeEventsPublisher(eventService),
            new KubernetesSharedPool(),
            context,
            namespace,
            emptyList());

    when(context.getEnvironment()).thenReturn(k8sEnv);
    when(serverCheckerFactory.create(any(), anyString(), any())).thenReturn(serversChecker);
    when(context.getIdentity()).thenReturn(IDENTITY);
    doNothing().when(namespace).cleanUp();
    when(namespace.services()).thenReturn(services);
    when(namespace.ingresses()).thenReturn(ingresses);
    when(namespace.pods()).thenReturn(pods);
    when(bootstrapperFactory.create(any(), anyList(), any())).thenReturn(bootstrapper);
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
    final Container container = mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1, INTERNAL_PORT);
    final ImmutableMap<String, Pod> allPods =
        ImmutableMap.of(POD_NAME, mockPod(ImmutableList.of(container)));
    when(services.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(ingresses.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(ingresses.wait(any(), anyInt(), any())).thenReturn(ingress);
    when(pods.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(k8sEnv.getServices()).thenReturn(allServices);
    when(k8sEnv.getIngresses()).thenReturn(allIngresses);
    when(k8sEnv.getPods()).thenReturn(allPods);
    when(pods.waitAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(null));
    when(bootstrapper.bootstrapAsync()).thenReturn(CompletableFuture.completedFuture(null));
    when(serversChecker.startAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
  }

  @Test
  public void startsKubernetesEnvironment() throws Exception {
    final ImmutableMap<String, Pod> podsMap =
        ImmutableMap.of(
            POD_NAME,
            mockPod(
                ImmutableList.of(
                    mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1),
                    mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT))));
    when(k8sEnv.getPods()).thenReturn(podsMap);

    internalRuntime.internalStart(emptyMap());

    verify(pods).create(any());
    verify(ingresses).create(any());
    verify(services).create(any());
    verify(bootstrapper, times(2)).bootstrapAsync();
    verify(eventService, times(4)).publish(any());
    verifyOrderedEventsChains(
        new MachineStatusEvent[] {newEvent(M1_NAME, STARTING), newEvent(M1_NAME, RUNNING)},
        new MachineStatusEvent[] {newEvent(M2_NAME, STARTING), newEvent(M2_NAME, RUNNING)});
    verify(serverCheckerFactory).create(IDENTITY, M1_NAME, emptyMap());
    verify(serverCheckerFactory).create(IDENTITY, M2_NAME, emptyMap());
    verify(serversChecker, times(2)).startAsync(any());
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
      verify(namespace, never()).pods();
      throw rethrow;
    }
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void stopsWaitingAllMachineStartWhenOneMachineStartFailed() throws Exception {
    final Container container1 = mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1);
    final Container container2 = mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT);
    final ImmutableMap<String, Pod> allPods =
        ImmutableMap.of(POD_NAME, mockPod(ImmutableList.of(container1, container2)));
    when(k8sEnv.getPods()).thenReturn(allPods);
    doThrow(InfrastructureException.class).when(bootstrapper).bootstrapAsync();

    try {
      internalRuntime.internalStart(emptyMap());
    } catch (Exception rethrow) {
      verify(pods).create(any());
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
    doThrow(InfrastructureException.class).when(namespace).services();

    try {
      internalRuntime.internalStart(emptyMap());
    } catch (Exception rethrow) {
      verify(namespace).cleanUp();
      verify(namespace).services();
      verify(namespace, never()).ingresses();
      verify(namespace, never()).pods();
      throw rethrow;
    }
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "Kubernetes environment start was interrupted"
  )
  public void throwsInfrastructureExceptionWhenMachinesWaitingIsInterrupted() throws Exception {
    final Thread thread = Thread.currentThread();
    final ImmutableMap<String, Pod> podsMap =
        ImmutableMap.of(
            POD_NAME,
            mockPod(
                ImmutableList.of(
                    mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1),
                    mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT))));
    when(k8sEnv.getPods()).thenReturn(podsMap);
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
    final ContainerEvent out1 = mockContainerEvent("pulling image", "07/07/2007 19:01:22");
    final ContainerEvent out2 = mockContainerEvent("image pulled", "07/07/2007 19:08:53");
    final ArgumentCaptor<MachineLogEvent> captor = ArgumentCaptor.forClass(MachineLogEvent.class);

    internalRuntime.doStartMachine(kubernetesServerResolver);
    logsPublisher.handle(out1);
    logsPublisher.handle(out2);

    verify(eventService, atLeastOnce()).publish(captor.capture());
    final ImmutableList<MachineLogEvent> machineLogs =
        ImmutableList.of(asMachineLogEvent(out1), asMachineLogEvent(out2));
    assertTrue(captor.getAllValues().containsAll(machineLogs));
  }

  @Test
  public void testDoNotPublishForeignMachineOutput() {
    final MachineLogsPublisher logsPublisher = internalRuntime.new MachineLogsPublisher();
    final ContainerEvent out1 = mockContainerEvent("folder created", "33/03/2033 19:01:06");

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

    verify(probesScheduler).schedule(eq(workspaceProbes), any());
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
    mockName(POD_NAME, pod);
    when(spec.getContainers()).thenReturn(containers);
    when(pod.getSpec()).thenReturn(spec);
    when(pod.getMetadata().getLabels())
        .thenReturn(ImmutableMap.of(POD_SELECTOR, POD_NAME, CHE_ORIGINAL_NAME_LABEL, POD_NAME));
    return pod;
  }

  private static Service mockService() {
    final Service service = mock(Service.class);
    final ServiceSpec spec = mock(ServiceSpec.class);
    mockName(SERVICE_NAME, service);
    when(service.getSpec()).thenReturn(spec);
    when(spec.getSelector()).thenReturn(ImmutableMap.of(POD_SELECTOR, POD_NAME));
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

  private static ContainerEvent mockContainerEvent(String message, String time) {
    final ContainerEvent event = mock(ContainerEvent.class);
    when(event.getPodName()).thenReturn(POD_NAME);
    when(event.getContainerName()).thenReturn(CONTAINER_NAME_1);
    when(event.getMessage()).thenReturn(message);
    when(event.getTime()).thenReturn(time);
    return event;
  }

  private static MachineLogEvent asMachineLogEvent(ContainerEvent event) {
    return newDto(MachineLogEvent.class)
        .withRuntimeId(DtoConverter.asDto(IDENTITY))
        .withText(event.getMessage())
        .withTime(event.getTime())
        .withMachineName(event.getPodName() + '/' + event.getContainerName());
  }

  private static IntOrString intOrString(int port) {
    return new IntOrStringBuilder().withIntVal(port).withStrVal(String.valueOf(port)).build();
  }
}

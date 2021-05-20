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
package org.eclipse.che.workspace.infrastructure.openshift;

import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.STARTING;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
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
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.RouteTargetReference;
import io.opentracing.Tracer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.RuntimeCleaner;
import org.eclipse.che.workspace.infrastructure.kubernetes.RuntimeHangingDetector;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizer;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.CheNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.SecretAsContainerResourceProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftRoutes;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftPreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftServerResolverFactory;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftInternalRuntime}.
 *
 * @author Anton Korneta
 */
public class OpenShiftInternalRuntimeTest {

  private static final int EXPOSED_PORT_1 = 4401;
  private static final int EXPOSED_PORT_2 = 8081;
  private static final int INTERNAL_PORT = 4411;

  private static final String WORKSPACE_ID = "workspace123";
  private static final String POD_NAME = "app";
  private static final String ROUTE_NAME = "test-route";
  private static final String SERVICE_NAME = "test-service";
  private static final String POD_SELECTOR = "che.pod.name";
  private static final String CONTAINER_NAME_1 = "test1";
  private static final String CONTAINER_NAME_2 = "test2";
  private static final String ROUTE_HOST = "localhost";
  private static final String M1_NAME = POD_NAME + '/' + CONTAINER_NAME_1;
  private static final String M2_NAME = POD_NAME + '/' + CONTAINER_NAME_2;

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1", "infraNamespace");

  @Mock private StartSynchronizerFactory startSynchronizerFactory;
  @Mock private StartSynchronizer startSynchronizer;

  @Mock private OpenShiftRuntimeContext context;
  @Mock private EventService eventService;
  @Mock private ServersCheckerFactory serverCheckerFactory;
  @Mock private ServersChecker serversChecker;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private OpenShiftProject project;
  @Mock private KubernetesServices services;
  @Mock private KubernetesSecrets secrets;
  @Mock private KubernetesConfigsMaps configMaps;
  @Mock private OpenShiftRoutes routes;
  @Mock private KubernetesDeployments deployments;
  @Mock private WorkspaceVolumesStrategy volumesStrategy;
  @Mock private WorkspaceProbesFactory workspaceProbesFactory;
  @Mock private ProbeScheduler probesScheduler;
  @Mock private KubernetesRuntimeStateCache runtimeStateCache;
  @Mock private KubernetesMachineCache machinesCache;
  @Mock private InternalEnvironmentProvisioner internalEnvironmentProvisioner;
  @Mock private OpenShiftEnvironmentProvisioner kubernetesEnvironmentProvisioner;
  @Mock private SidecarToolingProvisioner<OpenShiftEnvironment> toolingProvisioner;
  @Mock private UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory;
  @Mock private RuntimeHangingDetector runtimeHangingDetector;
  @Mock private OpenShiftPreviewUrlCommandProvisioner previewUrlCommandProvisioner;
  @Mock private SecretAsContainerResourceProvisioner secretAsContainerResourceProvisioner;
  @Mock private CheNamespace cheNamespace;
  @Mock private ServiceExposureStrategyProvider serviceExposureStrategyProvider;
  @Mock private RuntimeCleaner runtimeCleaner;
  private OpenShiftServerResolverFactory serverResolverFactory;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private Tracer tracer;

  @Captor private ArgumentCaptor<MachineStatusEvent> machineStatusEventCaptor;

  private OpenShiftInternalRuntime internalRuntime;

  private Map<String, Service> allServices;
  private Map<String, Route> allRoutes;

  @BeforeMethod
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(startSynchronizerFactory.create(any())).thenReturn(startSynchronizer);

    serverResolverFactory =
        new OpenShiftServerResolverFactory(
            "che-host", MULTI_HOST_STRATEGY, WorkspaceExposureType.NATIVE.getConfigValue());

    internalRuntime =
        new OpenShiftInternalRuntime(
            13,
            5,
            new URLRewriter.NoOpURLRewriter(),
            unrecoverablePodEventListenerFactory,
            serverCheckerFactory,
            volumesStrategy,
            probesScheduler,
            workspaceProbesFactory,
            new RuntimeEventsPublisher(eventService),
            mock(KubernetesSharedPool.class),
            runtimeStateCache,
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
            project);

    when(context.getEnvironment()).thenReturn(osEnv);
    when(serverCheckerFactory.create(any(), anyString(), any())).thenReturn(serversChecker);
    when(context.getIdentity()).thenReturn(IDENTITY);
    doNothing().when(project).cleanUp();
    when(project.services()).thenReturn(services);
    when(project.routes()).thenReturn(routes);
    when(project.secrets()).thenReturn(secrets);
    when(project.configMaps()).thenReturn(configMaps);
    when(project.deployments()).thenReturn(deployments);
    doReturn(
            ImmutableMap.of(
                M1_NAME,
                mock(InternalMachineConfig.class),
                M2_NAME,
                mock(InternalMachineConfig.class)))
        .when(osEnv)
        .getMachines();

    allServices = ImmutableMap.of(SERVICE_NAME, mockService());
    allRoutes = ImmutableMap.of(SERVICE_NAME, mockRoute());
    final Container container = mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1, INTERNAL_PORT);
    final ImmutableMap<String, Pod> allPods =
        ImmutableMap.of(POD_NAME, mockPod(ImmutableList.of(container)));
    when(services.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(routes.create(any())).thenAnswer(a -> a.getArguments()[0]);
    when(deployments.deploy(any(Pod.class))).thenAnswer(a -> a.getArguments()[0]);
    when(osEnv.getServices()).thenReturn(allServices);
    when(osEnv.getRoutes()).thenReturn(allRoutes);
    when(osEnv.getPodsCopy()).thenReturn(allPods);
    when(osEnv.getSecrets()).thenReturn(ImmutableMap.of("secret", new Secret()));
    when(osEnv.getConfigMaps()).thenReturn(ImmutableMap.of("configMap", new ConfigMap()));
  }

  @Test
  public void shouldStartMachines() throws Exception {
    final Container container1 = mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1);
    final Container container2 = mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT);
    final ImmutableMap<String, Pod> allPods =
        ImmutableMap.of(POD_NAME, mockPod(ImmutableList.of(container1, container2)));
    when(osEnv.getPodsCopy()).thenReturn(allPods);
    when(unrecoverablePodEventListenerFactory.isConfigured()).thenReturn(true);

    internalRuntime.startMachines();

    verify(deployments).deploy(any(Pod.class));
    verify(routes).create(any());
    verify(services).create(any());
    verify(secrets).create(any());
    verify(configMaps).create(any());

    verify(project.deployments(), times(2)).watchEvents(any());
    verify(eventService, times(2)).publish(any());
    verifyEventsOrder(newEvent(M1_NAME, STARTING), newEvent(M2_NAME, STARTING));
  }

  @Test
  public void shouldStartMachinesWithoutUnrecoverableEventHandler() throws Exception {
    when(unrecoverablePodEventListenerFactory.isConfigured()).thenReturn(false);
    final Container container1 = mockContainer(CONTAINER_NAME_1, EXPOSED_PORT_1);
    final Container container2 = mockContainer(CONTAINER_NAME_2, EXPOSED_PORT_2, INTERNAL_PORT);
    final ImmutableMap<String, Pod> allPods =
        ImmutableMap.of(POD_NAME, mockPod(ImmutableList.of(container1, container2)));
    when(osEnv.getPodsCopy()).thenReturn(allPods);

    internalRuntime.startMachines();

    verify(deployments).deploy(any(Pod.class));
    verify(routes).create(any());
    verify(services).create(any());

    verify(project.deployments(), times(1)).watchEvents(any());
    verify(eventService, times(2)).publish(any());
    verifyEventsOrder(newEvent(M1_NAME, STARTING), newEvent(M2_NAME, STARTING));
  }

  private static MachineStatusEvent newEvent(String machineName, MachineStatus status) {
    return newDto(MachineStatusEvent.class)
        .withIdentity(DtoConverter.asDto(IDENTITY))
        .withMachineName(machineName)
        .withEventType(status);
  }

  private void verifyEventsOrder(MachineStatusEvent... expectedEvents) {
    final Iterator<MachineStatusEvent> actualEvents = captureEvents().iterator();
    for (MachineStatusEvent expected : expectedEvents) {
      if (!actualEvents.hasNext()) {
        fail("It is expected to receive machine status events");
      }
      final MachineStatusEvent actual = actualEvents.next();
      assertEquals(actual, expected);
    }
    if (actualEvents.hasNext()) {
      fail("No more events expected");
    }
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

  private static Route mockRoute() {
    final Route route = mock(Route.class);
    mockName(ROUTE_NAME, route);
    final RouteSpec spec = mock(RouteSpec.class);
    final RouteTargetReference target = mock(RouteTargetReference.class);
    when(target.getName()).thenReturn(SERVICE_NAME);
    when(spec.getTo()).thenReturn(target);
    when(spec.getHost()).thenReturn(ROUTE_HOST);
    when(route.getSpec()).thenReturn(spec);
    when(route.getMetadata().getLabels())
        .thenReturn(ImmutableMap.of(CHE_ORIGINAL_NAME_LABEL, ROUTE_NAME));
    return route;
  }

  private static ObjectMeta mockName(String name, HasMetadata mock) {
    final ObjectMeta metadata = mock(ObjectMeta.class);
    when(mock.getMetadata()).thenReturn(metadata);
    when(metadata.getName()).thenReturn(name);
    return metadata;
  }

  private static IntOrString intOrString(int port) {
    return new IntOrStringBuilder().withIntVal(port).withStrVal(String.valueOf(port)).build();
  }
}

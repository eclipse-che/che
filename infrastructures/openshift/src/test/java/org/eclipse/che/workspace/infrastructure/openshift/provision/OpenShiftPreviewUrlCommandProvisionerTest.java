/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RoutePort;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.RouteTargetReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.PreviewUrlImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Warnings;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftRoutes;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class OpenShiftPreviewUrlCommandProvisionerTest {

  private OpenShiftPreviewUrlCommandProvisioner previewUrlCommandProvisioner;
  @Mock private OpenShiftEnvironment mockEnvironment;
  @Mock private OpenShiftProject mockProject;
  @Mock private KubernetesServices mockServices;
  @Mock private OpenShiftRoutes mockRoutes;

  @BeforeMethod
  public void setUp() {
    previewUrlCommandProvisioner = new OpenShiftPreviewUrlCommandProvisioner();
  }

  @Test
  public void shouldDoNothingWhenGetCommandsIsNull() throws InfrastructureException {
    Mockito.when(mockEnvironment.getCommands()).thenReturn(null);

    previewUrlCommandProvisioner.provision(mockEnvironment, mockProject);
  }

  @Test(expectedExceptions = InternalInfrastructureException.class)
  public void throwsInfrastructureExceptionWhenK8sNamespaces() throws InfrastructureException {
    KubernetesNamespace namespace = Mockito.mock(KubernetesNamespace.class);
    previewUrlCommandProvisioner.provision(mockEnvironment, namespace);
  }

  @Test
  public void shouldDoNothingWhenNoCommandsDefined() throws InfrastructureException {
    Mockito.when(mockEnvironment.getCommands()).thenReturn(Collections.emptyList());
    Mockito.when(mockProject.routes()).thenReturn(mockRoutes);
    Mockito.when(mockProject.services()).thenReturn(mockServices);

    previewUrlCommandProvisioner.provision(mockEnvironment, mockProject);
  }

  @Test
  public void shouldDoNothingWhenCommandsWithoutPreviewUrlDefined() throws InfrastructureException {
    List<CommandImpl> commands =
        Arrays.asList(new CommandImpl("a", "a", "a"), new CommandImpl("b", "b", "b"));
    OpenShiftEnvironment env =
        OpenShiftEnvironment.builder().setCommands(new ArrayList<>(commands)).build();

    Mockito.when(mockProject.routes()).thenReturn(mockRoutes);
    Mockito.when(mockProject.services()).thenReturn(mockServices);

    previewUrlCommandProvisioner.provision(env, mockProject);

    assertTrue(commands.containsAll(env.getCommands()));
    assertTrue(env.getCommands().containsAll(commands));
    assertTrue(env.getWarnings().isEmpty());
  }

  @Test
  public void shouldDoNothingWhenCantFindServiceForPreviewurl() throws InfrastructureException {
    List<CommandImpl> commands =
        Collections.singletonList(
            new CommandImpl("a", "a", "a", new PreviewUrlImpl(8080, null), Collections.emptyMap()));
    OpenShiftEnvironment env =
        OpenShiftEnvironment.builder().setCommands(new ArrayList<>(commands)).build();

    Mockito.when(mockProject.routes()).thenReturn(mockRoutes);
    Mockito.when(mockProject.services()).thenReturn(mockServices);
    Mockito.when(mockServices.get()).thenReturn(Collections.emptyList());

    previewUrlCommandProvisioner.provision(env, mockProject);

    assertTrue(commands.containsAll(env.getCommands()));
    assertTrue(env.getCommands().containsAll(commands));
    assertEquals(
        env.getWarnings().get(0).getCode(), Warnings.NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL);
  }

  @Test
  public void shouldDoNothingWhenCantFindRouteForPreviewUrl() throws InfrastructureException {
    int port = 8080;
    List<CommandImpl> commands =
        Collections.singletonList(
            new CommandImpl("a", "a", "a", new PreviewUrlImpl(port, null), Collections.emptyMap()));
    OpenShiftEnvironment env =
        OpenShiftEnvironment.builder().setCommands(new ArrayList<>(commands)).build();

    Mockito.when(mockProject.services()).thenReturn(mockServices);
    Service service = new Service();
    ServiceSpec spec = new ServiceSpec();
    spec.setPorts(
        Collections.singletonList(
            new ServicePort(null, "a", null, port, "TCP", new IntOrString(port))));
    service.setSpec(spec);
    Mockito.when(mockServices.get()).thenReturn(Collections.singletonList(service));

    Mockito.when(mockProject.routes()).thenReturn(mockRoutes);
    Mockito.when(mockRoutes.get()).thenReturn(Collections.emptyList());

    previewUrlCommandProvisioner.provision(env, mockProject);

    assertTrue(commands.containsAll(env.getCommands()));
    assertTrue(env.getCommands().containsAll(commands));
    assertEquals(
        env.getWarnings().get(0).getCode(), Warnings.NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL);
  }

  @Test
  public void shouldUpdateCommandWhenServiceAndIngressFound() throws InfrastructureException {
    int port = 8080;
    List<CommandImpl> commands =
        Collections.singletonList(
            new CommandImpl("a", "a", "a", new PreviewUrlImpl(port, null), Collections.emptyMap()));
    OpenShiftEnvironment env =
        OpenShiftEnvironment.builder().setCommands(new ArrayList<>(commands)).build();

    Mockito.when(mockProject.services()).thenReturn(mockServices);
    Service service = new Service();
    ObjectMeta metadata = new ObjectMeta();
    metadata.setName("servicename");
    service.setMetadata(metadata);
    ServiceSpec spec = new ServiceSpec();
    spec.setPorts(
        Collections.singletonList(
            new ServicePort(null, "8080", null, port, "TCP", new IntOrString(port))));
    service.setSpec(spec);
    Mockito.when(mockServices.get()).thenReturn(Collections.singletonList(service));

    Route route = new Route();
    RouteSpec routeSpec = new RouteSpec();
    routeSpec.setPort(new RoutePort(new IntOrString("8080")));
    routeSpec.setTo(new RouteTargetReference("a", "servicename", 1));
    routeSpec.setHost("testhost");
    route.setSpec(routeSpec);

    Mockito.when(mockProject.routes()).thenReturn(mockRoutes);
    Mockito.when(mockRoutes.get()).thenReturn(Collections.singletonList(route));

    previewUrlCommandProvisioner.provision(env, mockProject);

    assertTrue(env.getCommands().get(0).getAttributes().containsKey("previewUrl"));
    assertEquals(env.getCommands().get(0).getAttributes().get("previewUrl"), "testhost");
    assertTrue(env.getWarnings().isEmpty());
  }
}

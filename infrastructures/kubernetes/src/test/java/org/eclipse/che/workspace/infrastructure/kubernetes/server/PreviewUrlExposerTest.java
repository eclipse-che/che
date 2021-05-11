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

package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.PreviewUrlImpl;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressServerExposer;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PreviewUrlExposerTest {

  private PreviewUrlExposer<KubernetesEnvironment> previewUrlExposer;

  @Mock private ExternalServiceExposureStrategy externalServiceExposureStrategy;

  @BeforeMethod
  public void setUp() {
    IngressServerExposer externalServerExposer =
        new IngressServerExposer(
            externalServiceExposureStrategy, Collections.emptyMap(), null, null);
    previewUrlExposer = new PreviewUrlExposer<>(externalServerExposer);
  }

  @Test
  public void shouldDoNothingWhenNoCommandsDefined() throws InternalInfrastructureException {
    KubernetesEnvironment env = KubernetesEnvironment.builder().build();

    previewUrlExposer.expose(env);

    assertTrue(env.getCommands().isEmpty());
    assertTrue(env.getServices().isEmpty());
    assertTrue(env.getIngresses().isEmpty());
  }

  @Test
  public void shouldDoNothingWhenNoCommandWithPreviewUrlDefined()
      throws InternalInfrastructureException {
    CommandImpl command = new CommandImpl("a", "a", "a");
    KubernetesEnvironment env =
        KubernetesEnvironment.builder()
            .setCommands(singletonList(new CommandImpl(command)))
            .build();

    previewUrlExposer.expose(env);

    assertEquals(env.getCommands().get(0), command);
    assertTrue(env.getServices().isEmpty());
    assertTrue(env.getIngresses().isEmpty());
  }

  @Test
  public void shouldNotProvisionWhenServiceAndIngressFound()
      throws InternalInfrastructureException {
    final int PORT = 8080;
    final String SERVER_PORT_NAME = "server-" + PORT;

    CommandImpl command =
        new CommandImpl("a", "a", "a", new PreviewUrlImpl(PORT, null), Collections.emptyMap());

    Service service = new Service();
    ObjectMeta serviceMeta = new ObjectMeta();
    serviceMeta.setName("servicename");
    service.setMetadata(serviceMeta);
    ServiceSpec serviceSpec = new ServiceSpec();
    serviceSpec.setPorts(
        singletonList(
            new ServicePort(null, SERVER_PORT_NAME, null, PORT, "TCP", new IntOrString(PORT))));
    service.setSpec(serviceSpec);

    Ingress ingress = new Ingress();
    ObjectMeta ingressMeta = new ObjectMeta();
    ingressMeta.setName("ingressname");
    ingress.setMetadata(ingressMeta);
    IngressSpec ingressSpec = new IngressSpec();
    IngressRule ingressRule = new IngressRule();
    ingressRule.setHost("ingresshost");
    IngressBackend ingressBackend =
        new IngressBackend(null, "servicename", new IntOrString(SERVER_PORT_NAME));
    ingressRule.setHttp(
        new HTTPIngressRuleValue(singletonList(new HTTPIngressPath(ingressBackend, null, null))));
    ingressSpec.setRules(singletonList(ingressRule));
    ingress.setSpec(ingressSpec);

    Map<String, Service> services = new HashMap<>();
    services.put("servicename", service);
    Map<String, Ingress> ingresses = new HashMap<>();
    ingresses.put("ingressname", ingress);

    KubernetesEnvironment env =
        KubernetesEnvironment.builder()
            .setCommands(singletonList(new CommandImpl(command)))
            .setServices(services)
            .setIngresses(ingresses)
            .build();

    assertEquals(env.getIngresses().size(), 1);
    previewUrlExposer.expose(env);
    assertEquals(env.getIngresses().size(), 1);
  }

  @Test
  public void shouldProvisionIngressWhenNotFound() throws InternalInfrastructureException {
    Mockito.when(
            externalServiceExposureStrategy.getExternalPath(Mockito.anyString(), Mockito.any()))
        .thenReturn("some-server-path");

    final int PORT = 8080;
    final String SERVER_PORT_NAME = "server-" + PORT;
    final String SERVICE_NAME = "servicename";

    CommandImpl command =
        new CommandImpl("a", "a", "a", new PreviewUrlImpl(PORT, null), Collections.emptyMap());

    Service service = new Service();
    ObjectMeta serviceMeta = new ObjectMeta();
    serviceMeta.setName(SERVICE_NAME);
    service.setMetadata(serviceMeta);
    ServiceSpec serviceSpec = new ServiceSpec();
    serviceSpec.setPorts(
        singletonList(
            new ServicePort(null, SERVER_PORT_NAME, null, PORT, "TCP", new IntOrString(PORT))));
    service.setSpec(serviceSpec);

    Map<String, Service> services = new HashMap<>();
    services.put(SERVICE_NAME, service);

    KubernetesEnvironment env =
        KubernetesEnvironment.builder()
            .setCommands(singletonList(new CommandImpl(command)))
            .setServices(services)
            .setIngresses(new HashMap<>())
            .build();

    previewUrlExposer.expose(env);
    assertEquals(env.getIngresses().size(), 1);
    Ingress provisionedIngress = env.getIngresses().values().iterator().next();
    IngressBackend provisionedIngressBackend =
        provisionedIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getBackend();
    assertEquals(provisionedIngressBackend.getServicePort().getStrVal(), SERVER_PORT_NAME);
    assertEquals(provisionedIngressBackend.getServiceName(), SERVICE_NAME);
  }

  @Test
  public void shouldProvisionServiceAndIngressWhenNotFound()
      throws InternalInfrastructureException {
    Mockito.when(
            externalServiceExposureStrategy.getExternalPath(Mockito.anyString(), Mockito.any()))
        .thenReturn("some-server-path");

    final int PORT = 8080;
    final String SERVER_PORT_NAME = "server-" + PORT;

    CommandImpl command =
        new CommandImpl("a", "a", "a", new PreviewUrlImpl(PORT, null), Collections.emptyMap());

    KubernetesEnvironment env =
        KubernetesEnvironment.builder()
            .setCommands(singletonList(new CommandImpl(command)))
            .setIngresses(new HashMap<>())
            .setServices(new HashMap<>())
            .build();

    previewUrlExposer.expose(env);

    assertEquals(env.getIngresses().size(), 1);
    assertEquals(env.getServices().size(), 1);

    Service provisionedService = env.getServices().values().iterator().next();
    ServicePort provisionedServicePort = provisionedService.getSpec().getPorts().get(0);
    assertEquals(provisionedServicePort.getName(), SERVER_PORT_NAME);
    assertEquals(provisionedServicePort.getPort().intValue(), PORT);

    Ingress provisionedIngress = env.getIngresses().values().iterator().next();
    IngressBackend provisionedIngressBackend =
        provisionedIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getBackend();
    assertEquals(provisionedIngressBackend.getServicePort().getStrVal(), SERVER_PORT_NAME);
    assertEquals(
        provisionedIngressBackend.getServiceName(), provisionedService.getMetadata().getName());
  }
}

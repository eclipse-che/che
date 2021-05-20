/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.ProxySettingsProvisioner.HTTPS_PROXY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.ProxySettingsProvisioner.HTTP_PROXY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.ProxySettingsProvisioner.NO_PROXY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_POD_NAME;
import static org.mockito.Mockito.lenient;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class ProxySettingsProvisionerTest {

  private static final String WORKSPACE_ID = "workspace123";

  private static final String HTTP_PROXY_VALUE = "http.proxy1.somewhere.com:3128";
  private static final String HTTPS_PROXY_VALUE = "https.proxy2.somewhere.com:8080";
  private static final String NO_PROXY_VALUE = "localhost,127.0.0.1";

  @Mock private RuntimeIdentity runtimeId;

  private ProxySettingsProvisioner provisioner;

  @BeforeMethod
  public void setUp() {
    lenient().when(runtimeId.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    provisioner = new ProxySettingsProvisioner(HTTPS_PROXY_VALUE, HTTP_PROXY_VALUE, NO_PROXY_VALUE);
  }

  @Test
  public void shouldApplyProxySettingsToAllContainers() throws Exception {

    Map<String, Pod> pods = new HashMap<>();
    Pod pod1 = pods.put("pod1", buildPod("pod1", buildContainers(2)));
    pods.put("pod2", buildPod("pod2", buildContainers(3)));

    KubernetesEnvironment k8sEnv = KubernetesEnvironment.builder().setPods(pods).build();
    provisioner.provision(k8sEnv, runtimeId);

    assertTrue(
        k8sEnv
            .getPodsData()
            .values()
            .stream()
            .flatMap(pod -> pod.getSpec().getContainers().stream())
            .allMatch(
                container ->
                    container.getEnv().contains(new EnvVar(HTTP_PROXY, HTTP_PROXY_VALUE, null))
                        && container
                            .getEnv()
                            .contains(new EnvVar(HTTPS_PROXY, HTTPS_PROXY_VALUE, null))
                        && container
                            .getEnv()
                            .contains(new EnvVar(NO_PROXY, NO_PROXY_VALUE, null))));
  }

  @Test
  public void shouldNotApplyProxySettingsToJWTProxyContainer() throws Exception {

    Map<String, Pod> pods = new HashMap<>();
    pods.put(JWT_PROXY_POD_NAME, buildPod(JWT_PROXY_POD_NAME, buildContainers(2)));

    KubernetesEnvironment k8sEnv = KubernetesEnvironment.builder().setPods(pods).build();
    provisioner.provision(k8sEnv, runtimeId);

    assertTrue(
        k8sEnv
            .getPodsData()
            .values()
            .stream()
            .filter(pod -> pod.getMetadata().getName().equals(JWT_PROXY_POD_NAME))
            .flatMap(pod -> pod.getSpec().getContainers().stream())
            .noneMatch(
                container ->
                    container.getEnv().contains(new EnvVar(HTTP_PROXY, HTTP_PROXY_VALUE, null))
                        || container
                            .getEnv()
                            .contains(new EnvVar(HTTPS_PROXY, HTTPS_PROXY_VALUE, null))
                        || container
                            .getEnv()
                            .contains(new EnvVar(NO_PROXY, NO_PROXY_VALUE, null))));
  }

  @Test
  public void shouldApplyProxySettingsToInitContainers() throws Exception {
    Map<String, Pod> pods = new HashMap<>();
    Pod pod1 = buildPod("pod1", buildContainers(3));
    pod1.getSpec().setInitContainers(Arrays.asList(buildContainers(2)));
    pods.put("pod1", pod1);

    KubernetesEnvironment k8sEnv = KubernetesEnvironment.builder().setPods(pods).build();
    provisioner.provision(k8sEnv, runtimeId);

    assertTrue(
        k8sEnv
            .getPodsData()
            .values()
            .stream()
            .flatMap(
                pod ->
                    Stream.concat(
                        pod.getSpec().getContainers().stream(),
                        pod.getSpec().getInitContainers().stream()))
            .allMatch(
                container ->
                    container.getEnv().contains(new EnvVar(HTTP_PROXY, HTTP_PROXY_VALUE, null))
                        && container
                            .getEnv()
                            .contains(new EnvVar(HTTPS_PROXY, HTTPS_PROXY_VALUE, null))
                        && container
                            .getEnv()
                            .contains(new EnvVar(NO_PROXY, NO_PROXY_VALUE, null))));
  }

  private Pod buildPod(String podName, Container... containers) {
    return new PodBuilder()
        .withNewMetadata()
        .withName(podName)
        .endMetadata()
        .withNewSpec()
        .withContainers(containers)
        .endSpec()
        .build();
  }

  private Container[] buildContainers(int size) {
    List<Container> result = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      result.add(
          new ContainerBuilder()
              .withName(generate("container-", 4))
              .withNewResources()
              .endResources()
              .build());
    }
    return result.toArray(new Container[size]);
  }
}

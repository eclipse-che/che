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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertSame;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.opentracing.Tracer;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListener;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link DeployBroker}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class DeployBrokerTest {

  public static final String PLUGIN_BROKER_POD_NAME = "pluginBrokerPodName";
  @Mock private BrokerPhase nextBrokerPhase;

  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesDeployments k8sDeployments;
  @Mock private KubernetesConfigsMaps k8sConfigMaps;
  @Mock private KubernetesSecrets k8sSecrets;

  @Mock private KubernetesEnvironment k8sEnvironment;
  @Mock private ConfigMap configMap;
  @Mock private Secret tlsSecret;
  private Pod pod;

  @Mock private BrokersResult brokersResult;
  @Mock private UnrecoverablePodEventListenerFactory unrecoverableEventListenerFactory;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private Tracer tracer;

  private List<ChePlugin> plugins = emptyList();

  private DeployBroker deployBrokerPhase;

  @BeforeMethod
  public void setUp() throws Exception {
    deployBrokerPhase =
        new DeployBroker(
            "workspaceId",
            k8sNamespace,
            k8sEnvironment,
            brokersResult,
            unrecoverableEventListenerFactory,
            tracer);
    deployBrokerPhase.then(nextBrokerPhase);

    when(nextBrokerPhase.execute()).thenReturn(plugins);

    when(k8sNamespace.configMaps()).thenReturn(k8sConfigMaps);
    when(k8sNamespace.deployments()).thenReturn(k8sDeployments);
    when(k8sNamespace.secrets()).thenReturn(k8sSecrets);

    pod = new PodBuilder().withNewMetadata().withName(PLUGIN_BROKER_POD_NAME).endMetadata().build();
    when(k8sEnvironment.getPodsCopy()).thenReturn(ImmutableMap.of(PLUGIN_BROKER_POD_NAME, pod));
    when(k8sEnvironment.getConfigMaps()).thenReturn(ImmutableMap.of("configMap", configMap));
    when(k8sEnvironment.getSecrets()).thenReturn(ImmutableMap.of("secret", tlsSecret));

    when(k8sDeployments.create(any())).thenReturn(pod);
  }

  @Test
  public void shouldDeployPluginBrokerEnvironment() throws Exception {
    // when
    List<ChePlugin> result = deployBrokerPhase.execute();

    // then
    assertSame(result, plugins);
    verify(k8sConfigMaps).create(configMap);
    verify(k8sDeployments).create(pod);
    verify(k8sSecrets).create(tlsSecret);

    verify(k8sDeployments).stopWatch();
    verify(k8sDeployments).delete();
    verify(k8sConfigMaps).delete();
    verify(k8sSecrets).delete();
  }

  @Test
  public void shouldListenToUnrecoverableEventsIfFactoryIsConfigured() throws Exception {
    // given
    when(unrecoverableEventListenerFactory.isConfigured()).thenReturn(true);
    UnrecoverablePodEventListener listener = mock(UnrecoverablePodEventListener.class);
    when(unrecoverableEventListenerFactory.create(any(Set.class), any())).thenReturn(listener);

    // when
    deployBrokerPhase.execute();

    // then
    verify(unrecoverableEventListenerFactory).isConfigured();
    verify(unrecoverableEventListenerFactory)
        .create(eq(ImmutableSet.of(PLUGIN_BROKER_POD_NAME)), any());
    verify(k8sDeployments).watchEvents(listener);
    verify(k8sDeployments).stopWatch();
  }

  @Test
  public void shouldDoNotListenToUnrecoverableEventsIfFactoryIsConfigured() throws Exception {
    // given
    when(unrecoverableEventListenerFactory.isConfigured()).thenReturn(false);

    // when
    deployBrokerPhase.execute();

    // then
    verify(unrecoverableEventListenerFactory).isConfigured();
    verify(unrecoverableEventListenerFactory, never())
        .create(eq(ImmutableSet.of(PLUGIN_BROKER_POD_NAME)), any());
    verify(k8sDeployments, never()).watchEvents(any());
    verify(k8sDeployments).stopWatch();
  }

  @Test(
      expectedExceptions = InternalInfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Plugin broker environment must have only one pod\\. Workspace `workspaceId` contains `0` pods\\.")
  public void shouldThrowExceptionIfThereIsNoAnyPodsInEnvironment() throws Exception {
    // given
    when(k8sEnvironment.getPodsCopy()).thenReturn(emptyMap());

    // when
    deployBrokerPhase.execute();
  }

  @Test(
      expectedExceptions = InternalInfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Plugin broker environment must have only one pod\\. Workspace `workspaceId` contains `2` pods\\.")
  public void shouldThrowExceptionIfThereAreMoreThanOnePodsInEnvironment() throws Exception {
    // given
    when(k8sEnvironment.getPodsCopy()).thenReturn(ImmutableMap.of("pod1", pod, "pod2", pod));

    // when
    deployBrokerPhase.execute();
  }
}

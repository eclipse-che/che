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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory.BrokersConfigs;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class BrokerEnvironmentFactoryTest {

  private static final String INIT_IMAGE = "init:image";
  private static final String UNIFIED_BROKER_IMAGE = "unified:image";
  private static final String DEFAULT_REGISTRY = "default.registry";
  private static final String IMAGE_PULL_POLICY = "Never";
  private static final String PUSH_ENDPOINT = "http://localhost:8080";

  @Mock private CertificateProvisioner certProvisioner;
  @Mock private AgentAuthEnableEnvVarProvider authEnableEnvVarProvider;
  @Mock private MachineTokenEnvVarProvider machineTokenEnvVarProvider;
  @Mock private RuntimeIdentity runtimeId;

  private BrokerEnvironmentFactory<KubernetesEnvironment> factory;

  @BeforeMethod
  public void setUp() throws Exception {
    factory =
        spy(
            new BrokerEnvironmentFactory<KubernetesEnvironment>(
                PUSH_ENDPOINT,
                IMAGE_PULL_POLICY,
                authEnableEnvVarProvider,
                machineTokenEnvVarProvider,
                UNIFIED_BROKER_IMAGE,
                INIT_IMAGE,
                DEFAULT_REGISTRY,
                certProvisioner) {
              @Override
              protected KubernetesEnvironment doCreate(BrokersConfigs brokersConfigs) {
                return null;
              }
            });

    when(authEnableEnvVarProvider.get(any(RuntimeIdentity.class)))
        .thenReturn(new Pair<>("test1", "value1"));
    when(machineTokenEnvVarProvider.get(any(RuntimeIdentity.class)))
        .thenReturn(new Pair<>("test2", "value2"));
    when(runtimeId.getEnvName()).thenReturn("env");
    when(runtimeId.getOwnerId()).thenReturn("owner");
    when(runtimeId.getWorkspaceId()).thenReturn("wsid");
    when(certProvisioner.isConfigured()).thenReturn(false);
  }

  @Test
  public void testInitBrokerContainer() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.create(pluginFQNs, runtimeId);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    List<Container> initContainers = brokersConfigs.pod.getSpec().getInitContainers();
    assertEquals(initContainers.size(), 1);
    Container initContainer = initContainers.get(0);
    assertEquals(initContainer.getName(), "init-image");
    assertEquals(initContainer.getImage(), INIT_IMAGE);
    assertEquals(initContainer.getImagePullPolicy(), IMAGE_PULL_POLICY);
    assertEquals(
        initContainer.getEnv(),
        asList(new EnvVar("test1", "value1", null), new EnvVar("test2", "value2", null)));
    assertEquals(
        initContainer.getArgs().toArray(),
        new String[] {
          "-push-endpoint",
          PUSH_ENDPOINT,
          "-runtime-id",
          String.format(
              "%s:%s:%s",
              runtimeId.getWorkspaceId(), runtimeId.getEnvName(), runtimeId.getOwnerId()),
          "-cacert",
          "",
          "--registry-address",
          DEFAULT_REGISTRY
        });
    assertEquals(Containers.getRamLimit(initContainer), 262144000);
    assertEquals(Containers.getRamLimit(initContainer), 262144000);
  }

  @Test
  public void testSelfSignedCertificate() throws Exception {
    when(certProvisioner.isConfigured()).thenReturn(true);
    when(certProvisioner.getCertPath()).thenReturn("/tmp/che/cacert");
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.create(pluginFQNs, runtimeId);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();

    List<Container> initContainers = brokersConfigs.pod.getSpec().getInitContainers();
    assertEquals(initContainers.size(), 1);
    Container initContainer = initContainers.get(0);
    assertEquals(
        initContainer.getArgs().toArray(),
        new String[] {
          "-push-endpoint",
          PUSH_ENDPOINT,
          "-runtime-id",
          String.format(
              "%s:%s:%s",
              runtimeId.getWorkspaceId(), runtimeId.getEnvName(), runtimeId.getOwnerId()),
          "-cacert",
          "/tmp/che/cacert",
          "--registry-address",
          DEFAULT_REGISTRY,
        });

    List<Container> containers = brokersConfigs.pod.getSpec().getContainers();
    assertEquals(containers.size(), 1);
    Container container = containers.get(0);
    assertEquals(
        container.getArgs().toArray(),
        new String[] {
          "-push-endpoint",
          PUSH_ENDPOINT,
          "-runtime-id",
          String.format(
              "%s:%s:%s",
              runtimeId.getWorkspaceId(), runtimeId.getEnvName(), runtimeId.getOwnerId()),
          "-cacert",
          "/tmp/che/cacert",
          "--registry-address",
          DEFAULT_REGISTRY,
          "-metas",
          "/broker-config/config.json",
        });
  }

  @Test
  public void shouldNameContainersAfterPluginBrokerImage() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.create(pluginFQNs, runtimeId);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    PodSpec brokerPodSpec = brokersConfigs.pod.getSpec();

    List<Container> initContainers = brokerPodSpec.getInitContainers();
    assertEquals(initContainers.size(), 1);
    assertEquals(initContainers.get(0).getName(), "init-image");

    List<Container> containers = brokerPodSpec.getContainers();
    assertEquals(containers.size(), 1);
    assertEquals(containers.get(0).getName(), "unified-image");
  }

  @Test
  public void shouldCreateConfigMapWithPluginFQNs() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs =
        ImmutableList.of(
            new PluginFQN(null, "testPublisher/testPlugin1/testver1"),
            new PluginFQN(new URI("testregistry"), "testPublisher/testPlugin2/testver2"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.create(pluginFQNs, runtimeId);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    ConfigMap brokerConfigMap = brokersConfigs.configMaps.values().iterator().next();
    String config = brokerConfigMap.getData().get(BrokerEnvironmentFactory.CONFIG_FILE);

    assertFalse(config.contains("\"registry\":null"), "Should not serialize null registry");
    List<String> expected =
        ImmutableList.of(
            "\"id\":\"testPublisher/testPlugin1/testver1\"",
            "\"registry\":\"testregistry\"",
            "\"id\":\"testPublisher/testPlugin2/testver2\"");
    for (String expect : expected) {
      assertTrue(
          config.contains(expect),
          String.format(
              "Missing field from serialized config: expected '%s' in '%s'", expect, config));
    }
  }
}

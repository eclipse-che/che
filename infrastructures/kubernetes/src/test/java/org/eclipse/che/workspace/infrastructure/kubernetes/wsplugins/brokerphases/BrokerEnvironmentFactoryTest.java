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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

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
import io.fabric8.kubernetes.api.model.PodSpec;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory.BrokersConfigs;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class BrokerEnvironmentFactoryTest {

  private static final String ARTIFACTS_BROKER_IMAGE = "artifacts:image";
  private static final String METADATA_BROKER_IMAGE = "metadata:image";
  private static final String DEFAULT_REGISTRY = "default.registry";
  private static final String IMAGE_PULL_POLICY = "Never";
  private static final String PUSH_ENDPOINT = "http://localhost:8080";
  private static final String PLUGINS_VOLUME_NAME = "plugins";
  private static final String CA_CERTIFICATES_MOUNT_PATH = "/public-certs";
  @Mock private CertificateProvisioner certProvisioner;
  @Mock private TrustedCAProvisioner trustedCAProvisioner;
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
                ARTIFACTS_BROKER_IMAGE,
                METADATA_BROKER_IMAGE,
                DEFAULT_REGISTRY,
                "",
                trustedCAProvisioner,
                CA_CERTIFICATES_MOUNT_PATH,
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
  public void testMetadataBrokerSelfSignedCertificate() throws Exception {
    when(certProvisioner.isConfigured()).thenReturn(true);
    when(certProvisioner.getCertPath()).thenReturn("/tmp/che/cacert");
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForMetadataBroker(pluginFQNs, runtimeId, false);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();

    List<Container> containers =
        brokersConfigs
            .pods
            .values()
            .stream()
            .flatMap(p -> p.getSpec().getContainers().stream())
            .collect(Collectors.toList());
    assertEquals(containers.size(), 1);
    Container container = containers.get(0);
    assertEquals(
        container.getArgs().toArray(),
        new String[] {
          "--push-endpoint",
          PUSH_ENDPOINT,
          "--runtime-id",
          String.format(
              "%s:%s:%s",
              runtimeId.getWorkspaceId(), runtimeId.getEnvName(), runtimeId.getOwnerId()),
          "--cacert",
          "/tmp/che/cacert",
          "--registry-address",
          DEFAULT_REGISTRY,
          "--metas",
          "/broker-config/config.json",
        });
  }

  @Test
  public void testArtifactsBrokerSelfSignedCertificate() throws Exception {
    when(certProvisioner.isConfigured()).thenReturn(true);
    when(certProvisioner.getCertPath()).thenReturn("/tmp/che/cacert");
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForArtifactsBroker(pluginFQNs, runtimeId, false);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();

    List<Container> containers =
        brokersConfigs
            .pods
            .values()
            .stream()
            .flatMap(p -> p.getSpec().getContainers().stream())
            .collect(Collectors.toList());
    assertEquals(containers.size(), 1);
    Container container = containers.get(0);
    assertEquals(
        container.getArgs().toArray(),
        new String[] {
          "--push-endpoint",
          PUSH_ENDPOINT,
          "--runtime-id",
          String.format(
              "%s:%s:%s",
              runtimeId.getWorkspaceId(), runtimeId.getEnvName(), runtimeId.getOwnerId()),
          "--cacert",
          "/tmp/che/cacert",
          "--registry-address",
          DEFAULT_REGISTRY,
          "--metas",
          "/broker-config/config.json",
        });
  }

  @Test
  public void testAddsMergeArgToArtifactsBroker() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForArtifactsBroker(pluginFQNs, runtimeId, true);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();

    List<Container> containers =
        brokersConfigs
            .pods
            .values()
            .stream()
            .flatMap(p -> p.getSpec().getContainers().stream())
            .collect(Collectors.toList());
    assertEquals(containers.size(), 1);
    Container container = containers.get(0);
    assertTrue(container.getArgs().stream().anyMatch(e -> "--merge-plugins".equals(e)));
  }

  @Test
  public void testAddsMergeArgToMetadataBroker() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForMetadataBroker(pluginFQNs, runtimeId, true);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();

    List<Container> containers =
        brokersConfigs
            .pods
            .values()
            .stream()
            .flatMap(p -> p.getSpec().getContainers().stream())
            .collect(Collectors.toList());
    assertEquals(containers.size(), 1);
    Container container = containers.get(0);
    assertTrue(container.getArgs().stream().anyMatch(e -> "--merge-plugins".equals(e)));
  }

  @Test
  public void shouldNameContainersAfterMetadataPluginBrokerImage() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForMetadataBroker(pluginFQNs, runtimeId, false);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    PodSpec brokerPodSpec = brokersConfigs.pods.values().iterator().next().getSpec();

    List<Container> containers = brokerPodSpec.getContainers();
    assertEquals(containers.size(), 1);
    assertEquals(containers.get(0).getName(), "metadata-image");
  }

  @Test
  public void shouldNameContainersAfterArtifactsPluginBrokerImage() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForArtifactsBroker(pluginFQNs, runtimeId, false);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    PodSpec brokerPodSpec = brokersConfigs.pods.values().iterator().next().getSpec();

    List<Container> containers = brokerPodSpec.getContainers();
    assertEquals(containers.size(), 1);
    assertEquals(containers.get(0).getName(), "artifacts-image");
  }

  @Test
  public void shouldCreateConfigMapWithPluginFQNsWithMetadataBroker() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs =
        ImmutableList.of(
            new PluginFQN(null, "testPublisher/testPlugin1/testver1"),
            new PluginFQN(new URI("testregistry"), "testPublisher/testPlugin2/testver2"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForMetadataBroker(pluginFQNs, runtimeId, false);

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

  @Test
  public void shouldCreateConfigMapWithPluginFQNsWithArtifactsBroker() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs =
        ImmutableList.of(
            new PluginFQN(null, "testPublisher/testPlugin1/testver1"),
            new PluginFQN(new URI("testregistry"), "testPublisher/testPlugin2/testver2"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForArtifactsBroker(pluginFQNs, runtimeId, false);

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

  @Test
  public void shouldIncludePluginsVolumeInArtifactsBroker() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForArtifactsBroker(pluginFQNs, runtimeId, false);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    InternalMachineConfig machine = brokersConfigs.machines.values().iterator().next();
    assertTrue(machine.getVolumes().containsKey(PLUGINS_VOLUME_NAME));
    assertEquals(machine.getVolumes().get(PLUGINS_VOLUME_NAME).getPath(), "/plugins");
  }

  @Test
  public void shouldNotIncludePluginsVolumeInMetadataBroker() throws Exception {
    // given
    Collection<PluginFQN> pluginFQNs = singletonList(new PluginFQN(null, "id"));
    ArgumentCaptor<BrokersConfigs> captor = ArgumentCaptor.forClass(BrokersConfigs.class);

    // when
    factory.createForMetadataBroker(pluginFQNs, runtimeId, false);

    // then
    verify(factory).doCreate(captor.capture());
    BrokersConfigs brokersConfigs = captor.getValue();
    InternalMachineConfig machine = brokersConfigs.machines.values().iterator().next();
    assertFalse(machine.getVolumes().containsKey(PLUGINS_VOLUME_NAME));
  }

  @Test(dataProvider = "imageRefs")
  public void testImageToContainerNameConversion(Object image, Object expected) {
    String actual = factory.generateContainerNameFromImageRef((String) image);
    assertEquals(
        actual,
        expected,
        String.format("Should generate name '%s' from image '%s'.", expected, image));
  }

  @DataProvider(name = "imageRefs")
  public Object[][] imageRefs() {
    return new Object[][] {
      {"quay.io/eclipse/che-unified-plugin-broker:v0.20", "che-unified-plugin-broker-v0-20"},
      {"very-long-registry-hostname-url.service/eclipse/image:tag", "image-tag"},
      {"eclipse/che-unified-plugin-broker:v0.20", "che-unified-plugin-broker-v0-20"},
      {"very-long-organization.name-eclipse-che/image:tag", "image-tag"},
      {"very-long-registry-hostname-url.service/very-long-organization/image:tag", "image-tag"},
      {
        "image-with-digest@sha256:7b868470f7b63d9da10a788d26abf4c076f90dc4c7de24d1298a8160c9a3dcc9",
        "image-with-digest-7b868470f7"
      },
      {"image-with-short-digest@sha256:abcd", "image-with-short-digest-abcd"},
      {"no-exception-when-no-colon@sha256abcd", "no-exception-when-no-colon-sha256abcd"},
      {
        "image-and-tag-longer-than-63-chars:really-long-tag-for-some-reason",
        "image-and-tag-longer-than-63-chars-really-long-tag-for-some-rea"
      }
    };
  }
}

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

import static org.eclipse.che.workspace.infrastructure.kubernetes.Names.createMachineNameAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesArtifactsBrokerApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesArtifactsBrokerApplier}.
 *
 * @author Angel Misevski
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesArtifactsBrokerApplierTest {

  private static final String WORKSPACE_POD_NAME = "workspacePod";
  private static final String WORKSPACE_MACHINE_NAME = "workspaceMachine";
  private static final String WORKSPACE_CONTAINER_NAME = "workspaceContainer";
  private static final Map<String, String> workspacePodAnnotations =
      createMachineNameAnnotations(WORKSPACE_CONTAINER_NAME, WORKSPACE_MACHINE_NAME);

  private static final String BROKER_POD_NAME = "brokerPod";
  private static final String BROKER_MACHINE_NAME = "brokerMachine";
  private static final String BROKER_CONTAINER_NAME = "brokerContainer";
  private static final String BROKER_CONFIGMAP_NAME = "brokerConfigMap";
  private static final Map<String, String> brokerPodAnnotations =
      createMachineNameAnnotations(BROKER_CONTAINER_NAME, BROKER_MACHINE_NAME);

  private static final Map<String, String> brokerConfigMapData =
      ImmutableMap.of("brokerConfigKey", "brokerConfigValue");

  @Mock private BrokerEnvironmentFactory<KubernetesEnvironment> brokerEnvironmentFactory;
  @Mock private RuntimeIdentity runtimeID;
  @Mock private Collection<PluginFQN> pluginFQNs;

  // Broker Environment mocks
  @Mock private InternalMachineConfig brokerMachine;
  private Volume brokerVolume;
  private ConfigMap brokerConfigMap;
  private Container brokerContainer;

  // Workspace Environment mocks
  private KubernetesEnvironment workspaceEnvironment;
  private Pod workspacePod;

  private KubernetesArtifactsBrokerApplier<KubernetesEnvironment> applier;

  @BeforeMethod
  public void setUp() throws Exception {
    // Workspace env setup
    ObjectMeta workspacePodMeta =
        new ObjectMetaBuilder().withAnnotations(workspacePodAnnotations).build();
    workspacePod = new PodBuilder().withMetadata(workspacePodMeta).withSpec(new PodSpec()).build();
    Map<String, ConfigMap> workspaceConfigMaps = new HashMap<>();
    workspaceEnvironment =
        KubernetesEnvironment.builder()
            .setPods(ImmutableMap.of(WORKSPACE_POD_NAME, workspacePod))
            .setMachines(new HashMap<>())
            .setConfigMaps(workspaceConfigMaps)
            .build();

    // Broker env setup
    ObjectMeta brokerPodMeta =
        new ObjectMetaBuilder().withAnnotations(brokerPodAnnotations).build();
    brokerContainer = new ContainerBuilder().withName(BROKER_CONTAINER_NAME).build();
    brokerVolume = new VolumeBuilder().build();
    Pod brokerPod =
        new PodBuilder()
            .withMetadata(brokerPodMeta)
            .withNewSpec()
            .withContainers(brokerContainer)
            .withVolumes(brokerVolume)
            .endSpec()
            .build();
    brokerConfigMap = new ConfigMapBuilder().addToData(brokerConfigMapData).build();
    KubernetesEnvironment brokerEnvironment =
        KubernetesEnvironment.builder()
            .setPods(ImmutableMap.of(BROKER_POD_NAME, brokerPod))
            .setConfigMaps(ImmutableMap.of(BROKER_CONFIGMAP_NAME, brokerConfigMap))
            .setMachines(ImmutableMap.of(BROKER_MACHINE_NAME, brokerMachine))
            .build();
    doReturn(brokerEnvironment)
        .when(brokerEnvironmentFactory)
        .createForArtifactsBroker(any(), any(), anyBoolean());

    applier = new KubernetesArtifactsBrokerApplier<>(brokerEnvironmentFactory);
  }

  @Test
  public void shouldAddBrokerMachineToWorkspaceEnvironment() throws Exception {
    applier.apply(workspaceEnvironment, runtimeID, pluginFQNs, false);

    assertNotNull(workspaceEnvironment.getMachines());
    assertTrue(workspaceEnvironment.getMachines().values().contains(brokerMachine));
  }

  @Test
  public void shouldAddBrokerConfigMapsToWorkspaceEnvironment() throws Exception {
    applier.apply(workspaceEnvironment, runtimeID, pluginFQNs, false);

    ConfigMap workspaceConfigMap = workspaceEnvironment.getConfigMaps().get(BROKER_CONFIGMAP_NAME);
    assertNotNull(workspaceConfigMap);
    assertFalse(workspaceConfigMap.getData().isEmpty());
    assertTrue(
        workspaceConfigMap
            .getData()
            .entrySet()
            .stream()
            .allMatch(e -> brokerConfigMap.getData().get(e.getKey()).equals(e.getValue())));
  }

  @Test
  public void shouldAddBrokerAsInitContainerOnWorkspacePod() throws Exception {
    applier.apply(workspaceEnvironment, runtimeID, pluginFQNs, false);

    List<Container> initContainers = workspacePod.getSpec().getInitContainers();
    assertEquals(initContainers.size(), 1);
    assertEquals(initContainers.iterator().next(), brokerContainer);
  }

  @Test
  public void shouldAddBrokerVolumesToWorkspacePod() throws Exception {
    applier.apply(workspaceEnvironment, runtimeID, pluginFQNs, false);

    List<Volume> workspaceVolumes = workspacePod.getSpec().getVolumes();
    assertEquals(workspaceVolumes.size(), 1);
    assertEquals(workspaceVolumes.iterator().next(), brokerVolume);
  }
}

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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Volume;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesBrokerInitContainerApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesBrokerInitContainerApplier}.
 *
 * @author Angel Misevski
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesBrokerInitContainerApplierTest {

  private static final String WORKSPACE_POD_NAME = "workspacePod";
  private static final String WORKSPACE_MACHINE_NAME = "workspaceMachine";
  private static final String WORKSPACE_CONTAINER_NAME = "workspaceContainer";

  private static final String BROKER_POD_NAME = "brokerPod";
  private static final String BROKER_MACHINE_NAME = "brokerMachine";
  private static final String BROKER_CONTAINER_NAME = "brokerContainer";
  private static final String BROKER_CONFIGMAP_NAME = "brokerConfigMap";

  @Mock private BrokerEnvironmentFactory<KubernetesEnvironment> brokerEnvironmentFactory;
  @Mock private RuntimeIdentity runtimeID;
  @Mock private Collection<PluginMeta> pluginsMeta;

  // Broker Environment mocks
  @Mock private KubernetesEnvironment brokerEnvironment;
  @Mock private Pod brokerPod;
  @Mock private Container brokerContainer;
  @Mock private InternalMachineConfig brokerMachine;
  @Mock private Map<String, InternalMachineConfig> brokerMachines;
  @Mock private ConfigMap brokerConfigMap;
  @Mock private Volume brokerVolume;
  private PodSpec brokerPodSpec;

  // Workspace Environment mocks
  @Mock private KubernetesEnvironment workspaceEnvironment;
  @Mock private Pod workspacePod;
  @Mock private Map<String, InternalMachineConfig> workspaceMachines;
  private PodSpec workspacePodSpec;
  private Map<String, ConfigMap> workspaceConfigMaps;

  private KubernetesBrokerInitContainerApplier<KubernetesEnvironment> applier;

  @BeforeMethod
  public void setUp() throws Exception {
    // Workspace mocking
    doReturn(ImmutableMap.of(WORKSPACE_POD_NAME, workspacePod))
        .when(workspaceEnvironment)
        .getPods();
    doReturn(workspaceMachines).when(workspaceEnvironment).getMachines();
    workspacePodSpec = new PodSpec();
    doReturn(workspacePodSpec).when(workspacePod).getSpec();
    workspaceConfigMaps = new HashMap<>();
    doReturn(workspaceConfigMaps).when(workspaceEnvironment).getConfigMaps();

    // Broker mocking
    doReturn(brokerEnvironment).when(brokerEnvironmentFactory).create(any(), any(), any());
    doReturn(ImmutableMap.of(BROKER_POD_NAME, brokerPod)).when(brokerEnvironment).getPods();
    brokerPodSpec = new PodSpec();
    brokerPodSpec.setContainers(ImmutableList.of(brokerContainer));
    brokerPodSpec.setVolumes(ImmutableList.of(brokerVolume));
    doReturn(brokerPodSpec).when(brokerPod).getSpec();
    doReturn(brokerMachines).when(brokerEnvironment).getMachines();
    doReturn(brokerMachine).when(brokerMachines).get(any());
    doReturn(ImmutableMap.of(BROKER_CONFIGMAP_NAME, brokerConfigMap))
        .when(brokerEnvironment)
        .getConfigMaps();

    // Mocks necessary to make Names.machineName(pod, container) work
    ObjectMeta workspacePodMetadata = mock(ObjectMeta.class);
    doReturn(workspacePodMetadata).when(workspacePod).getMetadata();
    doReturn(
            ImmutableMap.of(
                String.format(MACHINE_NAME_ANNOTATION_FMT, WORKSPACE_CONTAINER_NAME),
                WORKSPACE_MACHINE_NAME))
        .when(workspacePodMetadata)
        .getAnnotations();
    ObjectMeta brokerPodMetadata = mock(ObjectMeta.class);
    doReturn(brokerPodMetadata).when(brokerPod).getMetadata();
    doReturn(
            ImmutableMap.of(
                String.format(MACHINE_NAME_ANNOTATION_FMT, BROKER_CONTAINER_NAME),
                BROKER_MACHINE_NAME))
        .when(brokerPodMetadata)
        .getAnnotations();

    applier = new KubernetesBrokerInitContainerApplier<>(brokerEnvironmentFactory);
  }

  @Test
  public void shouldAddBrokerMachineToWorkspaceEnvironment() throws Exception {
    applier.apply(workspaceEnvironment, runtimeID, pluginsMeta);

    verify(workspaceMachines).put(Names.machineName(workspacePod, brokerContainer), brokerMachine);
  }

  @Test
  public void shouldAddBrokerConfigMapsToWorkspaceEnvironment() throws Exception {
    applier.apply(workspaceEnvironment, runtimeID, pluginsMeta);

    assertTrue(workspaceConfigMaps.get(BROKER_CONFIGMAP_NAME).equals(brokerConfigMap));
  }

  @Test
  public void shouldAddBrokerAsInitContainerOnWorkspacePod() throws Exception {
    applier.apply(workspaceEnvironment, runtimeID, pluginsMeta);

    List<Container> initContainers = workspacePod.getSpec().getInitContainers();
    assertEquals(initContainers.size(), 1);
    assertEquals(initContainers.iterator().next(), brokerContainer);
  }

  @Test
  public void shouldAddBrokerVolumesToWorkspacePod() throws Exception {
    applier.apply(workspaceEnvironment, runtimeID, pluginsMeta);

    List<Volume> workspaceVolumes = workspacePodSpec.getVolumes();
    assertEquals(workspaceVolumes.size(), 1);
    assertEquals(workspaceVolumes.iterator().next(), brokerVolume);
  }
}

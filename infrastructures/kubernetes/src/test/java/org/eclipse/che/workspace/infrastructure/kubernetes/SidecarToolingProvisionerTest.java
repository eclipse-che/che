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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.workspace.shared.Constants.MERGE_PLUGINS_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.wsplugins.ChePluginsApplier;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.EphemeralWorkspaceUtility;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesArtifactsBrokerApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.PluginBrokerManager;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link SidecarToolingProvisioner}.
 *
 * @author Angel Misevski
 */
@Listeners(MockitoTestNGListener.class)
public class SidecarToolingProvisionerTest {

  private static final String RECIPE_TYPE = "TestingRecipe";
  private static final String PLUGIN_FQN_ID = "TestPluginId";

  @Mock private StartSynchronizer startSynchronizer;
  @Mock private KubernetesArtifactsBrokerApplier<KubernetesEnvironment> artifactsBrokerApplier;
  @Mock private PluginFQNParser pluginFQNParser;
  @Mock private PluginBrokerManager<KubernetesEnvironment> brokerManager;
  @Mock private KubernetesEnvironment nonEphemeralEnvironment;
  @Mock private KubernetesEnvironment ephemeralEnvironment;
  @Mock private KubernetesEnvironment mergePluginsEnvironment;
  @Mock private KubernetesEnvironment noMergePluginsEnvironment;
  @Mock private RuntimeIdentity runtimeId;
  @Mock private ChePluginsApplier chePluginsApplier;

  private static Map<String, String> environmentAttributesBase =
      ImmutableMap.of(
          "editor", "eclipse/theia/1.0.0",
          "plugins", "eclipse/che-machine-exec-plugin/0.0.1");

  private Collection<PluginFQN> pluginFQNs = ImmutableList.of(new PluginFQN(null, PLUGIN_FQN_ID));

  private SidecarToolingProvisioner<KubernetesEnvironment> provisioner;
  private SidecarToolingProvisioner<KubernetesEnvironment> mergePluginsProvisioner;

  @BeforeMethod
  public void setUp() throws Exception {
    Map<String, String> ephemeralEnvAttributes = new HashMap<>(environmentAttributesBase);
    EphemeralWorkspaceUtility.makeEphemeral(ephemeralEnvAttributes);
    Map<String, String> nonEphemeralEnvAttributes = new HashMap<>(environmentAttributesBase);

    Map<String, String> mergePluginsEnvAttributes = new HashMap<>(environmentAttributesBase);
    mergePluginsEnvAttributes.put(MERGE_PLUGINS_ATTRIBUTE, "true");
    Map<String, String> noMergePluginsEnvAttributes = new HashMap<>(environmentAttributesBase);
    noMergePluginsEnvAttributes.put(MERGE_PLUGINS_ATTRIBUTE, "false");

    lenient().doReturn(RECIPE_TYPE).when(nonEphemeralEnvironment).getType();
    lenient().doReturn(RECIPE_TYPE).when(ephemeralEnvironment).getType();
    lenient().doReturn(RECIPE_TYPE).when(mergePluginsEnvironment).getType();
    lenient().doReturn(RECIPE_TYPE).when(noMergePluginsEnvironment).getType();
    lenient().doReturn(nonEphemeralEnvAttributes).when(nonEphemeralEnvironment).getAttributes();
    lenient().doReturn(ephemeralEnvAttributes).when(ephemeralEnvironment).getAttributes();
    lenient().doReturn(mergePluginsEnvAttributes).when(mergePluginsEnvironment).getAttributes();
    lenient().doReturn(noMergePluginsEnvAttributes).when(noMergePluginsEnvironment).getAttributes();
    doReturn(pluginFQNs).when(pluginFQNParser).parsePlugins(any());
  }

  @Test
  public void shouldIncludexArtifactsBrokerWhenWorkspaceIsNotEphemeral() throws Exception {
    provisioner = getSidecarToolingProvisioner("false");
    provisioner.provision(runtimeId, startSynchronizer, nonEphemeralEnvironment, emptyMap());

    verify(chePluginsApplier, times(1)).apply(any(), any(), any());
    verify(artifactsBrokerApplier, times(1)).apply(any(), any(), any(), anyBoolean());
  }

  @Test
  public void shouldIncludeArtifactsBrokerWhenWorkspaceIsEphemeral() throws Exception {
    provisioner = getSidecarToolingProvisioner("false");
    provisioner.provision(runtimeId, startSynchronizer, ephemeralEnvironment, emptyMap());

    verify(chePluginsApplier, times(1)).apply(any(), any(), any());
    verify(artifactsBrokerApplier, times(1)).apply(any(), any(), any(), anyBoolean());
  }

  @Test
  public void shouldMergePluginsWhenDefaultPropertyIsTrue() throws Exception {
    provisioner = getSidecarToolingProvisioner("true");
    provisioner.provision(runtimeId, startSynchronizer, nonEphemeralEnvironment, emptyMap());

    verify(brokerManager, times(1)).getTooling(any(), any(), any(), anyBoolean(), eq(true), any());
    verify(artifactsBrokerApplier, times(1)).apply(any(), any(), any(), eq(true));
  }

  @Test
  public void shouldNotMergePluginsWhenDefaultPropertyIsFalse() throws Exception {
    provisioner = getSidecarToolingProvisioner("false");
    provisioner.provision(runtimeId, startSynchronizer, nonEphemeralEnvironment, emptyMap());

    verify(brokerManager, times(1)).getTooling(any(), any(), any(), anyBoolean(), eq(false), any());
    verify(artifactsBrokerApplier, times(1)).apply(any(), any(), any(), eq(false));
  }

  @Test
  public void shouldMergePluginsWhenEnvironmentOverridesToTrue() throws Exception {
    provisioner = getSidecarToolingProvisioner("false");
    provisioner.provision(runtimeId, startSynchronizer, mergePluginsEnvironment, emptyMap());

    verify(brokerManager, times(1)).getTooling(any(), any(), any(), anyBoolean(), eq(true), any());
    verify(artifactsBrokerApplier, times(1)).apply(any(), any(), any(), eq(true));
  }

  @Test
  public void shouldNotMergePluginsWhenEnvironmentOverridesToFalse() throws Exception {
    provisioner = getSidecarToolingProvisioner("true");
    provisioner.provision(runtimeId, startSynchronizer, noMergePluginsEnvironment, emptyMap());

    verify(brokerManager, times(1)).getTooling(any(), any(), any(), anyBoolean(), eq(false), any());
    verify(artifactsBrokerApplier, times(1)).apply(any(), any(), any(), eq(false));
  }

  private SidecarToolingProvisioner<KubernetesEnvironment> getSidecarToolingProvisioner(
      String mergePlugins) {
    Map<String, ChePluginsApplier> workspaceNextAppliers =
        ImmutableMap.of(RECIPE_TYPE, chePluginsApplier);
    return new SidecarToolingProvisioner<>(
        workspaceNextAppliers,
        artifactsBrokerApplier,
        pluginFQNParser,
        brokerManager,
        mergePlugins);
  }
}

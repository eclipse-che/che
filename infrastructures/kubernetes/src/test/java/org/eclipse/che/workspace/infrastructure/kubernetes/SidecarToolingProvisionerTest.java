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

import static org.mockito.ArgumentMatchers.any;
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
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesBrokerInitContainerApplier;
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
  @Mock private KubernetesBrokerInitContainerApplier<KubernetesEnvironment> brokerApplier;
  @Mock private PluginFQNParser pluginFQNParser;
  @Mock private PluginBrokerManager<KubernetesEnvironment> brokerManager;
  @Mock private KubernetesEnvironment nonEphemeralEnvironment;
  @Mock private KubernetesEnvironment ephemeralEnvironment;
  @Mock private RuntimeIdentity runtimeId;
  @Mock private ChePluginsApplier chePluginsApplier;

  private static Map<String, String> environmentAttributesBase =
      ImmutableMap.of(
          "editor", "eclipse/theia/1.0.0",
          "plugins", "eclipse/che-machine-exec-plugin/0.0.1");

  private Collection<PluginFQN> pluginFQNs = ImmutableList.of(new PluginFQN(null, PLUGIN_FQN_ID));

  private SidecarToolingProvisioner<KubernetesEnvironment> provisioner;

  @BeforeMethod
  public void setUp() throws Exception {
    Map<String, ChePluginsApplier> workspaceNextAppliers =
        ImmutableMap.of(RECIPE_TYPE, chePluginsApplier);
    Map<String, String> ephemeralEnvironmentAttributes = new HashMap<>(environmentAttributesBase);
    EphemeralWorkspaceUtility.makeEphemeral(ephemeralEnvironmentAttributes);
    Map<String, String> nonEphemeralEnvironmentAttributes =
        new HashMap<>(environmentAttributesBase);

    lenient().doReturn(RECIPE_TYPE).when(nonEphemeralEnvironment).getType();
    lenient().doReturn(RECIPE_TYPE).when(ephemeralEnvironment).getType();
    lenient()
        .doReturn(nonEphemeralEnvironmentAttributes)
        .when(nonEphemeralEnvironment)
        .getAttributes();
    lenient().doReturn(ephemeralEnvironmentAttributes).when(ephemeralEnvironment).getAttributes();
    doReturn(pluginFQNs).when(pluginFQNParser).parsePlugins(any());

    provisioner =
        new SidecarToolingProvisioner<>(
            workspaceNextAppliers, brokerApplier, pluginFQNParser, brokerManager);
  }

  @Test
  public void shouldNotAddInitContainerWhenWorkspaceIsNotEphemeral() throws Exception {
    provisioner.provision(runtimeId, startSynchronizer, nonEphemeralEnvironment);

    verify(chePluginsApplier, times(1)).apply(any(), any(), any());
    verify(brokerApplier, times(0)).apply(any(), any(), any());
  }

  @Test
  public void shouldIncludeInitContainerWhenWorkspaceIsEphemeral() throws Exception {
    provisioner.provision(runtimeId, startSynchronizer, ephemeralEnvironment);

    verify(chePluginsApplier, times(1)).apply(any(), any(), any());
    verify(brokerApplier, times(1)).apply(any(), any(), any());
  }
}

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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.ChePluginsApplier;
import org.eclipse.che.api.workspace.server.wsplugins.PluginMetaRetriever;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.EphemeralWorkspaceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provisions sidecars-powered development tooling in a workspace.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class SidecarToolingProvisioner<E extends KubernetesEnvironment> {

  private static final Logger LOG = LoggerFactory.getLogger(SidecarToolingProvisioner.class);

  private final Map<String, ChePluginsApplier> workspaceNextAppliers;
  private final KubernetesBrokerInitContainerApplier<E> brokerApplier;
  private final PluginMetaRetriever pluginMetaRetriever;
  private final PluginBrokerManager<E> pluginBrokerManager;

  @Inject
  public SidecarToolingProvisioner(
      Map<String, ChePluginsApplier> workspaceNextAppliers,
      KubernetesBrokerInitContainerApplier<E> brokerApplier,
      PluginMetaRetriever pluginMetaRetriever,
      PluginBrokerManager<E> pluginBrokerManager) {
    this.workspaceNextAppliers = ImmutableMap.copyOf(workspaceNextAppliers);
    this.brokerApplier = brokerApplier;
    this.pluginMetaRetriever = pluginMetaRetriever;
    this.pluginBrokerManager = pluginBrokerManager;
  }

  @Beta
  public void provision(RuntimeIdentity id, E environment) throws InfrastructureException {

    Collection<PluginMeta> pluginsMeta = pluginMetaRetriever.get(environment.getAttributes());
    if (pluginsMeta.isEmpty()) {
      return;
    }
    LOG.debug("Started sidecar tooling provisioning workspace '{}'", id.getWorkspaceId());
    String recipeType = environment.getType();
    ChePluginsApplier pluginsApplier = workspaceNextAppliers.get(recipeType);
    if (pluginsApplier == null) {
      throw new InfrastructureException(
          "Sidecar tooling configuration is not supported with environment type " + recipeType);
    }

    boolean isEphemeral = EphemeralWorkspaceAdapter.isEphemeral(environment.getAttributes());
    List<ChePlugin> chePlugins = pluginBrokerManager.getTooling(id, pluginsMeta, isEphemeral);

    pluginsApplier.apply(environment, chePlugins);
    if (isEphemeral) {
      brokerApplier.apply(environment, id, pluginsMeta);
    }
    LOG.debug("Finished sidecar tooling provisioning workspace '{}'", id.getWorkspaceId());
  }
}

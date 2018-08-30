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

/**
 * Provisions sidecars-powered development tooling in a workspace.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class SidecarToolingProvisioner {

  private final Map<String, ChePluginsApplier> workspaceNextAppliers;
  private final PluginMetaRetriever pluginMetaRetriever;
  private final PluginBrokerManager pluginBrokerManager;

  @Inject
  public SidecarToolingProvisioner(
      Map<String, ChePluginsApplier> workspaceNextAppliers,
      PluginMetaRetriever pluginMetaRetriever,
      PluginBrokerManager pluginBrokerManager) {
    this.workspaceNextAppliers = ImmutableMap.copyOf(workspaceNextAppliers);
    this.pluginMetaRetriever = pluginMetaRetriever;
    this.pluginBrokerManager = pluginBrokerManager;
  }

  @Beta
  public void provision(RuntimeIdentity id, KubernetesEnvironment environment)
      throws InfrastructureException {

    Collection<PluginMeta> pluginsMeta = pluginMetaRetriever.get(environment.getAttributes());
    if (pluginsMeta.isEmpty()) {
      return;
    }

    String recipeType = environment.getRecipe().getType();
    ChePluginsApplier pluginsApplier = workspaceNextAppliers.get(recipeType);
    if (pluginsApplier == null) {
      throw new InfrastructureException(
          "Sidecar tooling configuration is not supported with recipe type " + recipeType);
    }

    List<ChePlugin> chePlugins = pluginBrokerManager.getTooling(id, pluginsMeta, environment);

    pluginsApplier.apply(environment, chePlugins);
  }
}

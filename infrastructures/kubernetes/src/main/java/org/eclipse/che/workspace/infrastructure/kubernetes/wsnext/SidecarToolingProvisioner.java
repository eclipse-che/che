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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsnext;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.wsnext.WorkspaceNextApplier;
import org.eclipse.che.api.workspace.server.wsnext.WorkspaceNextObjectsRetriever;
import org.eclipse.che.api.workspace.server.wsnext.model.PluginMeta;
import org.slf4j.Logger;

/**
 * Provisions sidecars-powered development tooling in a workspace.
 *
 * @author Oleksandr Garagatyi
 */
public class SidecarToolingProvisioner {

  private static final Logger LOG = getLogger(SidecarToolingProvisioner.class);

  private final Map<String, WorkspaceNextApplier> workspaceNextAppliers;
  private final WorkspaceNextObjectsRetriever workspaceNextObjectsRetriever;

  @Inject
  public SidecarToolingProvisioner(
      Map<String, WorkspaceNextApplier> workspaceNextAppliers,
      WorkspaceNextObjectsRetriever workspaceNextObjectsRetriever) {
    this.workspaceNextAppliers = ImmutableMap.copyOf(workspaceNextAppliers);
    this.workspaceNextObjectsRetriever = workspaceNextObjectsRetriever;
  }

  public void provision(InternalEnvironment environment) throws InfrastructureException {
    String recipeType = environment.getRecipe().getType();
    Collection<PluginMeta> pluginsMeta =
        workspaceNextObjectsRetriever.get(environment.getAttributes());
    if (pluginsMeta.isEmpty()) {
      return;
    }

    // TODO
    // Start container with a broker
    // Pass Metas to it
    // Ping it to retrieve actual state of Plugin broker execution process
    // Once finished successfully fetch workspace tooling config
    // Consider pushing logs from Broker to workspace logs

    // TODO remove this. Added for the Walking skeleton development purposes
    LOG.error("Sidecar tooling workspace attributes: {}", environment.getAttributes());
    LOG.error("Sidecar tooling metadata: {}", pluginsMeta);

    WorkspaceNextApplier wsNext = workspaceNextAppliers.get(recipeType);
    if (wsNext == null) {
      throw new InfrastructureException(
          "Sidecar tooling configuration is not supported with recipe type " + recipeType);
    }
    // TODO Apply tooling config to InternalEnvironment
    // wsNext.apply(environment, plugins);

    throw new InfrastructureException(
        "Sidecar powered Che tooling is not implemented yet. "
            + "Workspace start is not possible. Remove 'editor' and 'plugins' attributes from "
            + "workspace to switch to a regular tooling.");
  }
}

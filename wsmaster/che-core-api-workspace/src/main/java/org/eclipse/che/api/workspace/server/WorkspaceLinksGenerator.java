/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_ENVIRONMENT_STATUS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_IDE_URL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_SELF;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;

/**
 * Generates links for workspaces.
 *
 * @author Anton Korneta
 * @author Yevhenii Voievodin
 */
@Singleton
public class WorkspaceLinksGenerator {

  private final WorkspaceRuntimes workspaceRuntimes;
  private final String cheApiEndpoint;
  private final String envStatusEndpoint;

  @Inject
  public WorkspaceLinksGenerator(
      WorkspaceRuntimes workspaceRuntimes,
      @Named("che.api") String cheApiEndpoint,
      @Named("che.websocket.endpoint.base") String cheWsEndpointBase) {
    this.workspaceRuntimes = workspaceRuntimes;
    this.cheApiEndpoint = cheApiEndpoint;
    this.envStatusEndpoint = cheWsEndpointBase + "/wsmaster/websocket";
  }

  /** Returns 'rel -> url' map of links for the given workspace. */
  public Map<String, String> genLinks(Workspace workspace) throws ServerException {
    final LinkedHashMap<String, String> links = new LinkedHashMap<>();

    links.put(
        LINK_REL_SELF,
        UriBuilder.fromUri(cheApiEndpoint)
            .path(WorkspaceService.class)
            .path(WorkspaceService.class, "getByKey")
            .build(workspace.getId())
            .toString());
    links.put(
        LINK_REL_IDE_URL,
        UriBuilder.fromUri(cheApiEndpoint)
            .replacePath("")
            .path(workspace.getNamespace())
            .path(workspace.getConfig().getName())
            .build()
            .toString());
    if (workspace.getStatus() != WorkspaceStatus.STOPPED) {
      addRuntimeLinks(links, workspace.getId());
    }

    return links;
  }

  private void addRuntimeLinks(Map<String, String> links, String workspaceId)
      throws ServerException {
    Optional<RuntimeContext> ctxOpt = workspaceRuntimes.getRuntimeContext(workspaceId);
    if (ctxOpt.isPresent()) {
      try {
        links.put(LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL, ctxOpt.get().getOutputChannel().toString());
        links.put(LINK_REL_ENVIRONMENT_STATUS_CHANNEL, envStatusEndpoint);
      } catch (InfrastructureException x) {
        throw new ServerException(x.getMessage(), x);
      }
    }
  }
}

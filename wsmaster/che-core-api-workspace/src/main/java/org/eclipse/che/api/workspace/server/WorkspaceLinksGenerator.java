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
package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_ENVIRONMENT_STATUS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_IDE_URL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_SELF;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
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
  private final PreviewUrlLinksVariableGenerator previewUrlLinksVariableGenerator;
  private final String cheWebsocketEndpoint;

  @Inject
  public WorkspaceLinksGenerator(
      WorkspaceRuntimes workspaceRuntimes,
      PreviewUrlLinksVariableGenerator previewUrlLinksVariableGenerator,
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint) {
    this.workspaceRuntimes = workspaceRuntimes;
    this.previewUrlLinksVariableGenerator = previewUrlLinksVariableGenerator;
    this.cheWebsocketEndpoint = cheWebsocketEndpoint;
  }

  /** Returns 'rel -> url' map of links for the given workspace. */
  public Map<String, String> genLinks(WorkspaceImpl workspace, ServiceContext serviceContext)
      throws ServerException {
    final UriBuilder uriBuilder = serviceContext.getServiceUriBuilder();
    final LinkedHashMap<String, String> links = new LinkedHashMap<>();

    links.put(
        LINK_REL_SELF,
        uriBuilder
            .clone()
            .path(WorkspaceService.class, "getByKey")
            .build(workspace.getId())
            .toString());
    links.put(
        LINK_REL_IDE_URL,
        uriBuilder
            .clone()
            .replacePath("")
            .path(workspace.getNamespace())
            .path(workspace.getName())
            .build()
            .toString());
    if (workspace.getStatus() != WorkspaceStatus.STOPPED) {
      addRuntimeLinks(links, workspace.getId(), serviceContext);
    }

    links.putAll(
        previewUrlLinksVariableGenerator.genLinksMapAndUpdateCommands(
            workspace, uriBuilder.clone()));

    return links;
  }

  private void addRuntimeLinks(
      Map<String, String> links, String workspaceId, ServiceContext serviceContext)
      throws ServerException {
    URI uri = serviceContext.getServiceUriBuilder().build();
    links.put(
        LINK_REL_ENVIRONMENT_STATUS_CHANNEL,
        UriBuilder.fromUri(cheWebsocketEndpoint)
            .scheme(uri.getScheme().equals("https") ? "wss" : "ws")
            .host(uri.getHost())
            .port(uri.getPort())
            .build()
            .toString());

    Optional<RuntimeContext> ctxOpt = workspaceRuntimes.getRuntimeContext(workspaceId);
    if (ctxOpt.isPresent()) {
      try {
        links.put(LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL, ctxOpt.get().getOutputChannel().toString());
      } catch (UnsupportedOperationException e) {
        // Do not include output channel to links since it is not supported by context
      } catch (InfrastructureException x) {
        throw new ServerException(x.getMessage(), x);
      }
    }
  }
}

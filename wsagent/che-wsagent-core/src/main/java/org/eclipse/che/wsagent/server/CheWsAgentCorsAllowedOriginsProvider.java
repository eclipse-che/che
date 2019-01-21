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
package org.eclipse.che.wsagent.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider of "cors.allowed.origins" setting for CORS Filter of WS Agent. Provides the value such
 * algorithm:
 *
 * <ul>
 *   <li>If set che.wsagent.cors.allowed_origins
 *   <li>Server with "ide" attribute in workspace config
 *   <li>Server from url of "ide" link in workspace config
 *   <li>che.api
 * </ul>
 */
public class CheWsAgentCorsAllowedOriginsProvider implements Provider<String> {

  private static final Logger LOG =
      LoggerFactory.getLogger(CheWsAgentCorsAllowedOriginsProvider.class);

  private final String allowedOrigins;

  @Inject
  public CheWsAgentCorsAllowedOriginsProvider(
      @Named("che.api") String apiEndpoint,
      @Nullable @Named("che.wsagent.cors.allowed_origins") String allowedOrigins,
      HttpJsonRequestFactory httpJsonRequestFactory)
      throws ApiException, IOException {
    this.allowedOrigins = evaluateOrigins(apiEndpoint, allowedOrigins, httpJsonRequestFactory);
  }

  @Override
  public String get() {
    LOG.debug("allowedOrigins {} ", allowedOrigins);
    return allowedOrigins;
  }

  private String evaluateOrigins(
      String apiEndpoint, String allowedOrigins, HttpJsonRequestFactory httpJsonRequestFactory)
      throws ApiException, IOException {
    if (allowedOrigins != null) {
      return allowedOrigins;
    }

    final UriBuilder builder =
        UriBuilder.fromUri(apiEndpoint)
            .path(WorkspaceService.class)
            .path(WorkspaceService.class, "getByKey")
            .queryParam("includeInternalServers", "true");

    String href = builder.build(System.getenv("CHE_WORKSPACE_ID")).toString();
    WorkspaceDto workspaceDto =
        httpJsonRequestFactory.fromUrl(href).useGetMethod().request().asDto(WorkspaceDto.class);

    String ideUrl = getIdeUrl(workspaceDto);
    if (ideUrl != null) {
      return UriBuilder.fromUri(ideUrl).replacePath(null).build().toString();
    }
    return UriBuilder.fromUri(workspaceDto.getLinks().getOrDefault("ide", apiEndpoint))
        .replacePath(null)
        .build()
        .toString();
  }

  private String getIdeUrl(WorkspaceDto workspaceDto) {
    for (Machine machine : workspaceDto.getRuntime().getMachines().values()) {
      for (Server server : machine.getServers().values()) {
        if ("ide".equals(server.getAttributes().get("type"))) {
          LOG.debug("Found ide server {}", server);
          return server.getUrl();
        }
      }
    }
    return null;
  }
}

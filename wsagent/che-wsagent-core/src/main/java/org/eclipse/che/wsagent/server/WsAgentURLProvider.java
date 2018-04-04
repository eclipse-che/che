/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.wsagent.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.MachineDto;
import org.eclipse.che.api.workspace.shared.dto.ServerDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides URL to workspace agent inside container.
 *
 * @author Anton Korneta
 */
public class WsAgentURLProvider implements Provider<String> {
  private static final Logger LOG = LoggerFactory.getLogger(WsAgentURLProvider.class);

  private final String wsId;
  private final String workspaceApiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  private String cachedAgentUrl;

  @Inject
  public WsAgentURLProvider(
      @Named("che.api") String apiEndpoint,
      @Named("env.CHE_WORKSPACE_ID") String wsId,
      HttpJsonRequestFactory requestFactory) {
    this.wsId = wsId;
    this.workspaceApiEndpoint = apiEndpoint + "/workspace/";
    this.requestFactory = requestFactory;
  }

  @Override
  public String get() {
    if (isNullOrEmpty(cachedAgentUrl)) {
      try {
        final WorkspaceDto workspace =
            requestFactory
                .fromUrl(workspaceApiEndpoint + wsId)
                .useGetMethod()
                .request()
                .asDto(WorkspaceDto.class);
        if (workspace.getRuntime() != null) {
          for (MachineDto machine : workspace.getRuntime().getMachines().values()) {
            ServerDto wsAgent = machine.getServers().get(SERVER_WS_AGENT_HTTP_REFERENCE);
            if (wsAgent != null) {
              cachedAgentUrl = wsAgent.getUrl();
            }
          }
        }
      } catch (ApiException | IOException ex) {
        LOG.warn(ex.getLocalizedMessage());
        throw new RuntimeException("Failed to configure wsagent endpoint");
      }
    }
    return cachedAgentUrl;
  }
}

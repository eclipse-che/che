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
package org.eclipse.che.multiuser.machine.authentication.server;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_WEBSOCKET_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_STOP_WORKSPACE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.WorkspaceServiceLinksInjector;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.eclipse.che.multiuser.machine.authentication.shared.dto.MachineTokenDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to inject {@link WorkspaceService} related links.
 *
 * @author Anton Korneta
 */
public class WorkspaceServiceAuthLinksInjector extends WorkspaceServiceLinksInjector {
  private static final Logger LOG =
      LoggerFactory.getLogger(WorkspaceServiceAuthLinksInjector.class);
  private static final String MACHINE_TOKEN = "token";
  private static final String MACHINE_SERVICE_PATH = "/machine/token/";

  private final String tokenServiceBaseUrl;
  private final HttpJsonRequestFactory httpJsonRequestFactory;

  @Inject
  public WorkspaceServiceAuthLinksInjector(
      @Named("che.api") String apiEndpoint,
      HttpJsonRequestFactory httpJsonRequestFactory,
      MachineAuthLinksInjector machineLinksInjector) {
    super(machineLinksInjector);
    this.tokenServiceBaseUrl = apiEndpoint + MACHINE_SERVICE_PATH;
    this.httpJsonRequestFactory = httpJsonRequestFactory;
  }

  @Override
  protected void injectRuntimeLinks(
      WorkspaceDto workspace, URI ideUri, UriBuilder uriBuilder, ServiceContext serviceContext) {
    final WorkspaceRuntimeDto runtime = workspace.getRuntime();
    // add links for running workspace
    if (workspace.getStatus() == RUNNING && runtime != null) {
      runtime
          .getLinks()
          .add(
              createLink(
                  "DELETE",
                  uriBuilder
                      .clone()
                      .path(WorkspaceService.class, "stop")
                      .build(workspace.getId())
                      .toString(),
                  LINK_REL_STOP_WORKSPACE));
      String token = null;
      try {
        token =
            httpJsonRequestFactory
                .fromUrl(tokenServiceBaseUrl + workspace.getId())
                .setMethod(HttpMethod.GET)
                .request()
                .asDto(MachineTokenDto.class)
                .getMachineToken();
      } catch (ApiException | IOException ex) {
        LOG.warn("Failed to get machine token", ex);
      }
      final String machineToken = firstNonNull(token, "");

      runtime.getMachines().forEach(machine -> injectMachineLinks(machine, serviceContext));

      final MachineDto devMachine = runtime.getDevMachine();
      if (devMachine != null) {
        injectMachineLinks(devMachine, serviceContext);

        final Collection<ServerDto> servers = devMachine.getRuntime().getServers().values();
        servers
            .stream()
            .filter(server -> WSAGENT_REFERENCE.equals(server.getRef()))
            .findAny()
            .ifPresent(
                wsAgent -> {
                  runtime.getLinks().add(createLink("GET", wsAgent.getUrl(), WSAGENT_REFERENCE));
                  Link wsAgentWebsocketLink =
                      createLink(
                          "GET",
                          UriBuilder.fromUri(wsAgent.getUrl())
                              .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                              .path("/ws")
                              .queryParam(MACHINE_TOKEN, machineToken)
                              .build()
                              .toString(),
                          WSAGENT_WEBSOCKET_REFERENCE,
                          ImmutableList.of(
                              newDto(LinkParameter.class)
                                  .withName(MACHINE_TOKEN)
                                  .withDefaultValue(machineToken)
                                  .withRequired(true)));

                  runtime.getLinks().add(wsAgentWebsocketLink);
                  devMachine.getLinks().add(wsAgentWebsocketLink);
                });
      }
    }
  }
}

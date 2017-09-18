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

import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.machine.shared.Constants.EXEC_AGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.environment.server.MachineLinksInjector;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.multiuser.machine.authentication.shared.dto.MachineTokenDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to inject Machine related links.
 *
 * @author Anton Korneta
 */
public class MachineAuthLinksInjector extends MachineLinksInjector {
  private static final Logger LOG = LoggerFactory.getLogger(MachineAuthLinksInjector.class);
  private static final String MACHINE_TOKEN_SERVICE_PATH = "/machine/token/";

  private final String tokenServiceBaseUrl;
  private final HttpJsonRequestFactory httpJsonRequestFactory;

  @Inject
  public MachineAuthLinksInjector(
      @Named("che.api") String apiEndpoint, HttpJsonRequestFactory httpJsonRequestFactory) {
    this.tokenServiceBaseUrl = apiEndpoint + MACHINE_TOKEN_SERVICE_PATH;
    this.httpJsonRequestFactory = httpJsonRequestFactory;
  }

  @VisibleForTesting
  @Override
  protected void injectTerminalLink(
      MachineDto machine, ServiceContext serviceContext, List<Link> links) {
    if (machine.getRuntime() != null) {
      final String machineToken = getMachineToken(machine);
      final String scheme = serviceContext.getBaseUriBuilder().build().getScheme();
      final Collection<ServerDto> servers = machine.getRuntime().getServers().values();
      servers
          .stream()
          .filter(server -> TERMINAL_REFERENCE.equals(server.getRef()))
          .findAny()
          .ifPresent(
              terminal ->
                  links.add(
                      createLink(
                          "GET",
                          UriBuilder.fromUri(terminal.getUrl())
                              .scheme("https".equals(scheme) ? "wss" : "ws")
                              .queryParam("token", machineToken)
                              .path("/pty")
                              .build()
                              .toString(),
                          TERMINAL_REFERENCE)));
    }
  }

  @Override
  protected void injectExecAgentLink(
      MachineDto machine, ServiceContext serviceContext, List<Link> links) {
    final String scheme = serviceContext.getBaseUriBuilder().build().getScheme();
    if (machine.getRuntime() != null) {
      final String machineToken = getMachineToken(machine);
      final Collection<ServerDto> servers = machine.getRuntime().getServers().values();
      servers
          .stream()
          .filter(server -> EXEC_AGENT_REFERENCE.equals(server.getRef()))
          .findAny()
          .ifPresent(
              exec ->
                  links.add(
                      createLink(
                          "GET",
                          UriBuilder.fromUri(exec.getUrl())
                              .scheme("https".equals(scheme) ? "wss" : "ws")
                              .queryParam("token", machineToken)
                              .path("/connect")
                              .build()
                              .toString(),
                          EXEC_AGENT_REFERENCE)));
    }
  }

  private String getMachineToken(MachineDto machine) {
    try {
      return httpJsonRequestFactory
          .fromUrl(tokenServiceBaseUrl + machine.getWorkspaceId())
          .setMethod(HttpMethod.GET)
          .request()
          .asDto(MachineTokenDto.class)
          .getMachineToken();
    } catch (ApiException | IOException ex) {
      LOG.warn("Failed to get machine token", ex);
    }
    return "";
  }
}

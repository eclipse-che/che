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
package org.eclipse.che.api.environment.server;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_STATUS_CHANNEL_TEMPLATE;
import static org.eclipse.che.api.machine.shared.Constants.EXEC_AGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL;
import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;
import static org.eclipse.che.dto.server.DtoFactory.cloneDto;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;

/**
 * Helps to inject Machine related links.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineLinksInjector {

  public MachineDto injectLinks(MachineDto machine, ServiceContext serviceContext) {
    final List<Link> links = new ArrayList<>();

    injectTerminalLink(machine, serviceContext, links);
    injectExecAgentLink(machine, serviceContext, links);

    // add workspace channel links
    final Link workspaceChannelLink =
        createLink(
            "GET",
            serviceContext
                .getBaseUriBuilder()
                .path("ws")
                .scheme(
                    "https".equals(serviceContext.getBaseUriBuilder().build().getScheme())
                        ? "wss"
                        : "ws")
                .build()
                .toString(),
            null);
    final LinkParameter channelParameter =
        newDto(LinkParameter.class).withName("channel").withRequired(true);

    links.add(
        cloneDto(workspaceChannelLink)
            .withRel(LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL)
            .withParameters(
                singletonList(
                    cloneDto(channelParameter)
                        .withDefaultValue(
                            format(
                                ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE, machine.getWorkspaceId())))));

    links.add(
        cloneDto(workspaceChannelLink)
            .withRel(ENVIRONMENT_STATUS_CHANNEL_TEMPLATE)
            .withParameters(
                singletonList(
                    cloneDto(channelParameter)
                        .withDefaultValue(
                            format(
                                ENVIRONMENT_STATUS_CHANNEL_TEMPLATE, machine.getWorkspaceId())))));

    return machine.withLinks(links);
  }

  protected void injectTerminalLink(
      MachineDto machine, ServiceContext serviceContext, List<Link> links) {
    final String scheme = serviceContext.getBaseUriBuilder().build().getScheme();
    if (machine.getRuntime() != null) {
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
                              .path("/pty")
                              .build()
                              .toString(),
                          TERMINAL_REFERENCE)));
    }
  }

  protected void injectExecAgentLink(
      MachineDto machine, ServiceContext serviceContext, List<Link> links) {
    final String scheme = serviceContext.getBaseUriBuilder().build().getScheme();
    if (machine.getRuntime() != null) {
      final Collection<ServerDto> servers = machine.getRuntime().getServers().values();
      servers
          .stream()
          .filter(server -> EXEC_AGENT_REFERENCE.equals(server.getRef()))
          .findAny()
          .ifPresent(
              execAgent ->
                  links.add(
                      createLink(
                          "GET",
                          UriBuilder.fromUri(execAgent.getUrl())
                              .scheme("https".equals(scheme) ? "wss" : "ws")
                              .path("/connect")
                              .build()
                              .toString(),
                          EXEC_AGENT_REFERENCE)));
    }
  }
}

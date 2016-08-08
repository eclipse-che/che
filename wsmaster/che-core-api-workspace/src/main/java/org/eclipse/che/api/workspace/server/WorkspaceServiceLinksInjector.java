/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.machine.server.MachineServiceLinksInjector;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_STATUS_CHANNEL_TEMPLATE;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_ENVIRONMENT_STATUS_CHANNEL;
import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_WEBSOCKET_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.GET_ALL_USER_WORKSPACES;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_SNAPSHOT;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_IDE_URL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_REMOVE_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_SELF;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_START_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_STOP_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LIN_REL_GET_WORKSPACE;
import static org.eclipse.che.dto.server.DtoFactory.cloneDto;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helps to inject {@link WorkspaceService} related links.
 *
 * @author Anton Korneta
 */
@Singleton
public class WorkspaceServiceLinksInjector {

    //TODO: we need keep IDE context in some property to have possibility configure it because context is different in Che and Hosted packaging
    //TODO: not good solution do it here but critical for this task  https://jira.codenvycorp.com/browse/IDEX-3619
    private final MachineServiceLinksInjector machineLinksInjector;

    @Inject
    public WorkspaceServiceLinksInjector(MachineServiceLinksInjector machineLinksInjector) {
        this.machineLinksInjector = machineLinksInjector;
    }

    public WorkspaceDto injectLinks(WorkspaceDto workspace, ServiceContext serviceContext) {
        final UriBuilder uriBuilder = serviceContext.getServiceUriBuilder();
        final List<Link> links = new ArrayList<>();
        // add common workspace links
        links.add(createLink("GET",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "getByKey")
                                       .build(workspace.getId())
                                       .toString(),
                             LINK_REL_SELF));
        links.add(createLink("POST",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "startById")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_START_WORKSPACE));
        links.add(createLink("DELETE",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "delete")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_REMOVE_WORKSPACE));
        links.add(createLink("GET",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "getWorkspaces")
                                       .build()
                                       .toString(),
                             APPLICATION_JSON,
                             GET_ALL_USER_WORKSPACES));
        links.add(createLink("GET",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "getSnapshot")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_GET_SNAPSHOT));

        //TODO here we add url to IDE with workspace name not good solution do it here but critical for this task  https://jira.codenvycorp.com/browse/IDEX-3619
        final URI ideUri = uriBuilder.clone()
                                     .replacePath("")
                                     .path(workspace.getNamespace())
                                     .path(workspace.getConfig().getName())
                                     .build();
        links.add(createLink("GET", ideUri.toString(), TEXT_HTML, LINK_REL_IDE_URL));

        // add workspace channel links
        final Link workspaceChannelLink = createLink("GET",
                                                     serviceContext.getBaseUriBuilder()
                                                                   .path("ws")
                                                                   .path(workspace.getId())
                                                                   .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                                   .build()
                                                                   .toString(),
                                                     null);
        final LinkParameter channelParameter = newDto(LinkParameter.class).withName("channel")
                                                                          .withRequired(true);

        links.add(cloneDto(workspaceChannelLink).withRel(LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL)
                                                .withParameters(singletonList(
                                                        cloneDto(channelParameter).withDefaultValue("workspace:" + workspace.getId()))));

        links.add(cloneDto(workspaceChannelLink).withRel(LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL)
                                                .withParameters(singletonList(cloneDto(channelParameter)
                                                                                      .withDefaultValue(format(ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE,
                                                                                                               workspace.getId())))));

        links.add(cloneDto(workspaceChannelLink).withRel(LINK_REL_ENVIRONMENT_STATUS_CHANNEL)
                                                .withParameters(singletonList(cloneDto(channelParameter)
                                                                                      .withDefaultValue(format(ENVIRONMENT_STATUS_CHANNEL_TEMPLATE,
                                                                                                               workspace.getId())))));

        // add links for running workspace
        injectRuntimeLinks(workspace, ideUri, uriBuilder);
        return workspace.withLinks(links);
    }

    public SnapshotDto injectLinks(SnapshotDto snapshotDto, ServiceContext serviceContext) {
        final UriBuilder uriBuilder = serviceContext.getServiceUriBuilder();
        final Link machineLink = createLink("GET",
                                            serviceContext.getBaseUriBuilder()
                                                          .path("/machine/{id}")
                                                          .build(snapshotDto.getId())
                                                          .toString(),
                                            APPLICATION_JSON,
                                            "get machine");
        final Link workspaceLink = createLink("GET",
                                              uriBuilder.clone()
                                                        .path(WorkspaceService.class, "getByKey")
                                                        .build(snapshotDto.getWorkspaceId())
                                                        .toString(),
                                              APPLICATION_JSON,
                                              LIN_REL_GET_WORKSPACE);
        final Link workspaceSnapshotLink = createLink("GET",
                                                      uriBuilder.clone()
                                                                .path(WorkspaceService.class, "getSnapshot")
                                                                .build(snapshotDto.getWorkspaceId())
                                                                .toString(),
                                                      APPLICATION_JSON,
                                                      LINK_REL_SELF);
        return snapshotDto.withLinks(asList(machineLink, workspaceLink, workspaceSnapshotLink));
    }

    protected void injectRuntimeLinks(WorkspaceDto workspace, URI ideUri, UriBuilder uriBuilder) {
        final WorkspaceRuntimeDto runtime = workspace.getRuntime();
        // add links for running workspace
        if (workspace.getStatus() == RUNNING && runtime != null) {
            runtime.getLinks()
                   .add(createLink("DELETE",
                                   uriBuilder.clone()
                                             .path(WorkspaceService.class, "stop")
                                             .build(workspace.getId())
                                             .toString(),
                                   LINK_REL_STOP_WORKSPACE));

            final MachineDto devMachine = runtime.getDevMachine();
            if (devMachine != null) {
                final Collection<ServerDto> servers = devMachine.getRuntime()
                                                                .getServers()
                                                                .values();
                servers.stream()
                       .filter(server -> WSAGENT_REFERENCE.equals(server.getRef()))
                       .findAny()
                       .ifPresent(wsAgent -> {
                           runtime.getLinks()
                                  .add(createLink("GET",
                                                  wsAgent.getUrl(),
                                                  WSAGENT_REFERENCE));
                           runtime.getLinks()
                                  .add(createLink("GET",
                                                  UriBuilder.fromUri(wsAgent.getUrl())
                                                            .path("ws")
                                                            .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                            .build()
                                                            .toString(),
                                                  WSAGENT_WEBSOCKET_REFERENCE));
                           devMachine.getLinks()
                                     .add(createLink("GET",
                                                     UriBuilder.fromUri(wsAgent.getUrl())
                                                               .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                               .path("/ws")
                                                               .build()
                                                               .toString(),
                                                     WSAGENT_WEBSOCKET_REFERENCE));
                       });

                servers.stream()
                       .filter(server -> TERMINAL_REFERENCE.equals(server.getRef()))
                       .findAny()
                       .ifPresent(terminal -> devMachine.getLinks()
                                                        .add(createLink("GET",
                                                                        UriBuilder.fromUri(terminal.getUrl())
                                                                                  .scheme("https".equals(ideUri.getScheme()) ? "wss"
                                                                                                                             : "ws")
                                                                                  .path("/pty")
                                                                                  .build()
                                                                                  .toString(),
                                                                        TERMINAL_REFERENCE)));
            }
        }
    }

    public MachineDto injectMachineLinks(MachineDto machine, ServiceContext serviceContext) {
        return machineLinksInjector.injectLinks(machine, serviceContext);
    }
}

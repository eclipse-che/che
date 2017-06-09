/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_STATUS_CHANNEL_TEMPLATE;
import static org.eclipse.che.api.machine.shared.Constants.EXEC_AGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL;
import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link MachineLinksInjector}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class MachineServiceLinksInjectorTest {
    private static final String URI_BASE = "http://localhost:8080";

    @Mock
    private ServiceContext        serviceContextMock;
    @Mock
    private MachineRuntimeInfoDto machineRuntimeInfoDtoMock;
    @Mock
    private MachineConfigDto      machineConfigDtoMock;
    @Mock
    private ServerDto             terminalServerMock;

    @Mock
    private ServerDto             execAgentServerMock;


    private MachineLinksInjector machineLinksInjector;

    @BeforeMethod
    public void setUp() throws Exception {
        machineLinksInjector = new MachineLinksInjector();
        final UriBuilder uriBuilder = new UriBuilderImpl();
        uriBuilder.uri(URI_BASE);
        when(serviceContextMock.getServiceUriBuilder()).thenReturn(uriBuilder);
        when(serviceContextMock.getBaseUriBuilder()).thenReturn(uriBuilder);
    }

    @Test
    public void shouldInjectLinksIntoMachineDto() {
        when(terminalServerMock.getRef()).thenReturn(TERMINAL_REFERENCE);
        when(terminalServerMock.getUrl()).thenReturn(URI_BASE + "/pty");
        when(execAgentServerMock.getRef()).thenReturn(EXEC_AGENT_REFERENCE);
        when(execAgentServerMock.getUrl()).thenReturn(URI_BASE + "/connect");
        when(machineRuntimeInfoDtoMock.getServers()).thenReturn(ImmutableMap.of(TERMINAL_REFERENCE, terminalServerMock,
                                                                                EXEC_AGENT_REFERENCE, execAgentServerMock));
        final MachineDto machineDto = DtoFactory.newDto(MachineDto.class)
                                                .withId("id")
                                                .withWorkspaceId("wsId")
                                                .withConfig(machineConfigDtoMock)
                                                .withRuntime(machineRuntimeInfoDtoMock);
        machineLinksInjector.injectLinks(machineDto, serviceContextMock);
        final Set<Pair<String, String>> links = machineDto.getLinks()
                                                          .stream()
                                                          .map(link -> Pair.of(link.getMethod(), link.getRel()))
                                                          .collect(Collectors.toSet());
        final Set<Pair<String, String>> expectedLinks = new HashSet<>(asList(Pair.of("GET", TERMINAL_REFERENCE),
                                                                             Pair.of("GET", EXEC_AGENT_REFERENCE),
                                                                             Pair.of("GET", LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL),
                                                                             Pair.of("GET", ENVIRONMENT_STATUS_CHANNEL_TEMPLATE)));

        assertEquals(links, expectedLinks, "Difference " + Sets.symmetricDifference(links, expectedLinks) + "\n");
    }
}

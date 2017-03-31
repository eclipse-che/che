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

import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.OldMachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.OldServerDto;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import javax.ws.rs.core.UriBuilder;

import static org.mockito.Mockito.when;

/**
 * Tests for {@link MachineLinksInjector}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class MachineServiceLinksInjectorTest {
    private static final String URI_BASE = "http://localhost:8080";

    @Mock
    private ServiceContext      serviceContextMock;
    @Mock
    private MachineDto          machineRuntimeInfoDtoMock;
    @Mock
    private OldMachineConfigDto machineConfigDtoMock;
    @Mock
    private OldServerDto        terminalServerMock;

    @Mock
    private OldServerDto execAgentServerMock;


    private MachineLinksInjector machineLinksInjector;

    @BeforeMethod
    public void setUp() throws Exception {
        machineLinksInjector = new MachineLinksInjector();
        final UriBuilder uriBuilder = new UriBuilderImpl();
        uriBuilder.uri(URI_BASE);
        when(serviceContextMock.getServiceUriBuilder()).thenReturn(uriBuilder);
        when(serviceContextMock.getBaseUriBuilder()).thenReturn(uriBuilder);
    }

//    @Test
//    public void shouldInjectLinksIntoMachineDto() {
//        when(terminalServerMock.getRef()).thenReturn(TERMINAL_REFERENCE);
//        when(terminalServerMock.getUrl()).thenReturn(URI_BASE + "/pty");
//        when(execAgentServerMock.getRef()).thenReturn(EXEC_AGENT_REFERENCE);
//        when(execAgentServerMock.getUrl()).thenReturn(URI_BASE + "/connect");
//        when(machineRuntimeInfoDtoMock.getServers()).thenReturn(ImmutableMap.of(TERMINAL_REFERENCE, terminalServerMock,
//                                                                                EXEC_AGENT_REFERENCE, execAgentServerMock));
//        final OldMachineDto machineDto = DtoFactory.newDto(OldMachineDto.class)
//                                                .withId("id")
//                                                .withWorkspaceId("wsId")
//                                                .withConfig(machineConfigDtoMock)
//                                                .withRuntime(machineRuntimeInfoDtoMock);
//        machineLinksInjector.injectLinks(machineDto, serviceContextMock);
//        final Set<Pair<String, String>> links = machineDto.getLinks()
//                                                          .stream()
//                                                          .map(link -> Pair.of(link.getMethod(), link.getRel()))
//                                                          .collect(Collectors.toSet());
//        final Set<Pair<String, String>> expectedLinks = new HashSet<>(asList(Pair.of("GET", TERMINAL_REFERENCE),
//                                                                             Pair.of("GET", EXEC_AGENT_REFERENCE),
//                                                                             Pair.of("GET", LINK_REL_SELF),
//                                                                             Pair.of("GET", LINK_REL_GET_MACHINES),
//                                                                             Pair.of("POST", LINK_REL_EXECUTE_COMMAND),
//                                                                             Pair.of("DELETE", LINK_REL_DESTROY_MACHINE),
//                                                                             Pair.of("GET", LINK_REL_GET_PROCESSES),
//                                                                             Pair.of("GET", LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL),
//                                                                             Pair.of("GET", ENVIRONMENT_STATUS_CHANNEL_TEMPLATE)));
//
//        assertEquals(links, expectedLinks, "Difference " + Sets.symmetricDifference(links, expectedLinks) + "\n");
//    }
//
//    @Test
//    public void shouldInjectLinksIntoMachineProcessDto() {
//        final MachineProcessDto machineProcessDto = DtoFactory.newDto(MachineProcessDto.class);
//        machineLinksInjector.injectLinks(machineProcessDto, "workspaceId", "machineId", serviceContextMock);
//        final Set<Pair<String, String>> links = machineProcessDto.getLinks()
//                                                                 .stream()
//                                                                 .map(link -> Pair.of(link.getMethod(), link.getRel()))
//                                                                 .collect(Collectors.toSet());
//        final Set<Pair<String, String>> expectedLinks = ImmutableSet.of(Pair.of("DELETE", LINK_REL_STOP_PROCESS),
//                                                                        Pair.of("GET", LINK_REL_GET_PROCESS_LOGS),
//                                                                        Pair.of("GET", LINK_REL_GET_PROCESSES));
//
//        assertEquals(links, expectedLinks, "Difference " + Sets.symmetricDifference(links, expectedLinks) + "\n");
//    }
}

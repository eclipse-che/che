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
package org.eclipse.che.machine.authentication.server;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.machine.authentication.shared.dto.MachineTokenDto;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anton Korneta.
 */
@Listeners(MockitoTestNGListener.class)
public class MachineServiceAuthLinksInjectorTest {
    private static final String URI_BASE     = "http://localhost:8080";
    private static final String API_ENDPOINT = URI_BASE + "/api";

    @Mock
    private ServiceContext         serviceContextMock;
    @Mock
    private MachineRuntimeInfoDto  machineRuntimeInfoDtoMock;
    @Mock
    private HttpJsonRequestFactory requestFactoryMock;
    @Mock
    private ServerDto              serverDtoMock;
    private HttpJsonRequest        requestMock;

    private MachineAuthLinksInjector machineLinksInjector;

    @BeforeMethod
    public void setUp() throws Exception {
        final UriBuilder uriBuilder = new UriBuilderImpl();
        uriBuilder.uri(URI_BASE);
        requestMock  = mock(HttpJsonRequest.class, new SelfReturningAnswer());
        when(requestFactoryMock.fromUrl(anyString())).thenReturn(requestMock);
        machineLinksInjector = new MachineAuthLinksInjector(API_ENDPOINT, requestFactoryMock);
        when(serviceContextMock.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(serverDtoMock.getRef()).thenReturn(TERMINAL_REFERENCE);
        when(serverDtoMock.getUrl()).thenReturn(URI_BASE);
        when(machineRuntimeInfoDtoMock.getServers()).thenReturn(ImmutableMap.of("server", serverDtoMock));
    }

    @Test
    public void shouldInjectTerminalLinkWithMachineToken() throws Exception {
        final String machineToken = "machine12";
        final MachineTokenDto tokenDto = DtoFactory.newDto(MachineTokenDto.class)
                                                          .withMachineToken(machineToken);
        final HttpJsonResponse responseMock = mock(HttpJsonResponse.class);
        when(responseMock.asDto(MachineTokenDto.class)).thenReturn(tokenDto);
        when(requestMock.request()).thenReturn(responseMock);
        final MachineDto machineDto = DtoFactory.newDto(MachineDto.class)
                                                .withId("id")
                                                .withWorkspaceId("wsId")
                                                .withRuntime(machineRuntimeInfoDtoMock);
        final List<Link> links = new ArrayList<>();
        machineLinksInjector.injectTerminalLink(machineDto, serviceContextMock, links);

        final Link resultTerminalLink = links.get(0);
        assertEquals(1, links.size());
        assertEquals(resultTerminalLink.getRel(), TERMINAL_REFERENCE);
        assertEquals(resultTerminalLink.getHref(), "ws://localhost:8080/pty?token=" + machineToken);
    }

    @Test
    public void shouldInjectTerminalLinkWithoutMachineToken() throws Exception {
        when(requestMock.request()).thenThrow(new IOException("ioEx"));
        final MachineDto machineDto = DtoFactory.newDto(MachineDto.class)
                                                .withId("id")
                                                .withWorkspaceId("wsId")
                                                .withRuntime(machineRuntimeInfoDtoMock);
        final List<Link> links = new ArrayList<>();
        machineLinksInjector.injectTerminalLink(machineDto, serviceContextMock, links);

        final Link resultTerminalLink = links.get(0);
        assertEquals(1, links.size());
        assertEquals(resultTerminalLink.getRel(), TERMINAL_REFERENCE);
    }

    @Test
    public void shouldNotInjectTerminalLinkWhenNoRuntimeMachine() {
        final MachineDto machineDto = DtoFactory.newDto(MachineDto.class)
                                                .withRuntime(null);
        final List<Link> links = new ArrayList<>();

        machineLinksInjector.injectTerminalLink(machineDto, serviceContextMock, links);

        assertTrue(links.isEmpty());
    }
}

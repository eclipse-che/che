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
package org.eclipse.che.ide.extension.machine.client.machine;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineItemTest {

    private final static String SOME_TEXT = "someText";

    @Mock
    private MachineDto                  descriptor;
    @Mock
    private MachineConfigDto            machineConfig;
    @Mock
    private MachineRuntimeInfoDto       machineRuntimeDto;
    @Mock
    private ServerDto                   serverDescriptor;
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private EntityFactory               entityFactory;
    @Mock
    private AppContext                  appContext;

    private MachineItem machine;

    @Before
    public void setUp() {
        Map<String, ServerDto> servers = new HashMap<>();
        servers.put(SOME_TEXT, serverDescriptor);

        when(descriptor.getRuntime()).thenReturn(machineRuntimeDto);
        when(descriptor.getConfig()).thenReturn(machineConfig);
        when(serverDescriptor.getAddress()).thenReturn(SOME_TEXT);
        when(machineRuntimeDto.getServers()).thenReturn(servers);

        machine = new MachineItem(descriptor);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(descriptor).getLinks();
        verify(descriptor).getConfig();
    }

    @Test
    public void displayNameShouldBeReturned() {
        machine.getDisplayName();

        verify(machineConfig).getName();
    }

    @Test
    public void idShouldBeReturned() {
        machine.getId();

        verify(descriptor).getId();
    }

    @Test
    public void stateShouldBeReturned() {
        machine.getStatus();

        verify(descriptor).getStatus();
    }

    @Test
    public void typeShouldBeReturned() {
        machine.getType();

        verify(machineConfig).getType();
    }

    @Test
    public void boundedStateShouldBeReturned() {
        machine.isDev();

        verify(machineConfig).isDev();
    }

    @Test
    public void shouldReturnTerminalUrl() {
        String terminalHref = "terminalHref";
        Link someLink = mock(Link.class);
        Link terminalLink = mock(Link.class);
        List<Link> links = new ArrayList<>(2);
        links.add(someLink);
        links.add(terminalLink);
        when(terminalLink.getHref()).thenReturn(terminalHref);
        when(terminalLink.getRel()).thenReturn(TERMINAL_REFERENCE);
        when(descriptor.getLinks()).thenReturn(links);

        machine = new MachineItem(descriptor);
        String terminalUrl = machine.getTerminalUrl();

        assertEquals(terminalHref, terminalUrl);
    }

    @Test
    public void shouldReturnProperties() {
        Map<String, String> properties = Collections.emptyMap();
        when(machineRuntimeDto.getProperties()).thenReturn(properties);

        machine = new MachineItem(descriptor);
        Map<String, String> result = machine.getProperties();

        assertEquals(properties, result);
    }

    @Test
    public void shouldAvoidNPEWhenMachineRuntimeIsNull() {
        when(descriptor.getRuntime()).thenReturn(null);
        machine = new MachineItem(descriptor);

        Map<String, String> result = machine.getProperties();

        assertNull(result);
    }
}

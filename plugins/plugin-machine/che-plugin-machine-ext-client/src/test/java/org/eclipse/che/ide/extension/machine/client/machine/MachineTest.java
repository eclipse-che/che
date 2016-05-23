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
package org.eclipse.che.ide.extension.machine.client.machine;

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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineTest {

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

    private Machine machine;

    @Before
    public void setUp() {
        Map<String, ServerDto> servers = new HashMap<>();
        servers.put(SOME_TEXT, serverDescriptor);

        machine = new Machine(locale, entityFactory, descriptor);

        when(descriptor.getRuntime()).thenReturn(machineRuntimeDto);
        when(descriptor.getConfig()).thenReturn(machineConfig);
        when(serverDescriptor.getAddress()).thenReturn(SOME_TEXT);
        when(machineRuntimeDto.getServers()).thenReturn(servers);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).tabInfo();
    }

    @Test
    public void defaultActiveTabShouldBeReturned() {
        when(locale.tabInfo()).thenReturn(SOME_TEXT);
        machine = new Machine(locale, entityFactory, descriptor);

        String tabName = machine.getActiveTabName();

        assertThat(tabName, equalTo(SOME_TEXT));
    }

    @Test
    public void activeTabNameShouldBeSet() {
        machine.setActiveTabName(SOME_TEXT);

        String tabName = machine.getActiveTabName();

        assertThat(tabName, equalTo(SOME_TEXT));
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
}

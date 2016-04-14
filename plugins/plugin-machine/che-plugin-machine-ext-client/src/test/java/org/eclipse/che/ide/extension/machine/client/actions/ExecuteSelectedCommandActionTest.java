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
package org.eclipse.che.ide.extension.machine.client.actions;

import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteSelectedCommandActionTest {

    @Mock
    private MachineLocalizationConstant localizationConstants;
    @Mock
    private MachineResources            resources;
    @Mock
    private SelectCommandComboBoxReady  selectCommandAction;
    @Mock
    private CommandManager              commandManager;
    @Mock
    private ActionEvent                 event;

    @Mock
    private CommandConfiguration command;
    @Mock
    private MachineDto           machine;

    @InjectMocks
    private ExecuteSelectedCommandAction action;

    @Before
    public void setUp() throws Exception {
        when(selectCommandAction.getSelectedCommand()).thenReturn(command);
        when(selectCommandAction.getSelectedMachine()).thenReturn(machine);
    }

    @Test
    public void constructorShouldBeVerified() throws Exception {
        verify(localizationConstants).executeSelectedCommandControlTitle();
        verify(localizationConstants).executeSelectedCommandControlDescription();
        verify(resources).execute();
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        action.actionPerformed(event);

        verify(commandManager).execute(command, machine);
    }
}

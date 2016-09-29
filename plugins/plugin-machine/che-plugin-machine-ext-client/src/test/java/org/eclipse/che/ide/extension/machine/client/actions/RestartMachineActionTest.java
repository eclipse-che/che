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

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel.MachinePanelPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RestartMachineActionTest {

    private static final String SOME_TEXT = "someText";

    //constructors mocks
    @Mock
    private MachinePanelPresenter       panelPresenter;
    @Mock
    private MachineManager              machineManager;
    @Mock
    private MachineLocalizationConstant locale;

    //additional mocks
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ActionEvent      event;
    @Mock
    private MachineEntity    machine;
    @Mock
    private MachineConfigDto machineConfig;

    @InjectMocks
    private RestartMachineAction action;

    @Before
    public void setUp() {
        when(panelPresenter.getSelectedMachineState()).thenReturn(machine);
        when(machine.getConfig()).thenReturn(machineConfig);
    }

    @Test
    public void actionShouldBeUpdatedWhenSelectedMachineIsNotNull() {
        when(machineConfig.getName()).thenReturn(SOME_TEXT);
        when(locale.machineRestartTextByName(SOME_TEXT)).thenReturn(SOME_TEXT);

        action.updateInPerspective(event);

        verify(panelPresenter).getSelectedMachineState();
        verify(panelPresenter).isMachineRunning();

        verify(machineConfig).getName();
        verify(locale).machineRestartTextByName(SOME_TEXT);

        verify(event.getPresentation()).setText(SOME_TEXT);
    }

    @Test
    public void actionShouldBeUpdatedWhenSelectedMachineIsNull() {
        reset(locale);
        when(panelPresenter.getSelectedMachineState()).thenReturn(null);
        when(locale.controlMachineRestartText()).thenReturn(SOME_TEXT);

        action.updateInPerspective(event);

        verify(panelPresenter).getSelectedMachineState();
        verify(event.getPresentation()).setEnabled(false);

        verify(machineConfig, never()).getName();
        verify(locale, never()).machineRestartTextByName(SOME_TEXT);

        verify(locale).controlMachineRestartText();

        verify(event.getPresentation()).setText(SOME_TEXT);
    }

    @Test
    public void actionShouldBePerformed() {
        action.updateInPerspective(event);

        action.actionPerformed(event);

        verify(machineManager).restartMachine(machine);
    }
}

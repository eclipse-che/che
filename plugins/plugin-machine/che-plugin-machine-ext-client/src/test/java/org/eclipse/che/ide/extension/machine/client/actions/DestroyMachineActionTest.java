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

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.machine.shared.dto.MachineStateDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel.MachinePanelPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class DestroyMachineActionTest {
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private MachinePanelPresenter       panelPresenter;
    @Mock
    private ActionEvent                 event;
    @Mock
    private MachineManager              machineManager;
    @Mock
    private AnalyticsEventLogger        eventLogger;
    @Mock
    private DialogFactory               dialogFactory;

    @Mock
    private MachineStateDto machineState;

    @InjectMocks
    private DestroyMachineAction action;

    @Before
    public void setUp() {
        when(panelPresenter.getSelectedMachineState()).thenReturn(machineState);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).machineDestroyTitle();
        verify(locale).machineDestroyDescription();
    }

    @Test
    public void actionShouldBePerformed() {
        action.actionPerformed(event);

        verify(machineManager).destroyMachine(eq(machineState));
    }

//    @Test
//    public void devMachineShouldNotBeDestroyed() {
//        when(machineState.isDev()).thenReturn(true);
//        MessageDialog dialog = mock(MessageDialog.class);
//        when(dialogFactory.createMessageDialog(anyString(), anyString(), any(ConfirmCallback.class))).thenReturn(dialog);
//
//        action.actionPerformed(event);
//
//        verify(dialogFactory).createMessageDialog(anyString(), anyString(), any(ConfirmCallback.class));
//        verify(dialog).show();
//        verify(machineManager, never()).destroyMachine(eq(machineState));
//    }
}

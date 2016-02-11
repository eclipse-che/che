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

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class NewTerminalActionTest {
    private static final String MACHINE_ID = "machineID";

    @Mock
    private AppContext                  appContext;
    @Mock
    private WorkspaceAgent              workspaceAgent;
    @Mock
    private ConsolesPanelPresenter      consolesPanelPresenter;
    @Mock
    private AnalyticsEventLogger        eventLogger;
    @Mock
    private MachineLocalizationConstant locale;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ActionEvent actionEvent;

    @InjectMocks
    private NewTerminalAction action;

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).newTerminalTitle();
        verify(locale).newTerminalDescription();
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        when(appContext.getDevMachineId()).thenReturn(MACHINE_ID);

        action.actionPerformed(actionEvent);

        verify(eventLogger).log(action);
        verify(appContext).getDevMachineId();
        verify(consolesPanelPresenter).onAddTerminal(eq(MACHINE_ID));
        verify(workspaceAgent).setActivePart(eq(consolesPanelPresenter));
    }

    @Test
    public void actionShouldBeEnabledWhenDevMachineIsNotNull() throws Exception {
        when(appContext.getDevMachineId()).thenReturn(MACHINE_ID);

        action.updateInPerspective(actionEvent);

        verify(actionEvent.getPresentation()).setEnabled(eq(true));
    }

    @Test
    public void actionShouldBeDisabledWhenDevMachineIsNull() throws Exception {
        when(appContext.getDevMachineId()).thenReturn(null);

        action.updateInPerspective(actionEvent);

        verify(actionEvent.getPresentation()).setEnabled(eq(false));

        verify(eventLogger, never()).log(action);
        verify(consolesPanelPresenter, never()).onAddTerminal(eq(MACHINE_ID));
        verify(workspaceAgent, never()).setActivePart(eq(consolesPanelPresenter));
    }
}

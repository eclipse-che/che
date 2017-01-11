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
package org.eclipse.che.ide.extension.machine.client.perspective.terminal.container;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class TerminalContainerTest {

    //constructor mocks
    @Mock
    private TerminalContainerView view;
    @Mock
    private TerminalFactory       terminalFactory;

    @Mock
    private MachineEntity     machineState;
    @Mock
    private TerminalPresenter terminal;
    @Mock
    private AcceptsOneWidget  oneWidget;
    @Mock
    private MachineStateEvent machineStateEvent;

    @InjectMocks
    private TerminalContainer container;

    @Test
    public void terminalShouldBeAdded() {
        when(terminalFactory.create(machineState, container)).thenReturn(terminal);

        container.addOrShowTerminal(machineState);

        verify(terminalFactory).create(machineState, container);
        verify(view).addTerminal(terminal);

        verify(terminal, never()).connect();
        verify(view, never()).showTerminal(terminal);
    }

    @Test
    public void terminalShouldBeShown() {
        when(terminalFactory.create(machineState, container)).thenReturn(terminal);

        container.addOrShowTerminal(machineState);
        reset(view, terminalFactory);

        container.addOrShowTerminal(machineState);

        verify(terminal).connect();
        verify(view).showTerminal(terminal);

        verify(terminalFactory, never()).create(machineState, container);
        verify(view, never()).addTerminal(terminal);
    }

    @Test
    public void visibilityShouldBeChanged() {
        container.setVisible(true);

        verify(view).setVisible(true);
    }

    @Test
    public void widgetShouldBeSetToContainer() {
        container.go(oneWidget);

        verify(oneWidget).setWidget(view);
    }

    @Test
    public void onMachineShouldBeDestroyed() {
        when(terminalFactory.create(machineState, container)).thenReturn(terminal);

        container.addOrShowTerminal(machineState);

        verify(terminalFactory).create(machineState, container);
        reset(terminalFactory);

        when(terminalFactory.create(machineState, container)).thenReturn(terminal);

        container.onMachineDestroyed(machineStateEvent);

        container.addOrShowTerminal(machineState);

        verify(terminalFactory).create(machineState, container);
    }

}

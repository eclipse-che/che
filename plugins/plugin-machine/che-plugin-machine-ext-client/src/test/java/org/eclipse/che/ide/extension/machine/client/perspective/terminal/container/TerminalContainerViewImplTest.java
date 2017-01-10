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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class TerminalContainerViewImplTest {

    @Mock
    private TerminalPresenter terminal1;
    @Mock
    private TerminalPresenter terminal2;
    @Mock
    private IsWidget          widget;

    @InjectMocks
    private TerminalContainerViewImpl view;

    @Test
    public void terminalShouldBeAdded() {
        when(terminal1.getView()).thenReturn(widget);

        view.addTerminal(terminal1);

        verify(terminal1).getView();
        verify(view.container).add(widget);
    }

    @Test
    public void firstTerminalShouldBeShown() {
        view.addTerminal(terminal1);
        view.addTerminal(terminal2);

        view.showTerminal(terminal1);

        verify(terminal1).setVisible(false);
        verify(terminal2).setVisible(false);
        verify(terminal1).setVisible(true);
    }

}
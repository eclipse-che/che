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
package org.eclipse.che.ide.extension.machine.client.processes.container;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.TerminalsPanelPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ConsolesContainerPresenterTest {
    @Mock
    private MachineLocalizationConstant         localizationConstant;
    @Mock
    private MachineResources                    resources;
    @Mock
    private ConsolesContainerView                   view;
    @Mock
    private ConsolesPanelPresenter consolesPanelPresenter;
    @Mock
    private TerminalsPanelPresenter terminalsPanelPresenter;

    @InjectMocks
    private ConsolesContainerPresenter presenter;

    @Before
    public void setUp() {
        presenter = new ConsolesContainerPresenter(view, consolesPanelPresenter, terminalsPanelPresenter, localizationConstant, resources);
    }

    @Test
    public void shouldReturnTitle() throws Exception {
        reset(localizationConstant);

        presenter.getTitle();

        verify(localizationConstant).viewConsolesTitle();
    }

    @Test
    public void shouldReturnTitleToolTip() throws Exception {
        presenter.getTitleToolTip();

        verify(localizationConstant).viewProcessesTooltip();
    }

    @Test
    public void shouldSetViewVisible() throws Exception {
        presenter.setVisible(true);

        verify(view).setVisible(eq(true));
    }

    @Test
    public void shouldReturnTitleSVGImage() {
        presenter.getTitleImage();

        verify(resources).terminal();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);
        SimplePanel processesContainer = mock(SimplePanel.class);
        when(view.getProcessesContainer()).thenReturn(processesContainer);

        presenter.go(container);

        verify(view).getProcessesContainer();
        verify(container).setWidget(eq(view));
        verify(consolesPanelPresenter).go(eq(processesContainer));
    }

    @Test
    public void shouldApplyDefaultMode() throws Exception {
        SimplePanel processesContainer = mock(SimplePanel.class);
        when(view.getProcessesContainer()).thenReturn(processesContainer);

        presenter.onDefaultModeClick();

        verify(view).applyDefaultMode();
        verify(view).getProcessesContainer();
        verify(consolesPanelPresenter).go(eq(processesContainer));
    }

    @Test
    public void shouldSplitVerticallyConsolesArea() throws Exception {
        SimplePanel processesContainer = mock(SimplePanel.class);
        SimplePanel terminalsContainer = mock(SimplePanel.class);
        when(view.getProcessesContainer()).thenReturn(processesContainer);
        when(view.getTerminalsContainer()).thenReturn(terminalsContainer);

        presenter.onSplitVerticallyClick();

        verify(view).splitVertically();
        verify(view).getProcessesContainer();
        verify(view).getTerminalsContainer();
        verify(consolesPanelPresenter).go(eq(processesContainer));
        verify(terminalsPanelPresenter).go(eq(terminalsContainer));
    }

    @Test
    public void shouldSplitHorizontallyConsolesArea() throws Exception {
        SimplePanel processesContainer = mock(SimplePanel.class);
        SimplePanel terminalsContainer = mock(SimplePanel.class);
        when(view.getProcessesContainer()).thenReturn(processesContainer);
        when(view.getTerminalsContainer()).thenReturn(terminalsContainer);

        presenter.onSplitHorizontallyClick();

        verify(view).splitHorizontally();
        verify(view).getProcessesContainer();
        verify(view).getTerminalsContainer();
        verify(consolesPanelPresenter).go(eq(processesContainer));
        verify(terminalsPanelPresenter).go(eq(terminalsContainer));
    }

}

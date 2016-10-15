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
package org.eclipse.che.ide.ext.plugins.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class GwtChePagePresenterTest {

    private static final String GWT_MODULE          = "org.eclipse.CHE";
    private static final String CODE_SERVER_ADDRESS = "0.0.0.0";
    private static final String CHE_CLASS_PATH      = "class_path";
    private static final String COMMAND_LINE        = "java -classpath \"" + CHE_CLASS_PATH +
                                                      "\" com.google.gwt.dev.codeserver.CodeServer " + GWT_MODULE +
                                                      " -noincremental -noprecompile -bindAddress " + CODE_SERVER_ADDRESS;

    @Mock
    private GwtCheCommandPageView view;
    @Mock
    private CommandImpl           command;

    @InjectMocks
    private GwtCheCommandPagePresenter presenter;

    @Before
    public void setUp() {
        when(command.getCommandLine()).thenReturn(COMMAND_LINE);

        presenter.resetFrom(command);
    }

    @Test
    public void testResetting() throws Exception {
        verify(command).getCommandLine();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        presenter.go(container);

        verify(container).setWidget(eq(view));
        verify(view).setGwtModule(eq(GWT_MODULE));
        verify(view).setCodeServerAddress(eq(CODE_SERVER_ADDRESS));
        verify(view).setClassPath(eq(CHE_CLASS_PATH));
    }

    @Test
    public void testOnGwtModuleChanged() throws Exception {
        when(view.getGwtModule()).thenReturn(GWT_MODULE);

        final CommandPage.DirtyStateListener listener = mock(CommandPage.DirtyStateListener.class);
        presenter.setDirtyStateListener(listener);

        presenter.onGwtModuleChanged();

        verify(view).getGwtModule();
        verify(command).setCommandLine(eq(COMMAND_LINE));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnCodeServerAddressChanged() throws Exception {
        when(view.getCodeServerAddress()).thenReturn(CODE_SERVER_ADDRESS);

        final CommandPage.DirtyStateListener listener = mock(CommandPage.DirtyStateListener.class);
        presenter.setDirtyStateListener(listener);

        presenter.onCodeServerAddressChanged();

        verify(view).getCodeServerAddress();
        verify(command).setCommandLine(eq(COMMAND_LINE));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnClassPathChanged() throws Exception {
        when(view.getClassPath()).thenReturn(CHE_CLASS_PATH);

        final CommandPage.DirtyStateListener listener = mock(CommandPage.DirtyStateListener.class);
        presenter.setDirtyStateListener(listener);

        presenter.onClassPathChanged();

        verify(view).getClassPath();
        verify(command).setCommandLine(eq(COMMAND_LINE));
        verify(listener).onDirtyStateChanged();
    }
}
